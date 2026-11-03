package org.webtrade.atlasmapextended.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A named, coloured area on the map. Points are world x/z pairs and the polygon is closed. */
public class Region {
    public String id = UUID.randomUUID().toString();
    public String name = "";
    public String description = "";
    public int color = 0xFFB88C2C;
    public String dim = "minecraft:overworld";
    public double[] pts = new double[0];
    public boolean hidden;
    public long created = System.currentTimeMillis();
    public List<String> screenshots = new ArrayList<>();
    public Map<String, List<Stroke>> shotDrawings = new HashMap<>();

    public int pointCount() {
        return pts.length / 2;
    }

    public Region copy() {
        Region r = new Region();
        r.id = id;
        r.name = name;
        r.description = description;
        r.color = color;
        r.dim = dim;
        r.pts = pts.clone();
        r.hidden = hidden;
        r.created = created;
        r.screenshots = new ArrayList<>(screenshots);
        r.shotDrawings = new HashMap<>();
        shotDrawings.forEach((k, v) -> r.shotDrawings.put(k, new ArrayList<>(v)));
        return r;
    }

    public void apply(Region other) {
        name = other.name;
        description = other.description;
        color = other.color;
        dim = other.dim;
        pts = other.pts.clone();
        hidden = other.hidden;
        screenshots = new ArrayList<>(other.screenshots);
        shotDrawings = new HashMap<>();
        other.shotDrawings.forEach((k, v) -> shotDrawings.put(k, new ArrayList<>(v)));
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
