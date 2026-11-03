package org.webtrade.atlasmap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.webtrade.atlasmap.client.data.Marker;
import org.webtrade.atlasmap.client.data.Stroke;
import org.webtrade.atlasmap.client.map.MapLayer;
import org.webtrade.atlasmap.client.map.MapTile;

import java.util.List;

/** A viewport onto the world map. Shared by the full screen map and the minimap. */
public class MapView {
    public int x;
    public int y;
    public int w;
    public int h;
    public double cx;
    public double cz;
    /** GUI pixels per block. */
    public float zoom = 2f;

    public void setRect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public float sx(double wx) {
        return (float) (x + w / 2.0 + (wx - cx) * zoom);
    }

    public float sy(double wz) {
        return (float) (y + h / 2.0 + (wz - cz) * zoom);
    }

    public double wx(double sx) {
        return cx + (sx - x - w / 2.0) / zoom;
    }

    public double wz(double sy) {
        return cz + (sy - y - h / 2.0) / zoom;
    }

    public boolean contains(double mx, double my) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    /** Zooms keeping the world point under (mx, my) in place. */
    public void zoomAt(double mx, double my, float newZoom) {
        double wx = wx(mx);
        double wz = wz(my);
        zoom = newZoom;
        cx = wx - (mx - x - w / 2.0) / zoom;
        cz = wz - (my - y - h / 2.0) / zoom;
    }

    public void drawBackground(GuiGraphics g) {
        Paper.tiled(g, Paper.PARCHMENT, x, y, w, h, (int) Math.floor(cx * zoom), (int) Math.floor(cz * zoom));
    }

    public void drawTiles(GuiGraphics g, MapLayer layer) {
        int rx0 = Math.floorDiv((int) Math.floor(wx(x)), MapTile.SIZE);
        int rx1 = Math.floorDiv((int) Math.floor(wx(x + w)), MapTile.SIZE);
        int rz0 = Math.floorDiv((int) Math.floor(wz(y)), MapTile.SIZE);
        int rz1 = Math.floorDiv((int) Math.floor(wz(y + h)), MapTile.SIZE);
        PoseStack pose = g.pose();
        for (int rz = rz0; rz <= rz1; rz++) {
            for (int rx = rx0; rx <= rx1; rx++) {
                MapTile t = layer.getForRender(rx, rz);
                if (t == null) {
                    continue;
                }
                pose.pushPose();
                pose.translate(sx(rx * (double) MapTile.SIZE), sy(rz * (double) MapTile.SIZE), 0);
                pose.scale(zoom, zoom, 1f);
                g.blit(RenderType::guiTextured, t.id, 0, 0, 0f, 0f, MapTile.SIZE, MapTile.SIZE, MapTile.SIZE, MapTile.SIZE);
                pose.popPose();
            }
        }
    }

    /** Faint chunk grid when zoomed in far enough. */
    public void drawGrid(GuiGraphics g) {
        if (zoom < 3f) {
            return;
        }
        int color = zoom >= 6 ? Paper.a(Paper.INK, 0x22) : Paper.a(Paper.INK, 0x16);
        int bx0 = Math.floorDiv((int) Math.floor(wx(x)), 16) * 16;
        int bz0 = Math.floorDiv((int) Math.floor(wz(y)), 16) * 16;
        for (int bx = bx0; sx(bx) < x + w; bx += 16) {
            int px = Math.round(sx(bx));
            if (px >= x) {
                g.fill(px, y, px + 1, y + h, color);
            }
        }
        for (int bz = bz0; sy(bz) < y + h; bz += 16) {
            int py = Math.round(sy(bz));
            if (py >= y) {
                g.fill(x, py, x + w, py + 1, color);
            }
        }
    }

    public void drawStrokes(GuiGraphics g, List<Stroke> strokes, float widthScale) {
        for (Stroke s : strokes) {
            drawStroke(g, s, widthScale);
        }
    }

