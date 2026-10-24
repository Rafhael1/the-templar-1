package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.CombatEffect;
import br.com.thecruzader.gameplay.Enemy;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.OilPot;
import br.com.thecruzader.level.LevelData.EnemyKind;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Phase-driven enemy poses, weapons and bounded, allocation-free combat effects.
 */
final class EnemyAttackRenderer {
    private static final int[] MACE_HAND_HEIGHT = {34, 37, 41, 44, 47, 49, 52, 54,
        54, 51, 48, 45, 42, 37, 33, 28, 28, 28, 27, 28, 30, 31, 33, 34};
    // Pixel anchors match the key poses in generate_enemy_attack_assets.py.
    private static final int[] SWORD_HAND_X = {44, 43, 42, 49, 50, 48, 45, 44};
    private static final int[] SWORD_HAND_Y = {42, 29, 21, 36, 45, 46, 44, 42};
    private static final int[] BODY_LEAN = {-1, -3, -4, 4, 5, 3, 1, 0};
    private static final int[] BODY_LIFT = {0, -1, -2, 1, 2, 2, 1, 0};
    private final GameAssets assets;
    private final TextureRegion arrow;
    private final Color tint = new Color();

    EnemyAttackRenderer(GameAssets assets) {
        this.assets = assets;
        arrow = new TextureRegion(assets.texture("arrow"));
    }

    TextureRegion pose(Enemy enemy, TextureRegion walking) {
        int row = switch (enemy.kind()) {
            case BRUTE -> 0;
            case SPEARMAN -> 1;
            case OIL_THROWER -> 2;
            case SENTINEL -> 3;
            case SHIELD_BEARER -> 4;
            case ARCHER -> 5;
            case GUARDIAN -> 6;
        };
        if (enemy.stagger() > 0) return assets.enemyAttacks[row][18];
        int frame = phaseFrame(enemy);
        return frame < 0 ? walking : assets.enemyAttacks[row][frame];
    }

    void sword(Batch batch, Enemy enemy, double x, double y, float scale) {
        double p = enemy.attackProgress();
        double angle = switch (enemy.state()) {
            case WINDUP -> ease(-65, 110, p);
            case ATTACK -> ease(110, -35, p);
            case RECOVER -> ease(-35, -65, p);
            case DASH_WINDUP, JUMP_WINDUP -> 75;
            case DASH -> -15;
            default -> -65;
        };
        int frame = phaseFrame(enemy);
        if (frame < 0) frame = 23;
        if (enemy.stagger() > 0) frame = 18;
        double pixelX = poseKey(SWORD_HAND_X, frame) + poseKey(BODY_LEAN, frame);
        double pixelY = poseKey(SWORD_HAND_Y, frame) + poseKey(BODY_LIFT, frame);
        double grip = x - 17 + (enemy.facing() > 0 ? pixelX : 64 - pixelX) * scale;
        double handY = y - 10 + (80 - pixelY) * scale;
        if (enemy.state() == Enemy.State.ATTACK) {
            for (int i = 2; i >= 1; i--) {
                batch.setColor(.76f, .84f, .85f, .12f / i);
                drawSword(batch, grip, handY, scale, enemy.facing(), ease(110, -35, Math.max(0, p - i * .12)));
            }
        }
        batch.setColor(Color.WHITE);
        drawSword(batch, grip, handY, scale, enemy.facing(), angle);
    }

    private static double poseKey(int[] keys, int frame) {
        int first = frame / 8 * 2;
        int segments = frame < 16 ? 2 : 3;
        double t = frame % 8 / 7.0 * segments;
        int index = Math.min(segments - 1, (int) t);
        return Math.round(keys[first + index]
            + (keys[first + index + 1] - keys[first + index]) * (t - index));
    }

    private void drawSword(Batch batch, double grip, double y, float scale, int facing, double angle) {
        batch.draw(assets.swords[1], (float) Math.round(grip) - 7, (float) Math.round(y) - 8,
            7, 8, 40, 16, scale * facing, scale, (float) angle * facing);
    }

    void shield(Batch batch, Enemy enemy, double x, double y) {
        double p = enemy.attackProgress();
        double lower = switch (enemy.state()) {
            case ATTACK -> ease(0, 1, Math.min(1, p * 4));
            case RECOVER -> enemy.stagger() > 0 ? 1 : ease(1, 0, Math.min(1, p / .25));
            default -> enemy.blocking() ? 0 : 1;
        };
        double px = x + (enemy.facing() > 0 ? 22 : -24) - enemy.facing() * lower * 10;
        batch.setColor(Color.WHITE);
        batch.draw(assets.infantryShield, (float) Math.round(px), (float) Math.round(y + 5 - lower * 2),
            16, 22, 32, 44, enemy.facing(), (float) (1 - lower * .35), (float) (-55 * lower * enemy.facing()));
    }

