package org.webtrade.mapminecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** All key bindings of the mod. They can be changed in Options → Controls → Key Binds → MapMinecraft. */
public final class ModKeys {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mapminecraft", "main"));

    public static KeyMapping OPEN_MAP;
    public static KeyMapping MARKER_LIST;
    public static KeyMapping NEW_MARKER;
    public static KeyMapping TOGGLE_MINIMAP;
    public static KeyMapping MINIMAP_ZOOM_IN;
    public static KeyMapping MINIMAP_ZOOM_OUT;
    public static KeyMapping RULER;
    public static KeyMapping EDITOR;
    public static KeyMapping TOGGLE_DRAWINGS;

    private ModKeys() {
    }

    public static void register() {
        OPEN_MAP = key("open_map", InputConstants.KEY_M);
        MARKER_LIST = key("marker_list", InputConstants.KEY_J);
        NEW_MARKER = key("new_marker", InputConstants.KEY_B);
        TOGGLE_MINIMAP = key("toggle_minimap", InputConstants.KEY_N);
        MINIMAP_ZOOM_IN = key("minimap_zoom_in", InputConstants.KEY_EQUALS);
        MINIMAP_ZOOM_OUT = key("minimap_zoom_out", InputConstants.KEY_MINUS);
        RULER = key("ruler", InputConstants.KEY_R);
        EDITOR = key("editor", InputConstants.KEY_G);
        TOGGLE_DRAWINGS = key("toggle_drawings", InputConstants.KEY_H);
    }

    private static KeyMapping key(String name, int code) {
        return KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.mapminecraft." + name, InputConstants.Type.KEYBOARD, code, CATEGORY));
    }
}
