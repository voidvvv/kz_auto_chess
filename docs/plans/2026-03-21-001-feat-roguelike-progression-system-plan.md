---
title: Roguelike 难度递增模式系统
type: feat
status: active
date: 2026-03-21
origin: docs/brainstorms/2026-03-21-roguelike-progression-requirements.md
deepened: 2026-03-21
---

# Roguelike 难度递增模式系统

## Enhancement Summary

**Deepened on:** 2026-03-21
**Sections enhanced:** 8
**Research agents used:** 6 (code-guidelines, architecture-strategist, code-simplicity, data-integrity, performance-oracle, pattern-recognition)

### Key Improvements
1. **添加全局状态管理规范** - 在 `KzAutoChess` 中管理 `RoguelikeProgressBlackboard`
2. **完善 Manager 生命周期集成** - 明确 `onEnter/onExit` 调用模式
3. **添加数据完整性保障** - 原子性存档、事务管理器、校验和验证
4. **添加性能优化建议** - 对象池、批量渲染、配置预加载
5. **添加国际化支持** - 所有 UI 文本使用 `I18N.get()`
6. **命名规范修正** - `RandomEvent` → `RandomEventModel`

### New Considerations Discovered
- **状态持久化原子性** - 需要实现双缓冲存档策略
- **事件效果回滚机制** - 临时效果需要状态追踪和恢复
- **成就解锁幂等性** - 防止重复触发和解锁
- **存档版本迁移** - 支持游戏更新后的存档兼容

---

## Overview

引入类似《杀戮尖塔》的 Roguelike 递增模式，提供 30 个连续阶段的难度递增挑战。玩家经历「战斗阶段 → 备战阶段 → 随机事件 → Boss 战」循环，最终击败最终 Boss 完成通关。系统包含独立的 RoguelikeGameMode、备战界面、随机事件系统、Boss 战机制、战场效果和成就系统。

## Problem Statement

当前游戏内容过于单调，玩家选择关卡后只能重复挑战同一关卡，敌人难度固定无变化。缺乏长期挑战目标，游戏循环浅层化，难以维持玩家兴趣。

## Proposed Solution

采用**独立分支架构**（参见 origin:决策1），创建完全独立的 `RoguelikeGameMode` 和 `RoguelikeScreen`，保留现有 5 关卡系统作为「教学/练习模式」。通过 JSON 驱动的关卡配置、每 3 关触发的随机事件、每 10 关的 Boss 里程碑，构建完整的 30 关挑战循环。

## Technical Approach

### Architecture

```
RoguelikeScreen (screens/)
    ↓
RoguelikeGameMode (game/)
    ├── RoguelikeProgressManager (manage/) - 进度管理
    ├── RoguelikeEventManager (manage/) - 随机事件
    ├── AchievementManager (manage/) - 成就系统
    ├── BattleEffectManager (manage/) - 战场效果
    ├── RoguelikeConfigLoader (logic/) - JSON配置加载
    └── 复用现有 Managers:
        ├── BattleManager
        ├── EconomyManager
        ├── CardManager
        └── PlayerLifeManager
```

**关键设计决策**（来源：origin 文档）:
- **独立分支架构** - 保留现有系统，最小化影响 (origin:决策1)
- **事件固定间隔触发** - 每 3 关触发，可预期 (origin:决策2)
- **Boss 固定周期** - 第 10/20/30 关为 Boss (origin:决策3)

### Research Insights

#### Best Practices

**全局状态管理**（来自 `kz-autochess-code-guidelines` skill）:
- ✅ 跨 Screen 共享的状态必须放在 `KzAutoChess` 游戏主类中
- ✅ 提供 `getRoguelikeProgressBlackboard()` getter 方法
- ✅ 在 `create()` 中初始化

**Manager 生命周期集成**（来自 `kz-autochess-code-guidelines` skill）:
```java
// RoguelikeGameMode 必须实现完整生命周期
@Override
public void onEnter() {
    progressManager.onEnter();
    eventManager.onEnter();
    achievementManager.onEnter();
    battleEffectManager.onEnter();
    // 复用现有 managers
    battleManager.onEnter();
    economyManager.onEnter();
    cardManager.onEnter();
}

@Override
public void onExit() {
    progressManager.onExit();
    eventManager.onExit();
    achievementManager.onExit();
    battleEffectManager.onExit();
    // ...
}
```

**事件监听器注册模式**:
```java
// 每个 Manager 必须实现 GameEventListener
public class RoguelikeProgressManager implements GameEventListener {
    private final GameEventSystem eventSystem;

    public void onEnter() {
        eventSystem.registerListener(this);  // 必须
    }

    public void onExit() {
        eventSystem.unregisterListener(this);  // 必须
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event instanceof StageCompleteEvent) {
            // 处理
        }
    }
}
```

#### Performance Considerations

**对象池优化**（来自性能分析）:
- 为 `Projectile`、`Particle` 创建对象池
- 预期改进：减少 60-80% 的 GC 停顿

