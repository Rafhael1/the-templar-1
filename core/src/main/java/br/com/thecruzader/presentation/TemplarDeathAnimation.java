package br.com.thecruzader.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

/** Shared, solemn player sequence: kneel, sign of the cross, prayer, fall and rising light. */
public final class TemplarDeathAnimation {
    public static final float DURATION = 3.8f;
    private static final float[] POSE_STARTS = {0, .16f, .40f, .66f, .90f, 1.14f, 1.38f, 1.78f, 2.12f, 2.48f, 2.74f, 3.12f};
    private final GameAssets assets;
    private final Color tint = new Color();

    public TemplarDeathAnimation(GameAssets assets) { this.assets = assets; }

    public void draw(Batch batch, double x, double y, int facing, float elapsed, float left, float bottom) {
        float darkness = Math.min(1, elapsed / .5f);
        rect(batch, left, bottom, 960, 540, tint.set(.02f, .025f, .04f, darkness * .60f));
        int bars = Math.round(16 * darkness);
        rect(batch, left, bottom, 960, bars, Color.BLACK);
        rect(batch, left, bottom + 540 - bars, 960, bars, Color.BLACK);
        float light = Math.min(1, Math.max(0, (elapsed - .6f) / .7f));
        for (int strip = 0; strip < 5; strip++) {
            int width = 14 + strip * 12;
            rect(batch, x + 14 - width / 2, y, width, bottom + 540 - y,
                    tint.set(.94f, .80f, .50f, light * .018f));
        }
        int frame = 0;
        while (frame + 1 < POSE_STARTS.length && elapsed >= POSE_STARTS[frame + 1]) frame++;
        var pose = assets.templarDeath[frame];
        batch.setColor(Color.WHITE);
        batch.draw(pose.getTexture(), (float) Math.round(x - 50), (float) Math.round(y - 9),
                128, 112, pose.getRegionX(), pose.getRegionY(), 128, 112, facing < 0, false);
        // Light rises; the body stays on the floor, with no gameplay resurrection implied.
        float rise = Math.max(0, Math.min(1, (elapsed - 2.1f) / 1.7f));
        if (rise > 0) {
            double cx = x + 14, cy = y + 74 + Math.round(rise * 84);
            float glow = Math.min(1, rise * 4) * (1 - rise * .45f);
            rect(batch, cx - 7, cy - 4, 14, 32, tint.set(1, .76f, .38f, glow * .08f));
            rect(batch, cx - 2, cy, 4, 26, tint.set(.90f, .71f, .40f, glow));
            rect(batch, cx - 9, cy + 16, 18, 4, tint.set(.90f, .71f, .40f, glow));
            rect(batch, cx, cy + 2, 1, 22, tint.set(1, .93f, .73f, glow));
            for (int i = 0; i < 10; i++) {
                double px = cx + Math.sin(i * 2.4 + rise * 2) * (10 + i % 4 * 5);
                double py = y + 38 + (i * 13 + rise * 110) % 135;
                rect(batch, px, py, 2, 2, tint.set(1, .88f, .58f, glow * .6f));
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void rect(Batch batch, double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, Math.round(x), Math.round(y), (float) width, (float) height);
    }
}
