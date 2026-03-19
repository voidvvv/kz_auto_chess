# 技能系统设计头脑风暴

**日期**: 2026-03-11 (创建) / 2026-03-20 (更新)
**功能**: 为KZ AutoChess游戏添加技能机制

---

## 我们要构建什么

为游戏角色添加技能系统，包括：

1. **魔法值系统**：每个角色有可配置的魔法值上限，初始值为0 ✅
2. **魔法值增长**：随时间自然恢复 + 攻击时额外获得 ✅
3. **技能释放**：魔法值满时，在空闲状态下释放技能 ✅
4. **技能渲染**：在角色下方显示魔法条 ✅
5. **可扩展架构**：遵循开闭原则，方便添加新技能 ✅
6. **具体技能效果**：实现 AOE/HEAL/BUFF/DEBUFF 四种技能的实际效果 🚧

---

## 具体技能效果设计 (2026-03-20 更新)

### 技能类型与目标选择

| 技能类型 | 目标选择 | 效果描述 |
|---------|---------|---------|
| **AOE** | 圆形范围 | 对释放者周围一定半径内的所有敌人造成伤害 |
| **HEAL** | 自身 | 恢复释放者自身生命值 |
| **BUFF** | 全体友方 | 对场上所有友方单位施加属性增益 |
| **DEBUFF** | 当前目标 | 对释放者当前的攻击目标施加属性减益 |

### 数值设计

- **伤害/治疗/加成数值**: 可配置，由 Card 定义
  - Card 需要新增字段: `skillValue` (float)
  - AOE: skillValue 表示伤害量
  - HEAL: skillValue 表示治疗量
  - BUFF/DEBUFF: skillValue 表示加成百分比

- **AOE 范围**: 可配置，由 Card 定义
  - Card 需要新增字段: `skillRange` (float)

- **BUFF/DEBUFF 持续时间**: 可配置，回合结束清除
  - Card 需要新增字段: `skillDuration` (float，秒)
  - 战斗结束时清除所有临时效果

### 视觉反馈

- **技能名称显示**: 在角色头顶显示技能名称（如 "[单位名] 释放了 AOE!"）
- **粒子特效**:
  - AOE: 圆形爆炸特效
  - HEAL: 治疗光圈特效
  - BUFF: 增益光环特效
  - DEBUFF: 减益暗影特效

### 羁绊-技能关联

羁绊可以增强对应类型的技能效果：

| 羁绊类型 | 增强的技能 | 增强效果 |
|---------|-----------|---------|
| MAGE | AOE | 提升范围伤害 |
| WARRIOR | BUFF | 提升增益效果 |
| ARCHER | DEBUFF | 提升减益效果 |
| TANK | HEAL | 提升治疗效果 |

---

## 为什么选择策略模式方案

### 方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| **策略模式（已选）** | 符合现有架构、易于扩展、逻辑清晰 | 需要创建多个类 |
| 事件驱动 | 高度解耦、适合复杂交互 | 复杂度高、调试困难 |
| 极简方案 | 实现最快 | 扩展性差、违反开闭原则 |

### 选择理由

1. **符合项目架构**：项目的model/updater/render分离模式与策略模式天然契合
2. **开闭原则**：新增技能只需实现Skill接口，无需修改现有代码
3. **易于理解**：策略模式在游戏开发中是经典方案，团队容易接受

---

## 关键设计决策

### 1. 数据存放位置

**决策**：魔法值相关数据放在`BattleUnitBlackboard`中，作为内部类`ManaComponent`

**理由**：
- 魔法值是战斗状态，属于临时属性
- `BattleCharacter`保持纯数据模型，只存放基础属性
- 作为内部类可以高效访问BattleUnitBlackboard的战斗上下文
- 符合项目"battleBlackboard用于战斗相关能力"的设计原则

### 2. 魔法值上限设计

**决策**：通过`CharacterStats`添加`maxMana`字段配置

**理由**：
- 不同角色类型（法师、战士）应该有不同的魔法值上限
- 数据驱动设计，便于平衡调整
- 羁绊效果可以通过修改BattleUnitBlackboard中的maxMana来实现

### 3. 技能释放时机

**决策**：只在角色空闲状态时释放

**理由**：
- 避免打断攻击动作，游戏体验更流畅
- 状态机已经有"空闲状态"的判断，复用现有逻辑

### 4. 技能释放后处理

**决策**：清零重置魔法值

**理由**：
- 简单直接，避免复杂的消耗计算
- 玩家可以预期技能释放的节奏

### 5. 技能系统架构

**核心组件**：

