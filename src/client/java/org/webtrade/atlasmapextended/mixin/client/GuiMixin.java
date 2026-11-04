package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmapextended.client.AtlasMapExtendedCommon;
import org.webtrade.atlasmapextended.client.gui.WorldMarkerHud;

/** Replaces Fabric API's HUD element registration for the minimap. */
@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void atlasmapextended$renderWorldMarkers(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        WorldMarkerHud.render(g);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void atlasmapextended$renderMinimap(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        AtlasMapExtendedCommon.renderMinimap(g, delta);
    }
}
