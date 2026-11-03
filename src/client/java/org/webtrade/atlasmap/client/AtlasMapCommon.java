package org.webtrade.atlasmap.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.webtrade.atlasmap.client.data.ModConfig;
import org.webtrade.atlasmap.client.gui.MapScreen;
import org.webtrade.atlasmap.client.gui.MinimapHud;
import org.webtrade.atlasmap.client.gui.Paper;
import org.webtrade.atlasmap.client.gui.SettingsScreen;
import org.webtrade.atlasmap.client.gui.T;
import org.webtrade.atlasmap.client.gui.Theme;
import org.webtrade.atlasmap.client.gui.TitleButton;
import org.webtrade.atlasmap.client.gui.WorldsScreen;
import org.webtrade.atlasmap.client.map.MapManager;
import org.webtrade.atlasmap.mixin.client.ScreenAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Loader-agnostic client logic. Both the Fabric entrypoint and the NeoForge entrypoint
 * call {@link #init()}; all game hooks are implemented as vanilla mixins, so no Fabric API
 * or NeoForge event bus is required here.
 */
public final class AtlasMapCommon {

    private static final MinimapHud MINIMAP = new MinimapHud();
    private static boolean themeLoaded;

    private AtlasMapCommon() {
    }

    public static void init() {
        // Intentionally empty: the mixins do all hooking on both loaders.
    }

    public static void onTick(Minecraft mc) {
        loadTheme();
        MapManager.tick(mc);
        if (ModKeys.OPEN_MAP == null) {
            return;
        }
        ModConfig cfg = ModConfig.get();
        while (ModKeys.OPEN_MAP.consumeClick()) {
            if (mc.gui.screen() == null) {
                MapScreen.openLive(mc);
            }
        }
        while (ModKeys.MARKER_LIST.consumeClick()) {
            if (mc.gui.screen() == null) {
                MapScreen s = MapScreen.openLive(mc);
                if (s != null) {
                    s.openMarkerList();
                }
            }
        }
        while (ModKeys.NEW_MARKER.consumeClick()) {
            if (mc.gui.screen() == null) {
                MapScreen s = MapScreen.openLive(mc);
                if (s != null) {
                    s.newMarkerAtPlayer();
                }
            }
        }
        while (ModKeys.TOGGLE_MINIMAP.consumeClick()) {
            cfg.minimapEnabled = !cfg.minimapEnabled;
            cfg.save();
        }
        while (ModKeys.MINIMAP_ZOOM_IN.consumeClick()) {
            cfg.minimapZoomIndex = Math.min(ModConfig.MINIMAP_ZOOMS.length - 1, cfg.minimapZoomIndex + 1);
            cfg.save();
        }
        while (ModKeys.MINIMAP_ZOOM_OUT.consumeClick()) {
            cfg.minimapZoomIndex = Math.max(0, cfg.minimapZoomIndex - 1);
            cfg.save();
        }
        // in-map shortcuts are handled by the map screen itself
        ModKeys.RULER.consumeClick();
        ModKeys.EDITOR.consumeClick();
        ModKeys.TOGGLE_DRAWINGS.consumeClick();
    }

    /** Applies the saved UI design once the game directory (and so the config) is available. */
    public static void loadTheme() {
        if (!themeLoaded) {
            themeLoaded = true;
            Paper.apply(Theme.byId(ModConfig.get().theme));
        }
    }

    public static void renderMinimap(GuiGraphicsExtractor g, DeltaTracker delta) {
        MINIMAP.render(g, delta);
    }

    public static void addAtlasButton(Screen screen) {
        loadTheme();
        List<AbstractWidget> widgets = new ArrayList<>();
        for (GuiEventListener listener : screen.children()) {
            if (listener instanceof AbstractWidget widget) {
                widgets.add(widget);
            }
        }
        AbstractWidget anchor = null;
        for (AbstractWidget w : widgets) {
            if (w.getWidth() >= 150 && (anchor == null || w.getY() < anchor.getY())) {
                anchor = w;
            }
        }
        int x = anchor != null ? anchor.getX() + anchor.getWidth() + 4 : screen.width / 2 + 104;
        int y = anchor != null ? anchor.getY() : 100;
        int step = 24;
        if (overlaps(widgets, x, y) || overlaps(widgets, x + step, y)) {
            x = anchor != null ? anchor.getX() - 24 : screen.width / 2 - 124;
            step = -24;
        }
        Minecraft mc = Minecraft.getInstance();
        ScreenAccessor accessor = (ScreenAccessor) screen;
        accessor.atlasmap$addRenderableWidget(new TitleButton(x, y, "map", T.tr("atlas.button"),
                () -> mc.gui.setScreen(new WorldsScreen(screen))));
        accessor.atlasmap$addRenderableWidget(new TitleButton(x + step, y, "gear", T.tr("tool.settings"),
                () -> mc.gui.setScreen(new SettingsScreen(screen))));
    }

    private static boolean overlaps(List<AbstractWidget> widgets, int x, int y) {
        for (AbstractWidget w : widgets) {
            if (x < w.getX() + w.getWidth() && x + 20 > w.getX() && y < w.getY() + w.getHeight() && y + 20 > w.getY()) {
                return true;
            }
        }
        return false;
    }
}
