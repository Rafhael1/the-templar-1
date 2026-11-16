package br.com.thecruzader.gameplay;

public final class Arrow {
    enum Kind {
        ARCHER(360, 3), PLAYER(620, 1), CAPTAIN(940, 1);

        final double speed, lifetime;

        Kind(double speed, double lifetime) {
            this.speed = speed;
            this.lifetime = lifetime;
        }
    }

    double x, y;
    final double vx, vy;
    final Kind kind;
    double life;

    Arrow(double x, double y, double targetX, double targetY) {
        this(x, y, targetX, targetY, Kind.ARCHER);
    }

    Arrow(double x, double y, double targetX, double targetY, Kind kind) {
        this.x = x;
        this.y = y;
        this.kind = kind;
        double dx = targetX - x, dy = targetY - y;
        double length = Math.hypot(dx, dy);
        if (length == 0) throw new IllegalArgumentException("Arrow requires a direction");
        vx = dx / length * kind.speed;
        vy = dy / length * kind.speed;
        life = kind.lifetime;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double angle() { return Math.toDegrees(Math.atan2(vy, vx)); }

    public boolean heavy() {
        return kind == Kind.CAPTAIN;
    }
}
