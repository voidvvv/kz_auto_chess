package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.utils.FontUtils;

/**
 * 技能名称显示组件
 * 在效果位置上方显示技能名称，带有淡出效果
 */
public final class SkillNameDisplay {

    /**
     * 默认字体垂直偏移（相对于效果位置）
     */
    private static final float DEFAULT_Y_OFFSET = 50f;

    /**
     * 名称飘动速度（向上飘动）
     */
    private static final float FLOAT_SPEED = 30f;

    /**
     * 复用的 GlyphLayout 实例（避免 GC 压力）
     */
    private static final GlyphLayout GLYPH_LAYOUT = new GlyphLayout();

    private SkillNameDisplay() {
        // 工具类，禁止实例化
    }

    /**
     * 渲染技能名称
     *
     * @param holder 渲染上下文
     * @param model  效果模型
     * @param x      效果中心 X 坐标
     * @param y      效果中心 Y 坐标（名称将显示在此上方）
     */
    public static void render(RenderHolder holder, SkillEffectModel model, float x, float y) {
        render(holder, model, x, y, DEFAULT_Y_OFFSET);
    }

    /**
     * 渲染技能名称（可配置偏移）
     *
     * @param holder  渲染上下文
     * @param model   效果模型
     * @param x       效果中心 X 坐标
     * @param y       效果中心 Y 坐标
     * @param yOffset Y 偏移量
     */
    public static void render(RenderHolder holder, SkillEffectModel model,
                              float x, float y, float yOffset) {
        String skillName = model.getSkillName();
        if (skillName == null || skillName.isEmpty()) {
            return;
        }

        float progress = model.getProgress();
        BitmapFont font = FontUtils.getDefaultFont();

        // 计算淡出和飘动效果
        float alpha = MathUtils.clamp(1f - progress, 0f, 1f);
        float floatY = y + yOffset + (progress * FLOAT_SPEED);

        // 设置字体颜色（使用效果类型的默认颜色）
        Color baseColor = model.getType().getDefaultColor();
        Color textColor = new Color(baseColor.r, baseColor.g, baseColor.b, alpha);
        font.setColor(textColor);

        // 计算文本宽度以居中
        GLYPH_LAYOUT.setText(font, skillName);
        float textWidth = GLYPH_LAYOUT.width;
        float textX = x - textWidth / 2f;

        // 渲染文本
        SpriteBatch batch = holder.getSpriteBatch();
        batch.begin();
        font.draw(batch, skillName, textX, floatY);
        batch.end();

        // 恢复默认颜色
        font.setColor(Color.WHITE);
    }

    /**
     * 渲染技能名称（自定义颜色）
     *
     * @param holder  渲染上下文
     * @param model   效果模型
     * @param x       效果中心 X 坐标
     * @param y       效果中心 Y 坐标
     * @param yOffset Y 偏移量
     * @param color   自定义颜色
     */
    public static void render(RenderHolder holder, SkillEffectModel model,
                              float x, float y, float yOffset, Color color) {
        String skillName = model.getSkillName();
        if (skillName == null || skillName.isEmpty()) {
            return;
        }

        float progress = model.getProgress();
        BitmapFont font = FontUtils.getDefaultFont();

        // 计算淡出效果
        float alpha = MathUtils.clamp(1f - progress, 0f, 1f);
        float floatY = y + yOffset + (progress * FLOAT_SPEED);

        // 设置字体颜色
        Color textColor = new Color(color.r, color.g, color.b, alpha);
        font.setColor(textColor);

        // 计算文本宽度以居中
        GLYPH_LAYOUT.setText(font, skillName);
        float textWidth = GLYPH_LAYOUT.width;
        float textX = x - textWidth / 2f;

        // 渲染文本
        SpriteBatch batch = holder.getSpriteBatch();
        batch.begin();
        font.draw(batch, skillName, textX, floatY);
        batch.end();

        // 恢复默认颜色
        font.setColor(Color.WHITE);
    }
}
