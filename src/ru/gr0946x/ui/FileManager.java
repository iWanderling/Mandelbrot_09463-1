package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.Painter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Класс для сохранения фракталов.
 * Реализует пункт 5 лабораторной работы.
 */
public class FileManager {

    private final JFrame parentFrame;
    private final Painter painter;
    private final Converter conv;
    private final Mandelbrot mandelbrot;
    private final JPanel mainPanel;

    private static final String FRAC_SIGNATURE = "MANDELBROT_FRACTAL";
    private static final int FRAC_VERSION = 1;

    public FileManager(JFrame parentFrame, Painter painter, Converter conv,
                       Mandelbrot mandelbrot, JPanel mainPanel) {
        this.parentFrame = parentFrame;
        this.painter = painter;
        this.conv = conv;
        this.mandelbrot = mandelbrot;
        this.mainPanel = mainPanel;
    }

    /**
     * Сохранение в формате .frac (пункт 5а)
     */
    public void saveFracFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить фрактал (.frac)");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Фракталы (*.frac)", "frac"));
        fileChooser.setSelectedFile(new File("fractal.frac"));

        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), "frac");

            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)))) {

                // Сохраняем сигнатуру и версию
                dos.writeUTF(FRAC_SIGNATURE);
                dos.writeInt(FRAC_VERSION);

                // Параметры области просмотра
                dos.writeDouble(conv.xScr2Crt(0));
                dos.writeDouble(conv.xScr2Crt(mainPanel.getWidth()));
                dos.writeDouble(conv.yScr2Crt(mainPanel.getHeight()));
                dos.writeDouble(conv.yScr2Crt(0));

                // Размеры панели и параметры фрактала
                dos.writeInt(mainPanel.getWidth());
                dos.writeInt(mainPanel.getHeight());
                dos.writeInt(mandelbrot.getMaxIterations());

                // Размеры окна
                dos.writeInt(parentFrame.getWidth());
                dos.writeInt(parentFrame.getHeight());

                JOptionPane.showMessageDialog(parentFrame,
                        "Файл сохранён:\n" + file.getAbsolutePath(),
                        "Сохранение .frac", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parentFrame,
                        "Ошибка сохранения:\n" + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Сохранение в JPG/PNG с подписью координат (пункты 5б, 5в)
     */
    public void saveImageFile(String format) {
        JFileChooser fileChooser = new JFileChooser();
        String upperFormat = format.toUpperCase();
        fileChooser.setDialogTitle("Сохранить как " + upperFormat);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                upperFormat + " (*." + format + ")", format));
        fileChooser.setSelectedFile(new File("fractal." + format));

        if (fileChooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = ensureExtension(fileChooser.getSelectedFile(), format);
            saveImageToFile(file, format);
        }
    }

    private void saveImageToFile(File file, String format) {
        int captionHeight = 40;
        BufferedImage image = new BufferedImage(
                mainPanel.getWidth(),
                mainPanel.getHeight() + captionHeight,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем фрактал
        Graphics originalGraphics = g2d.create(0, 0,
                mainPanel.getWidth(), mainPanel.getHeight());
        painter.paint(originalGraphics);
        originalGraphics.dispose();

        // Чёрная полоса для подписи
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, mainPanel.getHeight(), image.getWidth(), captionHeight);

        // Подпись с координатами
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

        String coords = String.format(
                "X: [%.6f ... %.6f]  Y: [%.6f ... %.6f]  Iter: %d",
                conv.xScr2Crt(0), conv.xScr2Crt(mainPanel.getWidth()),
                conv.yScr2Crt(mainPanel.getHeight()), conv.yScr2Crt(0),
                mandelbrot.getMaxIterations());

        FontMetrics fm = g2d.getFontMetrics();
        int textX = (image.getWidth() - fm.stringWidth(coords)) / 2;
        int textY = mainPanel.getHeight() + (captionHeight + fm.getAscent()) / 2;
        g2d.drawString(coords, textX, textY);
        g2d.dispose();

        try {
            ImageIO.write(image, format, file);
            JOptionPane.showMessageDialog(parentFrame,
                    "Изображение сохранено:\n" + file.getAbsolutePath(),
                    "Сохранение", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Ошибка сохранения:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Автоматическое добавление/исправление расширения файла
     */
    private File ensureExtension(File file, String requiredExtension) {
        String path = file.getAbsolutePath();
        String lowerPath = path.toLowerCase();
        String lowerExt = "." + requiredExtension.toLowerCase();

        if (!lowerPath.endsWith(lowerExt)) {
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex > path.lastIndexOf(File.separator)) {
                path = path.substring(0, dotIndex);
            }
            return new File(path + lowerExt);
        }
        return file;
    }
}