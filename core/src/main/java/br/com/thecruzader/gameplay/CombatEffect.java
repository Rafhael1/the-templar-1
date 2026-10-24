package br.com.thecruzader.gameplay;

/**
 * Short-lived presentation cues emitted by resolved combat, never damage hitboxes.
 */
public final class CombatEffect {
    public enum Kind {MACE_IMPACT, POT_SHATTER, FIRE_IGNITE}

    private final Kind kind;
    private final double x, y;
    private double age;

    CombatEffect(Kind kind, double x, double y) {
        this.kind = kind;
        this.x = x;
        this.y = y;
    }

    void update(double dt) {
        age += dt;
    }

    boolean expired() {
        return age >= .45;
    }

    public Kind kind() {
        return kind;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double age() {
        return age;
    }
}
