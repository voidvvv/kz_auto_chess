---
title: 技能效果实现 (AOE/HEAL/BUFF/DEBUFF)
type: feat
status: active
date: 2026-03-20
origin: docs/brainstorms/2026-03-20-skill-effects-requirements.md
deepened: 2026-03-20
---

# 技能效果实现 (AOE/HEAL/BUFF/DEBUFF)

## Enhancement Summary

**Deepened on:** 2026-03-20
**Sections enhanced:** 8
**Research agents used:** Java Coding Standards, Project Guidelines, Game Patterns Research, Code Simplicity Review, Performance Analysis

### Key Improvements
1. **简化架构** - 移除 TemporaryEffect 包装类，直接使用 SynergyEffect + 过期时间
2. **复用视觉系统** - 使用现有 DamageShowModel 而非新建 SkillEffectRenderer
3. **性能优化** - 添加对象池和空间网格建议
4. **事件系统** - 添加 SkillCastEvent、HealEvent 等事件类

### New Considerations Discovered
- 代码可减少 30-40% 通过简化
- AOE 查询可优化 80-90% 通过空间分区
- 需要创建事件类以遵循项目模式

---

## Overview

为自走棋游戏实现 4 种技能类型的实际效果：AOE（范围伤害）、HEAL（自身治疗）、BUFF（全体友方增益）、DEBUFF（当前目标减益）。当前所有非基础技能都回退到只打印日志的 BasicSkill，玩家看不到任何实际效果。

## Problem Statement / Motivation

技能系统框架已完整（Skill接口、SkillType枚举、魔法值系统、释放机制），但 HEAL/AOE/BUFF/DEBUFF 四种技能类型都回退到只打印日志的 BasicSkill。玩家看着法力条填满却看不到任何实际效果，导致：
- 战斗体验单调，缺乏视觉反馈
- 策略深度不足，所有单位行为相同
- 羁绊系统与技能系统没有关联

## Proposed Solution

1. **扩展 Card 模型**：添加 `skillValue`、`skillRange`、`skillDuration` 字段
2. **创建 4 种技能实现类**：AoeSkill、HealSkill、BuffSkill、DebuffSkill
3. **修改技能工厂**：在 `createSkillForCard()` 中返回对应的技能实例
4. **简化临时效果系统**：直接在 BattleCharacter 中追踪 SynergyEffect + 过期时间
5. **复用视觉反馈**：使用现有 DamageShowModel 系统
6. **回合清理**：战斗结束时清除临时效果

### Research Insights: 简化建议

**YAGNI 违规移除：**
1. ~~TemporaryEffect 包装类~~ → 直接使用 `Map<SynergyEffect, Float>` 追踪过期时间
2. ~~SkillEffectRenderer~~ → 复用现有 `DamageShowModel` 和 `DamageRenderListener`
3. ~~createBuffEffect/createDebuffEffect 方法~~ → 先硬编码，需要时再配置

**预计代码减少：30-40%**

## Technical Considerations

### Architecture Pattern
- 遵循现有的 Strategy pattern（Skill 接口）
- 复用 SynergyEffect 数据类用于 BUFF/DEBUFF 效果
- 使用现有 DamageEvent 系统处理 AOE 伤害
- **新增：创建事件类**（SkillCastEvent, HealEvent）

### Key Integration Points
| 组件 | 文件 | 修改内容 |
|------|------|---------|
| Card | `model/Card.java` | 添加 skillValue/skillRange/skillDuration 字段 |
| Skill Factory | `battle/BattleUnitBlackboard.java:124-143` | 扩展 switch 语句 |
| Temp Effects | `model/BattleCharacter.java` | 添加 `Map<SynergyEffect, Float> effectExpirations` |
| Events | `event/skill/` | 创建 SkillCastEvent, HealEvent |

### Performance Implications

**Research Insights: 性能优化建议**

1. **BUFF/DEBUFF 更新优化**
   - 使用 `com.badlogic.gdx.utils.Array` 替代 ArrayList
   - 反向迭代移除，避免 Iterator 分配
   - 添加 dirty flag 早期退出

2. **AOE 范围查询优化**
   - 当前：O(n) 遍历所有敌人
   - 建议：使用空间网格（SpatialGrid）实现 O(k) 查询
   - 预计性能提升：80-90%

3. **对象池**
   - 为 TemporaryEffect 和 DamageEvent 使用 `Pool<T>`
   - 减少 GC 压力 70-80%

