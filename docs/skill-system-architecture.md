# Skill System Architecture

## Overview

The skill system is a flexible, extensible framework for implementing character abilities in the auto-chess game. It follows the Strategy pattern and adheres to Java coding standards.

## Design Patterns

### Strategy Pattern
The `Skill<T>` interface defines the contract for all skill implementations:
- `getName()`: Returns the skill's display name
- `cast(T context)`: Executes the skill logic

### Factory Pattern
`SkillFactory` provides centralized skill creation with proper validation and configuration.

### Value Object Pattern
`SkillContext` and `TemporaryEffect` are immutable records that encapsulate skill parameters.

## Package Structure

```
com.voidvvv.autochess.model.skill/
├── exception/              # Domain-specific exceptions
│   ├── SkillException.java
│   ├── SkillExecutionException.java
│   └── InvalidTargetException.java
├── AoeSkill.java          # Area of effect damage
├── BasicSkill.java        # Basic implementation
├── BuffSkill.java         # Stat boost effects
├── DebuffSkill.java       # Stat reduction effects
├── HealSkill.java         # Healing effects
├── SkillContext.java      # Skill parameters (record)
├── SkillFactory.java      # Skill creation factory
└── TemporaryEffect.java   # Effect tracking (record)
```

## Key Classes

### SkillContext (Immutable Record)
Encapsulates all configurable skill parameters:
- `skillValue`: Primary effect value
- `skillRange`: Maximum effect distance
- `skillDuration`: Effect duration for BUFF/DEBUFF
- `damageType`: Damage type for damage skills

### TemporaryEffect (Immutable Record)
Represents temporary stat modifications:
- `effectType`: BUFF or DEBUFF
- `value`: Effect magnitude
- `duration`: Total duration
- `remainingTime`: Time remaining
- `damageType`: Associated damage type

### Skill Implementations

#### AoeSkill
Deals damage to all enemies within range.

**Usage:**
```java
SkillContext context = SkillContext.of(100f, 150f, 0f);
AoeSkill skill = new AoeSkill(context);
skill.cast(blackboard);
```

#### HealSkill
Restores health to all allies within range.

**Usage:**
```java
SkillContext context = SkillContext.of(50f, 100f, 0f);
HealSkill skill = new HealSkill(context);
skill.cast(blackboard);
```

#### BuffSkill
Applies stat boosts to all allies within range.

**Usage:**
```java
SkillContext context = SkillContext.of(10f, 100f, 5f);
BuffSkill skill = new BuffSkill(context);
skill.cast(blackboard);
```

#### DebuffSkill
Applies stat reductions to all enemies within range.

**Usage:**
```java
SkillContext context = SkillContext.of(10f, 100f, 5f);
DebuffSkill skill = new DebuffSkill(context);
skill.cast(blackboard);
```

## Card Integration

The `Card` model has been extended with skill parameters:

```java
// New fields in Card
private float skillValue;
private float skillRange;
private float skillDuration;
private Damage.DamageType skillDamageType;

// New accessor methods with validation
public float getSkillValue()
public void setSkillValue(float skillValue)
public float getSkillRange()
public void setSkillRange(float skillRange)
public float getSkillDuration()
public void setSkillDuration(float skillDuration)
public Damage.DamageType getSkillDamageType()
public void setSkillDamageType(Damage.DamageType skillDamageType)
```

## Error Handling

The skill system uses domain-specific exceptions:

- `SkillException`: Base exception for all skill errors
- `SkillExecutionException`: Runtime errors during skill execution
- `InvalidTargetException`: Invalid target selection (null, dead, out of range)

**Example:**
```java
try {
    skill.cast(blackboard);
} catch (InvalidTargetException e) {
    Gdx.app.error(TAG, "Invalid target: " + e.getMessage());
} catch (SkillExecutionException e) {
    Gdx.app.error(TAG, "Skill execution failed: " + e.getMessage());
}
```

## Logging

All skills use LibGDX logging instead of System.out:

```java
Gdx.app.log(TAG, String.format("[%s] cast %s", caster.getName(), getName()));
Gdx.app.error(TAG, "Skill failed", exception);
```

## Constants

Magic numbers are replaced with named constants:

```java
private static final float DEFAULT_SKILL_VALUE = 50f;
private static final float DEFAULT_SKILL_RANGE = 100f;
private static final float DEFAULT_SKILL_DURATION = 5f;
private static final float MINIMUM_ATTACK = 0f;
```

## Validation

All inputs are validated at system boundaries:

```java
public void setSkillValue(float skillValue) {
    if (skillValue < 0f) {
        throw new IllegalArgumentException("Skill value cannot be negative: " + skillValue);
    }
    this.skillValue = skillValue;
}
```

## Future Extensions

### Adding New Skills

1. Create new skill class implementing `Skill<BattleUnitBlackboard>`
2. Add new `SkillType` enum value
3. Update `SkillFactory.createSkill()` switch statement
4. Add skill-specific logic and validation

### Effect System Enhancement

1. Implement effect management in `BattleCharacter`
2. Add effect update logic in game loop
3. Create visual effects for buff/debuff states
4. Implement stacking rules for multiple effects

### Skill Cooldowns

1. Add `cooldown` field to `SkillContext`
2. Track last cast time in skill instances
3. Implement cooldown checking before casting
4. Display cooldown status in UI
