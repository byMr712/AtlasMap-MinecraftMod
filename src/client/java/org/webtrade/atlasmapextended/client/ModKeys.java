package org.webtrade.atlasmapextended.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** All key bindings of the mod. They can be changed in Options → Controls → Key Binds → AtlasMapExtended. */
public final class ModKeys {
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("atlasmapextended", "main"));

    private static boolean registered;

    public static KeyMapping OPEN_MAP;
    public static KeyMapping MARKER_LIST;
    public static KeyMapping NEW_MARKER;
    public static KeyMapping TOGGLE_MINIMAP;
    public static KeyMapping MINIMAP_ZOOM_IN;
    public static KeyMapping MINIMAP_ZOOM_OUT;
    public static KeyMapping MINIMAP_ZOOM_TOGGLE;
    public static KeyMapping RULER;
    public static KeyMapping EDITOR;
    public static KeyMapping TOGGLE_DRAWINGS;

    private ModKeys() {
    }

    /**
     * Creates the key mappings and returns them so the {@code Options} mixin can append
     * them to the vanilla key list. Returns an empty array on subsequent calls.
     */
    public static KeyMapping[] createAll() {
        if (registered) {
            return new KeyMapping[0];
        }
        registered = true;
        OPEN_MAP = key("open_map", InputConstants.KEY_M);
        MARKER_LIST = key("marker_list", InputConstants.KEY_J);
        NEW_MARKER = key("new_marker", InputConstants.KEY_B);
        TOGGLE_MINIMAP = key("toggle_minimap", InputConstants.KEY_N);
        MINIMAP_ZOOM_IN = key("minimap_zoom_in", InputConstants.KEY_EQUALS);
        MINIMAP_ZOOM_OUT = key("minimap_zoom_out", InputConstants.KEY_MINUS);
        MINIMAP_ZOOM_TOGGLE = key("minimap_zoom_toggle", InputConstants.KEY_Z);
        RULER = key("ruler", InputConstants.KEY_R);
        EDITOR = key("editor", InputConstants.KEY_G);
        TOGGLE_DRAWINGS = key("toggle_drawings", InputConstants.KEY_H);
        return new KeyMapping[]{OPEN_MAP, MARKER_LIST, NEW_MARKER, TOGGLE_MINIMAP,
                MINIMAP_ZOOM_IN, MINIMAP_ZOOM_OUT, MINIMAP_ZOOM_TOGGLE, RULER, EDITOR, TOGGLE_DRAWINGS};
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.atlasmapextended." + name, InputConstants.Type.KEYBOARD, code, CATEGORY);
    }
}
