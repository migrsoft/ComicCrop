package com.migrsoft.main;

import com.migrsoft.image.PicWorkerParam;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBarInEditMode {

    public interface Callback {
        void onFileOpen();
        void onFileOpenComic();
        boolean isLockWindowSize();
        void onLockWindowSize();
        void onLoadTask();
        void onSaveTask();

        void onToGrayscale();
        void onForceGrayCalc(boolean checked);
        void onAutoGrayCalc(boolean checked);
        PicWorkerParam.ImageFormat getImageFormat();
        void onImageFormat(PicWorkerParam.ImageFormat format);
        boolean isCutWhiteEdge();
        void onCutWhiteEdge(boolean value);
        int getImageSize();
        void onImageSize(int width, int height);
    }
    private final JMenuBar bar;

    public MenuBarInEditMode(Callback cb) {
        assert cb != null;
        bar = new JMenuBar();

        ActionListener handler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                switch (cmd) {
                    case StringResources.MENU_FILE_OPEN -> cb.onFileOpen();
                    case StringResources.MENU_FILE_OPEN_COMIC -> cb.onFileOpenComic();
                    case StringResources.MENU_FILE_LOCK_WIN_SIZE -> cb.onLockWindowSize();
                    case StringResources.MENU_FILE_LOAD_TASK -> cb.onLoadTask();
                    case StringResources.MENU_FILE_SAVE_TASK -> cb.onSaveTask();
                    case StringResources.MENU_TO_GRAYSCALE -> cb.onToGrayscale();
                    case StringResources.MENU_FORCE_GRAY_CALC -> cb.onForceGrayCalc(
                            ((JCheckBoxMenuItem)e.getSource()).isSelected());
                    case StringResources.MENU_AUTO_GRAY_CALC -> cb.onAutoGrayCalc(
                            ((JCheckBoxMenuItem)e.getSource()).isSelected());
                    case StringResources.MENU_FORMAT_PNG -> cb.onImageFormat(PicWorkerParam.ImageFormat.Png);
                    case StringResources.MENU_FORMAT_JPG -> cb.onImageFormat(PicWorkerParam.ImageFormat.Jpeg);
                    case StringResources.MENU_FORMAT_WEBP -> cb.onImageFormat(PicWorkerParam.ImageFormat.Webp);
                    case StringResources.MENU_CUT_WHITE_EDGE -> cb.onCutWhiteEdge(true);
                    case StringResources.MENU_CUT_BLACK_EDGE -> cb.onCutWhiteEdge(false);
                    case StringResources.MENU_PIC_SIZE_480 -> cb.onImageSize(480, 800);
                    case StringResources.MENU_PIC_SIZE_600 -> cb.onImageSize(600, 1000);
                    case StringResources.MENU_PIC_SIZE_800 -> cb.onImageSize(800, 1280);
                }
            }
        };

        JMenu file = new JMenu(StringResources.MENU_FILE);
        bar.add(file);

        JMenuItem fileOpen = new JMenuItem(StringResources.MENU_FILE_OPEN);
        fileOpen.addActionListener(handler);
        JMenuItem fileOpenComic = new JMenuItem(StringResources.MENU_FILE_OPEN_COMIC);
        fileOpenComic.addActionListener(handler);

        JCheckBoxMenuItem fileLockSize = new JCheckBoxMenuItem(StringResources.MENU_FILE_LOCK_WIN_SIZE);
        fileLockSize.addActionListener(handler);
        fileLockSize.setSelected(cb.isLockWindowSize());

        JMenuItem fileLoadTask = new JMenuItem(StringResources.MENU_FILE_LOAD_TASK);
        fileLoadTask.addActionListener(handler);
        JMenuItem fileSaveTask = new JMenuItem(StringResources.MENU_FILE_SAVE_TASK);
        fileSaveTask.addActionListener(handler);

        file.add(fileOpen);
        file.add(fileOpenComic);
        file.addSeparator();
        file.add(fileLockSize);
        file.addSeparator();
        file.add(fileLoadTask);
        file.add(fileSaveTask);

        JMenu image = new JMenu(StringResources.MENU_IMAGE);
        bar.add(image);

        JMenuItem imageToGray = new JMenuItem(StringResources.MENU_TO_GRAYSCALE);
        imageToGray.addActionListener(handler);

        JCheckBoxMenuItem imageForceGray = new JCheckBoxMenuItem(StringResources.MENU_FORCE_GRAY_CALC);
        imageForceGray.addActionListener(handler);
        JCheckBoxMenuItem imageAutoGray = new JCheckBoxMenuItem(StringResources.MENU_AUTO_GRAY_CALC);
        imageAutoGray.addActionListener(handler);

        JRadioButtonMenuItem imageFormatPNG = new JRadioButtonMenuItem(StringResources.MENU_FORMAT_PNG);
        imageFormatPNG.addActionListener(handler);
        JRadioButtonMenuItem imageFormatJPG = new JRadioButtonMenuItem(StringResources.MENU_FORMAT_JPG);
        imageFormatJPG.addActionListener(handler);
        JRadioButtonMenuItem imageFormatWEBP = new JRadioButtonMenuItem(StringResources.MENU_FORMAT_WEBP);
        imageFormatWEBP.addActionListener(handler);

        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(imageFormatPNG);
        formatGroup.add(imageFormatJPG);
        formatGroup.add(imageFormatWEBP);

        switch (cb.getImageFormat()) {
            case Png -> imageFormatPNG.setSelected(true);
            case Jpeg -> imageFormatJPG.setSelected(true);
            case Webp -> imageFormatWEBP.setSelected(true);
        }

        JRadioButtonMenuItem imageCutWhite = new JRadioButtonMenuItem(StringResources.MENU_CUT_WHITE_EDGE);
        imageCutWhite.addActionListener(handler);
        JRadioButtonMenuItem imageCutBlack = new JRadioButtonMenuItem(StringResources.MENU_CUT_BLACK_EDGE);
        imageCutBlack.addActionListener(handler);

        ButtonGroup cutGroup = new ButtonGroup();
        cutGroup.add(imageCutWhite);
        cutGroup.add(imageCutBlack);

        if (cb.isCutWhiteEdge()) {
            imageCutWhite.setSelected(true);
        } else {
            imageCutBlack.setSelected(true);
        }

        JRadioButtonMenuItem imageSize480 = new JRadioButtonMenuItem(StringResources.MENU_PIC_SIZE_480);
        imageSize480.addActionListener(handler);
        JRadioButtonMenuItem imageSize600 = new JRadioButtonMenuItem(StringResources.MENU_PIC_SIZE_600);
        imageSize600.addActionListener(handler);
        JRadioButtonMenuItem imageSize800 = new JRadioButtonMenuItem(StringResources.MENU_PIC_SIZE_800);
        imageSize800.addActionListener(handler);

        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(imageSize480);
        sizeGroup.add(imageSize600);
        sizeGroup.add(imageSize800);

        switch (cb.getImageSize()) {
            case 480 -> imageSize480.setSelected(true);
            case 600 -> imageSize600.setSelected(true);
            case 800 -> imageSize800.setSelected(true);
        }

        image.add(imageToGray);
        image.addSeparator();
        image.add(imageForceGray);
        image.add(imageAutoGray);
        image.addSeparator();
        image.add(imageFormatPNG);
        image.add(imageFormatJPG);
        image.add(imageFormatWEBP);
        image.addSeparator();
        image.add(imageCutWhite);
        image.add(imageCutBlack);
        image.addSeparator();
        image.add(imageSize480);
        image.add(imageSize600);
        image.add(imageSize800);
    }

    public JMenuBar getMenuBar() {
        return bar;
    }
}
