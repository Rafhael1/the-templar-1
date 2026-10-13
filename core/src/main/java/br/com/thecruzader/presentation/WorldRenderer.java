package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.Attack;
import br.com.thecruzader.gameplay.Enemy;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;
import br.com.thecruzader.level.Bounds;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class WorldRenderer implements Disposable {
    private final GameAssets assets;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(960, 540, camera);
    private final OrthogonalTiledMapRenderer mapRenderer;

    public WorldRenderer(GameAssets assets) {
        this.assets = assets;
        mapRenderer = new OrthogonalTiledMapRenderer(assets.map(), 1, batch);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render(GameWorld world) {
        ScreenUtils.clear(0.035f, 0.05f, 0.08f, 1);
        viewport.apply();
        camera.position.set(
                (float)
                        Math.max(
                                480,
                                Math.min(world.level().width() - 480, world.player().x() + 140)),
                270,
                0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        float left = camera.position.x - 480;
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(assets.texture("sky"), left, 0, 960, 540);
        float offset = (camera.position.x * .32f) % 960;
        batch.draw(assets.texture("ruins"), left - offset, 0, 960, 540);
        batch.draw(assets.texture("ruins"), left - offset + 960, 0, 960, 540);
        batch.end();
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.begin();
        drawObjects(world);
        for (Enemy enemy : world.enemies()) if (enemy.health() > 0) drawEnemy(enemy, world.time());
        drawPlayer(world.player(), world.time());
        for (int i = 0; i < 30; i++) {
            float x = left + (float) ((i * 173 + world.time() * (8 + i % 7)) % 960);
            float y = 110 + (float) ((i * 67 + world.time() * 8) % 320);
            rect(x, y, 2, 2, new Color(.8f, .66f, .45f, .24f));
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawObjects(GameWorld world) {
        double x = world.level().checkpoint().x(), y = world.level().checkpoint().y();
        rect(x - 14, y, 54, 24, Color.valueOf("655864"));
        rect(x - 20, y + 24, 66, 7, GameAssets.GOLD);
        cross(x + 8, y + 33, 32, GameAssets.PAPER);
        if (world.checkpoint()) flame(x + 12, y + 77, world.time());
        if (!world.relic()) {
            Bounds r = world.level().relic();
            rect(
                    r.x() - 8,
                    r.y() - 5,
                    r.width() + 16,
                    r.height() + 12,
                    new Color(.9f, .7f, .4f, .12f));
            cross(r.x() + 8, r.y() + 3 + Math.sin(world.time() * 3) * 3, 25, GameAssets.GOLD);
        }
        Bounds gate = world.level().gate();
        rect(gate.x() - 18, gate.y(), 16, gate.height() + 20, Color.valueOf("807167"));
        rect(gate.right() + 3, gate.y(), 16, gate.height() + 20, Color.valueOf("807167"));
        rect(gate.x() - 18, gate.top() + 10, gate.width() + 37, 13, GameAssets.GOLD);
        for (int i = 0; i < 4; i++)
            rect(
                    gate.x() + i * 12,
                    world.gateLocked() ? gate.y() : gate.top() - 25,
                    4,
                    world.gateLocked() ? gate.height() + 10 : 35,
                    Color.valueOf("a29b88"));
        for (int torch : new int[] {230, 930, 1570, 2330, 2920}) {
            rect(torch, 72, 5, 27, Color.valueOf("54444b"));
            flame(torch + 2, 103, world.time());
        }
    }

    private void cross(double x, double y, double height, Color color) {
        rect(x, y, 5, height, color);
        rect(x - 7, y + height * .6, 19, 5, color);
    }

    private void flame(double x, double y, double time) {
        rect(x - 14, y - 12, 29, 37, new Color(1, .55f, .2f, .08f));
        rect(x - 4, y, 9, 14 + Math.sin(time * 13 + x) * 3, Color.valueOf("d08a51"));
        rect(x - 1, y + 2, 4, 9, Color.valueOf("f6d59b"));
    }

    private void drawPlayer(Player p, double time) {
        if (p.invulnerable() > 0 && !p.dodging() && (int) (time * 24) % 2 == 0) return;
        int row =
                p.dodging()
                        ? 4
                        : p.attack().running()
                                ? 3
                                : !p.grounded() ? 2 : Math.abs(p.speed()) > 1 ? 1 : 0;
        int frame = (int) (time * (row == 1 ? 12 : 5)) % 8;
        if (row == 3)
            frame =
                    switch (p.attack().phase()) {
                        case WINDUP -> 0;
                        case ACTIVE -> 2;
                        case RECOVERY -> 4;
                        case READY -> 5;
                    };
        if (p.dodging()) {
            batch.setColor(1, 1, 1, .22f);
            sprite(
                    assets.characters[row][frame],
                    p.x() - 18 - p.facing() * 24,
                    p.y() - 10,
                    64,
                    80,
                    p.facing());
        }
        batch.setColor(Color.WHITE);
        sprite(assets.characters[row][frame], p.x() - 18, p.y() - 10, 64, 80, p.facing());
        double handX = p.x() + (p.facing() > 0 ? 29 : -1);
        double handY = p.y() + 27;
        float angle = -90;
        if (p.attack().running()) {
            angle = p.attackDirection() == Player.AttackDirection.DOWN ? -90
                    : p.attack().phase() == Attack.Phase.WINDUP ? 90
                    : p.facing() > 0 ? 0 : 180;
            if (p.attackDirection() == Player.AttackDirection.DOWN) {
                handX = p.x() + Player.WIDTH / 2;
                handY = p.y() + 7;
            }
        }
        sword(handX, handY, angle, 1, false);
        shield(p);

    }

    private void drawEnemy(Enemy e, double time) {
        boolean warning = e.state() == Enemy.State.WINDUP;
        batch.setColor(
                e.flash() > 0 ? Color.SALMON : warning ? new Color(1, .75f, .65f, 1) : Color.WHITE);
        float scale = e.boss() ? 1.3f : 1;
        sprite(
                assets.characters[e.boss() ? 7 : 6][(int) (time * 7) % 8],
                e.x() - 17,
                e.y() - 10,
                64 * scale,
                80 * scale,
                e.facing());
        rect(e.x(), e.y() + (e.boss() ? 93 : 72), 34, 3, Color.valueOf("342e3c"));
        rect(
                e.x(),
                e.y() + (e.boss() ? 93 : 72),
                34.0 * e.health() / e.maxHealth(),
                3,
                GameAssets.GOLD);
        if (warning) {
            rect(e.x() + 12, e.y() + (e.boss() ? 102 : 82), 4, 12, Color.valueOf("edab71"));
            rect(e.x() + 12, e.y() + (e.boss() ? 97 : 77), 4, 3, Color.valueOf("edab71"));
        }
        float angle = switch (e.state()) {
            case WINDUP -> 90;
            case ATTACK, RECOVER -> e.facing() > 0 ? 0 : 180;
            default -> -90;
        };
        sword(e.facing() > 0 ? e.bounds().right() : e.x(), e.y() + 27, angle,
                scale, true);

    }

    private void shield(Player p) {
        double x = p.x() + (p.blocking() ? (p.facing() > 0 ? 22 : -16)
                : (p.facing() > 0 ? -7 : 20));
        double y = p.y() + 12;
        batch.setColor(Color.WHITE);
        sprite(assets.shields[p.blockFlash() > 0 ? 1 : 0], Math.round(x), Math.round(y),
                24, 36, p.facing());
        if (p.blockFlash() > 0) {
            rect(x - 4, y + 31, 3, 7, Color.WHITE);
            rect(x + 25, y + 14, 6, 3, GameAssets.GOLD);
        }
    }

    private void sword(double x, double y, float angle, float scale, boolean enemy) {
        // Keep the sprite's pixel scale constant across attack phases.
        // The grip sits at the hand; cardinal rotations preserve the pixel grid.
        batch.setColor(Color.WHITE);
        batch.draw(assets.swords[enemy ? 1 : 0], (float) Math.round(x) - 7,
                (float) Math.round(y) - 8, 7, 8, 40, 16, scale, scale, angle);
    }

    private void sprite(
            TextureRegion region, double x, double y, float width, float height, int facing) {
        batch.draw(
                region,
                (float) x + (facing < 0 ? width : 0),
                (float) y,
                facing < 0 ? -width : width,
                height);
    }

    private void rect(double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, (float) x, (float) y, (float) width, (float) height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
    }
}
