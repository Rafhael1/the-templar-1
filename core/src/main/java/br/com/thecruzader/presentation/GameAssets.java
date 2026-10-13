package br.com.thecruzader.presentation;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/** Owns GPU/audio resources once for the lifetime of the application. */
public final class GameAssets implements Disposable {
    public static final String MAP = "maps/exiles-road.tmx";
    public static final Color PAPER = Color.valueOf("e5dbc0"), GOLD = Color.valueOf("c8a773");
    private final AssetManager manager = new AssetManager();
    public final Skin skin = new Skin();
    public final Texture white;
    public final TextureRegion[][] characters;
    public final TextureRegion[] swords;
    public final TextureRegion[] shields;

    public GameAssets() {
        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        TmxMapLoader.Parameters mapParameters = new TmxMapLoader.Parameters();
        mapParameters.projectFilePath = "maps/the-cruzader.tiled-project";
        manager.load(MAP, TiledMap.class, mapParameters);
        for (String file : new String[] {"characters", "swords", "shields", "sky", "ruins"})
            manager.load("sprites/" + file + ".png", Texture.class);
        for (String name : new String[] {"sword", "hit", "jump", "hurt", "chime"})
            manager.load("audio/" + name + ".wav", Sound.class);
        manager.load("audio/ambience.wav", Music.class);
        manager.finishLoading();
        characters = TextureRegion.split(texture("characters"), 64, 80);
        swords = TextureRegion.split(texture("swords"), 40, 16)[0];
        shields = TextureRegion.split(texture("shields"), 24, 36)[0];
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        try {
            white = new Texture(pixel);
        } finally {
            pixel.dispose();
        }
        skin.add("white", white);
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);
        skin.add("default", new Label.LabelStyle(font, PAPER));
        TextButton.TextButtonStyle button = new TextButton.TextButtonStyle();
        button.font = font;
        button.fontColor = PAPER;
        button.up = skin.newDrawable("white", Color.valueOf("703940"));
        button.over = skin.newDrawable("white", Color.valueOf("96545a"));
        button.down = skin.newDrawable("white", Color.valueOf("482b35"));
        skin.add("default", button);
    }

    public Texture texture(String name) {
        return manager.get("sprites/" + name + ".png", Texture.class);
    }

    public TiledMap map() {
        return manager.get(MAP, TiledMap.class);
    }

    public Sound sound(String name) {
        return manager.get("audio/" + name + ".wav", Sound.class);
    }

    public Music ambience() {
        return manager.get("audio/ambience.wav", Music.class);
    }

    public com.badlogic.gdx.scenes.scene2d.utils.Drawable panel(Color color) {
        return new TextureRegionDrawable(new TextureRegion(white)).tint(color);
    }

    @Override
    public void dispose() {
        skin.dispose();
        manager.dispose();
    }
}
