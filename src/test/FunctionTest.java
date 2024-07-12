package test;

import org.junit.jupiter.api.Test;

import java.awt.*;

public class FunctionTest {

    @Test
    public void listAvailableFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // Get all font family names
        String[] fontNames = ge.getAvailableFontFamilyNames();

        // Print all font family names
        System.out.println("Available Fonts:");
        for (String fontName : fontNames) {
            System.out.println(fontName);
        }
    }
}
