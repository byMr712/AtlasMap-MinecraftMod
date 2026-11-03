package org.webtrade.atlasmapextended.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/** A 256x256 block region of the map, one pixel per block, backed by a GPU texture. */
public class MapTile {
    public static final int SIZE = 256;
    public static final int SHIFT = 8;

    public final int rx;
    public final int rz;
    public final Identifier id;
    private final DynamicTexture texture;
    boolean textureDirty;
    boolean diskDirty;
    long lastUsed = System.currentTimeMillis();

    MapTile(String key, int rx, int rz, NativeImage image) {
        this.rx = rx;
        this.rz = rz;
        this.id = Identifier.fromNamespaceAndPath("atlasmapextended", "tile/" + key + "/" + rx + "_" + rz);
        this.texture = new DynamicTexture(() -> "atlasmapextended tile " + key + " " + rx + "," + rz, image);
        Minecraft.getInstance().getTextureManager().register(id, texture);
    }

    public void setPixel(int x, int z, int argb) {
        NativeImage img = texture.getPixels();
        if (img != null && img.getPixel(x, z) != argb) {
            img.setPixel(x, z, argb);
            textureDirty = true;
            diskDirty = true;
        }
    }

    public int getPixel(int x, int z) {
        NativeImage img = texture.getPixels();
        return img == null ? 0 : img.getPixel(x, z);
    }

    public void touch() {
        lastUsed = System.currentTimeMillis();
    }

    void upload() {
        if (textureDirty) {
            texture.upload();
            textureDirty = false;
        }
    }

    /** Copies the pixels so that they can be written to disk off-thread. */
    NativeImage snapshot() {
        NativeImage copy = new NativeImage(SIZE, SIZE, false);
        copy.copyFrom(texture.getPixels());
        return copy;
    }

    void release() {
        Minecraft.getInstance().getTextureManager().release(id);
    }
}
