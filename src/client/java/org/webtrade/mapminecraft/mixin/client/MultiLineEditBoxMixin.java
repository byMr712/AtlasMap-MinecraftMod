package org.webtrade.mapminecraft.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Disables the vanilla MultiLineEditBox text shadow on 1.21-1.21.5. */
@Mixin(MultiLineEditBox.class)
public class MultiLineEditBoxMixin {

    @Redirect(method = "renderContents", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private int mod$noShadowStr(GuiGraphics g, Font font, String text, int x, int y, int color) {
        return g.drawString(font, text, x, y, color, false);
    }

    @Redirect(method = "renderDecorations", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private int mod$noShadowComp(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        return g.drawString(font, text, x, y, color, false);
    }
}
