package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameWorld;
import br.com.thecruzader.level.Bounds;
import com.badlogic.gdx.graphics.Color;
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
        batch.setColor(Color.WHITE);
        batch.draw(background, (float) left(world), (float) bottom(world));
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
            drawDoor(batch, world.arenaEntrance(), entranceClosure, true);
            drawDoor(batch, world.level().gate(), exitClosure, true);
        } else drawDoor(batch, world.level().gate(), exitClosure, false);
    }

    private void drawDoor(Batch batch, Bounds gate, float closure, boolean interior) {
        batch.setColor(Color.WHITE);
        if (interior) batch.draw(frame, (float) Math.round(gate.x() + gate.width() / 2 - 48),
                (float) gate.y(), 96, (float) gate.height() + 32);
        if (closure == 0) return;
        float panel = Math.round(gate.width() * closure / 2);
        if (panel == 0) return;
        batch.draw(door, (float) gate.x(), (float) gate.y(), panel, (float) gate.height(), 0, 0, 32, 256, false, false);
        batch.draw(door, (float) gate.right() - panel, (float) gate.y(), panel, (float) gate.height(), 32, 0, 32, 256, false, false);
    }

    void drawForeground(Batch batch, GameWorld world) {
        batch.setColor(Color.WHITE);
        batch.draw(foreground, (float) left(world), (float) bottom(world));
    }
    private void rect(Batch batch, double x, double y, double width, double height, Color color) {
        batch.setColor(color);
        batch.draw(assets.white, Math.round(x), Math.round(y), (float) width, (float) height);
    }
}
