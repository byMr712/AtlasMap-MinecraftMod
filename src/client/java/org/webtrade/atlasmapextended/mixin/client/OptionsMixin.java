package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmapextended.client.ModKeys;

import java.util.Arrays;

/**
 * Replaces Fabric API's key mapping registration. The vanilla {@code Options.load()}
 * call runs at the end of the {@code Options} constructor, so our keys are present
 * before the saved key bindings are applied and before the controls screen is built.
 */
@Mixin(Options.class)
public class OptionsMixin {

    @Shadow
    @Final
    @Mutable
    public KeyMapping[] keyMappings;

    @Inject(method = "load", at = @At("HEAD"))
    private void atlasmapextended$appendKeyMappings(CallbackInfo ci) {
        KeyMapping[] custom = ModKeys.createAll();
        if (custom.length == 0) {
            return;
        }
        KeyMapping[] merged = Arrays.copyOf(keyMappings, keyMappings.length + custom.length);
        System.arraycopy(custom, 0, merged, keyMappings.length, custom.length);
        keyMappings = merged;
    }
}
