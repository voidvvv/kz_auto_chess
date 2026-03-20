---
title: feat: 技能效果渲染系统设计
type: feat
status: completed
date: 2026-03-20
origin: docs/brainstorms/2026-03-20-skill-effects-requirements.md
completed_date: 2026-03-20
---

# 技能效果渲染系统设计

## Overview

为 KzAutoChess 游戏设计一个可扩展的技能效果渲染系统，支持治疗、增益、伤害、范围等多种技能类型的视觉表现。系统遵循项目现有的 Model/Updator/Manager/Render 分离架构，采用策略模式和工厂模式实现高可扩展性。

## Problem Statement / Motivation

自走棋游戏的技能系统框架已完整（Skill 接口、SkillType 枚举、魔法值系统、释放机制），四种技能类型（HEAL/AOE/BUFF/DEBUFF）的逻辑实现已完成。但技能释放时缺乏视觉反馈：

- 玩家无法看到技能何时被释放
- 不同技能类型没有视觉区分
- AOE 技能没有范围指示器
- 治疗和增益/减益效果不可见

这导致战斗体验单调，玩家难以理解战斗进程和策略效果。

**来源**: [origin: R6 视觉反馈](docs/brainstorms/2026-03-20-skill-effects-requirements.md)

## Proposed Solution

设计一个分层渲染系统，采用 **策略模式 + 事件驱动** 架构：

```
技能释放 → SkillCastEvent → SkillEffectManager → SkillEffectModel → SkillEffectRenderer
```

### 核心设计原则

1. **策略模式**: 每种技能类型对应独立的渲染器实现
2. **工厂模式**: 通过 `SkillEffectRendererFactory` 创建渲染器
3. **事件驱动**: 技能释放触发 `SkillCastEvent`，渲染系统监听并响应
4. **Model/Render 分离**: 数据模型与渲染逻辑完全解耦
5. **生命周期管理**: 效果有明确的开始、持续、结束阶段

## Technical Approach

### Architecture

```
com.voidvvv.autochess
├── model/
│   └── effect/                          # 新增目录
│       ├── SkillEffectModel.java        # 技能效果数据模型
│       ├── SkillEffectType.java         # 效果类型枚举
│       └── SkillEffectHolder.java       # 效果模型容器
├── event/
│   └── skill/                           # 新增目录
│       └── SkillCastEvent.java          # 技能释放事件
├── render/
│   └── effect/                          # 新增目录
│       ├── SkillEffectRenderer.java     # 渲染接口
│       ├── SkillEffectRendererFactory.java  # 渲染器工厂
│       ├── HealEffectRenderer.java      # 治疗效果渲染器
│       ├── AoeEffectRenderer.java       # AOE 效果渲染器
│       ├── BuffEffectRenderer.java      # 增益效果渲染器
│       ├── DebuffEffectRenderer.java    # 减益效果渲染器
│       └── SkillNameDisplay.java        # 技能名称显示组件
└── manage/
    └── SkillEffectManager.java          # 技能效果管理器
```

### Core Interfaces

#### 1. SkillEffectModel (Model 层)

```java
// model/effect/SkillEffectModel.java
public class SkillEffectModel {
    private final String id;                    // 唯一标识
    private final SkillEffectType type;         // 效果类型
    private final BattleCharacter caster;       // 释放者
    private final BattleCharacter target;       // 目标（可为 null）
    private final String skillName;             // 技能名称
    private final float worldX;                 // 效果中心 X
    private final float worldY;                 // 效果中心 Y
    private final float range;                  // 范围（AOE 用）
    private final float duration;               // 持续时间
    private final float startTime;              // 开始时间
    private float elapsed;                      // 已过时间

    // 不可变设计，通过 withXxx() 方法创建新实例
    public SkillEffectModel withElapsed(float elapsed) { ... }

    public boolean isExpired() { return elapsed >= duration; }
    public float getProgress() { return Math.min(1f, elapsed / duration); }
}
```

#### 2. SkillEffectType (枚举)

```java
// model/effect/SkillEffectType.java
public enum SkillEffectType {
    HEAL(Color.GREEN),          // 绿色
    AOE(Color.ORANGE),          // 橙色
    BUFF(Color.CYAN),           // 青色
    DEBUFF(Color.PURPLE),       // 紫色
    BASIC(Color.WHITE);         // 白色

    private final Color defaultColor;

    SkillEffectType(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Color getDefaultColor() { return defaultColor; }
}
```

#### 3. SkillCastEvent (Event 层)

