package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.mapminecraft.client.MapminecraftCommon;

/** Replaces Fabric API's screen init event for the title screen buttons. */
@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void mapminecraft$addAtlasButton(CallbackInfo ci) {
        MapminecraftCommon.addAtlasButton((Screen) (Object) this);
    }
}
