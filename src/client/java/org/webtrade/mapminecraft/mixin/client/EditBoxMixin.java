package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Disables the vanilla EditBox text shadow on 1.21-1.21.5 (those versions have no setTextShadow). */
@Mixin(EditBox.class)
public class EditBoxMixin {

    @Redirect(method = "renderWidget", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int mod$noShadowSeq(GuiGraphics g, Font font, FormattedCharSequence text, int x, int y, int color) {
        return g.drawString(font, text, x, y, color, false);
    }

    @Redirect(method = "renderWidget", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int mod$noShadowStr(GuiGraphics g, Font font, String text, int x, int y, int color) {
        return g.drawString(font, text, x, y, color, false);
    }

    @Redirect(method = "renderWidget", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int mod$noShadowComp(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        return g.drawString(font, text, x, y, color, false);
    }
}