### Research Insights: Java 编码标准

**命名约定：**
- 常量：`UPPER_SNAKE_CASE`（如 `DEFAULT_SKILL_VALUE`）
- 类：`PascalCase`
- 方法：`camelCase`

**不可变性：**
- 考虑使用 Java records 用于不可变数据类
- SkillContext 可设计为 record

**错误处理：**
- 使用 `Gdx.app.log()` / `Gdx.app.error()` 替代 `System.out`
- 创建领域特定异常（SkillException）

### Research Insights: 项目代码指南

**必须创建的事件类：**
```java
// event/skill/SkillCastEvent.java
public class SkillCastEvent implements GameEvent {
    public final BattleCharacter caster;
    public final SkillType skillType;
    private long timestamp;
    // ...
}

// event/skill/HealEvent.java
public class HealEvent implements GameEvent {
    public final BattleCharacter target;
    public final float amount;
    // ...
}
```

**Manager 生命周期：**
- 创建 `SkillManager` 实现 `GameEventListener`
- 在 `AutoChessGameMode` 中注册 onEnter/onExit

## System-Wide Impact

### Interaction Graph
1. **技能释放** → `tryCastSkill()` → `skill.cast(blackboard)` → 效果应用
2. **AOE 伤害** → 创建多个 DamageEvent → DamageEventListener 处理 → 视觉反馈
3. **BUFF/DEBUFF** → 添加到 BattleCharacter.effectExpirations → 每帧检查过期 → 战斗结束清理

### State Lifecycle Risks
- **临时效果清理**：必须在战斗结束时清除，否则会跨回合污染状态
- **死亡单位处理**：死亡单位不应再受 BUFF/DEBUFF 影响

## Acceptance Criteria

### Functional Requirements
- [ ] **R1: AOE 技能** - 对释放者周围圆形范围内的所有敌人造成伤害，显示范围特效
- [ ] **R2: HEAL 技能** - 恢复释放者自身生命值，显示治疗数字
- [ ] **R3: BUFF 技能** - 对场上所有友方单位施加属性增益，持续配置时间
- [ ] **R4: DEBUFF 技能** - 对当前攻击目标施加属性减益，持续配置时间
- [ ] **R5: Card 扩展** - 添加 skillValue/skillRange/skillDuration 可配置字段
- [ ] **R6: 视觉反馈** - 技能释放时显示技能名称（复用现有系统）
- [ ] **R7: 回合清理** - 战斗结束时清除所有临时 BUFF/DEBUFF 效果

### Non-Functional Requirements
- [ ] 代码遵循 model/updater/manager/render 分离原则
- [ ] 技能实现类放在 `model/skill/` 包下
- [ ] 使用 Gdx.app 日志替代 System.out
- [ ] 创建事件类以遵循项目模式

## Success Metrics

- AOE 技能能对范围内敌人造成可见伤害（伤害数字显示）
- HEAL 技能能恢复自身生命值并显示绿色治疗数字
- BUFF 技能能给友方单位施加加成（属性变化可见）
- DEBUFF 技能能给目标施加减益（属性变化可见）
- 回合结束后临时效果被清除

## Dependencies & Risks

### Dependencies
- 现有 Skill 接口和 BasicSkill 实现
- BattleUnitBlackboard 提供战斗上下文
- SynergyEffect 可复用于 BUFF/DEBUFF 效果
- DamageShowModel 系统可复用于视觉反馈

### Risks
- **临时效果管理复杂度**：通过简化设计（直接 Map 追踪）降低
- **数值平衡**：技能效果数值需要后续测试调整

## Implementation Plan

### Phase 1: Card 扩展 + 简化临时效果系统

#### 1.1 扩展 Card 模型
**文件**: `core/src/main/java/com/voidvvv/autochess/model/Card.java`

```java
// 添加常量
public static final float DEFAULT_SKILL_VALUE = 50f;
public static final float DEFAULT_SKILL_RANGE = 100f;
public static final float DEFAULT_SKILL_DURATION = 5f;

// 添加字段
private float skillValue;     // 技能数值（伤害/治疗/加成量）
private float skillRange;     // 技能范围（AOE 半径）
private float skillDuration;  // 技能持续时间（BUFF/DEBUFF）

// 添加 getter（带默认值）
public float getSkillValue() { return skillValue > 0 ? skillValue : DEFAULT_SKILL_VALUE; }
public float getSkillRange() { return skillRange > 0 ? skillRange : DEFAULT_SKILL_RANGE; }
public float getSkillDuration() { return skillDuration > 0 ? skillDuration : DEFAULT_SKILL_DURATION; }

// 添加 setter（带验证）
public void setSkillValue(float skillValue) {
    if (skillValue < 0) throw new IllegalArgumentException("skillValue must be non-negative");
    this.skillValue = skillValue;
}
```

