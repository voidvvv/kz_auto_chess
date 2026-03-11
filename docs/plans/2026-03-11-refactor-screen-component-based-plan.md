---
title: refactor: screen-component-based-refactoring
type: refactor
status: active
date: 2026-03-11
origin: docs/brainstorms/2026-03-11-screen-refactoring-brainstorm.md
---

# 重构：Screen类组件化重构

## 概述

将当前 945 行代码的 `GameScreen` 类重构为基于组件的架构，降低复杂度，提高可维护性和可扩展性。

---

## 问题陈述

### 当前GameScreen的问题

1. **职责过重** - 承担了商店、卡组、战斗、输入、渲染、UI布局等多个职责
2. **输入与渲染耦合** - `handleInput()` 在 `render()` 循环中被调用
3. **硬编码布局** - UI坐标散落在代码各处，难以适配不同屏幕尺寸
4. **Screen间架构不一致** - StartScreen/LevelSelectScreen 与 GameScreen 使用不同的UI框架

### Brainstorm中需要修正的设计问题

#### 问题1：事件分发器每帧创建新实例 ⚠️

**问题描述：**
Brainstorm 中 `render()` 方法每帧都创建新的 `GameEventDispatcher` 实例：

```java
@Override
public void render(float delta) {
    GameEventDispatcher dispatcher = new GameEventDispatcher(gameEventHolder, gameEventListenerHolder);
    dispatcher.dispatch();
    components.forEach(c -> c.update(delta));
    renderCoordinator.render(delta);
}
```

**专业分析：**
- **性能问题**：60fps 下每秒创建 60 个新对象，GC 压力巨大
- **设计缺陷**：EventDispatcher 应该是长期存在的组件，而非每帧创建
- **违反原则**：游戏开发中应避免在热路径（render 循环）中分配对象

**修正方案：**
在 `show()` 中创建 Dispatcher 实例，存储为成员变量，在 `render()` 中直接调用：

```java
public class GameScreen implements Screen {
    private GameEventDispatcher gameEventDispatcher;

    @Override
    public void show() {
        gameEventDispatcher = new GameEventDispatcher(gameEventHolder, gameEventListenerHolder);
        // ... 其他初始化
    }

    @Override
    public void render(float delta) {
        gameEventDispatcher.dispatch();  // 直接调用，不创建新对象
        components.forEach(c -> c.update(delta));
        renderCoordinator.render(delta);
    }
}
```

---

#### 问题2：拖拽事件类型不安全 ⚠️

**问题描述：**
Brainstorm 中使用 `Object` 类型存储拖拽对象：

```java
- `DragStarted(Object dragged, float x, float y)` - 拖拽开始
- `Dropped(Object dropped, float x, float y, String targetType)` - 放下拖拽对象
```

**专业分析：**
- **类型不安全**：Object 需要运行时类型检查或强制转换
- **容易出错**：可能传入错误的对象类型导致 ClassCastException
- **不符合 Java 最佳实践**：应使用具体类型或泛型

**修正方案：**
使用特定的事件类型：

```java
// 拖拽相关事件
public interface DragEvent extends GameEvent {
    enum DragTarget { BATTLEFIELD, DECK, SHOP, CANCEL }
    DragTarget getTargetType();
    float getX();
    float getY();
}

public class DragStartedEvent implements DragEvent {
    private final Object dragged;  // 保留 Object 以支持不同拖拽类型
    private final float x, y;
    // ... constructor, getters
}

public class DroppedEvent implements DragEvent {
    private final Object dropped;
    private final float x, y;
    private final DragTarget targetType;
    // ... constructor, getters
}

// UI相关事件
public class ShopCardClickedEvent implements GameEvent {
    private final Card card;
    // ...
}

public class ShopCardPurchasedEvent implements GameEvent {
    private final Card card;
    // ...
}
```

---

#### 问题3：组件间数据依赖未明确 ⚠️

**问题描述：**
Brainstorm 中 `RenderCoordinator` 需要访问 `BattleManager` 的渲染数据，但事件总线无法高效处理这种数据依赖。

**专业分析：**
- **数据耦合**：渲染需要访问 BattleManager 的实时状态
- **事件延迟**：通过事件传递数据可能导致同步问题
- **违反单一职责**：事件总线应处理事件，而非状态查询

**修正方案：**
明确组件间的数据依赖关系，使用以下方式之一：

**方案A：共享数据访问接口**
```java
// 定义数据提供者接口
public interface BattleDataProvider {
    List<BattleUnitBlackboard> getActiveUnits();
    List<DamageShowModel> getDamageShows();
    ProjectileManager getProjectiles();
}

// BattleManager 实现
public class BattleManager implements BattleDataProvider {
    // ... 实现接口方法
}

// RenderCoordinator 通过构造函数或setter获取数据提供者
public class RenderCoordinator {
    private BattleDataProvider battleDataProvider;

    public RenderCoordinator(BattleDataProvider battleDataProvider) {
        this.battleDataProvider = battleDataProvider;
    }
}
```

**方案B：保留直接引用（简单但耦合）**
如果数据访问非常频繁，可以保留直接引用，在文档中明确这种强依赖。

