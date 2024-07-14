package com.migrsoft.main;

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
}
