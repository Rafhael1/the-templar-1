package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

/**
 * A ballistic pot becomes a bounded patch of fire only when it lands on a floor.
 */
public final class OilPot {
    static final double GRAVITY = 700, FLIGHT = .9, RADIUS = 5;
    double x, y, vx, vy, life = 3, fireLeft, fireWidth;
    boolean burning;

    OilPot(double x, double y, double targetX, double targetY) {
        this.x = x;
        this.y = y;
        vx = (targetX - x) / FLIGHT;
        vy = (targetY + RADIUS - y) / FLIGHT + GRAVITY * FLIGHT / 2;
    }

    static boolean clearArc(double x, double y, double targetX, double targetY,
                            CollisionWorld collision, boolean gateLocked) {
        OilPot pot = new OilPot(x, y, targetX, targetY);
        for (double t = GameWorld.STEP; t < FLIGHT - .03; t += GameWorld.STEP) {
            double px = x + pot.vx * t, py = y + pot.vy * t - GRAVITY * t * t / 2;
            for (Bounds solid : collision.solids(gateLocked))
                if (solid.overlaps(px - RADIUS, py - RADIUS, RADIUS * 2, RADIUS * 2)) return false;
        }
        return true;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public boolean burning() {
        return burning;
    }

    public double velocityX() {
        return vx;
    }

    public double velocityY() {
        return vy;
    }

    public double life() {
        return life;
    }

    public Bounds fireBounds() {
        return new Bounds(fireLeft, y, fireWidth, 18);
    }
}
