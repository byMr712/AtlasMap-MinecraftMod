package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.mapminecraft.client.MapminecraftCommon;
import org.webtrade.mapminecraft.client.map.MapManager;

/** Replaces Fabric API's client tick and client stopping events. */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void mapminecraft$tick(CallbackInfo ci) {
        MapminecraftCommon.onTick((Minecraft) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void mapminecraft$close(CallbackInfo ci) {
        MapManager.onDisconnect();
    }
}
