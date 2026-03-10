---
title: 移动效果系统改造
type: feat
status: completed
date: 2026-03-10
origin: docs/brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md
# Note: brainstorm 文件位于项目根目录 docs/brainstorms/
---

# 移动效果系统改造

## Overview

对现有移动模块进行改造，使其支持外部移动效果的施加和管理。角色可以自主选择移动或静止，但外部（其他角色、技能、地形等）可以对其施加拖拽、禁锢、固定速度移动等效果，并且多种效果可以叠加。

## Problem Statement / Motivation

当前移动系统的局限性：

1. **外部效果单一**: `MoveComponent.otherVel` 只能存储一个向量，无法区分多个效果来源
2. **缺乏效果管理**: 没有效果的添加、移除、持续时间、衰减等机制
3. **无优先级系统**: 多个效果冲突时无法决定最终行为
4. **otherVel 未使用**: 预留字段从未被实际使用

需要一套完整的外部移动效果系统来支持：
- 击退/拉拽效果（如爆炸、钩子技能）
- 禁锢效果（如眩晕、定身）
- 强制移动效果（如传送带、冲刺技能）
- 效果叠加与冲突解决

## Proposed Solution

采用**分层效果管理模式**，完全遵循项目现有的 model/updater/manager/render 分离原则，与 `SynergyEffect` 系统保持一致：

```
[外部来源] --> [MovementEffect] --> [MoveComponent.effects] --> [MovementEffectManager] --> [MovementCalculator] --> [位置更新]
                                        (数据存储)              (生命周期管理)            (计算逻辑)
```

### 架构设计原则

1. **数据存储在 Model**: 效果列表存储在 `MoveComponent` 中
2. **管理逻辑在 Manager**: `MovementEffectManager` 只负责生命周期管理
3. **计算逻辑在 Calculator**: `MovementCalculator` 负责所有向量计算
4. **与 SynergyEffect 模式一致**: 避免引入新的架构模式

### 效果类型优先级

```
优先级从高到低:
IMMOBILIZE (禁锢) > FIXED_VELOCITY (强制速度) > DRAG (拖拽) > SPEED_MODIFIER (速度修正)

效果叠加规则:
- 同类型 DRAG 效果: 向量叠加
- 同类型 IMMOBILIZE 效果: 取最长持续时间
- 同类型 FIXED_VELOCITY 效果: 取最高优先级
- 同类型 SPEED_MODIFIER 效果: 乘法叠加
```

## Technical Approach

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           BattleCharacter                                │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────┐                            │
│  │           MoveComponent (Model)          │                           │
│  │  - canWalk: boolean                      │                           │
│  │  - speed: float                          │                           │
│  │  - dir: Vector2                          │                           │
│  │  - movementEffects: Array<Effect> ───────┼──┐                        │
│  └─────────────────────────────────────────┘  │                        │
└───────────────────────────────────────────────┼────────────────────────┘
                                                │
                                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│         MovementEffectManager (Manager - 生命周期管理)                    │
│  + addEffect(component, effect)      添加效果                            │
│  + removeEffect(component, id)       移除效果                            │
│  + updateEffects(component, delta)   更新效果生命周期                     │
│  + clearEffects(component)           清除所有效果                         │
└─────────────────────────────────────────────────────────────────────────┘
                                                │
                                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│         MovementCalculator (Logic - 计算逻辑)                            │
│  + calculateTotalMove(component)           计算总移动向量                 │
│  + calculateTotalDragVelocity(component)   计算拖拽速度叠加               │
│  + calculateSpeedModifier(component)       计算速度修正系数               │
│  + isImmobilized(component)                检查是否被禁锢                 │
│  + getActiveFixedVelocityEffect(component) 获取强制速度效果               │
└─────────────────────────────────────────────────────────────────────────┘
                                                │
                                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│         BattleCharacterUpdater (Updater)                                │
│  1. effectManager.updateEffects(component, delta)  更新效果              │
│  2. movementCalculator.calculateTotalMove(component) 计算移动            │
│  3. 应用位置更新                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 与 SynergyEffect 对比

