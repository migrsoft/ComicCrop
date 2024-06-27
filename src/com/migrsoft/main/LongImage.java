package com.migrsoft.main;

import org.w3c.dom.css.Rect;

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

    public int getXInViewPort(ImageItem ii, Rectangle viewPort) {
        return (viewPort.width - width) / 2 + ii.x;
    }

    // 将矩形由视图位标转换成图片坐标
    public Rectangle rectToImage(Rectangle rect, Rectangle viewPort) {
        ImageItem ii = getSelectedImage(rect.y + viewPort.y);
        return rectToImage(ii, rect, viewPort);
    }

    public Rectangle rectToImage(ImageItem ii, Rectangle rect, Rectangle viewPort) {
        Rectangle r = new Rectangle(0, 0, 0, 0);
        if (ii != null) {
            r.x = rect.x - getXInViewPort(ii, viewPort);
            r.y = rect.y + viewPort.y - ii.y;
            r.setSize(rect.width, rect.height);
        }
        return r;
    }

    // 将矩形由图片坐标转换成视图坐标
    public Rectangle rectToView(Rectangle rect, Rectangle viewPort) {
        ImageItem ii = getSelectedImage(rect.y);
        return rectToView(ii, rect, viewPort);
    }

    public Rectangle rectToView(ImageItem ii, Rectangle rect, Rectangle viewPort) {
        Rectangle r = new Rectangle(0, 0, 0, 0);
        if (ii != null) {
            r.x = rect.x + getXInViewPort(ii, viewPort);
            r.y = rect.y + ii.y - viewPort.y;
            r.setSize(rect.width, rect.height);
        }
        return r;
    }

    // 当图片变化时，视图 Y 的增减量
    private int adjustedY = 0;

    // 在头部或尾部追加图片
    public int addImage(BufferedImage image, int index, boolean last) {
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
        }
        height = y;
    }

    public void paint(Graphics2D g, Rectangle viewPort) {
        if (!imageList.isEmpty()) {
            boolean first = true;
            String hintSize = "? x ?";
            int dx1, dx2, dy1 = 0, dy2;
            int sx1, sx2, sy1, sy2;
            Rectangle rect = new Rectangle();
            Rectangle isr = new Rectangle();
            for (ImageItem ii : imageList) {
                if (ii.y >= viewPort.y + viewPort.height) {
                    break;
                }
                rect.setBounds(
                        getXInViewPort(ii, viewPort), ii.y,
                        ii.image.getWidth(), ii.image.getHeight());
                if (rect.intersects(viewPort)) {
                    Rectangle.intersect(rect, viewPort, isr);
                    if (first) {
                        first = false;
                        hintSize = rect.width + " x " + rect.height;
                        currentIndex = ii.index;
                    }

                    // 绘制图片
                    dx1 = rect.x;
                    dx2 = dx1 + rect.width;
                    dy2 = dy1 + isr.height;
                    sx1 = 0;
                    sx2 = sx1 + rect.width;
                    sy1 = isr.y - rect.y;
                    sy2 = sy1 + isr.height;
                    g.drawImage(ii.image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
//                    g.setPaint(Color.RED);
//                    g.drawRect(dx1, dy1, (dx2 - dx1), (dy2 - dy1));
                    dy1 += isr.height;

                    // 绘制字幕
                    for (SubtitleItem si : ii.subtitles) {
                        rect.setBounds(
                                getXInViewPort(ii, viewPort) + si.rect.x, ii.y + si.rect.y,
                                si.rect.width, si.rect.height);
                        if (rect.intersects(viewPort)) {
                            Rectangle r = rectToView(ii, si.rect, viewPort);
                            si.paint(g, r);
                        }
                    }
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

    public ImageItem getSelectedImage(int y) {
        for (ImageItem ii : imageList) {
            if (y >= ii.y && y < ii.y + ii.image.getHeight()) {
                return ii;
            }
        }
        return null;
    }

    public SubtitleItem getSubtitleByPos(int x, int y, Rectangle viewPort) {
        ImageItem ii = getSelectedImage(y + viewPort.y);
        if (ii != null) {
            x = x - getXInViewPort(ii, viewPort);
            y = y + viewPort.y - ii.y;
            for (SubtitleItem si : ii.subtitles) {
                if (si.rect.contains(x, y)) {
                    return si;
                }
            }
        }
        return null;
    }

    public void addSubtitle(ImageItem ii, SubtitleItem nsi, Rectangle viewPort) {
        nsi.rect = rectToImage(ii, nsi.rect, viewPort);
        int index = 0;
        for (SubtitleItem si : ii.subtitles) {
            if (si.rect == nsi.rect) {
                ii.subtitles.set(index, nsi);
                return;
            }
            index++;
        }
        ii.subtitles.add(nsi);
    }

    public void removeSubtitle(ImageItem ii, SubtitleItem nsi, Rectangle viewPort) {
        nsi.rect = rectToImage(ii, nsi.rect, viewPort);
        int index = 0;
        for (SubtitleItem si : ii.subtitles) {
            if (si.rect == nsi.rect) {
                ii.subtitles.remove(index);
                return;
            }
            index++;
        }
    }
}
