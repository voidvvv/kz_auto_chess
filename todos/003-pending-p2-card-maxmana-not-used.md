---
status: pending
priority: p2
issue_id: 003
tags:
  - code-review
  - architecture
  - configuration
dependencies: []
---

# P2: Card.maxMana Field Not Used - Configuration Ignored

## Problem Statement

The `Card` class has a `maxMana` field (line 23) with getter/setter methods (lines 122-128), but this field is never actually used in `BattleUnitBlackboard.createManaForCard()`. The method always uses hardcoded default values instead of the card's configured value.

## Findings

### Evidence from Code

**Card.java** - Field exists but is unused:
```java
private SkillType skillType; // Line 22
private float maxMana;      // Line 23 - ✅ This exists!

public float getMaxMana() {
    return maxMana;
}       // Lines 122-124

public void setMaxMana(float maxMana) {
    this.maxMana = maxMana;
}       // Lines 126-128
```

**BattleUnitBlackboard.java** - Ignored configuration:
```java
private ManaComponent createManaForCard(Card card) {
    float maxMana = DEFAULT_MAX_MANA;  // ❌ Always uses default, never checks card.maxMana!
    float regenRate = DEFAULT_REGEN_RATE;
    float attackGain = DEFAULT_ATTACK_GAIN;

    // Role-based adjustment (lines 152-171)
    if (card != null) {
        switch (card.getType()) {
            case MAGE:    maxMana = 150f; break;
            case WARRIOR: maxMana = 80f; break;
            // ... etc
        }
    }

    return new ManaComponent(maxMana, regenRate, attackGain);
}
```

### Impact Analysis

| Configuration | Expected Behavior | Actual Behavior |
|--------------|------------------|----------------|
| `card.setMaxMana(200f)` | Unit gets 200 max mana | Unit gets role-based default (e.g., 150 for Mage) |
| JSON config sets `maxMana` | Respects config | Ignores config completely |

### Design Intent Violation

The plan document states:
> "Card添加skillType枚举和maxMana字段" (Card: add skillType enum and maxMana field)

This clearly indicates `maxMana` on Card should be used for **per-card configuration**, but the implementation ignores it entirely.

## Proposed Solutions

### Solution A: Check Card.maxMana First (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Respects card configuration
- Maintains role-based defaults as fallback
- Minimal code change

**Cons:**
- None

**Code Changes:**

```java
private ManaComponent createManaForCard(Card card) {
    // Check card-specific maxMana first
    float maxMana = (card != null && card.getMaxMana() > 0)
        ? card.getMaxMana()                    // ✅ Use card config
        : DEFAULT_MAX_MANA;                    // Fallback to default

    float regenRate = DEFAULT_REGEN_RATE;
    float attackGain = DEFAULT_ATTACK_GAIN;

    // Role-based adjustment (only if not overridden by card config)
    if (card != null && card.getMaxMana() <= 0) {  // ✅ Only if no card config
        switch (card.getType()) {
            case MAGE:    maxMana = 150f; break;
            case WARRIOR: maxMana = 80f; break;
            case ARCHER:  maxMana = 100f; break;
            case ASSASSIN:maxMana = 90f; break;
            case TANK:    maxMana = 70f; break;
            default:      maxMana = DEFAULT_MAX_MANA;
        }
    }

    return new ManaComponent(maxMana, regenRate, attackGain);
}
```

### Solution B: Add Card Configuration Methods for All Params

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Full configurability
- Extensible for future params

**Cons:**
- More fields on Card
- Card class becomes bloated

**Code Changes:**

```java
// Card.java - Add more configuration fields
private float maxMana;
private float manaRegenRate;
private float manaAttackGain;

// Getters/Setters...

// BattleUnitBlackboard.java - Use all card configs
private ManaComponent createManaForCard(Card card) {
    float maxMana = (card != null && card.getMaxMana() > 0)
        ? card.getMaxMana()
        : DEFAULT_MAX_MANA;

    float regenRate = (card != null && card.getManaRegenRate() > 0)
        ? card.getManaRegenRate()
        : DEFAULT_REGEN_RATE;

    float attackGain = (card != null && card.getManaAttackGain() > 0)
        ? card.getManaAttackGain()
        : DEFAULT_ATTACK_GAIN;

    // Role-based adjustments only if card doesn't override
    if (card != null && card.getMaxMana() <= 0) {
        // ... role-based switch ...
    }

    return new ManaComponent(maxMana, regenRate, attackGain);
}
```

### Solution C: Remove Card.maxMana - Use Only Role-Based

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Simplifies Card class
- Single source of truth

**Cons:**
- Breaks plan design
- Less flexibility for unique units

**Code Changes:**

```java
// Card.java - Remove unused field
// private float maxMana;  // ❌ Remove this line

// Also remove getMaxMana() and setMaxMana() methods

// Update plan document to reflect this design decision
```

## Recommended Action

**Adopt Solution A** - Check Card.maxMana first, fall back to role-based defaults. This:
- Honors the plan's design intent
- Provides maximum flexibility
- Maintains sensible defaults
- Is a minimal change

## Technical Details

### Affected Files

| File | Lines | Method |
|------|-------|--------|
| `BattleUnitBlackboard.java` | 145-174 | `createManaForCard()` |
| `Card.java` | 23, 122-128 | Field and accessors (already exist) |

### Configuration Priority (Recommended)

```
1. Card.maxMana (if > 0) ← Highest priority
2. Role-based default (Mage/Warrior/etc)
3. DEFAULT_MAX_MANA ← Fallback
```

### CharacterStats vs Card.maxMana

The code currently has:
- `CharacterStats.maxMana` - loaded from JSON (character_stats.json)
- `Card.maxMana` - per-card override

**Question:** Should CharacterStats.maxMana also be used?

**Proposed Resolution:**
```java
private ManaComponent createManaForCard(Card card) {
    float maxMana;

    // Priority: Card > Stats > Role > Default
    if (card != null && card.getMaxMana() > 0) {
        maxMana = card.getMaxMana();
    } else if (self.getStats() != null && self.getStats().getMaxMana() > 0) {
        maxMana = self.getStats().getMaxMana();
    } else if (card != null) {
        // Role-based fallback
        switch (card.getType()) {
            case MAGE: maxMana = 150f; break;
            // ...
        }
    } else {
        maxMana = DEFAULT_MAX_MANA;
    }
    // ...
}
```

### Database Changes

None.

### API Changes

None (existing field will start working).

## Acceptance Criteria

- [ ] `createManaForCard()` checks Card.maxMana first
- [ ] If Card.maxMana > 0, use that value (ignore role-based default)
- [ ] If Card.maxMana <= 0 or card is null, use role-based default
- [ ] If role-based not applicable, use DEFAULT_MAX_MANA
- [ ] Existing Card instances work correctly after fix
- [ ] New Card instances can set custom maxMana values
- [ ] Documentation updated to reflect configuration priority

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found unused field | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:50, 57`
- **Code Files:**
  - `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java:145-174`
  - `core/src/main/java/com/voidvvv/autochess/model/Card.java:23, 122-128`
- **Config File:** `assets/character_stats.json`
