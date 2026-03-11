---
title: feat: Add combat skill system with mana
type: feat
status: active
date: 2026-03-11
origin: docs/brainstorms/2026-03-11-skill-system-brainstorm.md
---

# feat: Add combat skill system with mana

## Overview

为KZ AutoChess游戏添加技能机制，包括魔法值系统、技能释放和可视化渲染。系统采用策略模式实现开闭原则，所有战斗相关状态存储在`BattleUnitBlackboard`中。

## Problem Statement / Motivation

当前游戏角色只有普通攻击，缺乏技能系统来增加战斗深度和策略性。添加技能系统可以：
- 增加战斗策略维度
- 提供角色差异化（不同职业有不同技能）
- 创造更丰富的游戏体验

## Proposed Solution

采用策略模式实现技能系统，遵循项目的model/updater/render分离架构。

### 核心功能

1. **魔法值系统**：每个角色有可配置的魔法值上限，初始值为0
2. **魔法值增长**：随时间自然恢复 + 攻击时额外获得
3. **技能释放**：魔法值满时，在空闲状态下释放技能
4. **技能渲染**：在角色下方显示魔法条
5. **可扩展架构**：遵循开闭原则，方便添加新技能

## Technical Considerations

### 架构模式

```
Skill (interface)
    ↓
BasicSkill (implementation - console print)
    ↓
Future: HealSkill, AOESkill, etc.
```

### 数据存放（see brainstorm: ManaComponent内部类设计）

- **BattleUnitBlackboard**：添加`ManaComponent`内部类和`Skill skill`字段
- **CharacterStats**：添加`maxMana`字段
- **Card**：添加`skillType`枚举和`maxMana`覆盖值

### 集成点

| 文件 | 集成内容 |
|------|---------|
| `battle/BattleUnitBlackboard.java` | 添加ManaComponent内部类、skill字段、updateMana()、onAttackGainMana()、tryCastSkill()方法 |
| `model/CharacterStats.java` | 添加maxMana字段和getter/setter |
| `model/Card.java` | 添加skillType枚举和maxMana字段 |
| `sm/state/common/NormalState.java` | 在onUpdate()中调用tryCastSkill() |
| `render/BattleCharacterRender.java` | 添加魔法条渲染逻辑 |
| `battle/BattleUnitBlackboard.java:171` | update()方法中调用updateMana(delta) |
| `battle/BattleUnitBlackboard.java:93` | onMessageDoAttack()方法中调用onAttackGainMana() |

### System-Wide Impact

**Interaction Graph:**
```
GameScreen.updateBattle()
  → BattleUnitBlackboard.update(delta)
    → updateMana(delta) [NEW: 增加魔法值]
  → NormalState.update()
    → tryCastSkill() [NEW: 检查并释放技能]
      → Skill.cast(blackboard)
        → System.out.println() [MVP: 控制台输出]
```

**Error Propagation:**
- ManaComponent值在每次更新后被clamp到[0, maxMana]
- Skill.cast()调用包装在try-catch中，异常时记录并继续
- Null skill检查在tryCastSkill()中进行

**State Lifecycle Risks:**
- BattleCharacter.reset()时重置currentMana为0
- 角色死亡时不更新魔法值（通过isDead()检查）

**API Surface Parity:**
- 现有Damage/DamageEvent系统可用于未来技能伤害
- SynergyEffect.manaRegenBonus已存在，可后续集成

## Acceptance Criteria

### 功能需求