    public void drawStroke(GuiGraphics g, Stroke s, float widthScale) {
        double[] p = s.pts;
        if (p.length < 2) {
            return;
        }
        float wpx = Math.max(1f, s.width * widthScale);
        int c = s.color;
        switch (s.type) {
            case Stroke.LINE -> {
                if (p.length >= 4) {
                    seg(g, p[0], p[1], p[2], p[3], wpx, c);
                }
            }
            case Stroke.RECT -> {
                if (p.length >= 4) {
                    seg(g, p[0], p[1], p[2], p[1], wpx, c);
                    seg(g, p[2], p[1], p[2], p[3], wpx, c);
                    seg(g, p[2], p[3], p[0], p[3], wpx, c);
                    seg(g, p[0], p[3], p[0], p[1], wpx, c);
                    if (wpx >= 2) {
                        Paper.dot(g, sx(p[0]), sy(p[1]), wpx, c);
                        Paper.dot(g, sx(p[2]), sy(p[1]), wpx, c);
                        Paper.dot(g, sx(p[2]), sy(p[3]), wpx, c);
                        Paper.dot(g, sx(p[0]), sy(p[3]), wpx, c);
                    }
                }
            }
            case Stroke.CIRCLE -> {
                if (p.length >= 4) {
                    double r = Math.hypot(p[2] - p[0], p[3] - p[1]);
                    float rs = (float) (r * zoom);
                    int segs = Math.max(16, Math.min(128, (int) (rs / 2)));
                    double px = p[0] + r;
                    double pz = p[1];
                    for (int i = 1; i <= segs; i++) {
                        double a = i * Math.PI * 2 / segs;
                        double nx = p[0] + Math.cos(a) * r;
                        double nz = p[1] + Math.sin(a) * r;
                        seg(g, px, pz, nx, nz, wpx, c);
                        px = nx;
                        pz = nz;
                    }
                }
            }
            default -> {
                if (p.length == 2) {
                    Paper.dot(g, sx(p[0]), sy(p[1]), wpx, c);
                }
                for (int i = 0; i + 3 < p.length; i += 2) {
                    seg(g, p[i], p[i + 1], p[i + 2], p[i + 3], wpx, c);
                    if (wpx >= 2) {
                        Paper.dot(g, sx(p[i + 2]), sy(p[i + 3]), wpx, c);
                    }
                }
            }
        }
    }

    private void seg(GuiGraphics g, double x1, double z1, double x2, double z2, float w, int c) {
        float ax = sx(x1);
        float ay = sy(z1);
        float bx = sx(x2);
        float by = sy(z2);
        // cheap culling
        float minX = Math.min(ax, bx) - w;
        float maxX = Math.max(ax, bx) + w;
        float minY = Math.min(ay, by) - w;
        float maxY = Math.max(ay, by) + w;
        if (maxX < x || minX > x + w() || maxY < y || minY > y + h) {
            return;
        }
        Paper.line(g, ax, ay, bx, by, w, c);
    }

    private int w() {
        return w;
    }

    /** Distance (in screen pixels) from a point to a stroke, used by the eraser. */
    public double distanceTo(Stroke s, double mx, double my) {
        double[] p = s.pts;
        double best = Double.MAX_VALUE;
        switch (s.type) {
            case Stroke.RECT -> {
                if (p.length >= 4) {
                    best = Math.min(best, segDist(mx, my, p[0], p[1], p[2], p[1]));
                    best = Math.min(best, segDist(mx, my, p[2], p[1], p[2], p[3]));
                    best = Math.min(best, segDist(mx, my, p[2], p[3], p[0], p[3]));
                    best = Math.min(best, segDist(mx, my, p[0], p[3], p[0], p[1]));
                }
            }
            case Stroke.CIRCLE -> {
                if (p.length >= 4) {
                    double r = Math.hypot(p[2] - p[0], p[3] - p[1]) * zoom;
                    double d = Math.hypot(mx - sx(p[0]), my - sy(p[1]));
                    best = Math.abs(d - r);
                }
            }
            default -> {
                if (p.length == 2) {
                    best = Math.hypot(mx - sx(p[0]), my - sy(p[1]));
                }
                for (int i = 0; i + 3 < p.length; i += 2) {
                    best = Math.min(best, segDist(mx, my, p[i], p[i + 1], p[i + 2], p[i + 3]));
                }
            }
        }
        return best;
    }

