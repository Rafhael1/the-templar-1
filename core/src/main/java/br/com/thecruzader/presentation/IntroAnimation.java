package br.com.thecruzader.presentation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Layered scene choreography on a 480 x 200 pixel grid. The screen owns the clock. */
public final class IntroAnimation extends Actor {
    private static final int[] WINDOWS = {72, 160, 322, 410};
    private static final int[] KNIGHTS = {53, 98, 145, 273, 320, 366};
    private final GameAssets assets;
    private final TextureRegion[] pope, banners, kneeling, marchers;
    private final Color tint = new Color();
    private int scene;
    private float elapsed, opacity;

    public IntroAnimation(GameAssets assets) {
        this.assets = assets;
        pope = TextureRegion.split(assets.texture("intro/pope"), 68, 88)[0];
        banners = TextureRegion.split(assets.texture("intro/banner"), 32, 80)[0];
        kneeling = TextureRegion.split(assets.texture("intro/kneeling"), 64, 80)[0];
        marchers = TextureRegion.split(assets.texture("intro/marchers"), 32, 40)[0];
    }

    public void setFrame(int scene, float elapsed) {
        this.scene = scene;
        this.elapsed = elapsed;
    }

    @Override public void draw(Batch batch, float parentAlpha) {
        batch.flush();
        if (!clipBegin()) return;
        opacity = parentAlpha * Math.min(1, elapsed * 3);
        try {
            if (scene == 2) march(batch);
            else council(batch, scene == 1);
        } finally {
            batch.setColor(Color.WHITE);
            batch.flush();
            clipEnd();
        }
    }

    private void council(Batch batch, boolean close) {
        int zoom = close ? 2 : 1;
        int offsetX = close ? -240 : 0, offsetY = close ? -96 : 0;
        image(batch, assets.texture("intro/chamber"), offsetX, offsetY, 480 * zoom, 200 * zoom);
        // Soft window light is made of stepped pixel strips, staying behind the figures.
        for (int window : WINDOWS) {
            for (int strip = 0; strip < 8; strip++) {
                int x = (window + strip * 2) * zoom + offsetX;
                rect(batch, x, (93 - strip * 8) * zoom + offsetY, (4 + strip) * zoom, 8 * zoom,
                        .78f, .72f, .58f, .025f);
            }
        }
        int bannerFrame = (int) (elapsed * 8) % banners.length;
        sprite(batch, banners[bannerFrame], 173 * zoom + offsetX, 90 * zoom + offsetY, 32 * zoom, 80 * zoom, false);
        sprite(batch, banners[(bannerFrame + 3) % banners.length], 276 * zoom + offsetX, 90 * zoom + offsetY,
                32 * zoom, 80 * zoom, false);
        sprite(batch, pope[(int) (elapsed * 2) % pope.length], 205 * zoom + offsetX, 48 * zoom + offsetY,
                68 * zoom, 88 * zoom, false);
        if (close) {
            rect(batch, 0, 0, 480, 2, .25f, .22f, .26f, 1);
            // The two witnesses lower themselves in sequence while the blessing stays in focus.
            for (int i = 0; i < 2; i++) {
                int pose = Math.max(0, Math.min(3, (int) ((elapsed - .5f - i * .25f) * 4)));
                sprite(batch, kneeling[pose], i == 0 ? -8 : 366, -14, 128, 160, i == 1);
            }
        } else {
            for (int i = 0; i < KNIGHTS.length; i++) {
                int breath = (int) (elapsed * 2 + i) % 8;
                sprite(batch, assets.characters[0][breath], KNIGHTS[i], i % 3 == 2 ? 15 : 19, 64, 80, i >= 3);
            }
        }
        candle(batch, 190 * zoom + offsetX, 41 * zoom + offsetY, zoom, 0);
        candle(batch, 285 * zoom + offsetX, 41 * zoom + offsetY, zoom, 2);
    }

