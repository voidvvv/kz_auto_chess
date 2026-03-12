# GameScreen 重构 Brainstorm

**日期:** 2026-03-12
**主题:** GameScreen 类重构 - 领域驱动架构 + 事件总线

---

## What We're Building

重构过大的 GameScreen 类（1032行），采用领域驱动架构 + 事件总线模式，实现清晰的职责分离。

### 当前问题
- **UI逻辑过于复杂**: GameScreen 中混合了大量 UI 渲染代码
- **渲染 begin/end 管理混乱**: 多个渲染器的 begin/end 调用分散
- **整体职责不清晰**: GameScreen 混合了生命周期协调、UI 渲染、战斗逻辑、输入处理等多种职责

### 重构目标
1. GameScreen 仅作为协调者（生命周期、更新、渲染协调）
2. 每个领域模块自行管理其 update 和 render
3. GameUIManager 完全接管 UI 逻辑
4. 使用事件总线进行组件间通信

---

## Why This Approach

### 选择领域驱动 + 事件总线 + RenderHolder 的原因

1. **符合项目现有架构**
   - 项目已有 `GameEventSystem` 事件系统
   - 已有 Blackboard 模式实践
   - 遵循 model/updator/manager/render 分离原则

2. **清晰的领域边界**
   - BattleManager: 战斗逻辑（开始、更新、结束）
   - EconomyManager: 经济逻辑（金币、刷新成本、回合结算）
   - CardManager: 卡牌逻辑（卡池、购买、升级）
   - UIManager (已存在): UI 渲染和交互

3. **低耦合，高内聚**
   - 每个 Manager 独立管理自己的数据和逻辑
   - 通过事件总线解耦组件间依赖
   - 易于单元测试和扩展

4. **RenderHolder 解决渲染管理问题**
   - 集中管理 SpriteBatch 和 ShapeRenderer 引用
   - 每个 Manager 自行控制 begin/end，避免混乱
   - 通过 flush() 确保 Manager 之间状态正确
   - RenderCoordinator 不关心渲染细节，只需按顺序调用

5. **渐进式重构友好**
   - 可以逐步将 GameScreen 中的逻辑迁移到各 Manager
   - 不影响现有功能的正常运行
   - 分阶段降低风险

---

## Key Decisions

### 决策 1: 领域划分

| Manager | 职责范围 | 主要数据 | 渲染 |
|----------|----------|----------|--------|
| **BattleManager** | 战斗生命周期、角色行为树、伤害计算 | 通过 BattleContext 访问 | 实现 GameRenderer，内部管理 begin/end |
| **EconomyManager** | 金币管理、经济计算、回合结算 | PlayerEconomy | 可选实现 GameRenderer（经济可视化） |
| **CardManager** | 卡池管理、购买逻辑、升级逻辑 | CardPool, CardShop, PlayerDeck | 可选实现 GameRenderer |
| **UIManager** | UI 渲染、布局、交互（独立于 GameMode） | Stage, Skin, 各种 UI 组件 | 实现 GameRenderer，内部管理 begin/end |
| **RenderCoordinator** | 持有 RenderHolder，按顺序调用各 Manager | RenderHolder | 不实现 GameRenderer，负责调度 |

### 决策 2: 渲染管理

创建 `RenderHolder` 类作为渲染上下文：

```java
public class RenderHolder {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;

    public RenderHolder(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    /**
     * 确保渲染器状态正确，用于 Manager 之间切换时调用
     */
    public void flush() {
        spriteBatch.flush();
    }
}
```

**设计特点：**
- `RenderHolder` 聚合 `SpriteBatch` 和 `ShapeRenderer`
- `RenderCoordinator` 持有 `RenderHolder`，传递给各 Manager
- 每个 Manager 只有一个 `render(RenderHolder holder)` 方法
- Manager 内部自行管理 begin/end 调用
- RenderCoordinator 不关心 begin/end，只需按顺序调用各 Manager
- 每次调用 `flush()` 确保上一个 Manager 渲染完成

