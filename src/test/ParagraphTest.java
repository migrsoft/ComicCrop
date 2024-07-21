package test;

import com.migrsoft.main.Paragraph;
import com.migrsoft.main.StringResources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class ParagraphTest {

    private Graphics g;
    private Font font;

    @Before
    public void setup() {
        g = Mockito.mock(Graphics2D.class);
        font = new Font(StringResources.FONT_MAIN, Font.PLAIN, 16);
        g.setFont(font);
    }

    @Test
    public void testLayout() {
        Paragraph p = new Paragraph("这里面有一段中文长句子.", 100, null);
        p.layout();
        for (String s : p.getLines()) {
            System.out.println(s);
        }
    }

    @Test
    public void layoutTest() {
        Paragraph p = new Paragraph("这是一段中文句子，其中还有 English……测试分段", 100, null);
        p.layout(g);
    }

    @Test
    public void ellipseTest() {
        Ellipse2D elli = new Ellipse2D.Double(0, 0, 200, 100);
        PathIterator pi = elli.getPathIterator(null);
        double[] coords = new double[6];
        boolean over = false;
        while (!pi.isDone() && !over) {
            int type = pi.currentSegment(coords);
            Point2D.Double P0 = new Point2D.Double(0, 0);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    System.out.println("Move to: (" + coords[0] + ", " + coords[1] + ")");
                    P0 = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.println("Line to: (" + coords[0] + ", " + coords[1] + ")");
                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.println("Cubic curve to: (" + coords[0] + ", " + coords[1] + "), (" +
                            coords[2] + ", " + coords[3] + "), (" +
                            coords[4] + ", " + coords[5] + ")");
                    Point2D.Double P1 = new Point2D.Double(coords[0], coords[1]);
                    Point2D.Double P2 = new Point2D.Double(coords[2], coords[3]);
                    Point2D.Double P3 = new Point2D.Double(coords[4], coords[5]);
                    int numPoints = 10;
                    java.util.List<Point2D.Double> points = new ArrayList<>(numPoints);
                    for (int i = 0; i <= numPoints; i++) {
                        double t = i / (double) numPoints;
                        double x = Math.pow(1 - t, 3) * P0.x +
                                3 * Math.pow(1 - t, 2) * t * P1.x +
                                3 * (1 - t) * Math.pow(t, 2) * P2.x +
                                Math.pow(t, 3) * P3.x;
                        double y = Math.pow(1 - t, 3) * P0.y +
                                3 * Math.pow(1 - t, 2) * t * P1.y +
                                3 * (1 - t) * Math.pow(t, 2) * P2.y +
                                Math.pow(t, 3) * P3.y;
                        points.add(new Point2D.Double(x, y));
                    }
                    for (Point2D.Double point : points) {
                        System.out.println("(" + point.x + ", " + point.y + ")");
                    }
                    over = true;
                    break;
                case PathIterator.SEG_QUADTO:
                    System.out.println("Quadratic curve to: (" + coords[0] + ", " + coords[1] + "), (" +
                            coords[2] + ", " + coords[3] + ")");
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.println("Close path");
                    break;
            }
            pi.next();
        }
    }
}
