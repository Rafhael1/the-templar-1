package br.com.thecruzader.gameplay;

/** One attack timeline, shared by player and enemy rules. */
public final class Attack {
    public enum Phase {
        READY,
        WINDUP,
        ACTIVE,
        RECOVERY
    }

    private final double windup, active, recovery;
    private double remaining;
    private long sequence;

    Attack(double windup, double active, double recovery) {
        if (windup <= 0 || active <= 0 || recovery <= 0)
            throw new IllegalArgumentException("Attack durations must be positive");
        this.windup = windup;
        this.active = active;
        this.recovery = recovery;
    }

    boolean start() {
        if (running()) return false;
        remaining = windup + active + recovery;
        sequence++;
        return true;
    }

    void update(double dt) {
        remaining = Math.max(0, remaining - dt);
    }

    public Phase phase() {
        if (remaining == 0) return Phase.READY;
        if (remaining > active + recovery) return Phase.WINDUP;
        return remaining > recovery ? Phase.ACTIVE : Phase.RECOVERY;
    }

    /**
     * Progress within the current phase, for synchronizing presentation with combat.
     */
    public double phaseProgress() {
        return switch (phase()) {
            case READY -> 0;
            case WINDUP -> 1 - (remaining - active - recovery) / windup;
            case ACTIVE -> 1 - (remaining - recovery) / active;
            case RECOVERY -> 1 - remaining / recovery;
        };
    }

    public boolean running() {
        return remaining > 0;
    }

    public boolean lateRecovery(double window) {
        return phase() == Phase.RECOVERY && remaining <= window;
    }

    void cancel() {
        remaining = 0;
    }

    public long sequence() {
        return sequence;
    }
}
