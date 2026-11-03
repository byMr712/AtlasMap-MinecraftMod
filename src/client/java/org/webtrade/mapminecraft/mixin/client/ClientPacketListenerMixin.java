package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.mapminecraft.client.map.MapManager;

/** Replaces Fabric API's join and disconnect events. */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void mapminecraft$handleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        MapManager.onJoin(Minecraft.getInstance());
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void mapminecraft$close(CallbackInfo ci) {
        MapManager.onDisconnect();
    }
}
