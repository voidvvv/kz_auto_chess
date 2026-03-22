---
title: Roguelike 闯关模式
type: feat
status: active
date: 2026-03-22
origin: docs/brainstorms/2026-03-22-roguelike-progression-mode-v2-requirements.md
---

# feat: Roguelike 闯关模式

## Overview

为 KzAutoChess 添加一个新的 Roguelike 连续闯关模式，玩家从第 1 关开始，挑战 30+ 个难度递增的关卡，包含随机敌人生成、随机事件和 Boss 战。

## Problem Statement / Motivation

当前游戏内容单调，玩家选择关卡后只能重复挑战同一关卡，敌人配置通过 `LevelEnemyConfig` 硬编码固定，缺乏变化和挑战性。玩家需要一个难度递增、内容丰富的连续闯关模式来提升游戏的可玩性和重玩价值 (see origin)。

## Proposed Solution

创建新的 `RoguelikeGameMode` 实现 `GameMode` 接口，复用现有的 `BattleManager`、`EconomyManager`、`CardManager`，添加以下新组件：

1. **StageManager** - 管理关卡进度和类型（普通/事件/Boss）
2. **RoguelikeEnemyPool** - 从 JSON 配置加载敌人池，随机生成敌人队伍
3. **RandomEventSystem** - 管理随机事件，提供 3 选 1 奖励选项
4. **ActiveEffectsManager** - 管理持久化的事件效果（属性加成等）
5. **RoguelikeScreen** - 新的专用屏幕，包含备战界面和事件选择 UI
6. **RoguelikeGameOverScreen** - 结算界面，显示到达关卡和最高记录

### 游戏流程

```
主界面 → 点击「闯关模式」 → RoguelikeScreen 第 1 关备战
    → 开始战斗 → 胜利 → 下一关备战
    → 胜利 → 事件关 → 触发随机事件（3 选 1）
    → 失败 → RoguelikeGameOverScreen → 返回主界面
```

## Technical Approach

### 架构设计

```
RoguelikeGameMode (实现 GameMode 接口)
├── StageManager              # 阶段/关卡管理器
│   ├── currentStage: int
│   ├── maxStages: int
│   ├── nextStage()
│   └── getStageType(): StageType (NORMAL/BOSS/EVENT)
├── RoguelikeEnemyPool        # 敌人池配置
│   ├── loadFromJson(String path)
│   └── getEnemiesForStage(stage, stageType): List<Integer>
├── RandomEventSystem         # 随机事件系统
│   ├── triggerEvent(): RandomEvent
│   ├── getEventChoices(): List<EventChoice>
│   └── applyEffect(EventChoice)
├── ActiveEffectsManager      # 活跃效果管理器
│   ├── activeEffects: List<ActiveEffect>
│   └── applyToCharacter(BattleCharacter)
└── SharedCardPool            # 独立卡池实例（每局游戏新建）
```

### 配置文件结构

#### `assets/roguelike_config.json`
```json
{
  "maxStages": 30,
  "initialGold": 5,
  "normalStageReward": 5,
  "bossStageReward": 10,
  "eventStages": [3, 8, 13, 18, 23, 28],
  "bossStages": [5, 10, 15, 20, 25, 30],
  "statScaling": 0.05
}
```

#### `assets/roguelike_enemies.json`
```json
{
  "tiers": {
    "1": {
      "cardIds": [140, 141, 142, 143],
      "minCount": 3,
      "maxCount": 4
    },
    "2": {
      "cardIds": [140, 141, 142, 143, 144],
      "minCount": 3,
      "maxCount": 4
    },
    "3": {
      "cardIds": [141, 142, 143, 144, 145],
      "minCount": 3,
      "maxCount": 5
    },
    "4": {
      "cardIds": [145, 146, 147, 148, 149],
      "minCount": 2,
      "maxCount": 4
    },
    "5": {
      "cardIds": [148, 149, 150, 151],
      "minCount": 1,
      "maxCount": 2
    }
  },
  "stageTierMap": {
    "1-6": 1,
    "7-12": 2,
    "13-18": 3,
    "19-24": 4,
    "25-30": 5
  }
}
```

#### `assets/roguelike_events.json`
```json
{
  "events": [
    {
      "id": "merchant_gift",
      "type": "ECONOMY",
      "title": "富商的馈赠",
      "description": "一位富商路过，决定赞助你的旅程。",
      "choices": [
        {
          "text": "接受 10 金币",
          "effect": {
            "type": "GOLD",
            "value": 10
          }
        }
      ]
    },
    {
      "id": "training_bonus",
      "type": "COMBAT",
      "title": "训练有素",
      "description": "你的部队经过额外训练，战斗力提升。",
      "choices": [
        {
          "text": "攻击力 +10%",
          "effect": {
            "type": "ATTACK_BOOST",
            "value": 0.1
          }
        },
        {
          "text": "生命值 +10%",
          "effect": {
            "type": "HEALTH_BOOST",
            "value": 0.1
          }
        }
      ]
    }
  ]
}
```

