package com.migrsoft.main;

import java.awt.*;

public class EdgeDetect {

    public enum Edge {
        None,
        West,
        East,
        North,
        South,
        NorthWest,
        NorthEast,
        SouthWest,
        SouthEast,
    }

    private final Rectangle rect;

    private int sensing = 3;

    public EdgeDetect(Rectangle rect) {
        this.rect = rect;
    }

    public void setSensingValue(int value) {
        sensing = value;
    }

    public Edge isOnTheEdge(Point pos) {
        Edge result = Edge.None;
        if (rect.contains(pos)) {
            int wX = Math.abs(pos.x - rect.x);
            int eX = Math.abs(pos.x - (rect.x + rect.width));
            int nY = Math.abs(pos.y - rect.y);
            int sY = Math.abs(pos.y - (rect.y + rect.height));

            if (wX < sensing && nY < sensing) {
                result = Edge.NorthWest;
            } else if (wX < sensing && sY < sensing) {
                result = Edge.SouthWest;
            } else if (eX < sensing && nY < sensing) {
                result = Edge.NorthEast;
            } else if (eX < sensing && sY < sensing) {
                result = Edge.SouthEast;
            } else if (wX < sensing) {
                result = Edge.West;
            } else if (eX < sensing) {
                result = Edge.East;
            } else if (nY < sensing) {
                result = Edge.North;
            } else if (sY < sensing) {
                result = Edge.South;
            }
        }
        return result;
    }

    public static void adjust(Rectangle rect, Edge edge, int dx, int dy) {
        switch (edge) {
            case West -> {
                rect.x += dx;
                rect.width += -dx;
            }
            case East -> {
                rect.width += dx;
            }
            case North -> {
                rect.y += dy;
                rect.height += -dy;
            }
            case South -> {
                rect.height += dy;
            }
            case NorthWest -> {
                rect.x += dx;
                rect.width += -dx;
                rect.y += dy;
                rect.height += -dy;
            }
            case NorthEast -> {
                rect.width += dx;
                rect.y += dy;
                rect.height += -dy;
            }
            case SouthWest -> {
                rect.x += dx;
                rect.width += -dx;
                rect.height += dy;
            }
            case SouthEast -> {
                rect.width += dx;
                rect.height += dy;
            }
        }
    }
}
