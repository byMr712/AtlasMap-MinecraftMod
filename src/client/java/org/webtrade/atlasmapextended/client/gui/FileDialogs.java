package org.webtrade.atlasmapextended.client.gui;

import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import org.webtrade.atlasmapextended.client.data.Storage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Native "open file" dialog via SDL3. It is asynchronous, so the game does not freeze. */
public final class FileDialogs {
    private static volatile boolean open;

    private FileDialogs() {
    }

    public static void openImages(String title, Path startDir, Consumer<List<Path>> onChosen) {
        if (open) {
            return;
        }
        open = true;
        // SDL reads the filters until the callback fires, so they live in malloc'd memory freed there.
        ByteBuffer name = MemoryUtil.memUTF8("Images");
        ByteBuffer pattern = MemoryUtil.memUTF8("png;jpg;jpeg");
        SDL_DialogFileFilter.Buffer filters = SDL_DialogFileFilter.calloc(1);
        filters.get(0).name(name).pattern(pattern);
        SDL_DialogFileCallback[] holder = new SDL_DialogFileCallback[1];
        holder[0] = SDL_DialogFileCallback.create((userdata, filelist, filter) -> {
            try {
                List<Path> paths = new ArrayList<>();
                if (filelist != 0L) {
                    for (int i = 0; ; i++) {
                        long p = MemoryUtil.memGetAddress(filelist + (long) i * PointerBuffer.POINTER_SIZE);
                        if (p == 0L) {
                            break;
                        }
                        paths.add(Path.of(MemoryUtil.memUTF8(p)));
                    }
                }
                if (!paths.isEmpty()) {
                    Minecraft.getInstance().execute(() -> onChosen.accept(paths));
                }
            } catch (Throwable e) {
                Storage.LOG.warn("File dialog failed", e);
            } finally {
                filters.free();
                MemoryUtil.memFree(name);
                MemoryUtil.memFree(pattern);
                // never free a native closure while it is still running
                Minecraft.getInstance().execute(holder[0]::free);
                open = false;
            }
        });
        try {
            Path dir = Files.isDirectory(startDir) ? startDir : startDir.getParent();
            String start = dir.toAbsolutePath() + File.separator;
            long window = Minecraft.getInstance().getWindow().handle();
            SDLDialog.SDL_ShowOpenFileDialog(holder[0], 0L, window, filters, start, true);
        } catch (Throwable e) {
            Storage.LOG.warn("File dialog failed", e);
            filters.free();
            MemoryUtil.memFree(name);
            MemoryUtil.memFree(pattern);
            holder[0].free();
            open = false;
        }
    }
}