```
model/Skill.java                    # 技能接口 ✅
model/skill/BasicSkill.java         # 基础技能实现 ✅
model/skill/AoeSkill.java           # AOE技能实现 🚧
model/skill/HealSkill.java          # 治疗技能实现 🚧
model/skill/BuffSkill.java          # 增益技能实现 🚧
model/skill/DebuffSkill.java        # 减益技能实现 🚧

battle/BattleUnitBlackboard.java    # 添加ManaComponent内部类和技能管理方法 ✅

render/ManaBarRenderer.java         # 魔法条渲染器 ✅
render/SkillEffectRenderer.java     # 技能特效渲染器 🚧

model/Card.java                     # 添加技能配置字段 🚧
```

### 6. Card 扩展字段 (新增)

```java
// Card.java 需要添加的字段
private float skillValue;      // 技能数值（伤害/治疗/加成量）
private float skillRange;      // 技能范围（AOE 半径）
private float skillDuration;   // 技能持续时间（BUFF/DEBUFF）
```

---

## 架构设计

### 类结构

```java
// 技能接口
public interface Skill<T> {
    String getName();
    void cast(T context);
}

// AOE 技能示例
public class AoeSkill implements Skill<BattleUnitBlackboard> {
    @Override
    public String getName() {
        return "范围攻击";
    }

    @Override
    public void cast(BattleUnitBlackboard blackboard) {
        BattleCharacter self = blackboard.getSelf();
        Battlefield battlefield = blackboard.getBattlefield();
        Card card = self.getCard();

        float range = card.getSkillRange();
        float damage = card.getSkillValue();

        // 应用羁绊加成
        damage = applySynergyBonus(self, damage);

        // 获取范围内的敌人
        List<BattleCharacter> enemies = battlefield.getOpponents(self);
        for (BattleCharacter enemy : enemies) {
            float distance = getDistance(self, enemy);
            if (distance <= range) {
                dealDamage(enemy, damage);
            }
        }

        // 触发视觉特效
        triggerAoeEffect(self, range);
    }
}
```

### 集成点

1. **BattleUnitBlackboard**：添加`ManaComponent`内部类、`skill`字段和相关方法 ✅
2. **BattleUnitBlackboard.update()**：调用`updateMana(delta)`更新魔法值 ✅
3. **BattleUnitBlackboard.onMessageDoAttack()**：攻击成功后调用`onAttackGainMana()` ✅
4. **NormalState.update()**：调用`tryCastSkill()`尝试释放技能 ✅
5. **BattleCharacterRender**：渲染魔法条 ✅
6. **BattleUnitBlackboard.createSkillForCard()**：根据 SkillType 创建对应技能实例 🚧
7. **Card**：添加 skillValue/skillRange/skillDuration 字段 🚧
8. **SkillEffectRenderer**：渲染技能视觉特效 🚧
9. **BattleManager.endBattle()**：清除所有临时 BUFF/DEBUFF 🚧

---

## 未解决的问题

### Resolve Before Planning
无阻塞问题

### Deferred to Planning
1. **[Affects AOE] [Technical]** AOE 范围的具体数值（半径多少像素）
2. **[Affects BUFF/DEBUFF] [Technical]** 临时效果存储位置（BattleCharacter 还是 BattleUnitBlackboard）
3. **[Affects VFX] [Technical]** 粒子特效的具体实现方式（复用现有 ParticleSystem 还是新建）
4. **[Affects Balance] [Needs research]** 技能数值的平衡性（需要测试调整）

---

## 已解决的问题

1. ~~魔法值上限如何设计？~~ → 可配置参数，通过CharacterStats定义 ✅
2. ~~技能释放后魔法值如何处理？~~ → 清零重置 ✅
3. ~~技能释放时机？~~ → 空闲时释放 ✅
4. ~~使用哪种架构？~~ → 策略模式方案 ✅
5. ~~AOE 目标选择？~~ → 圆形范围 ✅
6. ~~HEAL 目标选择？~~ → 自身治疗 ✅
7. ~~BUFF 目标选择？~~ → 全体友方增益 ✅
8. ~~DEBUFF 目标选择？~~ → 当前目标减益 ✅
9. ~~技能数值设计？~~ → 可配置（Card 定义）✅
10. ~~BUFF/DEBUFF 持续时间？~~ → 可配置，回合结束清除 ✅
11. ~~视觉反馈？~~ → 粒子特效 + 技能名称显示 ✅
12. ~~羁绊关联？~~ → 羁绊可增强对应类型技能 ✅

---

## 后续考虑

**第一优先级**（本次实现）：
1. 实现 4 种技能类（AoeSkill, HealSkill, BuffSkill, DebuffSkill）
2. Card 添加技能配置字段
3. 修改 createSkillForCard() 返回对应技能实例
4. 添加简单的视觉反馈（技能名称显示）
5. 回合结束时清除临时效果

**第二优先级**（后续优化）：
1. 粒子特效实现
2. 羁绊-技能增强关联
3. 技能平衡性调整

**未来扩展**（不在当前范围）：
- 更多技能类型（召唤、传送等）
- 技能冷却时间
- 复杂的技能组合效果

---

## 下一步

→ `/ce:plan` 进行详细实现规划
