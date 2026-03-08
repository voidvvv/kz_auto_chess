package com.voidvvv.autochess.manage;

import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.BaseCollision;
import com.voidvvv.autochess.utils.TiledAssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 角色渲染数据管理器
 * 管理角色纹理和碰撞数据的加载与检索，实现模型与渲染的分离
 */
public class CharacterRenderDataManager {

    private TiledAssetLoader tiledAssetLoader;

    public CharacterRenderDataManager(TiledAssetLoader tiledAssetLoader) {
        this.tiledAssetLoader = tiledAssetLoader;
    }

    /**
     * 为角色加载Tiled资源和碰撞数据
     * 注意：碰撞数据会更新到角色模型，因为这是游戏状态的一部分
     * 纹理数据由管理器管理，不存储在角色模型中
     *
     * @param character 要加载资源的角色
     */
    public void loadResourcesForCharacter(BattleCharacter character) {
        if (character == null || character.getCard() == null) {
            return;
        }

        String key = character.getCard().getTiledResourceKey();
        if (key == null || !tiledAssetLoader.hasResource(key)) {
            return;
        }

        BaseCollision collision = tiledAssetLoader.getCollision(key);
        if (collision != null) {
            character.baseCollision = collision;
        }
    }

    /**
     * 检查角色是否有Tiled纹理
     *
     * @param character 要检查的角色
     * @return 如果有对应的纹理则返回true
     */
    public boolean hasTextureForCharacter(BattleCharacter character) {
        if (character == null || character.getCard() == null) {
            return false;
        }

        String key = character.getCard().getTiledResourceKey();
        return key != null && tiledAssetLoader.hasResource(key);
    }

    /**
     * 获取角色的Tiled纹理
     *
     * @param character 要获取纹理的角色
     * @return 纹理区域，如果不存在则返回null
     */
    public TextureRegion getTextureForCharacter(BattleCharacter character) {
        if (character == null || character.getCard() == null) {
            return null;
        }

        String key = character.getCard().getTiledResourceKey();
        if (key == null) {
            return null;
        }

        return tiledAssetLoader.getTexture(key);
    }

    /**
     * 获取Tiled资源加载器
     *
     * @return Tiled资源加载器
     */
    public TiledAssetLoader getTiledAssetLoader() {
        return tiledAssetLoader;
    }
}