| 层级 | SynergyEffect | MovementEffect |
|------|---------------|----------------|
| **数据存储** | `BattleCharacter.activeSynergyEffects: Map` | `MoveComponent.movementEffects: Array` |
| **数据类** | `SynergyEffect` | `MovementEffect` |
| **管理器** | `SynergyManager` | `MovementEffectManager` |
| **计算** | 在 SynergyManager 中 | 在 `MovementCalculator` 中 |

---

### Implementation Phases

#### Phase 1: 数据模型定义

**目标**: 创建效果系统的核心数据结构

**Tasks**:
- [x] 创建 `MovementEffectType` 枚举
- [x] 创建 `MovementEffect` 数据类（包含 reset 方法）
- [x] 扩展 `MoveComponent` 添加效果列表
- [x] 在 `MoveComponent` 中添加 `com.badlogic.gdx.utils.Array` 导入

**关键文件**:
```
core/src/main/java/com/voidvvv/autochess/model/
├── MovementEffectType.java      # 新增
├── MovementEffect.java          # 新增
└── MoveComponent.java           # 修改
```

**MovementEffectType 枚举**:
```java
// core/src/main/java/com/voidvvv/autochess/model/MovementEffectType.java
package com.voidvvv.autochess.model;

/**
 * 移动效果类型枚举
 */
public enum MovementEffectType {
    /**
     * 拖拽效果 - 施加外部速度（击退、拉拽）
     * 叠加规则: 向量叠加
     */
    DRAG(1),

    /**
     * 禁锢效果 - 阻止自身移动（眩晕、定身）
     * 叠加规则: 取最长持续时间
     */
    IMMOBILIZE(4),

    /**
     * 强制速度 - 覆盖移动速度（冲刺、传送带）
     * 叠加规则: 取最高优先级
     */
    FIXED_VELOCITY(3),

    /**
     * 速度修正 - 百分比调整速度（加速、减速）
     * 叠加规则: 乘法叠加
     */
    SPEED_MODIFIER(2);

    private final int priority;

    MovementEffectType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
```

**MovementEffect 数据类**:
```java
// core/src/main/java/com/voidvvv/autochess/model/MovementEffect.java
package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;

/**
 * 移动效果数据类（纯数据容器）
 */
public class MovementEffect {
    // 唯一标识
    private String effectId;

    // 效果来源（用于按来源清除）
    private String sourceId;

    // 效果类型
    private MovementEffectType type;

    // 速度向量（用于 DRAG/FIXED_VELOCITY）
    private Vector2 velocity = new Vector2();

    // 速度修正系数（用于 SPEED_MODIFIER，1.0=无变化，0.5=减速50%）
    private float speedModifier = 1.0f;

    // 持续时间（秒，-1表示永久，0表示瞬发）
    private float duration = 0f;
    private float remainingDuration = 0f;

    // 效果优先级（同类型内的优先级）
    private int priority = 0;

    // 衰减系数（每秒衰减比例，0=不衰减）
    private float decay = 0f;

    // Getters and Setters
    public String getEffectId() { return effectId; }
    public void setEffectId(String effectId) { this.effectId = effectId; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public MovementEffectType getType() { return type; }
    public void setType(MovementEffectType type) { this.type = type; }

    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(Vector2 velocity) { this.velocity.set(velocity); }

    public float getSpeedModifier() { return speedModifier; }
    public void setSpeedModifier(float speedModifier) { this.speedModifier = speedModifier; }

    public float getDuration() { return duration; }
    public void setDuration(float duration) {
        this.duration = duration;
        this.remainingDuration = duration;
    }

    public float getRemainingDuration() { return remainingDuration; }
    public void setRemainingDuration(float remainingDuration) {
        this.remainingDuration = remainingDuration;
    }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public float getDecay() { return decay; }
    public void setDecay(float decay) { this.decay = decay; }

    public boolean isExpired() {
        return duration > 0 && remainingDuration <= 0;
    }

    public boolean isPermanent() {
        return duration < 0;
    }

    /**
     * 重置效果状态（用于对象池重用）
     */
    public void reset() {
        this.effectId = null;
        this.sourceId = null;
        this.type = null;
        this.velocity.set(0, 0);
        this.speedModifier = 1.0f;
        this.duration = 0f;
        this.remainingDuration = 0f;
        this.priority = 0;
        this.decay = 0f;
    }
}
```

