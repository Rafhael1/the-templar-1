package br.com.thecruzader.gameplay;

import java.util.List;

/** Living bodies stop each other without dealing contact damage or blocking jumps over them. */
final class BodyCollisionSystem {
    void resolve(Player player, List<Enemy> enemies, CollisionWorld terrain, boolean locked) {
        if (player.dodging()) return;
        // Revisit contacts when separating one body changes contact with another.
        for (int pass = 0; pass < enemies.size(); pass++) {
            boolean contact = false;
            for (Enemy enemy : enemies) {
                if (enemy.health <= 0 || !enemy.bounds().overlaps(player.bounds())) continue;
                contact = true;
                separate(player, enemy, terrain, locked);
            }
            if (!contact) return;
        }
    }

    private void separate(Player player, Enemy enemy, CollisionWorld terrain, boolean locked) {
        if (player.previousY >= enemy.previousY + enemy.height() - .01 && player.vy <= 0) {
            player.y = enemy.y + enemy.height();
            player.vy = 0;
            player.grounded = true;
            return;
        }
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
