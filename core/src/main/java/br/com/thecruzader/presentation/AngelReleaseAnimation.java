package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.BossRelease;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.level.Bounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

/** Two feathered messengers descend to the portals, bless them open and ascend. */
final class AngelReleaseAnimation {
    private final GameAssets assets;
    private final Color glow = new Color();
    AngelReleaseAnimation(GameAssets assets) { this.assets = assets; }

    void draw(Batch batch, GameWorld world) {
        if (!world.bossReleaseActive() || world.player().health() <= 0) return;
        double elapsed = world.bossReleaseElapsed();
        double descent = smooth(Math.min(1, elapsed / 1.5));
        double ascent = smooth(Math.max(0, (elapsed - BossRelease.OPEN_END)
                / (BossRelease.DURATION - BossRelease.OPEN_END)));
        float opacity = (float) Math.min(1, Math.min(elapsed / .3, (BossRelease.DURATION - elapsed) / .45));
        angel(batch, world.arenaEntrance(), 60, -1, elapsed, descent, ascent, opacity);
        angel(batch, world.level().gate(), -60, 1, elapsed, descent, ascent, opacity);
        batch.setColor(Color.WHITE);
    }

    private void angel(Batch batch, Bounds gate, double offset, int facing, double elapsed,
                       double descent, double ascent, float opacity) {
        double center = gate.x() + gate.width() / 2 + offset;
        double sky = gate.y() + 520;
        double hover = gate.y() + 112;
        double y = sky + (hover - sky) * descent + (sky - hover) * ascent;
        y += Math.round(Math.sin(elapsed * 4 + facing) * 3);
        for (int strip = 0; strip < 4; strip++) {
            int width = 20 + strip * 16;
            rect(batch, center - width / 2, gate.y(), width, 540,
                    glow.set(1, .85f, .52f, opacity * .026f));
        }
        for (int i = 0; i < 14; i++) {
            double px = center + Math.sin(i * 2.3 + elapsed) * (14 + i % 4 * 6);
            double py = gate.y() + (i * 29 + elapsed * 30) % 330;
            rect(batch, px, py, 2, 2, glow.set(1, .91f, .65f, opacity * .55f));
        }
        var pose = assets.angels[(int) (elapsed * 9) % assets.angels.length];
        batch.setColor(1, 1, 1, opacity);
        batch.draw(pose.getTexture(), (float) Math.round(center - 48), (float) Math.round(y),
                96, 112, pose.getRegionX(), pose.getRegionY(), 96, 112, facing < 0, false);
        float opening = BossRelease.opening(elapsed);
        if (opening > 0 && opening < 1) {
            rect(batch, gate.x() - 6, gate.y(), gate.width() + 12, gate.height(),
                    glow.set(1, .88f, .55f, (float) Math.sin(opening * Math.PI) * .24f));
        }
    }

    private static double smooth(double t) { return t * t * (3 - 2 * t); }
    private void rect(Batch batch, double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, Math.round(x), Math.round(y), (float) width, (float) height);
    }
}
