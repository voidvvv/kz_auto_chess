# 角色移动逻辑分析

## 架构概览

项目中角色的移动逻辑采用 **AI行为树 + 状态机** 的双重架构，遵循项目的分层原则：`Model → Updater → Manager → Render`。

---

## 1. 行为树决策层

### 行为树结构

```
UnitBehaviorTreeFactory 创建行为树:
├── FindEnemyTask    (寻找敌人)
├── MoveToEnemyTask  (向敌人移动) ← 移动决策入口
└── AttackTargetTask (攻击敌人)
```

**文件位置**: `battle/UnitBehaviorTreeFactory.java`

---

## 2. 移动任务 (MoveToEnemyTask)

**文件位置**: `battle/MoveToEnemyTask.java`

### 执行流程

| 方法 | 作用 |
|------|------|
| `start()` | 切换状态机到 `MoveState` |
| `execute()` | 计算与目标的距离，设置移动方向 |
| `end()` | 任务结束清理 |

### 核心逻辑

```java
if (dst > attackRange) {
    self.moveComponent.dir.set(targetX - x, targetY - y);
    return Status.RUNNING;
} else {
    bb.stateMachine.switchState(States.NORMAL_STATE);
    return Status.SUCCEEDED;
}
```

- 当距离 > 攻击范围时，设置移动方向向量，返回 `RUNNING`
- 当距离 ≤ 攻击范围时，切换到 `NormalState`，返回 `SUCCEEDED`

---

## 3. 状态机控制层

**文件位置**: `sm/state/common/MoveState.java`

### 状态切换

```java
onEnter()  →  canWalk = true   (允许移动)
onExit()   →  canWalk = false  (停止移动)
```

通过 `canWalk` 标志控制移动开关。

---

## 4. 数据层 (Model)

**文件位置**: `model/MoveComponent.java`

```java
public class MoveComponent {
    // 移动控制
    public boolean canWalk = false;   // 是否允许移动
    public float speed;                // 移动速度
    public Vector2 dir = new Vector2();  // 移动方向向量

    // 其他速度叠加（如击退效果）
    public Vector2 otherVel = new Vector2();
}
```

---

## 5. 计算层 (Logic)

**文件位置**: `logic/MovementCalculator.java`

### 核心计算方法

```java
public Vector2 calculateTotalMove(MoveComponent moveComponent) {
    if (moveComponent.canWalk) {
        tmp.set(moveComponent.dir).nor().scl(moveComponent.speed);
    } else {
        tmp.set(0, 0);
    }
    return tmp.add(moveComponent.otherVel);
}
```

- 对方向向量进行归一化并乘以速度
- 支持叠加额外速度（如击退效果）

---

## 6. 更新层 (Updater)

**文件位置**: `updater/BattleCharacterUpdater.java`

```java
public void update(BattleCharacter character, float delta) {
    Vector2 totalMoveVal = movementCalculator.calculateTotalMove(character.moveComponent);
    character.setX(character.getX() + totalMoveVal.x * delta);
    character.setY(character.getY() + totalMoveVal.y * delta);
}
```

每帧调用，应用 `delta` 时间增量更新位置。

---

## 数据流总结

```
行为树决策 (MoveToEnemyTask)
    ↓
设置移动方向 (moveComponent.dir)
    ↓
状态机切换 (MoveState)
    ↓
canWalk = true (允许移动)
    ↓
MovementCalculator 计算 (dir.nor().scl(speed))
    ↓
BattleCharacterUpdater 更新位置 (position += move * delta)
```

---

## 架构特点

1. **关注点分离**: 每层职责单一，Model 纯数据，Logic 纯计算，Updater 纯更新
2. **可扩展性**: `otherVel` 字段支持叠加外部速度效果
3. **状态驱动**: 通过状态机控制移动开关，清晰可控
4. **遵循项目规范**: 完全符合 `Model → Updater → Manager → Render` 分层原则
