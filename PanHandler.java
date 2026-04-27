package ru.gr0946x.ui.interaction;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.Painter;
import ru.gr0946x.ui.SelectablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

/**
 * Обработчик сдвига изображения правой кнопкой мыши + зум колесом.
 * Пункт 1 лабораторной работы №3.
 */
public class PanHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final SelectablePanel panel;
    private final Painter painter;
    private final Converter conv;

    private int lastMouseX;
    private int lastMouseY;
    private boolean isPanning = false;
    private Cursor originalCursor;

    // Начальные параметры для сброса зума
    private final double defaultXMin = -2.0;
    private final double defaultXMax = 1.0;
    private final double defaultYMin = -1.0;
    private final double defaultYMax = 1.0;

    public PanHandler(JPanel panel, Painter painter, Converter conv) {
        this.panel = (SelectablePanel) panel;
        this.painter = painter;
        this.conv = conv;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            isPanning = true;

            originalCursor = panel.getCursor();
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            panel.setPanningMode(true);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && isPanning) {
            int deltaX = e.getX() - lastMouseX;
            int deltaY = e.getY() - lastMouseY;

            double xMin = conv.xScr2Crt(0);
            double xMax = conv.xScr2Crt(painter.getWidth());
            double yMin = conv.yScr2Crt(painter.getHeight());
            double yMax = conv.yScr2Crt(0);

            double xRange = xMax - xMin;
            double yRange = yMax - yMin;

            double shiftX = deltaX * xRange / painter.getWidth();
            double shiftY = -deltaY * yRange / painter.getHeight();

            conv.setXShape(xMin + shiftX, xMax + shiftX);
            conv.setYShape(yMin + shiftY, yMax + shiftY);

            lastMouseX = e.getX();
            lastMouseY = e.getY();

            panel.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e) && isPanning) {
            isPanning = false;
            panel.setCursor(originalCursor);
            panel.setPanningMode(false);
        }
    }

    /**
     * Сброс зума при двойном клике левой кнопкой
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            conv.setXShape(defaultXMin, defaultXMax);
            conv.setYShape(defaultYMin, defaultYMax);
            panel.repaint();
        }
    }

    /**
     * Зум колесом мыши
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double zoomFactor;
        if (e.getWheelRotation() < 0) {
            zoomFactor = 0.9;  // Приблизить (колесо вверх)
        } else {
            zoomFactor = 1.1;  // Отдалить (колесо вниз)
        }

        // Зумим к точке под курсором
        double mouseX = conv.xScr2Crt(e.getX());
        double mouseY = conv.yScr2Crt(e.getY());

        double xMin = conv.xScr2Crt(0);
        double xMax = conv.xScr2Crt(painter.getWidth());
        double yMin = conv.yScr2Crt(painter.getHeight());
        double yMax = conv.yScr2Crt(0);

        double newXMin = mouseX + (xMin - mouseX) * zoomFactor;
        double newXMax = mouseX + (xMax - mouseX) * zoomFactor;
        double newYMin = mouseY + (yMin - mouseY) * zoomFactor;
        double newYMax = mouseY + (yMax - mouseY) * zoomFactor;

        conv.setXShape(newXMin, newXMax);
        conv.setYShape(newYMin, newYMax);
        panel.repaint();
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}