**MoveComponent 扩展**:
```java
// core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java
package com.voidvvv.autochess.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MoveComponent {
    // 自身移动（现有）
    public boolean canWalk = false;
    public float speed;
    public Vector2 dir = new Vector2();

    // 外部速度（现有，将被废弃）
    @Deprecated
    public Vector2 otherVel = new Vector2();

    // 新增: 效果列表（纯数据容器）
    public Array<MovementEffect> movementEffects = new Array<>();
}
```

**Success Criteria**:
- 编译通过，无警告
- 枚举值和优先级定义清晰
- MovementEffect 包含所有必要字段
- MoveComponent 添加效果列表

---

#### Phase 2: 效果管理器实现

**目标**: 实现效果的生命周期管理（只负责管理，不负责计算）

**Tasks**:
- [x] 创建 `MovementEffectManager` 类
- [x] 实现效果添加、移除、更新方法
- [x] 实现按来源清除方法

**关键文件**:
```
core/src/main/java/com/voidvvv/autochess/manage/
└── MovementEffectManager.java    # 新增
```

**MovementEffectManager 实现**:
```java
// core/src/main/java/com/voidvvv/autochess/manage/MovementEffectManager.java
package com.voidvvv.autochess.manage;

import com.badlogic.gdx.utils.Array;
import com.voidvvv.autochess.model.MoveComponent;
import com.voidvvv.autochess.model.MovementEffect;
import com.voidvvv.autochess.model.MovementEffectType;

/**
 * 移动效果管理器 - 只负责效果的生命周期管理
 * 计算逻辑在 MovementCalculator 中
 */
public class MovementEffectManager {

    /**
     * 添加效果到组件
     */
    public void addEffect(MoveComponent component, MovementEffect effect) {
        if (effect == null || component == null) return;

        // 检查是否需要替换同ID效果
        removeEffect(component, effect.getEffectId());

        // 根据类型处理叠加
        switch (effect.getType()) {
            case IMMOBILIZE:
                // 禁锢效果：如果已存在禁锢，取较长持续时间
                MovementEffect existing = findEffectByType(component, MovementEffectType.IMMOBILIZE);
                if (existing != null && existing.getRemainingDuration() < effect.getDuration()) {
                    removeEffect(component, existing.getEffectId());
                    component.movementEffects.add(effect);
                } else if (existing == null) {
                    component.movementEffects.add(effect);
                }
                break;

            case FIXED_VELOCITY:
                // 强制速度：移除低优先级同类型效果
                removeLowerPriorityEffects(component, MovementEffectType.FIXED_VELOCITY, effect.getPriority());
                component.movementEffects.add(effect);
                break;

            default:
                // DRAG 和 SPEED_MODIFIER：直接叠加
                component.movementEffects.add(effect);
                break;
        }
    }

    /**
     * 移除效果
     */
    public void removeEffect(MoveComponent component, String effectId) {
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            if (effects.get(i).getEffectId().equals(effectId)) {
                effects.removeIndex(i);
                break;
            }
        }
    }

    /**
     * 按来源移除所有效果
     */
    public void removeEffectsBySource(MoveComponent component, String sourceId) {
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            if (effects.get(i).getSourceId().equals(sourceId)) {
                effects.removeIndex(i);
            }
        }
    }

    /**
     * 更新所有效果（每帧调用）
     */
    public void updateEffects(MoveComponent component, float delta) {
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            MovementEffect effect = effects.get(i);

            // 更新持续时间
            if (!effect.isPermanent()) {
                effect.setRemainingDuration(effect.getRemainingDuration() - delta);
            }

            // 应用衰减
            if (effect.getDecay() > 0) {
                float decayFactor = 1.0f - effect.getDecay() * delta;
                effect.getVelocity().scl(decayFactor);
            }

            // 移除过期效果
            if (effect.isExpired()) {
                effects.removeIndex(i);
            }
        }
    }

    /**
     * 清除所有效果
     */
    public void clearEffects(MoveComponent component) {
        component.movementEffects.clear();
    }

    // 私有辅助方法
    private MovementEffect findEffectByType(MoveComponent component, MovementEffectType type) {
        for (MovementEffect effect : component.movementEffects) {
            if (effect.getType() == type && !effect.isExpired()) {
                return effect;
            }
        }
        return null;
    }

    private void removeLowerPriorityEffects(MoveComponent component, MovementEffectType type, int minPriority) {
        Array<MovementEffect> effects = component.movementEffects;
        for (int i = effects.size - 1; i >= 0; i--) {
            MovementEffect effect = effects.get(i);
            if (effect.getType() == type && effect.getPriority() < minPriority) {
                effects.removeIndex(i);
            }
        }
    }
}
```