- [ ] 创建`Skill`接口，包含`getName()`和`cast(BattleUnitBlackboard)`方法
- [ ] 创建`BasicSkill`实现类，控制台打印技能释放信息
- [ ] 创建`SkillType`枚举（BASIC, HEAL, AOE等）
- [ ] 在`BattleUnitBlackboard`中添加`ManaComponent`内部类（currentMana, maxMana, regenRate, attackGain）
- [ ] 在`BattleUnitBlackboard`中添加`Skill skill`字段
- [ ] 实现`updateMana(float delta)`方法，随时间增加魔法值
- [ ] 实现`onAttackGainMana()`方法，攻击时额外增加魔法值
- [ ] 实现`tryCastSkill()`方法，只在NORMAL_STATE且魔法值满时释放技能
- [ ] 在`CharacterStats`中添加`maxMana`字段
- [ ] 在`Card`中添加`skillType`枚举和`maxMana`字段
- [ ] 创建`ManaBarRenderer`类，在角色下方渲染魔法条
- [ ] 在`NormalState.update()`中调用`tryCastSkill()`
- [ ] 在`BattleUnitBlackboard.update()`中调用`updateMana(delta)`
- [ ] 在`BattleUnitBlackboard.onMessageDoAttack()`中调用`onAttackGainMana()`
- [ ] 魔法值clamp到[0, maxMana]范围
- [ ] 添加null skill检查
- [ ] 添加异常处理

### 非功能需求

- [ ] 魔法值使用float类型，与现有属性保持一致
- [ ] 魔法条使用ShapeRenderer渲染，与现有渲染风格一致
- [ ] 技能释放后魔法值清零
- [ ] 魔法条位置：角色下方(size/2 + padding)

### 质量门控

- [ ] 遵循model/updater/render分离原则
- [ ] 所有新类添加适当的文档注释
- [ ] 遵循现有代码风格

## Success Metrics

- 技能成功释放时控制台有输出
- 魔法条正确显示在角色下方
- 魔法值随时间和攻击正确增长
- 技能只在空闲状态释放，不中断攻击
- 代码通过项目现有测试

## MVP Configuration Values

**第一优先级**（规划时确定的默认数值，可后续调整）：

```java
// 默认魔法值配置
private static final float DEFAULT_MAX_MANA = 100f;
private static final float DEFAULT_REGEN_RATE = 10f;    // 每秒恢复10点
private static final float DEFAULT_ATTACK_GAIN = 20f;    // 每次攻击获得20点

// 不同角色类型的默认魔法值上限
switch (card.getType()) {
    case MAGE: maxMana = 150f;      // 法师上限更高
    case WARRIOR: maxMana = 80f;    // 战士较低
    case ARCHER: maxMana = 100f;      // 弓手中等
    case ASSASSIN: maxMana = 90f;    // 刺客中等
    case TANK: maxMana = 70f;       // 坦克较低
    default: maxMana = DEFAULT_MAX_MANA;
}
```

## Dependencies & Prerequisites

- 项目已实现的状态机系统（States, StateMachine）
- 现有BattleUnitBlackboard架构
- 现有渲染系统（ShapeRenderer）

## Files to Create

```bash
core/src/main/java/com/voidvvv/autochess/model/Skill.java
core/src/main/java/com/voidvvv/autochess/model/skill/SkillType.java
core/src/main/java/com/voidvvv/autochess/model/skill/BasicSkill.java
core/src/main/java/com/voidvvv/autochess/render/ManaBarRenderer.java
```

## Files to Modify

```bash
core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java
core/src/main/java/com/voidvvv/autochess/model/CharacterStats.java
core/src/main/java/com/voidvvv/autochess/model/Card.java
core/src/main/java/com/voidvvv/autochess/sm/state/common/NormalState.java
core/src/main/java/com/voidvvv/autochess/render/BattleCharacterRender.java
```

## Implementation Notes

### ManaComponent内部类结构

```java
private static class ManaComponent {
    float currentMana;
    float maxMana;
    float regenRate;     // 每秒恢复量
    float attackGain;    // 攻击获得量

    ManaComponent(float maxMana, float regenRate, float attackGain) {
        this.currentMana = 0f;
        this.maxMana = maxMana;
        this.regenRate = regenRate;
        this.attackGain = attackGain;
    }

    void gainMana(float amount) {
        this.currentMana += amount;
        this.currentMana = Math.min(this.currentMana, this.maxMana);
    }
}
```

### 技能架构决策

**重要**：为了避免model层依赖battle层，Skill接口使用泛型解耦

```java
// Skill接口在model包中，不依赖battle包
public interface Skill<T> {
    String getName();
    void cast(T context);
}

// 实现类时指定具体类型
public class BasicSkill implements Skill<BattleUnitBlackboard> {
    // ...
}
```

