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
    public boolean filled = true;
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
        r.filled = filled;
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
        filled = other.filled;
        screenshots = new ArrayList<>(other.screenshots);
        shotDrawings = new HashMap<>();
        other.shotDrawings.forEach((k, v) -> shotDrawings.put(k, new ArrayList<>(v)));
    }

    /** True when two non-adjacent edges of the polygon cross each other. */
    public static boolean selfIntersects(double[] pts) {
        int n = pts.length / 2;
        if (n < 4) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            int i2 = (i + 1) % n;
            for (int j = i + 1; j < n; j++) {
                int j2 = (j + 1) % n;
                if (i == j || i2 == j || j2 == i) {
                    continue;
                }
                if (segmentsCross(pts[i * 2], pts[i * 2 + 1], pts[i2 * 2], pts[i2 * 2 + 1],
                        pts[j * 2], pts[j * 2 + 1], pts[j2 * 2], pts[j2 * 2 + 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean segmentsCross(double ax, double az, double bx, double bz,
                                         double cx, double cz, double dx, double dz) {
        double d1 = side(cx, cz, dx, dz, ax, az);
        double d2 = side(cx, cz, dx, dz, bx, bz);
        double d3 = side(ax, az, bx, bz, cx, cz);
        double d4 = side(ax, az, bx, bz, dx, dz);
        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
                && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private static double side(double ax, double az, double bx, double bz, double px, double pz) {
        return (bx - ax) * (pz - az) - (bz - az) * (px - ax);
    }

    /** Convex hull of the points (monotone chain); used to clean up tangled freehand regions. */
    public static double[] convexHull(double[] pts) {
        int n = pts.length / 2;
        if (n < 3) {
            return pts.clone();
        }
        double[][] p = new double[n][2];
        for (int i = 0; i < n; i++) {
            p[i][0] = pts[i * 2];
            p[i][1] = pts[i * 2 + 1];
        }
        java.util.Arrays.sort(p, (a, b) -> a[0] != b[0]
                ? Double.compare(a[0], b[0]) : Double.compare(a[1], b[1]));
        double[][] hull = new double[2 * n][2];
        int k = 0;
        for (int i = 0; i < n; i++) {
            while (k >= 2 && cross(hull[k - 2], hull[k - 1], p[i]) <= 0) {
                k--;
            }
            hull[k++] = p[i];
        }
        int lower = k + 1;
        for (int i = n - 2; i >= 0; i--) {
            while (k >= lower && cross(hull[k - 2], hull[k - 1], p[i]) <= 0) {
                k--;
            }
            hull[k++] = p[i];
        }
        int m = Math.max(3, k - 1);
        double[] out = new double[m * 2];
        for (int i = 0; i < m; i++) {
            out[i * 2] = hull[i][0];
            out[i * 2 + 1] = hull[i][1];
        }
        return out;
    }

    private static double cross(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
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
