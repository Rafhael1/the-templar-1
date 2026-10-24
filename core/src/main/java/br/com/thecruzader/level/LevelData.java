package br.com.thecruzader.level;

import java.util.List;
import java.util.Objects;

public record LevelData(
        double width,
        double height,
        Point spawn,
        Point checkpoint,
        List<Bounds> bandages,
        Bounds gate,
        Bounds exit,
        Bounds arena,
        List<Bounds> solids,
        List<Spawn> enemies) {
    public static final double PLAYER_WIDTH = 28, PLAYER_HEIGHT = 52;
    public record Point(double x, double y) {
        public Point { requireFinitePoint(x, y); }
    }

    public enum EnemyKind {
        SENTINEL(30, 48, false), ARCHER(30, 48, false), SPEARMAN(30, 48, false), SHIELD_BEARER(30, 48, false),
        BRUTE(30, 48, false), OIL_THROWER(30, 48, false), GUARDIAN(42, 68, true);
        public final double width, height;
        public final boolean boss;

        EnemyKind(double width, double height, boolean boss) {
            this.width = width;
            this.height = height;
            this.boss = boss;
        }
    }

    public record Spawn(double x, double y, EnemyKind kind) {
        public Spawn { Objects.requireNonNull(kind); requireFinitePoint(x, y); }
    }

    public boolean insideArena(double x) { return x >= arena.x() && x < gate.right(); }

    public Bounds arenaEntrance() { return new Bounds(arena.x() - 32, arena.y(), 32, arena.height()); }

    private static void requireFinitePoint(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y))
            throw new IllegalArgumentException("Point coordinates must be finite");
    }

    public LevelData {
        if (!Double.isFinite(width) || !Double.isFinite(height) || width <= 0 || height <= 0)
            throw new IllegalArgumentException("Level dimensions must be finite and positive");
        Objects.requireNonNull(spawn);
        Objects.requireNonNull(checkpoint);
        bandages = List.copyOf(bandages);
        if (bandages.isEmpty()) throw new IllegalArgumentException("Level requires bandage pickups");
        Objects.requireNonNull(gate);
        Objects.requireNonNull(exit);
        Objects.requireNonNull(arena);
        if (arena.x() < 32 || arena.y() < 0 || arena.right() > width
                || arena.right() != gate.x() || arena.top() > height)
            throw new IllegalArgumentException("Arena must be inside the map and end at the locked gate");
        solids = List.copyOf(solids);
        enemies = List.copyOf(enemies);
        if (solids.isEmpty())
            throw new IllegalArgumentException("Level requires collision geometry");
        if (enemies.stream().filter(enemy -> enemy.kind().boss).count() != 1)
            throw new IllegalArgumentException("Level requires exactly one boss");
    }
}
