# 角色移动逻辑分析报告

## 概述

本文档分析当前项目中角色（BattleCharacter）的移动逻辑，包括数据模型、决策、计算与更新的完整流程。

---

## 1. 数据层（Model）

### MoveComponent

**文件**: `core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java`

纯数据容器，包含：
- `canWalk`：是否允许行走
- `speed`：移动速度（标量）
- `dir`：移动方向（Vector2）
- `otherVel`：其他速度叠加（如击退等）

每个 `BattleCharacter` 持有一个 `moveComponent` 实例。

---

## 2. 决策层

### 2.1 行为树（Behavior Tree）

**文件**: `core/src/main/java/com/voidvvv/autochess/battle/UnitBehaviorTreeFactory.java`

行为树结构：`Sequence(FindEnemyTask → MoveToEnemyTask → AttackTargetTask)`

**MoveToEnemyTask**（`battle/MoveToEnemyTask.java`）负责移动决策：
- 进入时：切换到 `MoveState`（开启 `canWalk`）
- 每帧执行：
  - 若 `dst > attackRange`：设置 `moveComponent.dir.set(targetX - x, targetY - y)`，返回 `RUNNING`
  - 若 `dst <= attackRange`：切换到 `NormalState`，返回 `SUCCEEDED`（停止移动）

### 2.2 状态机（State Machine）

**MoveState**（`sm/state/common/MoveState.java`）：
- `onEnter`：`moveComponent.canWalk = true`
- `onExit`：`moveComponent.canWalk = false`

行为树通过 `bb.stateMachine.switchState(States.BASE_MOVE_STATE)` 控制移动状态切换。

---

## 3. 计算层（Logic）

### MovementCalculator

**文件**: `core/src/main/java/com/voidvvv/autochess/logic/MovementCalculator.java`

职责：
- `calculateTotalMove(moveComponent)`：计算总移动向量 = `dir.nor().scl(speed)` + `otherVel`
- 当 `canWalk == false` 时返回零向量

---

## 4. 更新层（Updater）

### BattleCharacterUpdater

**文件**: `core/src/main/java/com/voidvvv/autochess/updater/BattleCharacterUpdater.java`

每帧对存活角色执行：
1. 调用 `MovementCalculator.calculateTotalMove(character.moveComponent)`
2. 用 `delta` 更新位置：`x += totalMove.x * delta`，`y += totalMove.y * delta`

---

## 5. 执行流程（GameScreen）

```
updateBattle(delta)
  ├── tree.step()                    // 行为树：FindEnemy → MoveToEnemy → AttackTarget
  ├── battleUpdater.update(delta)    // 伤害事件处理
  ├── bb.update(delta)               // 状态机更新
  ├── projectileManager.update(...)  // 投掷物更新
  └── postUpdateBattle(delta)
        └── battleCharacterUpdater.update(c, delta)  // 对所有存活角色应用位置更新
```

---

## 6. 放置阶段移动

玩家在放置阶段拖拽棋子时，由 `Battlefield.moveCharacter()` 处理：
- 直接更新 `initX` / `initY`
- 不经过行为树和状态机

---

## 7. 角色职责总结

| 模块 | 类 | 职责 |
|------|-----|------|
| Model | `MoveComponent` | 存储移动数据（方向、速度、是否可走） |
| Battle | `MoveToEnemyTask` | 决策是否移动、设置朝向 |
| SM | `MoveState` | 控制 `canWalk` 开关 |
| Logic | `MovementCalculator` | 计算移动向量 |
| Updater | `BattleCharacterUpdater` | 应用 delta 更新角色位置 |

---

## 8. 架构特点

- **决策与执行分离**：行为树决定何时移动、朝哪移动；状态机控制 `canWalk`；Updater 只负责按 delta 更新位置
- **符合 Model/Updater/Logic 分层**：Model 纯数据、Logic 纯计算、Updater 纯更新
- **Blackboard 聚合**：`BattleUnitBlackboard` 持有 self、target、stateMachine、battlefield，供行为树和状态机使用
