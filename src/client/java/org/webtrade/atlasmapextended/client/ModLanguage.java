package org.webtrade.atlasmapextended.client;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;

/** Wraps the vanilla language and adds the mod's translations for vanilla screens such as key bindings. */
public final class ModLanguage extends Language {
    private final Language delegate;
    private Map<String, String> extra;

    public ModLanguage(Language delegate, Map<String, String> extra) {
        this.delegate = delegate;
        this.extra = extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    @Override
    public String getOrDefault(String key, String fallback) {
        String value = extra.get(key);
        return value != null ? value : delegate.getOrDefault(key, fallback);
    }

    @Override
    public boolean has(String key) {
        return extra.containsKey(key) || delegate.has(key);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return delegate.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText text) {
        return delegate.getVisualOrder(text);
    }
}
