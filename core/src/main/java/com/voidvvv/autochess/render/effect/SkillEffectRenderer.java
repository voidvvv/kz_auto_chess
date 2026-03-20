package com.voidvvv.autochess.render.effect;

import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 技能效果渲染器接口
 * 采用策略模式，每种技能类型对应独立的渲染器实现
 */
public interface SkillEffectRenderer {

    /**
     * 渲染技能效果
     *
     * @param holder 渲染上下文（包含 SpriteBatch 和 ShapeRenderer）
     * @param model  效果数据模型
     */
    void render(RenderHolder holder, SkillEffectModel model);

    /**
     * 获取此渲染器支持的效果类型
     *
     * @return 支持的 SkillEffectType
     */
    SkillEffectType getSupportedType();

    /**
     * 更新效果状态（可选）
     * 默认实现：增加已过时间
     *
     * @param model 效果模型
     * @param delta 时间增量（秒）
     * @return 更新后的模型（不可变设计）
     */
    default SkillEffectModel update(SkillEffectModel model, float delta) {
        return model.withElapsed(model.getElapsed() + delta);
    }
}
