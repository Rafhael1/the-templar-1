package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * Holding formation calls friendly relief; leaving it pauses support, never damages the player.
 */
public final class CampDefense {
    public static final double RELIEF_SECONDS = 45;
    private final Bounds formation;
    private double elapsed;

    CampDefense(Bounds arena) {
        formation = new Bounds(arena.x() + arena.width() / 2 - 180, arena.y(), 360, 180);
    }

    void update(double dt, Player player, List<GameEvent> events) {
        if (reinforcementsArrived() || !inFormation(player)) return;
        elapsed = Math.min(RELIEF_SECONDS, elapsed + dt);
        if (reinforcementsArrived()) events.add(GameEvent.REINFORCEMENTS);
    }

    void relieve(List<GameEvent> events) {
        if (reinforcementsArrived()) return;
        elapsed = RELIEF_SECONDS;
        events.add(GameEvent.REINFORCEMENTS);
    }

    void reset() {
        elapsed = 0;
    }

    public Bounds formation() {
        return formation;
    }

    public boolean inFormation(Player player) {
        return player.x() >= formation.x() && player.x() + Player.WIDTH <= formation.right()
            && player.y() >= formation.y() && player.y() < formation.top();
    }

    public int secondsRemaining() {
        return (int) Math.ceil(Math.max(0, RELIEF_SECONDS - elapsed));
    }

    public boolean reinforcementsArrived() {
        return elapsed >= RELIEF_SECONDS;
    }
}
