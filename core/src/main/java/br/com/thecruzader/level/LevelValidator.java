package br.com.thecruzader.level;

/** Supported authored maps are horizontal, with a one-screen, flat boss arena. */
public final class LevelValidator {
    public static final int VIEW_WIDTH = 960, VIEW_HEIGHT = 540;
    private LevelValidator() {}

    public static void validate(LevelData level) {
        if (level.width() < VIEW_WIDTH || level.height() < VIEW_HEIGHT)
            throw new IllegalArgumentException("Map must cover the 960 x 540 viewport");
        Bounds arena = level.arena();
        if (arena.width() < LevelData.PLAYER_WIDTH + LevelData.EnemyKind.GUARDIAN.width
                || arena.width() / 2 + Math.max(32, level.gate().width()) > VIEW_WIDTH / 2.0
                || arena.height() < 240)
            throw new IllegalArgumentException("Arena must fit the viewport including gates and contain boss jumps");
        for (Bounds solid : level.solids()) inside(level, solid, "Collision");
        inside(level, arena, "Arena");
        inside(level, level.gate(), "Gate");
        inside(level, level.arenaEntrance(), "Arena entrance");
        if (level.solids().stream().anyMatch(solid -> solid.overlaps(level.gate()) || solid.overlaps(level.arenaEntrance())))
            throw new IllegalArgumentException("Arena gates must be clear of static terrain");
        clear(level, level.exit(), "Exit");
        if (level.gate().y() != arena.y() || level.gate().height() < 176
                || level.exit().x() < level.gate().right() || level.exit().y() != arena.y())
            throw new IllegalArgumentException("Gate and exit must follow the arena on the same floor");
        Bounds spawn = playerBody(level.spawn()), checkpoint = playerBody(level.checkpoint());
        clear(level, spawn, "Spawn");
        clear(level, checkpoint, "Checkpoint");
        supported(level, spawn, "Spawn");
        supported(level, checkpoint, "Checkpoint");
        if (spawn.right() > level.arenaEntrance().x() || checkpoint.right() > level.arenaEntrance().x())
            throw new IllegalArgumentException("Spawn and checkpoint must precede the arena entrance");
        for (Bounds bandage : level.bandages()) clear(level, bandage, "Bandage");
        boolean supportedFloor = level.solids().stream().anyMatch(solid -> solid.top() == arena.y()
                && solid.x() <= arena.x() && solid.right() >= level.exit().right());
        if (!supportedFloor || level.solids().stream().anyMatch(arena::overlaps))
            throw new IllegalArgumentException("Arena and exit require a continuous, clear floor");
        for (var enemy : level.enemies()) {
            Bounds body = new Bounds(enemy.x(), enemy.y(), enemy.kind().width, enemy.kind().height);
            clear(level, body, "Enemy " + enemy.kind());
            supported(level, body, "Enemy " + enemy.kind());
            if (enemy.kind() == LevelData.EnemyKind.GUARDIAN) {
                if (body.x() < arena.x() || body.right() > arena.right() || body.y() != arena.y())
                    throw new IllegalArgumentException("Guardian must stand inside the arena");
            } else if (body.right() > level.arenaEntrance().x()) {
                throw new IllegalArgumentException("Road enemies must precede the arena entrance");
            }
        }
    }
    private static void supported(LevelData level, Bounds body, String name) {
        if (level.solids().stream().noneMatch(solid -> solid.top() == body.y()
                && solid.x() <= body.x() && solid.right() >= body.right()))
            throw new IllegalArgumentException(name + " must stand fully on a floor or platform");
    }
    private static Bounds playerBody(LevelData.Point point) {
        return new Bounds(point.x(), point.y(), LevelData.PLAYER_WIDTH, LevelData.PLAYER_HEIGHT);
    }
    private static void inside(LevelData level, Bounds bounds, String name) {
        if (bounds.x() < 0 || bounds.y() < 0 || bounds.right() > level.width()
                || bounds.top() > Math.min(level.height(), VIEW_HEIGHT))
            throw new IllegalArgumentException(name + " lies outside the visible map");
    }
    private static void clear(LevelData level, Bounds bounds, String name) {
        inside(level, bounds, name);
        if (level.solids().stream().anyMatch(bounds::overlaps) || bounds.overlaps(level.gate())
                || bounds.overlaps(level.arenaEntrance()))
            throw new IllegalArgumentException(name + " overlaps terrain or a gate");
    }
}
