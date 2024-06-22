package com.migrsoft.main;

import java.awt.image.BufferedImage;

class ImageItem {
    /* 图片在列表中的索引值 */
    public int index;
    /* 图片在可视区中的 X */
    public int viewX;

    // 相对于整个拼接图片的 X
    public int x;
    // 相对于整个拼接图片的 Y
    public int y;

    public BufferedImage image;
}