```java
// event/skill/SkillCastEvent.java
public class SkillCastEvent implements GameEvent {
    private long timestamp;
    private final BattleCharacter caster;
    private final BattleCharacter target;        // 可为 null
    private final Skill<?> skill;
    private final SkillContext context;
    private final float worldX;                  // 释放位置
    private final float worldY;

    // constructors and getters...
}
```

#### 4. SkillEffectRenderer (Render 层)

```java
// render/effect/SkillEffectRenderer.java
public interface SkillEffectRenderer {
    /**
     * 渲染技能效果
     * @param holder 渲染上下文
     * @param model 效果数据模型
     */
    void render(RenderHolder holder, SkillEffectModel model);

    /**
     * 此渲染器支持的效果类型
     */
    SkillEffectType getSupportedType();

    /**
     * 更新效果状态（可选）
     * @param model 效果模型
     * @param delta 时间增量
     * @return 更新后的模型（不可变设计）
     */
    default SkillEffectModel update(SkillEffectModel model, float delta) {
        return model.withElapsed(model.getElapsed() + delta);
    }
}
```

### Renderer Implementations

#### HealEffectRenderer

```java
// render/effect/HealEffectRenderer.java
public class HealEffectRenderer implements SkillEffectRenderer {
    private static final Color HEAL_COLOR = new Color(0.2f, 1f, 0.2f, 0.8f);
    private static final float RING_EXPAND_SPEED = 50f;  // 扩散速度

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        BattleCharacter caster = model.getCaster();

        // 1. 渲染扩散光环
        renderExpandingRing(holder, caster, progress);

        // 2. 渲染治疗数字（向上飘动）
        renderHealText(holder, caster, model);

        // 3. 渲染技能名称
        renderSkillName(holder, caster, model);
    }

    private void renderExpandingRing(RenderHolder holder,
                                      BattleCharacter caster,
                                      float progress) {
        ShapeRenderer sr = holder.getShapeRenderer();
        float radius = RING_EXPAND_SPEED * progress;
        float alpha = 1f - progress;  // 逐渐透明

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(HEAL_COLOR.r, HEAL_COLOR.g, HEAL_COLOR.b, alpha));
        sr.circle(caster.getX(), caster.getY(), radius);
        sr.end();
    }

    // ... 其他渲染方法
}
```

#### AoeEffectRenderer

```java
// render/effect/AoeEffectRenderer.java
public class AoeEffectRenderer implements SkillEffectRenderer {
    private static final Color AOE_COLOR = new Color(1f, 0.5f, 0f, 0.6f);

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        float radius = model.getRange();

        // 1. 渲染范围指示器（脉冲圆环）
        renderPulsingRange(holder, model, radius, progress);

        // 2. 渲染爆炸效果（从中心扩散）
        renderExplosion(holder, model, progress);

        // 3. 渲染技能名称
        renderSkillName(holder, model);
    }

    private void renderPulsingRange(RenderHolder holder,
                                     SkillEffectModel model,
                                     float radius,
                                     float progress) {
        ShapeRenderer sr = holder.getShapeRenderer();
        float pulseRadius = radius * (0.8f + 0.2f * (float)Math.sin(progress * Math.PI * 4));
        float alpha = 0.8f * (1f - progress * 0.5f);

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(AOE_COLOR.r, AOE_COLOR.g, AOE_COLOR.b, alpha));
        sr.circle(model.getWorldX(), model.getWorldY(), pulseRadius);
        sr.circle(model.getWorldX(), model.getWorldY(), radius * 0.5f);
        sr.end();
    }
}
```

#### BuffEffectRenderer

```java
// render/effect/BuffEffectRenderer.java
public class BuffEffectRenderer implements SkillEffectRenderer {
    private static final Color BUFF_COLOR = new Color(0f, 1f, 1f, 0.7f);

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        // 1. 渲染向上箭头效果
        renderBuffArrows(holder, model);

        // 2. 渲染目标轮廓高亮
        renderTargetHighlight(holder, model);

        // 3. 渲染技能名称
        renderSkillName(holder, model);
    }
}
```

#### DebuffEffectRenderer

```java
// render/effect/DebuffEffectRenderer.java
public class DebuffEffectRenderer implements SkillEffectRenderer {
    private static final Color DEBUFF_COLOR = new Color(0.6f, 0f, 0.8f, 0.7f);

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        // 1. 渲染向下箭头效果
        renderDebuffArrows(holder, model);

        // 2. 渲染目标暗色轮廓
        renderTargetOutline(holder, model);

        // 3. 渲染技能名称
        renderSkillName(holder, model);
    }
}
```

### SkillEffectRendererFactory

