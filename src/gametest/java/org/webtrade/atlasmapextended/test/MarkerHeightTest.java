package org.webtrade.atlasmapextended.test;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.level.levelgen.Heightmap;
import org.webtrade.atlasmapextended.client.ModKeys;
import org.webtrade.atlasmapextended.client.data.Marker;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.map.MapManager;

import java.util.List;

/** A marker placed on the map gets surface + 2, or 70 when the chunk is not loaded. */
public class MarkerHeightTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext ctx) {
        TestInput input = ctx.getInput();
        input.resizeWindow(1280, 720);
        ctx.runOnClient(mc -> {
            mc.options.guiScale().set(2);
            mc.resizeGui();
        });

        try (TestSingleplayerContext sp = ctx.worldBuilder().setUseConsistentSettings(false)
                .adjustSettings(s -> s.setAllowCommands(true)).create()) {
            sp.getConnection().waitForChunksRender();
            ctx.waitTicks(80);
            input.pressKey(ModKeys.OPEN_MAP);
            ctx.waitForScreen(MapScreen.class);
            ctx.waitTicks(10);

            // loaded chunk: surface + 2 (pick a column whose height does not coincide with
            // the 70 fallback, otherwise the check proves nothing)
            int[] spot = ctx.computeOnClient(mc -> {
                int px = mc.player.getBlockX();
                int pz = mc.player.getBlockZ();
                for (int r = 8; r <= 64; r += 8) {
                    for (int a = 0; a < 16; a++) {
                        int x = px + (int) (Math.cos(a * Math.PI / 8) * r);
                        int z = pz + (int) (Math.sin(a * Math.PI / 8) * r);
                        if (mc.level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) {
                            continue;
                        }
                        int h = mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                        if (h > mc.level.getMinY() && h + 2 != 70) {
                            return new int[]{x, z};
                        }
                    }
                }
                return new int[]{px, pz};
            });
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen())
                    .setView(spot[0] + 0.5, spot[1] + 0.5, 2f));
            ctx.waitTicks(10);
            click(ctx, input);
            int[] loaded = ctx.computeOnClient(mc -> {
                List<Marker> all = MapManager.live().markers;
                Marker m = all.get(all.size() - 1);
                int h = mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, m.x, m.z);
                return new int[]{m.x, m.y, m.z, h, mc.level.getMinY()};
            });
            System.out.println("[marker height] loaded chunk at " + loaded[0] + "," + loaded[2]
                    + " y=" + loaded[1] + " heightmap=" + loaded[3] + " minY=" + loaded[4]);
            if (loaded[1] != loaded[3] + 2) {
                throw new AssertionError("loaded chunk marker y=" + loaded[1] + ", expected " + (loaded[3] + 2));
            }
            if (loaded[1] == 70) {
                throw new AssertionError("loaded chunk marker fell back to 70 (getChunkNow did not see the chunk)");
            }
            if (loaded[1] <= loaded[4]) {
                throw new AssertionError("marker y is not above minY: " + loaded[1]);
            }

            // unloaded chunk: plain 70, never a broken heightmap value
            ctx.runOnClient(mc -> {
                MapScreen screen = (MapScreen) mc.gui.screen();
                screen.closePanels();
                screen.setView(30_000_000, 30_000_000, 1f);
            });
            ctx.waitTicks(10);
            click(ctx, input);
            int[] unloaded = ctx.computeOnClient(mc -> {
                List<Marker> all = MapManager.live().markers;
                Marker m = all.get(all.size() - 1);
                return new int[]{m.x, m.y, m.z, mc.level.getMinY()};
            });
            System.out.println("[marker height] unloaded area at " + unloaded[0] + "," + unloaded[2]
                    + " y=" + unloaded[1] + " minY=" + unloaded[3]);
            if (unloaded[1] != 70) {
                throw new AssertionError("unloaded chunk marker y=" + unloaded[1] + ", expected 70");
            }
            ctx.takeScreenshot("marker_heights");
        }
    }

    private static void click(ClientGameTestContext ctx, TestInput input) {
        int[] p = ctx.computeOnClient(mc -> {
            int s = mc.getWindow().getGuiScale();
            return new int[]{mc.gui.screen().width / 2 * s, mc.gui.screen().height / 2 * s};
        });
        input.setCursorPos(p[0], p[1]);
        ctx.waitTicks(2);
        input.pressMouse(InputConstants.MOUSE_BUTTON_RIGHT);
        ctx.waitTicks(4);
    }
}
