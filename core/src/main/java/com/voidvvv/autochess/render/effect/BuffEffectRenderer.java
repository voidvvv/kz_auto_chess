package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 增益效果渲染器
 * 显示向上箭头和轮廓高亮效果
 */
public class BuffEffectRenderer implements SkillEffectRenderer {

    private static final Color BUFF_COLOR = new Color(0f, 1f, 1f, 0.7f);
    private static final float ARROW_SIZE = 15f;
    private static final float ARROW_RISE_SPEED = 40f;

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        BattleCharacter target = model.getTarget();

        // 如果有目标，在目标位置渲染
        float x, y;
        if (target != null) {
            x = target.getX();
            y = target.getY();
        } else {
            x = model.getWorldX();
            y = model.getWorldY();
        }

        // 渲染向上箭头
        renderBuffArrows(holder, x, y, progress);

        // 渲染目标轮廓高亮
        if (target != null) {
            renderTargetHighlight(holder, target, progress);
        }

        // 渲染技能名称
        SkillNameDisplay.render(holder, model, x, y, 70f, BUFF_COLOR);
    }

    /**
     * 渲染向上箭头效果（表示增益）
     */
    private void renderBuffArrows(RenderHolder holder, float x, float y, float progress) {
        float alpha = 1f - progress;
        float riseOffset = progress * ARROW_RISE_SPEED;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(BUFF_COLOR.r, BUFF_COLOR.g, BUFF_COLOR.b, alpha));

        // 渲染三个箭头（左、中、右）
        for (int i = -1; i <= 1; i++) {
            float arrowX = x + i * 20f;
            float arrowY = y + riseOffset;

            // 绘制箭头形状
            drawArrow(sr, arrowX, arrowY, ARROW_SIZE, true);
        }

        sr.end();
    }

    /**
     * 渲染目标轮廓高亮
     */
    private void renderTargetHighlight(RenderHolder holder, BattleCharacter target, float progress) {
        float alpha = 0.5f * (1f - progress * 0.5f);
        float x = target.getX();
        float y = target.getY();
        float size = target.getSize();

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(new Color(BUFF_COLOR.r, BUFF_COLOR.g, BUFF_COLOR.b, alpha));
        sr.circle(x, y, size + 5f);

        sr.end();
    }

    /**
     * 绘制箭头形状
     * @param up 如果为 true，箭头向上；否则向下
     */
    private void drawArrow(ShapeRenderer sr, float x, float y, float size, boolean up) {
        float direction = up ? 1f : -1f;
        float halfWidth = size * 0.5f;
        float height = size;

        // 箭头由两个三角形组成
        // 1. 箭头尖端
        sr.triangle(
            x, y + height * direction,           // 尖端
            x - halfWidth, y,                    // 左角
            x + halfWidth, y                     // 右角
        );
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.BUFF;
    }
}
