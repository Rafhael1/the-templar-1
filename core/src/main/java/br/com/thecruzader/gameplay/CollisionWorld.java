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
    private final List<Bounds> lockedSolids, arenaSolids;
    private boolean arenaClosed;

    public CollisionWorld(LevelData level) {
        this.level = level;
        var withGate = new ArrayList<>(level.solids());
        withGate.add(level.gate());
        lockedSolids = List.copyOf(withGate);
        withGate.add(level.arenaEntrance());
        arenaSolids = List.copyOf(withGate);
    }

    LevelData level() { return level; }

    boolean clearShot(double fromX, double fromY, double toX, double toY, boolean gateLocked) {
        for (Bounds solid : solids(gateLocked)) {
            double near = 0, far = 1;
            double dx = toX - fromX, dy = toY - fromY;
            if (dx == 0) {
                if (fromX <= solid.x() || fromX >= solid.right()) continue;
            } else {
                double a = (solid.x() - fromX) / dx, b = (solid.right() - fromX) / dx;
                near = Math.max(near, Math.min(a, b));
                far = Math.min(far, Math.max(a, b));
            }
            if (dy == 0) {
                if (fromY <= solid.y() || fromY >= solid.top()) continue;
            } else {
                double a = (solid.y() - fromY) / dy, b = (solid.top() - fromY) / dy;
                near = Math.max(near, Math.min(a, b));
                far = Math.min(far, Math.max(a, b));
            }
            if (near < far && far > 0 && near < 1) return false;
        }
        return true;
    }

    void setArenaClosed(boolean closed) { arenaClosed = closed; }
    List<Bounds> solids(boolean gateLocked) {
        return gateLocked ? (arenaClosed ? arenaSolids : lockedSolids) : level.solids();
    }

    double constrainX(double fromX, double targetX, double y, double width, double height, boolean locked) {
        List<Bounds> solids = solids(locked);
        double x = Math.max(0, Math.min(level.width() - width, targetX));
        for (Bounds solid : solids) {
            if (!solid.overlaps(x, y, width, height)) continue;
            if (targetX > fromX) x = Math.min(x, solid.x() - width);
            else if (targetX < fromX) x = Math.max(x, solid.right());
        }
        return Math.max(0, Math.min(level.width() - width, x));
    }

    void move(Player p, double dt, boolean gateLocked) {
        List<Bounds> solids = solids(gateLocked);
        p.x += p.vx * dt;
        for (Bounds solid : solids)
            if (solid.overlaps(p.x, p.y, Player.WIDTH, Player.HEIGHT)) {
                if (p.vx > 0) p.x = solid.x() - Player.WIDTH;
                else if (p.vx < 0) p.x = solid.right();
            }
        p.x = Math.max(0, Math.min(level.width() - Player.WIDTH, p.x));
        p.y += p.vy * dt;
        p.grounded = false;
        for (Bounds solid : solids)
            if (solid.overlaps(p.x, p.y, Player.WIDTH, Player.HEIGHT)) {
                if (p.vy < 0) {
                    p.y = solid.top();
                    p.grounded = true;
                } else if (p.vy > 0) p.y = solid.y() - Player.HEIGHT;
                p.vy = 0;
            }
    }
    void move(Enemy enemy, double dt, boolean gateLocked) {
        if (enemy.health <= 0) return;
        List<Bounds> solids = solids(gateLocked);
        double previousX = enemy.x;
        enemy.x = Math.max(0, Math.min(level.width() - enemy.width(),
                enemy.x + enemy.vx * dt));
        for (Bounds solid : solids) {
            if (!solid.overlaps(enemy.x, enemy.y, enemy.width(), enemy.height())) continue;
            double rise = solid.top() - enemy.y;
            Bounds stepped = new Bounds(enemy.x, solid.top(),
                    enemy.width(), enemy.height());
            if (enemy.grounded && rise > 0 && rise <= 32
                    && solids.stream().noneMatch(other -> stepped.overlaps(other))) {
                enemy.y = solid.top();
            } else if (enemy.x > previousX) {
                enemy.x = solid.x() - enemy.width();
            } else if (enemy.x < previousX) {
                enemy.x = solid.right();
            }
        }
        enemy.vy = Math.max(-700, enemy.vy - 1450 * dt);
        enemy.y += enemy.vy * dt;
        enemy.grounded = false;
        for (Bounds solid : solids) {
            if (!solid.overlaps(enemy.x, enemy.y, enemy.width(), enemy.height())) continue;
            if (enemy.vy < 0) {
                enemy.y = solid.top();
                enemy.grounded = true;
            } else if (enemy.vy > 0) {
                enemy.y = solid.y() - enemy.height();
            }
            enemy.vy = 0;
        }
        if (enemy.y < -100) {
            enemy.health = 0;
            enemy.state = Enemy.State.DEAD;
        }
    }
}
