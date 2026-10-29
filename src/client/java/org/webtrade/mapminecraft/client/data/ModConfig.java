package org.webtrade.mapminecraft.client.data;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;

/** Client settings, stored in {@code config/mapminecraft.json}. */
public class ModConfig {
    public static final float[] MINIMAP_ZOOMS = {0.5f, 1f, 1.5f, 2f, 3f, 4f};
    public static final int[] MINIMAP_SIZES = {80, 110, 140, 170};

    public boolean minimapEnabled = true;
    public int minimapSizeIndex = 1;
    public int minimapZoomIndex = 3;
    /** 0 = top right, 1 = top left, 2 = bottom right, 3 = bottom left. */
    public int minimapCorner = 0;
    public boolean minimapCoords = true;
    public boolean minimapMarkers = true;
    public boolean drawingsVisible = true;
    public boolean markersVisible = true;
    public int markerSort = 0;

    private static ModConfig instance;

    public static ModConfig get() {
        if (instance == null) {
            instance = Storage.readJson(file(), ModConfig.class, new ModConfig());
            instance.clamp();
        }
        return instance;
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("mapminecraft.json");
    }

    private void clamp() {
        minimapSizeIndex = Math.floorMod(minimapSizeIndex, MINIMAP_SIZES.length);
        minimapZoomIndex = Math.max(0, Math.min(MINIMAP_ZOOMS.length - 1, minimapZoomIndex));
        minimapCorner = Math.floorMod(minimapCorner, 4);
    }

    public void save() {
        clamp();
        Storage.writeJson(file(), this);
    }

    public int minimapSize() {
        return MINIMAP_SIZES[minimapSizeIndex];
    }

    public float minimapZoom() {
        return MINIMAP_ZOOMS[minimapZoomIndex];
    }
}
