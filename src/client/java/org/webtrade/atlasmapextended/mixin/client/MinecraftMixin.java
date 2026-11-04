package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmapextended.client.AtlasMapExtendedCommon;
import org.webtrade.atlasmapextended.client.data.ModConfig;
import org.webtrade.atlasmapextended.client.gui.WorldMarkerHud;
import org.webtrade.atlasmapextended.client.map.MapManager;

/** Replaces Fabric API's client tick and client stopping events. */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void atlasmapextended$tick(CallbackInfo ci) {
        AtlasMapExtendedCommon.onTick((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void atlasmapextended$close(CallbackInfo ci) {
        MapManager.onDisconnect();
    }

    /** Queue the screenshot so the next frame can be drawn without world marker icons. */
    @Redirect(method = "handleGlobalKeyPress",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Screenshot;grab(Lnet/minecraft/client/Minecraft;Z)V"))
    private void atlasmapextended$grabScreenshot(Minecraft mc, boolean panorama) {
        ModConfig cfg = ModConfig.get();
        if (cfg.worldMarkers && cfg.markersVisible) {
            WorldMarkerHud.requestScreenshot(panorama);
        } else {
            Screenshot.grab(mc, panorama);
        }
    }
}