### 实现阶段

#### Phase 1: 基础架构

**任务:**
1. 创建 `RoguelikeGameMode` 类，实现 `GameMode` 接口
2. 创建 `StageManager` 类，管理关卡进度和类型判断
3. 创建 `RoguelikeScreen` 类，作为 Roguelike 模式的专用屏幕
4. 修改 `StartScreen`，添加「闯关模式」按钮
5. 创建 JSON 配置文件加载器

**验收标准:**
- [ ] 点击「闯关模式」可以进入 `RoguelikeScreen`
- [ ] 显示第 1 关备战界面
- [ ] 关卡进度正确显示（第 X 关 / 共 30 关）
- [ ] 下一关类型提示正确显示

#### Phase 2: 敌人池和随机生成

**任务:**
1. 创建 `RoguelikeEnemyPool` 类
2. 实现 `loadFromJson()` 方法加载敌人配置
3. 实现梯队映射和随机队伍生成
4. 实现属性缩放公式：`属性 = 基础属性 × (1 + (关卡数 - 1) × 0.05)`
5. 修改 `BattleManager.startBattle()` 支持 Roguelike 敌人注入

**验收标准:**
- [ ] 每关敌人从对应梯队随机生成
- [ ] 敌人属性随关卡递增
- [ ] Boss 关只有 1-2 个高属性敌人
- [ ] 每次游戏的敌人组合不同

#### Phase 3: 随机事件系统

**任务:**
1. 创建 `RandomEventSystem` 类
2. 创建 `RandomEvent` 和 `EventChoice` Model
3. 创建 `ActiveEffectsManager` 管理持久化效果
4. 创建事件选择 UI（3 个选项按钮）
5. 实现事件效果应用（金币、属性加成、免费卡牌等）
6. 创建事件相关事件类（`EventTriggeredEvent`, `EventChoiceSelectedEvent`）

**验收标准:**
- [ ] 事件关战斗胜利后触发事件
- [ ] 显示事件标题、描述和 3 个选项
- [ ] 选择选项后正确应用效果
- [ ] 事件效果在后续关卡中持续生效
- [ ] 备战界面显示活跃效果列表

#### Phase 4: 结算界面

**任务:**
1. 创建 `RoguelikeGameOverScreen` 类
2. 显示到达的关卡数
3. 显示获得的事件效果记录
4. 实现最高记录持久化到本地文件
5. 添加「返回主界面」按钮

**验收标准:**
- [ ] 失败后显示结算界面
- [ ] 正确显示到达关卡
- [ ] 最高记录正确保存和加载
- [ ] 点击返回主界面按钮正确导航

#### Phase 5: UI 优化和平衡调优

**任务:**
1. 完善备战界面 UI（进度、敌人预览、活跃效果）
2. 调整属性缩放公式平衡性
3. 调整事件权重配置
4. 添加音效和动画（可选）

**验收标准:**
- [ ] UI 响应流畅，信息清晰易读
- [ ] 难度曲线平滑自然
- [ ] Boss 战有挑战性但可战胜

## System-Wide Impact

### Interaction Graph

```
用户点击「闯关模式」
    → StartScreen.handleInput()
    → KzAutoChess.setScreen(new RoguelikeScreen(game))
    → RoguelikeScreen.show()
    → RoguelikeGameMode.onEnter()
        → StageManager.initialize()
        → SharedCardPool.reset()
        → eventSystem.registerListener(this)

用户点击「开始战斗」
    → RoguelikeGameMode.handleInput()
    → BattleManager.startBattle()
        → 从 RoguelikeEnemyPool 获取敌人列表
        → 应用属性缩放
        → 生成敌人到战场
    → 触发 BattleStartEvent

战斗胜利
    → BattleManager.endBattle()
    → 触发 BattleEndEvent(playerWon=true)
    → RoguelikeGameMode.onBattleEnd()
        → 如果是事件关 → RandomEventSystem.triggerEvent()
        → 否则 → StageManager.nextStage()

用户选择事件选项
    → RandomEventSystem.applyEffect(choice)
    → ActiveEffectsManager.addEffect(effect)
    → StageManager.nextStage()

战斗失败
    → BattleManager.endBattle()
    → 触发 BattleEndEvent(playerWon=false)
    → game.setScreen(new RoguelikeGameOverScreen(game))
```

### Error & Failure Propagation