```java
// render/effect/SkillEffectRendererFactory.java
public final class SkillEffectRendererFactory {
    private static final Map<SkillEffectType, Supplier<SkillEffectRenderer>> REGISTRY = new EnumMap<>(SkillEffectType.class);

    static {
        // 默认注册
        register(SkillEffectType.HEAL, HealEffectRenderer::new);
        register(SkillEffectType.AOE, AoeEffectRenderer::new);
        register(SkillEffectType.BUFF, BuffEffectRenderer::new);
        register(SkillEffectType.DEBUFF, DebuffEffectRenderer::new);
        register(SkillEffectType.BASIC, BasicEffectRenderer::new);
    }

    /**
     * 注册新的效果渲染器（扩展点）
     */
    public static void register(SkillEffectType type, Supplier<SkillEffectRenderer> supplier) {
        REGISTRY.put(type, supplier);
    }

    /**
     * 创建渲染器实例
     */
    public static SkillEffectRenderer create(SkillEffectType type) {
        Supplier<SkillEffectRenderer> supplier = REGISTRY.get(type);
        if (supplier == null) {
            return new BasicEffectRenderer();  // 默认回退
        }
        return supplier.get();
    }

    /**
     * 根据 SkillType 转换为 SkillEffectType
     */
    public static SkillEffectType toEffectType(SkillType skillType) {
        return switch (skillType) {
            case HEAL -> SkillEffectType.HEAL;
            case AOE -> SkillEffectType.AOE;
            case BUFF -> SkillEffectType.BUFF;
            case DEBUFF -> SkillEffectType.DEBUFF;
            default -> SkillEffectType.BASIC;
        };
    }
}
```

### SkillEffectManager

```java
// manage/SkillEffectManager.java
public class SkillEffectManager implements GameEventListener, GameRenderer {
    private final GameEventSystem eventSystem;
    private final ModelHolder<SkillEffectModel> effectHolder;
    private final Map<SkillEffectType, SkillEffectRenderer> renderers;

    public SkillEffectManager(GameEventSystem eventSystem) {
        this.eventSystem = eventSystem;
        this.effectHolder = new ModelHolder<>();
        this.renderers = new EnumMap<>(SkillEffectType.class);

        // 初始化所有渲染器
        for (SkillEffectType type : SkillEffectType.values()) {
            renderers.put(type, SkillEffectRendererFactory.create(type));
        }
    }

    public void onEnter() {
        eventSystem.registerListener(this);
    }

    public void onExit() {
        eventSystem.unregisterListener(this);
        effectHolder.clear();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof SkillCastEvent skillEvent) {
            handleSkillCast(skillEvent);
        }
    }

    private void handleSkillCast(SkillCastEvent event) {
        SkillEffectType effectType = SkillEffectRendererFactory.toEffectType(
            event.getSkill().getSkillType()
        );

        SkillEffectModel model = new SkillEffectModel(
            generateId(),
            effectType,
            event.getCaster(),
            event.getTarget(),
            event.getSkill().getName(),
            event.getWorldX(),
            event.getWorldY(),
            event.getContext().skillRange(),
            calculateDuration(effectType, event.getContext()),
            event.getTimestamp() / 1000f
        );

        effectHolder.addModel(model);
    }

    public void update(float delta, float currentTime) {
        // 更新所有效果，移除已过期的
        List<SkillEffectModel> expired = new ArrayList<>();
        List<SkillEffectModel> updated = new ArrayList<>();

        for (SkillEffectModel model : effectHolder.getModels()) {
            SkillEffectRenderer renderer = renderers.get(model.getType());
            SkillEffectModel newModel = renderer.update(model, delta);

            if (newModel.isExpired()) {
                expired.add(model);
            } else {
                updated.add(newModel);
            }
        }

        // 移除过期效果
        for (Model model : expired) {
            effectHolder.removeModel(model);
        }
    }

    @Override
    public void render(RenderHolder holder) {
        for (SkillEffectModel model : effectHolder.getModels()) {
            SkillEffectRenderer renderer = renderers.get(model.getType());
            renderer.render(holder, model);
            holder.flush();  // 确保状态一致性
        }
    }
}
```

### Integration Points

#### 1. 修改 BattleUnitBlackboard 触发事件

