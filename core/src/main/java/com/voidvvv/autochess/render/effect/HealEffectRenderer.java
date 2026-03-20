package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 治疗效果渲染器
 * 显示绿色扩散光环和飘字效果
 */
public class HealEffectRenderer implements SkillEffectRenderer {

    private static final Color HEAL_COLOR = new Color(0.2f, 1f, 0.2f, 0.8f);
    private static final float RING_EXPAND_SPEED = 50f;
    private static final float RING_MAX_RADIUS = 60f;

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        float x = model.getWorldX();
        float y = model.getWorldY();

        // 渲染扩散光环
        renderExpandingRing(holder, x, y, progress);

        // 渲染技能名称
        SkillNameDisplay.render(holder, model, x, y, 70f, HEAL_COLOR);
    }

    /**
     * 渲染扩散光环效果
     */
    private void renderExpandingRing(RenderHolder holder, float x, float y, float progress) {
        float radius = RING_EXPAND_SPEED * progress;
        radius = Math.min(radius, RING_MAX_RADIUS);
        float alpha = 1f - progress;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);

        // 外圈
        sr.setColor(new Color(HEAL_COLOR.r, HEAL_COLOR.g, HEAL_COLOR.b, alpha));
        sr.circle(x, y, radius);

        // 内圈
        float innerRadius = radius * 0.6f;
        sr.circle(x, y, innerRadius);

        sr.end();
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.HEAL;
    }
}