| 错误场景 | 处理方式 |
|----------|----------|
| JSON 配置文件不存在 | 使用默认配置值，记录警告日志 |
| 敌人池为空 | 降级使用 T1 敌人，记录错误日志 |
| 事件效果应用失败 | 跳过该效果，继续游戏，记录错误日志 |
| 最高记录文件读写失败 | 使用内存中的记录，不影响游戏进行 |

### State Lifecycle Risks

| 状态 | 生命周期 | 清理机制 |
|------|----------|----------|
| `SharedCardPool` | 每局游戏新建 | `RoguelikeGameMode.onExit()` 清理 |
| `ActiveEffectsManager.activeEffects` | 整个 Roguelike 游戏 | `RoguelikeGameMode.onExit()` 清理 |
| `StageManager.currentStage` | 整个 Roguelike 游戏 | 每局游戏重置为 1 |
| 最高记录 | 跨游戏持久化 | 写入 `~/.kzautochess/roguelike_save.json` |

### API Surface Parity

| 接口 | 需要更新 | 共享代码路径 |
|------|----------|-------------|
| `GameMode` | ✅ 新增实现 | `RoguelikeGameMode` |
| `BattleManager.startBattle()` | ✅ 支持外部敌人列表 | 复用现有逻辑 |
| `GameUIManager` | ❌ 创建新的 `RoguelikeUIManager` | 独立实现 |
| `SharedCardPool` | ❌ 每局独立实例 | 复用现有类 |

### Integration Test Scenarios

1. **完整流程**: 从主界面进入 Roguelike 模式，连续游玩 5 关，包含 1 次事件触发，最后故意失败
2. **事件效果持久化**: 选择属性加成事件后，验证后续关卡中己方单位属性正确增加
3. **卡池独立性**: 在 Roguelike 模式中购买卡牌后退出，重新进入验证卡池已重置
4. **最高记录**: 完成第 10 关后退出，重新进入验证最高记录正确显示
5. **Boss 难度**: 到达第 5 关（第一个 Boss 关），验证 Boss 敌人数量和属性正确

## Acceptance Criteria

### 功能需求 (来自 origin document)

- [R1] **主界面按钮**: 在 `StartScreen` 添加「闯关模式」按钮，与现有「开始游戏」按钮并列显示
- [R2] **模式生命周期**: 创建新的 `RoguelikeGameMode` 实现 `GameMode` 接口
- [R3] **独立卡池**: 每局游戏创建独立的 `SharedCardPool` 实例
- [R4] **游戏流程**: 点击闯关模式 → 备战 → 战斗 → 胜利/失败 → 下一关/结算
- [R5] **关卡数量**: 共 30 个关卡（可配置）
- [R6] **关卡类型**: 普通关、事件关（第 3,8,13,18,23,28 关）、Boss 关（第 5,10,15,20,25,30 关）
- [R7] **难度曲线**: 梯队渐进 + 属性缩放公式，支持随机生成队伍
- [R8] **敌人池配置**: 创建 `roguelike_enemies.json`
- [R9] **随机组队**: 每关从对应梯队随机抽取 3-5 张卡牌
- [R10] **Boss 配置**: 1-2 个敌人，高等级卡牌，属性 +50%
- [R11] **事件触发时机**: 事件关战斗胜利后触发
- [R12] **事件 UI**: 显示标题、描述和 3 个选项按钮
- [R13] **事件类型**: 经济类、战斗类、特殊类（当前仅正面/中性效果，架构可扩展）
- [R14] **事件效果持久化**: 整个 Roguelike 游戏中持续生效
- [R15] **进度显示**: 显示「第 X 关 / 共 30 关」
- [R16] **下一关类型提示**: 显示普通关/Boss 关/事件关
- [R17] **敌人预览**: 显示下一关敌人的卡牌缩略图和数量
- [R18] **商店功能**: 复用现有商店系统，初始金币可配置
- [R19] **活跃效果显示**: 显示当前活跃的事件效果列表
- [R20] **胜利金币**: 普通关 5 金币，Boss 关 10 金币
- [R21] **失败即结束**: 无复活机制
- [R22] **死亡保留**: 战斗中死亡的己方单位在下一关复活（需重新购买）
- [R23] **结算信息**: 显示到达关卡数、事件效果记录
- [R24] **最高记录**: 持久化到本地文件
- [R25] **返回主界面**: 提供返回按钮

### 非功能需求

- [ ] **性能**: UI 渲染保持 60 FPS
- [ ] **可扩展性**: 新增关卡/事件/敌人配置只需修改 JSON
- [ ] **可维护性**: 遵循 Model/Updator/Manager/Render 分离原则

## Success Metrics

| 指标 | 目标 |
|------|------|
| 难度曲线平滑度 | 玩家可以连续游玩 30 关，无突然难度跳跃 |
| 重玩价值 | 每次游戏的敌人和事件组合都不同 |
| 扩展性 | 新增事件只需修改 JSON，无需改代码 |
| UI 响应 | 备战信息清晰易读，无卡顿 |

