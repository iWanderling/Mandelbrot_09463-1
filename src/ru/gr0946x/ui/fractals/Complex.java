package ru.gr0946x.ui.fractals;

public record Complex(double re, double im) {
    public Complex add(Complex other) {
        return new Complex(re + other.re, im + other.im);
    }

    public Complex square() {
        return new Complex(re * re - im * im, 2 * re * im);
    }

    public double absSq() {
        return re * re + im * im;
    }
}