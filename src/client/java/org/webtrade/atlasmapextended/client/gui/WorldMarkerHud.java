package org.webtrade.atlasmapextended.client.gui;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.webtrade.atlasmapextended.client.data.Marker;
import org.webtrade.atlasmapextended.client.data.ModConfig;
import org.webtrade.atlasmapextended.client.map.MapManager;
import org.webtrade.atlasmapextended.client.map.MapSession;

import java.util.HashMap;
import java.util.Map;

/** Draws marker icons over the game world while playing. */
public final class WorldMarkerHud {
    private static final int ICON_SIZE = 20;
    private static final double FADE_RADIUS_SQ = 5.0 * 5.0;

    // Reused every frame so rendering does not allocate per marker.
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final Vector3f POINT = new Vector3f();
    private static final Map<String, Identifier> PLAIN_ICONS = new HashMap<>();
    private static final Map<String, Identifier> TINTED_ICONS = new HashMap<>();

    private WorldMarkerHud() {
    }

    public static void render(GuiGraphicsExtractor g) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.worldMarkers || !cfg.markersVisible) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        MapSession session = MapManager.live();
        if (p == null || mc.level == null || session == null || mc.gui.screen() != null || session.markers.isEmpty()) {
            return;
        }
        Camera camera = mc.gameRenderer.mainCamera();
        if (camera == null || !camera.isInitialized()) {
            return;
        }
        String dim = mc.level.dimension().identifier().toString();
        Vec3 eye = camera.position();
        Vector3fc forward = camera.forwardVector();
        camera.getViewRotationProjectionMatrix(PROJECTION);

        int sw = g.guiWidth();
        int sh = g.guiHeight();
        for (Marker m : session.markers) {
            if (m.hidden || !m.dim.equals(dim)) {
                continue;
            }
            double dx = m.x + 0.5 - eye.x;
            double dy = m.y + 1.0 - eye.y;
            double dz = m.z + 0.5 - eye.z;
            if (dx * forward.x() + dy * forward.y() + dz * forward.z() <= 0) {
                continue;
            }
            POINT.set((float) dx, (float) dy, (float) dz);
            PROJECTION.transformProject(POINT);
            if (POINT.z < -1 || POINT.z > 1) {
                continue;
            }
            int sx = Math.round((POINT.x + 1) * 0.5f * sw);
            int sy = Math.round((1 - POINT.y) * 0.5f * sh);
            if (sx < -ICON_SIZE || sx > sw + ICON_SIZE || sy < -ICON_SIZE || sy > sh + ICON_SIZE) {
                continue;
            }
            int alpha = dx * dx + dy * dy + dz * dz <= FADE_RADIUS_SQ ? 128 : 255;
            Identifier texture = m.color == 0
                    ? PLAIN_ICONS.computeIfAbsent(m.icon, Paper::marker)
                    : TINTED_ICONS.computeIfAbsent(m.icon,
                            icon -> Paper.id("textures/gui/markers_tint/" + icon + ".png"));
            int tint = m.color == 0 ? alpha << 24 | 0xFFFFFF : Paper.a(m.color, alpha);
            Paper.icon(g, texture, sx - ICON_SIZE / 2, sy - ICON_SIZE / 2, ICON_SIZE, tint);
        }
    }
}
