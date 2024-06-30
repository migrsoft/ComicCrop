package com.migrsoft.main;

import java.awt.*;
import java.util.ArrayList;

public class SubtitleItem {

    public Rectangle rect;

    public String originalText;
    public String translatedText;

    public int originalTextFontSize = -1;
    public int translatedTextFontSize = -1;

    private final Paragraph paragraph;

    public SubtitleItem() {
        originalText = new String();
        translatedText = new String();
        paragraph = new Paragraph();
    }

    public void paint(Graphics g, Rectangle rect, boolean original) {
        String text;
        int fontSize;
        if (original) {
            text = originalText;
            fontSize = originalTextFontSize;
        } else {
            text = translatedText;
            fontSize = translatedTextFontSize;
        }
        if (text != null && !text.isEmpty()) {
            fontSize = paint(g, text, rect, fontSize);
            if (original) {
                originalTextFontSize = fontSize;
            } else {
                translatedTextFontSize = fontSize;
            }
        }
    }

    private int paint(Graphics g, String text, Rectangle rect, int fontSize) {
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        Font font;
        if (fontSize < 0) {
            paragraph.setText(text);
            paragraph.setWidth(rect.width);
            font = determineFontSize(g, rect.height);
//            System.out.println("Adaptive font size: " + font.getSize());
        } else {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, fontSize);
        }

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawLines(g, rect);

        return font.getSize();
    }

    private Font determineFontSize(Graphics g, int height) {
        int size = 10;
        Font font;
        FontMetrics metrics;

        do {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, ++size);
            metrics = g.getFontMetrics(font);
            paragraph.setFontMetrics(metrics);
            paragraph.layout();
        } while (metrics.getHeight() * paragraph.getLines().size() <= height);

        font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, --size);
        metrics = g.getFontMetrics(font);
        paragraph.setFontMetrics(metrics);
        paragraph.layout();
        return font;
    }

    private void drawLines(Graphics g, Rectangle rect) {
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int startY = rect.y + metrics.getAscent();

        for (String line : paragraph.getLines()) {
            g.drawString(line, rect.x, startY);
            startY += lineHeight;
        }
    }
}