```java
// battle/BattleUnitBlackboard.java (修改 tryCastSkill 方法)
public boolean tryCastSkill() {
    if (mana == null || skill == null) return false;
    if (!mana.isFull()) return false;

    if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        return false;
    }

    try {
        // 发布技能释放事件
        SkillCastEvent event = new SkillCastEvent(
            self,                    // caster
            target,                  // target
            skill,                   // skill
            getSkillContext(self),   // context
            self.getX(),             // worldX
            self.getY()              // worldY
        );
        battlefield.getEventSystem().postEvent(event);

        skill.cast(this);
        mana.reset();
        return true;
    } catch (Exception e) {
        Gdx.app.error("SkillSystem", "技能释放失败: " + e.getMessage(), e);
        return false;
    }
}
```

#### 2. 集成到 AutoChessGameMode

```java
// game/AutoChessGameMode.java
public class AutoChessGameMode implements GameMode {
    // 新增字段
    private final SkillEffectManager skillEffectManager;

    public AutoChessGameMode(..., GameEventSystem eventSystem, ...) {
        // 初始化技能效果管理器
        this.skillEffectManager = new SkillEffectManager(eventSystem);

        // 注册到渲染协调器
        renderCoordinator.addRenderer(skillEffectManager);
    }

    @Override
    public void onEnter() {
        // ... 现有代码 ...
        skillEffectManager.onEnter();
    }

    @Override
    public void onExit() {
        // ... 现有代码 ...
        skillEffectManager.onExit();
    }

    @Override
    public void update(float delta) {
        eventSystem.dispatch();  // 先分发事件
        // ... 现有代码 ...
        skillEffectManager.update(delta, currentTime);
    }
}
```

### Implementation Phases

#### Phase 1: 基础架构 (Day 1)

- [ ] 创建 `model/effect/` 包结构
- [ ] 实现 `SkillEffectType` 枚举
- [ ] 实现 `SkillEffectModel` 数据类
- [ ] 创建 `event/skill/SkillCastEvent`
- [ ] 编写单元测试验证数据模型

**验收标准**:
- 所有数据模型可正确创建和访问
- `SkillEffectModel` 不可变设计通过测试

#### Phase 2: 渲染接口与工厂 (Day 2)

- [ ] 创建 `render/effect/` 包结构
- [ ] 实现 `SkillEffectRenderer` 接口
- [ ] 实现 `SkillEffectRendererFactory`
- [ ] 实现 `BasicEffectRenderer`（默认回退）
- [ ] 实现 `SkillNameDisplay` 通用组件
- [ ] 编写渲染器工厂测试

**验收标准**:
- 工厂可正确创建所有注册的渲染器
- 默认渲染器可正常工作

#### Phase 3: 具体渲染器实现 (Day 3-4)

- [ ] 实现 `HealEffectRenderer`（绿色扩散光环 + 飘字）
- [ ] 实现 `AoeEffectRenderer`（范围指示器 + 爆炸效果）
- [ ] 实现 `BuffEffectRenderer`（向上箭头 + 轮廓高亮）
- [ ] 实现 `DebuffEffectRenderer`（向下箭头 + 暗色轮廓）
- [ ] 编写各渲染器测试

**验收标准**:
- 每种技能类型有独特的视觉效果
- 效果有明确的开始、持续、结束阶段

#### Phase 4: 管理器与集成 (Day 5)

- [ ] 实现 `SkillEffectManager`
- [ ] 修改 `BattleUnitBlackboard.tryCastSkill()` 触发事件
- [ ] 集成到 `AutoChessGameMode`
- [ ] 注册到 `RenderCoordinator`
- [ ] 端到端测试

**验收标准**:
- 技能释放时自动显示对应效果
- 效果在持续时间后自动消失
- 不影响现有战斗逻辑

#### Phase 5: 优化与扩展 (Day 6)

- [ ] 性能优化（批处理、对象池）
- [ ] 添加效果配置支持（从 JSON 加载）
- [ ] 文档更新
- [ ] 代码审查

**验收标准**:
- 多个效果同时渲染时帧率稳定
- 可通过配置文件自定义效果参数

## System-Wide Impact

### Interaction Graph

```
Skill.cast() → BattleUnitBlackboard.tryCastSkill()
    → GameEventSystem.postEvent(SkillCastEvent)
    → GameEventSystem.dispatch()
    → SkillEffectManager.onGameEvent()
    → SkillEffectModel 创建
    → RenderCoordinator.renderAll()
    → SkillEffectManager.render()
    → SkillEffectRenderer.render()
```

### Error & Failure Propagation

- 渲染器创建失败 → 使用 `BasicEffectRenderer` 回退
- 效果模型创建失败 → 记录日志，不影响技能释放
- 渲染异常 → 捕获并记录，不影响游戏主循环

### State Lifecycle Risks

