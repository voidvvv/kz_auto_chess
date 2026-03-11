# Screen类重新设计方案

**Date:** 2026-03-11
**Status:** Ready for Planning

---

## What We're Building

重构当前Screen类（主要是GameScreen），降低其复杂度，使其更易于维护和扩展。

---

## Problem Statement

当前 `GameScreen` 类存在以下问题：

1. **职责过重** - 945行代码，承担了商店、卡组、战斗、输入、渲染等多个职责
2. **输入与渲染耦合** - `handleInput()` 在 `render()` 循环中被调用
3. **硬编码布局** - UI坐标散落在代码各处，难以适配不同屏幕
4. **Screen间架构不一致** - StartScreen/LevelSelectScreen 与 GameScreen 使用不同的UI框架

---

## Design Goals

1. 降低 GameScreen 复杂度
2. 统一使用 Scene2D 作为UI框架
3. 全面分离职责：输入处理、UI逻辑、游戏阶段
4. 遵循项目现有的 model/updater/manager/render 架构
5. 不引入统一的 Screen 基类
6. **复用现有的事件模式（ModelHolder）**，而非引入全新的事件总线

---

## 事件系统说明

本项目使用三套事件系统，各有其适用场景。组件化重构**复用现有 `ModelHolder` 模式**，保持设计一致。

### 1. 领域事件系统（已有）
- **类**: `DamageEventListener` + `DamageEventListenerHolder` + `DamageEventHolder`
- **用途**: 战斗伤害等特定领域逻辑
- **范围**: `BattleManager` → `BattleUpdater` → `RenderManager`
- **模式**: 继承 `ModelHolder<T>`

### 2. Scene2D 事件系统（已有）
- **类**: `ClickListener`, `ChangeListener` 等 LibGDX 内置接口
- **用途**: UI按钮点击、Actor交互
- **范围**: `GameUIManager` 内部的 Scene2D 组件
- **模式**: Scene2D 标准API

### 3. 游戏事件系统（新增，复用现有模式）
- **类**: `GameEventListener` + `GameEventListenerHolder` + `GameEventHolder`
- **用途**: GameScreen 组件间协调（输入、UI、战斗、商店）
- **范围**: `GameInputHandler` ↔ `GameUIManager` ↔ `BattleManager` ↔ `ShopManager`
- **模式**: **复用 `ModelHolder<T>`**，与领域事件系统一致

### 使用原则
| 场景 | 使用的事件系统 | 示例 |
|------|-------------|------|
| 领域逻辑 | 领域事件系统 | 伤害计算→结算→更新HP |
| UI交互 | Scene2D事件系统 | 按钮点击、卡片悬停 |
| 组件间协调 | 游戏事件系统 | 点击购买→商店更新→UI更新 |

---

## Proposed Solution: Component-Based Refactoring

### 架构概览

```
GameScreen (调度容器)
├── GameInputHandler (输入处理)
├── GameUIManager (UI构建和布局)
├── BattleManager (战斗逻辑)
├── ShopManager (商店逻辑)
├── RenderCoordinator (渲染协调)
└── Models (数据模型)
```

### 组件职责

#### GameScreen
- 作为容器协调各组件
- 管理 Screen 生命周期
- 维护核心游戏状态（GamePhase）
- 创建并持有 `GameEventHolder` 和 `GameEventListenerHolder`，在各组件间传递

#### GameInputHandler
- 处理所有输入事件（键盘、鼠标、触摸）
- 将输入转换为高层事件（如 `ShopCardClicked`, `BattleStarted`）并添加到 `GameEventHolder`
- 注册为 `GameEventListener` 响应拖拽等事件
- 与 Scene2D InputMultiplexer 协作
- 管理拖拽状态（draggingCard/draggingCharacter）
- 拖拽时抑制其他输入事件

#### GameUIManager
- 使用 Scene2D `Table` 构建UI布局（仅UI部分，不包括战场）
- 管理所有Scene2D Actor（按钮、卡片等）
- 注册为 `GameEventListener` 响应UI更新事件
- 处理UI状态更新（刷新按钮文本、卡组显示等）
- 响应拖拽事件，更新拖拽预览显示

#### BattleManager
- 管理战斗逻辑（startBattle, updateBattle, endBattle）
- 协调 BehaviorTree 和 BattleUnitBlackboard
- 处理胜负判定
- 注册为 `GameEventListener`，分发战斗相关事件（BattleStarted, BattleEnded等）

