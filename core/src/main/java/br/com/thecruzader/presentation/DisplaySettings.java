package br.com.thecruzader.presentation;

import com.badlogic.gdx.Gdx;

/** Window presets are expressed in framebuffer pixels, including Retina scaling. */
public final class DisplaySettings {
    private DisplaySettings() {}
    public static boolean fullHd() {
        return Gdx.graphics.getBackBufferWidth() == 1920 && Gdx.graphics.getBackBufferHeight() == 1080;
    }
    public static void apply(boolean fullHd) {
        double scaleX = (double) Gdx.graphics.getBackBufferWidth() / Gdx.graphics.getWidth();
        double scaleY = (double) Gdx.graphics.getBackBufferHeight() / Gdx.graphics.getHeight();
        int width = (int) Math.round((fullHd ? 1920 : 1280) / scaleX);
        int height = (int) Math.round((fullHd ? 1080 : 720) / scaleY);
        if (!Gdx.graphics.setWindowedMode(width, height))
            throw new IllegalStateException("Could not apply window resolution");
    }
}
