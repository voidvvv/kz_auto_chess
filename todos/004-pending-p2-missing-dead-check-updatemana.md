---
status: pending
priority: p2
issue_id: 004
tags:
  - code-review
  - logic
  - bug
dependencies: []
---

# P2: Missing Death Check in updateMana() - Mana Regenerates While Dead

## Problem Statement

According to the plan document, mana should NOT update when a character is dead. However, `BattleUnitBlackboard.updateMana()` has no death check, causing mana to regenerate while the character is dead.

## Findings

### Evidence from Plan

**Plan Document:**
> "角色死亡时不更新魔法值（通过isDead()检查）"
> English: "When character is dead, do not update mana (checked via isDead())"

### Evidence from Code

**BattleUnitBlackboard.java:180-190**
```java
public void updateMana(float delta) {
    if (mana != null) return;  // ❌ Bug: should be == (separate issue, see todo 001)
    // ❌ MISSING: No death check here!
    float timeGain = mana.getRegenRate() * delta;
    mana.gainMana(timeGain);

    // Check if full and try to cast skill
    if (mana.isFull()) {
        tryCastSkill();
    }
}
```

### Compare with ManaBarRenderer

**ManaBarRenderer.java:27-30** - Has death check:
```java
// Death characters don't render mana bar
if (character.isDead()) {
    return;
}
```

### Impact Analysis

| Scenario | Expected | Actual |
|----------|----------|--------|
| Character dies | Mana stops regenerating | Mana continues regenerating |
| Mana fills while dead | Should not cast skill | May try to cast (blocked by state) |
| Character respawns | Starts with 0 mana | Has accumulated mana |

While the skill cast is blocked by the state machine (`!stateMachine.getCurrent().isState(States.NORMAL_STATE)`), allowing mana to regenerate while dead is:
1. **Incorrect behavior** per spec
2. **Wastes CPU cycles** (pointless calculation)
3. **Confusing for debugging** (unexpected state)
4. **Potential future issue** if death state logic changes

## Proposed Solutions

### Solution A: Add Death Check at Start (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Early exit, no wasted computation
- Matches plan specification
- Consistent with ManaBarRenderer

**Cons:**
- None

**Code Changes:**

```java
public void updateMana(float delta) {
    // Death check - don't regenerate mana if dead
    if (self.isDead()) {
        return;
    }

    if (mana == null) {
        return;
    }

    // Time recovery
    float timeGain = mana.getRegenRate() * delta;
    mana.gainMana(timeGain);

    // Check if full and try to cast skill
    if (mana.isFull()) {
        tryCastSkill();
    }
}
```

### Solution B: Combine Death and Null Checks

**Effort:** Small
**Risk:** Low
**Pros:**
- Single compound check
- Cleaner code

**Cons:**
- Slightly less explicit

**Code Changes:**

```java
public void updateMana(float delta) {
    // Skip if dead or no mana component
    if (self.isDead() || mana == null) {
        return;
    }

    // Time recovery
    float timeGain = mana.getRegenRate() * delta;
    mana.gainMana(timeGain);

    // Check if full and try to cast skill
    if (mana.isFull()) {
        tryCastSkill();
    }
}
```

### Solution C: Also Reset Mana on Death

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Ensures fresh start on respawn
- Prevents "cheating" by accumulating mana while dead

**Cons:**
- Changes game mechanics
- May not match design intent

**Code Changes:**

```java
// Add to BattleCharacter class
public void onDeath() {
    this.isDead = true;
    if (blackboard != null) {
        blackboard.resetMana();  // Reset mana when dying
    }
}
```

## Recommended Action

**Adopt Solution A** - Add death check at the start. This:
- Directly implements the plan specification
- Is consistent with existing rendering code
- Has minimal impact

## Technical Details

### Affected Files

| File | Lines | Method |
|------|-------|--------|
| `BattleUnitBlackboard.java` | 180-190 | `updateMana(float delta)` |

### Related Components

- `BattleCharacter.isDead()` - Returns death status
- `BattleCharacterRender` - Has death check for rendering
- `ManaBarRenderer` - Has death check for mana bar

### Death State Flow

```
Character takes damage
    ↓
Health <= 0
    ↓
BattleCharacter.setDead(true)
    ↓
State machine switches to DEAD_STATE (if exists)
    ↓
BattleUnitBlackboard.updateMana() ← Should skip here
    ↓
Next round: Character respawns
    ↓
Mana starts at 0 (should be reset)
```

### Database Changes

None.

### API Changes

None (internal logic only).

## Acceptance Criteria

- [ ] `updateMana()` checks `self.isDead()` at the start
- [ ] Returns immediately if character is dead
- [ ] Dead characters' mana does NOT regenerate
- [ ] Live characters' mana still regenerates correctly
- [ ] No performance regression (early exit actually improves performance)

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found missing check | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:84`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java:180-190`
- **Related Code:** `ManaBarRenderer.java:27-30` (death check for rendering)
