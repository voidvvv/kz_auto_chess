# KzAutoChess 游戏架构文档

> 项目自走棋游戏架构分析与运行流程说明
> 更新时间：2026-03-15

---

## 目录

- [一、整体架构概览](#一整体架构概览)
- [二、运行流程分析](#二运行流程分析)
- [三、GameScreen 核心架构](#三gamescreen-核心架构)
- [四、战斗更新逻辑](#四战斗更新逻辑)
- [五、渲染逻辑](#五渲染逻辑)
- [六、事件系统](#六事件系统)
- [七、核心设计模式](#七核心设计模式)
- [八、关键类职责总结](#八关键类职责总结)
- [九、目录结构](#九目录结构)

---

## 一、整体架构概览

KzAutoChess 是基于 **LibGDX** 框架开发的自走棋游戏，采用 **DDD (领域驱动设计)** 思想，严格遵循 `model/updater/manager/render` 分离原则。

### 技术栈

| 组件 | 技术/框架 | 版本 |
|------|----------|------|
| JDK | OpenJDK | 25 |
| 游戏引擎 | LibGDX | 1.14.0 |
| ECS框架 | Ashley | 内置 |
| 物理引擎 | Box2D | 内置 |
| 文本渲染 | FreeType | 内置 |
| AI框架 | LibGDX AI | 内置 |

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                     KzAutoChess (Game)                      │
│                   ┌─────────────────────┐                    │
│                   │   ViewManagement   │  ── Viewport管理   │
│                   └─────────────────────┘                    │
│                   ┌─────────────────────┐                    │
│                   │  SpriteBatch      │  ── 批量渲染       │
│                   └─────────────────────┘                    │
└─────────────────────────┬─────────────────────────────────────┘
                          │ setScreen()
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Screen 体系                             │
│  ┌──────────┐      ┌──────────────┐    ┌───────────────┐ │
│  │StartScreen│  →   │LevelSelect   │ →  │ GameScreen    │ │
│  └──────────┘      │   Screen     │    │               │ │
│                    └──────────────┘    │  主游戏界面    │ │
│                                        └───────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、运行流程分析

### 2.1 启动流程 (KzAutoChess.java)

```
create()
  ├─ FontUtils.init()              // 初始化字体系统
  │       └─ 加载系统字体: C:\Windows\Fonts\msyh.ttc
  │
  ├─ I18N.init()                  // 初始化国际化
  │       └─ 加载多语言配置
  │
  ├─ viewManagement.create()        // 创建Viewport
  │       ├─ uiViewport (屏幕坐标)
  │       └─ gameViewport (游戏世界: 480x600)
  │
  └─ setScreen(StartScreen)        // 进入开始界面

render()
  └─ super.render()               // 委托给当前Screen
```

### 2.2 Screen 切换流程

```
StartScreen (开始界面)
    └─ 点击"开始游戏"按钮
        ▼
LevelSelectScreen (关卡选择)
    └─ 选择关卡 (Level 1-5)
        ▼
GameScreen (游戏主界面)
    ├─ PLACEMENT阶段: 玩家放置/移动棋子
    ├─ BATTLE阶段: 自动战斗
    └─ 点击"返回" → LevelSelectScreen
```

### 2.3 游戏阶段 (GamePhase)

```java
public enum GamePhase {
    PLACEMENT,    // 放置阶段：玩家在己方区域放置/移动棋子
    BATTLE        // 战斗阶段：双方棋子自动战斗，玩家不可操作
}
```

---

## 三、GameScreen 核心架构

### 3.1 GameScreen 组装过程 (show() 方法)

```
┌─────────────────────────────────────────────────────────────────┐
│  GameScreen.show() - 核心组装逻辑                            │
└─────────────────────────────────────────────────────────────────┘
        │
        ├── 1. initGameData()         // 初始化共享数据模型
        │       ├─ CardPool          // 卡池
        │       ├─ CardShop         // 商店
        │       ├─ PlayerDeck       // 玩家手牌
        │       ├─ PlayerEconomy    // 玩家经济(金币)
        │       ├─ SynergyManager   // 协同管理器
        │       └─ Battlefield      // 战场
        │
        ├── 2. initRenderResources()   // 初始化渲染资源
        │       ├─ ShapeRenderer
        │       └─ CameraController
        │
        ├── 3. loadTiledResources()    // 加载地图资源
        │       └─ TmxMapLoader 加载 .tmx 地图文件
        │
        ├── 4. assembleArchitecture()   // ★★★ 核心：组装新架构 ★★★
        │       │
        │       ├─ GameEventSystem           // 事件系统
        │       │       ├─ GameEventHolder        // 事件队列
        │       │       ├─ GameEventListenerHolder // 监听器集合
        │       │       └─ GameEventDispatcher   // 分发器
        │       │
        │       ├─ BattleContext            // 战斗上下文(不可变)
        │       │       ├─ battlefield
        │       │       ├─ phase
        │       │       ├─ playerEconomy
        │       │       ├─ synergyManager
        │       │       └─ roundNumber
        │       │
        │       ├─ BattleState             // 战斗状态(可变)
        │       │       └─ currentContext + eventSystem
        │       │
        │       ├─ BattleManager           // 战斗管理器
        │       │       ├─ BattlePhaseManager       // 战斗阶段管理
        │       │       ├─ CharacterLifecycleManager // 角色生命周期
        │       │       └─ BehaviorTreeManager     // 行为树管理
        │       │
        │       ├─ EconomyManager          // 经济管理器
        │       ├─ CardManager            // 卡牌管理器
        │       ├─ RenderCoordinator      // 渲染协调器
        │       └─ GameInputHandler       // 输入处理器
        │
        │       └─ AutoChessGameMode      // ★ 游戏模式协调器 ★
        │               └─ 协调所有Manager
        │
        └── 5. setupInput()             // 配置输入处理
                └─ InputMultiplexer (UI优先 → 游戏输入 → 全局快捷键)
```

### 3.2 主循环 (render(float delta))

```
┌─────────────────────────────────────────────────────────────────┐
│  GameScreen.render(delta)                                     │
└─────────────────────────────────────────────────────────────────┘
        │
        ├── 1. gameMode.update(delta)            // 游戏逻辑更新
        │       │
        │       ├── eventSystem.dispatch()        // 分发所有事件
        │       │       └─ 遍历监听器回调
        │       │
        │       ├── eventSystem.clear()          // 清空事件队列
        │       │
        │       ├── battleManager.update(delta)     // 战斗更新
        │       │       └─ 见第四章详细说明
        │       │
        │       ├── economyManager.update(delta)   // 经济更新
        │       │       └─ 金币增长/利息计算
        │       │
        │       ├── cardManager.update(delta)      // 卡牌更新
        │       │       └─ 商店刷新等
        │       │
        │       └── inputHandler.update(delta)   // 输入更新
        │               └─ 拖放状态更新
        │
        └── 2. gameMode.render()                  // 游戏渲染
                │
                └── renderCoordinator.renderAll()   // 渲染所有
                        │
                        └─ 遍历 GameRenderer.render()
```

---

## 四、战斗更新逻辑

### 4.1 战斗阶段管理 (BattlePhaseManager)

```
startBattle()
  ├─ transitionTo(GamePhase.BATTLE)       // 切换到战斗阶段
  ├─ 生成敌人 (LevelEnemyConfig)
  │       └─ getEnemyCardIdsForLevel() + spawnEnemiesInBattlefield()
  │
  ├─ 角色加载 + 初始化
  │       ├─ characterLifecycle.loadCharacter()
  │       │       ├─ 创建 BattleUnitBlackboard
  │       │       ├─ 创建 BehaviorTree
  │       │       └─ 注册到 bbList
  │       └─ c.enterBattle()
  │
  └─ postEvent(BattleStartEvent)

updateBattle(delta)
  ├─ battleTime += delta               // 累加战斗时间
  │
  ├─ behaviorTreeManager.update()      // ★ 行为树更新 ★
  │       └─ 遍历所有角色的行为树 step()
  │               └─ 每个树: 寻敌 → 移动 → 攻击
  │
  ├─ projectileManager.update()        // 投掷物更新
  │       └─ 更新投掷物位置，检测碰撞
  │
  ├─ battleCharacterUpdater.update()   // ★ 角色更新 ★
  │       ├─ effectManager.updateEffects() // 移动效果更新
  │       ├─ movementCalculator.calculateTotalMove() // 计算总移动
  │       └─ 应用移动向量到位置
  │
  ├─ synergyManager.applySynergyEffects() // 协同效果应用
  │
  ├─ battleUpdater.update()           // ★ 伤害结算 ★
  │       └─ 遍历所有 DamageEvent
  │               ├─ damageSettlement()    // 计算伤害值
  │               │       ├─ 物理: max(1, 攻 - 防/2)
  │               │       ├─ 魔法: max(1, 攻 - 防/4)
  │               │       └─ 真实: 攻 (忽略防御)
  │               ├─ 更新 HP
  │               └─ 调用监听器回调
  │
  └─ 判断战斗结束
          ├─ 玩家方全灭 OR 敌方全灭
          └─ endBattle()

endBattle()
  ├─ 判定胜负
  ├─ 移除所有敌人
  │       └─ characterLifecycle.unloadCharacter() + remove()
  │
  ├─ 角色退出战斗状态
  │       └─ c.exitBattle() + c.reset()
  │
  ├─ 清理行为树和黑板
  │       └─ behaviorTreeManager.clear() + bbList.clear()
  │
  ├─ transitionTo(GamePhase.PLACEMENT)   // 切回放置阶段
  └─ postEvent(BattleEndEvent)
```

### 4.2 行为树逻辑 (UnitBehaviorTreeFactory)

每个角色的行为树结构：

```
Sequence (顺序执行)
  │
  ├── FindEnemyTask       // 1. 寻找敌人
  │       ├─ 获取所有敌方角色
  │       ├─ 计算距离，选择最近目标
  │       └─ 设置 bb.target
  │
  ├── MoveToEnemyTask    // 2. 移动向敌人
  │       ├─ 计算目标向量
  │       ├─ 设置移动速度
  │       └─ 碰撞检测避免重叠
  │
  └── AttackTargetTask   // 3. 攻击目标
          ├─ 检查攻击冷却
          ├─ 检查攻击范围
          └─ 发送攻击消息
```

### 4.3 状态机逻辑 (StateMachine)

每个角色有自己的状态机：

```
States:
  ├─ NORMAL_STATE      // 空闲状态
  ├─ MOVE_STATE        // 移动状态 (BaseMoveState)
  └─ ATTACK_STATE      // 攻击状态 (AttackState)

状态转换:
  NORMAL → ATTACK (收到攻击消息 & 冷却就绪)
  ATTACK → NORMAL (攻击动作结束)
  NORMAL → MOVE (有目标但距离远)
  MOVE → NORMAL (到达目标或目标改变)
```

状态机实现：
```java
public interface StateMachine<T> extends Telegraph {
    BaseState<T> getCurrent();
    void switchState(BaseState<T> next);      // 受守卫约束
    void forceSwitch(BaseState<T> next);      // 强制切换
    void setInitialState(BaseState<T> state);
    void update(float delta);
}
```

### 4.4 伤害系统流程

```
1. 攻击者调用 AttackTargetTask
        ↓
2. 发送攻击消息 (MessageConstants.attack)
        ↓
3. 黑板接收消息 → onMessageAttack()
        ↓
4. 状态机切换到 ATTACK_STATE
        ↓
5. AttackState 执行攻击动作
        │
        ├─ 设置 couldDamage = true
        ├─ 播放攻击动画
        └─ 发送 doAttack 消息
                ↓
6. 黑板接收消息 → onMessageDoAttack()
        ↓
7. 根据攻击者类型判断攻击方式:
        │
        ├─ 近战 (WARRIOR, TANK, ASSASSIN)
        │       └─ createDirectDamage()
        │               └─ 直接创建 DamageEvent
        │
        └─ 远程 (ARCHER, MAGE)
                └─ createProjectile()
                        ├─ 创建 Projectile
                        │       ├─ ARROW (弓箭手)
                        │       └─ MAGIC_BALL (法师)
                        └─ ProjectileManager 管理
                                └─ 投掷物飞行 + 碰撞检测
                                        ↓
                                        创建 DamageEvent
        ↓
8. DamageEvent 添加到 DamageEventHolder
        ↓
9. BattleUpdater.update() 处理所有 DamageEvent
        │
        ├─ damageSettlement()  // 计算伤害值
        ├─ 更新 defender.currentHp
        ├─ 调用监听器 onDamageEvent()
        └─ 调用监听器 postDamageEvent()
                │
                ├─ DamageRenderListener    // 渲染伤害数字
                ├─ DamageSettlementListener // 伤害结算
                └─ 其他监听器
```

### 4.5 魔法值与技能系统

```
ManaComponent (魔法值组件)
  ├─ currentMana  // 当前魔法值
  ├─ maxMana      // 上限 (因职业而异)
  ├─ regenRate    // 每秒恢复量
  └─ attackGain   // 每次攻击获得量

魔法值更新 (每帧):
  ├─ 时间恢复: currentMana += regenRate * delta
  ├─ 上限限制: currentMana = min(currentMana, maxMana)
  └─ 攻击获得: currentMana += attackGain (攻击时)

技能释放:
  ├─ 判断条件:
  │       ├─ mana.isFull()
  │       └─ stateMachine.getCurrent() == NORMAL_STATE
  │
  └─ 执行: skill.cast(blackboard)
          ├─ 技能逻辑执行
          ├─ mana.reset()      // 清零魔法值
          └─ 返回释放结果
```

---

## 五、渲染逻辑

### 5.1 渲染架构

```
RenderCoordinator (渲染协调器)
  └─ addRenderer(GameRenderer)
          │
          ├── BattleManager (实现 GameRenderer)
          │       │
          │       └─ render(RenderHolder)
          │               ├─ BattleFieldRender    // 战场背景
          │               ├─ BattleCharacterRender // 角色
          │               ├─ ProjectileRenderer   // 投掷物
          │               └─ DamageLineRender    // 伤害线
          │
          └─ 其他 Manager (如需)
```

### 5.2 渲染顺序 (BattleManager.render())

```
1. 应用 GameViewport (游戏世界坐标: 480x600)
2. BattleFieldRender.render()      // 绘制战场背景
        ├─ 玩家区域 (下方，蓝色)
        ├─ 敌方区域 (上方，红色)
        └─ 分界线
3. ProjectileRenderer.render()      // 绘制投掷物
        ├─ 箭头 (绿色)
        └─ 魔法球 (紫色)
4. DamageLineRender.render()        // 绘制伤害线 (调试用)
5. SpriteBatch - 绘制角色纹理
        ├─ 角色主体
        └─ 魔法值条
6. ShapeRenderer - 绘制角色调试信息
        ├─ 碰撞框
        ├─ 攻击范围
        └─ 面朝方向
7. SpriteBatch - 绘制状态文字
        └─ [状态名]:当前时间
8. flush()                         // 刷新渲染缓冲区
```

### 5.3 Viewport 管理

```
ViewManagement
  │
  ├── uiViewport (ScreenViewport)
  │       ├─ 使用屏幕坐标
  │       └─ 用于: 按钮、商店、卡牌等UI元素
  │
  └── gameViewport (StretchViewport)
          ├─ 固定世界大小: 480x600
          ├─ 自动缩放适应屏幕
          └─ 用于: 战场、角色、投掷物等游戏对象

坐标转换:
  ├─ screenToUI(screenX, screenY)      // 屏幕→UI坐标
  └─ screenToWorld(screenX, screenY)   // 屏幕→世界坐标
```

---

## 六、事件系统

### 6.1 事件系统架构

```
GameEventSystem (事件系统)
  │
  ├── GameEventHolder        // 事件队列 (List<GameEvent>)
  ├── GameEventListenerHolder // 监听器集合 (List<GameEventListener>)
  └─ GameEventDispatcher    // 分发器

事件流程:
  postEvent(event) → eventHolder.addModel()
      ↓
  dispatch() → 遍历所有监听器 → listener.onGameEvent(event)
      ↓
  clear() → eventHolder.clear()
```

### 6.2 事件类型

| 事件类 | 用途 | 触发时机 |
|--------|------|---------|
| `BattleStartEvent` | 战斗开始 | startBattle() |
| `BattleEndEvent` | 战斗结束 | endBattle() |
| `PhaseTransitionEvent` | 阶段切换 | transitionTo() |
| `ShopCardClickedEvent` | 商店卡牌点击 | UI点击 |
| `DragStartedEvent` | 拖拽开始 | 开始拖拽 |
| `DragMovedEvent` | 拖拽移动 | 拖拽中 |
| `DroppedEvent` | 放置拖拽 | 释放 |
| `DragCancelledEvent` | 取消拖拽 | 取消 |

### 6.3 伤害事件系统

```
DamageEventHolder
  └─ List<DamageEvent>   // 伤害事件队列

DamageEventListenerHolder
  └─ List<DamageEventListener>  // 监听器

DamageEvent
  ├─ from: BattleCharacter    // 攻击者
  ├─ to: BattleCharacter      // 防御者
  ├─ damage: Damage         // 伤害值和类型
  └─ extra: Object          // 附加信息

DamageEventListener
  ├─ onDamageEvent(event)    // 伤害前回调
  └─ postDamageEvent(event)  // 伤害后回调

实现:
  ├─ DamageRenderListener     // 渲染伤害数字
  ├─ DamageSettlementListener // 伤害结算
  └─ ...其他监听器
```

---

## 七、核心设计模式

| 模式 | 应用场景 | 示例 |
|------|---------|------|
| **Mediator (中介者)** | Manager间通信 | `GameEventSystem` 作为中介者解耦各Manager |
| **Observer (观察者)** | 事件监听 | `GameEventListener`, `DamageEventListener` |
| **State (状态模式)** | 角色状态机 | `StateMachine` + `States` |
| **Behavior Tree (行为树)** | AI决策 | `UnitBehaviorTreeFactory` 创建角色AI |
| **Blackboard (黑板)** | 行为树数据共享 | `BattleUnitBlackboard` 持有角色状态和战场引用 |
| **Strategy (策略模式)** | 渲染策略 | `GameRenderer` 接口，各Manager实现不同渲染 |
| **Builder (建造者)** | 上下文构建 | `BattleContext.Builder` |
| **Factory (工厂)** | 行为树创建 | `UnitBehaviorTreeFactory` |
| **Singleton (单例)** | 状态实例 | `NormalState.instance`, `AttackState.INSTANCE` |
| **Holder (持有者)** | 模型管理 | `ModelHolder<T>` 统一管理模式集合 |

---

## 八、关键类职责总结

| 类 | 职责 | 关键方法 |
|---|---|------|
| `KzAutoChess` | 游戏入口，管理全局资源 | `create()`, `render()`, `dispose()` |
| `ViewManagement` | Viewport和Camera管理 | `create()`, `update()`, `screenToWorld()` |
| `StartScreen` | 开始界面 | `render(float)` |
| `LevelSelectScreen` | 关卡选择界面 | `render(float)` |
| `GameScreen` | Screen生命周期，组装架构 | `show()`, `render(float)` |
| `AutoChessGameMode` | 中央协调器，协调各Manager | `update()`, `render()`, `buyCard()` |
| `BattleManager` | 战斗生命周期、角色管理、行为树 | `startBattle()`, `update()`, `placeCharacter()` |
| `BattleState` | 战斗状态（可变） | `transitionTo()`, `getContext()`, `nextRound()` |
| `BattleContext` | 战斗上下文（不可变） | Builder构建 |
| `GameEventSystem` | 事件分发系统 | `registerListener()`, `postEvent()`, `dispatch()` |
| `BattleUnitBlackboard` | 角色行为树黑板（状态+技能+碰撞） | `update()`, `handleMessage()` |
| `BattleCharacter` | 战斗角色模型 | `enterBattle()`, `exitBattle()` |
| `Battlefield` | 战场模型 | `placeCharacter()`, `getCharacters()` |
| `CardPool` | 卡池 | `getAllCards()`, `getCardById()` |
| `CardShop` | 卡牌商店 | `refresh()`, `getAvailableCards()` |
| `PlayerDeck` | 玩家手牌 | `addCard()`, `removeCard()` |
| `PlayerEconomy` | 玩家经济 | `addGold()`, `spendGold()` |
| `SynergyManager` | 协同效果管理 | `applySynergyEffects()` |
| `RenderCoordinator` | 渲染协调器 | `addRenderer()`, `renderAll()` |
| `BattleFieldRender` | 战场渲染器 | `render(Battlefield)` |
| `BattleCharacterRender` | 角色渲染器 | `render(SpriteBatch, character)` |
| `ProjectileRenderer` | 投掷物渲染器 | `render(ProjectileManager)` |
| `DamageLineRender` | 伤害线渲染器 | `render(shapeRenderer, spriteBatch)` |
| `BattleUpdater` | 战斗更新（伤害结算） | `update(float)`, `damageSettlement()` |
| `BattleCharacterUpdater` | 角色更新（位置移动） | `update(character, delta)` |
| `MovementCalculator` | 移动计算器 | `calculateTotalMove()` |
| `MovementEffectManager` | 移动效果管理器 | `updateEffects()`, `applyEffect()` |
| `CollisionDetector` | 碰撞检测器 | `checkCharacterCollision()` |
| `UnitBehaviorTreeFactory` | 行为树工厂 | `create(blackboard)` |
| `FindEnemyTask` | 寻敌任务 | `execute()` |
| `MoveToEnemyTask` | 移向敌人任务 | `execute()` |
| `AttackTargetTask` | 攻击目标任务 | `execute()` |

---

## 九、目录结构

```
kz_auto_chess/
├── core/src/main/java/com/voidvvv/autochess/
│   ├── KzAutoChess.java                    # 游戏主类
│   ├── battle/                            # 战斗相关
│   │   ├── BattleContext.java              # 战斗上下文(不可变)
│   │   ├── BattleState.java               # 战斗状态(可变)
│   │   ├── BattleUnitBlackboard.java      # 角色黑板
│   │   ├── BattleTelegraph.java           # 战斗电报
│   │   ├── UnitBehaviorTreeFactory.java   # 行为树工厂
│   │   ├── FindEnemyTask.java            # 寻敌任务
│   │   ├── MoveToEnemyTask.java         # 移动任务
│   │   ├── AttackTargetTask.java         # 攻击任务
│   │   └── collision/                   # 碰撞检测
│   │       ├── CollisionContext.java
│   │       └── CollisionDetector.java
│   ├── event/                            # 事件系统
│   │   ├── GameEvent.java                # 事件基类
│   │   ├── GameEventSystem.java          # 事件系统
│   │   ├── GameEventHolder.java
│   │   ├── GameEventListener.java
│   │   ├── GameEventDispatcher.java
│   │   ├── GameEventListenerHolder.java
│   │   ├── PhaseTransitionEvent.java
│   │   ├── BattleStartEvent.java
│   │   ├── BattleEndEvent.java
│   │   ├── ShopCardClickedEvent.java
│   │   └── drag/                        # 拖放事件
│   │       ├── DragEvent.java
│   │       ├── DragStartedEvent.java
│   │       ├── DragMovedEvent.java
│   │       ├── DroppedEvent.java
│   │       └── DragCancelledEvent.java
│   ├── game/                             # 游戏模式
│   │   └── AutoChessGameMode.java        # 游戏模式协调器
│   ├── input/                            # 输入处理
│   │   ├── GameInputHandler.java          # 游戏输入处理器
│   │   └── InputContext.java
│   ├── listener/                         # 监听器
│   │   └── damage/                      # 伤害监听器
│   │       ├── DamageEventListener.java
│   │       ├── DamageRenderListener.java
│   │       └── DamageSettlementListener.java
│   ├── logic/                            # 逻辑层
│   │   ├── CharacterStatsLoader.java      # 角色属性加载器
│   │   ├── EconomyCalculator.java         # 经济计算器
│   │   ├── CardUpgradeLogic.java         # 卡牌升级逻辑
│   │   ├── SynergyManager.java           # 协同管理器
│   │   └── MovementCalculator.java       # 移动计算器
│   ├── manage/                           # 管理器层
│   │   ├── BattleManager.java            # 战斗管理器
│   │   ├── CardManager.java              # 卡牌管理器
│   │   ├── EconomyManager.java           # 经济管理器
│   │   ├── ProjectileManager.java        # 投掷物管理器
│   │   ├── ParticleSpawner.java         # 粒子生成器
│   │   ├── MovementEffectManager.java    # 移动效果管理器
│   │   ├── RenderDataManager.java        # 渲染数据管理器
│   │   └── CharacterRenderDataManager.java
│   ├── model/                            # 模型层
│   │   ├── GamePhase.java                # 游戏阶段枚举
│   │   ├── Card.java                    # 卡牌模型
│   │   ├── CardPool.java                # 卡池
│   │   ├── CardShop.java                # 商店
│   │   ├── PlayerDeck.java              # 玩家手牌
│   │   ├── PlayerEconomy.java           # 玩家经济
│   │   ├── BattleCharacter.java          # 战斗角色
│   │   ├── Battlefield.java             # 战场
│   │   ├── CharacterStats.java          # 角色属性
│   │   ├── Skill.java                  # 技能接口
│   │   ├── SkillType.java              # 技能类型
│   │   ├── Projectile.java             # 投掷物
│   │   ├── Damage.java                 # 伤害
│   │   ├── DamageEvent.java            # 伤害事件
│   │   ├── DamageShowModel.java        # 伤害显示模型
│   │   ├── LevelEnemyConfig.java        # 关卡敌人配置
│   │   ├── SynergyEffect.java         # 协同效果
│   │   ├── SynergyType.java          # 协同类型
│   │   ├── Particle.java             # 粒子
│   │   ├── MoveComponent.java        # 移动组件
│   │   ├── MovementEffect.java       # 移动效果
│   │   ├── MovementEffectType.java   # 移动效果类型
│   │   ├── ModelHolder.java         # 模型持有者
│   │   ├── battle/
│   │   │   ├── DamageEventHolder.java
│   │   │   ├── DamageEventListenerHolder.java
│   │   │   └── Damage.java
│   │   └── skill/
│   │       └── BasicSkill.java       # 基础技能
│   ├── msg/                             # 消息系统
│   │   ├── KZConsumer.java
│   │   ├── DefaultKZConsumer.java
│   │   └── MessageConstants.java
│   ├── render/                           # 渲染层
│   │   ├── GameRenderer.java             # 渲染器接口
│   │   ├── RenderCoordinator.java         # 渲染协调器
│   │   ├── RenderHolder.java             # 渲染持有者
│   │   ├── BaseRender.java               # 基础渲染器
│   │   ├── BattleFieldRender.java        # 战场渲染器
│   │   ├── BattleCharacterRender.java    # 角色渲染器
│   │   ├── TiledBattleCharacterRender.java
│   │   ├── ProjectileRenderer.java       # 投掷物渲染器
│   │   ├── DamageLineRender.java        # 伤害线渲染器
│   │   ├── ParticleSystem.java          # 粒子系统
│   │   ├── ManaBarRenderer.java         # 魔法条渲染器
│   │   └── CardRenderer.java           # 卡牌渲染器
│   ├── screens/                          # Screen界面层
│   │   ├── StartScreen.java             # 开始界面
│   │   ├── LevelSelectScreen.java        # 关卡选择界面
│   │   └── GameScreen.java             # 游戏主界面
│   ├── sm/                              # 状态机
│   │   ├── machine/
│   │   │   ├── StateMachine.java        # 状态机接口
│   │   │   ├── BaseStateMachine.java    # 基础状态机实现
│   │   │   └── StateChangeListener.java
│   │   ├── state/
│   │   │   ├── BaseState.java          # 状态基类
│   │   │   ├── AbstractState.java      # 抽象状态
│   │   │   ├── StateType.java
│   │   │   └── common/               # 通用状态
│   │   │       ├── States.java        # 状态常量
│   │   │       ├── NormalState.java   # 空闲状态
│   │   │       ├── MoveState.java     # 移动状态
│   │   │       └── AttackState.java   # 攻击状态
│   ├── ui/                               # UI组件
│   │   ├── GameUIManager.java           # 游戏UI管理器
│   │   ├── CardRenderer.java            # 卡牌渲染器
│   │   └── ShapeRendererHelper.java     # 形状渲染辅助
│   ├── updater/                          # 更新器层
│   │   ├── IUpdater.java                # 更新器接口
│   │   ├── MyFunction.java              # 函数接口
│   │   ├── BaseMyFunction.java          # 基础函数
│   │   ├── BattleUpdater.java           # 战斗更新器
│   │   ├── BattleCharacterUpdater.java  # 角色更新器
│   │   ├── DamageRenderUpdater.java     # 伤害渲染更新器
│   │   ├── ProjectileUpdater.java       # 投掷物更新器
│   │   └── ParticleSystemUpdater.java   # 粒子系统更新器
│   └── utils/                            # 工具类
│       ├── ViewManagement.java           # Viewport管理
│       ├── CameraController.java         # 摄像机控制
│       ├── FontUtils.java              # 字体工具
│       ├── I18N.java                  # 国际化
│       ├── RenderConfig.java            # 渲染配置
│       ├── CharacterRenderer.java       # 角色渲染工具
│       ├── CharacterCamp.java          # 角色阵营
│       ├── AutoChessController.java    # 游戏控制器
│       └── TiledAssetLoader.java      # Tiled资源加载器
├── assets/                              # 资源目录
│   ├── fonts/                           # 字体文件
│   ├── tiled/                           # 地图文件
│   └── ...
├── ARCHITECTURE.md                      # 本文档
├── CLAUDE.md                           # Claude开发指南
└── build.gradle                         # Gradle构建配置
```

---

## 附录

### A. 游戏流程图

```
┌──────────────┐
│   启动游戏   │
└──────┬───────┘
       ▼
┌──────────────┐
│  开始界面    │  StartScreen
└──────┬───────┘
       ▼ (点击开始)
┌──────────────┐
│  关卡选择    │  LevelSelectScreen
└──────┬───────┘
       ▼ (选择关卡)
┌──────────────────────┐
│   游戏主界面        │  GameScreen
└──────┬─────────────┘
       │
       ▼
┌──────────────────────┐
│  PLACEMENT阶段      │
│  - 购买卡牌         │
│  - 放置角色         │
│  - 移动角色         │
└──────┬─────────────┘
       ▼ (点击开始战斗)
┌──────────────────────┐
│   BATTLE阶段        │
│  - 行为树驱动AI    │
│  - 自动战斗         │
│  - 伤害结算         │
└──────┬─────────────┘
       ▼ (战斗结束)
┌──────────────────────┐
│   PLACEMENT阶段      │  (循环)
└──────────────────────┘
```

### B. 战斗循环图

```
┌─────────────────────────────────────────┐
│           战斗循环 (每帧)             │
└─────────────────────────────────────────┘
           │
           ├─► 行为树更新
           │      ├─ FindEnemy: 寻找敌人
           │      ├─ MoveToEnemy: 移动向目标
           │      └─ AttackTarget: 攻击目标
           │
           ├─► 投掷物更新
           │      └─ 更新位置 + 碰撞检测
           │
           ├─► 角色更新
           │      ├─ 效果生命周期更新
           │      └─ 位置更新
           │
           ├─► 协同效果应用
           │
           ├─► 伤害结算
           │      └─ 处理所有 DamageEvent
           │
           └─► 状态机更新
                  └─ 每个角色状态机更新
```

---

> **文档维护**: 本文档应随着项目代码更新而同步更新，确保架构描述与实际代码一致。
