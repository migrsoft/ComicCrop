package com.migrsoft.main;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class RenamePlusDlg extends JDialog {
    private JPanel contentPane;
    private JTable tablePreview;
    private JTextField textPattern;
    private JTextField textStart;
    private JButton renameButton;
    private JButton cancelButton;
    private JTextField textWidth;
    private JButton previewButton;
    private String mBasePath;

    private int mCounterStart = 1;
    private int mWidthOfNumber = 3;

    private RenameThem mRenameThem = null;

    public Vector<RenameThem.Item> getResults() {
        return mRenameThem.getResult();
    }

    public interface MyActionListener {
        void onRename();
    }

    private MyActionListener mMyActionListener = null;

    void setMyActionListener(MyActionListener listener) {
        mMyActionListener = listener;
    }

    private class MyTableModel extends AbstractTableModel {
        @Override
        public String getColumnName(int column) {
            switch(column) {
                case 0:
                    return "原名";
                case 2:
                    return "新名";
                case 1:
                case 3:
                    return "扩展名";
            }
            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public int getRowCount() {
            return (mRenameThem != null) ? mRenameThem.getResult().size() : 0;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RenameThem.Item item = mRenameThem.getResult().get(rowIndex);
            switch(columnIndex) {
                case 0:
                    return item.getOriginNameWithoutExt();
                case 1:
                    return item.getOriginExtName();
                case 2:
                    return item.getNewNameWithoutExt();
                case 3:
                    return item.getNewExtName();
            }
            return "";
        }
    }

    public RenamePlusDlg() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancelButton);

        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPreview();
            }
        });

        renameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRename();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screen.getWidth() * 0.9f), (int)(screen.getHeight() * 0.9f));

        textStart.setText(Integer.toString(mCounterStart));
        textWidth.setText(Integer.toString(mWidthOfNumber));

        updateTable();
    }

    private void updateTable() {
        tablePreview.setModel(new MyTableModel());
        tablePreview.setShowGrid(true);
        TableColumn col2 = tablePreview.getColumnModel().getColumn(1);
        col2.setMaxWidth(150);
        col2.setResizable(false);
        TableColumn col4 = tablePreview.getColumnModel().getColumn(3);
        col4.setMaxWidth(150);
        col4.setResizable(false);
    }

    private void onPreview() {
        try {
            int start = Integer.parseInt(textStart.getText());
            int width = Integer.parseInt(textWidth.getText());
            mCounterStart = start;
            mWidthOfNumber = width;
        } catch (NumberFormatException e) {
            return;
        }
        mRenameThem.rename(textPattern.getText(), mCounterStart, mWidthOfNumber);
        updateTable();
    }

    private void onRename() {
        if (mRenameThem.isRenamed()) {
            if (mMyActionListener != null) {
                mMyActionListener.onRename();
            }
            dispose();
        }
    }

    private void onCancel() {
        dispose();
    }

    public void setData(Vector<String> list, String basePath) {
        mBasePath = basePath;
        mRenameThem = new RenameThem();
        mRenameThem.initial(list);
    }

    public static void main(String[] args) {
        RenamePlusDlg dialog = new RenamePlusDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
