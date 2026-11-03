package org.webtrade.mapminecraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Small themed icon button on the title screen (map atlas, settings). */
public class TitleButton extends AbstractWidget {
    private final String icon;
    private final Runnable onPress;

    public TitleButton(int x, int y, String icon, Component label, Runnable onPress) {
        super(x, y, 20, 20, label);
        this.icon = icon;
        this.onPress = onPress;
        setTooltip(Tooltip.create(label));
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mx, int my, float pt) {
        Paper.iconButton(g, Paper.tool(icon), getX(), getY(), 20, isHoveredOrFocused(), false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Paper.pageTurn();
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
