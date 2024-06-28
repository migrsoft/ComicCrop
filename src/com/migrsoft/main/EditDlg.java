package com.migrsoft.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.deepl.api.*;

public class EditDlg extends JDialog {

    private final JTextArea originalText;
    private final JTextArea translatedText;

    private final Translator translator = new Translator("f2b30731-e1e5-4f94-89c5-d852507769e2:fx");

    public interface Callback {
        void onSave();
    }

    private Callback callback = null;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public EditDlg(Frame owner) {
        super(owner, StringResources.MENU_POP_EDIT, true);

        setSize(400, 300);
        setLayout(new BorderLayout());

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
                if (callback != null) {
                    callback.onSave();
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
