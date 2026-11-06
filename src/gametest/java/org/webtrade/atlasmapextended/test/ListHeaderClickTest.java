package org.webtrade.atlasmapextended.test;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import org.webtrade.atlasmapextended.client.ModKeys;
import org.webtrade.atlasmapextended.client.data.Marker;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.gui.T;
import org.webtrade.atlasmapextended.client.map.MapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Clicks the "markers" header of the object list at several horizontal positions.
 * Every part of the header that is highlighted on hover must react to a click.
 */
public class ListHeaderClickTest implements FabricClientGameTest {

    private static final int TEXT_TAIL = -100;

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
            ctx.waitTicks(60);
            ctx.runOnClient(mc -> {
                MapManager.live().markers.clear();
                for (int i = 0; i < 8; i++) {
                    Marker m = new Marker();
                    m.name = "test " + i;
                    m.icon = "star";
                    m.x = mc.player.getBlockX() + i * 4;
                    m.z = mc.player.getBlockZ();
                    m.y = 70;
                    m.dim = mc.level.dimension().identifier().toString();
                    MapManager.live().markers.add(m);
                }
            });

            input.pressKey(ModKeys.OPEN_MAP);
            ctx.waitForScreen(MapScreen.class);
            ctx.waitTicks(10);
            input.pressKey(ModKeys.MARKER_LIST);
            ctx.waitTicks(5);
            ctx.takeScreenshot("list_header_before");

            List<String> failures = new ArrayList<>();
            for (int mode : new int[]{10, 40, TEXT_TAIL, 60, 200, -51, -6}) {
                int[] p = ctx.computeOnClient(mc -> headerPoint(mc, mode));
                click(ctx, input, p);
                boolean opened = ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).markersSectionOpen());
                click(ctx, input, p);
                boolean closed = !ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).markersSectionOpen());
                System.out.println("[list header] mode=" + mode + " point=" + p[0] + "," + p[1]
                        + " opened=" + opened + " closed=" + closed);
                if (!opened || !closed) {
                    failures.add("mode " + mode + " at " + p[0] + "," + p[1]
                            + " (opened=" + opened + ", closed=" + closed + ")");
                }
            }
            ctx.takeScreenshot("list_header_after");
            if (!failures.isEmpty()) {
                throw new AssertionError("header click did not toggle at: " + failures);
            }
        }
    }

    /**
     * Mouse position (scaled) inside the markers header.
     * mode &gt; 0 = pixels from the left edge, mode == TEXT_TAIL = over the last letters of the title,
     * other negatives = offset from the right edge.
     */
    private static int[] headerPoint(Minecraft mc, int mode) {
        int w = mc.gui.screen().width;
        int lw = Math.max(130, Math.min(180, w / 4));
        int x = 6 + 20 + 8 + 6;
        int width = lw - 12;
        int gx;
        if (mode == TEXT_TAIL) {
            gx = x + 14 + mc.font.width(T.s("list.section.markers") + " (8)") - 3;
        } else if (mode > 0) {
            gx = x + Math.min(mode, width - 2);
        } else {
            gx = x + width + mode;
        }
        int s = mc.getWindow().getGuiScale();
        return new int[]{gx * s, (46 + 9) * s};
    }

    private static void click(ClientGameTestContext ctx, TestInput input, int[] p) {
        input.setCursorPos(p[0], p[1]);
        ctx.waitTicks(2);
        input.pressMouse(InputConstants.MOUSE_BUTTON_LEFT);
        ctx.waitTicks(2);
    }
}
