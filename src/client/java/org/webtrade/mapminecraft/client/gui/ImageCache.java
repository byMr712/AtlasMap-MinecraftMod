package org.webtrade.mapminecraft.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.webtrade.mapminecraft.client.data.Storage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Loads screenshots asynchronously into GPU textures, either as thumbnails or in full size. */
public final class ImageCache {
    private static final int THUMB_WIDTH = 240;
    private static final ExecutorService DECODER = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "MapMinecraft-Images");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    public static final class Entry {
        public ResourceLocation id;
        public int width;
        public int height;
        /** Size of the source image (before thumbnail scaling). */
        public int origWidth;
        public int origHeight;
        public boolean failed;
        boolean closed;

        public boolean ready() {
            return id != null;
        }
    }

    /** A dynamic texture sampled linearly so that downscaled photos look smooth. */
    private static final class SmoothTexture extends DynamicTexture {
        SmoothTexture(String label, NativeImage image) {
            super(image);
            this.setFilter(true, false);
        }
    }

    private ImageCache() {
    }

    public static Entry get(Path file, boolean thumb) {
        String key = (thumb ? "t:" : "f:") + file.toAbsolutePath();
        Entry e = ENTRIES.get(key);
        if (e != null) {
            return e;
        }
        Entry entry = new Entry();
        ENTRIES.put(key, entry);
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture.supplyAsync(() -> {
            try (InputStream in = Files.newInputStream(file)) {
                NativeImage img = NativeImage.read(in);
                entry.origWidth = img.getWidth();
                entry.origHeight = img.getHeight();
                if (thumb && img.getWidth() > THUMB_WIDTH) {
                    int h = Math.max(1, img.getHeight() * THUMB_WIDTH / img.getWidth());
                    NativeImage small = new NativeImage(THUMB_WIDTH, h, false);
                    img.resizeSubRectTo(0, 0, img.getWidth(), img.getHeight(), small);
                    img.close();
                    return small;
                }
                return img;
            } catch (Exception ex) {
                Storage.LOG.warn("Failed to load image {}", file, ex);
                return null;
            }
        }, DECODER).thenAcceptAsync(img -> {
            if (img == null) {
                entry.failed = true;
                return;
            }
            if (entry.closed) {
                img.close();
                return;
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mapminecraft", "image/" + COUNTER.incrementAndGet());
            entry.width = img.getWidth();
            entry.height = img.getHeight();
            mc.getTextureManager().register(id, new SmoothTexture("mapminecraft image", img));
            entry.id = id;
        }, mc);
        return entry;
    }

    /** Draws the image fitted into the box, keeping its aspect ratio. */
    public static void drawFit(GuiGraphics g, Entry e, int x, int y, int w, int h) {
        if (e == null || !e.ready()) {
            return;
        }
        float s = Math.min(w / (float) e.width, h / (float) e.height);
        int dw = Math.max(1, Math.round(e.width * s));
        int dh = Math.max(1, Math.round(e.height * s));
        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;
        g.blit(e.id, dx, dy, dw, dh, 0, 0, e.width, e.height, e.width, e.height);
    }

    /** Draws the image covering the box (cropping the overflow). */
    public static void drawCover(GuiGraphics g, Entry e, int x, int y, int w, int h) {
        if (e == null || !e.ready()) {
            return;
        }
        float s = Math.max(w / (float) e.width, h / (float) e.height);
        int uw = Math.max(1, Math.round(w / s));
        int vh = Math.max(1, Math.round(h / s));
        int u = (e.width - uw) / 2;
        int v = (e.height - vh) / 2;
        g.blit(e.id, x, y, w, h, u, v, uw, vh, e.width, e.height);
    }

    public static void clear() {
        Minecraft mc = Minecraft.getInstance();
        for (Entry e : ENTRIES.values()) {
            e.closed = true;
            if (e.id != null) {
                mc.getTextureManager().release(e.id);
            }
        }
        ENTRIES.clear();
    }

    /** Copies an image file into a world's screenshot folder as PNG. Runs on the IO thread. */
    public static CompletableFuture<String> importImage(Path source, Path targetDir) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream in = Files.newInputStream(source)) {
                NativeImage img = NativeImage.read(in);
                Files.createDirectories(targetDir);
                String name = java.util.UUID.randomUUID() + ".png";
                try {
                    img.writeToFile(targetDir.resolve(name));
                } finally {
                    img.close();
                }
                return name;
            } catch (Exception ex) {
                Storage.LOG.warn("Failed to import image {}", source, ex);
                return null;
            }
        }, Storage.IO);
    }
}
