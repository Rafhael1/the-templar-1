package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;

import java.util.List;

final class CombatSystem {
    void resolve(Player player, List<Enemy> enemies, List<GameEvent> events) {
        Bounds sword = player.weaponBounds();
        for (Enemy enemy : enemies) {
            if (enemy.health <= 0) continue;
            if (player.attack.phase() == Attack.Phase.ACTIVE
                    && (player.attackDirection != Player.AttackDirection.DOWN || !player.grounded)
                    && enemy.lastPlayerAttack != player.attack.sequence()
                    && sword.overlaps(enemy.bounds())) {
                enemy.lastPlayerAttack = player.attack.sequence();
                enemy.health--;
                enemy.flash = 0.14;
                if (enemy.health == 0) enemy.state = Enemy.State.DEAD;
                events.add(GameEvent.HIT);
            }
            if (enemy.health <= 0 || enemy.attack.phase() != Attack.Phase.ACTIVE || enemy.hitPlayer)
                continue;
            if (enemy.weaponBounds().overlaps(player.bounds())) {
                enemy.hitPlayer = true;
                double enemyCenter = enemy.x + enemy.bounds().width() / 2;
                double playerCenter = player.x + Player.WIDTH / 2;
                if (player.blocking && (enemyCenter - playerCenter) * player.facing > 0) {
                    player.blockFlash = 0.16;
                    events.add(GameEvent.BLOCK);
                } else if (player.invulnerable == 0) {
                    player.health--;
                    player.invulnerable = 0.85;
                    events.add(GameEvent.HURT);
                }
            }
        }
    }
}
