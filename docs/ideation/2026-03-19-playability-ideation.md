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
- Synergy System: 8 types (Warrior/Mage/Archer/Assassin/Tank/Dragon/Beast/Human) with tiered bonuses
- Economy System: PlayerEconomy with gold, interest, win/lose streaks
- Player Life System: Persistent HP across levels (`PlayerLifeBlackboard`), game over handling

**Key Pain Points:**
1. Only BasicSkill implemented - HEAL/AOE/BUFF/DEBUFF all fallback to console log
2. Infinite card pool - no strategic scarcity
3. Passive battles - players only watch, no mid-battle decisions
4. Shallow progression - limited strategic depth
5. No synergy visibility - players cannot see active bonuses

**Past Learnings:**
- Skill system framework exists: `Skill<T>` interface, `SkillType` enum, mana system in `BattleUnitBlackboard`
- `tryCastSkill()` triggers on full mana but only calls `BasicSkill.cast()` which prints to console
- SynergyManager fully calculates 8 synergy types with thresholds but no UI exposure
- CardPool.getRandomCardsByLevel() creates cards on-demand with no quantity tracking

## Ranked Ideas

### 1. Skill System Completion (技能系统完成)
**Description:** 实现 AOE/Heal/Buff/Debuff 四种技能类型的实际效果。当前框架完整（Skill接口、SkillType枚举、法力系统、tryCastSkill触发器），但所有技能都回退到只打印日志的 BasicSkill。

**Rationale:** 技能系统是自走棋的核心差异化点。法力条填满后技能只打印日志是"期望-反馈"断层，让每场战斗感觉完全相同。实现真正的技能效果将被动观看转化为动态战术体验。AOE创造位置策略，Heal创造目标优先级，Buff/Debuff创造队伍组合深度。

**Downsides:** 需要设计技能数值平衡、视觉效果（粒子/着色器）、目标选择逻辑。可能需要调整法力积累速度以匹配战斗时长。

**Evidence:**
- `SkillType.java` lines 15-31 define HEAL, AOE, BUFF, DEBUFF as "future expansion"
- `BattleUnitBlackboard.java:132-142` switch statement returns `BasicSkill` for all types
- `BattleUnitBlackboard.java:217-235` `tryCastSkill()` already triggers on full mana
- Existing `ProjectileManager` and `DamageEvent` systems can be reused for AOE skills
- `SynergyManager` applies buffs - can be extended for BUFF/DEBUFF skills

**Confidence:** 95%
**Complexity:** Medium
**Status:** Explored (brainstorm 2026-03-19)

---

### 2. Synergy Visual Feedback (羁绊视觉反馈)
**Description:** 添加UI面板显示当前活跃的羁绊组合及下一级阈值进度。SynergyManager 已完全计算 8 种羁绊类型及其阈值（如 WARRIOR: 2/4/6），但数据仅存在于代码中。

**Rationale:** 玩家无法看到自己有 3 个 Warrior（差1个升2级）或添加一个 Mage 就能激活 Mage(3)。这是关键信息缺口，使策略性游戏无法进行。TFT/DAC的成功很大程度上依赖于玩家能做出信息充分的决策。

**Downsides:** 需要UI布局设计，可能在移动端屏幕空间紧张。需要设计视觉语言（图标、颜色、进度条）。

**Evidence:**
- `SynergyManager.getSynergyInfoString()` exists but appears unused in UI
- `SynergyType.getActivationThresholds()` and `getSynergyLevel()` methods exist
- No UI component found that displays synergy status during battle

**Deep Analysis (2026-03-20):**
- **现有实现**: `GameUIManager.java:441-452` 仅显示最多3行简单文本
- **完整后端**: `SynergyManager` 完整实现8种羁绊计算（WARRIOR/MAGE/ARCHER/ASSASSIN/TANK/DRAGON/BEAST/HUMAN）
- **可用数据**:
  - `getActiveSynergies()` - 激活的羁绊列表
  - `getAllSynergyCounts()` - 所有羁绊计数
  - `SynergyType.getActivationThresholds()` - 阈值数组
  - `SynergyType.getNextThreshold(level)` - 下一级阈值
- **效果数据**: `SynergyEffect` 有12种加成（攻击/防御/魔法/法力回复/攻速/暴击/暴击伤害/闪避/生命/伤害减免/生命偷取/经验）
- **改进方向**:
  1. 羁绊图标面板 - 左侧显示图标+等级
  2. 进度条 - 当前数量/下一级阈值
  3. 激活状态视觉 - 亮色=激活，灰色=未激活
  4. 悬停详情 - 显示具体加成数值

**Confidence:** 95%
**Complexity:** Low
**Status:** Explored (2026-03-20)

---

### 3. Player Level Unit Slots (玩家等级解锁单位槽)
**Description:** 根据玩家等级限制可部署的单位数量（等级1=3单位，等级5=5单位等）。PlayerEconomy 已追踪 playerLevel，Battlefield.getPlayerCharacters() 返回单位列表。

