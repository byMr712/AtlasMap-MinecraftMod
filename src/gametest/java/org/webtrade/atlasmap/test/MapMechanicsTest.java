package org.webtrade.atlasmap.test;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import org.webtrade.atlasmap.client.data.Marker;
import org.webtrade.atlasmap.client.data.ModConfig;
import org.webtrade.atlasmap.client.data.Storage;
import org.webtrade.atlasmap.client.data.Stroke;
import org.webtrade.atlasmap.client.data.WorldMeta;
import org.webtrade.atlasmap.client.gui.ImageCache;
import org.webtrade.atlasmap.client.gui.MapView;
import org.webtrade.atlasmap.client.gui.T;
import org.webtrade.atlasmap.client.gui.Theme;
import org.webtrade.atlasmap.client.map.MapLayer;
import org.webtrade.atlasmap.client.map.MapManager;
import org.webtrade.atlasmap.client.map.MapSession;
import org.webtrade.atlasmap.client.map.MapTile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Functional checks for the mod's data layer, storage, map session and image pipeline. */
public class MapMechanicsTest implements FabricClientGameTest {
    private int checks;

    @Override
    public void runTest(ClientGameTestContext ctx) {
        ctx.runOnClient(mc -> {
            storage();
            markerCopy();
            strokePoints();
            themes();
            translations();
            viewTransforms();
            configPersistence();
        });

        try (TestSingleplayerContext sp = ctx.worldBuilder()
                .setUseConsistentSettings(false)
                .adjustSettings(s -> s.setAllowCommands(true))
                .create()) {
            ctx.waitTicks(400);
            ctx.runOnClient(mc -> liveMap());
            ctx.runOnClient(mc -> persistence());
            ctx.runOnClient(mc -> screenshots());
        }

        System.out.println("[AtlasMap mechanics] " + checks + " checks passed");
    }