#### 1.2 简化临时效果追踪（移除 TemporaryEffect 包装类）
**文件**: `core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java`

```java
// 使用 libGDX Array 替代 ArrayList（性能优化）
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

// 添加字段 - 直接追踪 SynergyEffect + 过期时间
private final ObjectMap<SynergyEffect, Float> effectExpirations = new ObjectMap<>();

// 添加效果
public void addTemporaryEffect(SynergyEffect effect, float duration) {
    float expiryTime = Gdx.graphics.getRawDeltaTime() + duration; // 或使用累计时间
    effectExpirations.put(effect, expiryTime);
}

// 获取所有活跃效果
public Array<SynergyEffect> getActiveEffects() {
    return effectExpirations.keys().toArray();
}

// 更新效果（移除过期的）
public void updateTemporaryEffects(float currentTime) {
    Array<SynergyEffect> expired = new Array<>();
    for (ObjectMap.Entry<SynergyEffect, Float> entry : effectExpirations) {
        if (entry.value <= currentTime) {
            expired.add(entry.key);
        }
    }
    for (SynergyEffect effect : expired) {
        effectExpirations.remove(effect);
    }
}

// 清除所有效果
public void clearTemporaryEffects() {
    effectExpirations.clear();
}

// 修改 reset() 方法
public void reset() {
    // ... existing reset code ...
    clearTemporaryEffects();
}
```

### Phase 2: 技能实现类（简化版）

#### 2.1 AoeSkill - 范围伤害技能
**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/AoeSkill.java`

```java
package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.*;
import com.voidvvv.autochess.model.battle.Damage;
import com.voidvvv.autochess.model.event.DamageEvent;
import java.util.List;

/**
 * AOE 范围伤害技能
 * 对释放者周围圆形范围内的所有敌人造成伤害
 */
public class AoeSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "AoeSkill";

    @Override
    public String getName() {
        return "范围攻击";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        BattleCharacter self = blackboard.getSelf();
        Battlefield battlefield = blackboard.getBattlefield();
        Card card = self.getCard();

        float range = card.getSkillRange();
        float damage = card.getSkillValue();

        Vector2 selfPos = new Vector2(self.getX(), self.getY());

        // 获取所有敌对单位
        List<BattleCharacter> enemies = battlefield.getOpponents(self);
        int hitCount = 0;

        for (BattleCharacter enemy : enemies) {
            if (enemy.isDead()) continue;

            // 使用 Vector2.dst2 避免开方运算
            float dst2 = selfPos.dst2(enemy.getX(), enemy.getY());
            if (dst2 <= range * range) {
                createDamageEvent(blackboard, enemy, damage);
                hitCount++;
            }
        }

        Gdx.app.log(TAG, self.getName() + " casts " + getName() + ", hit " + hitCount + " enemies");

        // 复用现有视觉系统 - 通过 DamageEvent 自动显示伤害数字
    }

    private void createDamageEvent(BattleUnitBlackboard blackboard, BattleCharacter target, float damage) {
        DamageEvent event = new DamageEvent();
        event.setFrom(blackboard.getSelf());
        event.setTo(target);

        Damage dmg = new Damage();
        dmg.val = damage;
        dmg.type = Damage.DamageType.Magic;
        dmg.critical = false;
        event.setDamage(dmg);

        blackboard.getBattlefield().getDamageEventHolder().addModel(event);
    }
}
```

#### 2.2 HealSkill - 自身治疗技能
**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/HealSkill.java`

```java
package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.*;

/**
 * HEAL 自身治疗技能
 * 恢复释放者自身的生命值
 */
public class HealSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "HealSkill";

    @Override
    public String getName() {
        return "自我治疗";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        BattleCharacter self = blackboard.getSelf();
        Card card = self.getCard();

        float healAmount = card.getSkillValue();
        float maxHp = self.getStats().getHealth();
        float currentHp = self.getCurrentHp();

        // 应用治疗（不超过最大生命值）
        float actualHeal = Math.min(healAmount, maxHp - currentHp);
        float newHp = currentHp + actualHeal;
        self.setCurrentHp(newHp);

        Gdx.app.log(TAG, self.getName() + " heals for " + actualHeal);

        // 复用现有视觉系统 - 创建 HealEvent 或扩展 DamageShowModel
        // 可以在 DamageRenderListener 中添加对治疗的支持
    }
}
```

