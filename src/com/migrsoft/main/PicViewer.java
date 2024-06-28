package com.migrsoft.main;

import com.migrsoft.image.PicWorker;
import com.migrsoft.image.TesserOCR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.zip.ZipFile;

public class PicViewer extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener {

    private enum Mode {
        View,
        Create,
        Edit,
        EditDlg,
    }

    private Mode currentMode = Mode.View;

    private final SelectBox selectBox = new SelectBox();
    private ImageItem currentImageItem = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            mPopMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            switch (currentMode) {
                case View:
                    if (selectBox.contains(e.getX(), e.getY())) {
                        currentMode = Mode.Edit;
                    }
                    break;

                case Create:
                    selectBox.setTopLeft(e.getX(), e.getY());
                    currentImageItem = longImage.getSelectedImage(e.getY() + viewPort.y);
                    if (currentImageItem != null) {
                        Rectangle range = currentImageItem.getVisibleRectInViewPort(viewPort);
                        selectBox.setRange(range.x, range.y, range.width, range.height);
                    }
                    repaint();
                    break;

                case Edit:
                case EditDlg:
                    if (selectBox.contains(e.getX(), e.getY())) {
                        currentMode = Mode.Edit;
                    } else {
                        currentMode = Mode.View;
                    }
                    repaint();
                    break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentMode == Mode.Create) {
            currentMode = Mode.View;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentMode == Mode.Create) {
            if (selectBox.dragBottomRight(e.getX(), e.getY())) {
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public interface PicViewerCallback {
        ZipFile getZip();
        int getCurrentIndex();
        void setCurrentIndex(int index);
        String getStringByIndex(int index);
    }

    private PicViewerCallback callback = null;

    public void setActListener(PicViewerCallback listener) {
        callback = listener;
    }

    private final LongImage longImage = new LongImage();

    private final Rectangle viewPort = new Rectangle();

    private int lastLoaded = -1;

    private class ImageLoaderWorker extends SwingWorker<BufferedImage, Void> {

        private final ZipFile zip;
        private final String name;
        private final int index;
        private final boolean atLast;

        public ImageLoaderWorker(ZipFile zip, String name, int index, boolean last) {
            this.zip = zip;
            this.name = name;
            this.index = index;
            atLast = last;
        }

        @Override
        protected BufferedImage doInBackground() throws Exception {
            return PicWorker.load(zip, name, MainParam.getInstance());
        }

        @Override
        protected void done() {
            try {
                BufferedImage image = get();
                if (image != null) {
                    viewPort.y += longImage.addImage(image, index, atLast);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private JPopupMenu mPopMenu;

    PicViewer() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        createPopupMenu();
        setFocusable(true);
    }

    private void createPopupMenu() {
        ActionListener popMenuHandler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()) {
                    case StringResources.MENU_POP_CREATE:
                        currentMode = Mode.Create;
                        break;
                    case StringResources.MENU_POP_DELETE:
                        break;
                    case StringResources.MENU_POP_OCR:
                        onPopMenuOcr();
                        break;
                    case StringResources.MENU_POP_EDIT:
                        onPopMenuEdit();
                        break;
                    default:
                }
            }
        };

        mPopMenu = new JPopupMenu();
        JMenuItem menuCreate = new JMenuItem(StringResources.MENU_POP_CREATE);
        JMenuItem menuDelete = new JMenuItem(StringResources.MENU_POP_DELETE);
        JMenuItem menuOcr = new JMenuItem(StringResources.MENU_POP_OCR);
        JMenuItem menuEdit = new JMenuItem(StringResources.MENU_POP_EDIT);
        menuCreate.addActionListener(popMenuHandler);
        menuDelete.addActionListener(popMenuHandler);
        menuOcr.addActionListener(popMenuHandler);
        menuEdit.addActionListener(popMenuHandler);
        mPopMenu.add(menuCreate);
        mPopMenu.add(menuDelete);
        mPopMenu.add(menuOcr);
        mPopMenu.add(menuEdit);
    }

    private void onPopMenuOcr() {
        if (currentImageItem != null && selectBox.notEmpty()) {
            Rectangle r = longImage.rectToImage(currentImageItem, selectBox.rect, viewPort);
            String text = TesserOCR.ocr(currentImageItem.image, r);
            text = text.replace("\n", " ");
            selectBox.originalText = text;
            repaint();
        }
    }

    private void onPopMenuEdit() {
        if (selectBox.notEmpty()) {
            currentMode = Mode.EditDlg;
            repaint();
            final EditDlg dlg = new EditDlg(ComicCrop.getInstance());
            dlg.setOriginalText(selectBox.originalText);
            dlg.setTranslatedText(selectBox.translatedText);
            dlg.setCallback(new EditDlg.Callback() {
                @Override
                public void onSave() {
                    selectBox.updateOriginalText(dlg.getOriginalText());
                    selectBox.updateTranslatedText(dlg.getTranslatedText());
                    currentMode = Mode.View;
                    repaint();
                }
            });
            dlg.setLocationRelativeTo(ComicCrop.getInstance());
            dlg.setVisible(true);
        }
    }

    public void reset() {
        viewPort.setLocation(0, 0);
        viewPort.setSize(getWidth(), getHeight());
        longImage.reset();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        if (currentMode == Mode.View && longImage != null) {
            final int scrollAmount = 20;
            int amount = evt.getUnitsToScroll() * scrollAmount;
            viewPort.y += amount;
            int y = viewPort.y;
            viewPort.y = Math.max(0, Math.min(viewPort.y, longImage.height - viewPort.height));
            invalidate();
            repaint();

            if (y < 0 || y + viewPort.height > longImage.height) {
                int index = (y < 0)
                        ? longImage.getIndexAtFirst() - 1
                        : longImage.getIndexAtLast() + 1;
                String name = callback.getStringByIndex(index);
                if (lastLoaded != index && !name.isEmpty()) {
                    lastLoaded = index;
                    new ImageLoaderWorker(callback.getZip(), name, index, y > 0).execute();
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
        g2.setPaint(Color.DARK_GRAY);
        g2.fill(area);

        if (longImage.height > 0) {
            longImage.paint(g2, viewPort);
            callback.setCurrentIndex(longImage.getCurrentIndex());
        }

        // 绘制选择框
        selectBox.paint(g);

        // 显示选择的图片
        if ((currentMode == Mode.Create || currentMode == Mode.EditDlg)
                && currentImageItem != null && selectBox.notEmpty()) {
            Rectangle r = longImage.rectToImage(currentImageItem, selectBox.rect, viewPort);
            g2.drawImage(currentImageItem.image,
                    0, 0, selectBox.rect.width, selectBox.rect.height,
                    r.x, r.y, r.x + selectBox.rect.width, r.y + selectBox.rect.height,
                    null);
        }
    }

    /*
     * 打开指定的图片
     */
    public void load(String name) {
        assert callback != null;

        if (callback.getCurrentIndex() == longImage.getCurrentIndex()) {
            return;
        }

        reset();

        BufferedImage image = PicWorker.load(callback.getZip(), name, MainParam.getInstance());
        if (image != null) {
            lastLoaded = callback.getCurrentIndex();
            longImage.addImage(image, lastLoaded, true);
        }

        invalidate();
        repaint();
    }
}
