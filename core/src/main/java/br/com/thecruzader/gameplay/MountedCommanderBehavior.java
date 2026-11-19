package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

/**
 * Rides between the flanks, shoots a committed three-arrow volley, then charges back.
 */
final class MountedCommanderBehavior extends EnemyBehavior {
    private int direction = -1;
    private int shots;

    MountedCommanderBehavior() {
        super(18, 56, .75, .9, 1.2);
    }

    @Override
    double sightRange() {
        return 960;
    }

    @Override
    boolean heavyAttack(Enemy e) {
        return e.state == Enemy.State.RIDER_CHARGE_WINDUP || e.state == Enemy.State.RIDER_CHARGE;
    }

    @Override
    boolean contactAttackActive(Enemy e) {
        return e.state == Enemy.State.RIDER_CHARGE;
    }

    @Override
    Bounds contactAttackBounds(Enemy e) {
        return new Bounds(e.x + 6, e.y, e.width() - 12, 56);
    }

    @Override
    void update(Enemy e, Player p, double dt, List<Arrow> arrows, List<OilPot> pots,
                boolean locked, CollisionWorld collision, EnemyNavigation navigation, EnemyPerception perception) {
        e.shotFired = false;
        var arena = collision.level().arena();
        e.actionTime = Math.max(0, e.actionTime - dt);
        e.alertTime = 2;
        switch (e.state) {
            case RIDER_CHARGE_WINDUP -> {
                if (e.actionTime == 0) begin(e, Enemy.State.RIDER_CHARGE, 1.4);
            }
            case RIDER_CHARGE -> {
                e.vx = direction * 440;
                if (atEdge(e, arena) || (e.x + e.width() / 2 - e.aimX) * direction >= 0
                    || e.actionTime == 0) begin(e, Enemy.State.RECOVER, 1.5);
            }
            case RECOVER -> {
                if (e.actionTime == 0) {
                    direction = e.x < arena.x() + arena.width() / 2 ? 1 : -1;
                    begin(e, Enemy.State.RIDER_RIDE, .65);
                }
            }
            case RIDER_DRAW -> {
                e.vx = direction * 95;
                if (atEdge(e, arena)) e.vx = 0;
                if (e.actionTime == 0) {
                    shots = 0;
                    begin(e, Enemy.State.RIDER_VOLLEY, 0);
                }
            }
            case RIDER_VOLLEY -> {
                e.vx = direction * 145;
                if (atEdge(e, arena)) e.vx = 0;
                if (e.actionTime == 0) {
                    if (shots++ < 3) {
                        double center = e.x + e.width() / 2;
                        double x = center + (e.aimX >= center ? 34 : -34), y = e.y + 52;
                        arrows.add(new Arrow(x, y, e.aimX, e.aimY, Arrow.Kind.COMMANDER));
                        e.shotFired = true;
                        e.actionTime = .4;
                    } else {
                        prepareCharge(e, p, arena);
                    }
                }
            }
            default -> {
                e.state = Enemy.State.RIDER_RIDE;
                e.vx = direction * 205;
                if (atEdge(e, arena)) {
                    e.vx = 0;
                    prepareCharge(e, p, arena);
                } else if (e.actionTime == 0) {
                    e.aimX = p.x + Player.WIDTH / 2;
                    e.aimY = p.y + 26;
                    begin(e, Enemy.State.RIDER_DRAW, .85);
                }
            }
        }
        e.facing = direction;
    }

    private void prepareCharge(Enemy e, Player player, Bounds arena) {
        direction = player.x + Player.WIDTH / 2 < e.x + e.width() / 2 ? -1 : 1;
        e.aimX = Math.max(arena.x() + e.width() / 2 + 12,
            Math.min(arena.right() - e.width() / 2 - 12, player.x + direction * 150));
        begin(e, Enemy.State.RIDER_CHARGE_WINDUP, 1.0);
    }

    private boolean atEdge(Enemy e, Bounds arena) {
        return direction < 0 ? e.x <= arena.x() + 12 : e.x + e.width() >= arena.right() - 12;
    }

    private static void begin(Enemy e, Enemy.State state, double time) {
        e.state = state;
        e.actionTime = time;
        e.hitPlayer = false;
    }
}
