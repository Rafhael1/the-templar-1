package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * One recoverable arrow, falling until it reaches a solid surface.
 */
public final class ArrowPickup {
    private final double x;
    private double y, vy;

    ArrowPickup(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void update(double dt, List<Bounds> solids) {
        double previousY = y;
        vy = Math.max(-700, vy - 1450 * dt);
        y += vy * dt;
        for (Bounds solid : solids) {
            if (x < solid.right() && x + 16 > solid.x()
                && previousY >= solid.top() && y <= solid.top()) {
                y = Math.max(y, solid.top());
                vy = 0;
            }
        }
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public Bounds bounds() {
        return new Bounds(x, y, 16, 12);
    }
}