**Success Criteria**:
- 效果可以正确添加和移除
- 叠加规则正确执行
- 更新逻辑正确处理衰减和过期
- Manager 不包含计算逻辑

---

#### Phase 3: MovementCalculator 增强

**目标**: 添加效果相关的计算方法

**Tasks**:
- [x] 修改 `MovementCalculator.calculateTotalMove()` 方法
- [x] 添加效果查询和计算方法
- [x] 添加速度上限处理

**关键文件**:
```
core/src/main/java/com/voidvvv/autochess/logic/
└── MovementCalculator.java        # 修改
```

**MovementCalculator 增强**:
```java
// core/src/main/java/com/voidvvv/autochess/logic/MovementCalculator.java
package com.voidvvv.autochess.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.voidvvv.autochess.model.MoveComponent;
import com.voidvvv.autochess.model.MovementEffect;
import com.voidvvv.autochess.model.MovementEffectType;

/**
 * 移动计算器 - 包含所有移动相关的计算逻辑
 */
public class MovementCalculator {
    private final Vector2 tmp = new Vector2();

    // 速度上限（防止无限加速）
    private static final float MAX_VELOCITY = 500f;

    /**
     * 计算总移动向量
     */
    public Vector2 calculateTotalMove(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return new Vector2(0, 0);
        }

        tmp.set(0, 0);

        // 1. 检查禁锢效果
        if (isImmobilized(moveComponent)) {
            // 禁锢时只计算外部拖拽速度，不计算自身移动
            tmp.add(calculateTotalDragVelocity(moveComponent));
        } else {
            // 2. 检查强制速度效果
            MovementEffect fixedVel = getActiveFixedVelocityEffect(moveComponent);

            if (fixedVel != null) {
                // 强制速度覆盖自身移动
                tmp.set(fixedVel.getVelocity());
            } else {
                // 3. 计算自身移动（应用速度修正）
                if (moveComponent.canWalk) {
                    float modifiedSpeed = moveComponent.speed * calculateSpeedModifier(moveComponent);
                    tmp.set(moveComponent.dir).nor().scl(modifiedSpeed);
                }

                // 4. 叠加外部拖拽速度
                tmp.add(calculateTotalDragVelocity(moveComponent));
            }
        }

        // 5. 应用速度上限
        if (tmp.len() > MAX_VELOCITY) {
            tmp.nor().scl(MAX_VELOCITY);
        }

        return tmp;
    }

    /**
     * 检查是否被禁锢
     */
    public boolean isImmobilized(MoveComponent moveComponent) {
        if (moveComponent == null) return false;
        Array<MovementEffect> effects = moveComponent.movementEffects;
        for (MovementEffect effect : effects) {
            if (effect.getType() == MovementEffectType.IMMOBILIZE && !effect.isExpired()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取强制速度效果（如果有）
     */
    public MovementEffect getActiveFixedVelocityEffect(MoveComponent moveComponent) {
        if (moveComponent == null) return null;
        MovementEffect highest = null;
        for (MovementEffect effect : moveComponent.movementEffects) {
            if (effect.getType() == MovementEffectType.FIXED_VELOCITY && !effect.isExpired()) {
                if (highest == null || effect.getPriority() > highest.getPriority()) {
                    highest = effect;
                }
            }
        }
        return highest;
    }

    /**
     * 计算总拖拽速度（DRAG效果叠加）
     */
    public Vector2 calculateTotalDragVelocity(MoveComponent moveComponent) {
        Vector2 total = new Vector2(0, 0);
        if (moveComponent == null) return total;

        for (MovementEffect effect : moveComponent.movementEffects) {
            if (effect.getType() == MovementEffectType.DRAG && !effect.isExpired()) {
                total.add(effect.getVelocity());
            }
        }
        return total;
    }

    /**
     * 计算速度修正系数（乘法叠加）
     */
    public float calculateSpeedModifier(MoveComponent moveComponent) {
        if (moveComponent == null) return 1.0f;

        float modifier = 1.0f;
        for (MovementEffect effect : moveComponent.movementEffects) {
            if (effect.getType() == MovementEffectType.SPEED_MODIFIER && !effect.isExpired()) {
                modifier *= effect.getSpeedModifier();
            }
        }
        return modifier;
    }

    // 保留原有方法以兼容现有代码
    public boolean isMoving(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return false;
        }
        return moveComponent.canWalk && moveComponent.speed > 0;
    }

    public float getMoveSpeed(MoveComponent moveComponent) {
        if (moveComponent == null || !moveComponent.canWalk) {
            return 0;
        }
        return moveComponent.speed;
    }
}
```

