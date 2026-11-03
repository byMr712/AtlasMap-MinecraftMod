package org.webtrade.atlasmapextended.client.data;

import java.util.UUID;

/** A named, coloured area on the map. Points are world x/z pairs and the polygon is closed. */
public class Region {
    public String id = UUID.randomUUID().toString();
    public String name = "";
    public int color = 0xFFB88C2C;
    public String dim = "minecraft:overworld";
    public double[] pts = new double[0];
    public boolean hidden;

    public int pointCount() {
        return pts.length / 2;
    }

    public Region copy() {
        Region r = new Region();
        r.id = id;
        r.name = name;
        r.color = color;
        r.dim = dim;
        r.pts = pts.clone();
        r.hidden = hidden;
        return r;
    }

    public void apply(Region other) {
        name = other.name;
        color = other.color;
        dim = other.dim;
        pts = other.pts.clone();
        hidden = other.hidden;
    }

    /** Even-odd point-in-polygon test in world coordinates. */
    public boolean contains(double x, double z) {
        int n = pointCount();
        if (n < 3) {
            return false;
        }
        boolean inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = pts[i * 2];
            double zi = pts[i * 2 + 1];
            double xj = pts[j * 2];
            double zj = pts[j * 2 + 1];
            if ((zi > z) != (zj > z) && x < (xj - xi) * (z - zi) / (zj - zi) + xi) {
                inside = !inside;
            }
        }
        return inside;
    }
}
