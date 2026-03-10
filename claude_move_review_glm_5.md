# LibGDX 自走棋项目 - 角色移动逻辑分析报告

> 分析模型: glm-5
> 分析日期: 2026-03-10

## 一、项目整体目录结构

```
kz_auto_chess/
├── core/src/main/java/com/voidvvv/autochess/
│   ├── battle/               # 战斗行为树相关
│   │   ├── AttackTargetTask.java      # 攻击任务
│   │   ├── BattleTelegraph.java       # 战斗电报接口
│   │   ├── BattleUnitBlackboard.java  # 战斗单位黑板
│   │   ├── FindEnemyTask.java         # 寻敌任务
│   │   ├── MoveToEnemyTask.java       # 移动到敌人任务
│   │   └── UnitBehaviorTreeFactory.java # 行为树工厂
│   │
│   ├── listener/             # 事件监听器
│   │   └── damage/           # 伤害相关监听
│   │
│   ├── logic/                # 业务逻辑层
│   │   ├── CardUpgradeLogic.java      # 卡牌升级逻辑
│   │   ├── CharacterStatsLoader.java  # 角色属性加载
│   │   ├── EconomyCalculator.java     # 经济计算
│   │   ├── MovementCalculator.java    # 移动计算器 [核心]
│   │   └── SynergyManager.java        # 羁绊管理
│   │
│   ├── manage/               # 管理器
│   │   ├── ProjectileManager.java     # 投掷物管理
│   │   ├── RenderDataManager.java     # 渲染数据管理
│   │   └── ParticleSpawner.java       # 粒子生成器
│   │
│   ├── model/                # 数据模型层
│   │   ├── battle/           # 战斗相关模型
│   │   ├── event/            # 事件模型
│   │   ├── control/          # 控制模型
│   │   ├── BattleCharacter.java       # 战斗角色 [核心]
│   │   ├── Battlefield.java           # 战场
│   │   ├── Card.java                  # 卡牌
│   │   ├── MoveComponent.java         # 移动组件 [核心]
│   │   ├── PlayerEconomy.java         # 玩家经济
│   │   ├── Projectile.java            # 投掷物
│   │   └── ...
│   │
│   ├── msg/                  # 消息系统
│   │   └── MessageConstants.java      # 消息常量
│   │
│   ├── render/               # 渲染层
│   │   ├── BattleFieldRender.java     # 战场渲染
│   │   ├── ProjectileRenderer.java    # 投掷物渲染
│   │   └── ...
│   │
│   ├── screens/              # 游戏界面
│   │   ├── GameScreen.java            # 游戏运行界面 [核心]
│   │   ├── LevelSelectScreen.java     # 关卡选择
│   │   └── StartScreen.java           # 开始界面
│   │
│   ├── sm/                   # 状态机
│   │   ├── machine/          # 状态机实现
│   │   │   ├── BaseStateMachine.java  # 基础状态机 [核心]
│   │   │   ├── StateChangeListener.java
│   │   │   └── StateMachine.java      # 状态机接口
│   │   │
│   │   └── state/            # 状态定义
│   │       ├── common/
│   │       │   ├── AttackState.java   # 攻击状态 [核心]
│   │       │   ├── MoveState.java     # 移动状态 [核心]
│   │       │   ├── NormalState.java   # 普通状态
│   │       │   └── States.java        # 状态常量
│   │       ├── AbstractState.java     # 抽象状态基类
│   │       └── BaseState.java         # 状态接口
│   │
│   ├── updater/              # 更新器
│   │   ├── BattleCharacterUpdater.java # 角色更新器 [核心]
│   │   ├── BattleUpdater.java         # 战斗更新器
│   │   └── ...
│   │
│   └── utils/                # 工具类
│
├── lwjgl3/                   # LWJGL3 启动器
├── assets/                   # 游戏资源
└── docs/                     # 文档
```

---

## 二、移动相关核心类与文件

### 2.1 核心文件清单

| 文件路径 | 作用 |
|---------|------|
| `core/.../model/MoveComponent.java` | 移动组件数据模型 |
| `core/.../logic/MovementCalculator.java` | 移动向量计算器 |
| `core/.../updater/BattleCharacterUpdater.java` | 角色位置更新器 |
| `core/.../sm/state/common/MoveState.java` | 移动状态 |
| `core/.../battle/MoveToEnemyTask.java` | 移动到敌人行为树任务 |
| `core/.../model/BattleCharacter.java` | 战斗角色（含移动组件） |
| `core/.../sm/machine/BaseStateMachine.java` | 状态机（管理移动状态切换） |
| `core/.../battle/BattleUnitBlackboard.java` | 黑板（协调状态与行为树） |

---

## 三、移动逻辑详细分析