#### ShopManager
- 管理商店逻辑（刷新、购买）
- 维护 CardShop 和 PlayerDeck 状态
- 处理卡牌升级逻辑
- 注册为 `GameEventListener`，分发商店相关事件（ShopCardPurchased, CardSold等）

#### RenderCoordinator
- 协调各 Render 类的执行
- 管理 Viewport 切换（UI viewport vs 游戏世界viewport）
- 处理渲染顺序

### Scene2D UI 结构

使用 `Table` 进行布局，替代硬编码坐标。**注意：战场部分不使用Scene2D组件**，因为战场使用游戏世界viewport，与UI坐标系不同，混合使用会导致坐标转换混乱。

```java
// 根容器（使用UI viewport，只包含UI部分）
Table rootTable = new Table();
rootTable.setFillParent(true);

// 顶部栏：标题 + 经济信息 + 羁绊
Table topBar = new Table();
rootTable.add(topBar).growX().row();

// 中间分为两列：商店 + 卡组
Table middleSection = new Table();
middleSection.add(shopTable).grow().padRight(20);
middleSection.add(deckTable).grow().padLeft(20);
rootTable.add(middleSection).grow().row();

// 底部：战场使用游戏世界viewport渲染，不在Scene2D Stage中
// RenderCoordinator 直接切换到游戏世界viewport进行渲染
```

### 输入处理架构

```java
InputMultiplexer multiplexer = new InputMultiplexer();

// 按优先级顺序添加处理器（从前到后，先处理优先返回true则停止）

// 1. 热键处理器 - 最高优先级，全局热键如F5切换渲染模式
multiplexer.addProcessor(hotkeyHandler);

// 2. Scene2D Stage - UI事件（按钮、卡片点击等）
multiplexer.addProcessor(stage);

// 3. 游戏世界输入处理器 - 战场拖拽、角色选择等
multiplexer.addProcessor(worldInputHandler);

// 冲突规则：
// - hotkeyHandler 返回true则停止后续处理
// - UI交互不触发世界输入
// - 拖拽时worldInputHandler返回true抑制其他处理
```

### 组件通信

**复用项目现有的 `ModelHolder` 模式**，而非引入全新的事件总线。这保持了与现有 `DamageEventListener` 一致的设计风格。

```java
// 事件接口
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}

// 事件容器 - 复用 ModelHolder
public class GameEventHolder extends ModelHolder<GameEvent> {
    // 复用现有逻辑：addModel/removeModel/getModels/clear
}

// 事件监听器容器 - 复用 ModelHolder
public class GameEventListenerHolder extends ModelHolder<GameEventListener> {
    // 复用现有逻辑：addModel/removeModel/getModels/clear
}

// 事件分发器（在需要分发的组件中实现）
public class GameEventDispatcher {
    private final GameEventHolder eventHolder;
    private final GameEventListenerHolder listenerHolder;

    public GameEventDispatcher(GameEventHolder eventHolder, GameEventListenerHolder listenerHolder) {
        this.eventHolder = eventHolder;
        this.listenerHolder = listenerHolder;
    }

    public void dispatch() {
        List<GameEventListener> listeners = listenerHolder.getModels();
        for (GameEvent event : eventHolder.getModels()) {
            for (GameEventListener listener : listeners) {
                listener.onGameEvent(event);
            }
        }
        eventHolder.clear();
    }
}
```

**事件类型（语义明确）：**

### UI交互事件
- `ShopCardClicked(Card card)` - 点击商店卡牌（选中，非购买）
- `ShopCardPurchased(Card card)` - 完成购买卡牌逻辑
- `DeckCardClicked(Card card)` - 点击卡组卡牌（选中）
- `DeckCardRemoved(Card card)` - 从卡组移除卡牌
- `RefreshClicked()` - 点击刷新按钮
- `StartBattleClicked()` - 点击开始战斗按钮
- `BackClicked()` - 点击返回按钮

### 拖拽事件
- `DragStarted(Object dragged, float x, float y)` - 拖拽开始
- `DragMoved(float x, float y)` - 拖拽移动
- `Dropped(Object dropped, float x, float y, String targetType)` - 放下拖拽对象
  - targetType: "battlefield", "deck", "shop", "cancel"
- `DragCancelled()` - 拖拽取消

### 游戏逻辑事件
- `BattleStarted()` - 战斗开始
- `BattleEnded(boolean playerWon)` - 战斗结束
- `PhaseChanged(GamePhase phase)` - 游戏阶段改变
- `CharacterPlaced(BattleCharacter character, float x, float y)` - 角色放置到战场
- `CharacterRemoved(BattleCharacter character)` - 角色从战场移除

