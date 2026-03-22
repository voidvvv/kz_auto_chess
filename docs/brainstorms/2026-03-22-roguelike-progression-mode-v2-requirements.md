---
date: 2026-03-22
topic: roguelike-progression-mode-v2
---

# Roguelike 闯关模式需求文档 v2

## Problem Frame

当前游戏内容单调，玩家选择关卡后只能重复挑战同一关卡，敌人配置固定（通过 `LevelEnemyConfig` 硬编码），缺乏变化和挑战性。玩家需要一个难度递增、内容丰富的连续闯关模式来提升游戏的可玩性和重玩价值。

## Requirements

### 模式入口与流程

- **R1. 新增主界面按钮**：在 `StartScreen` 添加「闯关模式」按钮，与现有「开始游戏」按钮并列显示
- **R2. 模式生命周期**：创建新的 `RoguelikeGameMode` 实现 `GameMode` 接口，复用现有的 `BattleManager`、`EconomyManager`、`CardManager`
- **R3. 独立卡池**：每局 roguelike 游戏创建独立的 `SharedCardPool` 实例，不与关卡选择模式共享卡池状态
- **R4. 游戏流程**：
  1. 点击「闯关模式」→ 进入第1关备战界面
  2. 备战完毕→ 开始战斗
  3. 胜利→ 进入下一关备战界面
  4. 失败→ 游戏结束，显示结算界面
  5. 结算界面→ 返回主界面

### 关卡系统

- **R5. 关卡数量**：共 30 个关卡（可配置）
- **R6. 关卡类型**：
  - 普通关：第 1,2,4,6,7,9,11... 关
  - 事件关：第 3,8,13,18,23,28 关（战斗后触发随机事件）
  - Boss 关：第 5,10,15,20,25,30 关
- **R7. 难度曲线**：
  - **梯队渐进**：
    | 关卡区间 | 敌人梯队 |
    |----------|----------|
    | 1-6关 | T1 为主，少量 T2 |
    | 7-12关 | T1/T2 混合，少量 T3 |
    | 13-18关 | T2/T3 混合，少量 T4 |
    | 19-24关 | T3/T4 混合 |
    | 25-30关 | T4/T5 为主 |
  - **属性缩放**：相同卡牌的基础属性（生命、攻击力）随关卡线性增长，公式：`属性 = 基础属性 × (1 + (关卡数 - 1) × 0.05)`
  - 难度曲线仅作为参考，需要支持随机生成队伍阵容，保证每次游戏的敌人组合都不同
  
### 敌人生成系统

- **R8. 敌人池配置**：创建 JSON 配置文件 `roguelike_enemies.json`，定义每个梯队可用的敌人卡牌 ID
- **R9. 随机组队**：每关从对应梯队的敌人池中随机抽取 3-5 张卡牌生成敌人队伍
- **R10. Boss 配置**：
  - Boss 关只有 1-2 个敌人
  - Boss 使用更高等级的卡牌（如第 30 关使用 T5 卡牌）
  - Boss 属性额外增加 50%

### 随机事件系统

- **R11. 事件触**发时机**：在事件关战斗胜利后、进入下一关备战前触发
- **R12. 事件 UI**：显示事件标题、描述**和 3 个选项按钮，玩家必须选择一个
- **R13. 事件类型与示例**（当前版本仅包含正面/中性效果，架构支持扩展）：

  | 类型 | 示例事件 | 效果 |
  |------|----------|------|
  | 经济类 | 「富商的馈赠」 | 获得 10 金币 |
  | 经济类 | 「市场折扣」 | 下一次商店刷新免费 |
  | 战斗类 | 「训练有素」 | 所有己方单位攻击力 +10% |
  | 战斗类 | 「英勇护盾」 | 所有己方单位生命值 +10% |
  | 特殊类 | 「神秘商人」 | 随机获得一张 T3 卡牌 |
  | 特殊类 | 「羁绊祝福」 | 随机激活一个羁绊效果 |
  | 特殊类 | 「快速成长」 | 立即获得 3 经验值（用于升级） |

- **R14. 事件效果持久化**：事件效果在整个 roguelike 游戏中持续生效

### 备战界面

