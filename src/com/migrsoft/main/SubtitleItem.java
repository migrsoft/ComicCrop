package com.migrsoft.main;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class SubtitleItem {

    public Rectangle rect;
    private boolean isEllipse;

    public String originalText = "";
    public String translatedText = "";

    public int originalTextFontSize = -1;
    public int translatedTextFontSize = -1;

    private final Paragraph paraOriginal;
    private final Paragraph paraTranslated;

    java.util.List<Point2D.Double> allPoints;

    public SubtitleItem() {
        rect = new Rectangle();
        paraOriginal = new Paragraph();
        paraTranslated = new Paragraph();
        allPoints = new ArrayList<>();
        isEllipse = false;
    }

    public void needLayout() {
        originalTextFontSize = -1;
        translatedTextFontSize = -1;
        allPoints.clear();
    }

    public void setEllipse(boolean value) {
        isEllipse = value;
    }

    public boolean isEllipse() {
        return isEllipse;
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

    private void drawAllPoints(Graphics g) {
        g.setColor(Color.RED);
        for (Point2D.Double p : allPoints) {
            g.drawLine((int) p.x, (int) p.y, (int) p.x, (int) p.y);
        }
    }

    private int paint(Graphics g, Rectangle rect, int fontSize, Paragraph paragraph) {
        g.setColor(Color.WHITE);
        if (isEllipse) {
            Ellipse2D ellipse = new Ellipse2D.Double(rect.x, rect.y, rect.width, rect.height);
            ((Graphics2D)g).fill(ellipse);
            if (allPoints.isEmpty()) {
                calcAllPoints(rect);
            }
//            drawAllPoints(g);
        } else {
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        Font font;
        if (fontSize < 0) {
            paragraph.setWidth(rect.width);
            font = determineFontSize(g, rect, paragraph);
        } else {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, fontSize);
            if (paragraph.isEmpty()) {
                paragraph.setFontMetrics(g.getFontMetrics(font));
                if (isEllipse) {
//                    paragraph.debug = true;
                    paragraph.layoutInEllipse(rect, allPoints, g);
                    paragraph.debug = false;
                } else {
                    paragraph.setWidth(rect.width);
                    paragraph.layout();
                }
            }
        }

        g.setFont(font);
        g.setColor(Color.BLACK);
        drawLines(g, rect, paragraph);

        return font.getSize();
    }

    private Font determineFontSize(Graphics g, Rectangle rect, Paragraph paragraph) {
        int size = 10;
        Font font;
        FontMetrics metrics;

        do {
            font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, ++size);
            metrics = g.getFontMetrics(font);
            paragraph.setFontMetrics(metrics);
            if (isEllipse) {
                if (!paragraph.layoutInEllipse(rect, allPoints, g)) break;
            } else {
                paragraph.layout();
            }
        } while (metrics.getHeight() * paragraph.getLines().size() <= rect.height);

        font = FontManager.getInstance().getFont(StringResources.FONT_COMIC, Font.PLAIN, --size);
        metrics = g.getFontMetrics(font);
        paragraph.setFontMetrics(metrics);
        if (isEllipse) {
//            paragraph.debug = true;
            paragraph.layoutInEllipse(rect, allPoints, g);
            paragraph.debug = false;
        } else {
            paragraph.layout();
        }
        return font;
    }

    private void drawLines(Graphics g, Rectangle rect, Paragraph paragraph) {
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int startX;
        int startY = rect.y + metrics.getAscent() + (rect.height - lineHeight * paragraph.getLines().size()) / 2;
        boolean center = isEllipse;
        int w = Math.max(rect.width, rect.height);
        int h = Math.min(rect.width, rect.height);
        if (w / h <= 2) center = true;

        for (String line : paragraph.getLines()) {
            startX = rect.x;
            if (center) {
                startX = rect.x + (rect.width - metrics.stringWidth(line)) / 2;
            }
            g.drawString(line, startX, startY);
            Rectangle frect = new Rectangle(startX, startY - metrics.getAscent(), metrics.stringWidth(line), lineHeight);
//            g.drawRect(frect.x, frect.y, frect.width, frect.height);
            startY += lineHeight;
        }
    }

    private java.util.List<Point2D.Double> calculateBezierPoints(
            Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, int numPoints) {
        java.util.List<Point2D.Double> points = new ArrayList<>(numPoints);
        for (int i = 0; i <= numPoints; i++) {
            double t = i / (double) numPoints;
            double x = Math.pow(1 - t, 3) * p0.x +
                    3 * Math.pow(1 - t, 2) * t * p1.x +
                    3 * (1 - t) * Math.pow(t, 2) * p2.x +
                    Math.pow(t, 3) * p3.x;
            double y = Math.pow(1 - t, 3) * p0.y +
                    3 * Math.pow(1 - t, 2) * t * p1.y +
                    3 * (1 - t) * Math.pow(t, 2) * p2.y +
                    Math.pow(t, 3) * p3.y;
            points.add(new Point2D.Double(x, y));
        }
        return points;
    }

    private void calcAllPoints(Rectangle rect) {
        allPoints.clear();
        Ellipse2D ellipse = new Ellipse2D.Double(rect.x, rect.y, rect.width, rect.height);
        PathIterator pi = ellipse.getPathIterator(null);
        double[] cords = new double[6];
        Point2D.Double p0 = new Point2D.Double(0, 0);
        Point2D.Double p1 = new Point2D.Double(0, 0);
        Point2D.Double p2 = new Point2D.Double(0, 0);
        Point2D.Double p3 = new Point2D.Double(0, 0);
        while (!pi.isDone()) {
            int type = pi.currentSegment(cords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    p0.setLocation(cords[0], cords[1]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    p1.setLocation(cords[0], cords[1]);
                    p2.setLocation(cords[2], cords[3]);
                    p3.setLocation(cords[4], cords[5]);
                    java.util.List<Point2D.Double> points = calculateBezierPoints(
                            p0, p1, p2, p3, rect.height / 2 / 5);
                    allPoints.addAll(points);
                    p0.setLocation(cords[4], cords[5]);
                    break;
            }
            pi.next();
        }
    }

    public void fromJson(JsonSubtitle subtitle) {
        rect.setBounds(subtitle.getRect().getX(), subtitle.getRect().getY(),
                subtitle.getRect().getWidth(), subtitle.getRect().getHeight());
        originalText = subtitle.getLang1();
        translatedText = subtitle.getLang2();
        originalTextFontSize = subtitle.getSize1();
        translatedTextFontSize = subtitle.getSize2();
        if (subtitle.getShape() == 1) isEllipse = true;
    }

    public JsonSubtitle toJson() {
        JsonRect rect = new JsonRect();
        rect.setX((int) this.rect.getX());
        rect.setY((int) this.rect.getY());
        rect.setWidth((int) this.rect.getWidth());
        rect.setHeight((int) this.rect.getHeight());
        JsonSubtitle subtitle = new JsonSubtitle();
        subtitle.setRect(rect);
        subtitle.setLang1(originalText);
        subtitle.setSize1(originalTextFontSize);
        subtitle.setLang2(translatedText);
        subtitle.setSize2(translatedTextFontSize);
        if (isEllipse) subtitle.setShape(1);
        return subtitle;
    }
}
