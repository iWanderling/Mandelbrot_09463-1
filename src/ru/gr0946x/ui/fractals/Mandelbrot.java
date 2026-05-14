package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal{

    private final int maxIterations = 100;
    private final double R2 = 4;
    public double getR(){
        return sqrt(R2);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxIterations(double xMin, double xMax, double yMin, double yMax) {
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;
        double minRange = Math.min(xRange, yRange);
        
        if (minRange <= 0) return maxIterations;
        
        // Исходная область [-2,1] x [-1,1] имеет minRange = 2
        // Хотим чтобы при minRange = 2 было ровно 100 итераций
        double baseLevel = 2.0; // базовый уровень для исходного вида
        double zoomFactor = baseLevel / minRange;
        double dynamicIterations = maxIterations + Math.log(zoomFactor) * 30;
        
        return (int) Math.min(Math.max(dynamicIterations, maxIterations), 1000);
    }

    @Override
    public float inSetProbability(double x, double y) {
        return inSetProbability(x, y, maxIterations);
    }

    public float inSetProbability(double x, double y, int maxIterations) {
        var c = new Complex(x, y);
        var z = new Complex();
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations){
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float)i / maxIterations;
    }
}