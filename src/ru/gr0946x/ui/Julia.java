package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Julia extends JDialog {

    public Julia(Frame owner, double cReal, double cImag) {
        super(owner, "Множество Жюлиа", true);
        setSize(500, 500);
        setLocationRelativeTo(owner);
        add(new JuliaPanel(cReal, cImag, 500, 500));
        pack();
    }

    private static class JuliaPanel extends JPanel {
        private final BufferedImage image;

        public JuliaPanel(double cReal, double cImag, int w, int h) {
            setPreferredSize(new Dimension(w, h));
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    double zx = (x - w/2.0) * 4.0 / w;
                    double zy = (y - h/2.0) * 4.0 / h;

                    int iter = 0;
                    int maxIter = 200;

                    while (zx*zx + zy*zy < 4 && iter < maxIter) {
                        double newZx = zx*zx - zy*zy + cReal;
                        double newZy = 2*zx*zy + cImag;
                        zx = newZx;
                        zy = newZy;
                        iter++;
                    }

                    int color;
                    if (iter == maxIter) {
                        color = 0x000000;
                    } else {
                        int brightness = (iter * 255) / maxIter;
                        color = new Color(brightness, brightness, brightness).getRGB();
                    }

                    image.setRGB(x, y, color);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
    }
}