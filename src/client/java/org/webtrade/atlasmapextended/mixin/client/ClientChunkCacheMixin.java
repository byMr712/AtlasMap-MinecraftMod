package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.webtrade.atlasmapextended.client.map.MapManager;

import java.util.Map;
import java.util.function.Consumer;

/** Replaces Fabric API's client chunk load event. */
@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {

    @Inject(method = "replaceWithPacketData", at = @At("RETURN"))
    private void atlasmapextended$chunkLoaded(int x, int z, FriendlyByteBuf buffer,
                                           Map<Heightmap.Types, long[]> heightmaps,
                                           Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntityTagOutput,
                                           CallbackInfoReturnable<LevelChunk> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            MapManager.onChunkLoad(level, cir.getReturnValue());
        }
    }
}
