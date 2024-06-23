package com.migrsoft.main;

import java.awt.*;

public class SelectBox {

    public static final int MINI_SIDE = 20;

    public final Rectangle rect = new Rectangle();
    public final Rectangle range = new Rectangle();

    public void setTopLeft(int x, int y) {
        rect.x = x;
        rect.y = y;
        rect.width = MINI_SIDE;
        rect.height = MINI_SIDE;
    }

    public void setRange(int x, int y, int width, int height) {
        range.setBounds(x, y, width, height);
    }

    public void empty() {
        rect.setBounds(0, 0, 0, 0);
        range.setBounds(0, 0, 0, 0);
    }

    public boolean dragBottomRight(int x, int y) {
        int dx = x - rect.x;
        int dy = y - rect.y;
        boolean repaint = false;
        if (dx >= MINI_SIDE) {
            rect.setSize(dx, rect.height);
            repaint = true;
        }
        if (dy >= MINI_SIDE) {
            rect.setSize(rect.width, dy);
            repaint = true;
        }
        return repaint;
    }

    public boolean isEmpty() {
        return rect.isEmpty();
    }

    public boolean contains(int x, int y) {
        return rect.contains(x, y);
    }
}