### 3.1 数据模型 - MoveComponent

**文件位置**: `core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java`

```java
public class MoveComponent {
    // 行走相关
    public boolean canWalk = false;   // 是否允许行走
    public float speed;               // 移动速度
    public Vector2 dir = new Vector2(); // 移动方向向量

    // 其他速度（外部施加的力，如击退效果）
    public Vector2 otherVel = new Vector2();
}
```

**字段说明**:
- `canWalk`: 布尔标志，控制角色是否可以移动
- `speed`: 标量速度值（单位：世界单位/秒）
- `dir`: 方向向量，指向目标位置
- `otherVel`: 额外速度向量，用于非主动移动（如击退、推力等）

---

### 3.2 移动计算 - MovementCalculator

**文件位置**: `core/src/main/java/com/voidvvv/autochess/logic/MovementCalculator.java`

```java
public class MovementCalculator {
    private final Vector2 tmp = new Vector2();

    /**
     * 计算总移动向量
     * 公式: 总移动向量 = (方向向量.归一化() * 速度) + 其他速度
     */
    public Vector2 calculateTotalMove(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return new Vector2(0, 0);
        }

        if (moveComponent.canWalk) {
            tmp.set(moveComponent.dir).nor().scl(moveComponent.speed);
        } else {
            tmp.set(0, 0);
        }
        return tmp.add(moveComponent.otherVel);
    }

    /**
     * 检查角色是否正在移动
     */
    public boolean isMoving(MoveComponent moveComponent) {
        if (moveComponent == null) {
            return false;
        }
        return moveComponent.canWalk && moveComponent.speed > 0;
    }

    /**
     * 获取移动速度（标量）
     */
    public float getMoveSpeed(MoveComponent moveComponent) {
        if (moveComponent == null || !moveComponent.canWalk) {
            return 0;
        }
        return moveComponent.speed;
    }
}
```

**计算公式**:
```
总移动向量 = (方向向量.归一化() * 速度) + 其他速度
```

**设计特点**:
- 采用无状态设计（`tmp` 是复用的临时变量）
- 支持主动移动 + 被动移动（击退等）的组合

---

### 3.3 位置更新 - BattleCharacterUpdater

**文件位置**: `core/src/main/java/com/voidvvv/autochess/updater/BattleCharacterUpdater.java`

```java
public class BattleCharacterUpdater {
    private final MovementCalculator movementCalculator = new MovementCalculator();

    public void update(BattleCharacter character, float delta) {
        // 1. 计算总移动向量
        Vector2 totalMoveVal = movementCalculator.calculateTotalMove(character.moveComponent);

        // 2. 应用到位置（乘以时间增量）
        character.setX(character.getX() + totalMoveVal.x * delta);
        character.setY(character.getY() + totalMoveVal.y * delta);
    }
}
```

**更新公式**:
```
新位置X = 当前位置X + 总移动向量X * 时间增量
新位置Y = 当前位置Y + 总移动向量Y * 时间增量
```

**调用时机**:
在 `GameScreen.postUpdateBattle()` 方法中，每帧对每个存活角色调用：
```java
for (BattleCharacter c : battlefield.getCharacters()) {
    if (c.isDead()) continue;
    battleCharacterUpdater.update(c, delta);  // 位置更新
    aliveCharacters.add(c);
}
```

---

### 3.4 移动状态 - MoveState

**文件位置**: `core/src/main/java/com/voidvvv/autochess/sm/state/common/MoveState.java`

```java
public class MoveState extends AbstractState {
    public static final MoveState INSTANCE = new MoveState();  // 单例模式

    private MoveState() {}

    @Override
    public String name() {
        return "move";
    }

    @Override
    protected void onEnter(BattleUnitBlackboard entity) {
        // 进入移动状态时，启用行走
        entity.getSelf().moveComponent.canWalk = true;
    }

    @Override
    protected void onExit(BattleUnitBlackboard entity) {
        // 退出移动状态时，禁用行走
        entity.getSelf().moveComponent.canWalk = false;
    }
}
```

**状态生命周期**:
1. **进入状态** (`onEnter`): 设置 `canWalk = true`，允许角色移动
2. **更新中** (`onUpdate`): 无额外逻辑（由 `BattleCharacterUpdater` 处理位置更新）
3. **退出状态** (`onExit`): 设置 `canWalk = false`，停止移动

---

### 3.5 移动到敌人任务 - MoveToEnemyTask

**文件位置**: `core/src/main/java/com/voidvvv/autochess/battle/MoveToEnemyTask.java`

这是行为树的叶子节点，负责控制角色向敌人移动。

