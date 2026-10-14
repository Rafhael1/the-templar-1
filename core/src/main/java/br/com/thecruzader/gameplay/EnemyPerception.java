package br.com.thecruzader.gameplay;

/** Initial awareness requires nearby, unobstructed sight; pursuit remembers the last seen position. */
final class EnemyPerception {
    private final CollisionWorld collision;

    EnemyPerception(CollisionWorld collision) { this.collision = collision; }

    boolean update(Enemy enemy, Player player, double dt, boolean gateLocked) {
        double dx = player.x + Player.WIDTH / 2 - (enemy.x + enemy.width() / 2);
        double dy = player.y - enemy.y;
        boolean aware = enemy.alertTime > 0;
        boolean close = Math.abs(dx) <= 80 && Math.abs(dy) <= 60;
        double range = aware ? 420 : enemy.archer() ? 280 : 230;
        boolean visible = enemy.calmTime == 0 && Math.abs(dx) <= range
                && Math.abs(dy) <= (aware ? 240 : 160)
                && (aware || close || dx * enemy.facing > 0)
                && (aware || close || Math.abs(dy) <= Math.abs(dx) * .8 + 48)
                && collision.clearShot(enemy.x + enemy.width() / 2, enemy.y + enemy.height() * .85,
                        player.x + Player.WIDTH / 2, player.y + Player.HEIGHT * .85, gateLocked);
        if (!visible) {
            enemy.noticeTime = 0;
            enemy.alertTime = Math.max(0, enemy.alertTime - dt);
            return false;
        }
        if (aware || close) alert(enemy, player);
        else {
            enemy.noticeTime += dt;
            if (enemy.noticeTime >= .4) alert(enemy, player);
        }
        return enemy.alertTime > 0;
    }

    static void alert(Enemy enemy, Player player) {
        enemy.alertTime = 2;
        enemy.noticeTime = 0;
        enemy.targetX = player.x + Player.WIDTH / 2 - enemy.width() / 2;
        enemy.targetY = player.y;
    }
}
