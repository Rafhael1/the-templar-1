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
    private final CrusaderGame game = new CrusaderGame(br.com.thecruzader.level.CampaignLoader.PATH);
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
            captureEnemyPursuit();
            checkPlayerDeathControls();
            game.startJourney();
            Gdx.files.absolute(output + "/intro-frames").mkdirs();
            for (int scene = 0; scene < 3; scene++) {
                var intro = (br.com.thecruzader.screen.IntroScreen) game.getScreen();
                if (intro.scene() != scene) throw new IllegalStateException("Wrong intro scene");
                for (int fade = 0; fade < 6; fade++) intro.render(.1f);
                capture("16-intro-" + scene + ".png");
                long early = frameHash();
                for (int motion = 0; motion < 40; motion++) {
                    intro.render(.1f);
                    captureIntroFrame("intro-frames/scene-" + scene + "-" + String.format(java.util.Locale.ROOT, "%02d", motion) + ".png");
                }
                if (frameHash() == early) throw new IllegalStateException("Intro scene did not animate: " + scene);
                intro.pause();
                long frozen = frameHash();
                for (int pause = 0; pause < 6; pause++) intro.render(.1f);
                if (frameHash() != frozen) throw new IllegalStateException("Paused intro moved: " + scene);
                intro.resume();
                for (int resume = 0; resume < 6; resume++) intro.render(.1f);
                if (frameHash() == frozen) throw new IllegalStateException("Intro did not resume: " + scene);
                capture("26-animated-intro-" + scene + ".png");
                if (scene == 0) Gdx.input.getInputProcessor().keyDown(Input.Keys.SPACE);
                else if (scene == 1) Gdx.input.getInputProcessor().touchDown(100, 100, 0, Input.Buttons.LEFT);
                else Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            }
            game.render();
            if (!(game.getScreen() instanceof PlayScreen))
                throw new IllegalStateException("Completing cutscene must start gameplay");
            System.out.println("INTRO_SMOKE_PASS: animated pixels in all three scenes, pause/resume, space/click/enter, skip and gameplay transition");
            capture("21-english-gameplay.png");
            game.pause();
            game.getScreen().render(.016f);
            capture("21-english-pause.png");
            System.out.println("LANGUAGE_SMOKE_PASS: PT/EN options, menus, instructions, cutscene, HUD and saved preference");
            checkCampaignTransition();
            game.setLanguage(originalLanguage);
            game.showMenu();
            Gdx.input.getInputProcessor().keyDown(Input.Keys.UP);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            game.render();
            System.out.println("MENU_SMOKE_PASS: instructions, options, sound toggle, window sizes, new game and quit");
        }
    }

    private void captureEnemyPursuit() {
        var assets = new br.com.thecruzader.presentation.GameAssets();
        var resources = new br.com.thecruzader.presentation.LevelResources(game.campaign().levels().getFirst(), assets);
        var renderer = new br.com.thecruzader.presentation.WorldRenderer(assets, resources);
        var hud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false);
        try {
            renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            hud.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            var level = resources.data();
            var guardian = level.enemies().stream().filter(e ->
                    e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow();
            var world = new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
                    level.width(), level.height(), new br.com.thecruzader.level.LevelData.Point(520, 160),
                    level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(),
                    java.util.List.of(new br.com.thecruzader.level.LevelData.Spawn(300, 64,
                            br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL), guardian)));
            var enemy = world.enemies().getFirst();
            boolean noticed = false, launched = false, landed = false;
            renderer.render(world, 0, 1);
            hud.render(world, 0, false, 0);
            capture("27-platform-pursuit-before.png");
            for (int tick = 0; tick < 1200; tick++) {
                world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                        new br.com.thecruzader.input.PlayerInput(0, false, false, false, false, true));
                if (!noticed && enemy.state() == br.com.thecruzader.gameplay.Enemy.State.NOTICE) {
                    renderer.render(world, .016f, 1);
                    hud.render(world, .016f, false, 0);
                    capture("30-enemy-notice.png");
                    noticed = true;
                }
                if (!launched && enemy.vy() < 0 && enemy.y() > 165) {
                    renderer.render(world, .016f, 1);
                    hud.render(world, .016f, false, 0);
                    capture("27-platform-pursuit-jump.png");
                    launched = true;
                }
                if (!landed && enemy.y() == 160 && enemy.vy() == 0) {
                    renderer.render(world, .016f, 1);
                    hud.render(world, .016f, false, 0);
                    capture("27-platform-pursuit-landed.png");
                    landed = true;
                }
            }
            if (!noticed || !launched || !landed || world.state() != br.com.thecruzader.gameplay.GameWorld.State.PLAYING)
                throw new IllegalStateException("Enemy failed rendered platform pursuit: launched=" + launched
                        + ", landed=" + landed + ", enemy=" + enemy.state() + " at " + enemy.x() + "," + enemy.y()
                        + ", player=" + world.player().x() + "," + world.player().y() + ", world=" + world.state());
            System.out.println("ENEMY_PURSUIT_SMOKE_PASS: sentinel jumps onto the templar's platform and engages there");
        } finally { hud.dispose(); renderer.dispose(); resources.dispose(); assets.dispose(); }
    }

    private void checkCampaignTransition() {
        CrusaderGame campaignGame = new CrusaderGame("levels/test-campaign.xml");
        campaignGame.create();
        try {
            campaignGame.startJourney();
            campaignGame.startGame();
            var first = ((PlayScreen) campaignGame.getScreen()).world();
            completeWorld(first);
            campaignGame.getScreen().render(0);
            capture("25-next-level-prompt.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            campaignGame.getScreen().render(0);
            var second = ((PlayScreen) campaignGame.getScreen()).world();
            if (!campaignGame.journey().currentLevel().id().equals("test-road")
                    || second == first || second.level().width() != 1920
                    || second.time() != 0 || second.checkpoint() || second.collectedBandageCount() != 0
                    || second.arenaEntered() || !second.gateLocked())
                throw new IllegalStateException("Campaign transition did not create a fresh second level");
            campaignGame.getScreen().render(0);
            capture("25-second-level.png");
            // Exercise a second map through the actual GPU loader as well as the screen transition.
            var assets = new br.com.thecruzader.presentation.GameAssets();
            var resources = new br.com.thecruzader.presentation.LevelResources(campaignGame.journey().currentLevel(), assets);
            try {
                if (resources.map().getProperties().get("width", Integer.class) != 60
                        || resources.data().width() != 1920 || resources.definition().regions().size() != 1)
                    throw new IllegalStateException("Rendered and simulated second map disagree");
            } finally { resources.dispose(); assets.dispose(); }
            completeWorld(second);
            campaignGame.getScreen().render(0);
            capture("25-final-victory.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            campaignGame.getScreen().render(0);
            var restarted = ((PlayScreen) campaignGame.getScreen()).world();
            if (!campaignGame.journey().currentLevel().id().equals("exiles-road")
                    || restarted == first || restarted.state() != br.com.thecruzader.gameplay.GameWorld.State.PLAYING
                    || restarted.checkpoint() || restarted.collectedBandageCount() != 0)
                throw new IllegalStateException("Final victory did not restart the campaign");
            campaignGame.getScreen().render(0);
            capture("25-campaign-restarted.png");
            System.out.println("CAMPAIGN_SMOKE_PASS: next-level input, second map graphics/geometry, clean state, final victory and campaign restart");
        } finally { campaignGame.dispose(); }
    }

    private void completeWorld(br.com.thecruzader.gameplay.GameWorld world) {
        for (int tick = 0; tick < 180 * 120
                && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP, routeInput(world));
        }
        if (world.state() != br.com.thecruzader.gameplay.GameWorld.State.WON)
            throw new IllegalStateException("Campaign route failed at x=" + world.player().x());
    }

    private br.com.thecruzader.input.PlayerInput routeInput(br.com.thecruzader.gameplay.GameWorld world) {
        var player = world.player();
        var target = world.enemies().stream().filter(enemy -> enemy.health() > 0 && Math.abs(enemy.y() - player.y()) < 80)
                .min(java.util.Comparator.comparingDouble(enemy -> Math.abs(enemy.x() - player.x()))).orElse(null);
        int direction = 1;
        boolean attack = false, block = false;
        if (target != null && Math.abs(target.x() - player.x()) < 180) {
            double distance = target.x() - player.x();
            direction = distance >= 0 ? 1 : -1;
            block = switch (target.state()) {
                case WINDUP, ATTACK, DASH_WINDUP, DASH, JUMP_WINDUP, JUMP -> true;
                default -> false;
            };
            if (!block) {
                attack = target.state() == br.com.thecruzader.gameplay.Enemy.State.RECOVER && Math.abs(distance) < 80;
                if (player.attack().running() || Math.abs(distance) < 50) direction = 0;
            }
        }
        boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
                .anyMatch(solid -> solid.y() <= player.y() && solid.top() > player.y()
                        && solid.x() >= player.x() + br.com.thecruzader.gameplay.Player.WIDTH
                        && solid.x() - (player.x() + br.com.thecruzader.gameplay.Player.WIDTH) < 85
                        && solid.top() - player.y() < 160);
        return new br.com.thecruzader.input.PlayerInput(direction, jump, attack, false, false, block);
    }

    private void captureLevelTour() {
        Gdx.input.getInputProcessor().keyUp(Input.Keys.L);
        game.startJourney();
        game.startGame();
        var world = ((PlayScreen) game.getScreen()).world();
        double[] landmarks = {850, 2370, 4480, 6400, 7670, 8400};
        String[] names = {"06-bandages.png", "07-first-stairs.png", "08-sanctuary.png",
                "09-extended-road.png", "10-final-stairs.png", "11-arena.png"};
        int landmark = 0;
        boolean archerCaptured = false, arrowCaptured = false, arenaCaptured = false, angelsCaptured = false;
        java.util.Set<br.com.thecruzader.gameplay.Enemy.State> bossCaptures =
                java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.Enemy.State.class);
        for (int tick = 0; tick < 180 * 120
                && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
            var player = world.player();
            var target = world.enemies().stream().filter(enemy -> enemy.health() > 0 && Math.abs(enemy.y() - player.y()) < 80)
                    .min(java.util.Comparator.comparingDouble(enemy -> Math.abs(enemy.x() - player.x())))
                    .orElse(null);
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP, routeInput(world));
            if (!angelsCaptured && world.bossReleaseActive()) {
                captureAngelRelease(world);
                angelsCaptured = true;
            }
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
        if (!archerCaptured || !arrowCaptured || !angelsCaptured || bossCaptures.size() != 3
                || landmark != landmarks.length
                || world.state() != br.com.thecruzader.gameplay.GameWorld.State.WON)
            throw new IllegalStateException("Level tour failed at x=" + world.player().x());
        game.render();
        capture("12-victory.png");
        System.out.println("LEVEL_TOUR_PASS: bandage route, sanctuary, stairs, arena and victory");
    }

    private void captureAngelRelease(br.com.thecruzader.gameplay.GameWorld world) {
        Gdx.files.absolute(output + "/angel-frames").mkdirs();
        game.getScreen().render(0);
        if (!world.gateLocked()) throw new IllegalStateException("Doors opened before the angels arrived");
        for (int frame = 0; frame < 48; frame++) {
            game.getScreen().render(.1f);
            captureIntroFrame("angel-frames/angels-" + String.format(java.util.Locale.ROOT, "%02d", frame) + ".png");
            if (frame == 13) {
                game.pause();
                double elapsed = world.bossReleaseElapsed();
                game.getScreen().render(.1f);
                long frozen = frameHash();
                for (int pause = 0; pause < 4; pause++) game.getScreen().render(.1f);
                if (elapsed != world.bossReleaseElapsed() || frozen != frameHash())
                    throw new IllegalStateException("Paused angels or gates moved");
                Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
                game.getScreen().render(0);
                Gdx.input.getInputProcessor().keyUp(Input.Keys.ESCAPE);
            }
            if (frame == 28 && !world.gateLocked()) throw new IllegalStateException("Gate collision opened too early");
            if (frame == 35 && world.gateLocked()) throw new IllegalStateException("Angels failed to open gates");
        }
        if (world.bossReleaseActive()) throw new IllegalStateException("Angel release did not finish");
        capture("31-angels-opened-gates.png");
        System.out.println("ANGEL_RELEASE_SMOKE_PASS: descending wingbeats, paused scene, timed door collision and exit released");
    }

    private void checkTerrainAndDeaths() {
        var assets = new br.com.thecruzader.presentation.GameAssets();
        if (assets.texts.language() != Language.EN)
            throw new IllegalStateException("Saved language was not loaded by new assets");
        var resources = new br.com.thecruzader.presentation.LevelResources(game.campaign().levels().getFirst(), assets);
        var renderer = new br.com.thecruzader.presentation.WorldRenderer(assets, resources);
        var level = resources.data();
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
            var arenaHud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false);
            try {
                arenaHud.announce("notice.arena");
                arenaHud.render(arenaWorld, .35f, false, 0);
                capture("24-english-arena.png");
            } finally { arenaHud.dispose(); }
            System.out.println("ARENA_SMOKE_PASS: locked camera, sealed gates, boss health bar and localized announcement");
            checkChapelViews(renderer, level);
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
            var deathAudio = new br.com.thecruzader.presentation.AudioPlayer(assets);
            deathAudio.play(playerWorld.drainEvents());
            if (!assets.requiem().isPlaying() || assets.ambience().isPlaying())
                throw new IllegalStateException("Death cue did not replace ambience");
            deathAudio.setPaused(true);
            deathAudio.toggleMute();
            deathAudio.toggleMute();
            if (assets.requiem().isPlaying()) throw new IllegalStateException("Mute resumed paused requiem");
            deathAudio.setPaused(false);
            if (!assets.requiem().isPlaying()) throw new IllegalStateException("Requiem did not resume");
            Gdx.files.absolute(output + "/death-frames").mkdirs();
            long firstDeathFrame = 0;
            for (int phase = 0; phase < 42; phase++) {
                renderer.render(playerWorld, .1f, 1);
                if (phase == 0 || phase == 4 || phase == 11) capture("19-templar-death-" + phase + ".png");
                if (phase == 0) firstDeathFrame = frameHash();
                if (phase == 25 && frameHash() == firstDeathFrame)
                    throw new IllegalStateException("Templar death poses and light did not animate");
                captureIntroFrame("death-frames/death-" + String.format(java.util.Locale.ROOT, "%02d", phase) + ".png");
            }
            deathAudio.endDeath();
            if (assets.requiem().isPlaying() || !assets.ambience().isPlaying())
                throw new IllegalStateException("Respawn audio did not return to ambience");
            deathAudio.setPaused(true);
            var translatedHud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false);
            try {
                translatedHud.render(playerWorld, .1f, false, renderer.deathElapsed());
                capture("21-english-death.png");
                var victoryWorld = ((PlayScreen) game.getScreen()).world();
                renderer.render(victoryWorld, .016f, 1);
                translatedHud.render(victoryWorld, .016f, false, 0);
                capture("21-english-victory.png");
                renderer.render(playerWorld, 0, 1);
                for (int frame = 0; frame < 42; frame++) renderer.render(playerWorld, .1f, 1);
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
            System.out.println("REQUIEM_SMOKE_PASS: dedicated non-looping death music, ambience replacement, pause/mute and respawn audio");
        } finally { renderer.dispose(); resources.dispose(); assets.dispose(); }
    }

    private void checkPlayerDeathControls() {
        game.startJourney();
        game.startGame();
        var screen = (PlayScreen) game.getScreen();
        var world = screen.world();
        screen.render(0);
        for (int tick = 0; tick < 12000 && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++)
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(world.player().x() < 1800 ? 1 : 0,
                            false, false, false, false, false));
        if (world.state() != br.com.thecruzader.gameplay.GameWorld.State.DEAD)
            throw new IllegalStateException("Death input setup failed");
        var fallen = world.player();
        screen.render(.1f);
        Gdx.input.getInputProcessor().keyDown(Input.Keys.R);
        screen.render(.1f);
        Gdx.input.getInputProcessor().keyUp(Input.Keys.R);
        if (world.player() != fallen) throw new IllegalStateException("Respawn skipped the prayer sequence");
        screen.pause();
        screen.render(.1f);
        long frozen = frameHash();
        for (int frame = 0; frame < 4; frame++) screen.render(.1f);
        if (frameHash() != frozen) throw new IllegalStateException("Focus-paused death moved");
        Gdx.input.getInputProcessor().keyDown(Input.Keys.ESCAPE);
        screen.render(.1f);
        Gdx.input.getInputProcessor().keyUp(Input.Keys.ESCAPE);
        if (world.paused()) throw new IllegalStateException("Death could not resume after focus pause");
        for (int frame = 0; frame < 42; frame++) screen.render(.1f);
        capture("29-death-prayer.png");
        Gdx.input.getInputProcessor().keyDown(Input.Keys.R);
        screen.render(.1f);
        if (world.player() == fallen || world.state() != br.com.thecruzader.gameplay.GameWorld.State.PLAYING)
            throw new IllegalStateException("Respawn after prayer failed");
        System.out.println("TEMPLAR_DEATH_SMOKE_PASS: full prayer before restart, focus pause/resume, localized overlay and clean respawn");
    }

    private br.com.thecruzader.gameplay.GameWorld visualWorld(
            br.com.thecruzader.level.LevelData level, double spawnX,
            java.util.List<br.com.thecruzader.level.LevelData.Spawn> enemies) {
        return new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
                level.width(), level.height(), new br.com.thecruzader.level.LevelData.Point(spawnX, 64),
                level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(), enemies));
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

    private void checkChapelViews(br.com.thecruzader.presentation.WorldRenderer renderer,
                                  br.com.thecruzader.level.LevelData level) {
        var guardian = level.enemies().stream().filter(e ->
                e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow();
        var outside = visualWorld(level, level.arena().x() - 40, java.util.List.of(guardian));
        renderer.render(outside, 0, 1);
        if (renderer.interiorVisible()) throw new IllegalStateException("Chapel interior visible from road");
        captureIntroFrame("28-chapel-exterior.png");
        long exteriorHash = frameHash();
        var shiftedGuardian = new br.com.thecruzader.level.LevelData.Spawn(guardian.x() - 120,
                guardian.y(), guardian.kind());
        renderer.render(visualWorld(level, outside.player().x(), java.util.List.of(shiftedGuardian)), 0, 1);
        if (frameHash() != exteriorHash) throw new IllegalStateException("Guardian leaked into exterior view");
        renderer.render(outside, 0, 1);
        for (int tick = 0; tick < 120 && !outside.insideArena(); tick++)
            outside.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(1, false, false, true, false, false));
        if (!outside.insideArena()) throw new IllegalStateException("Chapel entry failed");
        renderer.render(outside, .016f, 1);
        if (!renderer.interiorVisible() || renderer.transitionOpacity() != 1)
            throw new IllegalStateException("Chapel did not switch under black");
        captureIntroFrame("28-chapel-fade-0.png");
        outside.setPaused(true);
        renderer.render(outside, .1f, 1);
        if (renderer.transitionOpacity() != 1) throw new IllegalStateException("Paused room fade advanced");
        outside.setPaused(false);
        for (int frame = 1; frame <= 4; frame++) {
            renderer.render(outside, .1f, 1);
            captureIntroFrame("28-chapel-fade-" + frame + ".png");
        }
        captureIntroFrame("28-chapel-interior.png");
        for (int tick = 0; tick < 4000 && outside.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++)
            outside.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    br.com.thecruzader.input.PlayerInput.NONE);
        if (outside.state() != br.com.thecruzader.gameplay.GameWorld.State.DEAD)
            throw new IllegalStateException("Chapel respawn death setup failed");
        renderer.render(outside, .1f, 1);
        outside.respawn();
        renderer.render(outside, .1f, 1);
        if (renderer.interiorVisible() || renderer.transitionOpacity() != 0)
            throw new IllegalStateException("Chapel respawn retained interior or fade");
        captureIntroFrame("28-chapel-respawn.png");
        System.out.println("CHAPEL_SMOKE_PASS: hidden guardian outside, physical entry, interior cut, paused fade and exterior respawn");
    }

    private long frameHash() {
        Pixmap image = Pixmap.createFromFrameBuffer(0, 0,
                Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            var crc = new java.util.zip.CRC32();
            crc.update(image.getPixels().duplicate());
            return crc.getValue();
        } finally { image.dispose(); }
    }

    private void captureIntroFrame(String name) {
        Pixmap full = Pixmap.createFromFrameBuffer(0, 0,
                Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Pixmap preview = new Pixmap(960, 540, Pixmap.Format.RGBA8888);
        try {
            preview.setFilter(Pixmap.Filter.NearestNeighbour);
            float scale = Math.min(full.getWidth() / 960f, full.getHeight() / 540f);
            int width = Math.round(960 * scale), height = Math.round(540 * scale);
            preview.drawPixmap(full, (full.getWidth() - width) / 2, (full.getHeight() - height) / 2,
                    width, height, 0, 0, 960, 540);
            PixmapIO.writePNG(new FileHandle(output + "/" + name), preview, -1, true);
        } finally { preview.dispose(); full.dispose(); }
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