```java
public class MoveToEnemyTask extends LeafTask<BattleUnitBlackboard> {
    boolean firstFrame = false;

    @Override
    public void start() {
        BattleUnitBlackboard bb = getObject();
        // 切换到移动状态
        bb.stateMachine.switchState(States.BASE_MOVE_STATE);
        firstFrame = true;
    }

    @Override
    public Status execute() {
        // 第一帧跳过（等待状态切换完成）
        if (firstFrame) {
            firstFrame = false;
            return Status.RUNNING;
        }

        BattleUnitBlackboard bb = getObject();
        BattleCharacter target = bb.getTarget();
        BattleCharacter self = bb.getSelf();

        // 失败条件检查
        if (target == null || target.isDead() || self == null || self.isDead()) {
            return Status.FAILED;
        }

        // 状态检查：如果不在移动状态，则失败
        if (!bb.stateMachine.getCurrent().isState(States.BASE_MOVE_STATE)) {
            return Status.FAILED;
        }

        // 获取位置信息
        float targetX = target.getX();
        float targetY = target.getY();
        float x = self.getX();
        float y = self.getY();

        // 计算距离
        float attackRange = self.getAttackRange();
        float dst = Vector2.dst(x, y, targetX, targetY);

        if (dst > attackRange) {
            // 【核心】设置移动方向：目标位置 - 当前位置
            self.moveComponent.dir.set(targetX - x, targetY - y);
            return Status.RUNNING;  // 继续移动
        } else {
            // 到达攻击范围，切换到普通状态
            bb.stateMachine.switchState(States.NORMAL_STATE);
            return Status.SUCCEEDED;  // 移动完成
        }
    }
}
```

**移动触发条件**:
1. 目标存在且未死亡
2. 自身未死亡
3. 当前处于移动状态
4. 距离大于攻击范围

**移动路径计算**:
- 采用**直线追踪**方式
- 方向向量 = 目标位置 - 自身位置
- 每帧重新计算方向，实现动态追踪

**移动终止条件**:
- 距离 <= 攻击范围（成功到达）
- 目标死亡或无效（失败）
- 自身死亡（失败）
- 状态被外部改变（失败）

---

## 四、移动触发的完整流程

### 4.1 行为树驱动流程

```
UnitBehaviorTreeFactory.create()
    ↓
创建 Sequence 序列节点:
    [FindEnemyTask] → [MoveToEnemyTask] → [AttackTargetTask]
```

**完整流程**:

```
1. FindEnemyTask.execute()
   ├── 获取敌方列表
   ├── 根据职业选择目标（战士选前排、射手选最近等）
   └── 设置黑板.target
       ↓
2. MoveToEnemyTask.start()
   └── 状态机.switchState(MOVE_STATE)
       ↓
3. MoveState.onEnter()
   └── moveComponent.canWalk = true
       ↓
4. MoveToEnemyTask.execute() [循环执行]
   ├── 计算方向: dir = target.pos - self.pos
   ├── 检查距离 > attackRange?
   │   ├── YES: return RUNNING (继续移动)
   │   └── NO: switchState(NORMAL_STATE), return SUCCEEDED
   └── 同时: BattleCharacterUpdater.update() 每帧更新位置
       ↓
5. MoveState.onExit()
   └── moveComponent.canWalk = false
       ↓
6. AttackTargetTask.execute()
   └── 执行攻击逻辑
```

### 4.2 状态机协调

**文件位置**: `core/src/main/java/com/voidvvv/autochess/sm/machine/BaseStateMachine.java`

```java
public class BaseStateMachine<T> implements StateMachine<T> {
    private T own;
    private BaseState<T> current;
    private BaseState<T> pendingNext;  // 延迟切换

    @Override
    public void update(float delta) {
        processPendingSwitch();  // 处理待切换状态
        if (current != null) {
            current.update(own, delta);  // 更新当前状态
        }
    }

    @Override
    public void switchState(BaseState<T> next) {
        pendingNext = next;  // 延迟切换（避免在update中直接切换）
    }

    private void processPendingSwitch() {
        if (pendingNext == null) return;

        // 检查当前状态是否允许退出
        if (!force && current != null && !current.canExit(own, pendingNext)) {
            notifyRejected(current, pendingNext);
            return;  // 拒绝切换
        }

        // 执行切换
        current.exit(own);
        current = pendingNext;
        current.enter(own);
    }
}
```

**状态切换守卫机制** (AttackState 中的实现):
```java
@Override
public boolean canExit(BattleUnitBlackboard entity, BaseState<BattleUnitBlackboard> nextState) {
    if (nextState instanceof MoveState) {
        // 只有特殊角色（如刺客）可以在攻击时移动
        return entity.getSelf().canMoveWhileAttacking();
    }
    return true;
}
```

---

## 五、移动速度和方向处理

### 5.1 速度初始化