- **R15. 进度显示**：显示「第 X 关 / 共 30 关」
- **R16. 下一关类型提示**：显示下一关是「普通关」、「Boss 关」还是「事件关」
- **R17. 敌人预览**：显示下一关敌人的卡牌缩略图和数量
- **R18. 商店功能**：复用现有商店系统，初始金币在 JSON 配置文件中定义（默认建议 5 金币）
- **R19. 活跃效果显示**：如果之前选择了随机事件，显示当前活跃的效果列表

### 战斗与奖励

- **R20. 胜利金币**：普通关胜利获得 5 金币，Boss 关胜利获得 10 金币
- **R21. 失败即结束**：战斗失败后游戏结束，无复活机制
- **R22. 死亡保留**：战斗中死亡的己方单位在下一关复活（但需重新购买卡牌）

### 结算界面

- **R23. 结算信息**：显示到达的关卡数、获得的事件效果记录
- **R24. 最高记录**：记录并显示玩家到达的最远关卡（持久化到本地文件）
- **R25. 返回主界面**：提供「返回主界面」按钮

## Success Criteria

- [ ] 玩家可以连续游玩 30 关，难度曲线平滑自然
- [ ] 每次游戏的敌人和事件组合都不同，提供高重玩价值
- [ ] 随机事件影响策略但不会导致不公平的失败
- [ ] Boss 战有挑战性但通过合理构筑可以战胜
- [ ] 代码易于扩展（新增关卡/事件/敌人配置只需修改 JSON）
- [ ] UI 响应流畅，备战信息清晰易读

## Scope Boundaries

### 包含

| 组件                        | 说明 |
|---------------------------|------|
| `RoguelikeGameMode`       | 新的 GameMode 实现 |
| `StageManager`            | 阶段/关卡管理器 |
| `RoguelikeEnemyPool`      | 敌人池配置加载器 |
| `RandomEventSystem`       | 随机事件管理器 |
| `RoguelikeUIManager`      | 闯关模式专用 UI 组件 |
| `RoguelikeGameOverScreen` | 结算界面 |
| 主界面修改                     | `StartScreen` 添加新按钮 |
| `RoguelikeScreen`           | 添加新的roguelikeScreen |

### 不包含

- 玩家数据云同步
- 成就系统集成
- 排行榜功能
- 卡牌平衡性调整
- 新的羁绊系统
- 多人联机


## Dependencies / Assumptions

- 现有的 `GameMode` 接口可以扩展
- 现有的 `BattleManager`、`EconomyManager`、`CardManager` 可以重用
- 玩家已熟悉自走棋核心玩法（商店、购卡、羁绊）
- `SharedCardPool.reset()` 方法可正常工作

## Key Decisions (Updated)

| 决策 | 理由 |
|------|------|
| 新增模式而非替换 | 保留现有玩法，给玩家选择自由度 |
| 硬核模式（失败即结束） | 符合 Roguelike 传统，简化实现，增强紧张感 |
| 每 5 关一个 Boss | 提供阶段性挑战目标，增强成就感 |
| 每 5 关触发一次事件（第 3,8,13...） | 避免事件过于频繁，保持战斗节奏 |
| 独立卡池 | 每局游戏从零开始，保证公平性 |
| JSON 配置化 | 无需重新编译即可调整平衡 |
| **事件关战斗与普通关相同** | 简化实现，事件只作为战斗后的奖励环节 |
| **事件系统可扩展设计** | 当前只有正面/中性事件，架构支持后续添加负面事件 |
| **初始金币可配置** | 在 JSON 配置文件中定义，便于平衡调整 |

## Outstanding Questions

### Deferred to Planning

- [Affects R7][Technical] 敌人属性缩放公式的平衡性调优
- [Affects R8][Technical] `roguelike_enemies.json` 的具体结构设计
- [Affects R13][Needs research] 随机事件的权重配置（哪些事件更常见）
- [Affects R19][UI] 活跃效果列表的 UI 布局设计

## Proposed Architecture

```
RoguelikeGameMode (实现 GameMode 接口)
├── StageManager              # 阶段管理器
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
├── RoguelikeUIManager        # 闯关模式UI
│   ├── renderProgress()
│   ├── renderNextStagePreview()
│   ├── renderActiveEffects()
│   └── renderEventDialog()
└── SharedCardPool            # 独立卡池实例
```

## Next Steps

→ `/ce:plan` 开始实现规划
