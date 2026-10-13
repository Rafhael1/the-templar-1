package br.com.thecruzader.gameplay;

final class EnemySystem {
    private final BossSystem bosses = new BossSystem();
    void update(Enemy enemy, Player player, double dt, java.util.List<Arrow> arrows) {
        enemy.vx = 0;
        if (enemy.health <= 0) {
            enemy.state = Enemy.State.DEAD;
            return;
        }
        enemy.flash = Math.max(0, enemy.flash - dt);
        boolean visible =
                Math.abs(player.x - enemy.x) < (enemy.boss ? 560 : 420)
                        && Math.abs(player.y - enemy.y) < 240;
        enemy.alertTime = visible ? 2.0 : Math.max(0, enemy.alertTime - dt);
        enemy.attack.update(dt);
        if (enemy.boss) {
            bosses.update(enemy, player, dt);
            return;
        }
        if (enemy.attack.running()) {
            enemy.state =
                    switch (enemy.attack.phase()) {
                        case WINDUP -> Enemy.State.WINDUP;
                        case ACTIVE -> Enemy.State.ATTACK;
                        case RECOVERY -> Enemy.State.RECOVER;
                        case READY ->
                                throw new IllegalStateException("Running attack cannot be ready");
                    };
            if (enemy.archer() && enemy.attack.phase() == Attack.Phase.ACTIVE && !enemy.shotFired) {
                enemy.shotFired = true;
                arrows.add(new Arrow(enemy.x + enemy.bounds().width() / 2, enemy.y + 32,
                        enemy.aimX, enemy.aimY));
            }
            return;
        }
        double distance = player.x + Player.WIDTH / 2 - (enemy.x + enemy.bounds().width() / 2);
        if (enemy.alertTime > 0) {
            enemy.facing = distance < 0 ? -1 : 1;
            if (enemy.archer() ? Math.abs(distance) < 400 : enemy.weaponBounds().overlaps(player.bounds())) {
                enemy.attack.start();
                enemy.hitPlayer = false;
                enemy.shotFired = false;
                enemy.aimX = player.x + Player.WIDTH / 2;
                enemy.aimY = player.y + Player.HEIGHT / 2;
                enemy.state = Enemy.State.WINDUP;
            } else {
                enemy.state = Enemy.State.CHASE;
                // Wait beneath airborne targets instead of oscillating or swinging at empty air.
                double spacing = enemy.bounds().overlaps(player.bounds())
                        ? (enemy.bounds().width() + Player.WIDTH) / 2 + enemy.range() * 0.5
                        : 0;
                double remaining = Math.abs(distance) - spacing;
                double travel = Math.min(Math.abs(remaining), (enemy.boss ? 145 : 125) * dt);
                enemy.vx = enemy.facing * Math.signum(remaining) * travel / dt;
            }
        } else {
            enemy.state = Enemy.State.PATROL;
            if (enemy.x < enemy.home - 70) enemy.facing = 1;
            if (enemy.x > enemy.home + 70) enemy.facing = -1;
            enemy.vx = enemy.facing * 22;
        }
        double nextX = Math.max(enemy.home - 230, Math.min(enemy.home + 230, enemy.x + enemy.vx * dt));
        enemy.vx = (nextX - enemy.x) / dt;
    }
}
