package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.voidvvv.autochess.game.AutoChessGameMode;
import com.voidvvv.autochess.manage.CharacterEffectManager;
import com.voidvvv.autochess.manage.RenderDataManager;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.utils.TiledAssetLoader;

/**
 * 使用Tiled纹理渲染角色
 */
public class TiledBattleCharacterRender {

    /**
     * 渲染角色（正常状态）
     */
    public static void render(SpriteBatch batch, BattleCharacter character) {
        render(batch, character, null, 1.0f);
    }

    /**
     * 渲染角色（带透明度）
     */
    public static void renderWithAlpha(SpriteBatch batch, BattleCharacter character, float alpha) {
        render(batch, character, null, alpha);
    }

    /**
     * 渲染角色（带视觉效果）
     * @param batch SpriteBatch
     * @param character 战斗角色
     * @param characterEffectManager 角色视觉效果管理器
     */
    public static void render(SpriteBatch batch, BattleCharacter character, CharacterEffectManager characterEffectManager) {
        render(batch, character, characterEffectManager, 1.0f);
    }

    /**
     * 渲染角色（带透明度和视觉效果）
     * @param batch SpriteBatch
     * @param character 战斗角色
     * @param characterEffectManager 角色视觉效果管理器
     * @param alpha 透明度
     */
    public static void renderWithAlpha(SpriteBatch batch, BattleCharacter character, float alpha, CharacterEffectManager characterEffectManager) {
        render(batch, character, characterEffectManager, alpha);
    }

    /**
     * 内部渲染方法（统一实现）
     */
    private static void render(SpriteBatch batch, BattleCharacter character, CharacterEffectManager characterEffectManager, float alpha) {
        if (character == null) {
            return;
        }

        TextureRegion texture = TiledAssetLoader.getTexture(character.getCard().getTiledResourceKey());
        if (texture == null) return;

        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 获取角色视觉效果
        CharacterEffectManager.CharacterRenderEffects effects = null;
        if (characterEffectManager != null) {
            effects = characterEffectManager.getRenderEffects(character.getId());
        }

        // 应用视觉偏移
        if (effects != null) {
            x += effects.xOffset;
            y += effects.yOffset;
        }

        // 应用颜色和透明度
        float finalAlpha = alpha;
        if (effects != null) {
            finalAlpha *= effects.alpha;
        }

        if (effects != null && effects.tintColor != null) {
            batch.setColor(effects.tintColor.r, effects.tintColor.g, effects.tintColor.b, finalAlpha);
        } else {
            // 根据阵营设置颜色
            if (character.isEnemy()) {
                batch.setColor(1f, 0.7f, 0.7f, finalAlpha);
            } else {
                batch.setColor(1f, 1f, 1f, finalAlpha);
            }
        }

        batch.draw(texture, x - size / 2, y - size / 2, size, size);

        // 恢复默认颜色
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