### 组件接口

**GameScreen 与组件的接口：**

```java
// 所有组件的基接口 - 完整生命周期管理
interface GameComponent {
    void show();                          // Screen显示时调用
    void update(float delta);                // 每帧更新
    void resize(int width, int height);      // 屏幕尺寸变化时调用
    void pause();                          // 应用暂停时调用
    void resume();                         // 应用恢复时调用
    void hide();                          // Screen隐藏时调用
    void dispose();                        // Screen销毁时调用
}

// GameScreen 调度伪代码 - 使用现有模式
class GameScreen implements Screen {
    private List<GameComponent> components;

    // 游戏事件容器和监听器 - 复用 ModelHolder 模式
    private GameEventHolder gameEventHolder;
    private GameEventListenerHolder gameEventListenerHolder;

    @Override
    public void show() {
        gameEventHolder = new GameEventHolder();
        gameEventListenerHolder = new GameEventListenerHolder();

        // 各组件通过Holder注册监听器
        inputHandler = new GameInputHandler(game, gameEventHolder, gameEventListenerHolder);
        uiManager = new GameUIManager(game, gameEventHolder, gameEventListenerHolder);
        battleManager = new BattleManager(game, gameEventHolder, gameEventListenerHolder);
        shopManager = new ShopManager(game, gameEventHolder, gameEventListenerHolder);
        renderCoordinator = new RenderCoordinator(game);

        components = List.of(inputHandler, uiManager, battleManager, shopManager, renderCoordinator);
        components.forEach(GameComponent::show);
    }

    @Override
    public void render(float delta) {
        // 分发游戏事件（与 BattleUpdater 逻辑一致）
        GameEventDispatcher dispatcher = new GameEventDispatcher(gameEventHolder, gameEventListenerHolder);
        dispatcher.dispatch();

        components.forEach(c -> c.update(delta));
        renderCoordinator.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        components.forEach(c -> c.resize(width, height));
    }

    @Override
    public void pause() {
        components.forEach(GameComponent::pause);
    }

    @Override
    public void resume() {
        components.forEach(GameComponent::resume);
    }

    @Override
    public void hide() {
        components.forEach(GameComponent::hide);
    }

    @Override
    public void dispose() {
        components.forEach(GameComponent::dispose);
    }
}
```

---

## Key Decisions

| 决策 | 理由 |
|------|------|
| 完全使用Scene2D | 统一UI框架，利用Table布局简化代码 |
| 不使用统一Screen基类 | 保持灵活性，避免过度抽象 |
| **复用现有ModelHolder模式实现事件系统** | 与项目现有DamageEventListener设计一致，学习成本低 |
| 保持现有updater/render架构 | 与项目整体设计一致 |
| 组件职责明确 | 便于独立维护和测试 |
| 分阶段迁移 | 降低风险，每阶段可独立验证 |
| show()/dispose()中创建/销毁组件 | 与Screen生命周期对齐 |
| 战场不使用Scene2D | 战场使用游戏世界viewport，Scene2D使用UI坐标系，混合会导致坐标转换混乱 |
| 输入处理器按优先级排序 | 热键>UI>世界输入，避免功能冲突 |
| 事件语义明确区分点击/购买/拖拽 | 避免实现歧义 |

---

## Resolved Questions

1. ~~**事件总线实现方式**~~ - **决定：复用现有 `ModelHolder` 模式，与 DamageEventListener 设计一致**
2. ~~**逐步迁移策略**~~ - **决定：按Phase1-3分阶段迁移**
3. ~~**组件生命周期**~~ - **决定：在show()中创建，在dispose()中销毁**
4. ~~**性能考虑**~~ - **决定：暂不关注，先实现功能，有性能问题再优化**

---

## Migration Strategy

建议分3个阶段进行重构：

### Phase 1: 输入处理分离
- 提取 `GameInputHandler`
- 将 `handleInput()` 逻辑迁移到独立类
- **验证**: 输入事件能正确触发原有逻辑，拖拽功能正常

### Phase 2: UI Scene2D化
- 提取 `GameUIManager`
- 用 Scene2D Table 替代硬编码布局
- 保留现有渲染逻辑
- **验证**: UI布局与原版一致，按钮点击正常，卡牌显示正常

### Phase 3: 逻辑组件分离
- 提取 `BattleManager` 和 `ShopManager`
- 引入游戏事件系统（复用 `ModelHolder` 模式）
- 简化 GameScreen 为调度容器
- **验证**: 商店功能、战斗功能、卡组功能全部正常，GameScreen行数显著降低