**批量渲染优化**:
- 合并 `SpriteBatch` 的 `begin/end` 调用
- 按材质/类型排序渲染对象
- 预期改进：减少 40-50% 的 Draw Calls

**配置预加载**:
- 在游戏启动时加载所有 JSON 配置
- 避免运行时阻塞主线程

#### Implementation Details

**渲染管线切换**（来自 `kz-autochess-code-guidelines` skill）:
```java
// 正确的渲染管线切换
game.getBatch().begin();
font.draw(game.getBatch(), layout, x, y);
game.getBatch().end();  // 先结束

shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
shapeRenderer.rect(x, y, width, height);
shapeRenderer.end();
```

**国际化文本**:
```java
// assets/i18n/i18n_zh.properties
roguelike.stage.name=阶段 {0}
roguelike.event.random=随机事件
roguelike.boss.warning=Boss 警告

// 代码中使用
String text = I18N.format("roguelike.stage.name", stageNum);
```

#### Edge Cases

**状态恢复场景**:
1. 应用崩溃后从上一关恢复
2. 事件效果应用失败时回滚
3. 成就重复解锁检测
4. 存档损坏后的降级策略

### Implementation Phases

#### Phase 1: 基础架构与进度管理

**目标**: 建立核心 GameMode 和进度追踪

**任务**:
1. 创建 `RoguelikeGameMode` 实现 `GameMode` 接口
2. 创建 `RoguelikeProgressManager` 管理阶段进度
3. 创建 `RoguelikeProgressBlackboard` 聚合状态
4. 创建 `RoguelikeScreen` 处理 UI 渲染
5. 在 `KzAutoChess` 中添加 `RoguelikeProgressBlackboard` 全局状态字段
6. 在 `KzAutoChess` 中添加「无尽模式」入口按钮
7. 实现 30 关基础流程（无事件、无 Boss）

**关键文件**:
- `game/RoguelikeGameMode.java` - 新建
- `manage/RoguelikeProgressManager.java` - 新建
- `battle/RoguelikeProgressBlackboard.java` - 新建
- `screens/RoguelikeScreen.java` - 新建
- `screens/StartScreen.java` - 修改（添加入口按钮）
- `KzAutoChess.java` - 修改（添加全局状态）

**依赖**:
- 现有 `GameMode` 接口: `game/GameMode.java:10-55`
- 现有 Manager 生命周期模式: `manage/PlayerLifeManager.java:34-49`
- 全局状态管理模式: `KzAutoChess.java:42-51`

**成功标准**:
- [ ] 点击「无尽模式」进入第 1 阶段战斗
- [ ] 战斗结束后进入备战界面
- [ ] 显示「当前阶段 X / 30」（使用 `I18N.format()`）
- [ ] 点击「下一阶段」正确递增阶段数
- [ ] 第 30 阶段后显示通关界面

**Research-Based Additions**:
- [ ] `RoguelikeGameMode.onEnter()` 调用所有 Manager 的 `onEnter()`
- [ ] `RoguelikeGameMode.onExit()` 调用所有 Manager 的 `onExit()`
- [ ] `RoguelikeProgressManager` 实现 `GameEventListener` 接口
- [ ] 使用 `RenderHolder` 进行渲染（如果需要自定义渲染）

#### Phase 2: 备战界面与商店集成

**目标**: 实现阶段间的备战机制

**任务**:
1. 创建 `RoguelikePreparationScreen` 独立备战界面
2. 创建 `RoguelikeUIManager` 管理备战界面 UI
3. 复用 `CardManager` 商店功能
4. 复用 `EconomyManager` 经济系统
5. 显示阶段信息、金币、血量
6. **实现战场视图切换功能**（新增）
7. 实现购买、升级、刷新、下一阶段按钮

**关键文件**:
- `screens/RoguelikePreparationScreen.java` - 新建
- `ui/RoguelikeUIManager.java` - 新建（备战界面 UI）
- `render/ArmyPreviewRenderer.java` - 新建（双方阵容预览渲染）

**战场视图切换功能设计**:
- **备战界面**: 显示商店、手牌、金币等经济操作
- **战场界面（可切换）**:
  - 显示我方当前部署的角色及其属性
  - 显示下一阶段敌人的预览阵容
  - 分屏显示或选项卡切换
- 切换按钮：「战场视图」/「备战视图」
- 帮助玩家根据敌方阵容调整己方部署

**依赖**:
- 现有 CardManager: `manage/CardManager.java`
- 现有 EconomyManager: `manage/EconomyManager.java`
- GameUIManager 模式: `ui/GameUIManager.java`

**成功标准**:
- [ ] 备战界面显示当前阶段、金币、血量（使用 `I18N.format()`）
- [ ] **可以通过按钮切换到战场视图**
- [ ] **战场视图显示我方当前部署的角色阵容**
- [ ] **战场视图显示下一阶段敌人的预览阵容**
- [ ] 点击购买卡牌正确扣款并添加到手牌
- [ ] 三张相同卡牌正确合并升级
- [ ] 刷新商店正确扣除金币
- [ ] 点击「下一阶段」进入下一关战斗

