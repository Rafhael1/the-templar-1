package br.com.thecruzader.presentation;

import br.com.thecruzader.i18n.GameText;
import br.com.thecruzader.i18n.Language;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/** Owns GPU/audio resources once for the lifetime of the application. */
public final class GameAssets implements Disposable {
    public static final float UI_FONT_SCALE = 1f / 6;
    public static final Color PAPER = Color.valueOf("e5dbc0");
    public static final Color GOLD = Color.valueOf("c8a773");

    private final AssetManager manager = new AssetManager();

    public final Skin skin = new Skin();
    public final GameText texts;
    public final Texture white;
    public final TextureRegion[][] characters;
    public final TextureRegion[][] deaths;
    public final TextureRegion[] templarDeath;
    public final TextureRegion[] angels;
    public final TextureRegion bow;
    public final TextureRegion guardianShield;
    public final TextureRegion[] swords;
    public final TextureRegion[] shields;
    public final TextureRegionDrawable[] hearts;
    public final NinePatchDrawable frame;

    public GameAssets() {
        Language language = Language.valueOf(Gdx.app.getPreferences("o-templario").getString("language", "PT"));
        texts = new GameText(Gdx.files.internal("i18n"), language);

        loadAssets();
        characters = TextureRegion.split(texture("characters"), 64, 80);
        deaths = TextureRegion.split(texture("deaths"), 96, 80);
        templarDeath = TextureRegion.split(texture("templar-death"), 128, 112)[0];
        angels = TextureRegion.split(texture("angels"), 96, 112)[0];
        bow = new TextureRegion(texture("bow"));
        guardianShield = new TextureRegion(texture("guardian-shield"));
        swords = TextureRegion.split(texture("swords"), 40, 16)[0];
        shields = TextureRegion.split(texture("shields"), 24, 36)[0];
        TextureRegion[] heartRegions = TextureRegion.split(texture("hearts"), 16, 16)[0];
        hearts = new TextureRegionDrawable[] {
                new TextureRegionDrawable(heartRegions[0]),
                new TextureRegionDrawable(heartRegions[1])
        };
        frame = new NinePatchDrawable(new NinePatch(texture("ui-frame"), 4, 4, 4, 4));

        white = createWhiteTexture();
        configureSkin();
    }

    private void loadAssets() {
        for (String file : new String[] {
                "characters", "swords", "shields", "bandages", "sanctuary", "bow", "arrow",
                "guardian-shield", "hearts", "ui-frame", "sky", "deaths", "templar-death", "angels",
                "intro/chamber", "intro/pope", "intro/banner", "intro/kneeling",
                "intro/marchers", "intro/march-sky", "intro/march-walls"
        }) {
            manager.load("sprites/" + file + ".png", Texture.class);
        }
        for (String name : new String[] {"sword", "hit", "jump", "hurt", "chime"}) {
            manager.load("audio/" + name + ".wav", Sound.class);
        }
        manager.load("audio/ambience.wav", Music.class);
        manager.load("audio/templar-requiem.wav", Music.class);
        manager.finishLoading();
    }

    private Texture createWhiteTexture() {
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        try {
            return new Texture(pixel);
        } finally {
            pixel.dispose();
        }
    }

    private void configureSkin() {
        skin.add("white", white);
        BitmapFont font = createFont();
        skin.add("default-font", font);
        skin.add("default", new Label.LabelStyle(font, PAPER));

        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle();
        button.font = font;
        button.fontColor = PAPER;
        button.up = frame;
        button.over = frame.tint(Color.valueOf("ead6a4"));
        button.down = frame.tint(Color.valueOf("a4b1b8"));
        button.checked = button.over;
        button.overFontColor = GOLD;
        button.checkedFontColor = GOLD;
        skin.add("default", button);
    }

    private BitmapFont createFont() {
        Texture fontTexture = new Texture(Gdx.files.internal("ui/interface.png"), true);
        fontTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        BitmapFont font = new BitmapFont(new BitmapFont.BitmapFontData(
                Gdx.files.internal("ui/interface.fnt"), false),
                new TextureRegion(fontTexture), false);
        font.getData().setScale(UI_FONT_SCALE);
        font.setUseIntegerPositions(false);
        skin.add("font-texture", fontTexture);
        return font;
    }

    public Texture texture(String name) {
        return manager.get("sprites/" + name + ".png", Texture.class);
    }

    public Sound sound(String name) {
        return manager.get("audio/" + name + ".wav", Sound.class);
    }

    public Music ambience() {
        return manager.get("audio/ambience.wav", Music.class);
    }

    public Music requiem() { return manager.get("audio/templar-requiem.wav", Music.class); }

    public Drawable panel(Color color) {
        return new TextureRegionDrawable(new TextureRegion(white)).tint(color);
    }

    @Override
    public void dispose() {
        skin.dispose();
        manager.dispose();
    }
}
