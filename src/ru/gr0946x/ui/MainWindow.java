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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private static final int UNDO_LIMIT = 100;
    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private ColorFunction defaultColorFunction;
    private final Deque<ViewPortState> undoHistory = new ArrayDeque<>();
    private FileManager fileManager;

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

        fileManager = new FileManager(this, painter, conv, (Mandelbrot) mandelbrot, mainPanel);

        mainPanel.addSelectListener((r)->{
            if (r.width <= 0 || r.height <= 0) return;
            saveStateForUndo();
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });

        configureUndoAction();
        setContent();
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

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ========== Меню "Файл" ==========
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveFracItem = new JMenuItem("Сохранить как .frac");
        saveFracItem.addActionListener(e -> fileManager.saveFracFile());

        JMenuItem saveJpgItem = new JMenuItem("Сохранить как JPG");
        saveJpgItem.addActionListener(e -> fileManager.saveImageFile("jpg"));

        JMenuItem savePngItem = new JMenuItem("Сохранить как PNG");
        savePngItem.addActionListener(e -> fileManager.saveImageFile("png"));

        fileMenu.add(saveFracItem);
        fileMenu.add(saveJpgItem);
        fileMenu.add(savePngItem);
        fileMenu.addSeparator();

        JMenuItem openItem = new JMenuItem("Открыть .frac");
        openItem.addActionListener(e -> openFracFile());
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // ========== Меню "Правка" ==========
        JMenu editMenu = new JMenu("Правка");

        JMenuItem undoItem = new JMenuItem("Отменить (Ctrl+Z)");
        undoItem.addActionListener(e -> undoLastAction());
        editMenu.add(undoItem);

        // ========== Меню "Вид" ==========
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem resetZoomItem = new JMenuItem("Сбросить масштаб");
        resetZoomItem.addActionListener(e -> {
            saveStateForUndo();
            conv.setXShape(-2.0, 1.0);
            conv.setYShape(-1.0, 1.0);
            mainPanel.repaint();
        });

        JMenuItem juliaItem = new JMenuItem("Множество Жюлиа");
        juliaItem.addActionListener(e -> openJuliaWindow());
        viewMenu.add(resetZoomItem);
        viewMenu.add(juliaItem);

        // ========== Меню "Экскурсия" ==========
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
                        "Фрактал Множество Мандельброта\nЛабораторная работа №3\nГрупповой проект",
                        "О программе",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(animMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    // ==================== СОХРАНЕНИЕ / ОТКРЫТИЕ ====================

    private void openFracFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Открыть фрактал");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Фракталы (*.frac)", "frac"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                String signature = dis.readUTF();
                if (!"MANDELBROT_FRACTAL".equals(signature)) {
                    throw new IOException("Неверный формат файла");
                }
                dis.readInt(); // version

                double xMin = dis.readDouble();
                double xMax = dis.readDouble();
                double yMin = dis.readDouble();
                double yMax = dis.readDouble();
                dis.readInt(); // panel width
                dis.readInt(); // panel height
                dis.readInt(); // iterations
                dis.readInt(); // window width
                dis.readInt(); // window height

                saveStateForUndo();
                conv.setXShape(xMin, xMax);
                conv.setYShape(yMin, yMax);
                mainPanel.repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            }
        }
    }

    // ==================== UNDO ====================

    private void saveStateForUndo() {
        if (undoHistory.size() == UNDO_LIMIT) {
            undoHistory.removeFirst();
        }
        undoHistory.addLast(new ViewPortState(
                conv.xScr2Crt(0),
                conv.xScr2Crt(mainPanel.getWidth()),
                conv.yScr2Crt(mainPanel.getHeight()),
                conv.yScr2Crt(0)
        ));
    }

    private void undoLastAction() {
        if (undoHistory.isEmpty()) return;
        var prev = undoHistory.removeLast();
        conv.setXShape(prev.xMin, prev.xMax);
        conv.setYShape(prev.yMin, prev.yMax);
        mainPanel.repaint();
    }

    private void configureUndoAction() {
        var rootPane = getRootPane();
        var inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = rootPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo-view");
        actionMap.put("undo-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoLastAction();
            }
        });
    }

    private record ViewPortState(double xMin, double xMax, double yMin, double yMax) {}

    // ==================== ЖЮЛИА ====================

    private void openJuliaWindow() {
        // Вычисляем центр текущей области как точку для Жюлиа
        double cx = (conv.xScr2Crt(0) + conv.xScr2Crt(mainPanel.getWidth())) / 2;
        double cy = (conv.yScr2Crt(mainPanel.getHeight()) + conv.yScr2Crt(0)) / 2;
        new Julia(this, cx, cy).setVisible(true);
    }

}