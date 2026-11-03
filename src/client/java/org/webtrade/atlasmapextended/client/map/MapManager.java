package org.webtrade.atlasmapextended.client.map;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;
import org.webtrade.atlasmapextended.client.data.Storage;
import org.webtrade.atlasmapextended.client.data.WorldMeta;

import java.nio.file.Path;

/** Owns the map session of the world the player is currently in and keeps it up to date. */
public final class MapManager {
    private static final long SCAN_BUDGET_NS = 3_000_000L;

    private static MapSession live;
    private static String liveDim;
    private static final LongLinkedOpenHashSet queue = new LongLinkedOpenHashSet();
    private static int ticks;

    private MapManager() {
    }

    public static MapSession live() {
        return live;
    }

    public static String liveDim() {
        return liveDim;
    }

    public static void onJoin(Minecraft mc) {
        closeLive();
        WorldMeta meta = resolveMeta(mc);
        live = MapSession.open(meta);
        live.live = true;
        liveDim = null;
        queue.clear();
        live.saveMeta();
    }

    public static void onDisconnect() {
        closeLive();
    }

    private static void closeLive() {
        if (live != null) {
            live.meta.lastPlayed = System.currentTimeMillis();
            live.saveMeta();
            live.saveMarkers();
            live.saveStrokes();
            live.close();
            live = null;
        }
        liveDim = null;
        queue.clear();
    }

    /** Opens a stored world for viewing. Reuses the live session if it is the same world. */
    public static MapSession openForViewing(WorldMeta meta) {
        if (live != null && live.meta.id.equals(meta.id)) {
            return live;
        }
        return MapSession.open(meta);
    }

    public static void onChunkLoad(ClientLevel level, LevelChunk chunk) {
        if (live != null && level == Minecraft.getInstance().level) {
            queue.add(MapLayer.pack(chunk.getPos().x, chunk.getPos().z));
        }
    }

    public static void tick(Minecraft mc) {
        if (live == null || mc.level == null || mc.player == null) {
            return;
        }
        ClientLevel level = mc.level;
        String dim = level.dimension().location().toString();
        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        if (!dim.equals(liveDim)) {
            liveDim = dim;
            queue.clear();
            int r = mc.options.getEffectiveRenderDistance();
            enqueueAround(pcx, pcz, r);
        }
        ticks++;
        if (ticks % 40 == 0) {
            enqueueAround(pcx, pcz, 2);
        }

        MapLayer layer = live.layer(dim);
        long start = System.nanoTime();
        while (!queue.isEmpty() && System.nanoTime() - start < SCAN_BUDGET_NS) {
            long k = queue.removeFirstLong();
            int cx = (int) (k >> 32);
            int cz = (int) k;
            if (level.hasChunk(cx, cz)) {
                LevelChunk chunk = level.getChunk(cx, cz);
                if (!chunk.isEmpty()) {
                    ChunkScanner.scan(level, chunk, layer);
                }
            }
        }
        layer.uploadDirty();

        if (ticks % 100 == 0) {
            live.meta.lastDim = dim;
            live.meta.lastX = mc.player.getX();
            live.meta.lastZ = mc.player.getZ();
            live.meta.lastPlayed = System.currentTimeMillis();
        }
        if (ticks % 1200 == 0) {
            live.saveTiles();
            live.saveMeta();
            live.unloadUnused();
        }
    }

    private static void enqueueAround(int cx, int cz, int r) {
        // spiral-ish order: closest chunks first
        for (int d = 0; d <= r; d++) {
            for (int x = -d; x <= d; x++) {
                for (int z = -d; z <= d; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == d) {
                        queue.add(MapLayer.pack(cx + x, cz + z));
                    }
                }
            }
        }
    }

    private static WorldMeta resolveMeta(Minecraft mc) {
        WorldMeta meta = new WorldMeta();
        IntegratedServer sp = mc.getSingleplayerServer();
        ServerData sd = mc.getCurrentServer();
        if (sp != null) {
            Path root = sp.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            String folder = root.getFileName() == null ? "world" : root.getFileName().toString();
            meta.id = "local_" + Storage.sanitize(folder);
            meta.name = sp.getWorldData().getLevelName();
            meta.address = folder;
            meta.type = WorldMeta.LOCAL;
        } else if (sd != null) {
            meta.id = "server_" + Storage.sanitize(sd.ip);
            meta.name = sd.name;
            meta.address = sd.ip;
            meta.type = WorldMeta.SERVER;
        } else {
            meta.id = "unknown";
            meta.name = "Unknown";
            meta.type = WorldMeta.SERVER;
        }
        WorldMeta old = Storage.readJson(Storage.worldDir(meta.id).resolve("meta.json"), WorldMeta.class, null);
        if (old != null) {
            meta.lastDim = old.lastDim;
            meta.lastX = old.lastX;
            meta.lastZ = old.lastZ;
        }
        meta.lastPlayed = System.currentTimeMillis();
        return meta;
    }
}