在 `BattleCharacter.reset()` 中设置：
```java
moveComponent.speed = 10f;         // 默认速度 10 世界单位/秒
moveComponent.canWalk = true;      // 默认允许行走
```

### 5.2 攻击范围（影响移动终止）

在 `BattleCharacter.inferAttackRange()` 中根据职业类型设置：
```java
switch (card.getType()) {
    case ARCHER:
        attackRange = 150f;  // 射手远程，提前停止
        break;
    case MAGE:
        attackRange = 120f;  // 法师中程
        break;
    default:
        attackRange = 70f;   // 近战
        break;
}
```

### 5.3 方向计算

在 `MoveToEnemyTask.execute()` 中：
```java
// 简单的直线方向
self.moveComponent.dir.set(targetX - x, targetY - y);
```

**注意**: 方向向量**未归一化**存储，在 `MovementCalculator.calculateTotalMove()` 中进行归一化：
```java
tmp.set(moveComponent.dir).nor().scl(moveComponent.speed);
```

---

## 六、移动状态更新机制

### 6.1 每帧更新流程

```
GameScreen.render(delta)
    ↓
if (phase == BATTLE)
    updateBattle(delta)
        ↓
    postUpdateBattle(delta)
        ├── BattleCharacterUpdater.update(c, delta)  // 位置更新
        └── SynergyManager.applySynergyEffects()     // 应用羁绊
```

### 6.2 黑板更新

在 `BattleUnitBlackboard.update()` 中：
```java
public void update(float delta) {
    this.stateMachine.update(delta);  // 更新状态机
    this.self.attackCooldown -= delta;  // 更新攻击冷却
}
```

---

## 七、设计模式总结

### 7.1 采用的设计模式

| 模式 | 应用位置 | 说明 |
|-----|---------|------|
| **单例模式** | `MoveState.INSTANCE`, `NormalState.instance` | 状态对象全局唯一 |
| **组合模式** | `BattleCharacter` 包含 `MoveComponent` | 将移动数据与角色分离 |
| **策略模式** | `MovementCalculator` | 移动计算逻辑独立封装 |
| **状态模式** | `BaseStateMachine` + 各种 `State` | 状态切换控制行为 |
| **行为树模式** | `UnitBehaviorTreeFactory`, `MoveToEnemyTask` 等 | AI决策逻辑 |

### 7.2 分层架构

```
┌─────────────────────────────────────────┐
│           screens (GameScreen)          │  ← 界面层
├─────────────────────────────────────────┤
│     battle (BehaviorTree, Blackboard)   │  ← AI决策层
├─────────────────────────────────────────┤
│  sm (StateMachine, States)              │  ← 状态管理层
├─────────────────────────────────────────┤
│  updater (BattleCharacterUpdater)       │  ← 更新层
├─────────────────────────────────────────┤
│  logic (MovementCalculator)             │  ← 计算层
├─────────────────────────────────────────┤
│  model (MoveComponent, BattleCharacter) │  ← 数据层
└─────────────────────────────────────────┘
```

---

## 八、关键文件路径汇总

| 类别 | 相对路径 |
|-----|---------|
| 移动组件 | `core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java` |
| 移动计算器 | `core/src/main/java/com/voidvvv/autochess/logic/MovementCalculator.java` |
| 角色更新器 | `core/src/main/java/com/voidvvv/autochess/updater/BattleCharacterUpdater.java` |
| 移动状态 | `core/src/main/java/com/voidvvv/autochess/sm/state/common/MoveState.java` |
| 移动任务 | `core/src/main/java/com/voidvvv/autochess/battle/MoveToEnemyTask.java` |
| 战斗角色 | `core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java` |
| 状态机 | `core/src/main/java/com/voidvvv/autochess/sm/machine/BaseStateMachine.java` |
| 黑板 | `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java` |
| 行为树工厂 | `core/src/main/java/com/voidvvv/autochess/battle/UnitBehaviorTreeFactory.java` |
| 游戏界面 | `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` |

---

## 九、总结

该项目的移动系统采用了清晰的分层架构：

1. **数据层**: `MoveComponent` 存储移动相关数据（速度、方向、是否可移动）
2. **计算层**: `MovementCalculator` 负责计算移动向量，支持主动移动和被动移动的组合
3. **更新层**: `BattleCharacterUpdater` 每帧更新角色位置
4. **状态层**: `MoveState` 通过状态机控制移动的开启/关闭
5. **AI决策层**: `MoveToEnemyTask` 通过行为树决定何时移动、向哪里移动

**移动特点**:
- 采用简单的直线追踪算法
- 动态计算方向（每帧更新）
- 通过攻击范围判断移动终止
- 支持状态守卫（攻击状态可阻止移动切换）
- 支持额外速度叠加（如击退效果）
