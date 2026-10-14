package br.com.thecruzader.gameplay;

/** One shared timeline synchronizes angel descent, door visuals and physical passage. */
public final class BossRelease {
    public static final double OPEN_START = 1.8;
    public static final double OPEN_END = 3.2;
    public static final double DURATION = 4.6;
    private BossRelease() {}
    public static float opening(double elapsed) {
        return (float) Math.max(0, Math.min(1, (elapsed - OPEN_START) / (OPEN_END - OPEN_START)));
    }
}
