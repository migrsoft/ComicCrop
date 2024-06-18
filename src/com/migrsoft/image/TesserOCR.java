package com.migrsoft.image;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TesserOCR {
    static public String ocr(BufferedImage image, Rectangle rect) {
        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/local/Cellar/tesseract/5.4.1/share/tessdata/");
        ArrayList<Rectangle> rects = new ArrayList<>();
        rects.add(rect);
        try {
            return instance.doOCR(image, "", rects);
        } catch (TesseractException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
