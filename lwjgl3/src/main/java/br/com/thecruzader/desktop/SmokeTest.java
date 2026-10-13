package br.com.thecruzader.desktop;

import br.com.thecruzader.CrusaderGame;
import br.com.thecruzader.screen.PlayScreen;

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

    SmokeTest(String output) {
        this.output = output;
    }

    @Override
    public void create() {
        Gdx.files.absolute(output).mkdirs();
        game.create();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void render() {
        game.render();
        frame++;
        if (frame == 12) {
            capture("01-menu.png");
            game.startGame();
        }
        if (frame == 30) Gdx.input.getInputProcessor().keyDown(Input.Keys.D);
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
            Gdx.app.exit();
        }
    }

    private void captureLevelTour() {
        Gdx.input.getInputProcessor().keyUp(Input.Keys.L);
        game.startGame();
        var world = ((PlayScreen) game.getScreen()).world();
        double[] landmarks = {850, 1450, 2370, 2800};
        String[] names = {"06-relic-route.png", "07-sanctuary.png", "08-stairs.png", "09-arena.png"};
        int landmark = 0;
        for (int tick = 0; tick < 120 * 120
                && world.state() == br.com.thecruzader.gameplay.GameWorld.State.PLAYING; tick++) {
            var player = world.player();
            var target = world.enemies().stream().filter(enemy -> enemy.health() > 0)
                    .min(java.util.Comparator.comparingDouble(enemy -> Math.abs(enemy.x() - player.x())))
                    .orElse(null);
            int direction = 1;
            boolean attack = false, dodge = false;
            if (target != null && Math.abs(target.x() - player.x()) < 180) {
                double distance = target.x() - player.x();
                direction = distance >= 0 ? 1 : -1;
                if (target.state() == br.com.thecruzader.gameplay.Enemy.State.WINDUP) {
                    direction = -target.facing();
                    dodge = !player.attack().running();
                } else {
                    attack = Math.abs(distance) < 80;
                    if (player.attack().running()) direction = 0;
                }
            }
            boolean jump = player.grounded() && direction > 0 && world.level().solids().stream()
                    .anyMatch(solid -> solid.x() >= 2208 && solid.x() < 2560
                            && solid.x() >= player.x() + br.com.thecruzader.gameplay.Player.WIDTH
                            && solid.x() - (player.x() + br.com.thecruzader.gameplay.Player.WIDTH) < 85
                            && solid.top() > player.y() && solid.top() - player.y() < 160);
            world.update(br.com.thecruzader.gameplay.GameWorld.STEP,
                    new br.com.thecruzader.input.PlayerInput(direction, jump, attack, dodge, false, false));
            if (landmark < landmarks.length && player.x() >= landmarks[landmark]) {
                game.render();
                capture(names[landmark++]);
            }
        }
        if (landmark != landmarks.length
                || world.state() != br.com.thecruzader.gameplay.GameWorld.State.WON)
            throw new IllegalStateException("Level tour failed at x=" + world.player().x());
        game.render();
        capture("10-victory.png");
        System.out.println("LEVEL_TOUR_PASS: relic route, sanctuary, stairs, arena and victory");
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
        game.dispose();
    }
}
