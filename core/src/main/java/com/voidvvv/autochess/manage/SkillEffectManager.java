package com.voidvvv.autochess.manage;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.event.GameEventListener;
import com.voidvvv.autochess.event.skill.SkillCastEvent;
import com.voidvvv.autochess.model.ModelHolder;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.model.skill.SkillContext;
import com.voidvvv.autochess.render.GameRenderer;
import com.voidvvv.autochess.render.RenderHolder;
import com.voidvvv.autochess.render.effect.SkillEffectRenderer;
import com.voidvvv.autochess.render.effect.SkillEffectRendererFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 技能效果管理器
 * 负责管理技能效果的创建、更新和渲染
 *
 * 职责：
 * - 监听 SkillCastEvent 事件
 * - 创建和管理 SkillEffectModel
 * - 协调渲染器渲染效果
 */
public class SkillEffectManager implements GameEventListener, GameRenderer {

    private final com.voidvvv.autochess.event.GameEventSystem eventSystem;
    private final ModelHolder<SkillEffectModel> effectHolder;
    private final Map<SkillEffectType, SkillEffectRenderer> renderers;

    /**
     * 效果 ID 生成器
     */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * 默认效果持续时间（秒）
     */
    private static final float DEFAULT_DURATION = 1.0f;

    public SkillEffectManager(com.voidvvv.autochess.event.GameEventSystem eventSystem) {
        this.eventSystem = eventSystem;
        this.effectHolder = new ModelHolder<>();
        this.renderers = new EnumMap<>(SkillEffectType.class);

        // 初始化所有渲染器
        for (SkillEffectType type : SkillEffectType.values()) {
            renderers.put(type, SkillEffectRendererFactory.create(type));
        }
    }

    // ========== 生命周期管理 ==========

    /**
     * 进入时注册事件监听器
     */
    public void onEnter() {
        eventSystem.registerListener(this);
        Gdx.app.log("SkillEffectManager", "SkillEffectManager initialized");
    }

    /**
     * 退出时注销事件监听器并清理效果
     */
    public void onExit() {
        eventSystem.unregisterListener(this);
        effectHolder.clear();
        Gdx.app.log("SkillEffectManager", "SkillEffectManager disposed");
    }

    // ========== 事件处理 ==========

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof SkillCastEvent skillEvent) {
            handleSkillCast(skillEvent);
        }
    }

    /**
     * 处理技能释放事件
     */
    private void handleSkillCast(SkillCastEvent event) {
        try {
            // 转换技能类型为效果类型
            SkillEffectType effectType = SkillEffectRendererFactory.toEffectType(
                event.getSkillType()
            );

            // 获取技能上下文
            SkillContext context = event.getContext();
            float range = context != null ? context.skillRange() : 100f;
            float duration = calculateDuration(effectType, context);

            // 创建效果模型
            SkillEffectModel model = new SkillEffectModel(
                generateId(),
                effectType,
                event.getCaster(),
                event.getTarget(),
                event.getSkill() != null ? event.getSkill().getName() : "Unknown",
                event.getWorldX(),
                event.getWorldY(),
                range,
                duration,
                event.getTimestamp() / 1000f
            );

            effectHolder.addModel(model);

            Gdx.app.log("SkillEffectManager",
                "Created effect: " + effectType + " at (" + event.getWorldX() + ", " + event.getWorldY() + ")");

        } catch (Exception e) {
            Gdx.app.error("SkillEffectManager", "Failed to create skill effect: " + e.getMessage(), e);
        }
    }

    /**
     * 计算效果持续时间
     */
    private float calculateDuration(SkillEffectType type, SkillContext context) {
        // 如果有上下文且有持续时间，使用上下文的值
        if (context != null && context.skillDuration() > 0) {
            return Math.min(context.skillDuration(), 3.0f); // 最大 3 秒
        }

        // 根据效果类型返回默认持续时间
        return switch (type) {
            case HEAL -> 1.0f;
            case AOE -> 0.8f;
            case BUFF -> 1.5f;
            case DEBUFF -> 1.5f;
            default -> DEFAULT_DURATION;
        };
    }

    /**
     * 生成唯一 ID
     */
    private String generateId() {
        return "effect_" + idGenerator.incrementAndGet() + "_" + System.currentTimeMillis();
    }

    // ========== 更新逻辑 ==========

    /**
     * 更新所有效果状态
     * @param delta 时间增量（秒）
     * @param currentTime 当前游戏时间（秒）
     */
    public void update(float delta, float currentTime) {
        // 获取所有效果
        List<SkillEffectModel> models = effectHolder.getModels();

        // 收集需要移除的效果
        List<SkillEffectModel> expired = new ArrayList<>();
        List<SkillEffectModel> updated = new ArrayList<>();

        for (SkillEffectModel model : models) {
            SkillEffectRenderer renderer = renderers.get(model.getType());
            SkillEffectModel newModel = renderer.update(model, delta);

            if (newModel.isExpired()) {
                expired.add(model);
            } else {
                // 需要更新模型（不可变设计）
                if (newModel != model) {
                    expired.add(model); // 移除旧模型
                    updated.add(newModel); // 添加新模型
                }
            }
        }

        // 移除过期效果
        for (SkillEffectModel model : expired) {
            effectHolder.removeModel(model);
        }

        // 添加更新后的效果
        for (SkillEffectModel model : updated) {
            effectHolder.addModel(model);
        }
    }

    // ========== 渲染 ==========

    @Override
    public void render(RenderHolder holder) {
        for (SkillEffectModel model : effectHolder.getModels()) {
            SkillEffectRenderer renderer = renderers.get(model.getType());
            if (renderer != null) {
                renderer.render(holder, model);
                holder.flush(); // 确保状态一致性
            }
        }
    }

    // ========== 工具方法 ==========

    /**
     * 获取当前活跃效果数量
     */
    public int getActiveEffectCount() {
        return effectHolder.getModels().size();
    }

    /**
     * 清除所有效果（用于战斗结束等场景）
     */
    public void clearAllEffects() {
        effectHolder.clear();
    }
}