**备战界面 UI 设计**:
```
┌─────────────────────────────────────────────────────────┐
│  第 5 / 30 阶段                    金币: 15   血量: 45/50 │
│  [备战视图] [战场视图] ← 可切换                           │
├─────────────────────────────────────────────────────────┤
│  【备战视图】                                            │
│  ┌─────────────────────────────────────────────────┐   │
│  │  商店 (刷新: 2金币)                              │   │
│  │  [卡1] [卡2] [卡3] [卡4] [卡5]                 │   │
│  └─────────────────────────────────────────────────┘   │
│  手牌: [卡A] [卡B] [卡C]                                │
│  [购买卡牌] [升级卡牌] [刷新商店]                       │
│              [开始下一阶段 →]                            │
└─────────────────────────────────────────────────────────┘
```

**战场视图 UI 设计**:
```
┌─────────────────────────────────────────────────────────┐
│  第 5 / 30 阶段                    金币: 15   血量: 45/50 │
│  [备战视图] [战场视图] ← 可切换                           │
├─────────────────────────────────────────────────────────┤
│  【战场视图】                                            │
│                                                         │
│  我方阵容:                    敌方阵容 (下一关):           │
│  ┌────┐ ┌────┐ ┌────┐         ┌────┐ ┌────┐ ┌────┐     │
│  │战士│ │弓手│ │法师│         │战士│ │战士│ │弓手│     │
│  │T3 │ │T2 │ │T2 │         │T2 │ │T2 │ │T2 │     │
│  │HP: │ │HP: │ │HP: │         │HP:80│ │HP:80│ │HP:60│    │
│  │120 │ │60  │ │50  │         └────┘ └────┘ └────┘     │
│  └────┘ └────┘ └────┘                                   │
│                                                         │
│  提示: 点击角色查看详细属性，可拖拽调整站位               │
│                                                         │
│              [开始下一阶段 →]                            │
└─────────────────────────────────────────────────────────┘
```

**Research-Based Additions**:
- [ ] 确保渲染管线正确切换（`SpriteBatch` ↔ `ShapeRenderer`）
- [ ] `RoguelikePreparationScreen.show()` 中注册事件监听器
- [ ] `RoguelikePreparationScreen.hide()` 中注销事件监听器

#### Phase 3: 随机事件系统

**目标**: 实现每 3 关触发的随机事件选择

**任务**:
1. 创建 `RoguelikeEventManager` 管理事件触发
2. 创建 `RandomEventModel` Model 类（纯数据）
3. 创建 `RandomEventScreen` 事件选择界面
4. 实现 5 个基础随机事件
5. 创建 `event/roguelike/` 事件包
6. 实现事件效果的持久化和应用

**关键文件**:
- `manage/RoguelikeEventManager.java` - 新建
- `model/RandomEventModel.java` - 新建（命名规范：XxxModel）
- `model/RandomEventOption.java` - 新建
- `screens/RandomEventScreen.java` - 新建
- `event/roguelike/RandomEventTriggeredEvent.java` - 新建
- `event/roguelike/RandomEventSelectedEvent.java` - 新建

**事件示例**（来自 origin 文档）:
```java
// 神秘商人
MYSTERIOUS_MERCHANT(
    "神秘商人",
    "获得3张随机T2卡牌，失去5金币",
    Option.reward("cards", 3, 2).cost(5)
),
// 训练场
TRAINING_GROUND(
    "训练场",
    "所有单位攻击力+10%，失去3血量",
    Option.buff("attack", 1.1f).cost(3).healthCost()
),
// 宝箱
TREASURE_CHEST(
    "宝箱",
    "获得10金币",
    Option.gold(10)
),
// 诅咒祭坛
CURSED_ALTAR(
    "诅咒祭坛",
    "所有单位血量-20%，获得15金币",
    Option.debuff("health", 0.8f).gold(15)
)
```

**触发阶段**: 3, 6, 9, 12, 15, 18, 21, 24, 27（共 9 次）

**依赖**:
- 现有事件系统: `event/GameEventSystem.java:14-60`
- 现有事件监听器: `event/GameEventListener.java:7-13`
- 技能效果模式: `docs/solutions/logic-errors/skill-effects-not-applied.md`

**成功标准**:
- [ ] 第 3/6/9/.../27 阶段结束后触发事件界面
- [ ] 显示 3 个随机选项
- [ ] 选择选项后正确应用效果
- [ ] Buff/Debuff 持续到游戏结束（使用 `addTemporaryEffect()` 模式）
- [ ] 卡牌/金币奖励立即生效

**Research-Based Additions**:
- [ ] 使用 `addTemporaryEffect()` 应用 Buff/Debuff（参考技能效果模式）
- [ ] 实现事件效果的事务性应用（全部成功或全部回滚）
- [ ] 所有事件文本使用 `I18N.get()` 或 `I18N.format()`

#### Phase 4: Boss 战系统

**目标**: 实现第 10/20/30 关的 Boss 挑战

