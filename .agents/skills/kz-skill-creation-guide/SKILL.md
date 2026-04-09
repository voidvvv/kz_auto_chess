---
name: kz-skill-creation-guide
description: KzAutoChess 技能创建指南 - 添加新技能时必须使用此 skill。当用户提到"添加新技能"、"创建技能"、"实现新技能类型"、"技能系统"等相关需求时，自动触发此 skill。此 skill 提供完整的技能架构指南，包括 Model 层、渲染层和事件系统的实现步骤。
---

# KzAutoChess 技能创建指南

本指南描述如何为 KzAutoChess 游戏添加新的技能类型，遵循项目的 Model/Updator/Manager/Render 分离架构。

## 架构概览

技能系统分为三层：

```
┌─────────────────────────────────────────────────────────────┐
│                      技能系统架构                            │
├─────────────────────────────────────────────────────────────┤
│  Model 层                                                    │
│  ├── SkillType (枚举) - 技能类型定义                         │
│  ├── SkillContext (record) - 技能参数上下文                  │
│  ├── XxxSkill.java - 具体技能实现                            │
│  └── SkillFactory.java - 技能工厂                            │
├─────────────────────────────────────────────────────────────┤
│  事件层                                                      │
│  └── BattleUnitBlackboard.tryCastSkill() 发布 SkillCastEvent │
├─────────────────────────────────────────────────────────────┤
│  渲染层                                                      │
│  ├── SkillEffectType (枚举) - 效果类型定义                   │
│  ├── SkillEffectModel.java - 效果数据模型                    │
│  ├── XxxEffectRenderer.java - 具体效果渲染器                 │
│  ├── SkillEffectRendererFactory.java - 渲染器工厂            │
│  └── SkillEffectManager.java - 效果管理器                    │
└─────────────────────────────────────────────────────────────┘
```

## 添加新技能的完整步骤

### Step 1: 添加技能类型枚举

**文件**: `core/src/main/java/com/voidvvv/autochess/model/SkillType.java`

```java
public enum SkillType {
    BASIC,
    HEAL,
    AOE,
    BUFF,
    DEBUFF,
    NEW_SKILL_TYPE  // 添加新类型
}
```

### Step 2: 实现技能 Model

**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/NewSkill.java`

**必须遵循的模式**:

```java
package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Skill;
import com.voidvvv.autochess.model.skill.exception.InvalidTargetException;
import com.voidvvv.autochess.model.skill.exception.SkillExecutionException;

/**
 * 新技能实现
 * 遵循策略模式，实现 Skill 接口
 */
public class NewSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "NewSkill";
    private static final String SKILL_NAME = "技能名称";

    private final SkillContext context;

    public NewSkill() {
        this(SkillContext.DEFAULT);
    }

    public NewSkill(SkillContext context) {
        this.context = context != null ? context : SkillContext.DEFAULT;
    }

    @Override
    public String getName() {
        return SKILL_NAME;
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        // 1. 参数校验（必须）
        if (blackboard == null) {
            throw new InvalidTargetException(getName(), "null", "Blackboard cannot be null");
        }

        BattleCharacter caster = blackboard.getSelf();
        if (caster == null) {
            throw new InvalidTargetException(getName(), "null", "Caster cannot be null");
        }

        // 2. 死亡检查（必须，避免异常日志污染）
        if (caster.isDead()) {
            throw new InvalidTargetException(getName(), caster.getName(), "Caster is dead");
        }

        try {
            // 3. 技能核心逻辑
            executeSkillLogic(caster, blackboard);

            // 4. 日志记录
            if (Gdx.app != null) {
                Gdx.app.log(TAG, String.format("[%s] cast %s",
                        caster.getName(), getName()));
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to cast %s for %s: %s",
                    getName(), caster.getName(), e.getMessage());
            if (Gdx.app != null) {
                Gdx.app.error(TAG, errorMessage, e);
            }
            throw new SkillExecutionException(errorMessage, e);
        }
    }

    private void executeSkillLogic(BattleCharacter caster, BattleUnitBlackboard blackboard) {
        // 使用 context 获取技能参数:
        // - context.skillValue() - 技能数值
        // - context.skillRange() - 技能范围
        // - context.skillDuration() - 持续时间
        // - context.damageType() - 伤害类型
    }

    public SkillContext getContext() {
        return context;
    }
}
```

**关键要点**:
- 必须检查 `caster.isDead()`，否则死亡角色释放技能会产生大量异常日志
- 使用 `SkillContext` 获取配置参数，不要硬编码
- 日志使用 `Gdx.app.log/error`，检查 `Gdx.app != null`

### Step 3: 注册到技能工厂

**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/SkillFactory.java`

在 `createSkill` 方法中添加:

```java
return switch (skillType) {
    case BASIC -> new BasicSkill();
    case HEAL -> new HealSkill(context);
    case AOE -> new AoeSkill(context);
    case BUFF -> new BuffSkill(context);
    case DEBUFF -> new DebuffSkill(context);
    case NEW_SKILL_TYPE -> new NewSkill(context);  // 添加这行
};
```

### Step 4: 添加效果类型枚举

**文件**: `core/src/main/java/com/voidvvv/autochess/model/effect/SkillEffectType.java`

```java
public enum SkillEffectType {
    HEAL(new Color(0.2f, 1f, 0.2f, 0.8f)),
    AOE(new Color(1f, 0.5f, 0f, 0.6f)),
    BUFF(new Color(0f, 1f, 1f, 0.7f)),
    DEBUFF(new Color(0.6f, 0f, 0.8f, 0.7f)),
    BASIC(Color.WHITE),
    NEW_EFFECT_TYPE(new Color(...));  // 添加新效果类型

    // ...
}
```

### Step 5: 实现效果渲染器

