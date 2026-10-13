package br.com.thecruzader.level;

import java.util.List;
import java.util.Objects;

public record LevelData(
        double width,
        double height,
        Point spawn,
        Point checkpoint,
        List<Bounds> breads,
        Bounds gate,
        Bounds exit,
        List<Bounds> solids,
        List<Spawn> enemies) {
    public record Point(double x, double y) {}

    public enum EnemyKind { SENTINEL, ARCHER, GUARDIAN }

    public record Spawn(double x, double y, EnemyKind kind) {
        public Spawn { Objects.requireNonNull(kind); }
    }

    public LevelData {
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Level dimensions must be positive");
        Objects.requireNonNull(spawn);
        Objects.requireNonNull(checkpoint);
        breads = List.copyOf(breads);
        if (breads.isEmpty()) throw new IllegalArgumentException("Level requires bread pickups");
        Objects.requireNonNull(gate);
        Objects.requireNonNull(exit);
        solids = List.copyOf(solids);
        enemies = List.copyOf(enemies);
        if (solids.isEmpty())
            throw new IllegalArgumentException("Level requires collision geometry");
        if (enemies.stream().filter(enemy -> enemy.kind() == EnemyKind.GUARDIAN).count() != 1)
            throw new IllegalArgumentException("Level requires exactly one guardian");
    }
}