**渲染顺序（由 RenderCoordinator 控制）：**
1. BattleManager 层
2. CardManager 层（卡片/角色渲染）
3. ProjectileManager 层
4. DamageManager 层
5. UIManager 层（独立，由 UIManager 自行管理）

### 决策 3: RenderCoordinator 实现

```java
public class RenderCoordinator {
    private final RenderHolder holder;

    public RenderCoordinator(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.holder = new RenderHolder(spriteBatch, shapeRenderer);
    }

    public void renderAll(List<GameRenderer> renderers) {
        for (GameRenderer renderer : renderers) {
            renderer.render(holder);
            holder.flush();
        }
    }

    public RenderHolder getHolder() {
        return holder;
    }
}

public interface GameRenderer {
    void render(RenderHolder holder);
}
```

**关键点：**
- RenderCoordinator 创建并持有 RenderHolder
- 各 Manager 实现 `GameRenderer` 接口
- 每次调用 `render()` 后调用 `flush()` 确保状态正确
- Coordinator 不关心 begin/end，完全由各 Manager 内部管理

### 决策 4: 事件驱动通信

使用现有的 `GameEventSystem`，定义以下事件类型：

```java
// 战斗事件
BattleStartEvent, BattleEndEvent, DamageEvent

// 经济事件
GoldSpendEvent, GoldEarnEvent, RefreshEvent

// 卡牌事件
CardBuyEvent, CardSellEvent, CardUpgradeEvent

// 拖拽事件
DragStartEvent, DragMoveEvent, DragDropEvent, DragCancelEvent
```

### 决策 4: BattleContext - 战斗上下文

创建 `BattleContext` 类聚合战斗相关的所有对象，各 Manager 通过引用 BattleContext 访问共享数据：

```java
public class BattleContext {
    private Battlefield battlefield;
    private List<BattleUnitBlackboard> bbList;
    private GamePhase phase;
    private PlayerEconomy playerEconomy;  // 引用，非持有
    // ... 其他战斗相关数据
}
```

### 决策 5: GameMode - 游戏模式抽象

创建 `GameMode` 类解耦 Screen 与游戏主逻辑，便于扩展不同游戏类型：

```java
public interface GameMode {
    void update(float delta);
    void render(RenderHolder holder);
    void handleInput(float x, float y, boolean justTouched);
    void dispose();
}

public class AutoChessGameMode implements GameMode {
    private BattleContext battleContext;
    private BattleManager battleManager;
    private EconomyManager economyManager;
    private CardManager cardManager;
    private RenderCoordinator renderCoordinator;
    private GameEventSystem eventSystem;

    @Override
    public void update(float delta) {
        eventSystem.dispatch();
        battleManager.update(delta);
        economyManager.update(delta);
        cardManager.update(delta);
    }

    @Override
    public void render(RenderHolder holder) {
        battleManager.render(holder);
        economyManager.render(holder);  // 可选，如果经济有可视化
        cardManager.render(holder);    // 可选，如果卡牌有可视化
        holder.flush();
    }
}
```

### 决策 5: GameScreen 新架构

```java
public class GameScreen implements Screen {
    private KzAutoChess game;
    private GameMode gameMode;
    private GameUIManager uiManager;
    private GameInputHandler inputHandler;
    private RenderHolder renderHolder;  // 持有渲染器引用

    @Override
    public void render(float delta) {
        // 清屏
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // GameMode 处理游戏逻辑和渲染
        gameMode.update(delta);
        gameMode.render(renderHolder);

        // UI 独立渲染（始终在最上层）
        uiManager.act(delta);
        uiManager.renderCustomUI();
        uiManager.render(renderHolder);  // 使用同一 RenderHolder
        uiManager.draw();
    }
}
```

---

## Implementation Phases

