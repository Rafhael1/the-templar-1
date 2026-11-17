package br.com.thecruzader.presentation;

import br.com.thecruzader.level.LevelData;
import br.com.thecruzader.level.LevelDefinition;
import br.com.thecruzader.level.LevelLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Disposable;

/** A screen owns this bundle. Map geometry and graphics always use the same definition/path. */
public final class LevelResources implements Disposable {
    static final String CAPTAIN_ATLAS = "levels/constantinople/boss/captain.png";
    static final String DUELIST_ATLAS = "levels/nicaea/boss/duelist.png";
    private final AssetManager manager = new AssetManager();
    private final LevelDefinition definition;
    private final LevelData data;

    public LevelResources(LevelDefinition definition, GameAssets assets) {
        this.definition = definition;
        try {
            data = LevelLoader.load(Gdx.files.internal(definition.mapPath()));
            definition.validateMap(data);
            for (String key : definition.texts().keys()) assets.texts.text(key);
            manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
            var parameters = new TmxMapLoader.Parameters();
            parameters.projectFilePath = "maps/the-cruzader.tiled-project";
            manager.load(definition.mapPath(), TiledMap.class, parameters);
            for (var region : definition.regions()) {
                assets.texts.text(region.announcementKey());
                if (!manager.contains(region.texturePath())) manager.load(region.texturePath(), Texture.class);
            }
            for (var conversation : definition.conversations()) {
                conversation.textKeys().forEach(assets.texts::text);
                if (!manager.contains(conversation.spritePath()))
                    manager.load(conversation.spritePath(), Texture.class);
            }
            for (String path : definition.interior().textures())
                if (!manager.contains(path)) manager.load(path, Texture.class);
            if (data.enemies().stream().anyMatch(spawn -> spawn.kind() == LevelData.EnemyKind.HARBOR_CAPTAIN))
                manager.load(CAPTAIN_ATLAS, Texture.class);
            if (data.enemies().stream().anyMatch(spawn -> spawn.kind() == LevelData.EnemyKind.RELIEF_DUELIST))
                manager.load(DUELIST_ATLAS, Texture.class);
            manager.finishLoading();
        } catch (RuntimeException error) {
            manager.dispose();
            throw new IllegalArgumentException("Cannot load level " + definition.id() + " (" + definition.mapPath() + ")", error);
        }
    }
    public LevelDefinition definition() { return definition; }
    public LevelData data() { return data; }
    public TiledMap map() { return manager.get(definition.mapPath(), TiledMap.class); }
    public Texture texture(String path) { return manager.get(path, Texture.class); }
    public Texture scenery(int region) { return manager.get(definition.regions().get(region).texturePath(), Texture.class); }
    @Override public void dispose() { manager.dispose(); }
}
