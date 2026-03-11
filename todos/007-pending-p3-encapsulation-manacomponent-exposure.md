---
status: pending
priority: p3
issue_id: 007
tags:
  - code-review
  - encapsulation
  - design
dependencies: []
---

# P3: ManaComponent Encapsulation Violation - Internal Class Exposed

## Problem Statement

`ManaComponent` is a private static inner class of `BattleUnitBlackboard`, but it's exposed through a public `getMana()` method. This violates encapsulation principles and:
1. Breaks the "private internal class" design intent
2. Allows external code to modify internal state directly
3. Makes refactoring harder (can't change ManaComponent without breaking API)
4. Violates the "tell, don't ask" principle

## Findings

### Evidence from Code

**BattleUnitBlackboard.java:43-88** - ManaComponent is private:
```java
/**
 * Mana component (internal class, pure data)
 * Stores character mana state
 */
private static class ManaComponent {  // ✅ Private
    float currentMana;
    float maxMana;
    float regenRate;
    float attackGain;

    // ... private methods ...
}
```

**BattleUnitBlackboard.java:233-237** - But exposed publicly:
```java
/**
 * Get mana component
 */
public ManaComponent getMana() {  // ❌ Exposes private inner class!
    return mana;
}
```

### Design Intent Contradiction

The JavaDoc comment clearly states:
> "Mana component (internal class, pure data)"

But the public getter contradicts this by exposing it externally.

### Usage Analysis

Let's check if any code actually uses `getMana()`:

```java
// Current code uses these methods instead:
blackboard.getCurrentMana()   // Line 33 in ManaBarRenderer
blackboard.getMaxMana()       // Line 34 in ManaBarRenderer
blackboard.getManaRatio()     // Line 385 in BattleUnitBlackboard

// No code appears to use getMana() directly!
```

### Encapsulation Comparison

| Access | Current | Should Be |
|--------|---------|-----------|
| ManaComponent class | Private | ✅ Private |
| `getMana()` method | Public | ❌ Private or remove |
| `getCurrentMana()` | Public | ✅ Public |
| `getMaxMana()` | Public | ✅ Public |
| `getManaRatio()` | Public | ✅ Public |

## Proposed Solutions

### Solution A: Remove getMana() Entirely (Recommended)

**Effort:** Small
**Risk:** Very Low
**Pros:**
- True encapsulation
- Simpler API
- Clearer separation of concerns
- Easier to refactor ManaComponent

**Cons:**
- If external code used it, would break
- But our analysis shows no usage

**Code Changes:**

```java
// BattleUnitBlackboard.java - Remove lines 233-237
// DELETE THIS:
// /**
//  * Get mana component
//  */
// public ManaComponent getMana() {
//     return mana;
// }
```

### Solution B: Make getMana() Private

**Effort:** Small
**Risk:** Low
**Pros:**
- Still accessible within class
- Could be used for testing
- Public API change if needed

**Cons:**
- Still exposes to same class (less pure)
- Why have it if not used?

**Code Changes:**

```java
/**
 * Get mana component (internal use only)
 */
private ManaComponent getMana() {  // ✅ Made private
    return mana;
}
```

### Solution C: Return Immutable Wrapper

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Still provides access if needed
- Protects internal state
- Future-proof

**Cons:**
- Additional code
- Overkill if not used
- Adds complexity

**Code Changes:**

```java
public class ManaComponent {
    // ... existing code ...

    public ManaComponentSnapshot toSnapshot() {
        return new ManaComponentSnapshot(
            currentMana,
            maxMana,
            regenRate,
            attackGain
        );
    }
}

public static class ManaComponentSnapshot {
    private final float currentMana;
    private final float maxMana;
    private final float regenRate;
    private final float attackGain;

    // Constructor, getters only - no setters!
}

// In BattleUnitBlackboard
public ManaComponentSnapshot getManaSnapshot() {
    return mana.toSnapshot();
}
```

## Recommended Action

**Adopt Solution A** - Remove `getMana()` entirely. This is justified because:
1. The class is documented as "internal class, pure data"
2. No code in the current codebase uses it
3. Public accessors (`getCurrentMana()`, `getMaxMana()`, `getManaRatio()`) provide all needed functionality
4. Follows encapsulation best practices

## Technical Details

### Affected Files

| File | Lines | Action |
|------|-------|--------|
| `BattleUnitBlackboard.java` | 233-237 | Remove public `getMana()` method |

### Accessor Methods (Keep These)

| Method | Purpose | Keep? |
|--------|---------|-------|
| `getCurrentMana()` | Get current mana value | ✅ Yes |
| `getMaxMana()` | Get max mana value | ✅ Yes |
| `getManaRatio()` | Get mana ratio (0-1) | ✅ Yes |
| `updateMana()` | Update mana over time | ✅ Yes |
| `onAttackGainMana()` | Add mana on attack | ✅ Yes |
| `tryCastSkill()` | Attempt to cast skill | ✅ Yes |
| `resetMana()` | Reset to 0 | ✅ Yes |
| `getMana()` | Get component | ❌ Remove |

### Design Pattern: Law of Demeter

The current `getMana()` violates the Law of Demeter:
```java
// Don't do this (violates Law of Demeter):
ManaComponent mc = blackboard.getMana();
float mana = mc.getCurrentMana();

// Do this instead:
float mana = blackboard.getCurrentMana();
```

### Database Changes

None.

### API Changes

Removal of public method (low risk - no usage found).

## Acceptance Criteria

- [ ] `getMana()` method is removed from BattleUnitBlackboard
- [ ] No code in project uses `getMana()`
- [ ] All mana access goes through `getCurrentMana()`, `getMaxMana()`, `getManaRatio()`
- [ ] ManaComponent remains private static inner class
- [ ] Code compiles without errors
- [ ] No tests fail (if tests exist)

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found encapsulation issue | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:46`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java:233-237`
- **Design Principle:** Law of Demeter (minimum knowledge principle)