### Phase 1: 基础架构准备
- 创建 `GameMode` 接口和 `AutoChessGameMode` 实现
- 创建 `BattleContext` 类
- 创建 `RenderHolder` 类（聚合 SpriteBatch 和 ShapeRenderer）
- 创建 `GameRenderer` 接口
- 创建 `RenderCoordinator` 类
- 创建 `BattleManager`, `EconomyManager`, `CardManager` 骨架（实现 GameRenderer 接口）
- 定义事件类型（BattleStartEvent, BattleEndEvent, GoldSpendEvent 等）
- 修改 GameScreen 使用 GameMode 模式

### Phase 2: BattleContext 初始化
- 定义 BattleContext 的数据结构
- 将 GameScreen 中的战场相关数据迁移到 BattleContext
- 各 Manager 通过构造函数接收 BattleContext 引用

### Phase 3: 战斗逻辑迁移
- 迁移 `startBattle()`, `updateBattle()`, `endBattle()` 到 BattleManager
- 迁移角色加载/卸载逻辑（loadCharacter, unloadCharacter）
- 迁移行为树管理（unitTrees, behaviorTreeMap）
- 发送 BattleStartEvent, BattleEndEvent

### Phase 4: 经济逻辑迁移
- 迁移经济相关方法到 EconomyManager
- 发送 GoldSpendEvent, GoldEarnEvent
- EconomyManager 监听 BattleEndEvent 计算回合奖励

### Phase 5: 卡牌逻辑迁移
- 迁移卡池、商店逻辑到 CardManager
- 迁移购买、升级逻辑（isCardUpgradable）
- 发送 CardBuyEvent, CardSellEvent, CardUpgradeEvent

### Phase 6: UI 逻辑迁移
- 将 GameScreen 中剩余的 UI 渲染代码迁移到 UIManager
- UIManager 实现 `GameRenderer` 接口，提供 `render(RenderHolder holder)` 方法
- UIManager 内部自行管理 begin/end
- UIManager 监听各类事件更新 UI
- 统一拖拽预览渲染到 UIManager

### Phase 7: 渲染协调
- 确认各 Manager 实现 `GameRenderer` 接口
- 各 Manager 的 `render(RenderHolder holder)` 方法中自行管理 begin/end
- RenderCoordinator 按顺序调用各 Manager 的 render 方法
- 每次调用后调用 `holder.flush()` 确保状态正确
- UIManager 独立于 GameMode 的渲染流程，由 GameScreen 单独调用

### Phase 8: 清理和优化
- 删除 GameScreen 中已迁移的代码
- 清理重复代码（GameScreen 和 UIManager 之间的重复）
- 添加单元测试
- 性能优化（事件批量处理检查）

---

## Resolved Questions

1. ~~**事件性能**: 如果事件数量过多，是否会影响性能？需要批量处理吗？~~
   - **已解决**: 事件处理器每帧 dispatch 所有缓存的事件，组件 send 时存入缓存等待下一帧 dispatch

2. ~~**数据共享**: 各 Manager 之间需要共享数据（如 EconomyManager 需要知道战斗结果），是继续通过事件还是直接引用？~~
   - **已解决**: 创建 `BattleContext` 聚合战斗相关对象，各 Manager 通过引用 BattleContext 访问

3. ~~**Blackboard 角色**: 是否需要创建 GameScreenBlackboard 来聚合所有 Manager 的引用？~~
   - **已解决**: 创建 `GameMode` 类管理 BattleContext 和各种 Manager，Screen 只关心对 mode 的 update 和 render

4. ~~**渲染顺序**: 不同领域的渲染顺序是否有特定要求？UI 是否总是最后渲染？~~
   - **已解决**: 按图层顺序渲染：BattleField → Characters → Projectiles → DamageLines → UI

5. **测试策略**: 如何对事件驱动的组件进行单元测试？需要 mock 事件系统吗？
   - **待确认**: 在实施阶段根据实际需求决定

---

## Next Steps

1. **完成文档审查**: 检查 brainstorm 文档是否完整准确
2. **创建详细计划**: 为每个 Phase 制定具体的任务清单和验收标准
3. **开始实施**: 从 Phase 1 开始逐步迁移
