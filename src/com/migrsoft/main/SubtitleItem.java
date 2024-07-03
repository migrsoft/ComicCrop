package com.migrsoft.main;

import java.awt.*;

public class SubtitleItem {

    public Rectangle rect;

    public String originalText = "";
    public String translatedText = "";

    public int originalTextFontSize = -1;
    public int translatedTextFontSize = -1;

    private final Paragraph paraOriginal;
    private final Paragraph paraTranslated;

    public SubtitleItem() {
        rect = new Rectangle();
        paraOriginal = new Paragraph();
        paraTranslated = new Paragraph();
    }

    public void paintOriginalText(Graphics g, Rectangle rect) {
        if (!originalText.isEmpty()) {
            paraOriginal.setText(originalText);
            originalTextFontSize = paint(g, rect, originalTextFontSize, paraOriginal);
        }
    }

    public void paintTranslatedText(Graphics g, Rectangle rect) {
        if (!translatedText.isEmpty()) {
            paraTranslated.setText(translatedText);
            translatedTextFontSize = paint(g, rect, translatedTextFontSize, paraTranslated);
        }
    }

    private int paint(Graphics g, Rectangle rect, int fontSize, Paragraph paragraph) {
        g.setColor(Color.WHITE);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        Font font;
        if (fontSize < 0) {
            paragraph.setWidth(rect.width);
            font = determineFontSize(g, rect.height, paragraph);
        } else {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, fontSize);
        }

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawLines(g, rect, paragraph);

        return font.getSize();
    }

    private Font determineFontSize(Graphics g, int height, Paragraph paragraph) {
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

    private void drawLines(Graphics g, Rectangle rect, Paragraph paragraph) {
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int startY = rect.y + metrics.getAscent();

        for (String line : paragraph.getLines()) {
            g.drawString(line, rect.x, startY);
            startY += lineHeight;
        }
    }
}
