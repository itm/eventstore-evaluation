package de.uniluebeck.itm.tridentcom.eval;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {

    private final Queue<Double> window = new LinkedList<>();
    private final int period;
    private double sum = 0.0;

    public MovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer";
        this.period = period;
    }

    public void add(double num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    public double getAverage() {
        if (window.isEmpty()) {
            return 0d;
        }
        double divisor = window.size();
        return sum / divisor;
    }
}