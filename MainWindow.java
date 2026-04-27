package ru.gr0946x.ui;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import ru.gr0946x.ui.interaction.PanHandler;


import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private static final int UNDO_LIMIT = 100;
    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private final FileManager fileManager;

    private final Deque<ViewPortState> undoHistory = new ArrayDeque<>();
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));

        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(mandelbrot, conv, (value)->{
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin(3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });

        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);
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

        PanHandler panHandler = new PanHandler(mainPanel, painter, conv);
        mainPanel.addMouseListener(panHandler);
        mainPanel.addMouseMotionListener(panHandler);
        mainPanel.addMouseWheelListener(panHandler);

        fileManager = new FileManager(this, painter, conv, (Mandelbrot)mandelbrot, mainPanel);

        configureUndoAction();
        setContent();
        createMenu();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem openItem = new JMenuItem("Открыть");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        fileMenu.addSeparator();

        // Пункт 5а
        JMenuItem saveFracItem = new JMenuItem("Сохранить как .frac");
        saveFracItem.addActionListener(e -> saveFracFile());
        fileMenu.add(saveFracItem);

        // Пункт 5б
        JMenuItem saveJpgItem = new JMenuItem("Сохранить как JPG");
        saveJpgItem.addActionListener(e -> saveImageFile("jpg"));
        fileMenu.add(saveJpgItem);

        // Пункт 5в
        JMenuItem savePngItem = new JMenuItem("Сохранить как PNG");
        savePngItem.addActionListener(e -> saveImageFile("png"));
        fileMenu.add(savePngItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void saveFracFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить фрактал (.frac)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Фракталы (*.frac)", "frac"));
        fileChooser.setSelectedFile(new File("fractal.frac"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".frac")) {
                file = new File(path + ".frac");
            }

            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                dos.writeDouble(conv.xScr2Crt(0));
                dos.writeDouble(conv.xScr2Crt(mainPanel.getWidth()));
                dos.writeDouble(conv.yScr2Crt(mainPanel.getHeight()));
                dos.writeDouble(conv.yScr2Crt(0));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
            }
        }
    }

    private void saveImageFile(String format) {
        JFileChooser fileChooser = new JFileChooser();
        String upperFormat = format.toUpperCase();
        fileChooser.setDialogTitle("Сохранить как " + upperFormat);
        fileChooser.setFileFilter(new FileNameExtensionFilter(upperFormat + " (*." + format + ")", format));
        fileChooser.setSelectedFile(new File("fractal." + format));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith("." + format)) {
                file = new File(path + "." + format);
            }

            // Создаём картинку и рисуем на ней фрактал
            BufferedImage image = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight() + 30, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            painter.paint(g2d);

            // Подпись внизу
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, mainPanel.getHeight(), image.getWidth(), 30);
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.format("X:[%.6f,%.6f] Y:[%.6f,%.6f]",
                            conv.xScr2Crt(0), conv.xScr2Crt(mainPanel.getWidth()),
                            conv.yScr2Crt(mainPanel.getHeight()), conv.yScr2Crt(0)),
                    10, mainPanel.getHeight() + 20);
            g2d.dispose();

            try {
                ImageIO.write(image, format, file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + e.getMessage());
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Открыть фрактал");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Фракталы (*.frac)", "frac"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                double savedXMin = dis.readDouble();
                double savedXMax = dis.readDouble();
                double savedYMin = dis.readDouble();
                double savedYMax = dis.readDouble();

                conv.setXShape(savedXMin, savedXMax);
                conv.setYShape(savedYMin, savedYMax);

                mainPanel.repaint();

                JOptionPane.showMessageDialog(this, "Файл успешно открыт", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка при открытии файла: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveStateForUndo() {
        if (undoHistory.size() == UNDO_LIMIT) {
            undoHistory.removeFirst();
        }
        undoHistory.addLast(new ViewPortState(
                conv.xScr2Crt(0),
                conv.xScr2Crt(painter.getWidth()),
                conv.yScr2Crt(painter.getHeight()),
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
}