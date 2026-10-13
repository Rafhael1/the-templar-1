package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves each axis independently against solid rectangles, including ceilings and the locked
 * gate.
 */
public final class CollisionWorld {
    private final LevelData level;
    private final List<Bounds> lockedSolids;

    public CollisionWorld(LevelData level) {
        this.level = level;
        var withGate = new ArrayList<>(level.solids());
        withGate.add(level.gate());
        lockedSolids = List.copyOf(withGate);
    }

    void move(Player p, double dt, boolean gateLocked) {
        List<Bounds> solids = gateLocked ? lockedSolids : level.solids();
        p.x += p.vx * dt;
        for (Bounds solid : solids)
            if (p.bounds().overlaps(solid)) {
                if (p.vx > 0) p.x = solid.x() - Player.WIDTH;
                else if (p.vx < 0) p.x = solid.right();
            }
        p.x = Math.max(0, Math.min(level.width() - Player.WIDTH, p.x));
        p.y += p.vy * dt;
        p.grounded = false;
        for (Bounds solid : solids)
            if (p.bounds().overlaps(solid)) {
                if (p.vy < 0) {
                    p.y = solid.top();
                    p.grounded = true;
                } else if (p.vy > 0) p.y = solid.y() - Player.HEIGHT;
                p.vy = 0;
            }
    }
    void move(Enemy enemy, double dt, boolean gateLocked) {
        if (enemy.health <= 0) return;
        List<Bounds> solids = gateLocked ? lockedSolids : level.solids();
        double previousX = enemy.x;
        enemy.x = Math.max(0, Math.min(level.width() - enemy.bounds().width(),
                enemy.x + enemy.vx * dt));
        for (Bounds solid : solids) {
            if (!enemy.bounds().overlaps(solid)) continue;
            double rise = solid.top() - enemy.y;
            Bounds stepped = new Bounds(enemy.x, solid.top(),
                    enemy.bounds().width(), enemy.bounds().height());
            if (enemy.grounded && rise > 0 && rise <= 32
                    && solids.stream().noneMatch(other -> stepped.overlaps(other))) {
                enemy.y = solid.top();
            } else if (enemy.x > previousX) {
                enemy.x = solid.x() - enemy.bounds().width();
            } else if (enemy.x < previousX) {
                enemy.x = solid.right();
            }
        }
        enemy.vy = Math.max(-700, enemy.vy - 1450 * dt);
        enemy.y += enemy.vy * dt;
        enemy.grounded = false;
        for (Bounds solid : solids) {
            if (!enemy.bounds().overlaps(solid)) continue;
            if (enemy.vy < 0) {
                enemy.y = solid.top();
                enemy.grounded = true;
            } else if (enemy.vy > 0) {
                enemy.y = solid.y() - enemy.bounds().height();
            }
            enemy.vy = 0;
        }
        if (enemy.y < -100) {
            enemy.health = 0;
            enemy.state = Enemy.State.DEAD;
        }
    }
}
