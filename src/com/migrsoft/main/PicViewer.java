package com.migrsoft.main;

import com.migrsoft.image.PicWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.zip.ZipFile;

public class PicViewer extends JPanel implements MouseWheelListener {

    private final int SPACING = 10;
    private BufferedImage mImage;
    private int mViewX, mViewY, mViewWidth, mViewHeight;
    private int mX;
    private Font mFont;

    PicViewer() {
        addMouseWheelListener(this);
        mFont = new Font("SansSerif", Font.BOLD, 20);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        int mScrollAmount = 10;
        int amount = evt.getUnitsToScroll() * mScrollAmount;
        mViewY += amount;
        mViewY = Math.max(0, Math.min(mViewY, mImage.getHeight() - mViewHeight));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (mImage != null) {
            g2.drawImage(mImage,
                    mX, SPACING / 2, mViewWidth, mViewHeight,
                    mViewX, mViewY, mViewX + mViewWidth, mViewY + mViewHeight,
                    null);

            String txt = mImage.getWidth() + " x " + mImage.getHeight();
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D bounds = mFont.getStringBounds(txt, frc);
            Rectangle2D area = new Rectangle2D.Double(0, 0, bounds.getWidth() + 4, bounds.getHeight() + 4);

            g2.setPaint(Color.LIGHT_GRAY);
            g2.fill(area);

            g2.setFont(mFont);
            g2.setPaint(Color.BLUE);
            g2.drawString(txt, 2, -(int) bounds.getY() + 2);
        }
    }

    public interface ActListener {
        void loadPrevItem();
        void loadNextItem();
    }

    private ActListener mActListener = null;

    public void setActListener(ActListener listener) {
        mActListener = listener;
    }

    public void load(ZipFile zip, String name) {
        mImage = PicWorker.load(zip, name, MainParam.getInstance());
        if (mImage != null) {
            calcPosition();
            revalidate();
            repaint();
        }
    }

    private void calcPosition() {
        mViewX = 0;
        mViewY = 0;
        mViewWidth = getWidth() - SPACING;
        mViewHeight = getHeight() - SPACING;
        mX = (getWidth() - mImage.getWidth()) / 2;
    }
}
