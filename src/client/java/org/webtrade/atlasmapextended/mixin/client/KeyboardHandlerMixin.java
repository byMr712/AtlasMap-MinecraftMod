package org.webtrade.atlasmapextended.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.webtrade.atlasmapextended.client.data.ModConfig;
import org.webtrade.atlasmapextended.client.gui.WorldMarkerHud;

import java.io.File;
import java.util.function.Consumer;

/** Defers the F2 screenshot when world marker icons are visible. */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Redirect(method = "keyPress",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"))
    private void atlasmapextended$grabScreenshot(File directory, RenderTarget target,
                                                 Consumer<Component> messageConsumer) {
        ModConfig cfg = ModConfig.get();
        if (cfg.worldMarkers && cfg.markersVisible && WorldMarkerHud.hasVisibleMarkers()) {
            WorldMarkerHud.requestScreenshot();
        } else {
            Screenshot.grab(directory, target, messageConsumer);
        }
    }
}
