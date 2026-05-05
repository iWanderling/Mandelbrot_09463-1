package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.SelectablePanel;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;
import ru.gr0946x.ui.interaction.PanHandler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimationWindow extends JFrame {

    private static final int MAX_KEY_FRAMES = 50;

    private final SelectablePanel mainPanel;
    private final Fractal mandelbrot;
    private final Converter conv;

    private final DefaultListModel<KeyFrame> listModel;
    private final JList<KeyFrame> framesList;
    private final JButton btnAddFrame;
    private final JButton btnRemoveFrame;
    private final JButton btnExportVideo;

    private final JSlider durationSlider;
    private final JLabel durationLabel;

    public AnimationWindow(Fractal fractal, ColorFunction colorChooser) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 650));

        mandelbrot = fractal;
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        Painter painter = new FractalPainter(mandelbrot, conv, colorChooser);

        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        PanHandler panHandler = new PanHandler(mainPanel, painter, conv);
        mainPanel.addMouseListener(panHandler);
        mainPanel.addMouseMotionListener(panHandler);
        mainPanel.addMouseWheelListener(panHandler);

        // Список ключевых кадров
        listModel = new DefaultListModel<>();
        framesList = new JList<>(listModel);
        framesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnAddFrame = new JButton("+");
        btnAddFrame.addActionListener(_ -> {
            if (listModel.getSize() == MAX_KEY_FRAMES) {
                JOptionPane.showMessageDialog(this,
                        "Достигнут лимит количества кадров (" + MAX_KEY_FRAMES + ").");
                return;
            }
            KeyFrame frame = new KeyFrame(
                    conv.getXMin(), conv.getXMax(),
                    conv.getYMin(), conv.getYMax()
            );
            if (!listContains(frame)) {
                listModel.addElement(frame);
            }
        });

        btnRemoveFrame = new JButton("-");
        btnRemoveFrame.addActionListener(_ -> {
            int idx = framesList.getSelectedIndex();
            if (idx != -1) listModel.remove(idx);
        });

        durationSlider = new JSlider(5, 15, 10);
        durationSlider.setMajorTickSpacing(5);
        durationSlider.setPaintTicks(true);
        durationSlider.setPaintLabels(true);
        durationLabel = new JLabel("Длительность: 10 сек");
        durationSlider.addChangeListener(_ ->
                durationLabel.setText("Длительность: " + durationSlider.getValue() + " сек"));

        btnExportVideo = new JButton("Сохранить видео...");
        btnExportVideo.setEnabled(false);
        listModel.addListDataListener(new javax.swing.event.ListDataListener() {
            private void update() { btnExportVideo.setEnabled(listModel.getSize() > 1); }
            public void intervalAdded(javax.swing.event.ListDataEvent e) { update(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { update(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { update(); }
        });
        btnExportVideo.addActionListener(_ -> {
            List<KeyFrame> frames = new ArrayList<>();
            for (int i = 0; i < listModel.getSize(); i++) frames.add(listModel.get(i));
            VideoExporter.export(this, frames, durationSlider.getValue(),
                    mainPanel.getWidth(), mainPanel.getHeight(),
                    mandelbrot, colorChooser);
        });

        // Выделение области мышью – навигация по фракталу
        mainPanel.addSelectListener(r -> {
            if (r.width <= 0 || r.height <= 0) return;
            double xMin = conv.xScr2Crt(r.x);
            double xMax = conv.xScr2Crt(r.x + r.width);
            double yMin = conv.yScr2Crt(r.y + r.height);
            double yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });

        buildLayout();
    }

    private boolean listContains(KeyFrame frame) {
        for (int i = 0; i < listModel.getSize(); i++) {
            if (listModel.get(i).equals(frame)) return true;
        }
        return false;
    }

    private void buildLayout() {
        JScrollPane scrollPane = new JScrollPane(framesList);
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        btnPanel.add(btnAddFrame);
        btnPanel.add(btnRemoveFrame);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(scrollPane);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(btnPanel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(btnExportVideo);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(durationLabel);
        rightPanel.add(durationSlider);
        rightPanel.setPreferredSize(new Dimension(220, 0));

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout(8, 8));
        cp.add(mainPanel, BorderLayout.CENTER);
        cp.add(rightPanel, BorderLayout.EAST);
    }

    /** Ключевой кадр хранит координаты области просмотра. */
    public record KeyFrame(double xMin, double xMax, double yMin, double yMax) {}
}