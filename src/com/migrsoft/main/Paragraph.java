package com.migrsoft.main;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;

public class Paragraph {

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
}
