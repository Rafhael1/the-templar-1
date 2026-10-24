package br.com.thecruzader.gameplay;

final class EnemySystem {
    private final EnemyNavigation navigation;
    private final CollisionWorld collision;
    private final EnemyPerception perception;

    private final java.util.List<OilPot> oilPots;

    EnemySystem(CollisionWorld collision, java.util.List<OilPot> oilPots) {
        this.oilPots = java.util.Objects.requireNonNull(oilPots);
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
        enemy.flash = Math.max(0, enemy.flash - dt);
        if (enemy.health <= 0) {
            enemy.state = Enemy.State.DEAD;
            return;
        }
        enemy.calmTime = Math.max(0, enemy.calmTime - dt);
        if (enemy.stagger > 0) {
            enemy.stagger = Math.max(0, enemy.stagger - dt);
            if (enemy.stagger == 0) enemy.riposteReady = false;
            enemy.vx = enemy.knockback;
            enemy.knockback *= Math.max(0, 1 - dt * 14);
            enemy.state = Enemy.State.RECOVER;
            return;
        }
        enemy.attack.update(dt);
        enemy.behavior.update(enemy, player, dt, arrows, oilPots, gateLocked, collision, navigation, perception);
    }
}
