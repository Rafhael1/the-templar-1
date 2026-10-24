package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.BossRelease;
import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.level.Bounds;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** An authored enclosed room, rendered separately from the outdoor road and tilemap. */
final class EnclosedAreaRenderer {
    private final LevelResources resources;
    private final GameAssets assets;
    private final Texture background, foreground, door, frame;
    private final TextureRegion exteriorLeft, exteriorMiddle, exteriorRight;
    private final Color light = new Color();

    EnclosedAreaRenderer(GameAssets assets, LevelResources resources) {
        this.assets = assets;
        this.resources = resources;
        var definition = resources.definition().interior();
        Texture exterior = resources.texture(definition.exterior());
        background = resources.texture(definition.background());
        foreground = resources.texture(definition.foreground());
        door = resources.texture(definition.door());
        frame = resources.texture(definition.doorFrame());
        requireSize(exterior, 960, 448);
        requireSize(background, 960, 540);
        requireSize(foreground, 960, 540);
        requireSize(door, 64, 256);
        requireSize(frame, 96, 288);
        exteriorLeft = new TextureRegion(exterior, 0, 0, 256, 448);
        exteriorMiddle = new TextureRegion(exterior, 256, 0, 480, 448);
        exteriorRight = new TextureRegion(exterior, 736, 0, 224, 448);
    }

    private static void requireSize(Texture texture, int width, int height) {
        if (texture.getWidth() != width || texture.getHeight() != height)
            throw new IllegalArgumentException("Room texture must be " + width + " x " + height);
    }

    double left(GameWorld world) { return world.level().arena().x() + world.level().arena().width() / 2 - 480; }
    double bottom(GameWorld world) { return world.level().arena().y() - 64; }

    void drawExterior(Batch batch, GameWorld world) {
        double entranceCenter = world.arenaEntrance().x() + world.arenaEntrance().width() / 2;
        double exitCenter = world.level().gate().x() + world.level().gate().width() / 2;
        float y = (float) world.level().arena().y();
        float x = (float) Math.round(entranceCenter - 112);
        float end = (float) Math.round(exitCenter - 112);
        batch.setColor(Color.WHITE);
        batch.draw(exteriorLeft, x, y);
        // Crop whole pixels rather than stretch the chapel when an arena has a different width.
        for (float section = x + 256; section < end; section += 480) {
            int width = Math.min(480, Math.round(end - section));
            batch.draw(exteriorMiddle.getTexture(), section, y, width, 448,
                    256, 0, width, 448, false, false);
        }
        batch.draw(exteriorRight, end, y);
    }

    void drawInterior(Batch batch, GameWorld world) {
        float radiance = radiance(world);
        tintRoom(batch, radiance);
        batch.draw(background, (float) left(world), (float) bottom(world));
        if (radiance > 0) drawRadiance(batch, world, radiance);
        for (var source : resources.definition().interior().lights()) {
            double x = left(world) + source.x(), y = bottom(world) + source.y();
            float pulse = (float) (.08 + Math.sin(world.time() * 7 + source.x()) * .015);
            rect(batch, x - 18, y - 15, 36, 44, light.set(1, .65f, .30f, pulse));
            rect(batch, x - 3, y, 6, 10 + Math.sin(world.time() * 13 + source.x()) * 2,
                    light.set(.94f, .60f, .29f, 1));
            rect(batch, x - 1, y + 1, 2, 6, light.set(1, .89f, .63f, 1));
        }
        batch.setColor(Color.WHITE);
    }

    void drawDoors(Batch batch, GameWorld world, boolean interior, float entranceClosure, float exitClosure) {
        if (interior) {
            tintRoom(batch, radiance(world));
            drawDoor(batch, world.arenaEntrance(), entranceClosure, true);
            drawDoor(batch, world.level().gate(), exitClosure, true);
        } else drawDoor(batch, world.level().gate(), exitClosure, false);
    }

    private void drawDoor(Batch batch, Bounds gate, float closure, boolean interior) {
        if (!interior) batch.setColor(Color.WHITE);
        if (interior) batch.draw(frame, (float) Math.round(gate.x() + gate.width() / 2 - 48),
                (float) gate.y(), 96, (float) gate.height() + 32);
        if (closure == 0) return;
        float panel = Math.round(gate.width() * closure / 2);
        if (panel == 0) return;
        batch.draw(door, (float) gate.x(), (float) gate.y(), panel, (float) gate.height(), 0, 0, 32, 256, false, false);
        batch.draw(door, (float) gate.right() - panel, (float) gate.y(), panel, (float) gate.height(), 32, 0, 32, 256, false, false);
    }

    void drawForeground(Batch batch, GameWorld world) {
        tintRoom(batch, radiance(world));
        batch.draw(foreground, (float) left(world), (float) bottom(world));
        batch.setColor(Color.WHITE);
    }

    private static float radiance(GameWorld world) {
        if (!world.bossDefeated()) return 0;
        float progress = (float) Math.min(1, world.bossReleaseElapsed() / BossRelease.OPEN_END);
        return progress * progress * (3 - 2 * progress);
    }

    private static void tintRoom(Batch batch, float radiance) {
        batch.setColor(.46f + .54f * radiance, .44f + .56f * radiance,
            .62f + .38f * radiance, 1);
    }

    private void drawRadiance(Batch batch, GameWorld world, float radiance) {
        int source = batch.getBlendSrcFunc(), destination = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        // Warm daylight fills the room; stepped halos preserve the authored pixel-art style.
        rect(batch, left(world), bottom(world), 960, 540, light.set(1, .78f, .40f, .12f * radiance));
        double x = left(world) + 480, y = bottom(world) + 166;
        float glow = radiance * (float) (.88 + Math.sin(world.time() * 2) * .12);
        for (int spread = 24; spread >= 4; spread -= 4) {
            Color halo = light.set(1, .79f, .36f, .035f * glow);
            rect(batch, x - 4 - spread, y - spread, 10 + spread * 2, 98 + spread * 2, halo);
            rect(batch, x - 32 - spread, y + 58 - spread, 66 + spread * 2, 10 + spread * 2, halo);
        }
        rect(batch, x - 1, y + 2, 4, 94, light.set(1, .93f, .66f, .8f * glow));
        rect(batch, x - 30, y + 64, 62, 4, light.set(1, .93f, .66f, .8f * glow));
        batch.setBlendFunction(source, destination);
    }
    private void rect(Batch batch, double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, Math.round(x), Math.round(y), (float) width, (float) height);
    }
}
