package br.com.thecruzader.gameplay;

public final class Arrow {
    double x, y;
    final double vx, vy;
    double life = 3;

    Arrow(double x, double y, double targetX, double targetY) {
        this.x = x;
        this.y = y;
        double dx = targetX - x, dy = targetY - y;
        double length = Math.hypot(dx, dy);
        if (length == 0) throw new IllegalArgumentException("Arrow requires a direction");
        vx = dx / length * 360;
        vy = dy / length * 360;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double angle() { return Math.toDegrees(Math.atan2(vy, vx)); }
}
