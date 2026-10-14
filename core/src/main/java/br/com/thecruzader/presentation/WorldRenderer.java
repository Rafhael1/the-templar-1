package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.Attack;
import br.com.thecruzader.gameplay.BossRelease;
import br.com.thecruzader.gameplay.Enemy;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.gameplay.Player;
import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelDefinition;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class WorldRenderer implements Disposable {
    private static final Color STONE_SHADOW = Color.valueOf("655864");
    private static final Color STONE = Color.valueOf("807167");
    private static final Color WOOD_SHADOW = Color.valueOf("54444b");
    private static final Color WOOD_DARK = Color.valueOf("594333");
    private static final Color WOOD = Color.valueOf("8e7356");
    private static final Color STEEL_DARK = Color.valueOf("46525c");
    private static final Color LEATHER = Color.valueOf("655346");
    private static final Color SAND = Color.valueOf("8e7b68");
    private static final Color FLAME = Color.valueOf("d08a51");
    private static final Color FLAME_LIGHT = Color.valueOf("f6d59b");
    private static final Color HEALTH_BACK = Color.valueOf("342e3c");
    private static final Color WARNING = Color.valueOf("edab71");
    private static final Color BRASS = Color.valueOf("c6b590");
    private static final Color CLOUD = new Color(.78f, .72f, .65f, .10f);
    private static final Color CLOUD_LIGHT = new Color(.78f, .72f, .65f, .08f);
    private static final Color DUST = new Color(.8f, .66f, .45f, .24f);
    private static final Color FIRE_GLOW = new Color(1, .55f, .2f, .08f);
    private static final Color WINDUP = new Color(1, .75f, .65f, 1);
    private static final Color DASH_TRAIL = new Color(.82f, .58f, .31f, .25f);
    private final Color smoke = new Color();
    private GameWorld displayedWorld;
    private Player displayedPlayer;
    private float[] enemyDeaths;
    private float playerDeath, cameraX, entranceClosure, exitClosure;
    private final RoomTransition room = new RoomTransition();
    private final EnclosedAreaRenderer enclosed;
    private final TemplarDeathAnimation templarDeath;
    private final AngelReleaseAnimation angels;

    public float cameraX() { return camera.position.x; }
    public float deathElapsed() { return playerDeath; }
    public boolean interiorVisible() { return room.interior(); }
    public float transitionOpacity() { return room.opacity(); }

    private final Texture[] scenery;
    private final SceneryTransition transition;
    private final LevelResources level;
    private final GameAssets assets;
    private final SpriteBatch batch = new SpriteBatch();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(960, 540, camera);
    private final OrthogonalTiledMapRenderer mapRenderer;

    public WorldRenderer(GameAssets assets, LevelResources level) {
        this.assets = assets;
        this.level = level;
        transition = new SceneryTransition(level.definition());
        enclosed = new EnclosedAreaRenderer(assets, level);
        templarDeath = new TemplarDeathAnimation(assets);
        angels = new AngelReleaseAnimation(assets);
        scenery = new Texture[level.definition().regions().size()];
        for (int region = 0; region < scenery.length; region++) {
            scenery[region] = level.scenery(region);
            scenery[region].setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        }
        mapRenderer = new OrthogonalTiledMapRenderer(level.map(), 1, batch);
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void render(GameWorld world, float delta, float alpha) {
        updateDeaths(world, delta);
        ScreenUtils.clear(0.035f, 0.05f, 0.08f, 1);
        viewport.apply();
        room.update(world.insideArena(), delta, world.paused());
        boolean interior = room.interior();
        double target = interior ? world.level().arena().x() + world.level().arena().width() / 2
                : world.player().interpolatedX(alpha) + 140;
        if (!interior) target = Math.max(480, Math.min(world.level().width() - 480, target));
        cameraX = (float) target;
        // The scene switch covers the camera cut; each interior has a stable one-screen view.
        camera.position.set(Math.round(cameraX), interior ? (float) enclosed.bottom(world) + 270 : 270, 0);
        if (!world.paused()) {
            if (world.bossDefeated()) {
                entranceClosure = exitClosure = 1 - BossRelease.opening(world.bossReleaseElapsed());
            } else {
                entranceClosure = approach(entranceClosure, world.arenaLocked() ? 1 : 0, delta * 4);
                exitClosure = approach(exitClosure, world.gateLocked() ? 1 : 0, delta * 4);
            }
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        float left = camera.position.x - 480;
        batch.begin();
        batch.setColor(Color.WHITE);
        if (interior) enclosed.drawInterior(batch, world);
        else {
            batch.draw(assets.texture("sky"), left, 0, 960, 540);
            drawSceneryBackground(left);
            for (int i = 0; i < 6; i++) {
                float cx = left + (float) ((i * 211 + world.time() * 4 - left * .12) % 1150) - 95;
                rect(cx, 380 + i % 3 * 36, 116, 8, CLOUD);
                rect(cx + 24, 388 + i % 3 * 36, 66, 6, CLOUD_LIGHT);
            }
            drawScenery(world, left);
            enclosed.drawExterior(batch, world);
        }
        batch.end();
        if (!interior) {
            batch.setColor(Color.WHITE);
            mapRenderer.setView(camera);
            mapRenderer.render();
        }
        batch.begin();
        drawObjects(world, left, interior);
        for (int i = 0; i < world.enemies().size(); i++) {
            Enemy enemy = world.enemies().get(i);
            if (world.level().insideArena(enemy.x()) != interior) continue;
            if (enemy.x() < left - 140 || enemy.x() > left + 1100) continue;
            if (enemy.health() > 0) drawEnemy(enemy, world.time(), alpha);
            else if (enemyDeaths[i] < 2.2f)
                drawDeath(world, enemy.x(), enemy.y(), enemy.facing(), enemy.boss() ? 1.3f : 1,
                        enemy.archer() ? 3 : enemy.boss() ? 2 : 1, enemyDeaths[i], true);
        }
        for (var arrow : world.arrows()) {
            if (world.level().insideArena(arrow.x()) != interior) continue;
            batch.setColor(Color.WHITE);
            batch.draw(assets.texture("arrow"), (float) arrow.x() - 8, (float) arrow.y() - 3,
                    8, 3, 16, 6, 1, 1, (float) arrow.angle(), 0, 0, 16, 6, false, false);
        }
        if (world.player().health() > 0) drawPlayer(world.player(), world.time(), alpha);
        for (int i = 0; i < 30; i++) {
            float x = left + (float) ((i * 173 + world.time() * (8 + i % 7)) % 960);
            float y = 110 + (float) ((i * 67 + world.time() * 8) % 320);
            rect(x, y, 2, 2, DUST);
        }
        if (interior) enclosed.drawForeground(batch, world);
        if (interior) angels.draw(batch, world);
        if (world.player().health() <= 0) {
            Player player = world.player();
            double y = Math.max(floorBelow(world, player.x(), player.y()), player.y() - 725 * playerDeath * playerDeath);
            templarDeath.draw(batch, player.x(), y, player.facing(), playerDeath, left, camera.position.y - 270);
        }
        if (room.opacity() > 0) {
            float bottom = camera.position.y - 270;
            rect(left, bottom, 960, 540, smoke.set(0, 0, 0, room.opacity()));
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawSceneryBackground(float left) {
        float offset = (camera.position.x * .32f) % 960;
        // Group strips by texture so the blend adds quads without repeatedly flushing the batch.
        for (int region = 0; region < scenery.length; region++) {
            for (float x = (float) Math.floor(left / 8) * 8; x < left + 960; x += 8) {
                float opacity = transition.weight(region, x + 4);
                if (opacity == 0) continue;
                float u = (x - left + offset) / 960;
                batch.setColor(1, 1, 1, opacity);
                batch.draw(scenery[region], x, 0, 8, 540, u, 1, u + 8f / 960, 0);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void updateDeaths(GameWorld world, float delta) {
        if (displayedWorld != world || displayedPlayer != world.player()) {
            displayedWorld = world;
            displayedPlayer = world.player();
            enemyDeaths = new float[world.enemies().size()];
            playerDeath = 0;
            room.reset(world.insideArena());
            entranceClosure = 0;
            exitClosure = world.gateLocked() ? 1 : 0;
        }
        if (world.paused()) return;
        float step = Math.min(delta, .1f);
        if (world.player().health() <= 0) playerDeath += step;
        for (int i = 0; i < enemyDeaths.length; i++)
            if (world.enemies().get(i).health() <= 0) enemyDeaths[i] += step;
    }

    private void drawDeath(GameWorld world, double x, double y, int facing, float scale,
            int kind, float elapsed, boolean fade) {
        double fallingY = Math.max(floorBelow(world, x, y), y - 725 * elapsed * elapsed);
        int frame = Math.min(7, (int) (elapsed * 9));
        batch.setColor(1, 1, 1, fade ? Math.max(0, Math.min(1, 2.2f - elapsed)) : 1);
        sprite(assets.deaths[kind][frame], x - 33 * scale, fallingY - 10 * scale,
                96 * scale, 80 * scale, facing);
        batch.setColor(Color.WHITE);
    }

    private static double floorBelow(GameWorld world, double x, double y) {
        double floor = Double.NEGATIVE_INFINITY;
        for (Bounds solid : world.level().solids())
            if (x + 15 >= solid.x() && x + 15 < solid.right() && solid.top() <= y + .01)
                floor = Math.max(floor, solid.top());
        return floor;
    }

    private void drawObjects(GameWorld world, float left, boolean interior) {
        double x = world.level().checkpoint().x(), y = world.level().checkpoint().y();
        if (!interior) {
            batch.setColor(Color.WHITE);
            batch.draw(assets.texture("sanctuary"), (float) Math.round(x - 32), (float) Math.round(y), 96, 128);
            if (world.checkpoint()) {
                flame(x - 10, y + 40, world.time());
                flame(x + 52, y + 40, world.time() + .7);
            }
        }
        for (int i = 0; i < world.level().bandages().size(); i++) {
            if (world.bandageCollected(i)) continue;
            Bounds r = world.level().bandages().get(i);
            if (world.level().insideArena(r.x() + r.width() / 2) != interior) continue;
            if (r.right() < left - 32 || r.x() > left + 992) continue;
            batch.setColor(Color.WHITE);
            batch.draw(assets.texture("bandages"),
                    (float) Math.round(r.x() + r.width() / 2 - 16),
                    (float) Math.round(r.y() + r.height() / 2 - 16
                            + Math.sin(world.time() * 3) * 2), 32, 32);
        }
        enclosed.drawDoors(batch, world, interior, entranceClosure, exitClosure);
    }

    private static float approach(float current, float target, float step) {
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    private void drawScenery(GameWorld world, float left) {
        for (LevelDefinition.Decoration decoration : level.definition().decorations()) {
            double x = decoration.x(), y = decoration.y();
            if (x < left - 160 || x > left + 1000) continue;
            switch (decoration.kind()) {
                case CRATES -> {
                    rect(x, y, 30, 27, WOOD_DARK);
                    rect(x + 3, y + 3, 24, 21, WOOD);
                    rect(x + 12, y, 4, 27, STEEL_DARK);
                    rect(x + 38, y, 22, 33, LEATHER);
                    rect(x + 38, y + 6, 22, 3, GameAssets.GOLD);
                    rect(x + 38, y + 25, 22, 3, GameAssets.GOLD);
                }
                case FLAG -> {
                    rect(x, y, 4, 115, SAND);
                    Color color = smoke.set((decoration.flagRgb() << 8) | 0xff);
                    for (int strip = 0; strip < 8; strip++) {
                        int sway = (int) (Math.sin(world.time() * 3 + strip * .4) * 3);
                        rect(x + 4 + strip * 4, y + 68 + sway, 4, 44 - strip, color);
                    }
                }
                case SIGN -> {
                    rect(x, y, 8, 62, WOOD_SHADOW);
                    rect(x - 13, y + 47, 40, 18, WOOD);
                    rect(x - 7, y + 54, 25, 3, GameAssets.PAPER);
                    rect(x + 14, y + 50, 4, 11, GameAssets.PAPER);
                }
                case BRAZIER -> {
                    rect(x, y, 53, 8, STONE_SHADOW);
                    rect(x + 9, y + 8, 31, 10, STONE);
                    for (int i = 0; i < 3; i++) {
                        double drift = (world.time() * 14 + i * 17) % 56;
                        rect(x + 15 + Math.sin(drift * .1) * 7, y + 18 + drift, 8, 8,
                                smoke.set(.55f, .50f, .48f, (float) ((1 - drift / 56) * .24)));
                    }
                }
                case TORCH -> {
                    rect(x, y + 8, 5, 27, WOOD_SHADOW);
                    flame(x + 2, y + 39, world.time());
                }
            }
        }
    }

    private void flame(double x, double y, double time) {
        rect(x - 14, y - 12, 29, 37, FIRE_GLOW);
        rect(x - 4, y, 9, 14 + Math.sin(time * 13 + x) * 3, FLAME);
        rect(x - 1, y + 2, 4, 9, FLAME_LIGHT);
    }

    private void drawPlayer(Player p, double time, float alpha) {
        double x = p.interpolatedX(alpha), y = p.interpolatedY(alpha);
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
                    x - 18 - p.facing() * 24,
                    y - 10,
                    64,
                    80,
                    p.facing());
        }
        batch.setColor(Color.WHITE);
        sprite(assets.characters[row][frame], x - 18, y - 10, 64, 80, p.facing());
        double handX = x + (p.facing() > 0 ? 29 : -1);
        double handY = y + 27;
        float angle = -90;
        if (p.attack().running()) {
            angle = p.attackDirection() == Player.AttackDirection.DOWN ? -90
                    : p.attack().phase() == Attack.Phase.WINDUP ? 90
                    : p.facing() > 0 ? 0 : 180;
            if (p.attackDirection() == Player.AttackDirection.DOWN) {
                handX = x + Player.WIDTH / 2;
                handY = y + 7;
            }
        }
        sword(handX, handY, angle, 1, false);
        shield(p, x, y);

    }

    private void drawEnemy(Enemy e, double time, float alpha) {
        double x = e.interpolatedX(alpha), y = e.interpolatedY(alpha);
        boolean warning = e.state() == Enemy.State.WINDUP
                || e.state() == Enemy.State.DASH_WINDUP || e.state() == Enemy.State.JUMP_WINDUP;
        batch.setColor(
                e.flash() > 0 ? Color.SALMON : warning ? WINDUP : Color.WHITE);
        float scale = e.boss() ? 1.3f : 1;
        sprite(
                assets.characters[e.archer() ? 8 : e.boss() ? 7 : 6][(int) (time * 7) % 8],
                x - 17,
                y - 10,
                64 * scale,
                80 * scale,
                e.facing());
        rect(x, y + (e.boss() ? 93 : 72), 34, 3, HEALTH_BACK);
        rect(
                x,
                y + (e.boss() ? 93 : 72),
                34.0 * e.health() / e.maxHealth(),
                3,
                GameAssets.GOLD);
        if (warning) {
            rect(x + 12, y + (e.boss() ? 102 : 82), 4, 12, WARNING);
            rect(x + 12, y + (e.boss() ? 97 : 77), 4, 3, WARNING);
        }
        if (e.state() == Enemy.State.NOTICE) {
            rect(x + 10, y + 88, 8, 2, GameAssets.GOLD);
            rect(x + 16, y + 82, 2, 6, GameAssets.GOLD);
            rect(x + 12, y + 80, 6, 2, GameAssets.GOLD);
            rect(x + 12, y + 76, 2, 2, GameAssets.GOLD);
        }
        float angle = switch (e.state()) {
            case WINDUP, DASH_WINDUP, JUMP_WINDUP -> 90;
            case ATTACK, RECOVER, DASH -> e.facing() > 0 ? 0 : 180;
            default -> -90;
        };
        if (e.archer()) {
            batch.setColor(Color.WHITE);
            sprite(assets.bow,
                    e.facing() > 0 ? x + e.width() - 6 : x - 14,
                    y + 13, 20, 36, e.facing());
            if (warning) rect(x + (e.facing() > 0 ? 22 : -8), y + 30, 18, 2,
                    BRASS);
        } else {
            sword(e.facing() > 0 ? x + e.width() : x, y + 27, angle, scale, true);
        }
        if (e.boss()) {
            batch.setColor(Color.WHITE);
            sprite(assets.guardianShield,
                    x + (e.blocking() ? (e.facing() > 0 ? 29 : -20)
                            : (e.facing() > 0 ? -11 : 24)), y + 13, 32, 40, e.facing());
            if (e.blocking()) rect(x + 6, y + 96, 28, 3, GameAssets.GOLD);
            if (e.state() == Enemy.State.DASH)
                rect(x - e.facing() * 18, y + 10, 12, 38,
                        DASH_TRAIL);
        }

    }

    private void shield(Player p, double playerX, double playerY) {
        double x = playerX + (p.blocking() ? (p.facing() > 0 ? 22 : -16)
                : (p.facing() > 0 ? -7 : 20));
        double y = playerY + 12;
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
                Math.round(x) + (facing < 0 ? width : 0),
                Math.round(y),
                facing < 0 ? -width : width,
                height);
    }

    private void rect(double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, Math.round(x), Math.round(y), (float) width, (float) height);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
    }
}
