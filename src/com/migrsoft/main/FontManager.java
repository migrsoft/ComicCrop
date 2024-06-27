package com.migrsoft.main;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FontManager {

    private static FontManager instance = null;

    private final Map<FontKey, Font> fonts;

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    public FontManager() {
        fonts = new HashMap<>();
    }

    private void addFont(String name, int style, int size) {
        FontKey key = new FontKey(name, style, size);
        if (!fonts.containsKey(key)) {
            Font font = new Font(name, style, size);
            fonts.put(key, font);
        }
    }

    public Font getFont(String name, int style, int size) {
        FontKey key = new FontKey(name, style, size);
        Font font = fonts.get(key);
        if (font == null) {
            addFont(name, style, size);
            font = fonts.get(key);
        }
        return font;
    }

    private static class FontKey {
        private String name;
        private int style;
        private int size;

        public FontKey(String name, int style, int size) {
            this.name = name;
            this.style = style;
            this.size = size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontKey fontKey = (FontKey) o;
            return style == fontKey.style && size == fontKey.size && name.equals(fontKey.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + style;
            result = 31 * result + size;
            return result;
        }
    }
}
