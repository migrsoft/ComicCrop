package com.migrsoft.main;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

// 一张虚拟的图片，由多个图片拼接而成，形成一张纵向长图。
public class LongImage {

    public int width = 0;
    public int height = 0;

    private final Font mainFont;

    private final LinkedList<ImageItem> imageList = new LinkedList<>();

    private int currentIndex = -1;

    LongImage() {
        mainFont = new Font(StringResources.FONT_MAIN, Font.BOLD, 20);
    }

    public void reset() {
        width = 0;
        height = 0;
        currentIndex = -1;
        imageList.clear();
        System.gc();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getIndexAtFirst() {
        assert imageList.peekFirst() != null;
        return imageList.peekFirst().index;
    }

    public int getIndexAtLast() {
        assert imageList.peekLast() != null;
        return imageList.peekLast().index;
    }

    // 当图片变化时，视图 Y 的增减量
    private int adjustedY = 0;

    // 在头部或尾部追加图片
    public int addImage(BufferedImage image, int index, boolean last) {
//        System.out.println("add " + index + " " + last);
        ImageItem ii = new ImageItem();
        ii.index = index;
        ii.image = image;
        if (last) {
            imageList.addLast(ii);
        } else {
            imageList.addFirst(ii);
            adjustedY = ii.image.getHeight();
        }
        removeImage(!last);
        layout();
        return adjustedY;
    }

    // 控制虚拟图片中实际的图片数量
    private void removeImage(boolean last) {
        if (imageList.size() > 5) {
            ImageItem ii;
            if (last) {
                ii = imageList.removeLast();
            } else {
                ii = imageList.removeFirst();
                adjustedY = -ii.image.getHeight();
            }
            ii.image = null;
            System.gc();
        }
    }

    // 计算虚拟图片布局
    private void layout() {
        for (ImageItem ii : imageList) {
            width = Math.max(width, ii.image.getWidth());
        }
        int y = 0;
        for (ImageItem ii : imageList) {
            ii.x = (width - ii.image.getWidth()) / 2;
            ii.y = y;
            y += ii.image.getHeight();
//            System.out.println(ii.index + " " + ii.x + " " + ii.y);
        }
        height = y;
//        System.out.println("layout " + width + " x " + height);
    }

    public void paint(Graphics2D g, Rectangle viewPort) {
        if (!imageList.isEmpty()) {
            boolean first = true;
            String hintSize = "? x ?";
            int dx1, dx2, dy1 = 0, dy2;
            int sx1, sx2, sy1, sy2;
            Rectangle imageRect = new Rectangle();
            Rectangle isr = new Rectangle();
            for (ImageItem ii : imageList) {
                if (ii.y >= viewPort.y + viewPort.height) {
                    break;
                }
                imageRect.setBounds(
                        (viewPort.width - width) / 2 + ii.x, ii.y,
                        ii.image.getWidth(), ii.image.getHeight());
                if (imageRect.intersects(viewPort)) {
                    Rectangle.intersect(imageRect, viewPort, isr);
                    if (first) {
                        first = false;
                        hintSize = imageRect.width + " x " + imageRect.height;
                        currentIndex = ii.index;
                    }
                    dx1 = imageRect.x;
                    dx2 = dx1 + imageRect.width;
                    dy2 = dy1 + isr.height;
                    sx1 = 0;
                    sx2 = sx1 + imageRect.width;
                    sy1 = isr.y - imageRect.y;
                    sy2 = sy1 + isr.height;
                    g.drawImage(ii.image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
//                    g.setPaint(Color.RED);
//                    g.drawRect(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
                    dy1 += isr.height;
                }
            }
            // 显示单图尺寸
            FontRenderContext frc = g.getFontRenderContext();
            Rectangle2D bounds = mainFont.getStringBounds(hintSize, frc);
            g.setFont(mainFont);
            g.setPaint(Color.LIGHT_GRAY);
            g.drawString(hintSize, 2, -(int) bounds.getY() + 2);
        }
    }
}
