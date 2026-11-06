package org.webtrade.atlasmapextended.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.webtrade.atlasmapextended.client.ModKeys;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.map.MapManager;

/** A remote dimension opens where it was left, remembers panning, and shows the "you were here" pin. */
public class DimensionMemoryTest implements FabricClientGameTest {

    private static final String NETHER = "minecraft:the_nether";

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
            ctx.waitTicks(5);

            // first visit in this session: the last exit position from meta.json is used
            ctx.runOnClient(mc -> {
                MapManager.rememberView(NETHER, 1234, -4321, 2f);
                MapManager.live().meta.setPos(NETHER, 1234, -4321);
                ((MapScreen) mc.gui.screen()).showDimension(NETHER);
            });
            ctx.waitTicks(3);
            double[] first = ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).viewCenter());
            System.out.println("[dim memory] first visit: " + first[0] + ", " + first[1] + " zoom " + first[2]);
            if (Math.abs(first[0] - 1234) > 0.01 || Math.abs(first[1] + 4321) > 0.01 || Math.abs(first[2] - 2) > 0.01) {
                throw new AssertionError("remembered view was not restored: " + first[0] + "," + first[1] + " z" + first[2]);
            }

            // pan/zoom, go to the player's dimension and back: the panning is remembered this session
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).setView(52, 62, 3f));
            ctx.waitTicks(3);
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).showDimension("minecraft:overworld"));
            ctx.waitTicks(3);
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).showDimension(NETHER));
            ctx.waitTicks(3);
            double[] back = ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).viewCenter());
            System.out.println("[dim memory] after pan and return: " + back[0] + ", " + back[1] + " zoom " + back[2]);
            if (Math.abs(back[0] - 52) > 0.01 || Math.abs(back[1] - 62) > 0.01 || Math.abs(back[2] - 3) > 0.01) {
                throw new AssertionError("panned view was not remembered: " + back[0] + "," + back[1] + " z" + back[2]);
            }

            // the pin: only on a foreign dimension, positioned at the stored exit point
            ctx.runOnClient(mc -> {
                MapManager.live().meta.setPos(NETHER, 62, 72);
                ((MapScreen) mc.gui.screen()).setView(60, 70, 3f);
            });
            ctx.waitTicks(3);
            ctx.takeScreenshot("dim_memory_pin");
            int[] p = ctx.computeOnClient(mc -> {
                int s = mc.getWindow().getGuiScale();
                return new int[]{(mc.gui.screen().width / 2 + 6) * s, (mc.gui.screen().height / 2 + 6) * s};
            });
            input.setCursorPos(p[0], p[1]);
            ctx.waitTicks(3);
            ctx.takeScreenshot("dim_memory_pin_tooltip");
        }
    }
}