**任务**:
1. 在 JSON 配置中定义 Boss 阶段
2. 创建 `BossConfig` Model 类
3. 实现 Boss 特殊属性（血量、攻击力强化）
4. 在战斗 UI 显示「BOSS 战斗」标识
5. 实现 Boss 胜利后的额外奖励
6. 实现最终 Boss 胜利后的通关画面

**关键文件**:
- `model/BossConfig.java` - 新建
- `assets/roguelike_stage_config.json` - 新建（JSON 配置）
- `logic/RoguelikeConfigLoader.java` - 新建
- `screens/RoguelikeScreen.java` - 修改（Boss UI）
- `event/roguelike/BossDefeatedEvent.java` - 新建

**Boss 配置示例**（来自 origin 文档）:
```json
{
  "stage": 30,
  "enemyCount": 1,
  "enemyMinTier": 5,
  "enemyMaxTier": 5,
  "isBoss": true,
  "bossType": "final_boss",
  "healthMultiplier": 5.0,
  "attackMultiplier": 2.0,
  "reward": {
    "gold": 50,
    "cards": [{"tier": 5, "count": 1}]
  }
}
```

**Boss 阶段**: 10（小Boss）、20（小Boss）、30（最终Boss）

**依赖**:
- 现有战斗系统: `manage/BattleManager.java:55-456`
- 技能效果模式: `docs/solutions/logic-errors/skill-effects-not-applied.md`

**成功标准**:
- [ ] 第 10/20/30 关显示「BOSS 战斗」标识（使用 `I18N.get()`）
- [ ] Boss 血量和攻击力正确强化
- [ ] Boss 胜利后给予额外金币奖励
- [ ] 最终 Boss 胜利显示通关画面
- [ ] 通关后显示解锁的成就

**Research-Based Additions**:
- [ ] Boss 配置使用 JSON 验证和降级策略
- [ ] Boss 标识文本使用 `I18N.get("roguelike.boss.warning")`

#### Phase 5: 战场效果系统

**目标**: 实现天气和区域效果

**任务**:
1. 创建 `BattleEffectManager` 管理战场效果
2. 创建 `BattleEffectModel` Model 类
3. 创建 `WeatherEffectModel` Model 类
4. 创建 `AreaEffectModel` Model 类
5. 实现天气效果（雨天、雾天、风天）
6. 实现区域效果（毒性、治疗、加速）
7. 在战斗 UI 显示当前生效效果

**关键文件**:
- `manage/BattleEffectManager.java` - 新建
- `model/BattleEffectModel.java` - 新建（命名规范：XxxModel）
- `model/WeatherEffectModel.java` - 新建
- `model/AreaEffectModel.java` - 新建
- `event/roguelike/BattleEffectAppliedEvent.java` - 新建

**效果定义**（来自 origin 文档）:
```
天气效果:
- 晴天（默认）: 无特殊效果
- 雨天: 所有单位移动速度 -20%
- 雾天: 攻击命中率 -15%
- 风天: 远程单位射程 +30%

区域效果:
- 毒性区域: 进入区域每秒扣血
- 治疗区域: 进入区域每秒回血
- 加速区域: 进入区域移动速度 +50%
```

**依赖**:
- 现有角色属性系统: `model/BattleCharacter.java`
- 羁绊视觉反馈模式: `docs/brainstorms/2026-03-20-synergy-visual-feedback-requirements.md`

**成功标准**:
- [ ] 雨天效果正确降低单位移动速度
- [ ] 雾天效果正确降低命中率
- [ ] 风天效果正确增加远程射程
- [ ] 区域效果正确对进入单位生效
- [ ] UI 显示当前生效效果和描述（使用 `I18N.format()`）

**Research-Based Additions**:
- [ ] 使用 `addTemporaryEffect()` 模式应用天气效果
- [ ] 在 `BattleUnitBlackboard.update()` 中更新效果过期

#### Phase 6: 成就系统

**目标**: 实现元游戏渐进系统

**任务**:
1. 创建 `AchievementManager` 管理成就
2. 创建 `AchievementModel` Model 类
3. 创建 `AchievementBlackboard` 聚合成就状态
4. 实现本地文件持久化（使用 LibGDX `Preferences`）
5. 创建主菜单「成就」页面
6. 实现 5 个基础成就

**关键文件**:
- `manage/AchievementManager.java` - 新建
- `model/AchievementModel.java` - 新建（命名规范：XxxModel）
- `battle/AchievementBlackboard.java` - 新建
- `screens/AchievementScreen.java` - 新建
- `event/roguelike/AchievementUnlockedEvent.java` - 新建

