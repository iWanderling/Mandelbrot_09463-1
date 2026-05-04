package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.painting.FractalPainter;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VideoExporter {

    /**
     * Экспортирует анимацию перехода между ключевыми кадрами в GIF-файл.
     * @param parent         родительское окно для диалога сохранения
     * @param keyFrames      список кадров (минимум 2)
     * @param totalSeconds   общая длительность видео в секундах
     * @param width          ширина выходного видео (пиксели)
     * @param height         высота выходного видео
     * @param fractal        объект фрактала (Mandelbrot)
     * @param colorFunc      функция раскраски
     */
    public static void export(JFrame parent,
                              List<AnimationWindow.KeyFrame> keyFrames,
                              int totalSeconds,
                              int width,
                              int height,
                              Fractal fractal,
                              ColorFunction colorFunc) {
        // Диалог выбора файла
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить видео (GIF)");
        chooser.setFileFilter(new FileNameExtensionFilter("Анимированный GIF (*.gif)", "gif"));
        chooser.setSelectedFile(new File("fractal_animation.gif"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = ensureExtension(chooser.getSelectedFile(), "gif");

        // Рассчитываем кадры
        int fps = 15;
        int totalFrames = totalSeconds * fps;
        int segments = keyFrames.size() - 1;
        int framesPerSegment = Math.max(1, totalFrames / segments);

        // Подготовка писателя GIF
        try (ImageOutputStream output = new FileImageOutputStream(file)) {
            GifSequenceWriter gifWriter = new GifSequenceWriter(output,
                    BufferedImage.TYPE_INT_RGB, 1000 / fps, true);

            for (int seg = 0; seg < segments; seg++) {
                AnimationWindow.KeyFrame from = keyFrames.get(seg);
                AnimationWindow.KeyFrame to = keyFrames.get(seg + 1);
                for (int f = 0; f <= framesPerSegment; f++) {
                    double t = (double) f / framesPerSegment;
                    double xMin = lerp(from.xMin(), to.xMin(), t);
                    double xMax = lerp(from.xMax(), to.xMax(), t);
                    double yMin = lerp(from.yMin(), to.yMin(), t);
                    double yMax = lerp(from.yMax(), to.yMax(), t);

                    BufferedImage frame = renderFrame(fractal, colorFunc,
                            width, height, xMin, xMax, yMin, yMax);
                    gifWriter.writeToSequence(frame);
                }
            }
            gifWriter.close();
            JOptionPane.showMessageDialog(parent,
                    "Видео сохранено:\n" + file.getAbsolutePath(),
                    "Экспорт завершён", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Ошибка при сохранении видео:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Рендерит один кадр фрактала с заданными координатами. */
    private static BufferedImage renderFrame(Fractal fractal,
                                             ColorFunction colorFunc,
                                             int w, int h,
                                             double xMin, double xMax,
                                             double yMin, double yMax) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Converter tempConv = new Converter(xMin, xMax, yMin, yMax);
        FractalPainter tempPainter = new FractalPainter(fractal, tempConv, colorFunc);
        tempPainter.setWidth(w);
        tempPainter.setHeight(h);
        tempPainter.paint(g);
        g.dispose();
        return img;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static File ensureExtension(File f, String ext) {
        String path = f.getAbsolutePath().toLowerCase();
        if (!path.endsWith("." + ext)) {
            return new File(f.getAbsolutePath() + "." + ext);
        }
        return f;
    }

    /**
     * Простой писатель анимированного GIF (без внешних библиотек).
     * Требует Java 9+.
     */
    private static class GifSequenceWriter implements AutoCloseable {
        private final ImageWriter gifWriter;
        private final ImageWriteParam imageWriteParam;
        private final IIOMetadata imageMetaData;
        private final ImageOutputStream output;

        public GifSequenceWriter(ImageOutputStream output,
                                 int imageType,
                                 int timeBetweenFramesMS,
                                 boolean loopContinuously) throws IOException {
            this.output = output;
            gifWriter = ImageIO.getImageWritersBySuffix("gif").next();
            imageWriteParam = gifWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier =
                    ImageTypeSpecifier.createFromBufferedImageType(imageType);
            imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                    imageWriteParam);

            String metaFormatName = imageMetaData.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime",
                    Integer.toString(timeBetweenFramesMS / 10));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
            commentsNode.setAttribute("CommentExtension", "Created by MAH");

            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");

            int loop = loopContinuously ? 0 : 1;
            child.setUserObject(new byte[]{ 0x1, (byte)(loop & 0xFF), (byte)(0)});
            appExtensionsNode.appendChild(child);

            imageMetaData.setFromTree(metaFormatName, root);
            gifWriter.setOutput(output);
            gifWriter.prepareWriteSequence(null);
        }

        public void writeToSequence(BufferedImage img) throws IOException {
            gifWriter.writeToSequence(
                    new IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        @Override
        public void close() throws IOException {
            gifWriter.endWriteSequence();
        }

        private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
            int n = root.getLength();
            for (int i = 0; i < n; i++) {
                if (root.item(i).getNodeName().equalsIgnoreCase(nodeName))
                    return (IIOMetadataNode) root.item(i);
            }
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            root.appendChild(node);
            return node;
        }
    }
}