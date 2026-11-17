package br.com.thecruzader.gameplay;

import java.util.List;

/**
 * Boss lifecycle shared by level-specific skill sets.
 */
abstract class BossBehavior extends EnemyBehavior {
    BossBehavior(int health, double range, double windup, double active, double recovery) {
        super(health, range, windup, active, recovery);
    }

    @Override
    final double sightRange() {
        return 560;
    }

    @Override
    final void update(Enemy boss, Player player, double dt, List<Arrow> arrows, List<OilPot> oilPots,
                      boolean gateLocked, CollisionWorld collision, EnemyNavigation navigation, EnemyPerception perception) {
        boolean visible = Math.abs(player.x - boss.x) < sightRange() && Math.abs(player.y - boss.y) < 240;
        boss.alertTime = visible ? 2 : Math.max(0, boss.alertTime - dt);
        boss.actionTime = Math.max(0, boss.actionTime - dt);
        double tempo = boss.health <= boss.maxHealth() / 2 ? 0.8 : 1;
        if (updateSkill(boss, tempo, arrows)) return;
        if (boss.state == Enemy.State.RECOVER && !boss.attack.running() && boss.actionTime > 0) return;
        if (boss.attack.running()) {
            boss.updateAttackState();
            return;
        }
        if (boss.alertTime == 0) {
            boss.state = Enemy.State.PATROL;
            return;
        }
        double distance = player.x + Player.WIDTH / 2 - (boss.x + boss.width() / 2);
        boss.facing = distance < 0 ? -1 : 1;
        if (Math.abs(distance) > engagementRange()) {
            boss.state = Enemy.State.CHASE;
            boss.vx = boss.facing * approachSpeed();
            return;
        }
        if (boss.grounded) selectSkill(boss, tempo);
    }

    abstract boolean updateSkill(Enemy boss, double tempo, List<Arrow> arrows);

    abstract void selectSkill(Enemy boss, double tempo);

    double engagementRange() {
        return 300;
    }

    double approachSpeed() {
        return 145;
    }

    final void recover(Enemy boss, double duration) {
        boss.state = Enemy.State.RECOVER;
        boss.actionTime = duration;
    }
}
