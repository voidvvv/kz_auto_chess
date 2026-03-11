---
status: pending
priority: p1
issue_id: 001
tags:
  - code-review
  - security
  - bug
dependencies: []
---

# P1: Critical Logic Errors - Null Check Inversion

## Problem Statement

`BattleUnitBlackboard.java` contains critical logic bugs due to null check inversion in three methods. These bugs cause the system to behave in the exact opposite of what's intended, leading to complete non-functionality of the mana system.

## Findings

### Evidence from Code

**Location 1: `updateMana()` method (Line 181)**
```java
public void updateMana(float delta) {
    if (mana != null) return;  // ❌ WRONG: returns when mana IS present!
    // Time recovery
    float timeGain = mana.getRegenRate() * delta;  // ❌ NullPointerException when mana == null!
    mana.gainMana(timeGain);
    ...
}
```

**Location 2: `onAttackGainMana()` method (Line 196)**
```java
public void onAttackGainMana() {
    if (mana != null) return;  // ❌ WRONG: returns when mana IS present!
    mana.gainMana(mana.getAttackGain());  // ❌ NullPointerException when mana == null!
}
```

**Location 3: `getCurrentMana()` method (Line 371)**
```java
public float getCurrentMana() {
    return mana != null ? 0f : mana.getCurrentMana();  // ❌ WRONG: returns 0 when mana exists!
}
```

**Location 4: `getMaxMana()` method (Line 378)**
```java
public float getMaxMana() {
    return mana != null ? 0f : mana.getMaxMana();  // ❌ WRONG: returns 0 when mana exists!
}
```

### Impact Analysis

| Method | Expected Behavior | Actual Behavior | Result |
|--------|------------------|-----------------|--------|
| `updateMana()` | Skip if null, update if exists | Update if null, skip if exists | **Mana never regenerates** |
| `onAttackGainMana()` | Skip if null, add if exists | Add if null, skip if exists | **Attack never gives mana** |
| `getCurrentMana()` | Return 0 if null, value if exists | Return 0 if exists, value if null | **Mana bar always empty** |
| `getMaxMana()` | Return 0 if null, value if exists | Return 0 if exists, value if null | **Mana bar always empty** |

### Root Cause

The developer wrote `!=` instead of `==` in the null checks. This is a classic "copy-paste error" or "mental inversion" bug that:
- Compiles successfully
- Passes basic syntax checks
- Causes silent failures (no exceptions in the first two cases)
- Makes the entire feature non-functional

## Proposed Solutions

### Solution A: Fix All Null Checks (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Immediate fix for all four bugs
- No architectural changes required
- Simple to verify

**Cons:**
- None

**Code Changes:**

```java
// BattleUnitBlackboard.java:181
public void updateMana(float delta) {
    if (mana == null) return;  // ✅ FIXED
    float timeGain = mana.getRegenRate() * delta;
    mana.gainMana(timeGain);

    if (mana.isFull()) {
        tryCastSkill();
    }
}

// BattleUnitBlackboard.java:196
public void onAttackGainMana() {
    if (mana == null) return;  // ✅ FIXED
    mana.gainMana(mana.getAttackGain());
}

// BattleUnitBlackboard.java:371
public float getCurrentMana() {
    return mana == null ? 0f : mana.getCurrentMana();  // ✅ FIXED
}

// BattleUnitBlackboard.java:378
public float getMaxMana() {
    return mana == null ? 0f : mana.getMaxMana();  // ✅ FIXED
}
```

### Solution B: Add Defensive Null Checks and Logging

**Effort:** Medium
**Risk:** Low
**Pros:**
- Adds runtime safety
- Provides debugging information
- Catches unexpected nulls

**Cons:**
- Slightly more verbose
- May mask design issues

**Code Changes:**

```java
public void updateMana(float delta) {
    if (mana == null) {
        Gdx.app.error("BattleUnitBlackboard", "updateMana called with null mana component");
        return;
    }
    // ... rest of code
}

public float getCurrentMana() {
    if (mana == null) {
        Gdx.app.error("BattleUnitBlackboard", "getCurrentMana called with null mana component");
        return 0f;
    }
    return mana.getCurrentMana();
}
```

### Solution C: Make Mana Non-Null by Design

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Eliminates null checks entirely
- More aligned with immutability principles
- Cleaner code

**Cons:**
- Requires constructor changes
- May affect existing code that expects null

**Code Changes:**

```java
// In BattleUnitBlackboard
private final ManaComponent mana;  // Always initialized, never null

public BattleUnitBlackboard(BattleCharacter self, Battlefield battlefield) {
    this.self = self;
    this.battlefield = battlefield;
    this.mana = Objects.requireNonNull(createManaForCard(self.getCard()),
                                        "ManaComponent must not be null");
    this.skill = Objects.requireNonNull(createSkillForCard(self.getCard()),
                                        "Skill must not be null");
    // ... rest of code
}

// Remove all null checks - mana is guaranteed non-null
```

## Recommended Action

**Adopt Solution A** - Fix the null check inversion immediately. This is the minimal change required and has the lowest risk.

## Technical Details

### Affected Files

| File | Lines | Methods |
|------|-------|---------|
| `BattleUnitBlackboard.java` | 181, 196, 371, 378 | `updateMana()`, `onAttackGainMana()`, `getCurrentMana()`, `getMaxMana()` |

### Related Components

- `ManaComponent` (internal class)
- `Skill` interface
- `NormalState` (calls `tryCastSkill()`)
- `ManaBarRenderer` (depends on `getCurrentMana()`/`getMaxMana()`)

### Database Changes

None.

### API Changes

None (internal logic only).

## Acceptance Criteria

- [ ] All four null check inversions are fixed
- [ ] `updateMana()` correctly regenerates mana when mana component exists
- [ ] `onAttackGainMana()` correctly adds mana on attack
- [ ] `getCurrentMana()` returns the actual current mana value when mana exists
- [ ] `getMaxMana()` returns the actual max mana value when mana exists
- [ ] Mana bar displays correctly in the game
- [ ] Skills can be cast when mana is full
- [ ] Unit tests pass (if applicable)

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found bugs | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java`
- **Similar Pattern:** Review existing null-check patterns in the codebase
