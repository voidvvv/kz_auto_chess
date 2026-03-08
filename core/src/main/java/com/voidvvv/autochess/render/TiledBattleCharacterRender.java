package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.voidvvv.autochess.manage.RenderDataManager;
import com.voidvvv.autochess.model.BattleCharacter;

/**
 * 使用Tiled纹理渲染角色
 */
public class TiledBattleCharacterRender {

    /**
     * 渲染角色（正常状态）
     */
    public static void render(SpriteBatch batch, BattleCharacter character, RenderDataManager renderDataManager) {
        if (character == null || renderDataManager == null || !renderDataManager.hasCharacterTexture(character)) {
            return;
        }

        TextureRegion texture = renderDataManager.getCharacterTexture(character);
        if (texture == null) return;

        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 根据阵营设置颜色
        if (character.isEnemy()) {
            batch.setColor(1f, 0.7f, 0.7f, 1f);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.draw(texture, x - size / 2, y - size / 2, size, size);

        // 恢复默认颜色
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * 渲染角色（带透明度）
     */
    public static void renderWithAlpha(SpriteBatch batch, BattleCharacter character, float alpha, RenderDataManager renderDataManager) {
        if (character == null || renderDataManager == null || !renderDataManager.hasCharacterTexture(character)) {
            return;
        }

        TextureRegion texture = renderDataManager.getCharacterTexture(character);
        if (texture == null) return;

        float x = character.getX();
        float y = character.getY();
        float size = character.getSize();

        // 根据阵营设置颜色
        if (character.isEnemy()) {
            batch.setColor(1f, 0.7f, 0.7f, alpha);
        } else {
            batch.setColor(1f, 1f, 1f, alpha);
        }

        batch.draw(texture, x - size / 2, y - size / 2, size, size);

        // 恢复默认颜色
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
