package ru.gr0946x.ui.menu;

import javax.swing.*;
import java.awt.*;

/**
 * Главное окно приложения.
 * Пункт 4: Добавлено основное меню.
 */
public class task_4 extends JFrame {

    public task_4() {
        setTitle("Фрактал Множество Мандельброта");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        // Временная заглушка для панели (чтобы окно имело содержимое)
        JPanel fractalPanel = new JPanel();
        fractalPanel.setBackground(Color.BLACK);
        fractalPanel.add(new JLabel("Здесь будет отображаться фрактал", SwingConstants.CENTER));
        add(fractalPanel, BorderLayout.CENTER);

        createMenuBar();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu viewMenu = createViewMenu();
        JMenu animMenu = createAnimationMenu();
        JMenu helpMenu = createHelpMenu();

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(animMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
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

        return fileMenu;
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Правка");

        JMenuItem undoItem = new JMenuItem("Отменить");
        undoItem.addActionListener(e -> showMessage("Отмена действия (100 шагов)"));
        editMenu.add(undoItem);

        return editMenu;
    }

    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem resetZoomItem = new JMenuItem("Сбросить масштаб");
        resetZoomItem.addActionListener(e -> showMessage("Сброс масштаба"));

        JMenuItem juliaItem = new JMenuItem("Множество Жюлиа");
        juliaItem.addActionListener(e -> showMessage("Открытие окна Жюлиа"));

        viewMenu.add(resetZoomItem);
        viewMenu.add(juliaItem);

        return viewMenu;
    }

    private JMenu createAnimationMenu() {
        JMenu animMenu = new JMenu("Экскурсия");

        JMenuItem createVideoItem = new JMenuItem("Создать видео");
        createVideoItem.addActionListener(e -> showMessage("Создание видео"));
        animMenu.add(createVideoItem);

        return animMenu;
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Справка");

        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Фрактал Множество Мандельброта\nЛабораторная работа №3\nГрупповой проект\n\nПункт 4: Главное меню",
                        "О программе",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        helpMenu.add(aboutItem);

        return helpMenu;
    }

    private void showMessage(String text) {
        JOptionPane.showMessageDialog(this,
                text + "\n(Будет реализовано позже)",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Точка входа для тестирования
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            task_4 frame = new task_4();
            frame.setVisible(true);
        });
    }
}