**Success Criteria**:
- 禁锢效果正确阻止自身移动
- 强制速度效果正确覆盖
- 速度修正正确应用
- 拖拽效果正确叠加
- Calculator 保持无状态

---

#### Phase 4: 状态机集成

**目标**: 将效果系统与状态机整合

**Tasks**:
- [x] 修改 `MoveState` 检查禁锢状态
- [x] 修改 `MoveToEnemyTask` 检查移动能力
- [x] 在 `States.java` 中添加必要的引用

**关键文件**:
```
core/src/main/java/com/voidvvv/autochess/sm/state/common/
└── MoveState.java              # 修改

core/src/main/java/com/voidvvv/autochess/battle/
└── MoveToEnemyTask.java        # 修改
```

**MoveState 修改**:
```java
// core/src/main/java/com/voidvvv/autochess/sm/state/common/MoveState.java
package com.voidvvv.autochess.sm.state.common;

import com.voidvvv.autochess.battle.BattleUnitBlackboard;
import com.voidvvv.autochess.logic.MovementCalculator;

public class MoveState extends AbstractState {
    public static final MoveState INSTANCE = new MoveState();

    // 使用 Calculator 检查禁锢（而不是直接访问效果列表）
    private final MovementCalculator movementCalculator = new MovementCalculator();

    private MoveState() {}

    @Override
    public String name() {
        return "move";
    }

    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        // 检查是否被禁锢
        if (movementCalculator.isImmobilized(entity.getSelf().moveComponent)) {
            // 被禁锢时不启用移动
            return;
        }
        entity.getSelf().moveComponent.canWalk = true;
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        entity.getSelf().moveComponent.canWalk = false;
    }
}
```

**MoveToEnemyTask 修改**:
```java
// core/src/main/java/com/voidvvv/autochess/battle/MoveToEnemyTask.java
package com.voidvvv.autochess.battle;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.sm.state.common.States;
import com.voidvvv.autochess.logic.MovementCalculator;  // 新增导入

public class MoveToEnemyTask extends LeafTask<BattleUnitBlackboard> {
    boolean firstFrame = false;
    private final MovementCalculator movementCalculator = new MovementCalculator();  // 新增

    @Override
    public void start() {
        BattleUnitBlackboard bb = getObject();
        bb.stateMachine.switchState(States.BASE_MOVE_STATE);
        firstFrame = true;
    }

    @Override
    public Status execute() {
        if (firstFrame) {
            firstFrame = false;
            return Status.RUNNING;
        }

        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();

        if (target == null || target.isDead() || self == null || self.isDead()) {
            return Status.FAILED;
        }

        // 新增：检查是否被禁锢
        if (movementCalculator.isImmobilized(self.moveComponent)) {
            return Status.FAILED;
        }

        if (!bb.stateMachine.getCurrent().isState(States.BASE_MOVE_STATE)) {
            return Status.FAILED;
        }

        // 后续逻辑保持不变...
        float targetX = target.getX();
        float targetY = target.getY();
        float x = self.getX();
        float y = self.getY();
        float attackRange = self.getAttackRange();
        float dst = Vector2.dst(x, y, targetX, targetY);

        if (dst > attackRange) {
            self.moveComponent.dir.set(targetX - x, targetY - y);
            return Status.RUNNING;
        } else {
            bb.stateMachine.switchState(States.NORMAL_STATE);
            return Status.SUCCEEDED;
        }
    }

    @Override
    protected Task<BattleUnitBlackboard> copyTo(Task<BattleUnitBlackboard> task) {
        return task;
    }
}
```

