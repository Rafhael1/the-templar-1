package br.com.thecruzader.desktop;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.presentation.LevelResources;
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
    private LevelResources resources;
    private Hud hud;
    private GameWorld[] worlds;
    private double[] samples;
    private int frame, count;
    @Override public void create() {
        br.com.thecruzader.presentation.DisplaySettings.apply(true);
        assets = new GameAssets();
        var definition = CampaignLoader.load(Gdx.files.internal(CampaignLoader.PATH)).levels().getFirst();
        resources = new LevelResources(definition, assets);
        renderer = new WorldRenderer(assets, resources);
        hud = new Hud(assets, definition, false);
        LevelData level = resources.data();
        worlds = new GameWorld[definition.regions().size() + 1];
        samples = new double[worlds.length * 75];
        for (int i = 0; i < definition.regions().size(); i++)
            worlds[i] = new GameWorld(new LevelData(level.width(), level.height(),
                    new LevelData.Point(Math.min(level.width() - br.com.thecruzader.gameplay.Player.WIDTH, 480 + definition.regions().get(i).start()), level.spawn().y()), level.checkpoint(), level.bandages(),
                    level.gate(), level.exit(), level.arena(), level.solids(), level.enemies()));
        worlds[worlds.length - 1] = new GameWorld(new LevelData(level.width(), level.height(),
                new LevelData.Point(level.arena().x() + 80, level.arena().y()), level.checkpoint(), level.bandages(),
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
        if (++frame == worlds.length * 90) {
            Arrays.sort(samples);
            System.out.printf(java.util.Locale.ROOT,
                    "PERFORMANCE: %dx%d framebuffer, median %.3f ms, p95 %.3f ms, max %.3f ms (%d frames)%n",
                    Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
                    samples[count / 2], samples[(int) (count * .95)], samples[count - 1], count);
            Gdx.app.exit();
        }
    }
    @Override public void dispose() { hud.dispose(); renderer.dispose(); resources.dispose(); assets.dispose(); }
}
