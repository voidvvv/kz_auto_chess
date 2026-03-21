---
date: 2026-03-19
topic: playability-enhancement
focus: 增强游戏可玩性
---

# Ideation: 增强游戏可玩性

## Codebase Context

**Project Shape:**
- LibGDX 1.14.0 auto-chess game (JDK 25) with Ashley ECS, Box2D, FreeType, gdx-ai
- Standard LibGDX layout: `core/` (game logic), `lwjgl3/` (desktop backend), `assets/`
- Strict model/updater/manager/render architectural separation enforced

**Existing Systems:**
- Battle System: Behavior tree AI (`UnitBehaviorTreeFactory`), state machine (Normal/Move/Attack), collision detection, projectile system
- Card System: CardPool (25 cards, 5 tiers), CardShop (refresh/buy), PlayerDeck, star-level upgrades
- Synergy System: 8 types (Warrior/Mage/Archer/Assassin/Tank/Dragon/Beast/Human) with tiered bonuses, 12 effect types
- Economy System: PlayerEconomy with gold, interest, win/lose streaks, GoldManager, RoundRewardCalculator
- Player Life System: Persistent HP across levels (`PlayerLifeBlackboard`), PlayerLifeManager, game over handling
- Skill System: HEAL/AOE/BUFF/DEBUFF fully implemented with SkillEffectManager
- Shared Card Pool: Depletion mechanism implemented (T1=15, T2=12, T3=9, T4=6, T5=3)
- Synergy Panel: UI requirements documented, implementation pending

**Recent Branches (2026-03-21):**
- `feat/card-pool-depletion` - 共享卡池耗尽机制
- `feat/synergy-panel` - 羁绊面板UI（进行中）

**Key Remaining Pain Points:**
1. Shallow synergy depth - only linear bonuses, no combo incentives
2. Economy opacity - players can't see income breakdown or pool availability
3. Passive battles - players only watch, no mid-battle agency
4. Limited strategic feedback - no decision quality indicators

**Past Learnings:**
- SynergyManager fully calculates 8 synergy types with thresholds (`SynergyManager.java:83-200`)
- EconomyManager has GoldManager and RoundRewardCalculator with extension points
- SkillEffectManager implements all 4 skill types (HEAL/AOE/BUFF/DEBUFF)
- Event-driven architecture enables loose coupling for new features

## Ranked Ideas

### 1. Synergy Combo System (羁绊链式反应系统)
**Description:** 当激活特定羁绊组合时，额外解锁隐藏效果，创造组合激励。例如：激活 WARRIOR(3) + MAGE(3) 同时触发"战斗法师"加成。

**Rationale:**
- 利用现有的 `SynergyManager`（8种羁绊类型，12种效果）进行极低成本扩展
- 玩家从"单羁绊追求"转向"羁绊组合构建"，指数级扩展策略空间
- 创造"组合激励"而非简单的"越多越好"，增加阵容构建深度

**Downsides:** 需要设计合理的组合规则和平衡数值，可能增加新手理解难度

**Evidence:**
- `SynergyManager.java:83-103` - applySynergyEffects 已遍历激活羁绊，添加组合检测仅需插入检查
- `SynergyManager.java:108-200` - 8个独立 apply 方法（Warrior/Mage/Archer/Assassin/Tank/Dragon/Beast/Human）
- 可在 SynergyType 枚举中直接添加 COMBO 类型定义组合规则

**Confidence:** 85%
**Complexity:** Medium
**Status:** Unexplored

---

### 2. Economy Transparency System (经济透明度系统)
**Description:** 在商店UI中显示：(1) 商店刷新成本趋势，(2) 回合金收入明细（基础+利息+连胜奖励），(3) 每张卡的剩余池数量（"3/15"表示T1卡还剩3张）。

**Rationale:**
- `SharedCardPool` 已实现（T1=15, T2=12, T3=9, T4=6, T5=3）但玩家看不到
- `EconomyManager.RoundRewardCalculator` 已计算收入但只发送事件
- 玩家无法做出战略性抢卡决策和经济规划，这是关键信息缺口

**Downsides:** 纯UI改动，可能使界面拥挤

**Evidence:**
- `SharedCardPool.java:16-119` - 已有 getRemainingCopies 和 getMaxCopies 方法
- `EconomyManager.java:99-104` - 已有 getRoundIncomePreview 方法
- `GameUIManager.java` - 商店渲染入口明确，添加文本仅5-10行代码

**Confidence:** 95%
**Complexity:** Low
**Status:** Unexplored

---

### 3. Economic Accelerator Mechanism (经济加速器机制)
**Description:** 当玩家连续做出"最优经济决策"（如存钱吃利息、保持连胜）时，获得临时经济加成乘区。

**Rationale:**
- 复用 `EconomyManager` 的 `GoldManager` 和 `RoundRewardCalculator`
- 激励玩家学习经济系统，使"好玩家"更快建立优势
- 拉高技巧上限，创造决策质量反馈循环

**Downsides:** 可能加剧贫富差距，需要平衡阈值

**Evidence:**
- `EconomyManager.java:39-105` - GoldManager 管理所有金币操作
- `PlayerEconomy.java:10-44` - 已有 winStreak/loseStreak 追踪
- `EconomyManager.java:124-149` - RoundRewardCalculator 处理回合结束，扩展点明确
- 仅需在 PlayerEconomy 添加 decisionQuality 计数器