**文件**: `core/src/main/java/com/voidvvv/autochess/render/effect/NewEffectRenderer.java`

**必须遵循的模式**:

```java
package com.voidvvv.autochess.render.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.voidvvv.autochess.model.effect.SkillEffectModel;
import com.voidvvv.autochess.model.effect.SkillEffectType;
import com.voidvvv.autochess.render.RenderHolder;

/**
 * 新效果渲染器
 * 实现技能效果的视觉表现
 */
public class NewEffectRenderer implements SkillEffectRenderer {

    private static final Color EFFECT_COLOR = new Color(...);
    private static final float DURATION = 1.0f;

    @Override
    public void render(RenderHolder holder, SkillEffectModel model) {
        float progress = model.getProgress();
        float x = model.getWorldX();
        float y = model.getWorldY();

        // 1. 渲染视觉效果
        renderVisualEffect(holder, x, y, progress);

        // 2. 渲染技能名称（必须调用，使用效果类型默认颜色）
        SkillNameDisplay.render(holder, model, x, y, 70f, EFFECT_COLOR);
    }

    private void renderVisualEffect(RenderHolder holder, float x, float y, float progress) {
        float alpha = 1f - progress;

        ShapeRenderer sr = holder.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(new Color(EFFECT_COLOR.r, EFFECT_COLOR.g, EFFECT_COLOR.b, alpha));

        // 绘制效果图形
        sr.circle(x, y, 50f * progress);

        sr.end();
    }

    @Override
    public SkillEffectType getSupportedType() {
        return SkillEffectType.NEW_EFFECT_TYPE;
    }
}
```

**渲染器关键要点**:

1. **ShapeRenderer 和 SpriteBatch 切换**:
   ```java
   // ShapeRenderer 渲染图形
   sr.begin(ShapeRenderer.ShapeType.Filled);
   sr.rect(...);
   sr.end();

   // SpriteBatch 渲染文字（通过 SkillNameDisplay）
   SkillNameDisplay.render(holder, model, x, y, yOffset, color);
   ```

2. **进度计算**: 使用 `model.getProgress()` (0.0-1.0) 控制动画进度

3. **颜色透明度**: 根据 progress 计算淡出效果
   ```java
   float alpha = 1f - progress;  // 渐隐效果
   ```

4. **目标位置**: 如果有目标，从 `model.getTarget()` 获取
   ```java
   BattleCharacter target = model.getTarget();
   if (target != null) {
       x = target.getX();
       y = target.getY();
   }
   ```

### Step 6: 注册渲染器到工厂

**文件**: `core/src/main/java/com/voidvvv/autochess/render/effect/SkillEffectRendererFactory.java`

1. 添加静态导入:
```java
import com.voidvvv.autochess.render.effect.NewEffectRenderer;
```

2. 在静态初始化块中注册:
```java
static {
    register(SkillEffectType.HEAL, HealEffectRenderer::new);
    register(SkillEffectType.AOE, AoeEffectRenderer::new);
    register(SkillEffectType.BUFF, BuffEffectRenderer::new);
    register(SkillEffectType.DEBUFF, DebuffEffectRenderer::new);
    register(SkillEffectType.BASIC, BasicEffectRenderer::new);
    register(SkillEffectType.NEW_EFFECT_TYPE, NewEffectRenderer::new);  // 添加这行
}
```

3. 更新 `toEffectType` 方法:
```java
return switch (skillType) {
    case HEAL -> SkillEffectType.HEAL;
    case AOE -> SkillEffectType.AOE;
    case BUFF -> SkillEffectType.BUFF;
    case DEBUFF -> SkillEffectType.DEBUFF;
    case NEW_SKILL_TYPE -> SkillEffectType.NEW_EFFECT_TYPE;  // 添加这行
    default -> SkillEffectType.BASIC;
};
```

### Step 7: 验证编译

```bash
gradle compileJava
```

### Step 8: 更新卡牌配置

在 `character_stats.json` 或相关配置文件中，为新角色指定新技能类型:

```json
{
  "cardId": 100,
  "name": "新角色",
  "skillType": "NEW_SKILL_TYPE",
  "skillValue": 50,
  "skillRange": 100,
  "skillDuration": 5
}
```

## 文件清单

添加新技能需要创建/修改以下文件：

| 文件 | 操作 | 说明 |
|------|------|------|
| `model/SkillType.java` | 修改 | 添加枚举值 |
| `model/skill/NewSkill.java` | **新建** | 技能逻辑实现 |
| `model/skill/SkillFactory.java` | 修改 | 注册技能创建 |
| `model/effect/SkillEffectType.java` | 修改 | 添加效果类型 |
| `render/effect/NewEffectRenderer.java` | **新建** | 效果渲染实现 |
| `render/effect/SkillEffectRendererFactory.java` | 修改 | 注册渲染器 |

## 常见问题

### Q: 技能效果不显示？

检查 `Battlefield.eventSystem` 是否已设置。在 `BattleManager` 构造函数中应该有:
```java
this.battlefield.setEventSystem(eventSystem);
```

### Q: 大量异常日志？

确保在技能 `cast()` 方法中检查 `caster.isDead()`。

### Q: 渲染状态冲突？

确保 `ShapeRenderer` 和 `SpriteBatch` 的 `begin()/end()` 成对调用，使用 `holder.flush()` 切换状态。

## 参考文件

- 现有技能实现: `model/skill/HealSkill.java`, `model/skill/AoeSkill.java`
- 现有渲染器: `render/effect/HealEffectRenderer.java`, `render/effect/AoeEffectRenderer.java`
- 架构文档: `.Codex/docs/architectural_patterns.md`
- 代码规范: `.Codex/skills/kz-autochess-code-guidelines/SKILL.md`
