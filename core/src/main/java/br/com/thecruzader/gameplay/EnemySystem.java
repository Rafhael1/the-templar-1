package br.com.thecruzader.gameplay;

final class EnemySystem {
    private final BossSystem bosses = new BossSystem();
    private final EnemyNavigation navigation;
    private final CollisionWorld collision;
    private final EnemyPerception perception;

    EnemySystem(CollisionWorld collision) {
        this.collision = collision;
        navigation = new EnemyNavigation(collision.level());
        perception = new EnemyPerception(collision);
    }

    void update(Enemy enemy, Player player, double dt, java.util.List<Arrow> arrows, boolean gateLocked) {
        enemy.blockedTime = (enemy.state == Enemy.State.CHASE || enemy.state == Enemy.State.PATROL)
                && Math.abs(enemy.x - enemy.lastChaseX) + Math.abs(enemy.y - enemy.lastChaseY) < .1
                ? enemy.blockedTime + dt : 0;
        enemy.lastChaseX = enemy.x;
        enemy.lastChaseY = enemy.y;
        enemy.vx = 0;
        if (enemy.health <= 0) {
            enemy.state = Enemy.State.DEAD;
            return;
        }
        enemy.flash = Math.max(0, enemy.flash - dt);
        enemy.calmTime = Math.max(0, enemy.calmTime - dt);
        enemy.attack.update(dt);
        if (enemy.boss) {
            boolean visible = Math.abs(player.x - enemy.x) < 560 && Math.abs(player.y - enemy.y) < 240;
            enemy.alertTime = visible ? 2 : Math.max(0, enemy.alertTime - dt);
            bosses.update(enemy, player, dt);
            return;
        }
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
            enemy.state = switch (enemy.attack.phase()) {
                case WINDUP -> Enemy.State.WINDUP;
                case ACTIVE -> Enemy.State.ATTACK;
                case RECOVERY -> Enemy.State.RECOVER;
                case READY -> throw new IllegalStateException("Running attack cannot be ready");
            };
            if (enemy.archer() && enemy.attack.phase() == Attack.Phase.ACTIVE && !enemy.shotFired) {
                enemy.shotFired = true;
                arrows.add(new Arrow(enemy.x + enemy.width() / 2, enemy.y + 32, enemy.aimX, enemy.aimY));
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
            boolean canAttack = visible && enemy.jumpVx == 0 && (enemy.archer()
                    ? Math.abs(distance) < 400 && collision.clearShot(enemy.x + enemy.width() / 2,
                            enemy.y + 32, player.x + Player.WIDTH / 2, player.y + Player.HEIGHT / 2, gateLocked)
                    : enemy.weaponBounds().overlaps(player.bounds()));
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
                if (visible && enemy.bounds().overlaps(player.bounds())) {
                    double spacing = (enemy.width() + Player.WIDTH) / 2 + enemy.range() * .5;
                    target -= enemy.facing * spacing;
                }
                navigation.pursue(enemy, target, enemy.targetY, dt);
            }
        } else {
            enemy.state = Enemy.State.PATROL;
            navigation.patrol(enemy, dt);
        }
    }
}
