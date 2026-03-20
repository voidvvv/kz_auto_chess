package com.voidvvv.autochess.render.effect;

import com.voidvvv.autochess.model.SkillType;
import com.voidvvv.autochess.model.effect.SkillEffectType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 技能效果渲染器工厂
 * 采用工厂模式创建渲染器实例，支持运行时注册
 */
public final class SkillEffectRendererFactory {

    /**
     * 渲染器注册表：效果类型 -> 渲染器创建函数
     */
    private static final Map<SkillEffectType, Supplier<SkillEffectRenderer>> REGISTRY =
            new EnumMap<>(SkillEffectType.class);

    // 静态初始化：注册默认渲染器
    static {
        register(SkillEffectType.HEAL, HealEffectRenderer::new);
        register(SkillEffectType.AOE, AoeEffectRenderer::new);
        register(SkillEffectType.BUFF, BuffEffectRenderer::new);
        register(SkillEffectType.DEBUFF, DebuffEffectRenderer::new);
        register(SkillEffectType.BASIC, BasicEffectRenderer::new);
    }

    private SkillEffectRendererFactory() {
        // 私有构造函数，防止实例化
    }

    /**
     * 注册新的效果渲染器（扩展点）
     *
     * @param type     效果类型
     * @param supplier 渲染器创建函数
     */
    public static void register(SkillEffectType type, Supplier<SkillEffectRenderer> supplier) {
        REGISTRY.put(type, supplier);
    }

    /**
     * 创建渲染器实例
     *
     * @param type 效果类型
     * @return 对应的渲染器实例，如果未注册则返回 BasicEffectRenderer
     */
    public static SkillEffectRenderer create(SkillEffectType type) {
        Supplier<SkillEffectRenderer> supplier = REGISTRY.get(type);
        if (supplier == null) {
            return new BasicEffectRenderer();
        }
        return supplier.get();
    }

    /**
     * 根据 SkillType 转换为 SkillEffectType
     *
     * @param skillType 技能类型
     * @return 对应的效果类型
     */
    public static SkillEffectType toEffectType(SkillType skillType) {
        if (skillType == null) {
            return SkillEffectType.BASIC;
        }
        return switch (skillType) {
            case HEAL -> SkillEffectType.HEAL;
            case AOE -> SkillEffectType.AOE;
            case BUFF -> SkillEffectType.BUFF;
            case DEBUFF -> SkillEffectType.DEBUFF;
            default -> SkillEffectType.BASIC;
        };
    }

    /**
     * 检查是否已注册指定类型的渲染器
     *
     * @param type 效果类型
     * @return 是否已注册
     */
    public static boolean isRegistered(SkillEffectType type) {
        return REGISTRY.containsKey(type);
    }
}
