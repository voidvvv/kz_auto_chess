package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * AOE（范围伤害）效果渲染器
 * 显示范围指示器和爆炸效果
 */
public class AoeEffectRenderer implements SkillEffectRenderer {

    private static final Color AOE_COLOR = new Color(1f, 0.5f, 0f, 0.6f);
    private static final Color EXPLOSION_COLOR = new Color(1f, 0.8f, 0.2f, 0.8f);

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        float x = model.getWorldX();
        float y = model.getWorldY();
        float range = model.getRange();

        // 渲染范围指示器
        renderPulsingRange(holder, x, y, range, progress);

        // 渲染爆炸效果
        renderExplosion(holder, x, y, range, progress);

        // 渲染技能名称
        SkillNameDisplay.render(holder, model, x, y, range + 20f, AOE_COLOR);
    }

    /**
     * 渲染脉冲范围指示器
     */
    private void renderPulsingRange(RenderHolder holder, float x, float y,
                                    float radius, float progress) {
        // 脉冲效果：半径在 80%-100% 之间波动
        float pulseRadius = radius * (0.8f + 0.2f * (float) Math.sin(progress * Math.PI * 4));
        float alpha = 0.8f * (1f - progress * 0.5f);

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);

        // 外圈（脉冲）
        sr.setColor(new Color(AOE_COLOR.r, AOE_COLOR.g, AOE_COLOR.b, alpha));
        sr.circle(x, y, pulseRadius);

        // 内圈
        sr.circle(x, y, radius * 0.5f);

        // 十字准星
        float crossSize = radius * 0.2f;
        sr.line(x - crossSize, y, x + crossSize, y);
        sr.line(x, y - crossSize, x, y + crossSize);

        sr.end();
    }

    /**
     * 渲染爆炸效果（从中心扩散）
     */
    private void renderExplosion(RenderHolder holder, float x, float y,
                                 float maxRadius, float progress) {
        // 爆炸从中心向外扩散
        float explosionRadius = maxRadius * progress;
        float alpha = (1f - progress) * 0.6f;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // 填充圆（半透明）
        sr.setColor(new Color(EXPLOSION_COLOR.r, EXPLOSION_COLOR.g,
                             EXPLOSION_COLOR.b, alpha));
        sr.circle(x, y, explosionRadius);

        sr.end();
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.AOE;
    }
}
