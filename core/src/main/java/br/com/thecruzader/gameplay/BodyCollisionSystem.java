package br.com.thecruzader.gameplay;

import java.util.List;

/** Living bodies stop each other without dealing contact damage or blocking jumps over them. */
final class BodyCollisionSystem {
    private static final double SLIDE_SPEED = 150, SLIDE_FALL_SPEED = 100;
    private Enemy slidingEnemy;
    private int slideDirection;

    void resolve(Player player, List<Enemy> enemies, CollisionWorld terrain, boolean locked, double dt) {
        if (player.dodging()) {
            slidingEnemy = null;
            return;
        }
        if (slidingEnemy != null && (!enemies.contains(slidingEnemy) || slidingEnemy.health <= 0
            || player.grounded || player.vy > 0 || !slidingEnemy.bounds().overlaps(player.bounds())))
            slidingEnemy = null;
        if (slidingEnemy == null && !player.grounded && player.vy <= 0) {
            for (Enemy enemy : enemies) {
                if (enemy.health <= 0 || !enemy.bounds().overlaps(player.bounds())
                    || player.previousY < enemy.previousY + enemy.height() - .01) continue;
                slidingEnemy = enemy;
                double offset = player.previousX + Player.WIDTH / 2 - (enemy.previousX + enemy.width() / 2);
                slideDirection = player.vx != 0 ? (player.vx > 0 ? 1 : -1)
                    : offset == 0 ? player.facing : offset > 0 ? 1 : -1;
                double target = slideDirection > 0 ? enemy.x + enemy.width() : enemy.x - Player.WIDTH;
                if (Math.abs(terrain.constrainX(player.x, target, player.y, Player.WIDTH, Player.HEIGHT, locked)
                    - target) > .01) slideDirection = -slideDirection;
                break;
            }
        }
        if (slidingEnemy != null) slide(player, terrain, locked, dt);
        // Revisit contacts when separating one body changes contact with another.
        for (int pass = 0; pass < enemies.size(); pass++) {
            boolean contact = false;
            for (Enemy enemy : enemies) {
                if (enemy == slidingEnemy || enemy.health <= 0 || !enemy.bounds().overlaps(player.bounds())) continue;
                contact = true;
                separate(player, enemy, terrain, locked);
            }
            if (!contact) return;
        }
    }

    private void slide(Player player, CollisionWorld terrain, boolean locked, double dt) {
        double target = slideDirection > 0 ? slidingEnemy.x + slidingEnemy.width() : slidingEnemy.x - Player.WIDTH;
        double distance = Math.max(0, (target - player.x) * slideDirection);
        player.x = terrain.constrainX(player.x,
            player.x + slideDirection * Math.min(distance, SLIDE_SPEED * dt),
            player.y, Player.WIDTH, Player.HEIGHT, locked);
        // Keep descending while the curved shoulder contact guides the player outwards.
        player.vy = Math.max(player.vy, -SLIDE_FALL_SPEED);
    }

    private void separate(Player player, Enemy enemy, CollisionWorld terrain, boolean locked) {
        boolean playerLeft = player.previousX + Player.WIDTH / 2
                <= enemy.previousX + enemy.width() / 2;
        double penetration = playerLeft ? player.x + Player.WIDTH - enemy.x
                : enemy.x + enemy.width() - player.x;
        double playerMotion = Math.max(0, (player.x - player.previousX) * (playerLeft ? 1 : -1));
        double enemyMotion = Math.max(0, (enemy.x - enemy.previousX) * (playerLeft ? -1 : 1));
        double motion = playerMotion + enemyMotion;
        double playerShare = motion > 0 ? playerMotion / motion : .5;
        int away = playerLeft ? -1 : 1;
        player.x = terrain.constrainX(player.x, player.x + away * penetration * playerShare,
                player.y, Player.WIDTH, Player.HEIGHT, locked);
        enemy.x = terrain.constrainX(enemy.x, enemy.x - away * penetration * (1 - playerShare),
                enemy.y, enemy.width(), enemy.height(), locked);
        // A wall can prevent one body's correction; the other body absorbs the remaining overlap.
        if (enemy.bounds().overlaps(player.bounds())) {
            double target = playerLeft ? player.x + Player.WIDTH : player.x - enemy.width();
            enemy.x = terrain.constrainX(enemy.x, target, enemy.y, enemy.width(), enemy.height(), locked);
            target = playerLeft ? enemy.x - Player.WIDTH : enemy.x + enemy.width();
            player.x = terrain.constrainX(player.x, target, player.y, Player.WIDTH, Player.HEIGHT, locked);
        }
        player.vx = 0;
        enemy.vx = 0;
    }
}
