---
title: "Skill Effects System Implementation - All Skill Types Falling Back to BasicSkill"
category: "logic-errors"
date: "2026-03-20"
tags: [libgdx, skill-system, battle-mechanics, temporary-effects, auto-chess, strategy-pattern]
module: "battle-skill-system"
symptom: "All skill types (AOE/HEAL/BUFF/DEBUFF) were falling back to BasicSkill which only printed log messages without applying actual game effects"
root_cause: "Skill implementations (HealSkill, BuffSkill, DebuffSkill) lacked proper effect application logic and temporary effect tracking mechanism was missing in BattleCharacter"
solution: "Implemented proper skill behaviors: HealSkill as self-only healing, BuffSkill using addTemporaryEffect() for allies, DebuffSkill targeting current enemy with addTemporaryEffect(). Added temporary effect tracking (ObjectMap<SynergyEffect, Float>) to BattleCharacter with updateTemporaryEffects() called in BattleUnitBlackboard.update()"
---

# Skill Effects System Implementation

## Problem Description

In the KzAutoChess auto-chess game built with LibGDX, the skill system framework was complete (Skill interface, SkillType enum, mana system, cast mechanism), but all non-basic skill types (HEAL/AOE/BUFF/DEBUFF) were falling back to `BasicSkill` which only printed log messages. Players would see their mana bars fill up but observe no actual effects, resulting in monotonous gameplay and lack of strategic depth.

## Investigation Steps

1. **Analyzed existing skill implementation** - Examined `HealSkill`, `BuffSkill`, and `DebuffSkill` classes to understand current behavior
2. **Identified Blackboard pattern** - Found `BattleUnitBlackboard` manages battle state and character updates
3. **Reviewed SynergyEffect system** - Discovered the temporary effect mechanism needed enhancement
4. **Traced character update flow** - Found where temporary effects should be cleaned up during battle updates
5. **Checked SkillFactory** - Verified it correctly creates skill instances with Card parameters

## Root Cause Analysis

The skill implementations had several issues:

- **HealSkill** was healing all allies in range instead of just the caster (violated R2 requirement)
- **BuffSkill** was not using the new temporary effect system for proper expiration tracking
- **DebuffSkill** was applying debuffs to all enemies in range instead of just the current target (violated R4 requirement)
- **BattleCharacter** lacked proper temporary effect expiration tracking
- **SynergyEffect** was missing setter methods for bonus fields needed for dynamic effect creation

## Working Solution

### 1. HealSkill - Self-Only Healing (R2)

```java
@Override
public void cast(BattleUnitBlackboard blackboard) {
    BattleCharacter caster = blackboard.getSelf();
    if (caster == null || caster.isDead()) {
        return;
    }

    // R2: Self-only healing
    float healAmount = calculateHealAmount(caster);
    float actualHealed = healCharacter(caster, healAmount);

    Gdx.app.log(TAG, String.format("[%s] cast %s on self, healed: %.1f",
            caster.getName(), getName(), actualHealed));
}
```

### 2. BuffSkill - Temporary Effect System for All Allies (R3)

```java
private void applyBuffToAllies(BattleCharacter caster, List<BattleCharacter> allies,
                               BattleUnitBlackboard blackboard) {
    float currentTime = caster.currentTime;
    float duration = context.skillDuration();
    float bonusValue = context.skillValue() / 100f;

    for (BattleCharacter ally : allies) {
        SynergyEffect buffEffect = new SynergyEffect(
                "BUFF_" + caster.getName() + "_" + System.currentTimeMillis(),
                bonusValue,  // attackBonus
                0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
        );
        ally.addTemporaryEffect(buffEffect, duration, currentTime);
    }
}
```

### 3. DebuffSkill - Target-Only Debuff (R4)

```java
@Override
public void cast(BattleUnitBlackboard blackboard) {
    BattleCharacter caster = blackboard.getSelf();

    // R4: Current target only
    BattleCharacter target = caster.getTarget();
    if (target == null || target.isDead()) {
        Gdx.app.log(TAG, caster.getName() + " has no valid target");
        return;
    }

    float debuffValue = -context.skillValue() / 100f;
    SynergyEffect debuffEffect = new SynergyEffect(
            "DEBUFF_" + caster.getName() + "_" + System.currentTimeMillis(),
            debuffValue,  // negative for debuff
            0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
    );
    target.addTemporaryEffect(debuffEffect, duration, currentTime);
}
```

### 4. BattleCharacter - Temporary Effect Tracking

```java
// Add field
private final ObjectMap<SynergyEffect, Float> effectExpirations = new ObjectMap<>();

public void addTemporaryEffect(SynergyEffect effect, float duration, float currentTime) {
    float expirationTime = currentTime + duration;
    effectExpirations.put(effect, expirationTime);
    activeSynergyEffects.put(effect.getSynergyName(), effect);
}

public void updateTemporaryEffects(float currentTime) {
    ObjectMap.Entries<SynergyEffect, Float> iterator = effectExpirations.iterator();
    while (iterator.hasNext()) {
        ObjectMap.Entry<SynergyEffect, Float> entry = iterator.next();
        if (currentTime >= entry.value) {
            activeSynergyEffects.remove(entry.key.getSynergyName());
            iterator.remove();
        }
    }
}

public void clearTemporaryEffects() {
    for (ObjectMap.Entry<SynergyEffect, Float> entry : effectExpirations) {
        activeSynergyEffects.remove(entry.key.getSynergyName());
    }
    effectExpirations.clear();
}
```

### 5. SynergyEffect - Add Setters

```java
public void setAttackBonus(float attackBonus) { this.attackBonus = attackBonus; }
public void setDefenseBonus(float defenseBonus) { this.defenseBonus = defenseBonus; }
public void setMagicBonus(float magicBonus) { this.magicBonus = magicBonus; }
// ... setters for all 12 bonus fields
```

### 6. BattleUnitBlackboard - Update Integration

```java
public void update(float delta) {
    updateMana(delta);

    // Update temporary effects each frame
    this.self.updateTemporaryEffects(this.self.currentTime);

    this.stateMachine.update(delta);
    this.self.attackCooldown -= delta;
}
```

### 7. Reset Cleanup (Already Handled)

The `BattleCharacter.reset()` method already calls `clearTemporaryEffects()`, which is invoked by `BattleManager.endBattle()` for all characters.

## Prevention Strategies

1. **Test-Driven Development**: Write tests for skill effects before implementation
2. **Interface Contracts**: Define clear contracts for what each skill type should do
3. **Integration Testing**: Test skill effects in full battle context, not just unit tests
4. **Code Review Checklist**: Verify skill implementations apply actual effects, not just logging

## Key Files Modified

| File | Change |
|------|--------|
| `SynergyEffect.java` | Added setters for all 12 bonus fields |
| `BattleCharacter.java` | Added temporary effect tracking system with ObjectMap |
| `HealSkill.java` | Changed from AOE heal to self-only heal |
| `BuffSkill.java` | Updated to use new temporary effect system |
| `DebuffSkill.java` | Changed to target only current target, use new temp effect system |
| `BattleUnitBlackboard.java` | Added `updateTemporaryEffects()` call in update loop |

## References

- Requirements: `docs/brainstorms/2026-03-20-skill-effects-requirements.md`
- Implementation Plan: `docs/plans/2026-03-20-001-feat-skill-effects-implementation-plan.md`
- Brainstorm: `docs/brainstorms/2026-03-11-skill-system-brainstorm.md`
