package br.com.thecruzader.presentation;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelDefinition.Decoration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored landmarks and deterministic wear on the physical terrain, behind the actors.
 */
final class EnvironmentRenderer {
    private final GameAssets assets;
    private final LevelResources level;
    private final TextureRegion[] props;

    private record Detail(float x, float y, int width, int height, float color) {
    }

    private record SurfaceDetails(Bounds bounds, List<Detail> details) {
    }

    private final List<SurfaceDetails> surfaces = new ArrayList<>();

    EnvironmentRenderer(GameAssets assets, LevelResources level) {
        this.assets = assets;
        this.level = level;
        props = TextureRegion.split(assets.texture("environment"), 192, 256)[0];
        buildSurfaces();
    }

    void background(Batch batch, float left) {
        batch.setColor(Color.WHITE);
        for (Decoration decoration : level.definition().decorations()) {
            if (decoration.x() + 192 < left || decoration.x() > left + 960) continue;
            int frame = switch (decoration.kind()) {
                case ARCH -> 0;
                case SHRINE -> 1;
                case GRAVES -> 2;
                case DEAD_TREE -> 3;
                case WAGON -> 4;
                case PENNANT -> 5;
                default -> -1;
            };
            if (frame >= 0) {
                // Votive shrines are compact so the raised-route landmarks fit below the skyline.
                boolean shrine = decoration.kind() == br.com.thecruzader.level.LevelDefinition.DecorationKind.SHRINE;
                batch.draw(props[frame], (float) decoration.x(), (float) decoration.y(),
                    shrine ? 96 : 192, shrine ? 128 : 256);
            }
        }
    }

    private void buildSurfaces() {
        for (Bounds solid : level.data().solids()) {
            List<Detail> details = new ArrayList<>();
            // Only dress exposed top edges: buried stair treads must stay inside the rock.
            int start = (int) solid.x() + 8;
            for (int x = start; x < solid.right() - 8; x += 32) {
                if (!exposed(x, solid.top())) continue;
                int seed = Math.floorMod(x * 31 + (int) solid.top() * 17, 97);
                double y = solid.top();
                detail(details, x, y - 3, 14 + seed % 12, 3, 0x555751);
                if (seed % 3 == 0) {
                    detail(details, x + 4, y, 2, 6, 0x727661);
                    detail(details, x + 8, y, 2, 9, 0x555e50);
                    detail(details, x + 10, y + 4, 4, 2, 0x727661);
                } else {
                    detail(details, x + 6, y, 7, 3, 0x887a70);
                    detail(details, x + 7, y + 3, 4, 2, 0xa08c75);
                }
                if (solid.y() > 64 && seed % 4 == 0) {
                    int length = 12 + seed % 20;
                    detail(details, x + 14, y - length, 2, length, 0x454f48);
                    for (int leaf = 6; leaf < length; leaf += 6)
                        detail(details, x + 11 + leaf % 4, y - leaf, 6, 3, 0x646b55);
                }
            }
            // Dress suspended masonry with inset stones; the physical silhouette stays exact.
            if (solid.height() == 32 && solid.top() > 96) {
                for (double x = solid.x() + 10; x < solid.right() - 8; x += 48) {
                    detail(details, x, solid.y() + 5, 4, 12, 0x514a53);
                    detail(details, x + 4, solid.y() + 5, 8, 3, 0x9b8874);
                }
            }
            surfaces.add(new SurfaceDetails(solid, List.copyOf(details)));
        }
    }

    void surfaces(Batch batch, float left) {
        for (SurfaceDetails surface : surfaces) {
            if (surface.bounds().right() < left || surface.bounds().x() > left + 960) continue;
            for (Detail detail : surface.details()) {
                if (detail.x() + detail.width() < left || detail.x() > left + 960) continue;
                batch.setPackedColor(detail.color());
                batch.draw(assets.white, detail.x(), detail.y(), detail.width(), detail.height());
            }
        }
        batch.setColor(Color.WHITE);
    }

    private boolean exposed(double x, double top) {
        for (Bounds other : level.data().solids())
            if (other.x() <= x && other.right() > x && other.y() <= top && other.top() > top)
                return false;
        return true;
    }

    private static void detail(List<Detail> details, double x, double y, int width, int height, int rgb) {
        float color = Color.toFloatBits(rgb >>> 16 & 0xff, rgb >>> 8 & 0xff, rgb & 0xff, 255);
        details.add(new Detail((float) x, (float) y, width, height, color));
    }
}
