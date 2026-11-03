package org.webtrade.atlasmapextended.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.webtrade.atlasmapextended.client.data.Marker;
import org.webtrade.atlasmapextended.client.data.ModConfig;
import org.webtrade.atlasmapextended.client.data.Stroke;
import org.webtrade.atlasmapextended.client.gui.ImageCache;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.map.MapLayer;
import org.webtrade.atlasmapextended.client.map.MapManager;
import org.webtrade.atlasmapextended.client.map.MapSession;
import org.webtrade.atlasmapextended.client.map.MapTile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Stages nice looking scenes for a video thumbnail and saves 1920x1080 screenshots. */
public class ThumbnailShots implements FabricClientGameTest {
    private int sx;
    private int sy;
    private int sz;

    @Override
    public void runTest(ClientGameTestContext ctx) {
        TestInput input = ctx.getInput();
        input.resizeWindow(1920, 1080);
        ctx.runOnClient(mc -> {
            mc.options.languageCode = "ru_ru";
            mc.getLanguageManager().setSelected("ru_ru");
            mc.options.guiScale().set(4);
            mc.options.bobView().set(false);
            mc.resizeGui();
            ModConfig cfg = ModConfig.get();
            cfg.minimapEnabled = true;
            cfg.minimapSizeIndex = 3;
            cfg.minimapZoomIndex = 1;
            cfg.minimapCorner = 0;
        });
        ctx.runOnClient(mc -> mc.reloadResourcePacks());
        ctx.waitTicks(40);

        try (TestSingleplayerContext sp = ctx.worldBuilder().setUseConsistentSettings(false).adjustSettings(st -> {
            String seed = System.getenv("MM_SEED");
            if (seed != null && !seed.isBlank()) {
                st.setSeed(seed);
            }
        }).create()) {
            sp.getConnection().waitForChunksRender();
            ctx.runOnClient(mc -> mc.options.renderDistance().set(12));
            for (String c : new String[]{"time set 1500", "weather clear", "difficulty peaceful", "gamemode creative @a",
                    "gamerule advance_time false", "gamerule advance_weather false"}) {
                try {
                    sp.getServer().runCommand(c);
                } catch (Throwable ignored) {
                }
            }
            ctx.runOnClient(mc -> {
                sx = mc.player.getBlockX();
                sy = mc.player.getBlockY();
                sz = mc.player.getBlockZ();
            });
            // explore around the spawn so that the map has a large drawn area
            int[][] spots = {{0, 0}, {170, 0}, {170, 170}, {0, 170}, {-170, 170}, {-170, 0}, {-170, -170}, {0, -170},
                    {170, -170}};
            for (int[] o : spots) {
                sp.getServer().runCommand("tp @a " + (sx + o[0]) + " 200 " + (sz + o[1]));
                ctx.waitTicks(130);
            }
            sp.getServer().runCommand("tp @a " + sx + " " + (sy + 1) + " " + sz + " 135 12");
            ctx.waitTicks(200);
            ctx.runOnClient(mc -> mc.player.getAbilities().flying = false);
            ctx.waitTicks(40);

            input.setCursorPos(10, 10);
            ctx.takeScreenshot("thumb_ingame_raw");

            // markers on land/water picked from the drawn map
            ctx.runOnClient(this::placeMarkers);
            ctx.waitTicks(5);
            ctx.takeScreenshot("thumb_ingame_minimap");

            // attach an in-game screenshot with a drawing to the house marker
            ctx.runOnClient(mc -> {
                MapSession s = MapManager.live();
                Path src = mc.gameDirectory.toPath().resolve("screenshots");
                Path shot;
                try (var files = java.nio.file.Files.list(src)) {
                    shot = files.filter(p -> p.getFileName().toString().contains("thumb_ingame_raw")).findFirst().orElse(null);
                } catch (java.io.IOException ex) {
                    throw new RuntimeException(ex);
                }
                if (shot != null) {
                    String name = ImageCache.importImage(shot, s.screenshotsDir()).join();
                    Marker house = s.markers.get(0);
                    house.screenshots.add(name);
                    house.drawingsFor(name).add(circle(960, 560, 260, 0xFFB03A2E, 10));
                }
            });

            ctx.runOnClient(mc -> {
                mc.options.guiScale().set(5);
                mc.resizeGui();
                MapScreen screen = MapScreen.openLive(mc);
                screen.setCleanMode(true);
                screen.setView(sx + 8, sz + 6, 1.6f);
            });
            ctx.waitForScreen(MapScreen.class);
            ctx.waitTicks(40);
            cursor(ctx, input, mc -> new int[]{2, 2});
            ctx.waitTicks(10);
            ctx.takeScreenshot("thumb_clean_big");
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).setView(sx + 8, sz - 20, 1.6f));
            ctx.waitTicks(20);
            ctx.takeScreenshot("thumb_clean_low");
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).setView(sx + 8, sz + 6, 2.1f));
            ctx.waitTicks(20);
            ctx.takeScreenshot("thumb_clean_bigger");
            ctx.runOnClient(mc -> {
                mc.options.guiScale().set(4);
                mc.resizeGui();
                ((MapScreen) mc.gui.screen()).setView(sx + 8, sz + 6, 1.5f);
            });
            ctx.waitTicks(20);
            ctx.takeScreenshot("thumb_clean_wide");
            ctx.runOnClient(mc -> {
                mc.options.guiScale().set(3);
                mc.resizeGui();
                MapScreen screen = (MapScreen) mc.gui.screen();
                screen.setCleanMode(false);
                screen.setView(sx + 10, sz + 5, 1.6f);
            });
            ctx.waitTicks(20);
            cursor(ctx, input, mc -> new int[]{mc.gui.screen().width - 30, mc.gui.screen().height / 2 + 40});
            ctx.waitTicks(20);
            ctx.takeScreenshot("thumb_map_overview");

            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).setView(sx + 20, sz + 25, 3f));
            ctx.waitTicks(30);
            ctx.takeScreenshot("thumb_map_close");

            ctx.runOnClient(mc -> {
                MapScreen screen = (MapScreen) mc.gui.screen();
                screen.openMarkerList();
                screen.showMarker(MapManager.live().markers.get(0));
                screen.setView(MapManager.live().markers.get(0).x, MapManager.live().markers.get(0).z, 2.2f);
            });
            ctx.waitTicks(40);
            cursor(ctx, input, mc -> new int[]{mc.gui.screen().width / 2 - 20, mc.gui.screen().height - 30});
            ctx.waitTicks(5);
            ctx.takeScreenshot("thumb_map_panels");

            // icon picker full of icons
            ctx.runOnClient(mc -> {
                MapScreen screen = (MapScreen) mc.gui.screen();
                screen.closePanels();
                screen.editMarker(MapManager.live().markers.get(0));
            });
            ctx.waitTicks(5);
            input.pressKey(InputConstants.KEY_ESCAPE);
            clickGui(ctx, input, mc -> {
                int w = mc.gui.screen().width;
                int pw = Math.max(150, Math.min(190, w / 3));
                return new int[]{w - pw - 6 + 30, 6 + 22 + 10 + 20 + 11};
            });
            ctx.waitTicks(5);
            cursor(ctx, input, mc -> new int[]{mc.gui.screen().width - 4, mc.gui.screen().height - 4});
            ctx.waitTicks(5);
            ctx.takeScreenshot("thumb_icons_top");
            input.scroll(-11);
            ctx.waitTicks(5);
            ctx.takeScreenshot("thumb_icons_shapes");
            input.pressKey(InputConstants.KEY_ESCAPE);
            input.pressKey(InputConstants.KEY_ESCAPE);
            ctx.waitTicks(5);
            ctx.runOnClient(mc -> mc.gui.setScreen(null));
            ctx.waitTicks(5);
        }
    }

    private void placeMarkers(Minecraft mc) {
        MapSession s = MapManager.live();
        String dim = mc.level.dimension().identifier().toString();
        MapLayer layer = s.layer(dim);
        String[][] land = {
                {"house", "Мой дом"}, {"castle", "Крепость"}, {"mine", "Алмазная шахта"}, {"portal", "Портал в Незер"},
                {"bell", "Деревня"}, {"chest", "Клад"}, {"skull", "Логово криперов"}, {"farm", "Ферма"},
                {"windmill", "Мельница"}, {"star", "Точка сбора"}};
        int[][] offsets = {{28, 34}, {95, -60}, {-110, 40}, {60, 110}, {-70, -120}, {150, 70}, {-160, -20}, {25, -150},
                {-40, 160}, {130, -140}};
        List<int[]> used = new ArrayList<>();
        for (int i = 0; i < land.length; i++) {
            int[] p = findSpot(layer, sx + offsets[i][0] * 55 / 100, sz + offsets[i][1] * 45 / 100, false, used);
            if (p == null) {
                continue;
            }
            used.add(p);
            s.markers.add(marker(land[i][0], land[i][1], p, dim, i));
        }
        int[] port = findSpot(layer, sx, sz, true, used);
        if (port != null) {
            s.markers.add(marker("anchor", "Порт", port, dim, 99));
        }
        // a hand drawn route between the first markers and a warning circle
        List<Stroke> strokes = s.strokes(dim);
        if (s.markers.size() >= 3) {
            Marker a = s.markers.get(0);
            Marker b = s.markers.get(1);
            Marker c = s.markers.get(5 < s.markers.size() ? 5 : 2);
            strokes.add(route(new double[]{a.x, a.z, b.x, b.z, c.x, c.z}, 0xFFB03A2E));
        }
        if (s.markers.size() > 6) {
            Marker danger = s.markers.get(6);
            strokes.add(circle(danger.x + 0.5, danger.z + 0.5, 22, 0xFF7A3E9A, 3));
        }
        if (s.markers.size() > 7) {
            Marker farm = s.markers.get(7);
            strokes.add(new Stroke(Stroke.RECT, 0xFF4E8A34, 3, new double[]{farm.x - 18, farm.z - 14, farm.x + 18, farm.z + 14}));
        }
    }

    private static Marker marker(String icon, String name, int[] p, String dim, int i) {
        Marker m = new Marker();
        m.icon = icon;
        m.name = name;
        m.x = p[0];
        m.z = p[1];
        m.y = 70;
        m.dim = dim;
        m.created = System.currentTimeMillis() - i * 60_000L;
        m.description = "Заметка: " + name.toLowerCase() + ". Отсюда до дома рукой подать.";
        return m;
    }

    /** Searches a drawn pixel of the wanted kind (land or water) near the given point. */
    private static int[] findSpot(MapLayer layer, int x, int z, boolean water, List<int[]> used) {
        for (int r = 0; r < 120; r += 4) {
            for (int a = 0; a < 16; a++) {
                int px = x + (int) Math.round(Math.cos(a * Math.PI / 8) * r);
                int pz = z + (int) Math.round(Math.sin(a * Math.PI / 8) * r);
                int c = pixel(layer, px, pz);
                if ((c >>> 24) == 0) {
                    continue;
                }
                boolean isWater = (c & 0xFF) > ((c >> 16) & 0xFF) + 10;
                if (isWater != water) {
                    continue;
                }
                if (water && !nearLand(layer, px, pz)) {
                    continue;
                }
                boolean free = used.stream().noneMatch(u -> Math.hypot(u[0] - px, u[1] - pz) < 30);
                if (free) {
                    return new int[]{px, pz};
                }
            }
        }
        return null;
    }

    private static boolean nearLand(MapLayer layer, int x, int z) {
        for (int d = 6; d <= 18; d += 6) {
            for (int[] o : new int[][]{{d, 0}, {-d, 0}, {0, d}, {0, -d}}) {
                int c = pixel(layer, x + o[0], z + o[1]);
                if ((c >>> 24) != 0 && (c & 0xFF) <= ((c >> 16) & 0xFF) + 10) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int pixel(MapLayer layer, int x, int z) {
        MapTile t = layer.getOrCreate(Math.floorDiv(x, MapTile.SIZE), Math.floorDiv(z, MapTile.SIZE));
        return t.getPixel(Math.floorMod(x, MapTile.SIZE), Math.floorMod(z, MapTile.SIZE));
    }

    private static Stroke route(double[] pts, int color) {
        Stroke s = new Stroke(Stroke.PEN, color, 3, new double[0]);
        for (int i = 0; i + 3 < pts.length; i += 2) {
            double x0 = pts[i];
            double z0 = pts[i + 1];
            double x1 = pts[i + 2];
            double z1 = pts[i + 3];
            double len = Math.hypot(x1 - x0, z1 - z0);
            int n = Math.max(2, (int) (len / 4));
            for (int k = 0; k < n; k++) {
                double t = k / (double) n;
                double wob = Math.sin(t * Math.PI * 3) * len * 0.06;
                double nx = -(z1 - z0) / len;
                double nz = (x1 - x0) / len;
                s.addPoint(x0 + (x1 - x0) * t + nx * wob, z0 + (z1 - z0) * t + nz * wob);
            }
        }
        s.addPoint(pts[pts.length - 2], pts[pts.length - 1]);
        return s;
    }

    private static Stroke circle(double x, double z, double r, int color, int width) {
        return new Stroke(Stroke.CIRCLE, color, width, new double[]{x, z, x + r, z});
    }

    private interface Pos {
        int[] get(Minecraft mc);
    }

    private static void cursor(ClientGameTestContext ctx, TestInput input, Pos pos) {
        int[] p = ctx.computeOnClient(mc -> {
            int[] g = pos.get(mc);
            int s = mc.getWindow().getGuiScale();
            return new int[]{g[0] * s, g[1] * s};
        });
        input.setCursorPos(p[0], p[1]);
        ctx.waitTick();
    }

    private static void clickGui(ClientGameTestContext ctx, TestInput input, Pos pos) {
        cursor(ctx, input, pos);
        input.pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        ctx.waitTick();
    }
}
