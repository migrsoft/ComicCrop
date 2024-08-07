package com.migrsoft.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.deepl.api.*;

public class EditDlg extends JDialog {

    private final JTextArea originalText;
    private final JTextArea translatedText;

    private final Translator translator;

    public interface Callback {
        void onEllipse(boolean value);
        void onSave();
    }

    private Callback cb = null;

    public void setCallback(Callback cb) {
        this.cb = cb;
    }

    public EditDlg(Frame owner, boolean isEllipse) {
        super(owner, StringResources.MENU_POP_EDIT, true);

        setSize(400, 300);
        setLayout(new BorderLayout());

        translator = new Translator(Config.getInstance().getDeepLApiKey());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Font font = FontManager.getInstance().getFont(StringResources.FONT_MAIN, Font.PLAIN, 16);

        // Create the first text area
        originalText = new JTextArea(5, 30);
        originalText.setLineWrap(true);
        originalText.setWrapStyleWord(true);
        originalText.setFont(font);
        JScrollPane scrollPane1 = new JScrollPane(originalText);
        panel.add(scrollPane1);

        // Create the second text area
        translatedText = new JTextArea(5, 30);
        translatedText.setLineWrap(true);
        translatedText.setWrapStyleWord(true);
        translatedText.setFont(font);
        JScrollPane scrollPane2 = new JScrollPane(translatedText);
        panel.add(scrollPane2);

        // Add the panel to the dialog
        add(panel, BorderLayout.CENTER);

        JCheckBox ellipseButton = new JCheckBox(StringResources.BUTTON_ELLIPSE, isEllipse);
        ellipseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                cb.onEllipse(box.isSelected());
            }
        });

        JButton transButton = new JButton(StringResources.BUTTON_TRANSLATE);
        transButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TextResult result = translator.translateText(getOriginalText(), null, "ZH");
                    setTranslatedText(result.getText());
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

            }
        });

        JButton saveButton = new JButton(StringResources.BUTTON_SAVE);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb != null) {
                    cb.onSave();
                }
                dispose();
            }
        });

        // Create a button to close the dialog
        JButton closeButton = new JButton(StringResources.BUTTON_CANCEL);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Add the close button to the dialog
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(ellipseButton);
        buttonPanel.add(transButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getOriginalText() {
        return originalText.getText();
    }

    public void setOriginalText(String text) {
        originalText.setText(text);
    }

    public String getTranslatedText() {
        return translatedText.getText();
    }

    public void setTranslatedText(String text) {
        translatedText.setText(text);
    }
}