**Rationale:** 当前没有单位数量限制，等级只影响卡牌层级概率。玩家看到"等级7"却感受不到力量。单位槽限制创造有意义的进度决策和质量vs数量的战略选择。

**Downsides:** 需要重新平衡现有关卡设计。可能让低等级玩家感到受限。

**Evidence:**
- `PlayerEconomy.playerLevel` exists and is tracked
- `Battlefield.getPlayerCharacters()` returns list with no count check
- `CardPool.calculateTierByLevel()` is the ONLY consumer of playerLevel

**Confidence:** 85%
**Complexity:** Low
**Status:** Unexplored

---

### 4. Card Pool Depletion (卡池消耗)
**Description:** 实现有限共享卡池，购买的卡牌从池中移除，出售/升级的卡牌返回池中。按层级设置复制限制（层级1: 15张，层级2: 10张，层级3: 8张，层级4: 5张，层级5: 3张）。

**Rationale:** 当前 CardPool.getRandomCardsByLevel() 从无限池中抽取——相同的传说卡可以无限出现。这消除了自走棋的核心战略深度："如果三个玩家都在玩法师，我应该转型因为池子被抽干了。" 池消耗元游戏是 TFT/DAC 竞技玩法的核心。

**Downsides:** 需要池状态追踪、UI显示剩余数量、与购买/出售流程集成。可能需要调整AI对手的卡池消耗逻辑。

**Evidence:**
- `CardPool.java:78-85` `getRandomCards()` creates cards on-demand with no tracking
- `PlayerDeck` already tracks card counts by ID
- `CardShop.refresh()` could consume from pool, `CardShop.buyCard()` could remove
- Existing `Card` has `tier` field for probability weighting

**Confidence:** 85%
**Complexity:** Medium
**Status:** Unexplored

---

### 5. Position/Grid System (位置/网格系统)
**Description:** 用六边形或方形网格替换自由放置，启用位置策略（前排/后排、AOE规避、刺客侧翼）。

**Rationale:** Battlefield 使用连续坐标，只有区域验证。没有网格、没有定位元游戏。在成功的自走棋游戏中，前排vs后排的定位、角落保护核心、AOE分散是核心技能。网格系统使 AOE 技能（Idea #1）有意义。

**Downsides:** 需要重构放置逻辑、网格叠加渲染、槽位限制。可能降低自由度感觉。

**Evidence:**
- `Battlefield.placeCharacter()` only checks `isInPlayerZone()` and character overlap
- `BattleCharacter` has position (x, y) as floats with no cell reference
- No concept of "frontline," "backline," or adjacency bonuses

**Confidence:** 80%
**Complexity:** Medium
**Status:** Unexplored

## Rejection Summary

| # | Idea | Reason Rejected |
|---|------|-----------------|
| 1 | Equipment/Item System | 增加新层级但羁绊已提供进度；不解决被动战斗问题 |
| 2 | Unit Passive Traits | 太模糊，"差异化单位"不可操作；CardType已区分攻击范围 |
| 3 | Semi-Auto Combat | 合并到 #1，技能完成后才考虑手动触发选项 |
| 4 | Synergy-Skill Bridge | 合并到 #1，是技能完成后的自然扩展 |
| 5 | Kill Streak Bonuses | PlayerEconomy已有连败/连胜系统，重复复杂度 |
| 6 | Skill Draft on Upgrade | 技能选择UI工作量大，边际定制深度低 |
| 7 | Combat Preview | 需要完整战斗模拟，复杂度相对价值过高 |
| 8 | Interest Gambling | 破坏经济策略的噱头 |
| 9 | Losing as Resource | PlayerEconomy已有loseStreak奖励 |
| 10 | Betting Rounds | HP下注需要预测UI和条件追踪，复杂度高、受众窄 |
| 11 | Draft Carousel | 需要多人基础设施，当前为单人PvE |
| 12 | Combat Replay | 新系统，非核心可玩性 |
| 13 | Evolution Chains | 有趣但非v1必要 |
| 14 | Mana Surge Rounds | 可作为后续优化 |
| 15 | Inverse Synergy Mechanics | 有趣但增加理解成本 |

## Session Log
- 2026-03-19: Initial ideation - 40 candidates generated across 5 ideation frames (pain/friction, missing capability, inversion/automation, leverage/compounding, assumption-breaking), 5 survived adversarial filtering
- 2026-03-19: Brainstorm for idea #1 (Skill System Completion) selected
- 2026-03-20: Deep analysis for idea #2 (Synergy Visual Feedback) - discovered existing implementation in GameUIManager (3-line text only), identified improvement directions (icons, progress bars, hover details)
- 2026-03-20: Brainstorm for idea #2 (Synergy Visual Feedback) completed - requirements doc created at docs/brainstorms/2026-03-20-synergy-visual-feedback-requirements.md
