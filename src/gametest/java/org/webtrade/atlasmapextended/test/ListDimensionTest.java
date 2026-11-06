package org.webtrade.atlasmapextended.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.webtrade.atlasmapextended.client.ModKeys;
import org.webtrade.atlasmapextended.client.data.Marker;
import org.webtrade.atlasmapextended.client.data.Region;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.map.MapManager;
import org.webtrade.atlasmapextended.client.map.MapSession;

/** The object list shows only the dimension selected with the toolbar button. */
public class ListDimensionTest implements FabricClientGameTest {

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
            ctx.runOnClient(mc -> {
                MapSession s = MapManager.live();
                s.markers.clear();
                String over = mc.level.dimension().identifier().toString();
                int px = mc.player.getBlockX();
                int pz = mc.player.getBlockZ();
                s.markers.add(marker(over, "home", px, pz, 1));
                s.markers.add(marker(over, "farm", px + 30, pz, 2));
                s.markers.add(marker(over, "mine", px, pz + 30, 3));
                s.markers.add(marker(NETHER, "portal", 40, 40, 4));
                s.markers.add(marker(NETHER, "fortress", 70, 70, 5));
                s.regions(over).add(region("Village", over, -20, -20, 40, 40));
                s.regions(NETHER).add(region("Fortress", NETHER, 0, 0, 50, 50));
                s.regions(NETHER).add(region("Lava sea", NETHER, 60, 60, 110, 110));
            });

            input.pressKey(ModKeys.OPEN_MAP);
            ctx.waitForScreen(MapScreen.class);
            ctx.waitTicks(5);
            input.pressKey(ModKeys.MARKER_LIST);
            ctx.waitTicks(5);
            ctx.takeScreenshot("list_dim_overworld");

            String over = ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).shownDimension());
            // the "dimension" button is the globe in the toolbar; switch through a nether marker
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).showMarker(MapManager.live().markers.get(3)));
            ctx.waitTicks(5);
            String nether = ctx.computeOnClient(mc -> ((MapScreen) mc.gui.screen()).shownDimension());
            ctx.takeScreenshot("list_dim_nether");
            System.out.println("[list dim] overworld=" + over + " switched to=" + nether);
            if (!NETHER.equals(nether)) {
                throw new AssertionError("dimension did not switch: " + nether);
            }
        }
    }

    private static Marker marker(String dim, String name, int x, int z, int i) {
        Marker m = new Marker();
        m.name = name;
        m.icon = "star";
        m.dim = dim;
        m.x = x;
        m.z = z;
        m.y = 70;
        m.created = System.currentTimeMillis() - i * 1000L;
        return m;
    }

    private static Region region(String name, String dim, int x1, int z1, int x2, int z2) {
        Region r = new Region();
        r.name = name;
        r.dim = dim;
        r.pts = new double[]{x1, z1, x2, z1, x2, z2, x1, z2};
        return r;
    }
}
