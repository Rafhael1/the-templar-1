package br.com.thecruzader.desktop;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.CampaignLoader;
import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.presentation.GameAssets;
import br.com.thecruzader.presentation.Hud;
import br.com.thecruzader.presentation.LevelResources;
import br.com.thecruzader.presentation.WorldRenderer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Locale;

/**
 * Active encounters and camera movement, with separate work and presentation measurements.
 */
final class PerformanceTest extends ApplicationAdapter {
    private static final int WARMUP = 120, SAMPLES = 360, FRAMES = WARMUP + SAMPLES;
    private static final String[] SCENES = {"camp", "aqueduct", "ruins", "sanctuary", "gallery", "ridge", "chapel", "arena"};
    private final boolean paced;
    private final double[] simulation = new double[SAMPLES], render = new double[SAMPLES];
    private final double[] work = new double[SAMPLES], interval = new double[SAMPLES];
    private final com.sun.management.ThreadMXBean threads =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    private GameAssets assets;
    private WorldRenderer renderer;
    private LevelResources resources;
    private GLProfiler profiler;
    private Hud hud;
    private GameWorld[] worlds;
    private double[] anchors;
    private int frame, count, respawns;
    private long previousStart, allocated, draws, bindings, gcStart;

    PerformanceTest(boolean paced) {
        this.paced = paced;
    }

    @Override public void create() {
        threads.setThreadAllocatedMemoryEnabled(true);
        br.com.thecruzader.presentation.DisplaySettings.apply(true);
        assets = new GameAssets();
        var definition = CampaignLoader.load(Gdx.files.internal(CampaignLoader.PATH)).levels().getFirst();
        resources = new LevelResources(definition, assets);
        renderer = new WorldRenderer(assets, resources);
        LevelData level = resources.data();
        anchors = new double[]{480, 2150, 3470, 4400, 4910, 6550, 7480, level.arena().x() + 80};
        worlds = new GameWorld[anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            double x = anchors[i];
            double y = level.solids().stream().filter(s -> s.x() <= x && s.right() >= x + LevelData.PLAYER_WIDTH)
                .mapToDouble(br.com.thecruzader.level.Bounds::top).max().orElseThrow();
            worlds[i] = new GameWorld(new LevelData(level.width(), level.height(), new LevelData.Point(x, y),
                level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(), level.enemies()));
        }
        hud = new Hud(assets, definition, false, worlds[0].player());
        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();
        System.out.printf(Locale.ROOT, "PROFILE: paced=%s, framebuffer=%dx%d, refresh=%d Hz, GPU=%s%n",
            paced, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
            Gdx.graphics.getDisplayMode().refreshRate, Gdx.gl.glGetString(com.badlogic.gdx.graphics.GL20.GL_RENDERER));
    }

    @Override public void resize(int width, int height) {
        renderer.resize(width, height); hud.resize(width, height);
    }

    @Override public void render() {
        int scene = frame / FRAMES, local = frame % FRAMES;
        GameWorld world = worlds[scene];
        long start = System.nanoTime();
        double frameInterval = previousStart == 0 ? 0 : (start - previousStart) / 1_000_000.0;
        previousStart = start;
        long bytesBefore = threads.getThreadAllocatedBytes(Thread.currentThread().threadId());
        if (local == WARMUP) gcStart = collections();
        profiler.reset();
        if (world.state() == GameWorld.State.DEAD) {
            world.respawn();
            respawns++;
        }
        double displacement = world.player().x() - anchors[scene];
        int direction = displacement > 180 ? -1 : displacement < -180 ? 1 : (local / 90 % 2 == 0 ? 1 : -1);
        var input = new PlayerInput(direction, local % 50 == 0, local % 25 == 0,
            local % 110 == 0, false, local % 90 > 65);
        world.update(GameWorld.STEP, input);
        world.update(GameWorld.STEP, PlayerInput.NONE);
        world.drainEvents();
        long simulated = System.nanoTime();
        renderer.render(world, 1f / 60, 1);
        hud.render(world, 1f / 60, false, renderer.deathElapsed());
        long rendered = System.nanoTime();
        if (!paced) Gdx.gl.glFinish();
        long completed = System.nanoTime();
        if (local >= WARMUP) {
            simulation[count] = (simulated - start) / 1_000_000.0;
            render[count] = (rendered - simulated) / 1_000_000.0;
            work[count] = (completed - start) / 1_000_000.0;
            interval[count++] = frameInterval;
            allocated += threads.getThreadAllocatedBytes(Thread.currentThread().threadId()) - bytesBefore;
            draws += profiler.getDrawCalls();
            bindings += profiler.getTextureBindings();
        }
        frame++;
        if (frame % FRAMES == 0) {
            report(SCENES[scene]);
            if (frame == worlds.length * FRAMES) Gdx.app.exit();
        }
    }

    private void report(String scene) {
        Arrays.sort(simulation);
        Arrays.sort(render);
        Arrays.sort(work);
        Arrays.sort(interval);
        System.out.printf(Locale.ROOT,
            "PROFILE %s: sim.p95=%.3f render.p95=%.3f work.p50/p95/p99/max=%.3f/%.3f/%.3f/%.3f ms; "
                + "interval.p50/p95/p99/max=%.3f/%.3f/%.3f/%.3f ms; alloc=%.1f KB/frame draws=%.1f binds=%.1f GC=%d respawns=%d%n",
            scene, simulation[(int) (count * .95)], render[(int) (count * .95)],
            work[count / 2], work[(int) (count * .95)], work[(int) (count * .99)], work[count - 1],
            interval[count / 2], interval[(int) (count * .95)], interval[(int) (count * .99)], interval[count - 1],
            allocated / (1024.0 * count), (double) draws / count, (double) bindings / count,
            collections() - gcStart, respawns);
        count = 0;
        allocated = 0;
        draws = 0;
        bindings = 0;
        respawns = 0;
    }

    private static long collections() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream().mapToLong(bean -> bean.getCollectionCount()).sum();
    }

    @Override
    public void dispose() {
        profiler.disable();
        hud.dispose();
        renderer.dispose();
        resources.dispose();
        assets.dispose();
    }
}
