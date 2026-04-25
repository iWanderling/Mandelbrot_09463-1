package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
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
    private final Deque<ViewPortState> undoHistory = new ArrayDeque<>();
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(mandelbrot, conv, (value)->{
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
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
        configureUndoAction();
        setContent();
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
