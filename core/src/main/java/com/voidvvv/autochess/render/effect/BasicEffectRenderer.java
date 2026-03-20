package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 基础技能效果渲染器
 * 默认回退渲染器，显示简单的白色圆圈效果
 */
public class BasicEffectRenderer implements SkillEffectRenderer {

    private static final float DEFAULT_DURATION = 0.5f;
    private static final float RING_MAX_RADIUS = 30f;

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        float x = model.getWorldX();
        float y = model.getWorldY();

        // 逐渐扩散的白色圆圈
        float radius = RING_MAX_RADIUS * progress;
        float alpha = 1f - progress;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(1f, 1f, 1f, alpha));
        sr.circle(x, y, radius);
        sr.end();

        // 渲染技能名称
        SkillNameDisplay.render(holder, model, x, y + 60f);
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.BASIC;
    }
}
