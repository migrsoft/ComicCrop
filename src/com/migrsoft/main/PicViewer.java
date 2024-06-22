package com.migrsoft.main;

import com.migrsoft.image.PicWorker;

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
    }

    private Mode currentMode = Mode.View;

    private final int MINI_SIDE = 20;
    private Rectangle mSelectedRect = new Rectangle();

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            mPopMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (currentMode == Mode.Create) {
                float x = e.getX();
                float y = e.getY();
                mSelectedRect.setRect(x, y, MINI_SIDE, MINI_SIDE);
                repaint();
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
            int dx = e.getX() - mSelectedRect.x;
            int dy = e.getY() - mSelectedRect.y;
            if (dx >= MINI_SIDE && dy >= MINI_SIDE) {
                mSelectedRect.setSize(dx, dy);
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

    private LongImage longImage = null;

    private int viewY;
    private int viewWidth;
    private int viewHeight;

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
                    viewY += longImage.addImage(image, index, atLast);
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
                    default:
                }
            }
        };

        mPopMenu = new JPopupMenu();
        JMenuItem menuCreate = new JMenuItem(StringResources.MENU_POP_CREATE);
        JMenuItem menuDelete = new JMenuItem(StringResources.MENU_POP_DELETE);
        JMenuItem menuOcr = new JMenuItem(StringResources.MENU_POP_OCR);
        menuCreate.addActionListener(popMenuHandler);
        menuDelete.addActionListener(popMenuHandler);
        menuOcr.addActionListener(popMenuHandler);
        mPopMenu.add(menuCreate);
        mPopMenu.add(menuDelete);
        mPopMenu.add(menuOcr);
    }

//    private BufferedImage tempImage;
    private Rectangle ocrRect = new Rectangle();

    private void onPopMenuOcr() {
//        BufferedImage image = getCurrentImage();
//        assert image != null;
//        System.out.println(TesserOCR.ocr(image, ocrRect));
    }

    public void reset() {
        viewY = 0;
        if (longImage != null) {
            longImage.reset();
        }
        revalidate();
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        if (currentMode == Mode.View && longImage != null) {
            final int scrollAmount = 20;
            int amount = evt.getUnitsToScroll() * scrollAmount;
            viewY += amount;
            int tempY = viewY;
            viewY = Math.max(0, Math.min(viewY, longImage.height - viewHeight));
            invalidate();
            repaint();

            if (tempY < 0 || tempY + viewHeight > longImage.height) {
                int index = (tempY < 0)
                        ? longImage.getIndexAtFirst() - 1
                        : longImage.getIndexAtLast() + 1;
                String name = callback.getStringByIndex(index);
                if (lastLoaded != index && !name.isEmpty()) {
//                    System.out.println("last " + lastLoaded + " index " + index + " viewY " + viewY);
                    lastLoaded = index;
                    new ImageLoaderWorker(callback.getZip(), name, index, tempY > 0).execute();
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

        if (longImage != null) {
            Rectangle viewPort = new Rectangle();
            viewPort.setBounds(0, viewY, viewWidth, viewHeight);
            longImage.paint(g2, viewPort);
            callback.setCurrentIndex(longImage.getCurrentIndex());
        }
//
//            g2.setPaint(Color.RED);
//            g2.drawRect(mSelectedRect.x, mSelectedRect.y, mSelectedRect.width, mSelectedRect.height);
//        }

//        if (tempImage != null) {
//            g2.drawImage(tempImage,
//                    0, 0,
//                    ocrRect.width, ocrRect.height,
//                    ocrRect.x, ocrRect.y,
//                    ocrRect.x + ocrRect.width, ocrRect.y + ocrRect.height,
//                    null);
//        }
    }

    /*
     * 打开指定的图片
     */
    public void load(String name) {
        assert callback != null;

        if (longImage == null) {
            longImage = new LongImage();
        }

//        System.out.println("load " + callback.getCurrentIndex() + " " + longImage.getCurrentIndex());
        if (callback.getCurrentIndex() == longImage.getCurrentIndex()) {
            return;
        }

        viewY = 0;
        viewWidth = getWidth();
        viewHeight = getHeight();

        BufferedImage image = PicWorker.load(callback.getZip(), name, MainParam.getInstance());
        if (image != null) {
            lastLoaded = callback.getCurrentIndex();
            longImage.addImage(image, lastLoaded, true);
        }

        invalidate();
        repaint();
    }

//    private BufferedImage getCurrentImage() {
//        if (!mImageList.isEmpty()) {
//            int y = 0;
//            Rectangle imageRect = new Rectangle();
//            for (ImageItem ii : mImageList) {
//                imageRect.setBounds(0, y, ii.image.getWidth(), ii.image.getHeight());
//                if (imageRect.contains(mSelectedRect.x, mSelectedRect.y + mViewY)) {
//                    ocrRect.x = mSelectedRect.x - ii.viewX;
//                    ocrRect.y = mSelectedRect.y + (mViewY - y);
//                    ocrRect.setSize(mSelectedRect.width, mSelectedRect.height);
//                    return ii.image;
//                }
//                y += ii.image.getHeight();
//            }
//        }
//        return null;
//    }
}
