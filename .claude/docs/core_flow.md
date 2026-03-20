# KZ AutoChess 核心流程

本文档描述游戏的核心流程，帮助新人理解系统运转方式。

## 游戏屏幕流转

```plantuml
@startuml
rectangle "StartScreen" as start
rectangle "LevelSelectScreen" as level
rectangle "GameScreen" as game
rectangle "GameOverScreen" as over

start --> level : 点击开始
level --> game : 选择关卡
game --> level : 关卡胜利
game --> over : 玩家死亡
over --> start : 返回主菜单
over --> level : 重新开始

@enduml
```

**屏幕职责**:
- `StartScreen` - 开始界面，初始化游戏
- `LevelSelectScreen` - 关卡选择，显示关卡进度
- `GameScreen` - 主游戏界面，协调 GameMode
- `GameOverScreen` - 游戏结束，显示结果

## 游戏主循环

```plantuml
@startuml

participant "GameScreen" as screen
participant "AutoChessGameMode" as mode
participant "GameEventSystem" as event
participant "BattleManager" as battle
participant "EconomyManager" as economy
participant "CardManager" as card

-> screen : render(delta)
activate screen

screen -> mode : update(delta)
activate mode

mode -> event : dispatch()
activate event
event -> event : 分发所有待处理事件
deactivate event

mode -> battle : update(delta)
mode -> economy : update(delta)
mode -> card : update(delta)

mode -> mode : render(holder)
mode -> battle : render(holder)

deactivate mode
deactivate screen

@enduml
```

**关键点**: 每帧先分发事件，再执行更新，最后渲染。

## 游戏阶段流转

```plantuml
@startuml

state "放置阶段\nPLACEMENT" as placement
state "战斗阶段\nBATTLE" as battle

[*] --> placement

placement --> battle : 开始战斗\nstartBattle()
battle --> placement : 战斗结束\nendBattle()

placement : 玩家可以:
placement : - 购买/出售卡牌
placement : - 放置/移动棋子
placement : - 刷新商店
placement : - 升级卡牌

battle : 自动战斗:
battle : - AI行为树控制
battle : - 状态机管理
battle : - 玩家不可操作

@enduml
```

## 战斗循环流程

```plantuml
@startuml

participant "BattlePhaseManager" as phase
participant "BehaviorTreeManager" as bt
participant "BattleUnitBlackboard" as bb
participant "StateMachine" as sm
participant "BattleCharacter" as char

-> phase : updateBattle(delta)
activate phase

phase -> phase : battleTime += delta

phase -> bt : update(battleTime)
activate bt

loop 每个存活单位
    bt -> bb : setCurrentTime(battleTime)
    bt -> bb : tree.step()
    activate bb

    bb -> bb : updateMana(delta)
    bb -> sm : update(delta)
    activate sm

    sm -> sm : 执行当前状态逻辑
    note right of sm
        NORMAL_STATE: 寻敌/移动
        ATTACK_STATE: 攻击动画
        DEAD_STATE: 死亡处理
    end note

    deactivate sm
    deactivate bb
end

deactivate bt
deactivate phase

@enduml
```

## 行为树结构

```plantuml
@startuml

rectangle "行为树根节点\nSelector" as root
rectangle "寻敌任务\nFindEnemyTask" as find
rectangle "移动任务\nMoveToEnemyTask" as move
rectangle "攻击任务\nAttackTargetTask" as attack

root --> find
root --> move
root --> attack

note right of find
  1. 寻找最近敌人
  2. 设置 target 到黑板
end note

note right of move
  1. 检查是否在攻击范围
  2. 移动向目标
end note

note right of attack
  1. 检查攻击冷却
  2. 切换到攻击状态
  3. 造成伤害
end note

note bottom of root
    每帧按顺序执行
    直到某个任务成功
end note

@enduml
```

**行为树工厂**: `UnitBehaviorTreeFactory.java:30-60`

## 伤害处理流程

