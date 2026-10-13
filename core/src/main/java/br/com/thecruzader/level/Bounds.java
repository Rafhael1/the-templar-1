package br.com.thecruzader.level;

/** Immutable, axis-aligned world rectangle. Coordinates grow upwards. */
public record Bounds(double x, double y, double width, double height) {
    public Bounds {
        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 0
                || height <= 0) {
            throw new IllegalArgumentException("Bounds must be finite and have positive size");
        }
    }

    public double right() {
        return x + width;
    }

    public double top() {
        return y + height;
    }

    public boolean overlaps(Bounds other) {
        return x < other.right() && right() > other.x && y < other.top() && top() > other.y;
    }
}