    void bow(Batch batch, Enemy enemy, double x, double y) {
        double pull = enemy.state() == Enemy.State.WINDUP ? enemy.attackProgress()
            : enemy.state() == Enemy.State.ATTACK ? 1 - enemy.attackProgress() : 0;
        double handX = x + enemy.width() / 2 + enemy.facing() * (12 + pull * 3);
        double handY = y + 31 + pull * 2;
        boolean aiming = enemy.state() == Enemy.State.WINDUP || enemy.state() == Enemy.State.ATTACK;
        float rotation = aiming ? (float) Math.toDegrees(Math.atan2(enemy.aimY() - (enemy.y() + 32),
            Math.abs(enemy.aimX() - (enemy.x() + enemy.width() / 2)))) * enemy.facing() : 0;
        batch.setColor(Color.WHITE);
        batch.draw(assets.bow, (float) Math.round(handX) - 10, (float) Math.round(handY) - 18,
            10, 18, 20, 36, enemy.facing(), 1, rotation);
        if (enemy.state() == Enemy.State.WINDUP) {
            double stringX = x + (enemy.facing() > 0 ? 24 : 6) - enemy.facing() * pull * 7;
            batch.setColor(tint.set(0xd8c8a0cc));
            batch.draw(assets.white, (float) stringX, (float) handY - 9, 0, 9, 1, 18, 1, 1, rotation,
                0, 0, 1, 1, false, false);
            batch.setColor(Color.WHITE);
            batch.draw(arrow, (float) Math.round(handX) - 8, (float) Math.round(handY) - 3,
                8, 3, 16, 6, enemy.facing(), 1, rotation);
        }
    }

    private static int phaseFrame(Enemy enemy) {
        return switch (enemy.state()) {
            case WINDUP -> Math.min(7, (int) (enemy.attackProgress() * 8));
            case ATTACK -> 8 + Math.min(7, (int) (enemy.attackProgress() * 8));
            case RECOVER -> 16 + Math.min(7, (int) (enemy.attackProgress() * 8));
            default -> -1;
        };
    }

    void weapon(Batch batch, Enemy enemy, double x, double y, double time) {
        if (enemy.kind() == EnemyKind.BRUTE) mace(batch, enemy, x, y);
        else if (enemy.kind() == EnemyKind.SPEARMAN) spear(batch, enemy, x, y);
        else if (enemy.kind() == EnemyKind.OIL_THROWER) heldPot(batch, enemy, x, y, time);
        else throw new IllegalArgumentException("Specialist weapon required");
        batch.setColor(Color.WHITE);
    }

    private void mace(Batch batch, Enemy e, double x, double y) {
        double p = e.attackProgress();
        double angle = switch (e.state()) {
            case WINDUP -> ease(55, 128, p);
            case ATTACK -> ease(128, -14, Math.min(1, p * 1.3));
            case RECOVER -> ease(-14, 55, Math.max(0, (p - .22) / .78));
            default -> 55;
        };
        int frame = phaseFrame(e);
        double handY = y + (frame < 0 ? 31 : MACE_HAND_HEIGHT[frame]);
        double grip = e.facing() > 0 ? x + e.width() : x;
        if (e.state() == Enemy.State.ATTACK) {
            for (int i = 3; i >= 1; i--) {
                double trail = ease(128, -14, Math.max(0, Math.min(1, (p - i * .065) * 1.3)));
                batch.setColor(.84f, .72f, .56f, .10f + (3 - i) * .035f);
                drawMace(batch, grip, handY, e.facing(), trail);
            }
        }
        batch.setColor(Color.WHITE);
        drawMace(batch, grip, handY, e.facing(), angle);
        if (e.state() == Enemy.State.WINDUP && p > .7) {
            double radians = Math.toRadians(angle);
            double tipX = grip + e.facing() * Math.cos(radians) * 66, tipY = handY + Math.sin(radians) * 66;
            rect(batch, tipX - 4, tipY, 9, 2, 0xf0d5a0, .75f);
            rect(batch, tipX, tipY - 4, 2, 9, 0xf0d5a0, .75f);
        }
    }

    private void drawMace(Batch batch, double grip, double handY, int facing, double angle) {
        batch.draw(assets.mace, (float) Math.round(grip), (float) Math.round(handY) - 12,
            0, 12, 76, 24, facing, 1, (float) angle * facing);
    }

    private void spear(Batch batch, Enemy e, double x, double y) {
        double p = e.attackProgress();
        double extension = switch (e.state()) {
            case WINDUP -> ease(-28, -50, p);
            case ATTACK -> ease(-50, 0, Math.min(1, p * 4));
            case RECOVER -> ease(0, -28, p);
            default -> -28;
        };
        double tip = e.facing() > 0 ? x + e.width() + e.range() + extension : x - e.range() - extension;
        if (e.state() == Enemy.State.ATTACK) {
            float opacity = (float) ((1 - p) * .55);
            for (int line = 0; line < 3; line++)
                rect(batch, e.facing() > 0 ? tip - 65 - line * 6 : tip + 12, y + 22 + line * 5,
                    52 - line * 8, 2, 0xc6d5cd, opacity);
            rect(batch, e.facing() > 0 ? tip - 10 : tip, y + 26, 10, 2, 0xf0e4c2, 1);
        }
        batch.setColor(Color.WHITE);
        sprite(batch, assets.spear, e.facing() > 0 ? tip - 144 : tip, y + 22, 144, 12, e.facing());
    }