**成就定义**（来自 origin 文档）:
```java
// 初次挑战
FIRST_CHALLENGE(
    "初次挑战",
    "完成第1阶段",
    () -> progress.getCurrentStage() >= 1
),
// 初级冒险者
NOVICE_ADVENTURER(
    "初级冒险者",
    "到达第5阶段",
    () -> progress.getCurrentStage() >= 5
),
// Boss猎人
BOSS_HUNTER(
    "Boss猎人",
    "击败第10阶段Boss",
    () -> progress.getHighestStage() >= 10
),
// 生存专家
SURVIVAL_EXPERT(
    "生存专家",
    "到达第20阶段",
    () -> progress.getHighestStage() >= 20
),
// 通关者
GAME_MASTER(
    "通关者",
    "击败最终Boss",
    () -> progress.isFinalBossDefeated()
)
```

**依赖**:
- 全局状态管理模式: `KzAutoChess.java:42-51`
- 文件持久化模式: `logic/LifeConfig.java:26-42`

**成功标准**:
- [ ] 完成第 1 阶段解锁「初次挑战」成就
- [ ] 到达第 5 阶段解锁「初级冒险者」成就
- [ ] 击败第 10 阶段 Boss 解锁「Boss猎人」成就
- [ ] 到达第 20 阶段解锁「生存专家」成就
- [ ] 击败最终 Boss 解锁「通关者」成就
- [ ] 成就进度跨会话持久化（使用 `Preferences`）
- [ ] 主菜单「成就」页面正确显示已解锁/未解锁成就

**Research-Based Additions**:
- [ ] 实现幂等的成就解锁（防止重复触发）
- [ ] 成就解锁前先保存，再触发事件
- [ ] 使用 LibGDX `Preferences` API 替代直接文件操作
- [ ] 实现存档版本迁移机制

#### Phase 7: 游戏结束与重玩

**目标**: 实现失败/通关处理和重玩机制

**任务**:
1. 创建 `RoguelikeGameOverScreen` 失败界面
2. 创建 `RoguelikeVictoryScreen` 通关界面
3. 显示到达阶段数和解锁的成就
4. 实现「返回主菜单」和「再次挑战」选项
5. 实现「再次挑战」时重置游戏状态

**关键文件**:
- `screens/RoguelikeGameOverScreen.java` - 新建
- `screens/RoguelikeVictoryScreen.java` - 新建
- `event/roguelike/GameOverEvent.java` - 新建
- `event/roguelike/VictoryEvent.java` - 新建

**依赖**:
- 现有战斗结果界面模式: `docs/plans/2026-03-08-feat-battle-result-screen-plan.md`

**成功标准**:
- [ ] 玩家死亡显示「游戏结束」界面（使用 `I18N.get()`）
- [ ] 显示到达阶段数和解锁的成就
- [ ] 点击「返回主菜单」正确返回
- [ ] 点击「再次挑战」正确重置并开始新游戏
- [ ] 通关显示「胜利」界面和通关星级（使用 `I18N.get()`）

**Research-Based Additions**:
- [ ] `GameOverScreen` 和 `VictoryScreen` 的 `show()/hide()` 中注册/注销监听器
- [ ] 实现状态快照/恢复机制

#### Phase 8: JSON 配置与平衡

**目标**: 完善配置系统并调整数值平衡

**任务**:
1. 创建完整的 `roguelike_stage_config.json`
2. 实现 `ConfigValidator` 配置验证类
3. 实现降级策略（默认配置）
4. 定义 30 个阶段的敌人配置
5. 定义战场效果分布
6. 调整经济平衡（金币奖励、商店价格）
7. 调整难度曲线

**关键文件**:
- `core/src/main/resources/roguelike_stage_config.json` - 新建（路径修正）
- `logic/RoguelikeConfigLoader.java` - 新建（完善）
- `logic/ConfigValidator.java` - 新建

**配置结构**（来自 origin 文档）:
```json
{
  "stages": [
    {
      "stage": 1,
      "enemyCount": 3,
      "enemyMinTier": 1,
      "enemyMaxTier": 2,
      "battlefieldEffect": null
    },
    {
      "stage": 5,
      "enemyCount": 5,
      "enemyMinTier": 2,
      "enemyMaxTier": 3,
      "battlefieldEffect": {
        "type": "weather",
        "effect": "rain",
        "description": "雨天：所有单位移动速度-20%"
      }
    },
    {
      "stage": 10,
      "enemyCount": 1,
      "enemyMinTier": 4,
      "enemyMaxTier": 4,
      "isBoss": true,
      "bossType": "mini_boss_1"
    }
  ]
}
```

**依赖**:
- 现有 JSON 加载模式: `logic/CharacterStatsLoader.java:33-64`

**成功标准**:
- [ ] 修改 JSON 后游戏行为相应变化（无需重新编译）
- [ ] 配置加载失败时使用默认配置（不崩溃）
- [ ] 30 关难度曲线合理递增
- [ ] 经济系统平衡（不耗尽也不滚雪球）

**Research-Based Additions**:
- [ ] 实现双缓冲存档策略（主存档 + 临时文件）
- [ ] 实现校验和验证机制
- [ ] 在游戏启动时预加载所有配置

## Alternative Approaches Considered

### 替代方案 1: 修改现有 GameMode

**描述**: 直接在 `AutoChessGameMode` 中添加无尽模式逻辑

