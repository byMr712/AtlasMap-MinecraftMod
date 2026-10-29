package org.webtrade.mapminecraft.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.webtrade.mapminecraft.client.data.Storage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Translation shortcuts and the marker icon catalog (assets/mapminecraft/icons.json). */
public final class T {
    /** Marker icons shown in the icon picker, in display order. */
    public static final List<String> MARKER_ICONS = new ArrayList<>();
    /** Old coloured icon ids, mapped to the icon and tint that replace them. */
    public static final Map<String, Alias> ALIASES = new HashMap<>();
    private static final Map<String, String[]> ICON_NAMES = new HashMap<>();

    public record Alias(String icon, int color) {
    }

    static {
        try (InputStream in = T.class.getResourceAsStream("/assets/mapminecraft/icons.json")) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement ie : root.getAsJsonArray("icons")) {
                MARKER_ICONS.add(readIcon(ie.getAsJsonObject()));
            }
            for (JsonElement ie : root.getAsJsonArray("hidden")) {
                readIcon(ie.getAsJsonObject());
            }
            for (var e : root.getAsJsonObject("aliases").entrySet()) {
                JsonObject a = e.getValue().getAsJsonObject();
                int color = 0xFF000000 | Integer.parseInt(a.get("color").getAsString().substring(1), 16);
                ALIASES.put(e.getKey(), new Alias(a.get("icon").getAsString(), color));
            }
        } catch (Exception e) {
            Storage.LOG.error("Failed to load the marker icon catalog", e);
            if (MARKER_ICONS.isEmpty()) {
                MARKER_ICONS.add("pin");
            }
        }
    }

    private static String readIcon(JsonObject i) {
        String id = i.get("id").getAsString();
        ICON_NAMES.put(id, new String[]{i.get("ru").getAsString(), i.get("en").getAsString()});
        return id;
    }

    private T() {
    }

    public static MutableComponent tr(String key, Object... args) {
        return Component.translatable("mapminecraft." + key, args);
    }

    public static String s(String key, Object... args) {
        return tr(key, args).getString();
    }

    public static String dimName(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> s("dim.overworld");
            case "minecraft:the_nether" -> s("dim.nether");
            case "minecraft:the_end" -> s("dim.end");
            default -> {
                int i = dim.indexOf(':');
                yield i >= 0 ? dim.substring(i + 1) : dim;
            }
        };
    }

    private static int langIndex() {
        String lang = Minecraft.getInstance().options.languageCode;
        return lang != null && (lang.startsWith("ru") || lang.startsWith("uk") || lang.startsWith("be")) ? 0 : 1;
    }

    public static String iconName(String icon) {
        String[] n = ICON_NAMES.get(icon);
        return n == null ? icon : n[langIndex()];
    }

    /** Icons whose name (in any language) or id contains every word of the query. Russian endings are ignored. */
    public static List<String> searchIcons(String query) {
        List<String> words = new ArrayList<>();
        for (String w : query.toLowerCase(Locale.ROOT).trim().split("\s+")) {
            if (w.length() >= 4 && w.matches(".*[а-яё](ий|ый|ой|ая|яя|ое|ее|ые|ие)$")) {
                w = w.substring(0, w.length() - 2);
            }
            if (!w.isEmpty()) {
                words.add(w);
            }
        }
        List<String> out = new ArrayList<>();
        for (String id : MARKER_ICONS) {
            String[] n = ICON_NAMES.get(id);
            String hay = id + " " + (n == null ? "" : n[0].toLowerCase(Locale.ROOT) + " " + n[1].toLowerCase(Locale.ROOT));
            List<String> hayWords = List.of(hay.split("[\\s_-]+"));
            if (words.stream().allMatch(w -> hayWords.stream().anyMatch(h -> h.startsWith(w)))) {
                out.add(id);
            }
        }
        return out;
    }
}
