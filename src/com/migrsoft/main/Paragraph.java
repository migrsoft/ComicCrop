package com.migrsoft.main;

import java.awt.*;
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

    public Paragraph(String text, int width, FontMetrics metrics) {
        this.text = text;
        this.width = width;
        this.metrics = metrics;
    }

    public ArrayList<String> getLines() {
        return lines;
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
        int lb;
        while (i < text.length()) {
            charWidth = metrics.charWidth(text.charAt(i));
            if (lineWidth + charWidth > width) {
                getBreakPoint(i);
//                System.out.println("break " + i + " " + text.charAt(i));
                if (breakPoint > start) {
                    lines.add(text.substring(start, breakPoint));
                    i = breakPoint;
                } else {
                    lines.add(text.substring(start, i));
                }

                while (text.charAt(i) == ' ' && i < text.length()) i++;
                if (i == text.length()) return;

                breakPoint = i;
                start = i;
                lastType = getWordType(i);
                lineWidth = 0;
                continue;
            }
            lb = breakPoint;
            getBreakPoint(i);
            if (lb != breakPoint) {
//                System.out.println(breakPoint + " (" + text.charAt(breakPoint) + ") " + lastType);
            }
            lineWidth += charWidth;
//            System.out.println(i + " " + text.charAt(i) + " " + lineWidth + " " + breakPoint);
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
}