**拒绝理由**:
- 增加现有代码复杂度，提高维护成本
- 难以保持教学/练习模式独立性
- 增加测试难度

### 替代方案 2: 随机事件完全随机触发

**描述**: 不固定每 3 关触发，而是基于概率随机触发

**拒绝理由**:
- 玩家无法提前规划策略
- 平衡难度较高
- 增加实现复杂度

### 替代方案 3: 成就影响战斗平衡

**描述**: 成就解锁后获得永久战斗加成

**拒绝理由**:
- 新玩家与老玩家差距过大
- 平衡难度高
- 偏离「纯收集」设计目标

### 替代方案 4: MVP 优先方法（来自简化分析）

**描述**: 先实现最小可行产品，验证核心玩法后再添加复杂功能

**优势**:
- 将开发时间从 54 小时减少到 8-15 小时
- 快速验证游戏概念
- 避免过度设计

**建议的分阶段实施**:
- **MVP (v1.0)**: 30 关基础流程 + Boss 战 + 胜利/失败界面
- **v1.5**: 随机事件系统（如果 MVP 验证成功）
- **v2.0**: 成就系统（如果玩家留存好）
- **v3.0**: 战场效果系统（长期计划）

## System-Wide Impact

### Interaction Graph

```
用户点击「无尽模式」
    ↓
StartScreen.handleEndlessModeClicked()
    ↓
KzAutoChess.setScreen(new RoguelikeScreen(game))
    ↓
RoguelikeScreen.show() → RoguelikeGameMode.onEnter()
    ↓
RoguelikeGameMode.onEnter() 调用所有 Manager.onEnter()
    ├── RoguelikeProgressManager.onEnter() → 注册事件监听
    ├── RoguelikeEventManager.onEnter() → 注册事件监听
    ├── AchievementManager.onEnter() → 注册事件监听
    └── BattleEffectManager.onEnter() → 注册事件监听
    ↓
战斗开始 → BattleManager.startBattle()
    ↓
战斗结束 → BattleEndEvent → 各监听器响应
    ├── RoguelikeProgressManager.onBattleEnd() → 检查阶段/事件/Boss
    ├── AchievementManager.onBattleEnd() → 检查成就条件
    └── RoguelikeEventManager.onBattleEnd() → 触发事件（如果是事件阶段）
    ↓
切换到备战界面 / 事件界面 / Boss奖励界面
    ↓
用户点击「下一阶段」
    ↓
RoguelikeProgressManager.advanceToNextStage() → stage++
    ↓
检查是否是事件阶段 / Boss阶段 → 显示对应界面
    ↓
开始下一阶段战斗 → 循环
```

### Error & Failure Propagation

| 错误类型 | 来源 | 处理层级 | 处理策略 |
|----------|------|----------|----------|
| `JsonParseException` | `roguelike_stage_config.json` | `RoguelikeConfigLoader.load()` | 降级到默认配置，记录错误日志 |
| `IOException` | 成就文件读写 | `AchievementManager.save/load()` | 使用内存状态，提示用户 |
| `IllegalArgumentException` | 阶段超出范围 | `RoguelikeProgressManager.advanceStage()` | 限制在 1-30 范围，触发通关 |
| `NullPointerException` | Manager 未初始化 | `RoguelikeGameMode.onEnter()` | 快速失败，显示错误提示 |
| `IllegalStateException` | 事件状态不一致 | `RoguelikeEventManager.applyEffect()` | 回滚事件，显示错误提示 |

### State Lifecycle Risks

| 场景 | 风险 | 缓解机制 |
|------|------|----------|
| **事件效果应用失败** | Buff 部分应用，玩家获得不公平优势 | 使用事务模式：要么全部应用，要么全部回滚 |
| **成就保存失败** | 成就触发但未持久化，恢复后重复触发 | 成就解锁前先保存，再触发事件 |
| **阶段进度丢失** | 应用崩溃，玩家回到第 1 关 | 每关结束后自动保存进度到本地文件 |
| **配置加载失败** | JSON 损坏导致游戏无法启动 | 内置默认配置作为降级方案 |
| **Manager 注销失败** | onExit() 时异常导致监听器未注销 | try-finally 确保注销始终执行 |

### Data Integrity Requirements

**必须实现 (CRITICAL)**:

| 需求 | 优先级 | 实现方式 |
|------|--------|----------|
| 原子性存档保存 | P0 | 双缓冲策略：写入临时文件 → 原子性重命名 |
| 存档校验和验证 | P0 | 计算并验证 checksum |
| 幂等的成就解锁 | P0 | synchronized 块 + 集合去重 |
| 多级备份机制 | P0 | 保留最近 5 个备份文件 |

