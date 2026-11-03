package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.mapminecraft.client.MapminecraftCommon;

/** Replaces Fabric API's HUD element registration for the minimap. */
@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void mapminecraft$renderMinimap(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        MapminecraftCommon.renderMinimap(g, delta);
    }
}