    private void candle(Batch batch, int x, int y, int zoom, int phase) {
        int frame = (int) (elapsed * 10 + phase) % 4;
        rect(batch, x - 8 * zoom, y + 31 * zoom, 21 * zoom, 23 * zoom, 1, .63f, .3f, .055f + frame * .01f);
        rect(batch, x, y, 4 * zoom, 27 * zoom, .40f, .32f, .27f, 1);
        rect(batch, x - 4 * zoom, y, 13 * zoom, 3 * zoom, .78f, .67f, .47f, 1);
        rect(batch, x, y + 27 * zoom, 4 * zoom, 9 * zoom, .87f, .85f, .76f, 1);
        rect(batch, x + (frame % 2) * zoom, y + 36 * zoom, 3 * zoom, (5 + frame) * zoom, .95f, .64f, .31f, 1);
        rect(batch, x + zoom, y + 36 * zoom, zoom, 4 * zoom, 1, .89f, .63f, 1);
        for (int i = 0; i < 3; i++) {
            int rise = (int) (elapsed * 9 + i * 13 + phase * 7) % 38;
            rect(batch, x + ((i % 2) * 3 - 1) * zoom, y + (42 + rise) * zoom, zoom, zoom,
                    .96f, .76f, .44f, (1 - rise / 38f) * .5f);
        }
    }

    private void march(Batch batch) {
        scroll(batch, assets.texture("intro/march-sky"), (int) (elapsed * 2) % 480);
        // Clouds and the wall travel at different depths from the marching foreground.
        for (int i = 0; i < 4; i++) {
            int x = (int) (i * 137 + elapsed * 4) % 620 - 100;
            rect(batch, x, 146 + (i % 3) * 13, 85, 3, .79f, .75f, .68f, .14f);
            rect(batch, x + 15, 149 + (i % 3) * 13, 49, 2, .79f, .75f, .68f, .09f);
        }
        scroll(batch, assets.texture("intro/march-walls"), (int) (elapsed * 7) % 480);
        rect(batch, 0, 0, 480, 12, .25f, .22f, .26f, 1);
        rect(batch, 0, 11, 480, 2, .56f, .48f, .39f, 1);
        for (int i = 0; i < 11; i++) {
            int x = (int) (i * 55 + elapsed * 16) % 605 - 65;
            int frame = (int) (elapsed * 8 + i * 2) % marchers.length;
            if (i % 4 == 1) {
                rect(batch, x + 22, 19, 1, 63, .70f, .60f, .42f, 1);
                sprite(batch, banners[(int) (elapsed * 8 + i) % banners.length], x + 22, 42, 16, 40, false);
            }
            sprite(batch, marchers[frame], x, 9 + i % 2, 32, 40, false);
            int dust = (int) (elapsed * 18 + i * 7) % 18;
            rect(batch, x - dust, 11 + dust / 4, 2, 1, .70f, .58f, .41f, (1 - dust / 18f) * .6f);
        }
        // A nearer soldier and banner cross the frame at a larger, still integer pixel scale.
        int foregroundX = (int) (elapsed * 24) % 620 - 90;
        sprite(batch, assets.characters[1][(int) (elapsed * 8) % 8], foregroundX, -18, 64, 80, false);
    }

    private void scroll(Batch batch, Texture texture, int offset) {
        image(batch, texture, -offset, 0, 480, 200);
        image(batch, texture, 480 - offset, 0, 480, 200);
    }

    private void image(Batch batch, Texture texture, int x, int y, int width, int height) {
        batch.setColor(1, 1, 1, opacity);
        batch.draw(texture, getX() + x * 2, getY() + y * 2, width * 2, height * 2);
    }

    private void sprite(Batch batch, TextureRegion texture, int x, int y, int width, int height, boolean flip) {
        batch.setColor(1, 1, 1, opacity);
        batch.draw(texture, getX() + (x + (flip ? width : 0)) * 2, getY() + y * 2,
                (flip ? -width : width) * 2, height * 2);
    }

    private void rect(Batch batch, int x, int y, int width, int height, float r, float g, float b, float alpha) {
        batch.setColor(tint.set(r, g, b, alpha * opacity));
        batch.draw(assets.white, getX() + x * 2, getY() + y * 2, width * 2, height * 2);
    }
}