---

#### 问题4：拖拽状态管理未明确 ⚠️

**问题描述：**
Brainstorm 提到拖拽状态由 GameInputHandler 管理，但未明确与 UI 预览渲染的协调方式。

**专业分析：**
- **状态同步**：拖拽时需要同时更新输入状态和UI预览
- **生命周期**：拖拽取消时的清理逻辑未明确

**修正方案：**
在组件职责中明确：
```java
#### GameInputHandler
// ...（现有职责）
- 管理拖拽状态（draggingCard/draggingCharacter）
- 发布拖拽事件到 GameEventHolder（DragMoved, DragCancelled）

#### GameUIManager
// ...（现有职责）
- 监听拖拽事件（DragMoved, DragCancelled）
- 响应拖拽事件，更新拖拽预览显示
- 拖拽取消时清除预览
```

---

#### 问题5：GameScreen持有事件容器的设计问题 ⚠️

**问题描述：**
Brainstorm 中 GameScreen 创建并持有 `GameEventHolder` 和 `GameEventListenerHolder`，然后传递给所有组件：

```java
public class GameScreen implements Screen {
    private GameEventHolder gameEventHolder;
    private GameEventListenerHolder gameEventListenerHolder;

    public void show() {
        gameEventHolder = new GameEventHolder();
        gameEventListenerHolder = new GameEventListenerHolder();

        // 所有组件共享同一个Holder引用
        inputHandler = new GameInputHandler(game, gameEventHolder, gameEventListenerHolder);
        uiManager = new GameUIManager(game, gameEventHolder, gameEventListenerHolder);
        // ...
    }
}
```

**专业分析：**
- **设计不一致**：与现有 `DamageEventListenerHolder` 模式不同（Battlefield 直接持有 Holder，不由 Screen 传递）
- **责任混淆**：事件容器应该是组件内部管理还是由外部管理？
- **违反封装**：所有组件共享同一个 Holder 实例

**修正方案：**
复用现有 `Battlefield` 模式，每个需要事件的组件自己创建 Holder：

```java
// 方案A：每个组件自己创建Holder
public class BattleManager {
    private GameEventHolder eventHolder;
    private GameEventListenerHolder listenerHolder;

    public BattleManager() {
        this.eventHolder = new GameEventHolder();
        this.listenerHolder = new GameEventListenerHolder();
    }

    // 内部分发方法
    private void dispatchEvents() {
        List<GameEventListener> listeners = listenerHolder.getModels();
        for (GameEvent event : eventHolder.getModels()) {
            for (GameEventListener listener : listeners) {
                listener.onGameEvent(event);
            }
        }
        eventHolder.clear();
    }
}

// 方案B：使用集中式事件系统（推荐）
public class GameEventSystem {
    private final GameEventHolder eventHolder;
    private final GameEventListenerHolder listenerHolder;
    private final GameEventDispatcher dispatcher;

    public GameEventSystem() {
        this.eventHolder = new GameEventHolder();
        this.listenerHolder = new GameEventListenerHolder();
        this.dispatcher = new GameEventDispatcher(eventHolder, listenerHolder);
    }

    public void register(GameEventListener listener) {
        listenerHolder.addModel(listener);
    }

    public void unregister(GameEventListener listener) {
        listenerHolder.removeModel(listener);
    }

    public void postEvent(GameEvent event) {
        eventHolder.addModel(event);
    }

    public void dispatch() {
        dispatcher.dispatch();
    }
}

// GameScreen 持有单一事件系统
public class GameScreen implements Screen {
    private GameEventSystem gameEventSystem;

    public void show() {
        gameEventSystem = new GameEventSystem();
        // 各组件注册到 gameEventSystem
        inputHandler.register(gameEventSystem);
        uiManager.register(gameEventSystem);
        // ...
    }
}
```

---

## 提议的解决方案

### 架构设计

```
GameScreen (调度容器)
├── GameEventSystem (事件系统中心)
│   ├── GameEventHolder
│   ├── GameEventListenerHolder
│   └── GameEventDispatcher
├── GameInputHandler (输入处理)
├── GameUIManager (UI构建和布局)
├── BattleManager (战斗逻辑)
├── ShopManager (商店逻辑)
└── RenderCoordinator (渲染协调)
```

### 核心修正总结

| 问题 | 修正方案 |
|------|---------|
| 事件分发器每帧创建 | 在 `show()` 中创建一次，复用实例 |
| 拖拽事件类型不安全 | 使用特定事件类型（DragStartedEvent, DroppedEvent 等） |
| 组件间数据依赖未明确 | 引入 `BattleDataProvider` 接口或明确强依赖关系 |
| 拖拽状态管理未明确 | 在组件职责中明确拖拽事件监听和预览更新 |
| GameScreen持有事件容器 | 使用 `GameEventSystem` 中心化管理事件 |

---

## 技术实现方法

### 实现阶段

#### Phase 1: 输入处理分离
- 创建 `GameInputHandler` 类
- 实现 `InputProcessor` 接口
- 迁移 `handleInput()` 逻辑到独立类
- 拖拽状态内部管理
- 将输入转换为事件并发布到 `GameEventSystem`

