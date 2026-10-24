package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class OilSystem {
    void update(List<OilPot> pots, Player player, CollisionWorld collision, boolean gateLocked,
                double dt, List<GameEvent> events, List<CombatEffect> effects) {
        var iterator = pots.iterator();
        while (iterator.hasNext()) {
            OilPot pot = iterator.next();
            pot.life -= dt;
            if (pot.life <= 0) {
                iterator.remove();
                continue;
            }
            if (pot.burning) {
                if (pot.fireBounds().overlaps(player.bounds())) CombatSystem.hurtPlayer(player, events);
                continue;
            }
            double dx = pot.vx * dt, dy = pot.vy * dt - OilPot.GRAVITY * dt * dt / 2;
            double terrainHit = Double.POSITIVE_INFINITY;
            Bounds surface = null;
            for (Bounds solid : collision.solids(gateLocked)) {
                double hit = ProjectileSystem.hitFraction(pot.x, pot.y, dx, dy, solid, OilPot.RADIUS, OilPot.RADIUS);
                if (hit < terrainHit) {
                    terrainHit = hit;
                    surface = solid;
                }
            }
            double playerHit = ProjectileSystem.hitFraction(pot.x, pot.y, dx, dy, player.bounds(), OilPot.RADIUS, OilPot.RADIUS);
            if (playerHit < terrainHit) {
                CombatSystem.hitPlayer(player, pot.x, false, events);
                shatter(pot.x + dx * playerHit, pot.y + dy * playerHit, events, effects);
                iterator.remove();
                continue;
            }
            if (surface != null) {
                pot.x += dx * terrainHit;
                // Side and ceiling impacts shatter the pot without creating floating flames.
                if (dy >= 0 || Math.abs(pot.y + dy * terrainHit - surface.top() - OilPot.RADIUS) > .01) {
                    shatter(pot.x, pot.y + dy * terrainHit, events, effects);
                    iterator.remove();
                    continue;
                }
                pot.y = surface.top();
                pot.burning = true;
                pot.vx = pot.vy = 0;
                pot.life = 3;
                double left = Math.max(surface.x(), pot.x - 36), right = Math.min(surface.right(), pot.x + 36);
                for (Bounds obstacle : collision.solids(gateLocked)) {
                    if (obstacle.y() >= pot.y + 18 || obstacle.top() <= pot.y) continue;
                    if (obstacle.right() <= pot.x) left = Math.max(left, obstacle.right());
                    else if (obstacle.x() >= pot.x) right = Math.min(right, obstacle.x());
                }
                if (right <= left) {
                    iterator.remove();
                    continue;
                }
                pot.fireLeft = left;
                pot.fireWidth = right - left;
                shatter(pot.x, pot.y, events, effects);
                events.add(GameEvent.FIRE_IGNITE);
                effects.add(new CombatEffect(CombatEffect.Kind.FIRE_IGNITE, pot.x, pot.y));
            } else {
                pot.x += dx;
                pot.y += dy;
                pot.vy -= OilPot.GRAVITY * dt;
            }
        }
    }

    private static void shatter(double x, double y, List<GameEvent> events, List<CombatEffect> effects) {
        events.add(GameEvent.POT_SHATTER);
        effects.add(new CombatEffect(CombatEffect.Kind.POT_SHATTER, x, y));
    }
}
