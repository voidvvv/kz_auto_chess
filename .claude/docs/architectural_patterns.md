# KZ AutoChess 架构模式

本文档记录项目中使用的架构模式、设计决策和约定。

## 核心架构模式

### 1. Model/Updator/Manager/Render 分离

这是项目最核心的架构原则，在多个文件中严格遵循：

| 层级 | 职责 | 示例文件 |
|------|------|----------|
| **Model** | 纯数据结构，无业务逻辑 | `Card.java:1-120`, `BattleCharacter.java:1-150`, `PlayerEconomy.java:1-80` |
| **Updator** | 数据更新逻辑，每帧调用 | `BattleUpdater.java:1-90`, `BattleCharacterUpdater.java:1-50`, `ProjectileUpdater.java:1-120` |
| **Manager** | 生命周期管理、协调多个组件 | `BattleManager.java:55-453`, `EconomyManager.java:1-100`, `CardManager.java:1-150` |
| **Render** | 仅负责渲染，无业务逻辑 | `BattleFieldRender.java:1-80`, `BattleCharacterRender.java:1-60`, `ProjectileRenderer.java:1-100` |

**设计决策**: 严格的关注点分离使得每个类职责单一，便于测试和维护。

### 2. Blackboard 模式

Blackboard 是 AI 代理的上下文聚合器，持有当前单位与战场引用：

```
BattleUnitBlackboard (battle/BattleUnitBlackboard.java:29-473)
├── self: BattleCharacter      // 当前角色
├── battlefield: Battlefield    // 战场引用
├── target: BattleCharacter     // 当前目标
├── stateMachine: StateMachine  // 状态机
├── skill: Skill               // 技能实例
└── mana: ManaComponent        // 魔法值组件
```

**设计决策**:
- 黑板聚合多种 model 甚至状态机
- 各场景可有自己的 Blackboard（如 `PlayerLifeBlackboard`）
- AI 节点通过黑板访问数据，不直接访问模型

### 3. 事件驱动架构

通过 `GameEventSystem` 实现解耦通信：

```
GameEventSystem (event/GameEventSystem.java:14-60)
├── eventHolder: GameEventHolder     // 事件队列
├── listenerHolder: GameEventListenerHolder  // 监听器注册
└── dispatcher: GameEventDispatcher  // 分发器
```

**事件领域分包**:
```
event/
├── card/          # CardBuyEvent, CardSellEvent, CardUpgradeEvent
├── economy/       # GoldEarnEvent, RefreshEvent
├── drag/          # DragStartedEvent, DragMovedEvent, DroppedEvent
└── (root)         # BattleStartEvent, BattleEndEvent, PhaseTransitionEvent
```

**设计决策**:
- 所有跨模块通信通过事件系统
- 监听器实现 `GameEventListener` 接口
- 每帧先分发事件再执行更新

### 4. GameMode 抽象层

将屏幕与游戏逻辑分离：

```
GameMode (game/GameMode.java) - 接口
    └── AutoChessGameMode (game/AutoChessGameMode.java:29-228)
        ├── battleManager: BattleManager
        ├── economyManager: EconomyManager
        ├── cardManager: CardManager
        ├── playerLifeManager: PlayerLifeManager
        ├── renderCoordinator: RenderCoordinator
        ├── eventSystem: GameEventSystem
        └── inputHandler: GameInputHandler
```

**生命周期方法**: `onEnter()`, `update()`, `render()`, `handleInput()`, `pause()`, `resume()`, `onExit()`, `dispose()`

**设计决策**: Screen 只负责切换，GameMode 负责游戏逻辑。

### 5. 渲染协调器模式

集中管理渲染管线：

```
RenderCoordinator (render/RenderCoordinator.java)
├── renderers: List<GameRenderer>
└── renderAll() -> for each renderer: render(holder) -> holder.flush()
```

**RenderHolder 模式**:
```
RenderHolder (render/RenderHolder.java)
├── spriteBatch: SpriteBatch
├── shapeRenderer: ShapeRenderer
└── flush() -> 确保渲染状态一致性
```

**设计决策**:
- 每个管理器管理自己的 `begin()`/`end()`
- 协调器只负责 `flush()` 确保状态切换

### 6. 状态机模式

角色行为状态管理：

```
StateMachine<BattleUnitBlackboard> (sm/machine/BaseStateMachine.java)
├── currentState: State
├── owner: BattleUnitBlackboard
├── switchState(state) -> 状态切换
└── update(delta) -> 状态执行
```

**状态定义** (`sm/state/common/States.java`):
- `NORMAL_STATE` - 空闲/移动
- `ATTACK_STATE` - 攻击中
- `DEAD_STATE` - 死亡

### 7. ModelHolder 泛型容器

通用的模型持有者模式：

```java
// model/ModelHolder.java:6-27
public class ModelHolder<T> {
    private List<T> list = new ArrayList<>();
    public List<T> getModels() { return List.copyOf(list); }  // 不可变副本
    public void addModel(T model);
    public void removeModel(T model);
    public void clear();
}
```

**使用场景**: `GameEventHolder`, `GameEventListenerHolder`, `DamageEventHolder`

## 依赖注入模式

通过构造函数注入依赖：

```java
// AutoChessGameMode 构造函数 (game/AutoChessGameMode.java:43-61)
public AutoChessGameMode(
    BattleState battleState,
    BattleManager battleManager,
    EconomyManager economyManager,
    CardManager cardManager,
    // ...
)
```

**设计决策**: 所有依赖通过构造函数注入，便于测试和明确依赖关系。

## 输入处理模式

统一的输入上下文：

```java
// input/InputContext.java
public class InputContext {
    public final float screenX, screenY;   // 屏幕坐标
    public final float worldX, worldY;     // 世界坐标

    public static InputContext fromInput(Camera camera) {
        // 从 Gdx.input 转换坐标
    }
}
```

**设计决策**: 一次计算两种坐标，管理器按需选择使用。

## 共享卡池模式

追踪卡牌可用数量：

```java
// model/SharedCardPool.java:16-119
public class SharedCardPool {
    // 等级对应最大数量: T1=15, T2=12, T3=9, T4=6, T5=3
    private static final int[] TIER_MAX_COPIES = {0, 15, 12, 9, 6, 3};

    public boolean decrementCopies(int cardId);  // 购买时调用
    public boolean incrementCopies(int cardId);  // 出售时调用
    public boolean isCardAvailable(int cardId);  // 商店刷新时检查
}
```

**设计决策**: 全局共享，跨关卡持久化，实现卡池耗尽机制。
