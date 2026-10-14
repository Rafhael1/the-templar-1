package br.com.thecruzader.presentation;

/** A room switch fades in from black; initial load/respawn synchronizes without an old view. */
public final class RoomTransition {
    private static final float DURATION = .30f;
    private boolean interior;
    private float opacity;

    public void reset(boolean inside) { interior = inside; opacity = 0; }
    public boolean interior() { return interior; }
    public float opacity() { return opacity; }
    public void update(boolean inside, float delta, boolean paused) {
        if (paused) return;
        if (inside != interior) {
            interior = inside;
            opacity = 1;
        } else opacity = Math.max(0, opacity - Math.min(delta, .1f) / DURATION);
    }
}
