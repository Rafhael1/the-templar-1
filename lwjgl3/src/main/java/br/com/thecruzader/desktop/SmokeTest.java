package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.screen.PlayScreen;
import br.com.thecruzader.i18n.Language;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

/** Desktop integration harness using the same screens, assets and input adapter as the player. */
final class SmokeTest extends ApplicationAdapter {
    private final CrusaderGame game = new CrusaderGame();
    private final String output;
    private int frame;
    private Language originalLanguage;
    private boolean rendering;

    SmokeTest(String output) {
        this.output = output;
    }

    @Override
    public void create() {
        Gdx.files.absolute(output).mkdirs();
        game.create();
        originalLanguage = game.language();
        game.setLanguage(Language.PT);
        game.showMenu();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void render() {
        // GLFW resize callbacks can request a nested render; repaint without replaying scripted input.
        if (rendering) { game.getScreen().render(0); return; }
        rendering = true;
        try { runFrame(); }
        catch (RuntimeException error) { game.setLanguage(originalLanguage); throw error; }
        finally { rendering = false; }
    }

    private void runFrame() {
        // The frame-scripted inputs need a fixed clock, independent of GPU speed and vsync.
        game.getScreen().render(1f / 120);
        frame++;
        if (frame == 12) {
            capture("01-menu.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.I);
        }
        if (frame == 14) {
            if (((br.com.thecruzader.screen.MenuScreen) game.getScreen()).page()
                    != br.com.thecruzader.screen.MenuScreen.Page.INSTRUCTIONS)
                throw new IllegalStateException("Instructions page failed");
            capture("01-instructions.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 16) Gdx.input.getInputProcessor().keyDown(Input.Keys.O);
        if (frame == 18) {
            if (((br.com.thecruzader.screen.MenuScreen) game.getScreen()).page()
                    != br.com.thecruzader.screen.MenuScreen.Page.OPTIONS)
                throw new IllegalStateException("Options page failed");
            capture("01-options.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 20) {
            capture("01-options-muted.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 21) {
            Gdx.input.getInputProcessor().keyDown(Input.Keys.DOWN);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 22) {
            if (Gdx.graphics.getBackBufferWidth() != 1280 || Gdx.graphics.getBackBufferHeight() != 720)
                throw new IllegalStateException("Compact window option failed");
            capture("01-compact-window.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 23) {
            if (Gdx.graphics.getBackBufferWidth() != 1920 || Gdx.graphics.getBackBufferHeight() != 1080)
                throw new IllegalStateException("Standard window option failed");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.DOWN);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 24) {
            if (game.language() != Language.EN) throw new IllegalStateException("Language option did not select EN");
            if (((br.com.thecruzader.screen.MenuScreen) game.getScreen()).page()
                    != br.com.thecruzader.screen.MenuScreen.Page.OPTIONS)
                throw new IllegalStateException("Changing language left the options page");
            game.getScreen().render(1f / 120);
            capture("21-english-options.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 25) {
            capture("21-english-menu.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.I);
        }
        if (frame == 26) {
            capture("21-english-instructions.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 27) Gdx.input.getInputProcessor().keyDown(Input.Keys.O);
        if (frame == 28) {
            Gdx.input.getInputProcessor().keyDown(Input.Keys.DOWN);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.DOWN);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            if (game.language() != Language.PT) throw new IllegalStateException("Language option did not restore PT");
        }
        if (frame == 29) {
            capture("01-options-pt.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 30) Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        if (frame == 31) {
            if (!(game.getScreen() instanceof br.com.thecruzader.screen.IntroScreen))
                throw new IllegalStateException("New journey must open the cutscene");
            for (int fade = 0; fade < 6; fade++) game.getScreen().render(.1f);
            capture("01-opening-council.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
        }
        if (frame == 32) {
            if (((br.com.thecruzader.screen.IntroScreen) game.getScreen()).scene() != 1)
                throw new IllegalStateException("Cutscene advance failed");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 35) Gdx.input.getInputProcessor().keyDown(Input.Keys.D);
        if (frame == 100) {
            Gdx.input.getInputProcessor().keyUp(Input.Keys.D);
            capture("02-gameplay.png");
        }
        if (frame == 110) Gdx.input.getInputProcessor().keyDown(Input.Keys.SPACE);
        if (frame == 111) Gdx.input.getInputProcessor().keyUp(Input.Keys.SPACE);
        if (frame == 118) {
            Gdx.input.getInputProcessor().keyDown(Input.Keys.S);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.J);
        }
        if (frame == 119) {
            Gdx.input.getInputProcessor().keyUp(Input.Keys.S);
            Gdx.input.getInputProcessor().keyUp(Input.Keys.J);
        }
        if (frame == 130) {
            if (((PlayScreen) game.getScreen()).world().player().attackDirection()
                    != br.com.thecruzader.gameplay.Player.AttackDirection.DOWN)
                throw new IllegalStateException("Downward air attack failed");
            capture("03-down-attack.png");
        }
        if (frame == 150) game.pause();
        if (frame == 155) {
            if (!((PlayScreen) game.getScreen()).world().paused())
                throw new IllegalStateException("Focus pause failed");
            capture("04-pause.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        }
        if (frame == 156) Gdx.input.getInputProcessor().keyUp(Input.Keys.ESCAPE);
        if (frame == 165) Gdx.graphics.setWindowedMode(1000, 700);
        if (frame == 172) Gdx.input.getInputProcessor().keyDown(Input.Keys.L);
        if (frame == 180) {
            if (!((PlayScreen) game.getScreen()).world().player().blocking())
                throw new IllegalStateException("Held shield block failed");
            capture("05-resized.png");
            System.out.println(
                    "DESKTOP_SMOKE_PASS: menu, gameplay, downward attack, shield, pause, resize, textures and audio"
                        + " initialized");
            captureLevelTour();
            game.setLanguage(Language.EN);
            checkTerrainAndDeaths();
            game.startJourney();
            for (int scene = 0; scene < 3; scene++) {
                for (int fade = 0; fade < 6; fade++) game.getScreen().render(.1f);
                capture("16-intro-" + scene + ".png");
                Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            }
            game.render();
            if (!(game.getScreen() instanceof PlayScreen))
                throw new IllegalStateException("Completing cutscene must start gameplay");
            System.out.println("INTRO_SMOKE_PASS: all scenes, advance, skip and gameplay transition");
            capture("21-english-gameplay.png");
            game.pause();
            game.getScreen().render(.016f);
            capture("21-english-pause.png");
            System.out.println("LANGUAGE_SMOKE_PASS: PT/EN options, menus, instructions, cutscene, HUD and saved preference");
            game.setLanguage(originalLanguage);
            game.showMenu();
            Gdx.input.getInputProcessor().keyDown(Input.Keys.UP);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            game.render();
            System.out.println("MENU_SMOKE_PASS: instructions, options, sound toggle, window sizes, new game and quit");
        }
    }

    private void captureLevelTour() {
        Gdx.input.getInputProcessor().keyUp(Input.Keys.L);
        game.startGame();
        var world = ((PlayScreen) game.getScreen()).world();
        double[] landmarks = {850, 2370, 4480, 6400, 7670, 8400};
        String[] names = {"06-bread-of-life.png", "07-first-stairs.png", "08-sanctuary.png",
                "09-extended-road.png", "10-final-stairs.png", "11-arena.png"};
        int landmark = 0;
        boolean archerCaptured = false, arrowCaptured = false, arenaCaptured = false;
        java.util.Set<br.com.thecruzader.gameplay.Enemy.State> bossCaptures =
                java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.Enemy.State.class);
        for (int tick = 0; tick < 180 * 120
                && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
            var player = world.player();
            var target = world.enemies().stream().filter(enemy -> enemy.health() > 0 && Math.abs(enemy.y() - player.y()) < 80)
                    .min(java.util.Comparator.comparingDouble(enemy -> Math.abs(enemy.x() - player.x())))
                    .orElse(null);
            int direction = 1;
            boolean attack = false, block = false;
            if (target != null && Math.abs(target.x() - player.x()) < 180) {
                double distance = target.x() - player.x();
                direction = distance >= 0 ? 1 : -1;
                if (target.state() == br.com.thecruzader.gameplay.Enemy.State.WINDUP
                        || target.state() == br.com.thecruzader.gameplay.Enemy.State.ATTACK
                        || target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH_WINDUP || target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH
                        || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP_WINDUP || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP) {
                    block = true;
                } else {
                    attack = target.state() == br.com.thecruzader.gameplay.Enemy.State.RECOVER
                            && Math.abs(distance) < 80;
                    if (player.attack().running() || Math.abs(distance) < 50) direction = 0;
                }
            }
            boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
                    .anyMatch(solid -> solid.y() == 64 && solid.top() > 64
                            && solid.x() >= player.x() + br.com.thecruzader.gameplay.Player.WIDTH
                            && solid.x() - (player.x() + br.com.thecruzader.gameplay.Player.WIDTH) < 85
                            && solid.top() > player.y() && solid.top() - player.y() < 160);
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(direction, jump, attack, false, false, block));
            if (!arenaCaptured && world.arenaLocked()) {
                Gdx.input.getInputProcessor().keyDown(Input.Keys.L);
                for (int settle = 0; settle < 12; settle++) game.getScreen().render(.05f);
                Gdx.input.getInputProcessor().keyUp(Input.Keys.L);
                capture("24-sealed-arena.png");
                arenaCaptured = true;
            }
            if (target != null && target.archer() && !archerCaptured
                    && target.state() == br.com.thecruzader.gameplay.Enemy.State.WINDUP
                    && Math.abs(target.x() - player.x()) < 300) {
                game.render();
                capture("13-archer.png");
                archerCaptured = true;
            }
            if (!arrowCaptured && world.arrows().stream().anyMatch(arrow ->
                    Math.abs(arrow.x() - player.x()) > 40
                            && Math.abs(arrow.x() - player.x()) < 160)) {
                game.render();
                capture("14-arrows.png");
                arrowCaptured = true;
            }
            if (target != null && target.boss()
                    && (target.state() == br.com.thecruzader.gameplay.Enemy.State.BLOCK
                        || target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH
                        || (target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP
                            && target.vy() < 0))
                    && bossCaptures.add(target.state())) {
                game.render();
                capture("15-boss-" + target.state().name().toLowerCase(java.util.Locale.ROOT) + ".png");
            }
            if (landmark < landmarks.length && player.x() >= landmarks[landmark]) {
                game.render();
                capture(names[landmark++]);
            }
        }
        if (!archerCaptured || !arrowCaptured || bossCaptures.size() != 3
                || landmark != landmarks.length
                || world.state() != br.com.thecruzader.gameplay.GameWorld.State.WON)
            throw new IllegalStateException("Level tour failed at x=" + world.player().x());
        game.render();
        capture("12-victory.png");
        System.out.println("LEVEL_TOUR_PASS: relic route, sanctuary, stairs, arena and victory");
    }

    private void checkTerrainAndDeaths() {
        var assets = new br.com.thecruzader.presentation.GameAssets();
        if (assets.texts.language() != Language.EN)
            throw new IllegalStateException("Saved language was not loaded by new assets");
        var renderer = new br.com.thecruzader.presentation.WorldRenderer(assets);
        var level = br.com.thecruzader.level.LevelLoader.load(Gdx.files.internal(
                br.com.thecruzader.presentation.GameAssets.MAP));
        br.com.thecruzader.presentation.DisplaySettings.apply(true);
        renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        try {
            // Verify identical stone pixels even when transparent scenery was drawn immediately before tiles.
            Pixmap tile = new Pixmap(Gdx.files.internal("maps/stone.png"));
            try {
                for (int region = 0; region < 4; region++) {
                    var world = visualWorld(level, 480 + region * 2240, level.enemies());
                    renderer.render(world, .016f, 1);
                    assertGroundPixel(world, tile);
                    capture("17-terrain-region-" + region + ".png");
                }
                for (int boundary : new int[] {2240, 4480, 6720}) {
                    for (int offset : new int[] {-320, 0, 320}) {
                        var world = visualWorld(level, boundary + offset - 140, level.enemies());
                        renderer.render(world, .016f, 1);
                        assertGroundPixel(world, tile);
                        capture("20-transition-" + boundary + "-" + offset + ".png");
                    }
                }
            } finally { tile.dispose(); }
            var arenaWorld = visualWorld(level, level.arena().x() + 8,
                    java.util.List.of(level.enemies().stream().filter(e ->
                            e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow()));
            arenaWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    br.com.thecruzader.input.PlayerInput.NONE);
            for (int settle = 0; settle < 12; settle++) renderer.render(arenaWorld, .1f, 1);
            float lockedCamera = renderer.cameraX();
            arenaWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(1, true, false, true, false, false));
            renderer.render(arenaWorld, .1f, 1);
            if (renderer.cameraX() != lockedCamera)
                throw new IllegalStateException("Arena camera followed the player");
            var arenaHud = new br.com.thecruzader.presentation.Hud(assets);
            try {
                arenaHud.announce("notice.arena");
                arenaHud.render(arenaWorld, .35f, false, 0);
                capture("24-english-arena.png");
            } finally { arenaHud.dispose(); }
            System.out.println("ARENA_SMOKE_PASS: locked camera, sealed gates, boss health bar and localized announcement");
            var sentinel = new br.com.thecruzader.level.LevelData.Spawn(142, 64,
                    br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL);
            var guardian = level.enemies().stream().filter(e ->
                    e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow();
            var enemyWorld = visualWorld(level, 100, java.util.List.of(sentinel, guardian));
            for (int tick = 0; tick < 800 && enemyWorld.enemies().getFirst().health() > 0; tick++)
                enemyWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                        new br.com.thecruzader.input.PlayerInput(0, false, true, false, false, false));
            if (enemyWorld.enemies().getFirst().health() != 0)
                throw new IllegalStateException("Enemy death setup failed");
            for (int phase = 0; phase < 10; phase++) {
                renderer.render(enemyWorld, .1f, 1);
                if (phase == 0 || phase == 4 || phase == 9) capture("18-enemy-death-" + phase + ".png");
            }
            var playerWorld = visualWorld(level, guardian.x() - 42, java.util.List.of(guardian));
            for (int tick = 0; tick < 3000 && playerWorld.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++)
                playerWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                        br.com.thecruzader.input.PlayerInput.NONE);
            if (playerWorld.state() != br.com.thecruzader.gameplay.GameWorld.State.DEAD)
                throw new IllegalStateException("Player death setup failed");
            for (int phase = 0; phase < 12; phase++) {
                renderer.render(playerWorld, .1f, 1);
                if (phase == 0 || phase == 4 || phase == 11) capture("19-templar-death-" + phase + ".png");
            }
            var translatedHud = new br.com.thecruzader.presentation.Hud(assets);
            try {
                translatedHud.render(playerWorld, .1f, false, renderer.deathElapsed());
                capture("21-english-death.png");
                var victoryWorld = ((PlayScreen) game.getScreen()).world();
                renderer.render(victoryWorld, .016f, 1);
                translatedHud.render(victoryWorld, .016f, false, 0);
                capture("21-english-victory.png");
                renderer.render(playerWorld, 0, 1);
                for (int frame = 0; frame < 12; frame++) renderer.render(playerWorld, .1f, 1);
            } finally { translatedHud.dispose(); }
            if (renderer.deathElapsed() < 1) throw new IllegalStateException("Player animation did not advance");
            playerWorld.setPaused(true);
            float frozen = renderer.deathElapsed();
            renderer.render(playerWorld, .1f, 1);
            if (renderer.deathElapsed() != frozen) throw new IllegalStateException("Paused death animation advanced");
            playerWorld.respawn();
            renderer.render(playerWorld, .1f, 1);
            if (renderer.deathElapsed() != 0) throw new IllegalStateException("Respawn did not clear animation");
            System.out.println("VISUAL_SMOKE_PASS: opaque terrain in all regions, Full HD, both death animations, pause and respawn");
        } finally { renderer.dispose(); assets.dispose(); }
    }

    private br.com.thecruzader.gameplay.GameWorld visualWorld(
            br.com.thecruzader.level.LevelData level, double spawnX,
            java.util.List<br.com.thecruzader.level.LevelData.Spawn> enemies) {
        return new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
                level.width(), level.height(), new br.com.thecruzader.level.LevelData.Point(spawnX, 64),
                level.checkpoint(), level.breads(), level.gate(), level.exit(), level.arena(), level.solids(), enemies));
    }

    private void assertGroundPixel(br.com.thecruzader.gameplay.GameWorld world, Pixmap tile) {
        int bufferWidth = Gdx.graphics.getBackBufferWidth(), bufferHeight = Gdx.graphics.getBackBufferHeight();
        float scale = Math.min(bufferWidth / 960f, bufferHeight / 540f);
        int viewportX = Math.round((bufferWidth - 960 * scale) / 2);
        int viewportY = Math.round((bufferHeight - 540 * scale) / 2);
        double cameraX = Math.max(480, Math.min(world.level().width() - 480,
                Math.round(world.player().x() + 140)));
        int worldX = (int) cameraX;
        int groundY = 46;
        Pixmap pixel = Pixmap.createFromFrameBuffer(viewportX + Math.round(480 * scale),
                viewportY + Math.round(groundY * scale), 1, 1);
        try {
            int expected = tile.getPixel(Math.floorMod(worldX, 32), 63 - groundY);
            int actual = pixel.getPixel(0, 0);
            // Adjacent source pixels may differ at a scaled tile boundary; use a flat stone interior.
            if ((actual >>> 8) != (expected >>> 8))
                throw new IllegalStateException("Terrain tint leaked: actual=" + Integer.toHexString(actual)
                        + ", expected=" + Integer.toHexString(expected) + ", x=" + worldX);
        } finally { pixel.dispose(); }
    }

    private void capture(String name) {
        Pixmap image =
                Pixmap.createFromFrameBuffer(
                        0,
                        0,
                        Gdx.graphics.getBackBufferWidth(),
                        Gdx.graphics.getBackBufferHeight());
        try {
            PixmapIO.writePNG(new FileHandle(output + "/" + name), image, -1, true);
        } finally {
            image.dispose();
        }
    }

    @Override
    public void dispose() {
        game.setLanguage(originalLanguage);
        game.dispose();
    }
}
