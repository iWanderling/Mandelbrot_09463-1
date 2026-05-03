package ru.gr0946x.ui.fractals;

import java.awt.Color;

@FunctionalInterface
public interface ColorFunction {
    /**
     * @param value значение от 0.0 до 1.0 (результат работы фрактала)
     * @return объект Color для отрисовки пикселя
     */
    Color getColor(float value);
}