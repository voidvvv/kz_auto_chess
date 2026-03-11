# 技能系统设计头脑风暴

**日期**: 2026-03-11
**功能**: 为KZ AutoChess游戏添加技能机制

---

## 我们要构建什么

为游戏角色添加技能系统，包括：

1. **魔法值系统**：每个角色有可配置的魔法值上限，初始值为0
2. **魔法值增长**：随时间自然恢复 + 攻击时额外获得
3. **技能释放**：魔法值满时，在空闲状态下释放技能
4. **技能渲染**：在角色下方显示魔法条
5. **可扩展架构**：遵循开闭原则，方便添加新技能

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
model/Skill.java                    # 技能接口
model/skill/BasicSkill.java         # 基础技能实现（控制台打印）

battle/BattleUnitBlackboard.java    # 添加ManaComponent内部类和技能管理方法

render/ManaBarRenderer.java        # 魔法条渲染器

model/Card.java                     # 添加maxMana字段和skillType配置
model/CharacterStats.java           # 添加maxMana字段
```

---

## 架构设计

### 类结构

```java
// 技能接口
public interface Skill {
    String getName();
    void cast(BattleUnitBlackboard blackboard);
}

// BattleUnitBlackboard内部类 - 魔法值组件（纯数据）
private static class ManaComponent {
    float currentMana;
    float maxMana;
    float regenRate;     // 每秒恢复量
    float attackGain;    // 攻击获得量
}

// BattleUnitBlackboard添加的字段和方法
private ManaComponent mana;
private Skill skill;

// BattleUnitBlackboard中的方法
public void updateMana(float delta) {
    // 增加魔法值
    // 检查是否满并尝试释放技能
}

public void onAttackGainMana() {
    // 攻击时额外增加魔法值
}

public boolean isManaFull() {
    return mana.currentMana >= mana.maxMana;
}

public boolean tryCastSkill() {
    // 只有在空闲状态才释放技能
    if (isManaFull() && stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        skill.cast(this);
        mana.currentMana = 0;
        return true;
    }
    return false;
}
```

### Card/CharacterStats扩展

```java
// CharacterStats添加
private float maxMana;

// Card添加
private SkillType skillType;  // 技能类型枚举
private float maxMana;        // 覆盖默认上限

enum SkillType {
    BASIC,       // 基础技能（控制台打印）
    HEAL,       // 治疗
    AOE,        // 范围伤害
    // 未来扩展
}
```

### 集成点

1. **BattleUnitBlackboard**：添加`ManaComponent`内部类、`skill`字段和相关方法
2. **BattleUnitBlackboard.update()**：调用`updateMana(delta)`更新魔法值
3. **BattleUnitBlackboard.onMessageDoAttack()**：攻击成功后调用`onAttackGainMana()`
4. **NormalState.update()**：调用`tryCastSkill()`尝试释放技能
5. **BattleCharacterRender**：渲染魔法条

---

## 未解决的问题

暂无

---

## 已解决的问题

1. ~~魔法值上限如何设计？~~ → 可配置参数，通过CharacterStats定义
2. ~~技能释放后魔法值如何处理？~~ → 清零重置
3. ~~技能释放时机？~~ → 空闲时释放
4. ~~使用哪种架构？~~ → 策略模式方案

---

## 后续考虑

**第一优先级**（规划时确定具体数值）：
1. 不同角色类型的默认魔法值上限
2. 魔法值恢复速率和攻击获得量的具体数值
3. 基础技能的控制台输出格式

**未来扩展**（不在当前范围）：
- 具体技能效果的设计（治疗、伤害、增益等）
- 技能冷却时间
- 技能释放的视觉反馈（特效）
- 羁绊效果影响魔法值上限