**Success Criteria**:
- 禁锢状态下无法进入移动状态
- 行为树正确响应禁锢

---

#### Phase 5: 初始化与集成

**目标**: 在角色更新流程中集成效果系统

**Tasks**:
- [x] 修改 `BattleCharacter` 初始化效果列表
- [x] 修改 `BattleCharacterUpdater` 调用效果管理器
- [x] 添加测试用效果方法

**关键文件**:
```
core/src/main/java/com/voidvvv/autochess/model/
└── BattleCharacter.java        # 修改

core/src/main/java/com/voidvvv/autochess/updater/
└── BattleCharacterUpdater.java # 修改
```

**BattleCharacter 修改**:
```java
// core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java

public void reset() {
    battleStats = null;
    this.x = initX;
    this.y = initY;
    this.currentHp = stats != null ? stats.getHealth() : 100f;
    this.nextAttackTime = 0;
    moveComponent.speed = 10f;
    moveComponent.canWalk = true;
    setTarget(null);

    // 新增：清除移动效果列表
    moveComponent.movementEffects.clear();

    this.time = 0f;
    this.currentTime = 0f;
    this.lastStateTime = 0f;
    this.currentAttackProgress = 0f;
    if (stats != null && card != null) {
        inferAttackRange();
    }
}
```

**BattleCharacterUpdater 修改**:
```java
// core/src/main/java/com/voidvvv/autochess/updater/BattleCharacterUpdater.java
package com.voidvvv.autochess.updater;

import com.voidvvv.autochess.logic.MovementCalculator;
import com.voidvvv.autochess.manage.MovementEffectManager;
import com.voidvvv.autochess.model.BattleCharacter;

public class BattleCharacterUpdater {

    private final MovementCalculator movementCalculator = new MovementCalculator();
    private final MovementEffectManager effectManager = new MovementEffectManager();

    public void update(BattleCharacter character, float delta) {
        // 1. 更新效果生命周期
        effectManager.updateEffects(character.moveComponent, delta);

        // 2. 计算总移动向量
        var totalMoveVal = movementCalculator.calculateTotalMove(character.moveComponent);

        // 3. 应用到位置
        character.setX(character.getX() + totalMoveVal.x * delta);
        character.setY(character.getY() + totalMoveVal.y * delta);
    }

    /**
     * 获取效果管理器（供外部使用）
     */
    public MovementEffectManager getEffectManager() {
        return effectManager;
    }
}
```

**Success Criteria**:
- 效果每帧正确更新
- 位置更新正确应用效果
- 架构分层清晰

**单元测试文件**:
```
core/src/test/java/com/voidvvv/autochess/
├── model/MovementEffectTest.java          # 新增
├── manage/MovementEffectManagerTest.java  # 新增
└── logic/MovementCalculatorTest.java      # 修改（添加效果相关测试）
```

---

## System-Wide Impact

### Interaction Graph

```
[技能系统] ──施加效果──> [MovementEffectManager.addEffect()]
                                    │
[地形系统] ──施加效果──>            │
                                    ▼
[物品系统] ──施加效果──>    [MoveComponent.movementEffects]
                                    │
                                    ▼
              [BattleCharacterUpdater.update()]
                           │         │
                           │         ▼
                           │  [effectManager.updateEffects()]
                           │         │
                           ▼         │
              [movementCalculator.calculateTotalMove()]
                           │
                           ▼
                   [BattleCharacter.position]
```

### Error Propagation

- **无效效果ID**: 移除时静默忽略
- **空效果/组件**: 添加时检查并跳过
- **负持续时间**: 视为永久效果
- **速度溢出**: 通过 MAX_VELOCITY 限制

### State Lifecycle Risks

- **效果未清除**: 在角色死亡/重置时需要清空效果列表
- **更新顺序**: 效果更新必须在位置计算之前
- **状态一致性**: 禁锢检查在 Calculator 和 State 中都有

### API Surface Parity

