package com.migrsoft.main;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;

public class Paragraph {

    public boolean debug = false;

    private enum Type {
        English,
        Chinese,
        Number,
        Open,
        Close,
        Twin,
        Other,
    }

    private String text = "";

    private int width;

    private FontMetrics metrics;

    private Type lastType;

    private int breakPoint;

    private int next;

    private final ArrayList<String> lines = new ArrayList<>();

    public Paragraph() {
    }

    public Paragraph(String text, int width, FontMetrics metrics) {
        this.text = text;
        this.width = width;
        this.metrics = metrics;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setFontMetrics(FontMetrics metrics) {
        this.metrics = metrics;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    private Type getWordType(int index) {
        if (index < text.length()) {
            String punctBegin = "\"({[“‘《（〈〔「『【〖［";
            String punctEnd = ",.:;!?')}]，。、！？’”°》）〉〕」』】〗］．：；";
            String punctTwin = "—…";

            int code = text.codePointAt(index);
            if (code >= 0x30 && code <= 0x39) {
                return Type.Number;
            } else if ((code >= 0x41 && code <= 0x5a) || (code >= 0x61 && code <= 0x7a)) {
                return Type.English;
            } else if (punctBegin.indexOf(text.charAt(index)) != -1) {
                return Type.Open;
            } else if (punctEnd.indexOf(text.charAt(index)) != -1) {
                return Type.Close;
            } else if (punctTwin.indexOf(text.charAt(index)) != -1) {
                return Type.Twin;
            } else if (code >= 0x3000) {
                return Type.Chinese;
            }
        }
        return Type.Other;
    }

    private void getBreakPoint(int index) {
        Type type = getWordType(index);

        if (type == Type.Close) {
            return;
        }

        switch (lastType) {
        case Chinese:
        case Other:
            breakPoint = index;
            lastType = type;
            break;

        case Number:
            if (type != Type.Number) {
                breakPoint = index;
                lastType = type;
            }
            break;

        case English:
            if (type != Type.English) {
                breakPoint = index;
                lastType = type;
            }
            break;

        case Twin:
            if (type != Type.Twin) {
                breakPoint = index;
                lastType = type;
            }
            break;

        case Open:
            if (type == Type.Open && index - breakPoint > 1) {
                breakPoint = index;
            } else if (type != Type.Open) {
                if (type == Type.English || type == Type.Number) {
                    next = 1;
                    break;
                }
                if (type == Type.Twin) {
                    if (next == 1) {
                        breakPoint = index;
                        lastType = type;
                        next = 0;
                    } else {
                        next = 1;
                    }
                    break;
                }
                next++;
                if (next > 1) {
                    breakPoint = index;
                    lastType = type;
                    next = 0;
                }
            }
            break;
        }
    }

    public void layout() {
        lastType = Type.Other;
        breakPoint = 0;
        next = 0;
        lines.clear();

        int charWidth = 12;
        int lineWidth = 0;
        int start = 0;
        int i = 0;
        while (i < text.length()) {
            charWidth = metrics.charWidth(text.charAt(i));
            if (lineWidth + charWidth > width) {
                getBreakPoint(i);
                if (breakPoint > start) {
                    lines.add(text.substring(start, breakPoint));
                    i = breakPoint;
                } else {
                    lines.add(text.substring(start, i));
                }

                while (i < text.length() && text.charAt(i) == ' ') i++;
                if (i == text.length()) return;

                breakPoint = i;
                start = i;
                lastType = getWordType(i);
                lineWidth = 0;
                continue;
            }
            getBreakPoint(i);
            lineWidth += charWidth;
            i++;
        }
        if (start < i) {
            lines.add(text.substring(start, i));
        }
    }

    public void print() {
        for (String s : lines) {
            System.out.println(s);
        }
    }

    public void layout(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        FontRenderContext frc = g2.getFontRenderContext();
        AttributedString attrText = new AttributedString(text);
        AttributedCharacterIterator attrIt = attrText.getIterator();
        LineBreakMeasurer measurer = new LineBreakMeasurer(attrIt, frc);
        measurer.setPosition(attrIt.getBeginIndex());
        int head;
        lines.clear();
        while ((head = measurer.getPosition()) < attrIt.getEndIndex()) {
            TextLayout tl = measurer.nextLayout(width);
            lines.add(text.substring(head, head + tl.getCharacterCount()));
        }
    }

    private int getAvailableWidth(Rectangle rect, java.util.List<Point2D.Double> points) {
        int radius = rect.width / 2;
//        System.out.println(rect);
        for (Point2D.Double p : points) {
            if (rect.contains(p)) {
                int r = Math.abs((int) (rect.x + (double) rect.width / 2 - p.x));
//                System.out.println("radius:" + radius + " r:" + r);
                radius = Math.min(radius, r);
            }
        }
        return radius * 2;
    }

    public boolean layoutInEllipse(Rectangle rect, java.util.List<Point2D.Double> points, Graphics g) {
        final int fontHeight = metrics.getHeight();
        final int minWidth = metrics.charWidth('汉') * 2;
        int numLines = rect.height / fontHeight;
        int y = rect.y + (rect.height - fontHeight * numLines) / 2;
        Rectangle lineRect = new Rectangle(rect.x, y, rect.width, fontHeight);
        int maxWidth = getAvailableWidth(lineRect, points);
        lines.clear();
        while (maxWidth < minWidth && numLines > 1) {
            numLines--;
            y = rect.y + (rect.height - fontHeight * numLines) / 2;
            lineRect.setLocation(lineRect.x, y);
            maxWidth = getAvailableWidth(lineRect, points);
        }
        maxWidth = Math.max(maxWidth, minWidth);

        lastType = Type.Other;
        breakPoint = 0;
        next = 0;

        int charWidth = 12;
        int lineWidth = 0;
        int start = 0;
        int i = 0;
        while (i < text.length()) {
            charWidth = metrics.charWidth(text.charAt(i));
            if (lineWidth + charWidth > maxWidth) {
                if (debug) {
                    g.drawLine(rect.x + (rect.width - maxWidth) / 2, rect.y, rect.x + maxWidth, rect.y);
                }
                getBreakPoint(i);
                if (breakPoint > start) {
                    lines.add(text.substring(start, breakPoint));
                    i = breakPoint;
                } else {
                    lines.add(text.substring(start, i));
                }

                while (i < text.length() && text.charAt(i) == ' ') i++;
                start = i;
                if (i == text.length()) break;
                breakPoint = i;
                lastType = getWordType(i);
                lineWidth = 0;

                lineRect.setLocation(lineRect.x, lineRect.y + fontHeight);
                maxWidth = getAvailableWidth(lineRect, points);
                maxWidth = Math.max(maxWidth, minWidth);
                continue;
            }
            getBreakPoint(i);
            lineWidth += charWidth;
            i++;
        }
        if (start < i) {
            if (debug) {
                g.drawLine(rect.x + (rect.width - maxWidth) / 2, rect.y, rect.x + maxWidth, rect.y);
            }
            lines.add(text.substring(start, i));
        }
        return lines.size() <= numLines;
    }
}
