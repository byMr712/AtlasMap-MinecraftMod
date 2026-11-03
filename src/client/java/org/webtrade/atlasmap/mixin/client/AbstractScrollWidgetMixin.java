package org.webtrade.atlasmap.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineEditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the vanilla dark background/border of the description field on 1.21-1.21.3. */
@Mixin(AbstractScrollWidget.class)
public class AbstractScrollWidgetMixin {

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void mod$noBackground(GuiGraphics g, CallbackInfo ci) {
        if ((Object) this instanceof MultiLineEditBox) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBorder", at = @At("HEAD"), cancellable = true)
    private void mod$noBorder(GuiGraphics g, int x, int y, int w, int h, CallbackInfo ci) {
        if ((Object) this instanceof MultiLineEditBox) {
            ci.cancel();
        }
    }
}