**数据完整性代码示例**:
```java
// 原子性保存
public boolean atomicSave(SaveData data) {
    Path tempFile = saveFile.resolveSibling("save.json.tmp");
    try {
        Files.write(tempFile, serialize(data));
        Files.move(tempFile, saveFile,
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.ATOMIC_MOVE);
        return true;
    } catch (IOException e) {
        return false;
    }
}

// 幂等的成就解锁
public boolean unlockAchievement(String achievementId) {
    synchronized (lock) {
        if (unlockedAchievements.contains(achievementId)) {
            return false; // 已解锁，幂等返回
        }
        unlockedAchievements.add(achievementId);
        saveAchievement(achievementId); // 先保存
        eventSystem.postEvent(new AchievementUnlockedEvent(achievementId));
        return true;
    }
}
```

### API Surface Parity

需要更新的接口：
- `GameMode` - 新增 `RoguelikeGameMode` 实现
- `GameEventListener` - 新增 `RoguelikeProgressManager`、`RoguelikeEventManager`、`AchievementManager`、`BattleEffectManager`
- `GameEvent` - 新增 `event/roguelike/` 包的所有事件
- `Screen` - 新增 `RoguelikeScreen`、`RoguelikePreparationScreen`、`RandomEventScreen`、`RoguelikeGameOverScreen`、`RoguelikeVictoryScreen`、`AchievementScreen`

### Integration Test Scenarios

1. **完整 30 关流程**:
   - 从第 1 关开始，连续通过 30 关
   - 验证所有事件在正确阶段触发
   - 验证 Boss 在 10/20/30 关出现
   - 验证通关后显示成就

2. **事件效果叠加**:
   - 触发多个 Buff 事件
   - 验证效果正确叠加
   - 验证效果持续到游戏结束

3. **Boss 失败重试**:
   - 第 10 关 Boss 失败
   - 验证显示游戏结束界面
   - 验证「再次挑战」正确重置

4. **存档恢复**:
   - 在第 15 关关闭应用
   - 重新打开后从第 15 关继续
   - 验证所有状态正确恢复

5. **配置热更新**:
   - 修改 `roguelike_stage_config.json`
   - 不重新编译直接运行
   - 验证游戏行为相应变化

6. **成就解锁幂等性**:
   - 多次触发同一成就条件
   - 验证成就只解锁一次
   - 验证奖励只领取一次

## Acceptance Criteria

### Functional Requirements

- [ ] 玩家能够连续体验 30 个阶段的难度递增战斗
- [ ] 每阶段战斗结束后进入备战界面
- [ ] 备战界面显示当前阶段、金币、血量、商店
- [ ] **备战界面显示下一阶段的敌人预览**
- [ ] 玩家能够在备战界面购买/升级/刷新卡牌
- [ ] 第 3/6/9/.../27 阶段触发随机事件
- [ ] 随机事件显示 3 个选项供玩家选择
- [ ] 选择事件选项后正确应用效果
- [ ] 第 10/20/30 阶段显示「BOSS 战斗」标识
- [ ] Boss 战胜利后给予额外奖励
- [ ] 最终 Boss 胜利显示通关画面
- [ ] 修改 JSON 配置后游戏行为相应变化（无需重新编译）
- [ ] 战场效果正确应用并视觉呈现
- [ ] 成就系统正确追踪并持久化
- [ ] 失败/通关界面正确显示并提供重玩选项

### Non-Functional Requirements

- [ ] 配置加载失败时使用默认配置（不崩溃）
- [ ] 成就保存失败时不影响游戏继续
- [ ] 应用崩溃后进度可恢复（自动存档）
- [ ] UI 流畅无卡顿（60 FPS）
- [ ] 内存使用合理（无泄漏）

### Quality Gates

- [ ] 单元测试覆盖率 >= 80%
- [ ] 所有 Manager 生命周期正确集成
- [ ] 事件监听器正确注册/注销
- [ ] 渲染管线正确切换（ShapeRenderer ↔ SpriteBatch）
- [ ] 代码审查通过
- [ ] 所有 UI 文本使用国际化
- [ ] 所有类名与文件名匹配

## Success Metrics

- **参与度**: 玩家平均通关阶段数 >= 10
- **留存率**: 玩家完成 30 关后再次游玩比例 >= 30%
- **平衡性**: 第 10/20/30 关通关率分别为 60%/30%/10%
- **稳定性**: 崩溃率 < 1%

## Dependencies & Prerequisites

### 代码依赖

| 组件 | 依赖 | 状态 |
|------|------|------|
| `RoguelikeGameMode` | `GameMode` 接口 | ✅ 已存在 |
| `RoguelikeProgressManager` | `GameEventSystem` | ✅ 已存在 |
| `RoguelikeEventManager` | `GameEventSystem` | ✅ 已存在 |
| `AchievementManager` | `GameEventSystem` | ✅ 已存在 |
| `BattleEffectManager` | `GameEventSystem` | ✅ 已存在 |
| `RoguelikeConfigLoader` | LibGDX `JsonReader` | ✅ 已存在 |
| 商店功能 | `CardManager`, `EconomyManager` | ✅ 已存在 |
| 战斗功能 | `BattleManager` | ✅ 已存在 |
| 血量管理 | `PlayerLifeBlackboard` | ✅ 已存在 |

### 外部依赖

- **LibGDX 1.14.0** - 游戏框架
- **Ashley 1.7.4** - ECS 框架
- **JUnit 5** - 测试框架