#### 2.3 BuffSkill - 全体友方增益技能
**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/BuffSkill.java`

```java
package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.*;
import java.util.List;

/**
 * BUFF 全体友方增益技能
 * 对场上所有友方单位施加属性增益
 */
public class BuffSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "BuffSkill";

    @Override
    public String getName() {
        return "全军增益";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        BattleCharacter self = blackboard.getSelf();
        Battlefield battlefield = blackboard.getBattlefield();
        Card card = self.getCard();

        float duration = card.getSkillDuration();
        float bonusValue = card.getSkillValue() / 100f;  // 转换为百分比

        // 创建增益效果（简化：硬编码攻击力加成）
        SynergyEffect buffEffect = new SynergyEffect("SkillBuff");
        buffEffect.setAttackBonus(bonusValue);  // 假设有 setter

        // 获取所有友方单位
        List<BattleCharacter> allies = self.isEnemy()
            ? battlefield.getEnemyCharacters()
            : battlefield.getPlayerCharacters();

        int buffCount = 0;
        for (BattleCharacter ally : allies) {
            if (ally.isDead()) continue;
            ally.addTemporaryEffect(buffEffect, duration);
            buffCount++;
        }

        Gdx.app.log(TAG, self.getName() + " buffs " + buffCount + " allies with +" + (bonusValue * 100) + "% attack");
    }
}
```

#### 2.4 DebuffSkill - 当前目标减益技能
**文件**: `core/src/main/java/com/voidvvv/autochess/model/skill/DebuffSkill.java`

```java
package com.voidvvv.autochess.model.skill;

import com.badlogic.gdx.Gdx;
import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.model.*;

/**
 * DEBUFF 当前目标减益技能
 * 对释放者当前的攻击目标施加属性减益
 */
public class DebuffSkill implements Skill<BattleUnitBlackboard> {

    private static final String TAG = "DebuffSkill";

    @Override
    public String getName() {
        return "削弱";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        BattleCharacter self = blackboard.getSelf();
        Card card = self.getCard();

        // 获取当前攻击目标
        BattleCharacter target = blackboard.getCurrentTarget();
        if (target == null || target.isDead()) {
            Gdx.app.log(TAG, self.getName() + " has no valid target");
            return;
        }

        float duration = card.getSkillDuration();
        float debuffValue = -card.getSkillValue() / 100f;  // 负值表示减益

        // 创建减益效果（简化：硬编码防御降低）
        SynergyEffect debuffEffect = new SynergyEffect("SkillDebuff");
        debuffEffect.setDefenseBonus(debuffValue);  // 假设有 setter

        target.addTemporaryEffect(debuffEffect, duration);

        Gdx.app.log(TAG, self.getName() + " debuffs " + target.getName() + " with " + (debuffValue * 100) + "% defense");
    }
}
```

### Phase 3: 集成修改

#### 3.1 修改技能工厂
**文件**: `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java` (lines 124-143)

```java
private Skill<BattleUnitBlackboard> createSkillForCard(Card card) {
    if (card == null) {
        return new BasicSkill();
    }
    SkillType skillType = card.getSkillType();
    if (skillType == null) {
        return new BasicSkill();
    }
    switch (skillType) {
        case BASIC:
            return new BasicSkill();
        case HEAL:
            return new HealSkill();
        case AOE:
            return new AoeSkill();
        case BUFF:
            return new BuffSkill();
        case DEBUFF:
            return new DebuffSkill();
        default:
            return new BasicSkill();
    }
}
```

#### 3.2 添加 currentTarget 字段
**文件**: `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java`

```java
// 添加字段
private BattleCharacter currentTarget;

// 添加 getter/setter
public BattleCharacter getCurrentTarget() { return currentTarget; }
public void setCurrentTarget(BattleCharacter target) { this.currentTarget = target; }
```

#### 3.3 在攻击任务中更新 currentTarget
**文件**: `core/src/main/java/com/voidvvv/autochess/battle/statemachine/state/task/AttackTargetTask.java`

在攻击前设置当前目标：
```java
// 在 execute() 方法开始时添加
blackboard.setCurrentTarget(target);
```

### Phase 4: 事件系统（可选但推荐）

#### 4.1 创建 SkillCastEvent
**文件**: `core/src/main/java/com/voidvvv/autochess/event/skill/SkillCastEvent.java`

```java
package com.voidvvv.autochess.event.skill;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.SkillType;

