package com.migrsoft.main;

public class JsonSubtitle {

    private JsonRect rect;
    private String lang1;
    private String lang2;
    private int size1;
    private int size2;
    private int shape; // 0:rectangle 1:ellipse

    public String getLang1() {
        return lang1;
    }

    public void setLang1(String text) {
        lang1 = text;
    }

    public String getLang2() {
        return lang2;
    }

    public void setLang2(String text) {
        lang2 = text;
    }

    public JsonRect getRect() {
        return rect;
    }

    public void setRect(JsonRect rect) {
        this.rect = rect;
    }

    public int getSize1() {
        return size1;
    }

    public void setSize1(int size1) {
        this.size1 = size1;
    }

    public int getSize2() {
        return size2;
    }

    public void setSize2(int size2) {
        this.size2 = size2;
    }

    @Override
    public String toString() {
        return "JsonSubtitle{" +
                rect +
                "(" + shape + ") " +
                lang1 + "(" + size1 + ")" +
                lang2 + "(" + size2 + ")" +
                "}";
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }
}
