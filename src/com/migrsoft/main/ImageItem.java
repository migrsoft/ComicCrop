package com.migrsoft.main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageItem {

    // 图片在列表中的索引值
    public int index;
    public String name;

    // 相对于整个拼接图片的 X
    public int x;
    // 相对于整个拼接图片的 Y
    public int y;

    public BufferedImage image;

    public Rectangle getVisibleRectInViewPort(Rectangle viewPort) {
        Rectangle imageRect = new Rectangle();
        imageRect.setBounds((viewPort.width - image.getWidth()) / 2, y, image.getWidth(), image.getHeight());
        Rectangle isr = new Rectangle();
        Rectangle.intersect(imageRect, viewPort, isr);
        if (!isr.isEmpty()) {
            isr.setLocation(isr.x, isr.y - viewPort.y);
            isr.setSize(isr.width - 1, isr.height - 1);
        }
        return isr;
    }
}
