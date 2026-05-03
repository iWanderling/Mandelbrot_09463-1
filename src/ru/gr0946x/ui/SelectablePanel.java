package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.Painter;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SelectablePanel extends PaintPanel{
    private SelectedRect rect = null;
    private Graphics g;

    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();
    public void addSelectListener(SelectListener listener){
        selectHandlers.add(listener);
    }

    public void removeSelectListener(SelectListener listener){
        selectHandlers.remove(listener);
    }

    public SelectablePanel(Painter painter) {
        super(painter);
        g = getGraphics();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                rect = new SelectedRect(e.getX(), e.getY());
                paintSelectedRect();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                paintSelectedRect();
                if (rect != null) { // Добавьте эту проверку
                    for (var handler : selectHandlers) {
                        handler.onSelect(new Rectangle(
                                rect.getUpperLeft().x,
                                rect.getUpperLeft().y,
                                rect.getWidth(),
                                rect.getHeight()
                        ));
                    }
                    rect = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                paintSelectedRect();
                if (rect != null){
                    rect.setLastPoint(e.getX(), e.getY());
                }
                paintSelectedRect();
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                g = getGraphics();
            }
        });
    }

    private void paintSelectedRect(){
        Graphics g = getGraphics(); // Получаем свежий Graphics каждый раз
        if (g != null){
            g.setXORMode(Color.WHITE);
            g.setColor(Color.BLACK);
            if (rect != null) { // Добавьте проверку
                g.drawRect(
                        rect.getUpperLeft().x,
                        rect.getUpperLeft().y,
                        rect.getWidth(),
                        rect.getHeight()
                );
            }
            g.setPaintMode();
            g.dispose(); // Важно! Освобождаем ресурсы
        }
    }

    public void setPanningMode(boolean b) {
        // Включаем/отключаем выделение при панорамировании
        if (b) {
            // При панорамировании отключаем выделение прямоугольником
            if (rect != null) {
                paintSelectedRect();
                rect = null;
            }
        }
    }
}