```plantuml
@startuml

participant "攻击者" as attacker
participant "DamageEvent" as event
participant "DamageEventHolder" as holder
participant "BattleUpdater" as updater
participant "DamageEventListener" as listener
participant "BattleCharacter" as target

-> attacker : 攻击命中
activate attacker

attacker -> event : 创建 DamageEvent
attacker -> holder : addModel(event)
note right: 事件入队

deactivate attacker

-> updater : update(delta)
activate updater

updater -> holder : getModels()
activate holder
holder --> updater : 事件列表
deactivate holder

loop 每个事件
    updater -> listener : onDamageEvent(event)
    activate listener

    listener -> target : takeDamage(damage)
    activate target
    target -> target : hp -= damage
    target --> listener : 实际伤害
    deactivate target

    listener -> listener : 创建伤害显示模型
    listener -> listener : 触发死亡检测

    deactivate listener
end

deactivate updater

@enduml
```

## 购卡流程

```plantuml
@startuml

participant "玩家" as player
participant "GameUIManager" as ui
participant "AutoChessGameMode" as mode
participant "EconomyManager" as economy
participant "CardManager" as card
participant "SharedCardPool" as pool
participant "GameEventSystem" as event

-> player : 点击购买按钮
activate player

player -> ui : onBuyCardClicked(card)
activate ui

ui -> mode : buyCard(card)
activate mode

mode -> economy : canAfford(cost)
activate economy
economy --> mode : true/false
deactivate economy

alt 可以购买
    mode -> card : buyCard(card)
    activate card

    card -> pool : decrementCopies(cardId)
    activate pool
    pool --> card : true/false
    deactivate pool

    alt 卡池有货
        card -> card : 添加到手牌
        card --> mode : true

        mode -> economy : buyCard(cost)
        activate economy
        economy -> economy : gold -= cost
        deactivate economy

        mode -> event : postEvent(CardBuyEvent)
    else 卡池无货
        card --> mode : false
        mode --> ui : 购买失败
    end

    deactivate card
else 金币不足
    mode --> ui : 金币不足
end

deactivate mode
deactivate ui
deactivate player

@enduml
```

## 商店刷新流程

```plantuml
@startuml

participant "玩家" as player
participant "GameUIManager" as ui
participant "AutoChessGameMode" as mode
participant "EconomyManager" as economy
participant "CardManager" as card
participant "SharedCardPool" as pool
participant "CardPool" as cardPool

-> player : 点击刷新按钮
activate player

player -> ui : onRefreshClicked()
activate ui

ui -> mode : refreshShop()
activate mode

mode -> card : getRefreshCost()
activate card
card --> mode : 2金币
deactivate card

mode -> economy : payForRefresh(cost)
activate economy

alt 金币足够
    economy -> economy : gold -= cost
    economy --> mode : true
    deactivate economy

    mode -> card : refreshShop()
    activate card

    card -> pool : getAllAvailableCopies()
    activate pool
    pool --> card : 可用卡牌映射
    deactivate pool

    card -> card : 过滤可用数量>0的卡牌
    card -> card : 随机抽取5张
    card -> card : 更新商店显示

    deactivate card
else 金币不足
    economy --> mode : false
    deactivate economy
    mode --> ui : 刷新失败
end

deactivate mode
deactivate ui
deactivate player

@enduml
```

## 关键文件索引

| 流程 | 入口文件 | 关键方法 |
|------|----------|----------|
| 游戏启动 | `KzAutoChess.java:68-81` | `create()` |
| 屏幕切换 | `screens/*.java` | 各 Screen 类 |
| 战斗管理 | `BattleManager.java:59-161` | `BattlePhaseManager` 内部类 |
| 行为树 | `UnitBehaviorTreeFactory.java:30-60` | `create()` |
| 伤害系统 | `BattleUpdater.java:30-90` | `update()` |
| 经济系统 | `EconomyManager.java` | 全类 |
| 卡牌系统 | `CardManager.java` | 全类 |
| 卡池管理 | `SharedCardPool.java:16-119` | `decrementCopies()`, `incrementCopies()` |

## 数据流总结

```
用户输入
    ↓
GameInputHandler (input/GameInputHandler.java)
    ↓
AutoChessGameMode (game/AutoChessGameMode.java)
    ↓
┌─────────────────────────────────────────────────┐
│                  Manager 层                      │
├─────────────────────────────────────────────────┤
│ BattleManager ←→ EconomyManager ←→ CardManager │
│       ↓              ↓               ↓          │
│   Updator        Updator         Updator        │
│       ↓              ↓               ↓          │
│   Model          Model           Model          │
└─────────────────────────────────────────────────┘
    ↓
GameEventSystem (event/GameEventSystem.java)
    ↓
各 Manager 监听事件并响应
    ↓
RenderCoordinator → 渲染输出
```
