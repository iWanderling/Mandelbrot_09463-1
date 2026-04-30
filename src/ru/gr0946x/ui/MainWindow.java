package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.animation.AnimationWindow;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private ColorFunction defaultColorFunction;

    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);

        defaultColorFunction = (value) -> {
            if (value == 1.0) return Color.BLACK;
            var r = (float) abs(sin(5 * value));
            var g = (float) abs(cos(8 * value) * sin(3 * value));
            var b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        };

        painter = new FractalPainter(mandelbrot, conv, defaultColorFunction);
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.addSelectListener((r)->{
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });
        setContent();

        // ★★★★★ ДОБАВЛЯЕМ ВАШЕ МЕНЮ (ПУНКТ 4) ★★★★★
        createMenuBar();
    }

    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }

    /**
     * Создаёт главное меню (пункт 4 лабораторной работы)
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ========== Меню "Файл" ==========
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveFracItem = new JMenuItem("Сохранить как .frac");
        saveFracItem.addActionListener(e -> showMessage("Сохранение .frac"));

        JMenuItem saveJpgItem = new JMenuItem("Сохранить как JPG");
        saveJpgItem.addActionListener(e -> showMessage("Сохранение JPG"));

        JMenuItem savePngItem = new JMenuItem("Сохранить как PNG");
        savePngItem.addActionListener(e -> showMessage("Сохранение PNG"));

        fileMenu.add(saveFracItem);
        fileMenu.add(saveJpgItem);
        fileMenu.add(savePngItem);
        fileMenu.addSeparator();

        JMenuItem openItem = new JMenuItem("Открыть .frac");
        openItem.addActionListener(e -> showMessage("Открытие .frac"));
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // ========== Меню "Правка" ==========
        JMenu editMenu = new JMenu("Правка");

        JMenuItem undoItem = new JMenuItem("Отменить");
        undoItem.addActionListener(e -> showMessage("Отмена действия (100 шагов)"));
        editMenu.add(undoItem);

        // ========== Меню "Вид" ==========
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem resetZoomItem = new JMenuItem("Сбросить масштаб");
        resetZoomItem.addActionListener(e -> showMessage("Сброс масштаба"));

        JMenuItem juliaItem = new JMenuItem("Множество Жюлиа");
        juliaItem.addActionListener(e -> showMessage("Открытие окна Жюлиа"));

        viewMenu.add(resetZoomItem);
        viewMenu.add(juliaItem);

        // ========== Меню "Экскурсия" (пункт 11*) ==========
        JMenu animMenu = new JMenu("Экскурсия");

        JMenuItem animationSetupItem = new JMenuItem("Настройка анимации...");
        animationSetupItem.addActionListener(e -> {
            AnimationWindow animWin = new AnimationWindow(mandelbrot, defaultColorFunction);
            animWin.setVisible(true);
        });
        animMenu.add(animationSetupItem);

        // ========== Меню "Справка" ==========
        JMenu helpMenu = new JMenu("Справка");

        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Фрактал Множество Мандельброта\nЛабораторная работа №3\nГрупповой проект\n\nПункт 4: Главное меню",
                        "О программе",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        helpMenu.add(aboutItem);

        // Собираем всё в строку меню
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(animMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Вспомогательный метод для отображения сообщений-заглушек
     */
    private void showMessage(String text) {
        JOptionPane.showMessageDialog(this,
                text + "\n(Будет реализовано позже)",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
    }
}