    private void heldPot(Batch batch, Enemy e, double x, double y, double time) {
        if (e.state() == Enemy.State.ATTACK || e.state() == Enemy.State.RECOVER) return;
        double lift = e.state() == Enemy.State.WINDUP ? Math.sin(e.attackProgress() * Math.PI) * 25 : 0;
        double px = x + (e.facing() > 0 ? 22 : -8), py = y + 30 + lift;
        batch.setColor(Color.WHITE);
        sprite(batch, assets.oilPot, px, py, 16, 20, e.facing());
        if (e.state() == Enemy.State.WINDUP) {
            rect(batch, px - 5, py + 10, 26, 25, 0xdc8c49, .1f);
            for (int i = 0; i < 3; i++) {
                double age = (time * 3 + i * .33) % 1;
                rect(batch, px + 8 + Math.sin(i * 2 + age * 4) * 7, py + 19 + age * 14,
                    2, 2, 0xf4ca88, (float) (1 - age));
            }
        }
    }

    void oil(Batch batch, OilPot pot, double time) {
        if (!pot.burning()) {
            double rotation = time * 480;
            batch.setColor(Color.WHITE);
            batch.draw(assets.oilPot, (float) pot.x() - 8, (float) pot.y() - 10,
                8, 10, 16, 20, 1, 1, (float) rotation);
            for (int i = 1; i <= 3; i++)
                rect(batch, pot.x() - pot.velocityX() * i * .018, pot.y() - pot.velocityY() * i * .018,
                    2, 2, 0xf3ba73, .6f / i);
            return;
        }
        var fire = pot.fireBounds();
        float fade = (float) Math.min(1, pot.life() / .5);
        rect(batch, fire.x(), fire.y(), fire.width(), 3, 0x43332f, fade);
        rect(batch, fire.x(), fire.y() + 1, fire.width(), 24, 0xe68036, .08f * fade);
        for (double x = fire.x(); x + 4 <= fire.right(); x += 6) {
            double height = 9 + 6 * Math.sin(time * 18 + x);
            rect(batch, x, fire.y() + 2, 4, height * fade, 0xb54b30, fade);
            rect(batch, x + 1, fire.y() + 2, 3, height * .8 * fade, 0xee9949, fade);
            rect(batch, x + 1, fire.y() + 2, 2, height * .4 * fade, 0xf6d59b, fade);
        }
        for (int i = 0; i < 5; i++) {
            double age = (time * 1.7 + i * .2) % 1;
            double x = fire.x() + (i + .5) * fire.width() / 5 + Math.sin(age * 5 + i) * 4;
            rect(batch, x, fire.y() + 10 + age * 25, 2, 2, 0xf1b570, (float) ((1 - age) * fade));
        }
    }

    void effects(Batch batch, GameWorld world, float left, boolean interior) {
        for (CombatEffect effect : world.effects()) {
            if (world.level().insideArena(effect.x()) != interior || effect.x() < left - 100 || effect.x() > left + 1060)
                continue;
            double age = effect.age();
            float fade = (float) Math.max(0, 1 - age / .45);
            double x = effect.x(), y = effect.y();
            int color = switch (effect.kind()) {
                case MACE_IMPACT -> 0xb9aa8d;
                case POT_SHATTER -> 0xb87950;
                case FIRE_IGNITE -> 0xf0b36d;
            };
            for (int i = 0; i < 10; i++) {
                double vx = (i - 4.5) * 26, vy = 50 + i % 3 * 35;
                rect(batch, x + vx * age, y + 3 + Math.max(0, vy * age - 180 * age * age),
                    i % 3 == 0 ? 4 : 2, i % 2 == 0 ? 3 : 2, color, fade);
            }
            if (effect.kind() == CombatEffect.Kind.MACE_IMPACT) {
                double spread = age * 110;
                rect(batch, x - spread - 12, y + 1, 12, 3, 0xc6b590, fade * .65f);
                rect(batch, x + spread, y + 1, 12, 3, 0xc6b590, fade * .65f);
            } else if (effect.kind() == CombatEffect.Kind.FIRE_IGNITE) {
                rect(batch, x - 24, y + 2, 48, 30 * fade, 0xee9347, fade * .18f);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private static double ease(double from, double to, double progress) {
        return from + (to - from) * progress * progress * (3 - 2 * progress);
    }

    private void sprite(Batch batch, TextureRegion region, double x, double y, int width, int height, int facing) {
        batch.draw(region, (float) Math.round(x) + (facing < 0 ? width : 0), (float) Math.round(y),
            facing < 0 ? -width : width, height);
    }

    private void rect(Batch batch, double x, double y, double width, double height, int rgb, float alpha) {
        tint.set((rgb << 8) | 0xff);
        tint.a = alpha;
        batch.setColor(tint);
        batch.draw(assets.white, (float) Math.round(x), (float) Math.round(y), (float) width, (float) height);
    }
}
