package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Vanilla sorts key bindings by a private category order map that contains vanilla categories only.
 * Fabric API fills it for modded categories; this mod is dependency-free, so it does the same here.
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("CATEGORY_SORT_ORDER")
    static Map<String, Integer> getCategorySortOrder() {
        throw new AssertionError();
    }
}