    private double segDist(double mx, double my, double x1, double z1, double x2, double z2) {
        double ax = sx(x1);
        double ay = sy(z1);
        double bx = sx(x2);
        double by = sy(z2);
        double dx = bx - ax;
        double dy = by - ay;
        double l2 = dx * dx + dy * dy;
        double t = l2 == 0 ? 0 : Math.max(0, Math.min(1, ((mx - ax) * dx + (my - ay) * dy) / l2));
        return Math.hypot(mx - (ax + t * dx), my - (ay + t * dy));
    }

    /**
     * Draws markers of the given dimension.
     *
     * @param clampToEdge if true, markers outside the view are drawn small at the border (minimap)
     */
    public void drawMarkers(GuiGraphics g, Font font, List<Marker> markers, String dim, int size, boolean labels,
                            String selectedId, String hoveredId, boolean clampToEdge) {
        for (Marker m : markers) {
            if (m.hidden || !m.dim.equals(dim)) {
                continue;
            }
            float px = sx(m.x + 0.5);
            float py = sy(m.z + 0.5);
            int s = size;
            boolean outside = px < x || py < y || px >= x + w || py >= y + h;
            if (outside) {
                if (!clampToEdge) {
                    if (px < x - size || py < y - size || px > x + w + size || py > y + h + size) {
                        continue;
                    }
                } else {
                    s = Math.max(6, size * 2 / 3);
                    px = Math.max(x + s / 2f, Math.min(x + w - s / 2f, px));
                    py = Math.max(y + s / 2f, Math.min(y + h - s / 2f, py));
                }
            }
            int ix = Math.round(px - s / 2f);
            int iy = Math.round(py - s / 2f);
            boolean selected = m.id.equals(selectedId);
            boolean hovered = m.id.equals(hoveredId);
            if (selected || hovered) {
                int pad = 2;
                g.fill(ix - pad, iy - pad, ix + s + pad, iy + s + pad, selected ? Paper.a(Paper.GOLD_LIGHT, 0x60) : 0x40FFFFFF);
                Paper.outline(g, ix - pad, iy - pad, s + pad * 2, s + pad * 2, selected ? Paper.GOLD : Paper.INK_SOFT);
            }
            Paper.markerIcon(g, m.icon, m.color, ix, iy, s, outside ? 0xB0 : 0xFF);
            if (labels && !outside && (zoom >= 1.5f || selected || hovered) && !m.name.isEmpty()) {
                String name = font.plainSubstrByWidth(m.name, 120);
                int tw = font.width(name);
                int tx = Math.round(px - tw / 2f);
                int ty = iy + s + 2;
                g.fill(tx - 3, ty - 2, tx + tw + 3, ty + 9, Paper.a(Paper.CREAM, 0xD0));
                Paper.outline(g, tx - 3, ty - 2, tw + 6, 11, Paper.a(Paper.INK, 0x80));
                g.drawString(font, name, tx, ty, Paper.INK, false);
            }
        }
    }

    public Marker markerAt(List<Marker> markers, String dim, double mx, double my, int size) {
        Marker best = null;
        double bestD = size * 0.75;
        for (Marker m : markers) {
            if (m.hidden || !m.dim.equals(dim)) {
                continue;
            }
            double d = Math.max(Math.abs(mx - sx(m.x + 0.5)), Math.abs(my - sy(m.z + 0.5)));
            if (d <= bestD) {
                bestD = d;
                best = m;
            }
        }
        return best;
    }

    public void drawPlayer(GuiGraphics g, double px, double pz, float yaw, int size) {
        float sx = sx(px);
        float sy = sy(pz);
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(sx, sy, 0);
        pose.mulPose(Axis.ZP.rotation((float) Math.toRadians(yaw + 180)));
        g.blit(RenderType::guiTextured, Paper.tool("arrow"), -size / 2, -size / 2, 0f, 0f, size, size, 16, 16, 16, 16);
        pose.popPose();
    }

    public static void newFrame() {
        MapLayer.newFrame();
    }
}