**Confidence:** 80%
**Complexity:** Low
**Status:** Unexplored

---

### 4. Skill Cooldown Synergy Bonus (技能冷却羁绊加成)
**Description:** 不同羁绊类型影响技能冷却时间。例如：MAGE 羁绊减少 AOE 技能冷却，ASSASSIN 羁绊减少单体技能冷却。

**Rationale:**
- 完全利用现有技能系统（HEAL/AOE/BUFF/DEBUFF）和羁绊系统（12种效果）
- 仅需在 SkillEffectModel 中添加 synergyCooldownModifier 字段
- 突然间每个羁绊都影响战斗节奏，无需新渲染

**Downsides:** 需要数值平衡，可能使某些羁绊过于强势

**Evidence:**
- `manage/SkillEffectManager.java:33-232` - 完整的技能效果管理
- `battle/BattleUnitBlackboard.java:29-473` - 已有 mana 组件和状态机
- `SynergyManager.java:108-144` - 每个羁绊有独立 apply 方法，添加 CD 修改即可

**Confidence:** 82%
**Complexity:** Medium
**Status:** Unexplored

---

### 5. Real-Time Tactical Intervention (实时战术干预系统)
**Description:** 在战斗阶段允许玩家消耗资源（如黄金或能量）触发一次性的战术命令，如"聚焦目标"、"撤退"、"保护核心"。

**Rationale:**
- 当前游戏是纯放置→自动战斗，玩家只能观看
- 保持自走棋核心体验（仍然是AI控制），但增加代理感
- 创造新的资源管理维度（战斗中是否消耗资源干预）

**Downsides:** 需要设计干预机制和平衡，可能破坏自动战斗的纯粹性

**Evidence:**
- `AutoChessGameMode.java:165-169` - startBattle 后玩家无法干预
- `BattlePhaseManager.java:84-122` - updateBattle 是纯 AI 驱动
- `EconomyManager.java:39-105` - GoldManager 可扩展战斗中支持临时消费

**Confidence:** 75%
**Complexity:** Medium
**Status:** Unexplored

---

## Completed Ideas (Previously Ranked)

### Skill System Completion (技能系统完成) - COMPLETED 2026-03-19
- HEAL/AOE/BUFF/DEBUFF 四种技能类型已实现
- SkillEffectManager 完整管理技能效果
- 事件驱动架构集成

### Synergy Visual Feedback (羁绊视觉反馈) - REQUIREMENTS DOC 2026-03-20
- 需求文档已创建：`docs/brainstorms/2026-03-20-synergy-visual-feedback-requirements.md`
- 后端逻辑完整（SynergyManager），前端 UI 待实现

### Card Pool Depletion (卡池消耗) - SOLUTION DOC 2026-03-19
- 解决方案文档已创建：`docs/solutions/architecture/shared-card-pool-depletion.md`
- SharedCardPool 已实现

### Player HP and Round Damage System - COMPLETED
- PlayerLifeManager 完整实现血量系统
- 跨关卡状态持久化

## Rejection Summary

| # | Idea | Reason Rejected |
|---|------|-----------------|
| 1 | 卡牌升级便捷入口 | `CardManager.upgradeCard()` 已存在，仅UI缺失 |
| 2 | 战斗结果反馈界面 | `BattleEndEvent` 已触发，纯视觉需求 |
| 3 | 卡牌悬浮提示 | 纯UI需求，无架构价值 |
| 4 | 拖拽视觉引导 | `GameInputHandler` 已支持拖拽，过度工程化 |
| 5 | 操作撤销机制 | 破坏事件驱动架构，成本远超价值 |
| 6 | 羁绊冲突机制 | 未定义具体机制，无实现路径 |
| 7 | 英雄星级系统 | `CardUpgradeLogic` 已实现3张合并 |
| 8 | 装备系统 | 需要完整物品+属性+UI系统，超出架构 |
| 9 | 准备阶段计时器 | 仅UI倒计时，非架构改进 |
| 10 | 多轮赛季系统 | `PlayerLifeManager` 已处理血量，赛季需元游戏层 |
| 11 | 刷新概率系统 | 破坏确定性设计 |
| 12 | 阵容保存功能 | 需要持久化系统 |
| 13 | 动态难度系统 | 需要完整平衡系统+AI调整 |
| 14 | 经验联动 | 当前无经验系统 |
| 15 | 连击奖励系统 | 复杂时序检测，性能开销大 |
| 16 | 动态环境系统 | 混合3个独立系统，违反架构原则 |
| 17 | 形态变换系统 | 需要动画+状态机+属性调整 |
| 18 | 动态定价机制 | 破坏经济平衡 |
| 19 | 记忆恩怨系统 | 需要持久化关系系统 |
| 20 | 战斗回放系统 | 需要完整状态记录架构 |

## Session Log
- 2026-03-19: Initial ideation - 40 candidates generated across 5 ideation frames, 5 survived adversarial filtering
- 2026-03-19: Brainstorm for idea #1 (Skill System Completion) selected
- 2026-03-20: Deep analysis for idea #2 (Synergy Visual Feedback) - discovered existing implementation in GameUIManager
- 2026-03-20: Brainstorm for idea #2 (Synergy Visual Feedback) completed - requirements doc created
- 2026-03-21: Continuation ideation - 32 new candidates generated across 4 frames (pain/friction, missing capability, leverage, assumption-breaking), 5 survived adversarial filtering, 4 cross-cutting combinations synthesized
