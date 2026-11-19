package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.SpecialAbility;
import br.com.thecruzader.i18n.Language;
import br.com.thecruzader.input.PlayerInput;
import br.com.thecruzader.level.LevelData.EnemyKind;
import br.com.thecruzader.screen.PlayScreen;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

/** Desktop integration harness using the same screens, assets and input adapter as the player. */
final class SmokeTest extends ApplicationAdapter {
    private final CrusaderGame game;
    private final br.com.thecruzader.presentation.WindowMode windowMode;
    private final String output;
    private int frame;
    private Language originalLanguage;
    private boolean rendering;
    private int heavyWindup, shotWindup, crossWindup;

    SmokeTest(String output, br.com.thecruzader.presentation.WindowMode windowMode) {
        this.output = output;
        this.windowMode = windowMode;
        game = new CrusaderGame(br.com.thecruzader.level.CampaignLoader.PATH, windowMode, false,
            java.nio.file.Path.of(output, "smoke-progress.properties"));
    }

    @Override
    public void create() {
        Gdx.files.absolute(output).mkdirs();
        Gdx.files.absolute(java.nio.file.Path.of(output, "smoke-progress.properties").toString()).delete();
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
            checkConstantinople();
            checkNicaea();
            checkDorylaeum();
            checkDevelopmentSelector();
            game.setLanguage(originalLanguage);
            game.showMenu();
            Gdx.input.getInputProcessor().keyDown(Input.Keys.UP);
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            game.render();
            System.out.println("MENU_SMOKE_PASS: instructions, options, sound toggle, window sizes, new game and quit");
        }
    }

    private void checkConstantinople() {
        game.setLanguage(Language.PT);
        game.startJourney();
        game.startGame();
        completeWorld(((PlayScreen) game.getScreen()).world());
        game.getScreen().render(0);
        tap(Input.Keys.ENTER);
        game.getScreen().render(0);
        if (!(game.getScreen() instanceof PlayScreen play))
            throw new IllegalStateException("Constantinople did not start automatically");
        var world = play.world();
        if (!game.journey().currentLevel().id().equals("constantinople"))
            throw new IllegalStateException("Campaign did not enter Constantinople");
        var definition = game.journey().currentLevel();
        var stops = definition.conversations();
        int stop = 0;
        boolean arena = false, aimed = false, shot = false;
        for (int tick = 0; tick < 150 * 120 && world.state() == GameWorld.State.PLAYING
            && !world.bossDefeated(); tick++) {
            world.update(GameWorld.STEP, routeInput(world));
            if (stop < stops.size() && world.player().x() >= stops.get(stop).x() - 40) {
                play.render(0);
                if (stop != 1) {
                    tap(Input.Keys.F);
                    double time = world.time();
                    play.render(.1f);
                    if (!play.readingConversation() || world.time() != time)
                        throw new IllegalStateException("Historical conversation did not open or freeze the road");
                    capture(stop == 0 ? "39-constantinople-audience.png" : "39-constantinople-harbor.png");
                    tap(Input.Keys.F);
                    play.render(0);
                    if (play.readingConversation())
                        throw new IllegalStateException("Historical conversation did not close");
                } else {
                    capture("39-constantinople-market.png");
                }
                stop++;
            }
            if (world.arenaEntered() && !arena) {
                for (int frame = 0; frame < 8; frame++) play.render(.05f);
                capture("39-constantinople-captain.png");
                arena = true;
            }
            if (arena && !aimed && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.SHOT_WINDUP) {
                play.render(0);
                capture("39-constantinople-heavy-aim.png");
                aimed = true;
            }
            if (!shot && world.arrows().stream().anyMatch(arrow -> arrow.heavy()
                && Math.abs(arrow.x() - world.boss().x()) > 40)) {
                play.render(0);
                capture("39-constantinople-heavy-arrow.png");
                shot = true;
            }
        }
        if (stop != stops.size() || !world.checkpoint() || !aimed || !shot || !world.bossDefeated()
            || !world.player().abilities().contains(SpecialAbility.BOW))
            throw new IllegalStateException("Constantinople route failed at x=" + world.player().x()
                + ", y=" + world.player().y() + ", hearts=" + world.player().health()
                + ", boss=" + world.boss().health() + ", stops=" + stop + ", aim=" + aimed + ", shot=" + shot);
        play.render(0);
        while (world.bossReleaseActive()) world.update(GameWorld.STEP, PlayerInput.NONE);
        tap(Input.Keys.E);
        play.render(.075f);
        if (!world.player().drawingBow()) throw new IllegalStateException("Bow did not start in the second slot");
        capture("39-constantinople-bow.png");
        play.render(.1f);
        if (world.player().specialCharges(1) != 2 || world.arrows().isEmpty())
            throw new IllegalStateException("Bow did not consume one charge or spawn a projectile");
        completeWorld(world);
        play.render(0);
        capture("39-constantinople-victory.png");
        System.out.println("CONSTANTINOPLE_SMOKE_PASS: full road, optional historical conversations, heavy arrow, bow reward and crossing");
    }

    private void checkNicaea() {
        tap(Input.Keys.ENTER);
        game.getScreen().render(0);
        if (!(game.getScreen() instanceof PlayScreen play)
            || !game.journey().unlockedAbilities().contains(SpecialAbility.BOW))
            throw new IllegalStateException("Niceia did not receive the bow after Constantinople");
        var world = play.world();
        var definition = game.journey().currentLevel();
        if (!definition.id().equals("nicaea") || world.level().width() != 10240
            || world.boss().kind() != EnemyKind.RELIEF_DUELIST || world.player().specialCharges(1) != 3
            || !world.arenaEntranceClosed())
            throw new IllegalStateException("Niceia did not start with fresh arrows and its redoubt secured");
        var briefings = definition.conversations();
        int stop = 0;
        boolean capturedEarthworks = false, capturedArena = false, sawFirstCut = false, sawCrossCut = false;
        for (int tick = 0; tick < 180 * 120 && world.state() == GameWorld.State.PLAYING; tick++) {
            if (stop < briefings.size() && world.player().x() >= briefings.get(stop).x() - 42) {
                play.render(0);
                tap(Input.Keys.F);
                play.render(0);
                if (!play.readingConversation() || !world.arenaEntranceClosed())
                    throw new IllegalStateException("Briefing must remain pending until confirmed");
                capture(stop == 0 ? "41-nicaea-camp.png" : "42-nicaea-lake.png");
                tap(Input.Keys.F);
                play.render(0);
                stop++;
                if (play.readingConversation() || world.arenaEntranceClosed() != (stop < briefings.size()))
                    throw new IllegalStateException("The redoubt must open after both briefings");
            }
            var controls = routeInput(world);
            if (!sawCrossCut && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.TWIN_CROSS_WINDUP)
                controls = new PlayerInput(controls.direction(), false, false, false, false, true, -1);
            world.update(GameWorld.STEP, controls);
            if (!capturedEarthworks && world.player().x() > 4630 && world.player().grounded()) {
                play.render(0);
                capture("42-nicaea-earthworks.png");
                capturedEarthworks = true;
            }
            if (world.arenaEntered() && !capturedArena) {
                for (int frame = 0; frame < 8; frame++) play.render(.05f);
                capture("43-nicaea-redoubt.png");
                capturedArena = true;
            }
            if (capturedArena && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.TWIN_SLASH)
                sawFirstCut = true;
            if (capturedArena && !sawCrossCut
                && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.TWIN_CROSS) {
                play.render(0);
                capture("43-nicaea-duelist-cross-cut.png");
                sawCrossCut = true;
            }
        }
        if (stop != briefings.size() || !world.checkpoint() || !capturedEarthworks || !capturedArena
            || !sawFirstCut || !sawCrossCut
            || world.state() != GameWorld.State.WON || game.journey().unlockedAbilities().size() != 2)
            throw new IllegalStateException("Niceia route failed at x=" + world.player().x() + ", y="
                + world.player().y() + ", hearts=" + world.player().health() + ", boss=" + world.boss().health()
                + ", briefings=" + stop + ", first cut=" + sawFirstCut + ", cross cut=" + sawCrossCut);
        play.render(0);
        capture("44-nicaea-surrender.png");
        System.out.println("NICAEA_SMOKE_PASS: extended road, two mandatory briefings, both duelist cuts and Byzantine surrender");
    }

    private void checkDorylaeum() {
        tap(Input.Keys.ENTER);
        game.getScreen().render(0);
        var play = (PlayScreen) game.getScreen();
        var world = play.world();
        if (!game.journey().currentLevel().id().equals("dorylaeum") || world.level().width() != 8960
            || world.boss().kind() != EnemyKind.MOUNTED_COMMANDER)
            throw new IllegalStateException("Dorileia must follow Niceia with a unique mounted boss");
        boolean orders = false, gully = false, camp = false, volley = false, charge = false, relief = false;
        int previousHealth = world.player().health();
        for (int tick = 0; tick < 200 * 120 && world.state() == GameWorld.State.PLAYING; tick++) {
            if (!orders && world.player().x() >= 342) {
                play.render(0);
                tap(Input.Keys.F);
                play.render(0);
                if (!play.readingConversation()) throw new IllegalStateException("Dorileia briefing did not open");
                capture("45-dorylaeum-march.png");
                tap(Input.Keys.F);
                play.render(0);
                orders = true;
            }
            world.update(GameWorld.STEP, world.arenaEntered() ? campInput(world) : routeInput(world));
            if (world.player().health() < previousHealth) {
                System.out.println("DORYLAEUM_HIT: boss=" + world.boss().state() + ", x=" + world.player().x()
                    + ", y=" + world.player().y() + ", seconds=" + world.campDefense().orElseThrow().secondsRemaining());
            }
            previousHealth = world.player().health();
            if (!gully && world.player().x() >= 4700 && world.player().grounded()) {
                play.render(0);
                capture("45-dorylaeum-gully.png");
                gully = true;
            }
            if (world.arenaEntered() && !camp) {
                for (int f = 0; f < 8; f++) play.render(.05f);
                capture("46-dorylaeum-formation.png");
                camp = true;
            }
            if (camp && !volley && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_VOLLEY) {
                play.render(0);
                capture("47-dorylaeum-mounted-volley.png");
                volley = true;
            }
            if (camp && !charge && world.boss().state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_CHARGE_WINDUP) {
                play.render(0);
                capture("48-dorylaeum-charge.png");
                charge = true;
            }
            if (camp && !relief && world.campDefense().orElseThrow().reinforcementsArrived()) {
                play.render(0);
                capture("49-dorylaeum-reinforcements.png");
                relief = true;
            }
        }
        if (!orders || !gully || !camp || !volley || !charge || !relief || !world.checkpoint()
            || world.state() != GameWorld.State.WON)
            throw new IllegalStateException("Dorileia route failed: x=" + world.player().x() + ", y=" + world.player().y()
                + ", health=" + world.player().health() + ", boss=" + world.boss().health()
                + ", relief=" + relief);
        play.render(0);
        capture("50-dorylaeum-victory.png");
        tap(Input.Keys.ENTER);
        play.render(0);
        if (!game.journey().currentLevel().id().equals("exiles-road"))
            throw new IllegalStateException("Final Dorileia victory did not restart the campaign");
        System.out.println("DORYLAEUM_SMOKE_PASS: full road, briefing, formation, moving volley, charge, relief, victory and restart");
    }

    private PlayerInput campInput(GameWorld world) {
        var p = world.player();
        var boss = world.boss();
        var defense = world.campDefense().orElseThrow();
        if (world.bossDefeated()) return new PlayerInput(1, false, false, false, false, false, -1);
        double center = defense.formation().x() + defense.formation().width() / 2 - 14;
        double target = defense.reinforcementsArrived() && boss.health() > 0 ? boss.x() + boss.width() / 2 - 14 : center;
        int direction = Math.abs(target - p.x()) > 12 ? (target > p.x() ? 1 : -1) : 0;
        boolean charging = boss.state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_CHARGE;
        double bossCenter = boss.x() + boss.width() / 2;
        boolean approaching = (p.x() - bossCenter) * boss.facing() > 0;
        boolean chargeWarning = boss.state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_CHARGE_WINDUP;
        boolean jump = p.grounded() && Math.abs(bossCenter - p.x()) < 300
            && (chargeWarning && boss.actionSecondsRemaining() <= .25 || charging && approaching);
        var arrow = world.arrows().stream().filter(a -> Math.abs(a.y() - (p.y() + 26)) < 48
                && Math.abs(a.x() - p.x()) < 180
                && Math.cos(Math.toRadians(a.angle())) * (p.x() + 14 - a.x()) > 0)
            .min(java.util.Comparator.comparingDouble(a -> Math.abs(a.x() - p.x()))).orElse(null);
        boolean drawing = boss.state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_DRAW
            || boss.state() == br.com.thecruzader.gameplay.Enemy.State.RIDER_VOLLEY;
        boolean block = !jump && (arrow != null || drawing && Math.abs(bossCenter - p.x()) < 220);
        if (block) direction = (arrow != null ? arrow.x() : bossCenter) >= p.x() ? 1 : -1;
        double gap = boss.x() > p.x() ? boss.x() - (p.x() + 28) : p.x() - (boss.x() + boss.width());
        boolean attack = defense.reinforcementsArrived() && !block && boss.health() > 0 && gap < 52 && !charging
            && boss.state() == br.com.thecruzader.gameplay.Enemy.State.RECOVER && !p.attack().running();
        if (attack) direction = bossCenter >= p.x() ? 1 : -1;
        int special = attack && p.specialCharges(0) > 0 ? 0 : -1;
        return new PlayerInput(direction, jump, attack && special < 0, false, !p.grounded(), block, special);
    }

    private void checkDevelopmentSelector() {
        var dev = new CrusaderGame(br.com.thecruzader.level.CampaignLoader.PATH, windowMode, true);
        dev.create();
        try {
            if (!(dev.getScreen() instanceof br.com.thecruzader.screen.DevelopmentScreen))
                throw new IllegalStateException("Dev launch did not open its level selector");
            dev.getScreen().render(0);
            capture("40-dev-level-select.png");
            tap(Input.Keys.DOWN);
            tap(Input.Keys.ENTER);
            dev.getScreen().render(0);
            if (!(dev.getScreen() instanceof PlayScreen)
                || !dev.journey().unlockedAbilities().equals(java.util.List.of(SpecialAbility.GOLDEN_STRIKE)))
                throw new IllegalStateException("Dev stage two lacks the normal unlocked loadout");
            if (!dev.journey().currentLevel().id().equals("constantinople")
                || ((PlayScreen) dev.getScreen()).world().level().width() != 8960)
                throw new IllegalStateException("Dev selector did not load the production second stage");
            tap(Input.Keys.F2);
            dev.render();
            if (!(dev.getScreen() instanceof br.com.thecruzader.screen.DevelopmentScreen))
                throw new IllegalStateException("F2 did not return to the dev selector");
            tap(Input.Keys.DOWN);
            tap(Input.Keys.DOWN);
            tap(Input.Keys.ENTER);
            dev.getScreen().render(0);
            if (!(dev.getScreen() instanceof PlayScreen)
                || !dev.journey().currentLevel().id().equals("nicaea")
                || !dev.journey().unlockedAbilities().contains(SpecialAbility.BOW))
                throw new IllegalStateException("Dev selector did not offer Niceia with the bow unlocked");
            tap(Input.Keys.F2);
            dev.render();
            tap(Input.Keys.DOWN);
            tap(Input.Keys.DOWN);
            tap(Input.Keys.DOWN);
            tap(Input.Keys.ENTER);
            dev.getScreen().render(0);
            if (!(dev.getScreen() instanceof PlayScreen dorylaeum)
                || !dev.journey().currentLevel().id().equals("dorylaeum")
                || dorylaeum.world().campDefense().isEmpty())
                throw new IllegalStateException("Dev selector did not load Dorileia with its defense encounter");
            tap(Input.Keys.F2);
            dev.render();
            tap(Input.Keys.ENTER);
            dev.getScreen().render(0);
            if (!(dev.getScreen() instanceof PlayScreen) || !dev.journey().currentLevel().id().equals("exiles-road"))
                throw new IllegalStateException("Dev selector failed to reload stage one");
            if (!dev.journey().unlockedAbilities().isEmpty())
                throw new IllegalStateException("Dev selection leaked rewards from the previous session");
            System.out.println("DEV_SELECTOR_SMOKE_PASS: selection from the manifest, normal loadout, F2 and fresh replay");
        } finally {
            dev.dispose();
        }
    }

    private void tap(int key) {
        Gdx.input.getInputProcessor().keyDown(key);
        Gdx.input.getInputProcessor().keyUp(key);
    }

    private void captureEnemyPursuit() {
        var assets = new br.com.thecruzader.presentation.GameAssets();
        var resources = new br.com.thecruzader.presentation.LevelResources(game.campaign().levels().getFirst(), assets);
        var renderer = new br.com.thecruzader.presentation.WorldRenderer(assets, resources);
        var level = resources.data();
        var guardian = level.enemies().stream().filter(e ->
            e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow();
        var world = new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
            level.width(), level.height(), new br.com.thecruzader.level.LevelData.Point(520, 160),
            level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(),
            java.util.List.of(new br.com.thecruzader.level.LevelData.Spawn(300, 64,
                br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL), guardian)));
        var hud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false, world.player());
        try {
            renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            hud.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            var enemy = world.enemies().getFirst();
            boolean noticed = false, launched = false, landed = false;
            renderer.render(world, 0, 1);
            hud.render(world, 0, false, 0);
            capture("27-platform-pursuit-before.png");
            for (int tick = 0; tick < 1200; tick++) {
                world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(0, false, false, false, false, true, -1));
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
        CrusaderGame campaignGame = new CrusaderGame("levels/test-campaign.xml", windowMode, false,
            java.nio.file.Path.of(output, "test-campaign-progress.properties"));
        campaignGame.create();
        try {
            campaignGame.startJourney();
            campaignGame.startGame();
            var first = ((PlayScreen) campaignGame.getScreen()).world();
            completeWorld(first);
            campaignGame.getScreen().render(0);
            var reloaded = new br.com.thecruzader.persistence.JourneySave(
                java.nio.file.Path.of(output, "test-campaign-progress.properties"), "levels/test-campaign.xml",
                campaignGame.campaign()).load().orElseThrow();
            if (!campaignGame.hasSavedJourney() || !reloaded.currentLevel().id().equals("test-road")
                || !reloaded.unlockedAbilities().contains(SpecialAbility.GOLDEN_STRIKE))
                throw new IllegalStateException("Victory did not persist the next level and unlocked reward");
            capture("25-next-level-prompt.png");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.ENTER);
            campaignGame.getScreen().render(0);
            if (!(campaignGame.getScreen() instanceof PlayScreen))
                throw new IllegalStateException("Level two must start automatically");
            var second = ((PlayScreen) campaignGame.getScreen()).world();
            if (second.player().abilities().size() != 1 || second.player().specialCharges(0) != 3)
                throw new IllegalStateException("Unlocked special was not automatically equipped");
            Gdx.input.getInputProcessor().keyDown(Input.Keys.Q);
            second.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                ((br.com.thecruzader.input.InputMapper) Gdx.input.getInputProcessor()).consume());
            if (!second.player().goldenStrike() || second.player().specialCharges(0) != 2)
                throw new IllegalStateException("Golden strike activation failed");
            campaignGame.getScreen().render(0);
            capture("38-golden-strike.png");
            if (!campaignGame.journey().currentLevel().id().equals("test-road")
                    || second == first || second.level().width() != 1920
                || second.checkpoint() || second.collectedBandageCount() != 0
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
                case WINDUP, ATTACK, DASH_WINDUP, DASH, JUMP_WINDUP, JUMP, TWIN_WINDUP, TWIN_SLASH -> true;
                default -> false;
            };
            if (!block) {
                attack = (target.archer() || target.state() == br.com.thecruzader.gameplay.Enemy.State.RECOVER)
                    && Math.abs(distance) < 80;
                if (player.attack().running() || Math.abs(distance) < 50) direction = 0;
            }
        }
        if (target != null && target.kind() == br.com.thecruzader.level.LevelData.EnemyKind.SHIELD_BEARER
            && target.state() == br.com.thecruzader.gameplay.Enemy.State.WINDUP) {
            block = target.attackProgress() >= .9;
        }
        boolean heavy = target != null && target.boss()
            && (target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP
            || target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH_WINDUP || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP_WINDUP);
        boolean preparingHeavy = heavy && (target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH_WINDUP
            || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP_WINDUP);
        heavyWindup = preparingHeavy ? heavyWindup + (world.hitStopped() ? 0 : 1) : 0;
        double warningDuration = heavy && target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH_WINDUP ? .45 : .50;
        if (target != null && target.kind() == EnemyKind.HARBOR_CAPTAIN) warningDuration = .52;
        if (target != null && target.health() <= target.maxHealth() / 2) warningDuration *= .8;
        boolean timedParry = preparingHeavy && heavyWindup * br.com.thecruzader.gameplay.GameWorld.STEP >= warningDuration - .07
            && Math.abs(target.x() - player.x()) < 80 && !player.attack().running();
        if (heavy) {
            block = timedParry || (!preparingHeavy && player.blocking());
            direction = block ? (target.x() >= player.x() ? 1 : -1)
                : preparingHeavy && Math.abs(target.x() - player.x()) < 80 ? 0
                : target.x() >= player.x() ? -1 : 1;
        }
        if (heavy && (target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP_WINDUP
            || target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP)) {
            block = false;
            direction = target.x() >= player.x() ? -1 : 1;
        }
        if (target != null && target.boss() && player.attack().running()) attack = false;
        boolean dodge = heavy && !block && Math.abs(target.x() - player.x()) < 110
            && (target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH || (target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP
            && Math.abs(target.y() - player.y()) < 80));
        if (dodge && target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP)
            direction = target.x() >= player.x() ? 1 : -1;
        boolean preparingCross = target != null
            && target.state() == br.com.thecruzader.gameplay.Enemy.State.TWIN_CROSS_WINDUP;
        crossWindup = preparingCross ? crossWindup + (world.hitStopped() ? 0 : 1) : 0;
        if (preparingCross || target != null && target.state() == br.com.thecruzader.gameplay.Enemy.State.TWIN_CROSS) {
            direction = target.x() >= player.x() ? 1 : -1;
            block = !preparingCross || crossWindup * GameWorld.STEP >= .28;
        }
        boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
            .anyMatch(solid -> solid.top() > player.y()
                        && solid.x() >= player.x() + br.com.thecruzader.gameplay.Player.WIDTH
                        && solid.x() - (player.x() + br.com.thecruzader.gameplay.Player.WIDTH) < 85
                        && solid.top() - player.y() < 160);
        boolean aiming = target != null && target.state() == br.com.thecruzader.gameplay.Enemy.State.SHOT_WINDUP;
        shotWindup = aiming ? shotWindup + (world.hitStopped() ? 0 : 1) : 0;
        if (aiming && player.grounded()) {
            double aimDuration = target.health() <= target.maxHealth() / 2 ? .78 * .8 : .78;
            jump |= shotWindup * GameWorld.STEP >= aimDuration - .15
                && Math.abs(target.x() - player.x()) < 430;
        }
        var incoming = world.arrows().stream().filter(a -> Math.abs(a.x() - player.x()) < 160
                && a.y() >= player.y() && a.y() <= player.y() + 52
                && Math.cos(Math.toRadians(a.angle())) * (player.x() + 14 - a.x()) > 0)
            .min(java.util.Comparator.comparingDouble(a -> Math.abs(a.x() - player.x()))).orElse(null);
        if (incoming != null && !heavy && !player.attack().running()) {
            block = true;
            attack = false;
            direction = incoming.x() > player.x() ? 1 : -1;
        }
        int special = target != null && target.kind() == EnemyKind.HARBOR_CAPTAIN && attack
            && player.specialCharges(0) > 0 ? 0 : -1;
        return new PlayerInput(direction, jump, attack, dodge, false, block, special);
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
                game.getScreen().render(0);
                capture("13-archer.png");
                archerCaptured = true;
            }
            if (!arrowCaptured && world.arrows().stream().anyMatch(arrow ->
                    Math.abs(arrow.x() - player.x()) > 40
                            && Math.abs(arrow.x() - player.x()) < 160)) {
                game.getScreen().render(0);
                capture("14-arrows.png");
                arrowCaptured = true;
            }
            if (target != null && target.boss()
                    && (target.state() == br.com.thecruzader.gameplay.Enemy.State.BLOCK
                        || target.state() == br.com.thecruzader.gameplay.Enemy.State.DASH
                        || (target.state() == br.com.thecruzader.gameplay.Enemy.State.JUMP
                            && target.vy() < 0))
                    && bossCaptures.add(target.state())) {
                game.getScreen().render(0);
                capture("15-boss-" + target.state().name().toLowerCase(java.util.Locale.ROOT) + ".png");
            }
            if (landmark < landmarks.length && player.x() >= landmarks[landmark]) {
                game.getScreen().render(0);
                capture(names[landmark++]);
            }
        }
        // Boss moves are captured when encountered; a successful fight need not use every move.
        if (!archerCaptured || !arrowCaptured || !angelsCaptured
                || landmark != landmarks.length
                || world.state() != br.com.thecruzader.gameplay.GameWorld.State.WON)
            throw new IllegalStateException("Level tour failed at x=" + world.player().x()
                + ", state=" + world.state() + ", archer=" + archerCaptured + ", arrow=" + arrowCaptured
                + ", angels=" + angelsCaptured + ", boss=" + bossCaptures + ", landmarks=" + landmark);
        game.getScreen().render(0);
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
            checkAnchoredGroundDetails(renderer, level);
            var arenaWorld = visualWorld(level, level.arena().x() + 8,
                    java.util.List.of(level.enemies().stream().filter(e ->
                            e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow()));
            arenaWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    br.com.thecruzader.input.PlayerInput.NONE);
            for (int settle = 0; settle < 12; settle++) renderer.render(arenaWorld, .1f, 1);
            float lockedCamera = renderer.cameraX();
            arenaWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                new br.com.thecruzader.input.PlayerInput(1, true, false, true, false, false, -1));
            renderer.render(arenaWorld, .1f, 1);
            if (renderer.cameraX() != lockedCamera)
                throw new IllegalStateException("Arena camera followed the player");
            var arenaHud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false, arenaWorld.player());
            try {
                arenaHud.announce("notice.arena");
                arenaHud.render(arenaWorld, .35f, false, 0);
                capture("24-english-arena.png");
            } finally { arenaHud.dispose(); }
            System.out.println("ARENA_SMOKE_PASS: locked camera, sealed gates, boss health bar and localized announcement");
            capturePassages(renderer, level);
            checkChapelViews(renderer, level);
            checkDynamicCombat(renderer, level, assets);
            checkSpearman(renderer, level);
            checkEnemyVariants(renderer, level);
            checkSpecialistEffects(renderer, level, assets);
            checkMusicThemes(assets);
            var sentinel = new br.com.thecruzader.level.LevelData.Spawn(142, 64,
                    br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL);
            var guardian = level.enemies().stream().filter(e ->
                    e.kind() == br.com.thecruzader.level.LevelData.EnemyKind.GUARDIAN).findFirst().orElseThrow();
            var enemyWorld = visualWorld(level, 100, java.util.List.of(sentinel, guardian));
            for (int tick = 0; tick < 800 && enemyWorld.enemies().getFirst().health() > 0; tick++)
                enemyWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(0, false, true, false, false, false, -1));
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
            var translatedHud = new br.com.thecruzader.presentation.Hud(assets, resources.definition(), false, playerWorld.player());
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
                        tick % 90 == 0, false, false, false, false, -1));
        if (world.state() != br.com.thecruzader.gameplay.GameWorld.State.DEAD)
            throw new IllegalStateException("Death input setup failed at " + world.player().x() + "," + world.player().y()
                + ", health=" + world.player().health() + ", paused=" + world.paused());
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

    private void checkAnchoredGroundDetails(br.com.thecruzader.presentation.WorldRenderer renderer,
                                            br.com.thecruzader.level.LevelData level) {
        for (int spawnX : new int[]{700, 701, 715, 732, 733, 748}) {
            renderer.render(visualWorld(level, spawnX, level.enemies()), 0, 1);
            float scale = Gdx.graphics.getBackBufferWidth() / 960f;
            int screenX = Math.round((1005 - (renderer.cameraX() - 480)) * scale);
            Pixmap pixel = Pixmap.createFromFrameBuffer(screenX, Math.round(65 * scale), 1, 1);
            try {
                if ((pixel.getPixel(0, 0) >>> 8) != 0x727661)
                    throw new IllegalStateException("Ground vegetation moved with camera at spawn " + spawnX);
            } finally {
                pixel.dispose();
            }
        }
        System.out.println("SCENERY_ANCHOR_SMOKE_PASS: ground details stay fixed while camera crosses tile boundaries");
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

    private void capturePassages(br.com.thecruzader.presentation.WorldRenderer renderer,
                                 br.com.thecruzader.level.LevelData level) {
        double[][] viewpoints = {{1700, 64}, {2050, 64}, {5000, 160}, {5100, 384}, {6900, 384}};
        String[] names = {"36-crypt-entrance", "36-crypt-interior", "37-gallery-interior",
            "37-gallery-rooftop", "38-mountain-ridge"};
        for (int i = 0; i < viewpoints.length; i++) {
            var world = new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
                level.width(), level.height(),
                new br.com.thecruzader.level.LevelData.Point(viewpoints[i][0], viewpoints[i][1]),
                level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(),
                level.solids(), level.enemies()));
            for (int settle = 0; settle < 10; settle++) renderer.render(world, .1f, 1);
            captureIntroFrame(names[i] + ".png");
        }
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
                new br.com.thecruzader.input.PlayerInput(1, false, false, true, false, false, -1));
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

    private void checkDynamicCombat(br.com.thecruzader.presentation.WorldRenderer renderer,
                                    br.com.thecruzader.level.LevelData level,
                                    br.com.thecruzader.presentation.GameAssets assets) {
        var guardian = level.enemies().stream().filter(e -> e.kind().boss).findFirst().orElseThrow();
        var sentinel = new br.com.thecruzader.level.LevelData.Spawn(145, 64,
            br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL);
        var world = visualWorld(level, 100, java.util.List.of(sentinel, guardian));
        int windup = 0;
        for (int tick = 0; tick < 180 && world.player().parryFlash() == 0; tick++) {
            if (world.enemies().getFirst().state() == br.com.thecruzader.gameplay.Enemy.State.WINDUP)
                windup++;
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                new br.com.thecruzader.input.PlayerInput(0, false, false, false, false, windup >= 42, -1));
        }
        if (world.player().parryFlash() == 0 || world.enemies().getFirst().stagger() == 0
            || !world.hitStopped()) throw new IllegalStateException("Timed parry failed");
        renderer.render(world, 0, 1);
        captureIntroFrame("32-combat-parry.png");
        world.setPaused(true);
        long frozen = frameHash();
        renderer.render(world, .1f, 1);
        if (frameHash() != frozen) throw new IllegalStateException("Paused combat effects moved");
        world.setPaused(false);
        world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
            new br.com.thecruzader.input.PlayerInput(0, false, true, false, false, false, -1));
        for (int tick = 0; tick < 60 && world.enemies().getFirst().health() == 3; tick++)
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        if (world.enemies().getFirst().health() != 2) throw new IllegalStateException("Parry counterattack failed");
        for (int tick = 0; tick < 8; tick++)
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        renderer.render(world, 0, 1);
        captureIntroFrame("33-combat-counterattack.png");

        var airborne = new br.com.thecruzader.gameplay.GameWorld(new br.com.thecruzader.level.LevelData(
            level.width(), level.height(), new br.com.thecruzader.level.LevelData.Point(145, 150),
            level.checkpoint(), level.bandages(), level.gate(), level.exit(), level.arena(), level.solids(),
            java.util.List.of(sentinel, guardian)));
        airborne.update(br.com.thecruzader.gameplay.GameWorld.STEP,
            new br.com.thecruzader.input.PlayerInput(0, false, true, false, true, false, -1));
        for (int tick = 0; tick < 60 && airborne.enemies().getFirst().health() == 3; tick++)
            airborne.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        if (airborne.enemies().getFirst().health() != 2 || airborne.player().grounded()
            || !airborne.hitStopped()) throw new IllegalStateException("Downward bounce failed");
        double beforeBounce = airborne.player().y();
        renderer.render(airborne, 0, 1);
        captureIntroFrame("34-combat-downward-hit.png");
        for (int tick = 0; tick < 12; tick++)
            airborne.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        if (airborne.player().y() <= beforeBounce)
            throw new IllegalStateException("Downward hit did not launch player");
        renderer.render(airborne, 0, 1);
        captureIntroFrame("34-combat-bounce.png");
        var hurtWorld = visualWorld(level, 100, java.util.List.of(sentinel, guardian));
        for (int tick = 0; tick < 240 && hurtWorld.player().health() == hurtWorld.player().maxHealth(); tick++)
            hurtWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        if (hurtWorld.player().hurtFlash() <= 0)
            throw new IllegalStateException("Player damage feedback did not start");
        var hurtAudio = new br.com.thecruzader.presentation.AudioPlayer(assets);
        hurtAudio.play(hurtWorld.drainEvents());
        renderer.render(hurtWorld, 0, 1);
        captureIntroFrame("38-player-hurt-impact.png");
        hurtWorld.setPaused(true);
        long hurtFrozen = frameHash();
        renderer.render(hurtWorld, .1f, 1);
        if (frameHash() != hurtFrozen) throw new IllegalStateException("Paused player damage feedback moved");
        hurtWorld.setPaused(false);
        for (int tick = 0; tick < 12; tick++)
            hurtWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        renderer.render(hurtWorld, 0, 1);
        captureIntroFrame("38-player-hurt-fade.png");
        for (int tick = 0; tick < 28; tick++)
            hurtWorld.update(br.com.thecruzader.gameplay.GameWorld.STEP, br.com.thecruzader.input.PlayerInput.NONE);
        if (hurtWorld.player().hurtFlash() != 0)
            throw new IllegalStateException("Player damage feedback did not expire");
        renderer.render(hurtWorld, 0, 1);
        captureIntroFrame("38-player-hurt-recovered.png");
        hurtAudio.setPaused(true);
        System.out.println("PLAYER_HURT_SMOKE_PASS: armor impact, sparks, camera shake, damage edges, pause and expiry");
        System.out.println("DYNAMIC_COMBAT_SMOKE_PASS: timed parry, paused effects, counterattack, knockback and aerial bounce");
    }

    private void checkSpearman(br.com.thecruzader.presentation.WorldRenderer renderer,
                               br.com.thecruzader.level.LevelData level) {
        var guardian = level.enemies().stream().filter(e -> e.kind().boss).findFirst().orElseThrow();
        var spear = new br.com.thecruzader.level.LevelData.Spawn(220, 64,
            br.com.thecruzader.level.LevelData.EnemyKind.SPEARMAN);
        var world = visualWorld(level, 100, java.util.List.of(spear, guardian));
        var captured = java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.Enemy.State.class);
        for (int tick = 0; tick < 250; tick++) {
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                new br.com.thecruzader.input.PlayerInput(0, false, false, false, false, true, -1));
            var state = world.enemies().getFirst().state();
            if ((state == br.com.thecruzader.gameplay.Enemy.State.WINDUP
                || state == br.com.thecruzader.gameplay.Enemy.State.ATTACK
                || state == br.com.thecruzader.gameplay.Enemy.State.RECOVER) && captured.add(state)) {
                renderer.render(world, 0, 1);
                captureIntroFrame("35-spearman-" + state.name().toLowerCase(java.util.Locale.ROOT) + ".png");
            }
        }
        if (captured.size() != 3 || world.player().health() != world.player().maxHealth())
            throw new IllegalStateException("Spearman attack cycle or shield defense failed");
        var defeated = visualWorld(level, 180, java.util.List.of(spear, guardian));
        for (int tick = 0; tick < 600 && defeated.enemies().getFirst().health() > 0; tick++)
            defeated.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                new br.com.thecruzader.input.PlayerInput(0, false, true, false, false, false, -1));
        if (defeated.enemies().getFirst().health() != 0)
            throw new IllegalStateException("Spearman could not be defeated");
        renderer.render(defeated, .1f, 1);
        renderer.render(defeated, .1f, 1);
        captureIntroFrame("35-spearman-death.png");
        System.out.println("SPEARMAN_SMOKE_PASS: windup, thrust, recovery, shield defense and death");
    }

    private void checkEnemyVariants(br.com.thecruzader.presentation.WorldRenderer renderer,
                                    br.com.thecruzader.level.LevelData level) {
        var guardian = level.enemies().stream().filter(e -> e.kind().boss).findFirst().orElseThrow();
        for (var kind : java.util.List.of(br.com.thecruzader.level.LevelData.EnemyKind.SENTINEL,
            br.com.thecruzader.level.LevelData.EnemyKind.ARCHER,
            br.com.thecruzader.level.LevelData.EnemyKind.SHIELD_BEARER,
            br.com.thecruzader.level.LevelData.EnemyKind.BRUTE,
            br.com.thecruzader.level.LevelData.EnemyKind.OIL_THROWER)) {
            boolean oil = kind == br.com.thecruzader.level.LevelData.EnemyKind.OIL_THROWER;
            for (int facing : oil ? new int[]{-1} : new int[]{-1, 1}) {
                var spawn = new br.com.thecruzader.level.LevelData.Spawn(oil ? 300 : facing < 0 ? 365 : 235, 64, kind);
                var world = visualWorld(level, oil ? 100 : 300, java.util.List.of(spawn, guardian));
                var captured = java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.Enemy.State.class);
                boolean flying = false, burning = false;
                String prefix = "36-" + kind.name().toLowerCase(java.util.Locale.ROOT) + (facing > 0 ? "-right" : "");
                if (!oil && facing < 0) {
                    renderer.render(world, 0, 1);
                    captureIntroFrame(prefix + "-idle.png");
                }
                var midPhaseCaptured = java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.Enemy.State.class);
                for (int tick = 0; tick < 400 && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
                    int direction = oil && !world.oilPots().isEmpty() ? -1 : 0;
                    world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                        new br.com.thecruzader.input.PlayerInput(direction, false, false, false, false, !oil, -1));
                    var state = world.enemies().getFirst().state();
                    if ((state == br.com.thecruzader.gameplay.Enemy.State.WINDUP
                        || state == br.com.thecruzader.gameplay.Enemy.State.ATTACK
                        || state == br.com.thecruzader.gameplay.Enemy.State.RECOVER) && captured.add(state)) {
                        renderer.render(world, 0, 1);
                        captureIntroFrame(prefix + "-" + state.name().toLowerCase(java.util.Locale.ROOT) + ".png");
                    }
                    var enemy = world.enemies().getFirst();
                    if (!oil && (state == br.com.thecruzader.gameplay.Enemy.State.WINDUP
                        || state == br.com.thecruzader.gameplay.Enemy.State.ATTACK
                        || state == br.com.thecruzader.gameplay.Enemy.State.RECOVER)
                        && enemy.attackProgress() >= .5 && midPhaseCaptured.add(state)) {
                        renderer.render(world, 0, 1);
                        captureIntroFrame(prefix + "-" + state.name().toLowerCase(java.util.Locale.ROOT) + "-middle.png");
                    }
                    if (oil && !flying && world.oilPots().stream().anyMatch(pot -> !pot.burning())) {
                        renderer.render(world, 0, 1);
                        captureIntroFrame(prefix + "-pot.png");
                        flying = true;
                    }
                    if (oil && !burning && world.oilPots().stream().anyMatch(pot -> pot.burning())) {
                        renderer.render(world, 0, 1);
                        captureIntroFrame(prefix + "-fire.png");
                        burning = true;
                    }
                }
                if (captured.size() != 3 || (oil && (!flying || !burning)))
                    throw new IllegalStateException("Enemy variant smoke failed: " + kind + ", states=" + captured
                        + ", pot=" + flying + ", fire=" + burning);
            }
        }
        System.out.println("ENEMY_VARIANTS_SMOKE_PASS: sentinel sword, archer draw/release, lowered shield, heavy mace, ballistic pot and fire");
    }

    private void checkSpecialistEffects(br.com.thecruzader.presentation.WorldRenderer renderer,
                                        br.com.thecruzader.level.LevelData level, br.com.thecruzader.presentation.GameAssets assets) {
        var guardian = level.enemies().stream().filter(e -> e.kind().boss).findFirst().orElseThrow();
        var heard = java.util.EnumSet.noneOf(br.com.thecruzader.gameplay.GameEvent.class);
        var audio = new br.com.thecruzader.presentation.AudioPlayer(assets);
        for (var kind : java.util.List.of(br.com.thecruzader.level.LevelData.EnemyKind.BRUTE,
            br.com.thecruzader.level.LevelData.EnemyKind.SPEARMAN,
            br.com.thecruzader.level.LevelData.EnemyKind.OIL_THROWER)) {
            boolean oil = kind == br.com.thecruzader.level.LevelData.EnemyKind.OIL_THROWER;
            var world = visualWorld(level, oil ? 100 : 300,
                java.util.List.of(new br.com.thecruzader.level.LevelData.Spawn(oil ? 300 : 365, 64, kind), guardian));
            boolean effectCaptured = false;
            for (int tick = 0; tick < 280 && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
                world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(oil && !world.oilPots().isEmpty() ? -1 : 0,
                        false, false, false, false, !oil, -1));
                var events = world.drainEvents();
                heard.addAll(events);
                audio.play(events);
                if (tick % 8 == 0) {
                    renderer.render(world, 0, 1);
                    captureIntroFrame("specialist-frames/" + kind.name().toLowerCase(java.util.Locale.ROOT)
                        + "-" + String.format(java.util.Locale.ROOT, "%03d", tick) + ".png");
                }
                if (!effectCaptured && world.effects().stream().anyMatch(effect -> effect.age() >= .1)) {
                    world.setPaused(true);
                    audio.setPaused(true);
                    renderer.render(world, 0, 1);
                    long frozen = frameHash();
                    renderer.render(world, .1f, 1);
                    if (frozen != frameHash()) throw new IllegalStateException("Paused specialist particles moved");
                    captureIntroFrame("37-" + kind.name().toLowerCase(java.util.Locale.ROOT) + "-impact.png");
                    audio.toggleMute();
                    audio.toggleMute();
                    audio.setPaused(false);
                    world.setPaused(false);
                    effectCaptured = true;
                }
            }
            if (kind != br.com.thecruzader.level.LevelData.EnemyKind.SPEARMAN && !effectCaptured)
                throw new IllegalStateException("No specialist impact captured for " + kind);
        }
        for (var expected : java.util.List.of(br.com.thecruzader.gameplay.GameEvent.MACE_LIFT,
            br.com.thecruzader.gameplay.GameEvent.MACE_SWING, br.com.thecruzader.gameplay.GameEvent.MACE_IMPACT,
            br.com.thecruzader.gameplay.GameEvent.SPEAR_DRAW, br.com.thecruzader.gameplay.GameEvent.SPEAR_THRUST,
            br.com.thecruzader.gameplay.GameEvent.OIL_PRIME, br.com.thecruzader.gameplay.GameEvent.OIL_THROW,
            br.com.thecruzader.gameplay.GameEvent.POT_SHATTER, br.com.thecruzader.gameplay.GameEvent.FIRE_IGNITE))
            if (!heard.contains(expected)) throw new IllegalStateException("Specialist cue missing: " + expected);
        audio.setPaused(true);
        System.out.println("SPECIALIST_EFFECTS_SMOKE_PASS: nine sound cues, phase poses, mace debris, ceramic shatter, ignition and paused particles");
    }

    private void checkMusicThemes(br.com.thecruzader.presentation.GameAssets assets) {
        var audio = new br.com.thecruzader.presentation.AudioPlayer(assets);
        var menu = assets.menuMusic();
        var exploration = assets.ambience();
        var boss = assets.bossMusic();
        var victory = assets.victoryMusic();
        var requiem = assets.requiem();
        if (!menu.isLooping() || !exploration.isLooping() || !boss.isLooping()
            || !victory.isLooping() || requiem.isLooping())
            throw new IllegalStateException("Music loop configuration is incorrect");
        assertMusic(menu, assets);
        audio.setPaused(true);
        audio.toggleMute();
        audio.toggleMute();
        assertMusic(null, assets);
        audio.setPaused(false);
        assertMusic(menu, assets);
        audio.endDeath();
        assertMusic(exploration, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.ARENA_START));
        assertMusic(boss, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.BOSS_DEFEATED));
        assertMusic(victory, assets);
        audio.setPaused(true);
        assertMusic(null, assets);
        audio.setPaused(false);
        assertMusic(victory, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.GATES_OPEN,
            br.com.thecruzader.gameplay.GameEvent.VICTORY));
        assertMusic(victory, assets);
        audio.endDeath();
        assertMusic(exploration, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.ARENA_START));
        audio.toggleMute();
        assertMusic(null, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.BOSS_DEFEATED));
        assertMusic(null, assets);
        audio.toggleMute();
        assertMusic(victory, assets);
        audio.toggleMute();
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.ARENA_START));
        assertMusic(null, assets);
        audio.toggleMute();
        assertMusic(boss, assets);
        audio.play(java.util.List.of(br.com.thecruzader.gameplay.GameEvent.DEATH));
        assertMusic(requiem, assets);
        audio.endDeath();
        assertMusic(exploration, assets);
        audio.startMenu();
        assertMusic(menu, assets);
        audio.setPaused(true);
        System.out.println("MUSIC_THEMES_SMOKE_PASS: exclusive menu/exploration/boss/victory loops, muted transitions, pause, death and respawn");
    }

    private void assertMusic(com.badlogic.gdx.audio.Music expected,
                             br.com.thecruzader.presentation.GameAssets assets) {
        for (var music : java.util.List.of(assets.menuMusic(), assets.ambience(), assets.bossMusic(),
            assets.victoryMusic(), assets.requiem()))
            if (music.isPlaying() != (music == expected))
                throw new IllegalStateException("Unexpected active music track");
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