- 效果模型存储在 `ModelHolder` 中，有明确的添加/移除
- 回合结束时调用 `effectHolder.clear()` 清理
- 过期效果在 `update()` 中自动移除

### API Surface Parity

- 新增 `SkillEffectRenderer` 接口，与现有 `GameRenderer` 并行
- 新增 `SkillCastEvent`，与现有事件类型并行
- 不修改现有接口，完全向后兼容

## Acceptance Criteria

### Functional Requirements

- [ ] 治疗技能释放时显示绿色扩散光环和飘字
- [ ] AOE 技能释放时显示范围指示器和爆炸效果
- [ ] BUFF 技能施加时显示向上箭头和轮廓高亮
- [ ] DEBUFF 技能施加时显示向下箭头和暗色轮廓
- [ ] 技能释放时在角色头顶显示技能名称
- [ ] 效果持续时间结束后自动消失
- [ ] 战斗结束时清除所有效果

### Non-Functional Requirements

- [ ] 添加新技能类型时只需实现 `SkillEffectRenderer` 并注册
- [ ] 渲染器通过工厂创建，支持运行时注册
- [ ] 不影响现有战斗逻辑性能
- [ ] 遵循 Model/Updator/Manager/Render 分离原则

### Quality Gates

- [ ] 新代码有对应的单元测试
- [ ] 渲染器工厂测试覆盖所有类型
- [ ] 端到端测试验证效果显示
- [ ] 代码符合项目编码规范

## Success Metrics

1. **视觉反馈可见性**: 100% 的技能释放有对应的视觉效果
2. **可扩展性**: 添加新技能类型的渲染器仅需 1 个新类 + 1 行注册代码
3. **性能影响**: 多个效果同时渲染时帧率下降 < 5%

## Dependencies & Prerequisites

- 现有 `Skill` 接口和技能实现
- 现有 `GameEventSystem` 事件系统
- 现有 `RenderCoordinator` 渲染协调器
- 现有 `ModelHolder` 容器模式
- LibGDX ShapeRenderer 和 SpriteBatch

## Risk Analysis & Mitigation

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 多个效果同时渲染性能下降 | 中 | 使用对象池复用模型，批处理渲染 |
| 渲染器注册遗漏 | 低 | 工厂提供默认回退，单元测试覆盖 |
| 效果模型内存泄漏 | 中 | 确保过期效果被移除，回合结束清理 |
| 与现有渲染冲突 | 低 | 使用 `holder.flush()` 确保状态隔离 |

## Future Considerations

### 扩展点设计

1. **粒子特效系统**: 未来可替换简单图形为粒子特效
2. **配置化效果**: 从 JSON 加载效果参数（颜色、持续时间、动画曲线）
3. **组合效果**: 一个技能触发多种效果（如 AOE + DEBUFF）
4. **羁绊增强效果**: 根据羁绊等级调整效果视觉强度

### 新技能类型扩展示例

```java
// 添加新的召唤技能渲染器
public class SummonEffectRenderer implements SkillEffectRenderer {
    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        // 召唤门特效
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.SUMMON;  // 新增枚举值
    }
}

// 注册新渲染器
SkillEffectRendererFactory.register(SkillEffectType.SUMMON, SummonEffectRenderer::new);
```

## Documentation Plan

- [ ] 更新 `docs/architectural_patterns.md` 添加技能渲染系统说明
- [ ] 更新 `docs/core_flow.md` 添加技能效果渲染流程图
- [ ] 创建 `docs/skills/adding-skill-effects.md` 扩展指南

## Sources & References

### Origin

- **Origin document**: [docs/brainstorms/2026-03-20-skill-effects-requirements.md](../brainstorms/2026-03-20-skill-effects-requirements.md)
  - Key decisions carried forward:
    - AOE 目标选择: 圆形范围
    - HEAL 目标选择: 自身
    - BUFF 目标选择: 全体友方
    - DEBUFF 目标选择: 当前目标
    - 视觉反馈: 技能名称 + 简单特效

### Internal References

- Skill 接口: `model/Skill.java:1-21`
- 技能实现: `model/skill/HealSkill.java`, `model/skill/AoeSkill.java`, etc.
- 渲染接口: `render/GameRenderer.java:1-18`
- 渲染协调器: `render/RenderCoordinator.java:16-70`
- 事件系统: `event/GameEventSystem.java:14-60`
- 黑板模式: `battle/BattleUnitBlackboard.java:34-502`

### Related Work

- 现有伤害渲染: `render/DamageLineRender.java:1-40`
- 血条渲染: `render/HealthBarRenderer.java`
- 魔法条渲染: `render/ManaBarRenderer.java`