**优点**：
- model层的Skill接口不依赖battle包
- 遵循依赖倒置原则（依赖抽象而非具体实现）
- 更好的可测试性（可以传入mock context）
- 符合SOLID的单一职责原则

### Skill接口具体实现

```java
package com.voidvvv.autochess.model;

/**
 * 技能接口 - 使用泛型避免跨层依赖
 * 遵循策略模式，实现开闭原则
 */
public interface Skill<T> {
    /**
     * 获取技能名称
     */
    String getName();

    /**
     * 释放技能
     * @param context 上下文对象，由具体技能实现决定需要的类型
     */
    void cast(T context);
}
```

### BasicSkill实现

```java
package com.voidvvv.autochess.model.skill;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.Skill;

/**
 * 基础技能实现
 * MVP阶段：控制台打印技能释放信息
 */
public class BasicSkill implements Skill<BattleUnitBlackboard> {

    @Override
    public String getName() {
        return "基础技能";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        System.out.println("[" + blackboard.getSelf().getName() + "] 释放了技能: " + getName());
    }
}
```

### 技能工厂模式

```java
public class SkillFactory {
    public static Skill createSkill(SkillType type) {
        switch (type) {
            case BASIC: return new BasicSkill();
            // 未来扩展
            case HEAL: return new HealSkill();
            case AOE: return new AOESkill();
            default: return new BasicSkill(); // 默认
        }
    }
}
```

### 魔法条渲染规范

```java
public class ManaBarRenderer {
    private static final float BAR_HEIGHT = 6f;
    private static final float Y_OFFSET = 10f;  // 角色下方偏移

    public static void render(ShapeRenderer shapeRenderer, BattleCharacter character) {
        if (character.isDead()) return;

        float x = character.getX() - character.getSize() / 2;
        float y = character.getY() - character.getSize() - Y_OFFSET;
        // 渲染背景和填充
    }
}
```

## Future Considerations

**第一优先级**（规划时确定）：
- 不同角色类型的默认魔法值上限数值
- 魔法值恢复速率和攻击获得量的具体数值
- 基础技能的控制台输出格式

**未来扩展**（不在当前范围）：
- 具体技能效果的设计（治疗、伤害、增益等）
- 技能冷却时间
- 技能释放的视觉反馈（特效）
- 羁绊效果影响魔法值上限（manaRegenBonus集成）
- 多技能系统
- 技能目标选择和伤害计算

## Sources & References

### Origin

- **Brainstorm document:** [docs/brainstorms/2026-03-11-skill-system-brainstorm.md](docs/brainstorms/2026-03-11-skill-system-brainstorm.md)
- **Key decisions carried forward:**
  1. ManaComponent作为BattleUnitBlackboard内部类
  2. 技能采用策略模式（Skill接口）
  3. 技能只在NORMAL_STATE（空闲状态）释放
  4. 技能释放后魔法值清零
  5. maxMana通过CharacterStats/Card配置

### Internal References

- **BattleUnitBlackboard pattern:** `battle/BattleUnitBlackboard.java:22-41` - 构造函数和状态机初始化
- **State machine integration:** `battle/BattleUnitBlackboard.java:67-82` - handleMessage()消息处理
- **Update pattern:** `battle/BattleUnitBlackboard.java:171-175` - update()方法
- **Attack flow:** `battle/BattleUnitBlackboard.java:93-119` - onMessageDoAttack()
- **NormalState:** `sm/state/common/NormalState.java` - 空闲状态实现
- **CharacterStats:** `model/CharacterStats.java:7-38` - 属性配置
- **Rendering pattern:** `render/BattleCharacterRender.java:12-27` - 角色渲染

### Similar Features

- **MovementEffect system:** `model/MovementEffect.java`, `manage/MovementEffectManager.java` - 效果系统参考模式
- **Projectile system:** `model/Projectile.java`, `manage/ProjectileManager.java` - 投掷物系统参考
- **SynergyEffect:** `model/SynergyEffect.java` - 羁绊效果系统

### Configuration Files

- **character_stats.json** - 需要添加maxMana字段到角色配置中
