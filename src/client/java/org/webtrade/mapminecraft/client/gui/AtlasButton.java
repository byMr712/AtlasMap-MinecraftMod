package org.webtrade.mapminecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

/** Small parchment icon button on the title screen that opens the atlas of stored maps. */
public class AtlasButton extends AbstractWidget {
    private final Screen parent;

    public AtlasButton(int x, int y, Screen parent) {
        super(x, y, 20, 20, T.tr("atlas.button"));
        this.parent = parent;
        setTooltip(Tooltip.create(T.tr("atlas.button")));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        Paper.iconButton(g, Paper.tool("map"), getX(), getY(), 20, isHoveredOrFocused(), false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Paper.pageTurn();
        Minecraft.getInstance().gui.setScreen(new WorldsScreen(parent));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
