package br.com.thecruzader.presentation;

import br.com.thecruzader.gameplay.GameWorld;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Walk-through rooms authored in the TMX; facades dissolve as the player enters.
 */
final class PassageRenderer {
    private static final class Passage {
        final Rectangle bounds;
        final boolean cave;
        float facade = 1;

        Passage(Rectangle bounds, boolean cave) {
            this.bounds = bounds;
            this.cave = cave;
        }
    }

    private final List<Rectangle> ceilings = new ArrayList<>();
    private final List<Rectangle> mountains = new ArrayList<>();
    private final List<Passage> passages = new ArrayList<>();
    private final GameAssets assets;
    private final Color tint = new Color();

    PassageRenderer(GameAssets assets, LevelResources level) {
        this.assets = assets;
        var layer = level.map().getLayers().get("passages");
        if (layer == null) return;
        for (var object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject rectangle))
                throw new IllegalArgumentException("Passages must be rectangles");
            String type = object.getProperties().get("type", String.class);
            if ("mountain".equals(type)) {
                mountains.add(rectangle.getRectangle());
                continue;
            }
            if (!"cave".equals(type) && !"gallery".equals(type))
                throw new IllegalArgumentException("Unknown passage type: " + type);
            Rectangle bounds = rectangle.getRectangle();
            passages.add(new Passage(bounds, "cave".equals(type)));
            var roof = level.data().solids().stream()
                .filter(solid -> solid.x() == bounds.x && solid.width() == bounds.width
                    && solid.y() == bounds.y + bounds.height)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Passage requires a matching solid roof"));
            ceilings.add(new Rectangle((float) roof.x(), (float) roof.y(),
                (float) roof.width(), (float) roof.height()));
        }
    }

    void background(Batch batch, GameWorld world, float delta, float left) {
        for (Rectangle ridge : mountains) {
            if (ridge.x + ridge.width < left || ridge.x > left + 960) continue;
            for (int peak = 0; peak < 3; peak++) {
                float center = ridge.x + 384 + peak * 560;
                int height = (int) ridge.height - peak % 2 * 80;
                for (int y = 0; y < height; y += 16) {
                    float half = (height - y) * .9f;
                    rect(batch, center - half, ridge.y + y, half * 2, 16, 0x3b3945, 1);
                    rect(batch, center, ridge.y + y, half, 16, 0x504955, 1);
                    if (y > height - 64)
                        rect(batch, center - half, ridge.y + y, half, 8, 0x91828a, 1);
                    else if (y % 48 == 0) {
                        rect(batch, center - half + 12, ridge.y + y, half - 12, 3, 0x56505a, 1);
                        rect(batch, center + 8, ridge.y + y + 10, half - 16, 3, 0x625963, 1);
                    }
                }
            }
        }
        for (Passage passage : passages) {
            Rectangle r = passage.bounds;
            boolean inside = r.contains((float) world.player().x() + 14, (float) world.player().y() + 26);
            float target = inside ? 0 : 1;
            if (!world.paused()) passage.facade += Math.max(-delta * 4, Math.min(delta * 4, target - passage.facade));
            if (r.x + r.width < left || r.x > left + 960) continue;
            rect(batch, r.x, r.y, r.width, r.height, 0x171923, 1);
            for (int col = 0; col < r.width; col += 32) {
                for (int row = 0; row < r.height; row += 24) {
                    int shade = passage.cave ? 0x252831 : 0x35323d;
                    rect(batch, r.x + col + 2, r.y + row + 2, 28, 20, shade, 1);
                }
            }
            for (int offset = 48; offset < r.width - 32; offset += 128) {
                float x = r.x + offset;
                if (passage.cave) {
                    // Mineral seams on the rear wall.
                    rect(batch, x + 18, r.y + 8, 8, 20, 0x55575b, 1);
                } else {
                    rect(batch, x, r.y, 16, r.height - 32, 0x4e4850, 1);
                    rect(batch, x - 6, r.y, 28, 12, 0x70616a, 1);
                    rect(batch, x - 8, r.y + r.height - 48, 32, 16, 0x70616a, 1);
                    rect(batch, x + 40, r.y + 64, 36, 104, 0x11151f, 1);
                    rect(batch, x + 48, r.y + 168, 20, 12, 0x11151f, 1);
                }
                float flameY = r.y + 100;
                rect(batch, x + 56, flameY - 12, 4, 16, 0x746052, 1);
                rect(batch, x + 48, flameY - 8, 20, 24, 0xc17c44, .12f);
                rect(batch, x + 55, flameY, 6, 8 + (float) Math.sin(world.time() * 9 + offset) * 2, 0xf4c17b, 1);
            }
        }
        batch.setColor(Color.WHITE);
    }

    void foreground(Batch batch, float left) {
        for (Passage passage : passages) {
            Rectangle r = passage.bounds;
            if (r.x + r.width < left || r.x > left + 960) continue;
            // The openings stay visible from outside and never create invisible walls.
            float opacity = passage.facade;
            for (int offset = 64; offset < r.width - 64; offset += 32) {
                float height = passage.cave ? r.height - 24 - (offset / 32 % 3) * 8 : r.height - 24;
                rect(batch, r.x + offset, r.y, 32, height,
                    passage.cave ? 0x4a4549 : 0x655761, opacity);
                for (int row = 16; row < height; row += 32)
                    rect(batch, r.x + offset + 2, r.y + row, 28, 2, 0x302d38, opacity);
            }
            if (!passage.cave) {
                // Blind pointed arcades give the closed facade depth without changing collision.
                for (int offset = 80; offset < r.width - 96; offset += 128) {
                    float x = r.x + offset;
                    rect(batch, x, r.y + 20, 64, r.height - 72, 0x453e4b, opacity);
                    for (int step = 0; step < 4; step++)
                        rect(batch, x + step * 8, r.y + r.height - 52 + step * 8,
                            64 - step * 16, 8, 0x453e4b, opacity);
                    rect(batch, x + 4, r.y + 22, 4, r.height - 80, 0x88747b, opacity);
                    rect(batch, x - 16, r.y, 12, r.height - 32, 0x807078, opacity);
                    rect(batch, x - 20, r.y, 20, 12, 0xa18a79, opacity);
                    rect(batch, x - 20, r.y + r.height - 44, 20, 12, 0xa18a79, opacity);
                    rect(batch, x + 25, r.y + 72, 14, 3, 0xa18a79, opacity);
                    rect(batch, x + 30, r.y + 60, 4, 28, 0xa18a79, opacity);
                }
                rect(batch, r.x + 20, r.y + 12, r.width - 40, 6, 0x857078, opacity);
                rect(batch, r.x + 20, r.y + r.height - 20, r.width - 40, 6, 0x9b8377, opacity);
                rect(batch, r.x + 8, r.y, 12, r.height - 16, 0x70616a, 1);
                rect(batch, r.x + r.width - 20, r.y, 12, r.height - 16, 0x70616a, 1);
            }
        }
        // Use the physical roof bounds so trim cannot hang into the jump space.
        for (Rectangle ceiling : ceilings) {
            if (ceiling.x + ceiling.width < left || ceiling.x > left + 960) continue;
            rect(batch, ceiling.x, ceiling.y, ceiling.width, 16, 0x71646a, 1);
        }
        batch.setColor(Color.WHITE);
    }

    private void rect(Batch batch, float x, float y, float w, float h, int rgb, float alpha) {
        tint.set((rgb << 8) | 0xff);
        tint.a = alpha;
        batch.setColor(tint);
        batch.draw(assets.white, x, y, w, h);
    }
}