需要保持一致性的接口:
- `MoveComponent.movementEffects` - 新增（纯数据）
- `MovementCalculator` - 新增多个计算方法
- `MovementEffectManager` - 新增（只做生命周期管理）
- `BattleCharacterUpdater.update()` - 修改调用顺序

### Integration Test Scenarios

1. **单一击退效果**: 角色被击退后正确移动
2. **多个击退叠加**: 同向击退叠加，反向击退抵消
3. **禁锢+击退**: 禁锢时自身不能动，但仍被击退
4. **强制速度覆盖**: 冲刺时忽略自身移动方向
5. **效果过期**: 持续时间结束后效果自动移除
6. **效果衰减**: 击退速度随时间递减
7. **速度修正叠加**: 多个减速效果乘法叠加

---

## Acceptance Criteria

### Functional Requirements

- [x] 支持四种效果类型：DRAG、IMMOBILIZE、FIXED_VELOCITY、SPEED_MODIFIER
- [x] 效果可以正确添加和移除
- [x] 多个同类型效果按照规则叠加
- [x] 不同类型效果按照优先级处理
- [x] 效果支持持续时间和衰减
- [x] 禁锢效果正确阻止自身移动
- [x] 强制速度效果正确覆盖移动
- [x] 速度修正效果正确调整速度

### Non-Functional Requirements

- [x] 效果计算性能开销 < 0.1ms/帧
- [x] 支持至少 50 个同时存在的效果
- [x] 代码遵循项目 model/updater/manager/render 分离原则
- [x] 架构与 SynergyEffect 系统保持一致

### Quality Gates

- [ ] 所有新增类有单元测试
- [ ] 边缘情况测试覆盖（空值、负值、零值）
- [x] 与现有代码无冲突
- [x] 无内存泄漏（效果正确清除）

---

## Dependencies & Risks

### Dependencies

- 现有 `MoveComponent` 结构
- 现有 `MovementCalculator` 计算逻辑
- 现有状态机系统
- libgdx Vector2 和 Array 类

### Risks

| 风险 | 影响 | 缓解措施 |
|-----|------|---------|
| 效果叠加过多导致性能问题 | 中 | 设置最大效果数量限制 |
| 与现有 otherVel 字段冲突 | 低 | 标记 @Deprecated，逐步迁移 |
| 状态机与效果系统不同步 | 中 | 在 Calculator 中统一检查 |
| 效果ID冲突 | 低 | 使用 UUID 或来源+类型组合 |

---

## Future Considerations

### 可扩展性

1. **免疫机制**: 添加效果免疫类型检查
2. **效果净化**: 添加按类型清除效果的方法
3. **效果反射**: 添加效果被施加时的回调
4. **视觉效果**: 为不同效果类型添加视觉反馈
5. **网络同步**: 效果状态序列化

### 与其他系统的集成

1. **SynergyEffect 集成**: 羁绊效果可以添加 SPEED_MODIFIER
2. **技能系统**: 技能可以施加各种移动效果
3. **地形系统**: 地形可以施加持续效果（如减速区）
4. **道具系统**: 道具可以提供效果免疫

---

## Sources & References

### Origin

- **Brainstorm document**: [docs/brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md](docs/brainstorms/2026-03-10-state-machine-enhancement-brainstorm.md)
  - 状态机优先级机制可用于效果优先级参考
  - 异常状态链管理可用于禁锢效果链

### Internal References

- 移动组件: `core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java`
- 移动计算: `core/src/main/java/com/voidvvv/autochess/logic/MovementCalculator.java`
- 角色更新: `core/src/main/java/com/voidvvv/autochess/updater/BattleCharacterUpdater.java`
- 状态机: `core/src/main/java/com/voidvvv/autochess/sm/machine/BaseStateMachine.java`
- 羁绊效果参考: `core/src/main/java/com/voidvvv/autochess/model/SynergyEffect.java`
- 羁绊管理参考: `core/src/main/java/com/voidvvv/autochess/logic/SynergyManager.java`

### Design Patterns

- **组合模式**: MoveComponent 组合多个 MovementEffect
- **分层模式**: 数据存储、生命周期管理、计算逻辑分离
- **与 SynergyEffect 一致**: 遵循现有架构模式
