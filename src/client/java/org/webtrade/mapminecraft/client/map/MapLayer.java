package org.webtrade.mapminecraft.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import org.webtrade.mapminecraft.client.data.Storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** All map tiles of one dimension of one world. Tiles are loaded lazily and unloaded when unused. */
public class MapLayer {
    private static final int LOADS_PER_FRAME = 6;
    private static final long UNLOAD_AFTER_MS = 90_000;
    private static int frameLoads;

    public final String dim;
    private final String key;
    private final Path dir;
    private final Map<Long, MapTile> tiles = new HashMap<>();
    private final Set<Long> onDisk = new HashSet<>();
    private final Set<Long> broken = new HashSet<>();

    MapLayer(String worldId, String dim, Path dir) {
        this.dim = dim;
        this.dir = dir;
        this.key = Storage.asciiId(worldId) + "/" + Storage.asciiId(dim);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> s = Files.list(dir)) {
                s.forEach(p -> {
                    String n = p.getFileName().toString();
                    if (n.endsWith(".png")) {
                        String[] parts = n.substring(0, n.length() - 4).split("_");
                        if (parts.length == 2) {
                            try {
                                onDisk.add(pack(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                });
            } catch (IOException e) {
                Storage.LOG.warn("Failed to list tiles in {}", dir, e);
            }
        }
    }

    public static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /** Called once per rendered frame to limit how many tiles may be decoded from disk. */
    public static void newFrame() {
        frameLoads = 0;
    }

    public boolean isEmpty() {
        return onDisk.isEmpty() && tiles.isEmpty();
    }

    /** Returns the tile for rendering, loading it from disk if the per-frame budget allows. */
    public MapTile getForRender(int rx, int rz) {
        long k = pack(rx, rz);
        MapTile t = tiles.get(k);
        if (t != null) {
            t.touch();
            return t;
        }
        if (onDisk.contains(k) && !broken.contains(k) && frameLoads < LOADS_PER_FRAME) {
            frameLoads++;
            return load(rx, rz);
        }
        return null;
    }

    /** Returns the tile for writing, creating it (or loading it) if needed. */
    public MapTile getOrCreate(int rx, int rz) {
        long k = pack(rx, rz);
        MapTile t = tiles.get(k);
        if (t == null && onDisk.contains(k) && !broken.contains(k)) {
            t = load(rx, rz);
        }
        if (t == null) {
            t = new MapTile(key, rx, rz, new NativeImage(MapTile.SIZE, MapTile.SIZE, true));
            tiles.put(k, t);
        }
        t.touch();
        return t;
    }

    private MapTile load(int rx, int rz) {
        long k = pack(rx, rz);
        Path file = dir.resolve(rx + "_" + rz + ".png");
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage img = NativeImage.read(in);
            if (img.getWidth() != MapTile.SIZE || img.getHeight() != MapTile.SIZE || img.format() != NativeImage.Format.RGBA) {
                img.close();
                broken.add(k);
                return null;
            }
            MapTile t = new MapTile(key, rx, rz, img);
            tiles.put(k, t);
            return t;
        } catch (IOException e) {
            Storage.LOG.warn("Failed to load map tile {}", file, e);
            broken.add(k);
            return null;
        }
    }

    /** Pushes changed pixels to the GPU. */
    public void uploadDirty() {
        for (MapTile t : tiles.values()) {
            t.upload();
        }
    }

    public void saveDirty() {
        for (MapTile t : tiles.values()) {
            if (t.diskDirty) {
                t.diskDirty = false;
                NativeImage copy = t.snapshot();
                Path file = dir.resolve(t.rx + "_" + t.rz + ".png");
                onDisk.add(pack(t.rx, t.rz));
                Storage.IO.execute(() -> {
                    try {
                        Files.createDirectories(dir);
                        copy.writeToFile(file);
                    } catch (IOException e) {
                        Storage.LOG.warn("Failed to save map tile {}", file, e);
                    } finally {
                        copy.close();
                    }
                });
            }
        }
    }

    /** Frees GPU memory of tiles that were not looked at for a while. */
    public void unloadUnused() {
        long now = System.currentTimeMillis();
        Iterator<MapTile> it = tiles.values().iterator();
        List<MapTile> removed = new ArrayList<>();
        while (it.hasNext()) {
            MapTile t = it.next();
            if (!t.diskDirty && now - t.lastUsed > UNLOAD_AFTER_MS) {
                it.remove();
                removed.add(t);
            }
        }
        removed.forEach(MapTile::release);
    }

    public void close() {
        saveDirty();
        tiles.values().forEach(MapTile::release);
        tiles.clear();
    }
}
