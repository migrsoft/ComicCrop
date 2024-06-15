package com.migrsoft.main;

import com.migrsoft.image.PicWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.zip.ZipFile;

public class PicViewer extends JPanel implements MouseWheelListener {

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

    PicViewer() {
        addMouseWheelListener(this);
        mFont = new Font("SansSerif", Font.BOLD, 20);
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

        Rectangle2D area = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
        g2.setPaint(Color.DARK_GRAY);
        g2.fill(area);

        if (!mImageList.isEmpty()) {
            ListIterator<ImageItem> it = mImageList.listIterator();
            int y = 0;
            int dx1, dx2, dy1 = 0, dy2;
            int sx1, sx2, sy1, sy2;
            boolean first = true;
            String hintSize = "? x ?";
            while (it.hasNext() && y < mViewY + mViewHeight) {
                ImageItem ii = it.next();
                Rectangle2D image = new Rectangle2D.Double(0, y, ii.mImage.getWidth(), ii.mImage.getHeight());
                Rectangle2D view = new Rectangle2D.Double(0, mViewY, mViewWidth, mViewHeight);
                if (image.intersects(view)) {
                    Rectangle2D isr = new Rectangle2D.Double();
                    Rectangle2D.intersect(image, view, isr);
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
                    sy1 = (int)isr.getY() - y;
                    sy2 = sy1 + (int)isr.getHeight();
                    g2.drawImage(ii.mImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    dy1 += (int)isr.getHeight();
                }
                y += ii.mImage.getHeight();
            }

            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D bounds = mFont.getStringBounds(hintSize, frc);
            g2.setFont(mFont);
            g2.setPaint(Color.LIGHT_GRAY);
            g2.drawString(hintSize, 2, -(int) bounds.getY() + 2);
        }
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
//        System.out.println("add image -> index:" + index + " imageH:" + image.getHeight());
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
}
