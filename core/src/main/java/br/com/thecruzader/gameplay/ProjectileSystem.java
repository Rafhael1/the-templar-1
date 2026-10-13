package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import java.util.List;

final class ProjectileSystem {
    void update(List<Arrow> arrows, Player player, CollisionWorld collision, boolean gateLocked,
            double dt, List<GameEvent> events) {
        var iterator = arrows.iterator();
        while (iterator.hasNext()) {
            Arrow arrow = iterator.next();
            double dx = arrow.vx * dt, dy = arrow.vy * dt;
            double terrainHit = Double.POSITIVE_INFINITY;
            for (Bounds solid : collision.solids(gateLocked))
                terrainHit = Math.min(terrainHit, hitFraction(arrow, dx, dy, solid));
            double playerHit = hitFraction(arrow, dx, dy, player.bounds());
            if (playerHit < terrainHit) {
                CombatSystem.hitPlayer(player, player.x + Player.WIDTH / 2
                        - Math.signum(arrow.vx) * 10, events);
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

    private double hitFraction(Arrow arrow, double dx, double dy, Bounds bounds) {
        double near = 0, far = 1;
        double[] origin = {arrow.x, arrow.y};
        double[] motion = {dx, dy};
        double[] low = {bounds.x() - 6, bounds.y() - 2};
        double[] high = {bounds.right() + 6, bounds.top() + 2};
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
