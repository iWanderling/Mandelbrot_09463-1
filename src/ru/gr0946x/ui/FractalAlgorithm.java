package ru.gr0946x.ui;

import ru.gr0946x.ui.fractals.Complex;

@FunctionalInterface
public interface FractalAlgorithm {
    // Принимает текущее Z и константу C, возвращает следующее Z
    Complex calculateNext(Complex z, Complex c);
}