    private void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError("[AtlasMap mechanics] " + message);
        }
    }

    private static void flushIo() {
        try {
            Storage.IO.submit(() -> {
            }).get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new AssertionError("IO flush failed", e);
        }
    }

    private void storage() {
        check("my_world".equals(Storage.sanitize("My World")), "sanitize(My World)");
        check(Storage.sanitize("Привет").contains("привет"), "sanitize keeps Cyrillic");
        check(Storage.asciiId("minecraft:overworld").matches("[a-z0-9._-]+_[0-9a-f]+"), "asciiId format");
        check(Storage.dimFolder("minecraft:overworld").equals(Storage.sanitize("minecraft_overworld")), "dimFolder");

        Path dir = Storage.worldDir("test_mechanics");
        Path file = dir.resolve("roundtrip.json");
        Marker m = new Marker();
        m.name = "Roundtrip";
        m.x = 12;
        m.y = 70;
        m.z = -34;
        m.icon = "house";
        m.description = "hello";
        Storage.writeJson(file, m);
        Marker read = Storage.readJson(file, Marker.class, null);
        check(read != null, "json read back");
        check("Roundtrip".equals(read.name), "json name");
        check(read.x == 12 && read.y == 70 && read.z == -34, "json coordinates");
        check("house".equals(read.icon), "json icon");
        Storage.deleteRecursive(dir);
        check(!Files.exists(dir), "test world cleanup");
    }

    private void markerCopy() {
        Marker a = new Marker();
        a.name = "A";
        a.screenshots.add("1.png");
        a.drawingsFor("1.png").add(new Stroke(Stroke.LINE, 0xFF112233, 2, new double[]{1, 2, 3, 4}));
        Marker b = a.copy();
        check(a.id.equals(b.id), "copy id");
        check(!(a.screenshots == b.screenshots), "copy screenshots list");
        check(b.screenshots.contains("1.png"), "copy screenshot value");
        check(b.drawingsFor("1.png").size() == 1, "copy drawings");
        b.screenshots.add("2.png");
        check(!a.screenshots.contains("2.png"), "copy is deep");
        a.apply(b);
        check(a.screenshots.contains("2.png"), "apply screenshots");
    }

    private void strokePoints() {
        Stroke s = new Stroke();
        check(s.pointCount() == 0, "stroke starts empty");
        s.addPoint(1, 2);
        s.addPoint(3, 4);
        check(s.pointCount() == 2, "stroke point count");
        check(s.pts[0] == 1 && s.pts[1] == 2 && s.pts[2] == 3 && s.pts[3] == 4, "stroke points");
        check(Stroke.PEN.equals("pen") && Stroke.LINE.equals("line") && Stroke.RECT.equals("rect") && Stroke.CIRCLE.equals("circle"), "stroke types");
    }

    private void themes() {
        check(Theme.ALL.size() == 8, "eight themes");
        Set<String> ids = new HashSet<>();
        for (Theme t : Theme.ALL) {
            check(ids.add(t.id()), "unique theme id " + t.id());
        }
        check(Theme.byId("modern_dark").id().equals("modern_dark"), "theme by id");
        check(Theme.byId("missing").id().equals("papyrus"), "unknown theme fallback");
        check(Theme.PAPYRUS.ink() == 0xFF3A2616, "papyrus ink");
    }

    private void translations() {
        check(T.MARKER_ICONS.size() >= 100, "icon catalog");
        check(!T.searchIcons("cave").isEmpty(), "search cave");
        check(!T.searchIcons("дом").isEmpty(), "search дом");
        check(!T.iconName("house").isEmpty(), "icon name");
        check(!T.dimName("minecraft:overworld").isEmpty(), "dimension name");
        String key = T.tr("atlas.button").getString();
        check(!key.isEmpty() && !key.equals("atlasmap.atlas.button"), "translation loaded");
    }

    private void viewTransforms() {
        MapView v = new MapView();
        v.setRect(0, 0, 100, 100);
        v.cx = 0;
        v.cz = 0;
        v.zoom = 1;
        check(v.sx(0) == 50f && v.sy(0) == 50f, "view centre");
        check(v.wx(50) == 0 && v.wz(50) == 0, "view world to screen");
        v.zoomAt(50, 50, 2f);
        check(v.zoom == 2f, "view zoomAt");
        check(Math.abs(v.wx(50)) < 0.0001 && Math.abs(v.wz(50)) < 0.0001, "view zoom anchor");
        check(v.contains(50, 50) && !v.contains(-1, -1), "view contains");
    }

    private void configPersistence() {
        ModConfig cfg = ModConfig.get();
        String theme = cfg.theme;
        boolean minimap = cfg.minimapEnabled;
        int size = cfg.minimapSizeIndex;
        int zoom = cfg.minimapZoomIndex;
        int corner = cfg.minimapCorner;
        boolean teleport = cfg.teleport;
        int sort = cfg.markerSort;

        cfg.theme = "stone";
        cfg.minimapEnabled = false;
        cfg.minimapSizeIndex = 99;
        cfg.minimapZoomIndex = -1;
        cfg.minimapCorner = 9;
        cfg.teleport = true;
        cfg.markerSort = 2;
        cfg.save();

        Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("atlasmap.json");
        ModConfig loaded = Storage.readJson(file, ModConfig.class, null);
        check(loaded != null, "config read back");
        check("stone".equals(loaded.theme), "config theme");
        check(!loaded.minimapEnabled, "config minimap flag");
        check(loaded.minimapSizeIndex >= 0 && loaded.minimapSizeIndex < ModConfig.MINIMAP_SIZES.length, "config size clamped");
        check(loaded.minimapZoomIndex == 0, "config zoom clamped");
        check(loaded.minimapCorner == 1, "config corner clamped");
        check(loaded.teleport, "config teleport");
        check(loaded.markerSort == 2, "config sort");

        cfg.theme = theme;
        cfg.minimapEnabled = minimap;
        cfg.minimapSizeIndex = size;
        cfg.minimapZoomIndex = zoom;
        cfg.minimapCorner = corner;
        cfg.teleport = teleport;
        cfg.markerSort = sort;
        cfg.save();
    }

    private void liveMap() {
        MapSession s = MapManager.live();
        check(s != null, "live session");
        String dim = MapManager.liveDim();
        check(dim != null, "live dimension");
        check(s.meta.id != null && !s.meta.id.isEmpty(), "world meta id");
        MapLayer layer = s.layer(dim);
        check(layer != null, "map layer");
        check(!layer.isEmpty(), "map tiles were scanned");

        MapTile tile = layer.getOrCreate(1000, 1000);
        tile.setPixel(0, 0, 0xFF112233);
        check(tile.getPixel(0, 0) == 0xFF112233, "tile pixel roundtrip");

        long key = MapLayer.pack(-5, 7);
        check((int) (key >> 32) == -5, "pack x");
        check((int) key == 7, "pack z");
    }

    private void persistence() {
        MapSession s = MapManager.live();
        String dim = MapManager.liveDim();

        Marker m = new Marker();
        m.name = "Mechanics";
        m.description = "persist";
        m.icon = "house";
        m.dim = dim;
        m.x = 10;
        m.y = 70;
        m.z = -20;
        s.markers.add(m);
        s.saveMarkers();

        Stroke stroke = new Stroke(Stroke.RECT, 0xFF445566, 3, new double[]{0, 0, 8, 8});
        int strokesBefore = s.strokes(dim).size();
        s.strokes(dim).add(stroke);
        s.saveStrokes();
        flushIo();

        MapSession reloaded = MapSession.open(s.meta);
        Marker loaded = reloaded.marker(m.id);
        check(loaded != null, "marker reloaded");
        check("Mechanics".equals(loaded.name), "marker name reloaded");
        check(loaded.x == 10 && loaded.y == 70 && loaded.z == -20, "marker coordinates reloaded");
        check("persist".equals(loaded.description), "marker description reloaded");
        check(reloaded.strokes(dim).size() == strokesBefore + 1, "drawing reloaded");
        check(reloaded.knownDims().contains(dim), "known dimensions");

        s.markers.removeIf(x -> x.id.equals(m.id));
        s.strokes(dim).removeIf(x -> x == stroke);
        s.saveMarkers();
        s.saveStrokes();
        flushIo();
    }

    private void screenshots() {
        MapSession s = MapManager.live();
        Marker m = new Marker();
        m.name = "Shot";
        m.dim = MapManager.liveDim();
        s.markers.add(m);
        s.saveMarkers();

        Path src = Storage.root().resolve("mechanics_src.png");
        try {
            Files.createDirectories(src.getParent());
            NativeImage img = new NativeImage(2, 2, false);
            img.setPixel(0, 0, 0xFFFF0000);
            img.writeToFile(src);
            img.close();

            String name = ImageCache.importImage(src, s.screenshotsDir()).get(15, TimeUnit.SECONDS);
            check(name != null, "image imported");
            check(Files.exists(s.screenshot(name)), "image file exists");
            m.screenshots.add(name);
            s.saveMarkers();
            flushIo();

            MapSession reloaded = MapSession.open(s.meta);
            Marker loaded = reloaded.marker(m.id);
            check(loaded != null && loaded.screenshots.contains(name), "screenshot persisted");

            m.screenshots.remove(name);
            s.saveMarkers();
            Files.deleteIfExists(s.screenshot(name));
            Files.deleteIfExists(src);
        } catch (Exception e) {
            throw new AssertionError("[AtlasMap mechanics] screenshot pipeline", e);
        } finally {
            s.markers.removeIf(x -> x.id.equals(m.id));
            s.saveMarkers();
            ImageCache.clear();
        }
    }
}