## Dependencies & Risks

### 依赖项

| 依赖 | 状态 | 风险等级 |
|------|------|----------|
| `GameMode` 接口扩展 | ✅ 现有 | 低 |
| `BattleManager` 复用 | ✅ 现有 | 低 |
| `EconomyManager` 复用 | ✅ 现有 | 低 |
| `CardManager` 复用 | ✅ 现有 | 低 |
| `SharedCardPool.reset()` | ✅ 现有 | 低 |
| LibGDX Scene2D | ✅ 现有 | 低 |

### 风险

| 风险 | 缓解措施 |
|------|----------|
| 属性缩放公式不平衡 | 配置化，可后期调整 |
| 事件效果过于强大/弱小 | 通过多次迭代测试平衡 |
| JSON 配置文件格式错误 | 添加验证逻辑，使用默认值 |
| 渲染性能下降（活跃效果多） | 限制最大效果数量，优化渲染 |

## Key Decisions (来自 origin document)

| 决策 | 理由 |
|------|------|
| 新增模式而非替换 | 保留现有玩法，给玩家选择自由度 |
| 硬核模式（失败即结束） | 符合 Roguelike 传统，简化实现，增强紧张感 |
| 每 5 关一个 Boss | 提供阶段性挑战目标，增强成就感 |
| 每 5 关触发一次事件（第 3,8,13...） | 避免事件过于频繁，保持战斗节奏 |
| 独立卡池 | 每局游戏从零开始，保证公平性 |
| JSON 配置化 | 无需重新编译即可调整平衡 |
| 事件关战斗与普通关相同 | 简化实现，事件只作为战斗后的奖励环节 |
| 事件系统可扩展设计 | 当前只有正面/中性事件，架构支持后续添加负面事件 |
| 初始金币可配置 | 在 JSON 配置文件中定义，便于平衡调整 |

## Sources & References

### Origin

- **Origin document:** [docs/brainstorms/2026-03-22-roguelike-progression-mode-v2-requirements.md](../brainstorms/2026-03-22-roguelike-progression-mode-v2-requirements.md)
- Key decisions carried forward:
  - 独立卡池模式
  - 事件关战斗与普通关相同
  - 事件系统仅包含正面/中性效果，架构可扩展
  - 初始金币通过 JSON 配置

### Internal References

| 类型 | 文件路径 | 用途 |
|------|----------|------|
| GameMode 接口 | `core/src/main/java/com/voidvvv/autochess/game/GameMode.java:1-55` | 实现 `RoguelikeGameMode` |
| GameMode 实现 | `core/src/main/java/com/voidvvv/autochess/game/AutoChessGameMode.java:48-153` | 参考生命周期实现 |
| 屏幕切换 | `core/src/main/java/com/voidvvv/autochess/screens/StartScreen.java:17-149` | 添加新按钮 |
| 敌人生成 | `core/src/main/java/com/voidvvv/autochess/model/LevelEnemyConfig.java:25-31` | 参考生成逻辑 |
| UI 管理 | `core/src/main/java/com/voidvvv/autochess/ui/GameUIManager.java:391-586` | 参考 UI 渲染模式 |
| 事件系统 | `core/src/main/java/com/voidvvv/autochess/event/GameEventSystem.java:14-60` | 分发事件 |
| 全局状态 | `core/src/main/java/com/voidvvv/autochess/KzAutoChess.java:75` | 管理全局状态 |
| 架构模式 | `.claude/docs/architectural_patterns.md` | Model/Updator/Manager/Render 分离 |
| 代码指南 | `.claude/skills/kz-autochess-code-guidelines/SKILL.md` | 类名匹配、生命周期集成 |

### Institutional Learnings

| 学习文档 | 关键洞察 |
|----------|----------|
| `docs/solutions/architecture/shared-card-pool-depletion.md` | 全局状态在 `KzAutoChess` 中管理，Manager 生命周期集成模式 |
| `docs/solutions/logic-errors/skill-effects-not-applied.md` | 临时效果追踪和过期清理模式 |

### Deferred Questions (来自 origin)

- [Affects R7][Technical] 敌人属性缩放公式的平衡性调优 → 在 Phase 5 调优阶段解决
- [Affects R8][Technical] `roguelike_enemies.json` 的具体结构设计 → 在 Phase 2 实现
- [Affects R13][Needs research] 随机事件的权重配置 → 在 Phase 5 调优阶段解决
- [Affects R19][UI] 活跃效果列表的 UI 布局设计 → 在 Phase 5 实现

### External References

无 - 此功能基于现有代码库模式实现，无需外部依赖。
