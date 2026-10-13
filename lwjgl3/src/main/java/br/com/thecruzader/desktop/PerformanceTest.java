package br.com.thecruzader.desktop;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelLoader;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.Hud;
import br.com.thecruzader.presentation.WorldRenderer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import java.util.Arrays;

/** Uncapped desktop measurement; glFinish includes GPU completion, not vsync waiting. */
final class PerformanceTest extends ApplicationAdapter {
    private GameAssets assets;
    private WorldRenderer renderer;
    private Hud hud;
    private final GameWorld[] worlds = new GameWorld[4];
    private final double[] samples = new double[300];
    private int frame, count;
    @Override public void create() {
        br.com.thecruzader.presentation.DisplaySettings.apply(true);
        assets = new GameAssets();
        renderer = new WorldRenderer(assets);
        hud = new Hud(assets);
        LevelData level = LevelLoader.load(Gdx.files.internal(GameAssets.MAP));
        for (int i = 0; i < worlds.length; i++)
            worlds[i] = new GameWorld(new LevelData(level.width(), level.height(),
                    new LevelData.Point(480 + i * 2240, 64), level.checkpoint(), level.breads(),
                    level.gate(), level.exit(), level.arena(), level.solids(), level.enemies()));
    }
    @Override public void resize(int width, int height) {
        renderer.resize(width, height); hud.resize(width, height);
    }
    @Override public void render() {
        GameWorld world = worlds[frame / 90];
        long start = System.nanoTime();
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        renderer.render(world, 1f / 60, 1);
        hud.render(world, 1f / 60, false, renderer.deathElapsed());
        Gdx.gl.glFinish();
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        if (frame % 90 >= 15) samples[count++] = ms;
        if (++frame == 360) {
            Arrays.sort(samples);
            System.out.printf(java.util.Locale.ROOT,
                    "PERFORMANCE: %dx%d framebuffer, median %.3f ms, p95 %.3f ms, max %.3f ms (%d frames)%n",
                    Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
                    samples[count / 2], samples[(int) (count * .95)], samples[count - 1], count);
            Gdx.app.exit();
        }
    }
    @Override public void dispose() { hud.dispose(); renderer.dispose(); assets.dispose(); }
}
