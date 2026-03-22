---
date: 2026-03-22
topic: roguelike-progression-mode
---

# Roguelike 闯关模式需求文档

## Problem Frame

当前游戏内容单调，玩家选择关卡后只能重复挑战同一关卡，敌人配置固定，缺乏变化和挑战性。玩家需要一个难度递增、内容丰富的连续闯关模式来提升游戏的可玩性和重玩价值。

## Requirements

### 核心玩法

- **R1. 模式入口**：在主界面添加「闯关模式」按钮，与现有「关卡选择」模式并存
- **R2. 连续阶段**：模式包含30+个连续阶段，玩家从第1关开始，逐个挑战
- **R3. 难度递增**：使用混合模式提升难度：
  - 梯队渐进：前期T1敌人为主，中期引入T2/T3，后期出现T4/T5
  - 属性缩放：相同敌人的属性（生命、攻击）随关卡线性增长
- **R4. 随机敌人生成**：每个阶段的敌人从配置池中随机选取，避免固定配置
- **R5. 金币奖励**：每关胜利后给予金币奖励，用于商店购卡
- **R6. 硬核模式**：失败即游戏结束，显示最终到达关卡，返回主界面
- **R7. 最终Boss**：每5关出现一个Boss关卡（第5/10/15/20/25/30关），Boss难度随关卡递增
- **R8. 随机事件**：每3关（第3/6/9/12...关）战斗后出现随机事件，玩家从3个选项中选择一个
- **R9. 事件效果**：随机事件提供增益（如金币、属性提升、商店折扣）或debuff（如金币减少、敌人强化）

### 随机事件类型

- **R10. 事件分类**：
  - 经济类：获得/失去金币，商店折扣
  - 战斗类：己方属性增益/敌方属性增益
  - 特殊类：免费刷新、免费卡牌、羁绊激活等

### 关卡进度

- **R11. 进度显示**：在备战界面显示当前关卡（如「第7关 / 共30关」）
- **R12. 下一关预告**：显示下一关是普通关、Boss关还是事件关
- **R13. 保存最高记录**：记录玩家到达的最远关卡

### Boss设计

- **R14. Boss特征**：
  - Boss关只有1-2个敌人，但属性远高于普通关
  - 每5个Boss为一个梯队，属性逐级提升
  - 最终Boss（第30关）使用T5卡牌+高额属性加成

## Success Criteria

- [ ] 玩家可以连续游玩30+关，难度曲线平滑
- [ ] 每次游戏的敌人生成都不同，提供重玩价值
- [ ] 随机事件影响策略但不会让玩家过早失败
- [ ] Boss战有挑战性但可战胜
- [ ] 代码易于扩展（新增关卡/事件/敌人配置）

## Scope Boundaries

### 包含
- 新的 RoguelikeGameMode 实现
- 阶段管理器（StageManager）
- 敌人生成池配置（JSON）
- 随机事件系统
- Boss配置系统
- 主界面新增「闯关模式」按钮
- 进度显示UI

### 不包含
- 玩家数据持久化（本地存档）
- 成就系统
- 排行榜
- 卡牌/羁绊调整（使用现有内容）

## Key Decisions

| 决策 | 理由 |
|------|------|
| 新增模式而非替换 | 保留现有玩法，给玩家选择 |
| 硬核模式（失败即结束） | 简化实现，符合Roguelike传统 |
| 每3关触发事件 | 平衡游戏节奏，避免过于频繁 |
| 每5关一个Boss | 提供阶段性目标，增强成就感 |
| 混合难度递增 | 兼顾内容多样性和数值成长 |

## Dependencies / Assumptions

- 现有的 `GameMode` 接口可以扩展
- 现有的 `BattleManager`、`EconomyManager`、`CardManager` 可以重用
- 玩家已熟悉自走棋核心玩法（商店、购卡、羁绊）

## Outstanding Questions

### Resolve Before Planning
（暂无）

### Deferred to Planning
- [Affects R3][Technical] 敌人属性缩放公式如何设计？
- [Affects R4][Technical] 敌人池配置的JSON结构？
- [Affects R9][Needs research] 随机事件的平衡性如何保证？

## Proposed Architecture

```
RoguelikeGameMode (实现 GameMode 接口)
├── StageManager              # 阶段管理器
│   ├── currentStage: int
│   ├── maxStages: int (30+)
│   └── nextStage()
├── EnemyPoolConfig           # 敌人池配置
│   ├── getEnemiesForStage(stage)
│   └── getBossForStage(stage)
├── RandomEventSystem         # 随机事件系统
│   ├── triggerEvent(stage)
│   └── applyEffect(choice)
└── RoguelikeUIManager        # 闯关模式UI
    ├── renderProgress()
    └── renderEventDialog()
```

## Next Steps
→ `/ce:plan` 开始实现规划
