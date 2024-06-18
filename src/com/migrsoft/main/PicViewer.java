package com.migrsoft.main;

import com.migrsoft.image.PicWorker;
import com.migrsoft.image.TesserOCR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.ZipFile;

public class PicViewer extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener {

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
            float x = e.getX();
            float y = e.getY();
            mSelectedRect.setRect(x, y, 10, 10);
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - mSelectedRect.x;
        int dy = e.getY() - mSelectedRect.y;
        if (dx >= 10 && dy >= 10) {
            mSelectedRect.setSize(dx, dy);
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public interface ActListener {
        ZipFile getZip();
        int getCurrentIndex();
        void setCurrentIndex(int index);
        String getStringByIndex(int index);
    }

    private ActListener mActListener = null;

    public void setActListener(ActListener listener) {
        mActListener = listener;
    }

    private class ImageItem {
        public int mIndex;
        public int mViewX;
        public BufferedImage mImage;
    }

    LinkedList<ImageItem> mImageList = new LinkedList<>();

    private int mViewY;
    private int mViewWidth;
    private int mViewHeight;
    private int mTotalImageHeight;

    private final Font mFont;

    private int mLastLoaded = -1;

    private int mCurrentIndex = -1;

    private class ImageLoaderWorker extends SwingWorker<BufferedImage, Void> {

        private ZipFile mZip;
        private String mName;
        private int mIndex;
        private boolean mAtLast;

        public ImageLoaderWorker(ZipFile zip, String name, int index, boolean last) {
            mZip = zip;
            mName = name;
            mIndex = index;
            mAtLast = last;
        }

        @Override
        protected BufferedImage doInBackground() throws Exception {
            return PicWorker.load(mZip, mName, MainParam.getInstance());
        }

        @Override
        protected void done() {
            try {
                BufferedImage image = get();
                if (image != null) {
                    addImage(image, mIndex, mAtLast);
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
        mFont = new Font("SansSerif", Font.BOLD, 20);
        setFocusable(true);
    }

    private void createPopupMenu() {
        final String menuPopOcr = "OCR";

        ActionListener popMenuHandler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(menuPopOcr)) {
                    onPopMenuOcr();
                }
            }
        };

        mPopMenu = new JPopupMenu();
        JMenuItem menuOcr = new JMenuItem(menuPopOcr);
        menuOcr.addActionListener(popMenuHandler);
        mPopMenu.add(menuOcr);
    }

//    private BufferedImage tempImage;
    private Rectangle ocrRect = new Rectangle();

    private void onPopMenuOcr() {
        BufferedImage image = getCurrentImage();
        assert image != null;
        System.out.println(TesserOCR.ocr(image, ocrRect));
    }

    public void reset() {
        mCurrentIndex = -1;
        mTotalImageHeight = 0;
        mViewY = 0;
        mImageList.clear();
        System.gc();
        revalidate();
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        int mScrollAmount = 20;
        int amount = evt.getUnitsToScroll() * mScrollAmount;
        mViewY += amount;
        int viewY = mViewY;
        mViewY = Math.max(0, Math.min(mViewY, mTotalImageHeight - mViewHeight));
        invalidate();
        repaint();

        if (!mImageList.isEmpty()) {
            if (viewY < 0 || viewY + mViewHeight > mTotalImageHeight) {
                assert mImageList.peekFirst() != null;
                assert mImageList.peekLast() != null;
                int index = (viewY < 0) ? mImageList.peekFirst().mIndex - 1 : mImageList.peekLast().mIndex + 1;
                String name = mActListener.getStringByIndex(index);
                if (mLastLoaded != index && !name.isEmpty()) {
                    mLastLoaded = index;
                    new ImageLoaderWorker(mActListener.getZip(), name, index, viewY > 0).execute();
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

        if (!mImageList.isEmpty()) {
            ListIterator<ImageItem> it = mImageList.listIterator();
            int y = 0;
            int dx1, dx2, dy1 = 0, dy2;
            int sx1, sx2, sy1, sy2;
            boolean first = true;
            String hintSize = "? x ?";
            Rectangle imageRect = new Rectangle();
            Rectangle viewRect = new Rectangle();
            while (it.hasNext() && y < mViewY + mViewHeight) {
                ImageItem ii = it.next();
                imageRect.setBounds(0, y, ii.mImage.getWidth(), ii.mImage.getHeight());
                viewRect.setBounds(0, mViewY, mViewWidth, mViewHeight);
                if (imageRect.intersects(viewRect)) {
                    Rectangle isr = new Rectangle();
                    Rectangle.intersect(imageRect, viewRect, isr);
                    if (first) {
                        first = false;
                        hintSize = ii.mImage.getWidth() + " x " + ii.mImage.getHeight();
                        mCurrentIndex = ii.mIndex;
                        mActListener.setCurrentIndex(mCurrentIndex);
                    }
                    dx1 = ii.mViewX;
                    dx2 = dx1 + ii.mImage.getWidth();
                    dy2 = dy1 + (int)isr.getHeight();
                    sx1 = 0;
                    sx2 = sx1 + ii.mImage.getWidth();
                    sy1 = isr.y - y;
                    sy2 = sy1 + isr.height;
                    g2.drawImage(ii.mImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    dy1 += isr.height;
                }
                y += ii.mImage.getHeight();
            }

            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D bounds = mFont.getStringBounds(hintSize, frc);
            g2.setFont(mFont);
            g2.setPaint(Color.LIGHT_GRAY);
            g2.drawString(hintSize, 2, -(int) bounds.getY() + 2);

            g2.setPaint(Color.RED);
            g2.drawRect(mSelectedRect.x, mSelectedRect.y, mSelectedRect.width, mSelectedRect.height);
        }

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
        assert mActListener != null;

        if (mActListener.getCurrentIndex() == mCurrentIndex) {
            return;
        }

        mImageList.clear();
        System.gc();

        mTotalImageHeight = 0;
        mViewY = 0;
        mViewWidth = getWidth();
        mViewHeight = getHeight();

        BufferedImage image = PicWorker.load(mActListener.getZip(), name, MainParam.getInstance());
        if (image != null) {
            mLastLoaded = mActListener.getCurrentIndex();
            addImage(image, mLastLoaded, true);
        }

        invalidate();
        repaint();
    }

    private void addImage(BufferedImage image, int index, boolean last) {
        ImageItem ii = new ImageItem();
        ii.mIndex = index;
        ii.mImage = image;
        ii.mViewX = (mViewWidth - ii.mImage.getWidth()) / 2;
        if (last) {
            mImageList.addLast(ii);
        } else {
            mImageList.addFirst(ii);
            mViewY += image.getHeight();
        }
        mTotalImageHeight += ii.mImage.getHeight();
        removeImage(!last);
    }

    private void removeImage(boolean last) {
        if (mImageList.size() > 5) {
            ImageItem ii;
            if (last) {
                ii = mImageList.removeLast();
            } else {
                ii = mImageList.removeFirst();
                mViewY -= ii.mImage.getHeight();
            }
            mTotalImageHeight -= ii.mImage.getHeight();
            ii.mImage = null;
            System.gc();
        }
    }

    private BufferedImage getCurrentImage() {
        if (!mImageList.isEmpty()) {
            int y = 0;
            Rectangle imageRect = new Rectangle();
            for (ImageItem ii : mImageList) {
                imageRect.setBounds(0, y, ii.mImage.getWidth(), ii.mImage.getHeight());
                if (imageRect.contains(mSelectedRect.x, mSelectedRect.y + mViewY)) {
                    ocrRect.x = mSelectedRect.x - ii.mViewX;
                    ocrRect.y = mSelectedRect.y + (mViewY - y);
                    ocrRect.setSize(mSelectedRect.width, mSelectedRect.height);
                    return ii.mImage;
                }
                y += ii.mImage.getHeight();
            }
        }
        return null;
    }
}
