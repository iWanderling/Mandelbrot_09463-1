package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FractalPainter implements Painter {

    private final Fractal fractal;
    private final Converter conv;
    private final ColorFunction colorFunction;

    // Число потоков = числу логических ядер CPU
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf) {
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
    }

    // ─── Делегируем размеры в Converter ───────────────────────────────────────

    @Override public int getWidth()            { return conv.getWidth(); }
    @Override public int getHeight()           { return conv.getHeight(); }
    @Override public void setWidth(int width)  { conv.setWidth(width); }
    @Override public void setHeight(int height){ conv.setHeight(height); }

    // ─── Многопоточная отрисовка ───────────────────────────────────────────────

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        if (w <= 0 || h <= 0) return;

        // Создаём изображение, в которое потоки будут писать пиксели
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Барьер: ждём завершения всех THREAD_COUNT задач
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        int stripHeight = h / THREAD_COUNT;

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int startY = t * stripHeight;
            // Последний поток забирает остаток строк
            final int endY = (t == THREAD_COUNT - 1) ? h : startY + stripHeight;

            executor.submit(() -> {
                try {
                    renderStrip(image, w, startY, endY);
                } finally {
                    latch.countDown(); // обязательно, даже при исключении
                }
            });
        }

        // Главный поток (EDT) ждёт завершения всех полос
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        // Один вызов drawImage вместо миллиона drawLine — быстрее
        g.drawImage(image, 0, 0, null);
    }

    // ─── Отрисовка одной горизонтальной полосы ────────────────────────────────

    /**
     * Каждый поток работает со своим диапазоном строк [startY, endY).
     * Пересечений нет → дополнительная синхронизация не нужна.
     */
    private void renderStrip(BufferedImage image, int w, int startY, int endY) {
        for (int j = startY; j < endY; j++) {
            for (int i = 0; i < w; i++) {
                double x = conv.xScr2Crt(i);
                double y = conv.yScr2Crt(j);
                float res = fractal.inSetProbability(x, y);
                Color color = colorFunction.getColor(res);
                image.setRGB(i, j, color.getRGB());
            }
        }
    }
}
