package org.webtrade.atlasmapextended.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

/**
 * A screen-sized ARGB layer for region fills and outlines. Everything is rasterised on the CPU
 * once per view change and drawn with a single blit, so thousands of regions cost one draw call
 * per frame instead of one per edge.
 */
final class RegionOverlay {
    private static int counter;
    private final ResourceLocation id;
    private DynamicTexture texture;
    private int[] pixels = new int[0];
    private int width;
    private int height;
    private boolean dirty = true;

    RegionOverlay() {
        id = ResourceLocation.fromNamespaceAndPath("atlasmapextended", "region_overlay/" + (counter++));
    }

    /** Clears the buffer for the given size and returns it for rasterisation. */
    int[] begin(int w, int h) {
        if (texture == null || w != width || h != height) {
            release();
            width = w;
            height = h;
            NativeImage image = new NativeImage(w, h, true);
            texture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(id, texture);
            pixels = new int[w * h];
        }
        Arrays.fill(pixels, 0);
        dirty = true;
        return pixels;
    }

    /** Uploads the pixels (only after a rebuild) and draws the layer at the given screen position. */
    void draw(GuiGraphics g, int x, int y) {
        if (texture == null || width <= 0 || height <= 0) {
            return;
        }
        if (dirty) {
            NativeImage image = texture.getPixels();
            if (image != null) {
                for (int py = 0; py < height; py++) {
                    int row = py * width;
                    for (int px = 0; px < width; px++) {
                        image.setPixelRGBA(px, py, swap(pixels[row + px]));
                    }
                }
                texture.upload();
            }
            dirty = false;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(id, x, y, 0f, 0f, width, height, width, height);
    }

    private static int swap(int argb) {
        return (argb & 0xFF00FF00) | (argb & 0xFF) << 16 | (argb >> 16) & 0xFF;
    }

    void release() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(id);
            texture = null;
        }
    }
}
