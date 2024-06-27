package com.migrsoft.main;

import java.awt.*;
import java.util.ArrayList;

public class SubtitleItem {

    public Rectangle rect;

    public String originalText;
    public String translatedText;

    public int originalTextFontSize = -1;
    public int translatedTextFontSize = -1;

    public void paint(Graphics g, Rectangle rect) {
        if (!originalText.isEmpty()) {
            originalTextFontSize = paint(g, originalText, rect, originalTextFontSize);
        }
    }

    public static int paint(Graphics g, String text, Rectangle rect, int fontSize) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        Font font;
        if (fontSize < 0) {
            font = determineFontSize(g, text, rect.width, rect.height);
        } else {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, fontSize);
        }

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawStringInRectangle(g, text, rect);

        return font.getSize();
    }

    private static Font determineFontSize(Graphics g, String text, int width, int height) {
        int size = 10;
        Font font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, size);
        FontMetrics metrics = g.getFontMetrics(font);
        ArrayList<String> lines = getLines(g, text, width, metrics);

        while (metrics.getHeight() * lines.size() <= height) {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, ++size);
            metrics = g.getFontMetrics(font);
            lines = getLines(g, text, width, metrics);
        }

        return FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, --size);
    }

    private static ArrayList<String> getLines(Graphics g, String text, int width, FontMetrics metrics) {
        ArrayList<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(line + word) <= width) {
                line.append(word).append(" ");
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word).append(" ");
            }
        }
        lines.add(line.toString());
        return lines;
    }

    private static void drawStringInRectangle(Graphics g, String text, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics();
        ArrayList<String> lines = getLines(g, text, rect.width, metrics);
        int lineHeight = metrics.getHeight();
        int startY = rect.y + metrics.getAscent();

        for (String line : lines) {
            g.drawString(line, rect.x, startY);
            startY += lineHeight;
            if (startY > rect.y + rect.height) {
                break;
            }
        }
    }
}
