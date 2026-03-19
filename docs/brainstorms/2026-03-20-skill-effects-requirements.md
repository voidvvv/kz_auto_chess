---
date: 2026-03-20
topic: skill-effects-implementation
---

# 技能效果实现需求

## Problem Frame

自走棋游戏的技能系统框架已完整（Skill接口、SkillType枚举、魔法值系统、释放机制），但所有非基础技能类型（HEAL/AOE/BUFF/DEBUFF）都回退到只打印日志的 BasicSkill。玩家看着法力条填满却看不到任何实际效果，导致战斗体验单调、缺乏策略深度。

## Requirements

### R1. AOE 范围伤害技能
- 对释放者周围圆形范围内的所有敌人造成伤害
- 伤害数值和范围半径由 Card 配置
- 释放时显示技能名称和范围特效

### R2. HEAL 治疗技能
- 恢复释放者自身的生命值
- 治疗数值由 Card 配置
- 释放时显示技能名称和治疗特效

### R3. BUFF 增益技能
- 对场上所有友方单位施加属性增益
- 增益类型和数值由 Card 配置（复用 SynergyEffect）
- 效果持续到配置时间结束或回合结束
- 释放时显示技能名称和增益特效

### R4. DEBUFF 减益技能
- 对释放者当前的攻击目标施加属性减益
- 减益类型和数值由 Card 配置（复用 SynergyEffect）
- 效果持续到配置时间结束或回合结束
- 释放时显示技能名称和减益特效

### R5. Card 技能配置字段
- Card 需要添加以下可配置字段：
  - `skillValue` (float): 技能数值（伤害/治疗/加成量）
  - `skillRange` (float): 技能范围（AOE 半径）
  - `skillDuration` (float): 技能持续时间（BUFF/DEBUFF）

### R6. 视觉反馈
- 技能释放时在角色头顶显示技能名称
- AOE 技能显示范围指示器
- 每种技能类型有对应的粒子特效（可后续优化）

### R7. 回合清理
- 战斗结束时清除所有临时 BUFF/DEBUFF 效果

### R8. 羁绊增强（可选）
- 羁绊系统可以增强对应类型的技能效果
- 例如：MAGE 羁绊提升 AOE 伤害，WARRIOR 羁绊提升 BUFF 效果

## Success Criteria

- [ ] AOE 技能能对范围内敌人造成可见伤害
- [ ] HEAL 技能能恢复自身生命值并显示治疗数字
- [ ] BUFF 技能能给所有友方单位施加属性加成
- [ ] DEBUFF 技能能给当前目标施加属性减益
- [ ] 技能释放有明显的视觉反馈
- [ ] 回合结束后临时效果被清除
- [ ] 不同卡牌可以配置不同的技能参数

## Scope Boundaries

### In Scope
- 4 种技能类型的实现
- Card 配置字段扩展
- 基本视觉反馈（技能名称显示）
- 回合清理机制

### Out of Scope
- 复杂粒子特效系统（使用简单图形替代）
- 技能冷却时间
- 更多技能类型（召唤、传送等）
- 多人游戏同步
- 技能平衡性详细调整

## Key Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| AOE 目标选择 | 圆形范围 | 最直观的范围效果 |
| HEAL 目标选择 | 自身 | 最简单的治疗逻辑 |
| BUFF 目标选择 | 全体友方 | 增强策略意义 |
| DEBUFF 目标选择 | 当前目标 | 与攻击行为关联 |
| 数值设计 | Card 可配置 | 灵活性和可扩展性 |
| 持续时间 | 可配置，回合结束清除 | 避免跨回合复杂状态 |
| 视觉反馈 | 技能名称 + 简单特效 | MVP 阶段足够 |

## Dependencies / Assumptions

- 现有 Skill 接口和 BasicSkill 实现可用
- BattleUnitBlackboard 提供足够的战斗上下文
- SynergyEffect 可复用于 BUFF/DEBUFF 效果
- ParticleSystem 可扩展用于技能特效

## Outstanding Questions

### Deferred to Planning
- **[Affects R1] [Technical]** AOE 范围的具体默认数值
- **[Affects R3/R4] [Technical]** 临时效果存储位置设计
- **[Affects R6] [Technical]** 粒子特效的具体实现细节
- **[Affects R8] [Needs research]** 羁绊-技能增强的具体数值公式

## Next Steps

→ `/ce:plan` 进行详细实现规划
