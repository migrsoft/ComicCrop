package com.migrsoft.main;

import com.migrsoft.image.PicWorkerParam;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBarInViewMode {

    public interface Callback {
        void onFileOpen();
        void onFileOpenComic();
        void onFileSaveSubtitle();

        PicWorkerParam.SubtitleSwitch getSubtitleSwitch();
        void onSubtitleOff();
        void onSubtitleOrigin();
        void onSubtitleChinese();
    }

    private final JMenuBar bar;

    public MenuBarInViewMode(Callback cb) {
        assert cb != null;

        bar = new JMenuBar();

        ActionListener handler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                switch (cmd) {
                    case StringResources.MENU_FILE_OPEN -> cb.onFileOpen();

                    case StringResources.MENU_FILE_OPEN_COMIC -> cb.onFileOpenComic();

                    case StringResources.MENU_FILE_SAVE_SUBTITLE -> cb.onFileSaveSubtitle();

                    case StringResources.MENU_SUBTITLE_NO -> {
                        JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
                        if (item.isSelected()) {
                            cb.onSubtitleOff();
                        }
                    }

                    case StringResources.MENU_SUBTITLE_ORIGIN -> {
                        JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
                        if (item.isSelected()) {
                            cb.onSubtitleOrigin();
                        }
                    }

                    case StringResources.MENU_SUBTITLE_CHINESE -> {
                        JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
                        if (item.isSelected()) {
                            cb.onSubtitleChinese();
                        }
                    }
                }
            }
        };

        JMenu file = new JMenu(StringResources.MENU_FILE);
        bar.add(file);

        JMenuItem fileOpen = new JMenuItem(StringResources.MENU_FILE_OPEN);
        fileOpen.addActionListener(handler);
        JMenuItem fileOpenComic = new JMenuItem(StringResources.MENU_FILE_OPEN_COMIC);
        fileOpenComic.addActionListener(handler);
        JMenuItem fileSaveSubtitle = new JMenuItem(StringResources.MENU_FILE_SAVE_SUBTITLE);
        fileSaveSubtitle.addActionListener(handler);

        file.add(fileOpen);
        file.add(fileOpenComic);
        file.addSeparator();
        file.add(fileSaveSubtitle);

        JMenu image = new JMenu(StringResources.MENU_IMAGE);
        bar.add(image);

        JRadioButtonMenuItem subtitleNo = new JRadioButtonMenuItem(StringResources.MENU_SUBTITLE_NO);
        subtitleNo.addActionListener(handler);
        JRadioButtonMenuItem subtitleOrigin = new JRadioButtonMenuItem(StringResources.MENU_SUBTITLE_ORIGIN);
        subtitleOrigin.addActionListener(handler);
        JRadioButtonMenuItem subtitleChinese = new JRadioButtonMenuItem(StringResources.MENU_SUBTITLE_CHINESE);
        subtitleChinese.addActionListener(handler);

        ButtonGroup subtitle = new ButtonGroup();
        subtitle.add(subtitleNo);
        subtitle.add(subtitleOrigin);
        subtitle.add(subtitleChinese);

        switch (cb.getSubtitleSwitch()) {
            case Off -> subtitleNo.setSelected(true);
            case Original -> subtitleOrigin.setSelected(true);
            case Chinese -> subtitleChinese.setSelected(true);
        }

        image.add(subtitleNo);
        image.add(subtitleOrigin);
        image.add(subtitleChinese);
    }

    public JMenuBar getMenuBar() {
        return bar;
    }
}