### 阻塞依赖

无阻塞依赖。所有依赖组件已存在。

## Risk Analysis & Mitigation

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| **经济系统不平衡** | 中 | 高 | 实现 JSON 配置后进行多轮平衡测试 |
| **随机事件不公平** | 中 | 高 | 实现事件权重系统和互斥规则 |
| **Boss 难度过高** | 中 | 中 | 提供 Boss 属性调整配置 |
| **成就无法保存** | 低 | 中 | 实现降级策略，失败时使用内存状态 |
| **状态恢复失败** | 低 | 高 | 每关结束后自动保存，崩溃后从上一关恢复 |
| **内存泄漏** | 低 | 高 | 使用 LibGDX 对象池，定期 GC 检查 |
| **渲染性能下降** | 中 | 中 | 使用 `RenderHolder.flush()` 确保状态一致性 |

## Resource Requirements

### 开发时间估算

| 阶段 | 预计工时 |
|------|----------|
| Phase 1: 基础架构与进度管理 | 8 小时 |
| Phase 2: 备战界面与商店集成 | 6 小时 |
| Phase 3: 随机事件系统 | 8 小时 |
| Phase 4: Boss 战系统 | 6 小时 |
| Phase 5: 战场效果系统 | 8 小时 |
| Phase 6: 成就系统 | 6 小时 |
| Phase 7: 游戏结束与重玩 | 4 小时 |
| Phase 8: JSON 配置与平衡 | 8 小时 |
| **总计** | **54 小时** |

**MVP 方案（推荐）**:
- Phase 1 + Phase 2 + Phase 4 + Phase 7 = **15-20 小时**
- 其他阶段根据 MVP 验证结果决定是否实施

### 技能要求

- Java 25 熟练度
- LibGDX 框架经验
- 游戏设计理解（平衡、难度曲线）
- JSON 配置设计

## Future Considerations

### 扩展性

- **更多阶段**: 配置化设计支持扩展到 50/100 关
- **更多随机事件**: 事件池易于扩展
- **更多战场效果**: 效果系统支持自定义效果
- **多人排行榜**: 成就系统可扩展为在线排名

### 后续版本

- **Boss 特殊技能**: 第二阶段实现 Boss 独特技能机制
- **卡池重置**: 每大关卡（10 关）重置卡池
- **稀有度系统**: 随机事件分普通/稀有/传说
- **成就奖励**: 成就解锁后给予卡牌/称号奖励

## Documentation Plan

需要更新的文档：
- `README.md` - 添加无尽模式说明
- `CLAUDE.md` - 更新架构图
- `.claude/docs/architectural_patterns.md` - 添加 RoguelikeGameMode 模式
- `.claude/skills/kz-autochess-patterns.md` - 添加无尽模式工作流

## Sources & References

### Origin

- **Origin document:** [docs/brainstorms/2026-03-21-roguelike-progression-requirements.md](../brainstorms/2026-03-21-roguelike-progression-requirements.md)

**Key decisions carried forward:**
- **独立分支架构** - 创建 `RoguelikeGameMode` 和 `RoguelikeScreen`，保留现有系统
- **事件固定间隔触发** - 每 3 阶段触发（3, 6, 9, ..., 27）
- **Boss 固定周期** - 第 10/20/30 阶段为 Boss 战
- **JSON 驱动配置** - `assets/roguelike_stage_config.json`
- **成就纯收集** - 不影响战斗平衡

### Internal References

- **GameMode 接口**: `game/GameMode.java:10-55`
- **AutoChessGameMode**: `game/AutoChessGameMode.java:31-251`
- **Manager 生命周期**: `manage/PlayerLifeManager.java:34-49`
- **事件系统**: `event/GameEventSystem.java:14-60`
- **JSON 加载**: `logic/CharacterStatsLoader.java:33-64`
- **配置类**: `logic/LifeConfig.java:26-42`
- **全局状态**: `KzAutoChess.java:42-51`
- **技能效果**: `docs/solutions/logic-errors/skill-effects-not-applied.md`
- **卡池耗尽**: `docs/solutions/architecture/shared-card-pool-depletion.md`
- **DDD 重构**: `docs/plans/2026-03-12-refactor-gamescreen-domain-driven-architecture-plan.md`
- **战斗结果界面**: `docs/plans/2026-03-08-feat-battle-result-screen-plan.md`

### External References

- **LibGDX JSON**: https://libgdx.com/wiki/articles/reading-and-writing-json
- **杀戮尖塔设计**: https://masseffect.fandom.com/wiki/Slay_the_Spire
- **Roguelike 设计模式**: https://www.gamedeveloper.com/design/the-structure-of-roguelike-games

### Related Work

- Previous PRs: #[TBD]
- Related issues: #[TBD]
- Design documents: See origin document above

---

**Generated**: 2026-03-21
**Deepened**: 2026-03-21
**Author**: Claude Code (ce-plan skill + deepen-plan)
**Status**: Ready for implementation
