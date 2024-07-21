package com.migrsoft.main;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class SelectBox {

    public static final int MINI_SIDE = 20;

    public Rectangle rect;

    private SubtitleItem subtitle;

    private boolean modified = false;

    private boolean attached = false;

    private boolean displaySubtitles = true;

    public SelectBox() {
        rect = new Rectangle();
    }

    public void initialize() {
        modified = false;
        rect.setBounds(0, 0, 0, 0);
        if (subtitle != null) {
            if (attached) {
                subtitle = null;
                attached = false;
            } else {
                updateOriginalText("");
                updateTranslatedText("");
            }
        }
        if (subtitle == null) {
            subtitle = new SubtitleItem();
        }
    }

    public void setEllipse(boolean value) {
        subtitle.setEllipse(value);
    }

    public boolean isEllipse() {
        return subtitle.isEllipse();
    }

    public void setTopLeft(int x, int y) {
        rect.x = x;
        rect.y = y;
        rect.width = MINI_SIDE;
        rect.height = MINI_SIDE;
    }

    public void setLocation(int x, int y) {
        rect.setLocation(x, y);
        modified = true;
    }

    public SubtitleItem getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(SubtitleItem subtitle) {
        this.subtitle = subtitle;
        this.attached = true;
    }

    public SubtitleItem takeSubtitle() {
        subtitle.rect = rect;
        SubtitleItem item = subtitle;
        subtitle = null;
        return item;
    }

    public void updateOriginalText(String text) {
        if (!subtitle.originalText.equals(text)) {
            subtitle.originalText = text;
            subtitle.originalTextFontSize = -1;
            modified = true;
        }
    }

    public void updateTranslatedText(String text) {
        if (!subtitle.translatedText.equals(text)) {
            subtitle.translatedText = text;
            subtitle.translatedTextFontSize = -1;
            modified = true;
        }
    }

    public String getOriginalText() {
        return subtitle.originalText;
    }

    public String getTranslatedText() {
        return subtitle.translatedText;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified() {
        modified = true;
        if (subtitle != null) {
            subtitle.needLayout();
        }
    }

    public void empty() {
        rect.setBounds(0, 0, 0, 0);
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

    public void setDisplaySubtitles(boolean value) {
        displaySubtitles = value;
    }

    public void paint(Graphics g) {
        if (notEmpty()) {
            if (displaySubtitles && subtitle != null) {
                switch (MainParam.getInstance().getSubtitleSwitch()) {
                    case Off -> {}
                    case Original -> subtitle.paintOriginalText(g, rect);
                    case Chinese -> subtitle.paintTranslatedText(g, rect);
                }
            }

            g.setColor(Color.RED);
            g.drawRect(rect.x-1, rect.y-1, rect.width+1, rect.height+1);
            if (subtitle != null && subtitle.isEllipse()) {
                Ellipse2D ellipse = new Ellipse2D.Double(rect.x, rect.y, rect.width, rect.height);
                ((Graphics2D)g).draw(ellipse);
            }
        }
    }
}
