package org.webtrade.atlasmapextended.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestInput;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.webtrade.atlasmapextended.client.ModKeys;
import org.webtrade.atlasmapextended.client.data.Region;
import org.webtrade.atlasmapextended.client.gui.MapScreen;
import org.webtrade.atlasmapextended.client.gui.MapView;
import org.webtrade.atlasmapextended.client.map.MapManager;
import org.webtrade.atlasmapextended.client.map.MapSession;

import java.util.List;

/** Stress test: 300/500/1000 regions with 200 points each, all visible on one screen. */
public class RegionPerfTest implements FabricClientGameTest {

    private static final int POINTS = 200;

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
            ctx.runOnClient(mc -> ((MapScreen) mc.gui.screen()).setView(0.5, 0.5, 1f));
            ctx.waitTicks(3);

            for (int count : new int[]{300, 500, 1000}) {
                ctx.runOnClient(mc -> fill(count));
                ctx.waitTicks(10);
                double[] sample = ctx.computeOnClient(mc -> new double[]{
                        MapView.lastRegionsNanos / 1_000_000.0,
                        MapView.lastBakeNanos / 1_000_000.0,
                        MapView.lastRegionsDrawn,
                        MapView.lastRegionsPoints});
                long t0 = System.nanoTime();
                ctx.waitTicks(20);
                double frameMs = (System.nanoTime() - t0) / 1_000_000.0 / 20.0;
                System.out.println("[region perf] n=" + count
                        + " drawn=" + (int) sample[2]
                        + " simplifiedPoints=" + (int) sample[3]
                        + " perFrame=" + String.format("%.3f", sample[0]) + " ms"
                        + " bake=" + String.format("%.2f", sample[1]) + " ms"
                        + " tick=" + String.format("%.2f", frameMs) + " ms");
                ctx.takeScreenshot("region_perf_" + count);
            }

            // normal usage: a few large named regions must still look like before
            ctx.runOnClient(mc -> {
                MapSession s = MapManager.live();
                String dim = MapManager.liveDim();
                List<Region> regions = s.regions(dim);
                regions.clear();
                regions.add(blob("Village", dim, 0, 0, 45, 0xFF3FA34D));
                regions.add(blob("Forest", dim, 120, 40, 70, 0xFF2F6B34));
                regions.add(blob("Lake", dim, -90, 70, 55, 0xFF2E6F9E));
                ((MapScreen) mc.gui.screen()).setView(0.5, 0.5, 1.2f);
            });
            ctx.waitTicks(5);
            ctx.takeScreenshot("region_normal");
        }
    }

    private static Region blob(String name, String dim, double cx, double cz, double radius, int color) {
        int points = 48;
        double[] pts = new double[points * 2];
        for (int p = 0; p < points; p++) {
            double a = p * Math.PI * 2 / points;
            double rad = radius * (0.85 + 0.15 * Math.sin(a * 5));
            pts[p * 2] = cx + Math.cos(a) * rad;
            pts[p * 2 + 1] = cz + Math.sin(a) * rad;
        }
        Region r = new Region();
        r.name = name;
        r.dim = dim;
        r.color = color;
        r.pts = pts;
        return r;
    }

    private static void fill(int count) {
        MapSession s = MapManager.live();
        String dim = MapManager.liveDim();
        List<Region> regions = s.regions(dim);
        regions.clear();
        int cols = (int) Math.ceil(Math.sqrt(count));
        for (int i = 0; i < count; i++) {
            double cx = (i % cols - cols / 2.0) * 12;
            double cz = (i / cols - cols / 2.0) * 12;
            double[] pts = new double[POINTS * 2];
            for (int p = 0; p < POINTS; p++) {
                double a = p * Math.PI * 2 / POINTS;
                double rad = 4.5 + 1.5 * Math.sin(a * 9 + i);
                pts[p * 2] = cx + Math.cos(a) * rad;
                pts[p * 2 + 1] = cz + Math.sin(a) * rad;
            }
            Region r = new Region();
            r.name = "R" + i;
            r.dim = dim;
            r.color = 0xFF000000 | (i * 7919 & 0xFFFFFF);
            r.pts = pts;
            regions.add(r);
        }
    }
}
