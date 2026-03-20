package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 减益效果渲染器
 * 显示向下箭头和暗色轮廓效果
 */
public class DebuffEffectRenderer implements SkillEffectRenderer {

    private static final Color DEBUFF_COLOR = new Color(0.6f, 0f, 0.8f, 0.7f);
    private static final Color OUTLINE_COLOR = new Color(0.3f, 0f, 0.4f, 0.6f);
    private static final float ARROW_SIZE = 15f;
    private static final float ARROW_FALL_SPEED = 30f;

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

        // 渲染向下箭头
        renderDebuffArrows(holder, x, y, progress);

        // 渲染目标暗色轮廓
        if (target != null) {
            renderTargetOutline(holder, target, progress);
        }

        // 渲染技能名称
        SkillNameDisplay.render(holder, model, x, y, 70f, DEBUFF_COLOR);
    }

    /**
     * 渲染向下箭头效果（表示减益）
     */
    private void renderDebuffArrows(RenderHolder holder, float x, float y, float progress) {
        float alpha = 1f - progress;
        float fallOffset = progress * ARROW_FALL_SPEED;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Filled);

        sr.setColor(new Color(DEBUFF_COLOR.r, DEBUFF_COLOR.g, DEBUFF_COLOR.b, alpha));

        // 渲染三个箭头（左、中、右）
        for (int i = -1; i <= 1; i++) {
            float arrowX = x + i * 20f;
            float arrowY = y + 40f - fallOffset; // 从上方下落

            // 绘制向下箭头
            drawArrow(sr, arrowX, arrowY, ARROW_SIZE, false);
        }

        sr.end();
    }

    /**
     * 渲染目标暗色轮廓
     */
    private void renderTargetOutline(RenderHolder holder, BattleCharacter target, float progress) {
        float alpha = 0.6f * (1f - progress * 0.5f);
        float x = target.getX();
        float y = target.getY();
        float size = target.getSize();

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // 暗色光晕效果
        sr.setColor(new Color(OUTLINE_COLOR.r, OUTLINE_COLOR.g, OUTLINE_COLOR.b, alpha * 0.3f));
        sr.circle(x, y, size + 10f);

        sr.end();

        // 轮廓线
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(DEBUFF_COLOR.r, DEBUFF_COLOR.g, DEBUFF_COLOR.b, alpha));
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
        return SkillEffectType.DEBUFF;
    }
}
