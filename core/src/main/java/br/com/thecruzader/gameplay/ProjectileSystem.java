package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import java.util.List;

final class ProjectileSystem {
    void update(List<Arrow> arrows, Player player, List<Enemy> enemies, CollisionWorld collision, boolean gateLocked,
            double dt, List<GameEvent> events) {
        var iterator = arrows.iterator();
        while (iterator.hasNext()) {
            Arrow arrow = iterator.next();
            double dx = arrow.vx * dt, dy = arrow.vy * dt;
            double halfWidth = arrow.heavy() ? 12 : 6, halfHeight = arrow.heavy() ? 4 : 2;
            double terrainHit = Double.POSITIVE_INFINITY;
            for (Bounds solid : collision.solids(gateLocked))
                terrainHit = Math.min(terrainHit, hitFraction(arrow.x, arrow.y, dx, dy, solid, halfWidth, halfHeight));
            if (arrow.kind == Arrow.Kind.PLAYER) {
                Enemy target = null;
                double firstHit = terrainHit;
                for (Enemy enemy : enemies) {
                    if (enemy.health <= 0) continue;
                    double hit = hitFraction(arrow.x, arrow.y, dx, dy, enemy.bounds(), 6, 2);
                    if (hit >= firstHit) continue;
                    firstHit = hit;
                    target = enemy;
                }
                if (target != null) {
                    CombatSystem.resolveArrowStrike(player, target, (int) Math.signum(arrow.vx), events);
                    iterator.remove();
                    continue;
                }
            } else if (hitFraction(arrow.x, arrow.y, dx, dy, player.bounds(), halfWidth, halfHeight) < terrainHit) {
                CombatSystem.hitPlayer(player, player.x + Player.WIDTH / 2
                    - Math.signum(arrow.vx) * 10, arrow.heavy(), arrow.heavy() ? 2 : 1, events);
                iterator.remove();
                continue;
            }
            arrow.life -= dt;
            if (terrainHit <= 1 || arrow.life <= 0) {
                iterator.remove();
                continue;
            }
            arrow.x += dx;
            arrow.y += dy;
        }
    }

    static double hitFraction(double x, double y, double dx, double dy, Bounds bounds, double halfWidth, double halfHeight) {
        double near = 0, far = 1;
        double[] origin = {x, y};
        double[] motion = {dx, dy};
        double[] low = {bounds.x() - halfWidth, bounds.y() - halfHeight};
        double[] high = {bounds.right() + halfWidth, bounds.top() + halfHeight};
        for (int axis = 0; axis < 2; axis++) {
            if (motion[axis] == 0) {
                if (origin[axis] < low[axis] || origin[axis] > high[axis])
                    return Double.POSITIVE_INFINITY;
            } else {
                double first = (low[axis] - origin[axis]) / motion[axis];
                double second = (high[axis] - origin[axis]) / motion[axis];
                near = Math.max(near, Math.min(first, second));
                far = Math.min(far, Math.max(first, second));
                if (near > far) return Double.POSITIVE_INFINITY;
            }
        }
        return near;
    }
}
