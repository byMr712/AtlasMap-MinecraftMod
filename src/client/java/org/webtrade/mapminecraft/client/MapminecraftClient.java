package org.webtrade.mapminecraft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import org.webtrade.mapminecraft.client.data.ModConfig;
import org.webtrade.mapminecraft.client.gui.AtlasButton;
import org.webtrade.mapminecraft.client.gui.MapScreen;
import org.webtrade.mapminecraft.client.gui.MinimapHud;
import org.webtrade.mapminecraft.client.map.MapManager;

import java.util.List;

public class MapminecraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModKeys.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> mc.execute(() -> MapManager.onJoin(mc)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> mc.execute(MapManager::onDisconnect));
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> MapManager.onDisconnect());
        ClientChunkEvents.CHUNK_LOAD.register(MapManager::onChunkLoad);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath("mapminecraft", "minimap"), new MinimapHud());

        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof TitleScreen) {
                addAtlasButton(screen, w);
            }
        });
    }

    private void onTick(Minecraft mc) {
        MapManager.tick(mc);
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

    private static void addAtlasButton(net.minecraft.client.gui.screens.Screen screen, int width) {
        List<AbstractWidget> widgets = Screens.getWidgets(screen);
        AbstractWidget anchor = null;
        for (AbstractWidget w : widgets) {
            if (w.getWidth() >= 150 && (anchor == null || w.getY() < anchor.getY())) {
                anchor = w;
            }
        }
        int x = anchor != null ? anchor.getX() + anchor.getWidth() + 4 : width / 2 + 104;
        int y = anchor != null ? anchor.getY() : 100;
        if (overlaps(widgets, x, y)) {
            x = anchor != null ? anchor.getX() - 24 : width / 2 - 124;
        }
        widgets.add(new AtlasButton(x, y, screen));
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
