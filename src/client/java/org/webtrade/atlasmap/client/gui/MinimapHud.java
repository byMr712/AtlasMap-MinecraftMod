package org.webtrade.atlasmap.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.webtrade.atlasmap.client.data.ModConfig;
import org.webtrade.atlasmap.client.map.MapManager;
import org.webtrade.atlasmap.client.map.MapSession;

/** The papyrus minimap in a corner of the HUD. North is always up. */
public class MinimapHud {
    private final MapView view = new MapView();

    public void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        ModConfig cfg = ModConfig.get();
        MapSession session = MapManager.live();
        LocalPlayer p = mc.player;
        if (!cfg.minimapEnabled || session == null || p == null || mc.level == null
                || mc.screen instanceof MapScreen) {
            return;
        }
        String dim = mc.level.dimension().location().toString();
        float pt = delta.getGameTimeDeltaPartialTick(true);
        double px = p.xo + (p.getX() - p.xo) * pt;
        double pz = p.zo + (p.getZ() - p.zo) * pt;

        int size = cfg.minimapSize();
        int textH = cfg.minimapCoords ? 13 : 0;
        int margin = 8;
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        boolean right = cfg.minimapCorner == 0 || cfg.minimapCorner == 2;
        boolean top = cfg.minimapCorner < 2;
        int x = right ? sw - size - margin : margin;
        int y = top ? margin : sh - size - margin - textH;
        if (top && right && !p.getActiveEffects().isEmpty()) {
            y += 26;
        }

        Paper.panel(g, x - 4, y - 4, size + 8, size + 8 + textH);
        view.setRect(x, y, size, size);
        view.cx = px;
        view.cz = pz;
        view.zoom = cfg.minimapZoom();
        MapView.newFrame();

        g.enableScissor(x, y, x + size, y + size);
        view.drawBackground(g);
        view.drawTiles(g, session.layer(dim));
        if (cfg.drawingsVisible) {
            view.drawStrokes(g, session.strokes(dim), 0.75f);
        }
        if (cfg.minimapMarkers && cfg.markersVisible) {
            view.drawMarkers(g, mc.font, session.markers, dim, 10, false, null, null, true);
        }
        g.disableScissor();
        Paper.outline(g, x - 1, y - 1, size + 2, size + 2, Paper.INK_SOFT);
        view.drawPlayer(g, px, pz, p.getYRot(), 12);

        // north marker
        int nx = x + size / 2;
        g.fill(nx - 5, y - 6, nx + 5, y + 4, Paper.INK);
        g.fill(nx - 4, y - 5, nx + 4, y + 3, Paper.CREAM);
        g.drawString(mc.font, "N", nx - 2, y - 4, Paper.RED, false);

        if (cfg.minimapCoords) {
            String c = p.getBlockX() + ", " + p.getBlockY() + ", " + p.getBlockZ();
            g.drawString(mc.font, c, x + (size - mc.font.width(c)) / 2, y + size + 3, Paper.INK, false);
        }
    }
}
