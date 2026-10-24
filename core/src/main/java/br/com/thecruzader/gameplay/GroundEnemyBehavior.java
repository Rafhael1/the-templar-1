package br.com.thecruzader.gameplay;

import java.util.List;

/**
 * Shared awareness, pursuit and attack lifecycle for ground enemies.
 */
abstract class GroundEnemyBehavior extends EnemyBehavior {
    GroundEnemyBehavior(int health, double windup, double active, double recovery) {
        this(health, 54, windup, active, recovery);
    }

    GroundEnemyBehavior(int health, double range, double windup, double active, double recovery) {
        super(health, range, windup, active, recovery);
    }

    @Override
    final void update(Enemy enemy, Player player, double dt, List<Arrow> arrows, List<OilPot> oilPots,
                      boolean gateLocked, CollisionWorld collision, EnemyNavigation navigation, EnemyPerception perception) {
        boolean visible = perception.update(enemy, player, dt, gateLocked);
        if (enemy.alertTime > 0) enemy.chaseTime += dt;
        if (!enemy.attack.running() && enemy.chaseTime > 0
            && (enemy.alertTime == 0 || enemy.chaseTime >= 12
            || (enemy.state == Enemy.State.CHASE && enemy.blockedTime >= 3))) {
            enemy.alertTime = enemy.chaseTime = 0;
            enemy.noticeTime = 0;
            enemy.calmTime = 4;
            enemy.patrolCenter = enemy.x;
        }
        if (enemy.attack.running()) {
            if (visible && !enemy.shotFired) trackAttackTarget(enemy);
            enemy.updateAttackState();
            if (enemy.attack.phase() == Attack.Phase.ACTIVE && !enemy.shotFired) {
                onActiveAttack(enemy, arrows, oilPots);
            }
            return;
        }
        if (enemy.grounded) enemy.jumpVx = 0;
        if (enemy.noticeTime > 0) {
            enemy.state = Enemy.State.NOTICE;
            return;
        }
        if (enemy.alertTime > 0) {
            double distance = enemy.targetX - enemy.x;
            if (enemy.jumpVx == 0) enemy.facing = distance < 0 ? -1 : 1;
            boolean canAttack = visible && enemy.jumpVx == 0
                && (Math.abs(distance) >= preferredSpacing() * .8 || Math.abs(enemy.targetY - enemy.y) > 60)
                && canAttack(enemy, player, collision, gateLocked);
            if (canAttack) {
                enemy.attack.start();
                enemy.hitPlayer = false;
                enemy.shotFired = false;
                enemy.aimX = player.x + Player.WIDTH / 2;
                enemy.aimY = player.y + Player.HEIGHT / 2;
                enemy.state = Enemy.State.WINDUP;
            } else {
                enemy.state = Enemy.State.CHASE;
                double target = enemy.targetX;
                if (visible && Math.abs(distance) < preferredSpacing() && Math.abs(enemy.targetY - enemy.y) <= 60) {
                    target -= enemy.facing * preferredSpacing();
                } else if (visible && enemy.bounds().overlaps(player.bounds())) {
                    double spacing = (enemy.width() + Player.WIDTH) / 2 + enemy.range() * .5;
                    target -= enemy.facing * spacing;
                }
                navigation.pursue(enemy, target, enemy.targetY, dt);
                if (enemy.grounded) enemy.vx *= movementScale();
            }
        } else {
            enemy.state = Enemy.State.PATROL;
            navigation.patrol(enemy, dt);
            enemy.vx *= movementScale();
        }
    }

    abstract boolean canAttack(Enemy enemy, Player player, CollisionWorld collision, boolean gateLocked);

    // Committed melee swings and oil throws retain their original target.
    void trackAttackTarget(Enemy enemy) {
    }

    abstract double preferredSpacing();

    abstract void onActiveAttack(Enemy enemy, List<Arrow> arrows, List<OilPot> oilPots);
}
