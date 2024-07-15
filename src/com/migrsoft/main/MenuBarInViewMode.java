package com.migrsoft.main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBarInViewMode {

    public interface Callback {
        void onFileOpen();
        void onFileOpenComic();
        void onFileSaveSubtitle();

        MainParam.SubtitleSwitch getSubtitleSwitch();
        void onSubtitle(MainParam.SubtitleSwitch value);

        boolean getPageSpacingSwitch();
        void setSpacingSwitch(boolean value);

        boolean getPageNumberSwitch();
        void setPageNumberSwitch(boolean value);

        void onManual();
        void onAbout();
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
                    case StringResources.MENU_SUBTITLE_NO -> cb.onSubtitle(MainParam.SubtitleSwitch.Off);
                    case StringResources.MENU_SUBTITLE_ORIGIN -> cb.onSubtitle(MainParam.SubtitleSwitch.Original);
                    case StringResources.MENU_SUBTITLE_CHINESE -> cb.onSubtitle(MainParam.SubtitleSwitch.Chinese);
                    case StringResources.MENU_PAGE_SPACING -> {
                        JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                        cb.setSpacingSwitch(item.isSelected());
                    }
                    case StringResources.MENU_PAGE_NUMBER -> {
                        JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                        cb.setPageNumberSwitch(item.isSelected());
                    }
                    case StringResources.MENU_HELP_MANUAL -> cb.onManual();
                    case StringResources.MENU_HELP_ABOUT -> cb.onAbout();
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

        JCheckBoxMenuItem pageSpacing = new JCheckBoxMenuItem(StringResources.MENU_PAGE_SPACING);
        pageSpacing.addActionListener(handler);
        pageSpacing.setSelected(cb.getPageSpacingSwitch());

        JCheckBoxMenuItem pageNumber = new JCheckBoxMenuItem(StringResources.MENU_PAGE_NUMBER);
        pageNumber.addActionListener(handler);
        pageNumber.setSelected(cb.getPageNumberSwitch());

        image.add(subtitleNo);
        image.add(subtitleOrigin);
        image.add(subtitleChinese);
        image.addSeparator();
        image.add(pageSpacing);
        image.add(pageNumber);

        JMenu help = new JMenu(StringResources.MENU_HELP);
        bar.add(help);

        JMenuItem helpManual = new JMenuItem(StringResources.MENU_HELP_MANUAL);
        helpManual.addActionListener(handler);
        JMenuItem helpAbout = new JMenuItem(StringResources.MENU_HELP_ABOUT);
        helpAbout.addActionListener(handler);

        help.add(helpManual);
        help.add(helpAbout);
    }

    public JMenuBar getMenuBar() {
        return bar;
    }
}
