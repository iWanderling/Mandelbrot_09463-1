package ru.gr0946x.ui.fractals;

import java.awt.Color;

public class ColorSchemes {

    // Классика: Чёрный внутри, белый снаружи
    public static final ColorFunction BLACK_WHITE = (v) ->
            (v >= 1.0f) ? Color.BLACK : Color.WHITE;

    // Плавный синий градиент (используем HSB: оттенок 0.6 - это синий)
    public static final ColorFunction ELECTRIC_BLUE = (v) -> {
        if (v >= 1.0f) return Color.BLACK;
        return Color.getHSBColor(0.6f, 0.8f, v);
    };

    // "Огненная" схема
    public static final ColorFunction FIRE = (v) -> {
        if (v >= 1.0f) return Color.BLACK;
        // Переход от красного к желтому
        return Color.getHSBColor(0.12f * v, 1.0f, 1.0f);
    };
}