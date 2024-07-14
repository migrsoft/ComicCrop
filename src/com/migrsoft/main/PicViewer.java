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
        Move,
        Resize,
    }

    private Mode currentMode = Mode.View;

    private final SelectBox selectBox = new SelectBox();
    private ImageItem currentImageItem = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            popMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private Point initialClick;

    private EdgeDetect detector;

    private EdgeDetect.Edge selectedEdge = EdgeDetect.Edge.None;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            initialClick = null;
            switch (currentMode) {
                case View -> {
                    handleClickSubtitles(e.getPoint());
                    repaint();
                }
                case Create -> {
                    saveSelectedSubtitles();
                    selectBox.setTopLeft(e.getX(), e.getY());
                    currentImageItem = longImage.getSelectedImage(e.getY() + viewPort.y);
                    repaint();
                }
                case Edit -> {
                    if (selectedEdge != EdgeDetect.Edge.None) {
                        currentMode = Mode.Resize;
                        initialClick = e.getPoint();
                    } else if (selectBox.contains(e.getX(), e.getY())) {
                        currentMode = Mode.Move;
                        initialClick = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        handleClickSubtitles(e.getPoint());
                    }
                    repaint();
                }
                case EditDlg -> {
                    if (selectBox.contains(e.getX(), e.getY())) {
                        currentMode = Mode.Edit;
                    } else {
                        currentMode = Mode.View;
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (currentMode) {
            case Create -> {
                currentMode = Mode.Edit;
            }
            case Move -> {
                currentMode = Mode.Edit;
                setCursor(Cursor.getDefaultCursor());
            }
            case Resize -> {
                currentMode = Mode.Edit;
                selectedEdge = EdgeDetect.Edge.None;
                selectBox.setModified();
                selectBox.setDisplaySubtitles(true);
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }
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
        switch (currentMode) {
            case Create -> {
                if (selectBox.dragBottomRight(e.getX(), e.getY())) {
                    repaint();
                }
            }
            case Move -> {
                if (initialClick != null) {
                    int dx = e.getX() - initialClick.x;
                    int dy = e.getY() - initialClick.y;
                    int x = selectBox.rect.x + dx;
                    int y = selectBox.rect.y + dy;
                    selectBox.setLocation(x, y);
                    initialClick = e.getPoint();
                    repaint();
                }
            }
            case Resize -> {
                if (initialClick != null) {
                    int dx = e.getX() - initialClick.x;
                    int dy = e.getY() - initialClick.y;
                    EdgeDetect.adjust(selectBox.rect, selectedEdge, dx, dy);
                    initialClick = e.getPoint();
                    selectBox.setDisplaySubtitles(false);
                    repaint();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        switch (currentMode) {
            case Edit -> {
                if (detector != null) {
                    selectedEdge = detector.isOnTheEdge(e.getPoint());
                    switch (selectedEdge) {
                        case None -> setCursor(Cursor.getDefaultCursor());
                        case West -> setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                        case East -> setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                        case North -> setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                        case South -> setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                        case NorthWest -> setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                        case NorthEast -> setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                        case SouthWest -> setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                        case SouthEast -> setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    }
                }
            }
        }
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
                String name = cb.getNameByIndex(index);
                if (lastLoaded != index && !name.isEmpty()) {
                    lastLoaded = index;
                    new ImageLoaderWorker(cb.getZip(), name, index, y > 0).execute();
                }
            }
        }
    }

    private void handleClickSubtitles(Point pos) {
        saveSelectedSubtitles();
        currentImageItem = longImage.getSelectedImage(pos.y + viewPort.y);
        SubtitleItem si = longImage.getSubtitleByPos(currentImageItem, pos.x, pos.y, viewPort);
        if (si != null) {
            selectBox.rect = longImage.rectToView(currentImageItem, si.rect, viewPort);
            selectBox.setSubtitle(si);
            currentMode = Mode.Edit;
            initialClick = pos;
            detector = new EdgeDetect(selectBox.rect);
        } else {
            detector = null;
        }
    }

    public interface PicViewerCallback {
        ZipFile getZip();
        String getPath();
        String getFileName();
        int getCurrentIndex();
        void setCurrentIndex(int index);
        String getNameByIndex(int index);
    }

    private PicViewerCallback cb;

    public void setActListener(PicViewerCallback listener) {
        cb = listener;
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
                    viewPort.y += longImage.addImage(image, index, name, atLast);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private JPopupMenu popMenu;

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
                    case StringResources.MENU_POP_CREATE -> { currentMode = Mode.Create; }
                    case StringResources.MENU_POP_DELETE -> onPopMenuDelete();
                    case StringResources.MENU_POP_OCR -> onPopMenuOcr();
                    case StringResources.MENU_POP_EDIT -> onPopMenuEdit();
                }
            }
        };

        popMenu = new JPopupMenu();
        JMenuItem menuCreate = new JMenuItem(StringResources.MENU_POP_CREATE);
        JMenuItem menuDelete = new JMenuItem(StringResources.MENU_POP_DELETE);
        JMenuItem menuOcr = new JMenuItem(StringResources.MENU_POP_OCR);
        JMenuItem menuEdit = new JMenuItem(StringResources.MENU_POP_EDIT);
        menuCreate.addActionListener(popMenuHandler);
        menuDelete.addActionListener(popMenuHandler);
        menuOcr.addActionListener(popMenuHandler);
        menuEdit.addActionListener(popMenuHandler);
        popMenu.add(menuCreate);
        popMenu.add(menuDelete);
        popMenu.add(menuOcr);
        popMenu.add(menuEdit);
    }

    private void onPopMenuDelete() {
        if (currentImageItem != null && selectBox.notEmpty()) {
            longImage.removeSubtitle(currentImageItem, selectBox.getSubtitle(), viewPort);
            selectBox.initialize();
        }
    }

    private void onPopMenuOcr() {
        if (currentImageItem != null && selectBox.notEmpty()) {
            Rectangle r = longImage.rectToImage(currentImageItem, selectBox.rect, viewPort);
            String text = TesserOCR.ocr(currentImageItem.image, r);
            text = text.replace("\n", " ");
            selectBox.updateOriginalText(text);
            repaint();
        }
    }

    private void onPopMenuEdit() {
        if (selectBox.notEmpty()) {
            currentMode = Mode.EditDlg;
            repaint();
            final EditDlg dlg = new EditDlg(ComicCrop.getInstance());
            dlg.setOriginalText(selectBox.getOriginalText());
            dlg.setTranslatedText(selectBox.getTranslatedText());
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
        g2.setPaint(Color.DARK_GRAY);
        g2.fill(area);

        if (longImage.height > 0) {
            longImage.paint(g2, viewPort, selectBox.getSubtitle(), MainParam.getInstance().getPageNumberSwitch());
            cb.setCurrentIndex(longImage.getCurrentIndex());
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
    public void loadImage(String name) {
        assert cb != null;

        if (cb.getCurrentIndex() == longImage.getCurrentIndex()) {
            return;
        }

        reset();

        BufferedImage image = PicWorker.load(cb.getZip(), name, MainParam.getInstance());
        if (image != null) {
            lastLoaded = cb.getCurrentIndex();
            longImage.addImage(image, lastLoaded, cb.getNameByIndex(lastLoaded),true);
        }

        invalidate();
        repaint();
    }

    private void saveSelectedSubtitles() {
        if (selectBox.isModified()) {
            longImage.addSubtitle(currentImageItem, selectBox.takeSubtitle(), viewPort);
        }
        selectBox.initialize();
    }

    public void saveSubtitles() {
        if (!cb.getFileName().isEmpty()) {
            String path = cb.getPath() + cb.getFileName();
            longImage.saveSubtitles(path);
        }
    }

    public void loadSubtitles() {
        if (!cb.getFileName().isEmpty()) {
            String path = cb.getPath() + cb.getFileName();
            longImage.loadSubtitles(path);
        }
    }
}