#### Phase 2: UI Scene2D 化
- 创建 `GameUIManager` 类
- 使用 Scene2D `Table` 替代硬编码布局
- 监听游戏事件并更新 UI 状态
- 实现拖拽预览渲染（监听 DragMoved 事件）
- 保持战场使用游戏世界 viewport（不使用 Scene2D）

#### Phase 3: 逻辑组件分离
- 创建 `GameEventSystem` 事件系统中心
- 创建 `BattleManager` 类
- 创建 `ShopManager` 类
- 创建 `RenderCoordinator` 类（通过 `BattleDataProvider` 或直接引用获取数据）
- 简化 `GameScreen` 为纯调度容器

---

## 验收标准

### 功能要求
- [ ] 输入事件正确触发，拖拽功能正常
- [ ] UI 布局与原版一致，所有按钮可点击
- [ ] 商店功能正常（刷新、购买、升级）
- [ ] 战斗功能正常（开始、更新、结束）
- [ ] 卡组功能正常（显示、移除）
- [ ] 拖拽预览正确显示

### 质量要求
- [ ] `GameScreen` 行数降低 50% 以上（目标 <500 行）
- [ ] 各组件行数合理（<200 行/组件）
- [ ] 无每帧对象创建（render 循环中无 `new`）
- [ ] 事件类型安全（使用具体类型，非 `Object`）
- [ ] 遵循项目现有 ModelHolder 模式

---

## 系统影响分析

### 事件流图

```
输入事件 → GameInputHandler → GameEventSystem → [GameUIManager, BattleManager, ShopManager]
                                                                 ↓
                                                         RenderCoordinator
```

### 错误传播

- 输入错误：在 `GameInputHandler` 中捕获，不发布事件
- 事件监听器异常：捕获后记录，不影响其他监听器
- 组件初始化失败：记录并禁用该组件，不影响其他组件

### 状态生命周期风险

- `GameScreen` 显示时：所有组件初始化
- `GameScreen` 隐藏时：所有组件清理
- 拖拽中切换 Screen：`hide()` 中取消拖拽，清理状态

### API 一致性

| 接口 | 新设计 | 现有设计 |
|------|--------|---------|
| 事件注册 | `gameEventSystem.register(listener)` | `listenerHolder.addModel(listener)` |
| 事件发布 | `gameEventSystem.postEvent(event)` | `eventHolder.addModel(event)` |
| 事件分发 | `gameEventSystem.dispatch()` | `BattleUpdater` 内部分发 |

**一致性策略**：保持与现有 `ModelHolder` 模式的命名和调用方式一致。

---

## 依赖关系与风险

### 依赖关系

```
Phase 1 → Phase 2 → Phase 3（顺序依赖）

Phase 1: 输入处理
- 无新增依赖

Phase 2: UI Scene2D 化
- 依赖 Phase 1 完成
- 复用 `GameInputHandler` 的事件接口

Phase 3: 逻辑组件分离
- 依赖 Phase 1 和 Phase 2 完成
- 复用 `GameEventSystem` 和组件事件
```

### 风险分析

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| Scene2D 性能开销 | Table 布局可能增加 CPU 消耗 | 保持 Table 结构简单，避免嵌套 |
| 事件系统引入新概念 | 开发者需要学习 | 复用 ModelHolder 模式，保持一致 |
| 组件间通信延迟 | 事件传递可能慢于直接调用 | 对高频数据访问保留直接引用 |
| 回滚困难 | 重构范围较大 | 使用 git 分支，每阶段独立提交 |

---

## 成功指标

| 指标 | 目标 | 测量方式 |
|------|------|---------|
| `GameScreen` 行数 | < 500 行 | 代码统计 |
| 组件平均行数 | < 200 行/组件 | 代码统计 |
| 事件类型安全 | 100% | 代码审查 |
| 无每帧对象创建 | 0 个 | 性能分析（JProfiler） |
| 渲染帧率 | ≥ 60 FPS | 运行时测量 |

---

## 参考资料

### 来源
- **Brainstorm 文档**: [docs/brainstorms/2026-03-11-screen-refactoring-brainstorm.md](docs/brainstorms/2026-03-11-screen-refactoring-brainstorm.md)

**从 Brainstorm 携带的关键决策：**
1. 组件化重构方案（GameScreen 作为调度容器）
2. 复用现有 ModelHolder 模式实现事件系统
3. 战场不使用 Scene2D Table
4. 输入处理器按优先级排序（热键 > UI > 世界输入）
5. 分阶段迁移策略（Phase 1-3）

### 内部参考

| 文件 | 说明 |
|------|------|
| `screens/GameScreen.java:1-945` | 当前实现，待重构 |
| `model/ModelHolder.java` | 事件容器模式 |
| `listener/damage/DamageEventListener.java` | 领域事件接口 |
| `updater/BattleUpdater.java:23-42` | 事件分发参考 |
| `utils/ViewManagement.java` | Viewport 管理 |

### 相关工作

- 无相关 PR 或 Issue
