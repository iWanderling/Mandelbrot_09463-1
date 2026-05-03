package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.Painter;

public final class ViewAspectRatio {
    private ViewAspectRatio() {}

    public static void fitToPainter(Converter conv, Painter painter, double xMin, double xMax, double yMin, double yMax) {
        int width = painter.getWidth();
        int height = painter.getHeight();
        if (width <= 0 || height <= 0) return;

        double xCenter = (xMin + xMax) / 2;
        double yCenter = (yMin + yMax) / 2;
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        double panelAspect = (double) width / height;
        double viewAspect = xRange / yRange;

        if (viewAspect < panelAspect) {
            xRange = yRange * panelAspect;
        } else {
            yRange = xRange / panelAspect;
        }

        conv.setXShape(xCenter - xRange / 2, xCenter + xRange / 2);
        conv.setYShape(yCenter - yRange / 2, yCenter + yRange / 2);
    }
}
