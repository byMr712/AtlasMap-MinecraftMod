package org.webtrade.mapminecraft.client.gui;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** A UI design: panel textures and the ink/accent colours drawn on them. */
public record Theme(String id, ResourceLocation panel, ResourceLocation panelDark, boolean darkBg, boolean ornate,
                    int ink, int inkSoft, int accent, int accentLight, int cream, int disabled, int hint, int vignette) {

    public static final Theme PAPYRUS = new Theme("papyrus", tex("textures/gui/parchment.png"), tex("textures/gui/parchment_dark.png"),
            false, true, 0xFF3A2616, 0xFF6B4A2B, 0xFFB88C2C, 0xFFEACA6C, 0xFFF6ECD0, 0xFF8A7A62, 0xFF9A8A70, 0x2A1A0C);

    public static final List<Theme> ALL = List.of(
            PAPYRUS,
            of("stone", false, true, 0xFF1E1E1E, 0xFF464646, 0xFFB88C2C, 0xFFEACA6C, 0xFFD8D8D6, 0xFF6E6E6E, 0xFF7A7A78, 0x202020),
            of("deepslate", true, true, 0xFFE8E8EC, 0xFFB4B4BE, 0xFF4FD6C8, 0xFFA8F0E8, 0xFF2E2E34, 0xFF80808A, 0xFF8A8A94, 0x101014),
            of("modern_light", false, false, 0xFF1F2328, 0xFF59636E, 0xFF2F6FEB, 0xFF8FB4FF, 0xFFFFFFFF, 0xFF9AA1A9, 0xFF9AA1A9, 0x30343A),
            of("modern_dark", true, false, 0xFFE6E8EB, 0xFFA2AAB4, 0xFF4C9AFF, 0xFF9CC8FF, 0xFF262A31, 0xFF6E7681, 0xFF7D8590, 0x0A0C10),
            of("oak", false, true, 0xFF24160A, 0xFF4E3218, 0xFFE0B040, 0xFFFFE08A, 0xFFE8D2A8, 0xFF7A5E3C, 0xFF6E5638, 0x2A1A0C),
            of("nether", true, true, 0xFFF6E2D2, 0xFFD8AE98, 0xFFFF9A3C, 0xFFFFD080, 0xFF3A1418, 0xFFA07870, 0xFFB08878, 0x1A0406),
            of("end", false, true, 0xFF2A1A3A, 0xFF57406E, 0xFF9A4FD0, 0xFFD6A8FF, 0xFFF2F0C8, 0xFF8E8A6A, 0xFF8A8668, 0x2A2A10));

    private static Theme of(String id, boolean darkBg, boolean ornate, int ink, int inkSoft, int accent, int accentLight,
                            int cream, int disabled, int hint, int vignette) {
        return new Theme(id, tex("textures/gui/themes/" + id + ".png"), tex("textures/gui/themes/" + id + "_dark.png"),
                darkBg, ornate, ink, inkSoft, accent, accentLight, cream, disabled, hint, vignette);
    }

    // not Paper.id(): Paper's static init reads PAPYRUS, so this class must not touch Paper
    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath("mapminecraft", path);
    }

    public static Theme byId(String id) {
        for (Theme t : ALL) {
            if (t.id.equals(id)) {
                return t;
            }
        }
        return PAPYRUS;
    }
}
