package org.webtrade.atlasmapextended.mixin.client;

import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.webtrade.atlasmapextended.client.ModLanguage;
import org.webtrade.atlasmapextended.client.gui.T;

import java.util.List;
import java.util.Map;

/** Adds the mod's own translations to the final vanilla language, so key bindings and vanilla UI use them. */
@Mixin(LanguageManager.class)
public class LanguageManagerMixin {

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void atlasmapextended$addTranslations(ResourceManager manager, CallbackInfo ci) {
        Language current = Language.getInstance();
        String selected = ((LanguageManager) (Object) this).getSelected();
        Map<String, String> extra = T.clientTranslations(List.of(selected));
        if (current instanceof ModLanguage mod) {
            mod.setExtra(extra);
        } else {
            Language.inject(new ModLanguage(current, extra));
        }
    }
}
