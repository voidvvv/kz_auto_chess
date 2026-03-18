package com.voidvvv.autochess.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.PlayerLifeModel;

/**
 * 血条渲染器
 * 在UI界面顶部渲染玩家血条
 */
public class LifeBarRenderer {

    // 颜色配置
    private static final Color BACKGROUND_COLOR = new Color(0.1f, 0.1f, 0.1f, 0.8f);
    private static final Color HIGH_HEALTH_COLOR = new Color(0.2f, 0.8f, 0.2f, 1f);  // 绿色
    private static final Color MEDIUM_HEALTH_COLOR = new Color(0.9f, 0.8f, 0.1f, 1f); // 黄色
    private static final Color LOW_HEALTH_COLOR = new Color(0.9f, 0.2f, 0.2f, 1f);   // 红色
    private static final Color BORDER_COLOR = new Color(1f, 1f, 1f, 0.8f);
    private static final Color TEXT_COLOR = Color.WHITE;

    // 血量分段阈值（百分比）
    private static final float LOW_HEALTH_THRESHOLD = 0.25f;
    private static final float MEDIUM_HEALTH_THRESHOLD = 0.5f;

    /**
     * 渲染血条
     * @param lifeModel 玩家血量模型
     * @param x 血条左上角x坐标
     * @param y 血条左上角y坐标
     * @param width 血条宽度
     * @param height 血条高度
     * @param font 用于显示文字的字体
     */
    public static void render(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch,
                          PlayerLifeModel lifeModel, float x, float y,
                          float width, float height, BitmapFont font) {
        if (lifeModel == null) return;

        float healthPercentage = lifeModel.getHealthPercentage();

        // 渲染背景
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.rect(x, y - height, width, height);
        shapeRenderer.end();

        // 渲染血量条
        float healthWidth = width * healthPercentage;
        if (healthWidth > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(getHealthColor(healthPercentage));
            shapeRenderer.rect(x, y - height, healthWidth, height);
            shapeRenderer.end();
        }

        // 渲染边框
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);
        shapeRenderer.rect(x, y - height, width, height);
        shapeRenderer.end();

        // 渲染血量文字
        if (font != null) {
            spriteBatch.begin();
            font.setColor(TEXT_COLOR);
            font.getData().setScale(0.8f);

            String healthText = String.format("%d/%d", lifeModel.getCurrentHealth(), lifeModel.getMaxHealth());
            GlyphLayout layout = new GlyphLayout(font, healthText);
            float textX = x + (width - layout.width) / 2;
            float textY = y - height / 2 + layout.height / 2;
            font.draw(spriteBatch, layout, textX, textY);

            spriteBatch.end();
        }
    }

    /**
     * 根据血量百分比获取颜色
     */
    private static Color getHealthColor(float percentage) {
        if (percentage <= LOW_HEALTH_THRESHOLD) {
            return LOW_HEALTH_COLOR;
        } else if (percentage <= MEDIUM_HEALTH_THRESHOLD) {
            return MEDIUM_HEALTH_COLOR;
        } else {
            return HIGH_HEALTH_COLOR;
        }
    }

    /**
     * 渲染紧凑型血条（只显示血条，不显示背景和文字）
     * @param lifeModel 玩家血量模型
     * @param x 血条左上角x坐标
     * @param y 血条左上角y坐标
     * @param width 血条宽度
     * @param height 血条高度
     */
    public static void renderCompact(ShapeRenderer shapeRenderer, PlayerLifeModel lifeModel,
                                   float x, float y, float width, float height) {
        if (lifeModel == null) return;

        float healthPercentage = lifeModel.getHealthPercentage();
        float healthWidth = width * healthPercentage;

        if (healthWidth > 0) {
            shapeRenderer.setColor(getHealthColor(healthPercentage));
            shapeRenderer.rect(x, y - height, healthWidth, height);
        }

        // 渲染剩余部分背景
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.rect(x + healthWidth, y - height, width - healthWidth, height);
    }
}
