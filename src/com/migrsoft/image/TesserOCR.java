package com.migrsoft.image;

import com.migrsoft.main.Config;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TesserOCR {

    private static ITesseract instance = null;

    public static String ocr(BufferedImage image, Rectangle rect) {
        if (instance == null) {
            instance = new Tesseract();
        }
        instance.setDatapath(Config.getInstance().getTesserOcrDataPath());
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