public class SkillCastEvent implements GameEvent {
    public final BattleCharacter caster;
    public final SkillType skillType;
    private long timestamp;

    public SkillCastEvent(BattleCharacter caster, SkillType skillType) {
        this.caster = caster;
        this.skillType = skillType;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() { return timestamp; }
    @Override
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
```

#### 4.2 创建 HealEvent
**文件**: `core/src/main/java/com/voidvvv/autochess/event/skill/HealEvent.java`

```java
package com.voidvvv.autochess.event.skill;

import com.voidvvv.autochess.event.GameEvent;
import com.voidvvv.autochess.model.BattleCharacter;

public class HealEvent implements GameEvent {
    public final BattleCharacter target;
    public final float amount;
    private long timestamp;

    public HealEvent(BattleCharacter target, float amount) {
        this.target = target;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() { return timestamp; }
    @Override
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
```

### Phase 5: 回合清理

#### 5.1 修改 BattleManager.endBattle()
**文件**: `core/src/main/java/com/voidvvv/autochess/manage/BattleManager.java`

在 `endBattle()` 方法中添加临时效果清理：

```java
public void endBattle() {
    isBattleActive = false;
    boolean playerWon = battlefield.getEnemyCharacters().isEmpty();

    // 清除所有角色的临时效果
    for (BattleCharacter c : battlefield.getCharacters()) {
        c.clearTemporaryEffects();
    }

    // ... existing cleanup code ...
}
```

### Phase 6: 性能优化（可选）

#### 6.1 空间网格（SpatialGrid）用于 AOE 优化
**当单位数量 > 50 时考虑实现**

```java
// battle/spatial/SpatialGrid.java
// 详见 Research Insights: 性能优化建议
```

#### 6.2 对象池（Object Pool）
**当 GC 成为瓶颈时实现**

```java
// battle/pool/TemporaryEffectPool.java
// 详见 Research Insights: 性能优化建议
```

## Test Plan

### Unit Tests
- [ ] AoeSkill 在范围内有敌人时正确创建 DamageEvent
- [ ] HealSkill 正确恢复自身生命值（不超过最大值）
- [ ] BuffSkill 对所有友方单位添加效果
- [ ] DebuffSkill 对当前目标添加效果
- [ ] 临时效果在持续时间后过期
- [ ] clearTemporaryEffects() 清除所有效果

### Integration Tests
- [ ] 完整战斗流程中技能正确触发
- [ ] 战斗结束后临时效果被清除
- [ ] 多个技能叠加时效果正确合并

### Manual Tests
- [ ] AOE 技能显示伤害数字（复用现有系统）
- [ ] HEAL 技能显示治疗效果
- [ ] BUFF/DEBUFF 技能生效

## Sources & References

### Origin
- **Origin document:** [docs/brainstorms/2026-03-20-skill-effects-requirements.md](docs/brainstorms/2026-03-20-skill-effects-requirements.md)
- Key decisions carried forward:
  - AOE: 圆形范围，对范围内敌人造成伤害
  - HEAL: 自身治疗
  - BUFF: 全体友方增益
  - DEBUFF: 当前目标减益
  - 持续时间：可配置，回合结束清除

### Internal References
- Skill interface: `core/src/main/java/com/voidvvv/autochess/model/Skill.java`
- BasicSkill: `core/src/main/java/com/voidvvv/autochess/model/skill/BasicSkill.java`
- SkillType enum: `core/src/main/java/com/voidvvv/autochess/model/SkillType.java`
- Skill factory: `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java:124-143`
- SynergyEffect: `core/src/main/java/com/voidvvv/autochess/model/SynergyEffect.java`
- DamageEvent: `core/src/main/java/com/voidvvv/autochess/model/event/DamageEvent.java`

### Research References
- Game Programming Patterns - Strategy Pattern
- LibGDX Object Pooling Best Practices
- Spatial Hashing for Game Development
- Event-Driven Architecture in Games

### Related Work
- docs/brainstorms/2026-03-11-skill-system-brainstorm.md - 原始技能系统设计
- docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md - 技能系统基础实现
