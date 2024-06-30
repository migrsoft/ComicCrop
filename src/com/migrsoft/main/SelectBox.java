package com.migrsoft.main;

import java.awt.*;
import java.util.Objects;

public class SelectBox {

    public static final int MINI_SIDE = 20;

    public final Rectangle rect;
    public final Rectangle range;

    private SubtitleItem subtitle;

    public SelectBox() {
        rect = new Rectangle();
        range = new Rectangle();
        subtitle = new SubtitleItem();
    }

    public void reset() {
        updateOriginalText("");
        updateTranslatedText("");
    }

    public void setTopLeft(int x, int y) {
        rect.x = x;
        rect.y = y;
        rect.width = MINI_SIDE;
        rect.height = MINI_SIDE;
    }

    public void setRange(int x, int y, int width, int height) {
        range.setBounds(x, y, width, height);
    }

    public void updateOriginalText(String text) {
        if (!subtitle.originalText.equals(text)) {
            subtitle.originalText = text;
            subtitle.originalTextFontSize = -1;
        }
    }

    public void updateTranslatedText(String text) {
        if (!subtitle.translatedText.equals(text)) {
            subtitle.translatedText = text;
            subtitle.translatedTextFontSize = -1;
        }
    }

    public String getOriginalText() {
        return subtitle.originalText;
    }

    public String getTranslatedText() {
        return subtitle.translatedText;
    }

    public void empty() {
        rect.setBounds(0, 0, 0, 0);
        range.setBounds(0, 0, 0, 0);
    }

    public boolean dragBottomRight(int x, int y) {
        int dx = x - rect.x;
        int dy = y - rect.y;
        boolean repaint = false;
        if (dx >= MINI_SIDE) {
            rect.setSize(dx, rect.height);
            repaint = true;
        }
        if (dy >= MINI_SIDE) {
            rect.setSize(rect.width, dy);
            repaint = true;
        }
        return repaint;
    }

    public boolean notEmpty() {
        return !rect.isEmpty();
    }

    public boolean contains(int x, int y) {
        return rect.contains(x, y);
    }

    public void paint(Graphics g) {
        if (notEmpty()) {
            g.setColor(Color.RED);
            g.drawRect(rect.x-1, rect.y-1, rect.width+1, rect.height+1);
//            g.setColor(Color.BLUE);
//            g.drawRect(range.x, range.y, range.width, range.height);

            subtitle.paint(g, rect, true);
        }
    }
}
