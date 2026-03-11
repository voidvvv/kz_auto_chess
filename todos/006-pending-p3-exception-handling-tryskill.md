---
status: pending
priority: p3
issue_id: 006
tags:
  - code-review
  - error-handling
  - logging
dependencies: []
---

# P3: Basic Exception Handling in tryCastSkill() - No Error Recovery

## Problem Statement

The `tryCastSkill()` method has basic try-catch exception handling, but it:
1. Only logs to `System.err` (doesn't use LibGDX logging)
2. Returns false without distinguishing different error types
3. Provides no way to recover from transient errors
4. Doesn't notify the game system of skill failures

## Findings

### Evidence from Code

**BattleUnitBlackboard.java:205-223**
```java
public boolean tryCastSkill() {
    if (mana == null || skill == null) return false;
    if (!mana.isFull()) return false;

    // Only cast in idle state
    if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        return false;
    }

    try {
        skill.cast(this);
        // Reset mana after successful cast
        mana.reset();
        return true;
    } catch (Exception e) {
        System.err.println("技能释放失败: " + e.getMessage());  // ❌ Uses System.err
        return false;  // ❌ No distinction between failure types
    }
}
```

### Error Scenarios Not Handled

| Scenario | Current Behavior | Desired Behavior |
|----------|------------------|-----------------|
| NullPointerException | Logs null message | Indicates missing component |
| IllegalStateException | Logs generic message | Indicates state issue |
| Custom SkillException | Logs generic message | Could indicate cooldown, mana issue |
| Transient error | Logs, returns false | Could retry |

### Logging Inconsistency

Looking at other logging in the codebase:
```java
// ManaBarRenderer - No logging at all
// BattleUnitBlackboard.updateMana() - No logging
// Pattern suggests minimal logging, but if we log, use Gdx.app.error()
```

## Proposed Solutions

### Solution A: Use LibGDX Logging with Error Types (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Proper logging framework
- Error type information
- Consistent with LibGDX apps

**Cons:**
- Slightly more code

**Code Changes:**

```java
public boolean tryCastSkill() {
    if (mana == null || skill == null) return false;
    if (!mana.isFull()) return false;

    if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        return false;
    }

    try {
        skill.cast(this);
        mana.reset();
        return true;
    } catch (NullPointerException e) {
        Gdx.app.error("SkillSystem", "Null component in skill cast", e);
        return false;
    } catch (IllegalStateException e) {
        Gdx.app.error("SkillSystem", "Invalid state for skill cast", e);
        return false;
    } catch (Exception e) {
        Gdx.app.error("SkillSystem", "Unexpected error casting skill: " + skill.getName(), e);
        return false;
    }
}
```

### Solution B: Add SkillException Hierarchy

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Rich error information
- Can handle different failure reasons
- Extensible for custom skills

**Cons:**
- Requires creating new exception classes
- More complex

**Code Changes:**

```java
// New exception classes
public class SkillException extends Exception {
    private final SkillFailureReason reason;

    public enum SkillFailureReason {
        COOLDOWN_NOT_READY,
        INSUFFICIENT_MANA,
        NO_VALID_TARGET,
        STATE_INVALID,
        UNKNOWN
    }

    public SkillException(String message, SkillFailureReason reason) {
        super(message);
        this.reason = reason;
    }

    public SkillFailureReason getReason() {
        return reason;
    }
}

// In tryCastSkill()
public boolean tryCastSkill() {
    if (mana == null || skill == null) return false;
    if (!mana.isFull()) return false;

    if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        return false;
    }

    try {
        skill.cast(this);
        mana.reset();
        return true;
    } catch (SkillException e) {
        Gdx.app.error("SkillSystem",
            "Skill cast failed: " + e.getReason() + " - " + e.getMessage());
        // Could trigger different UI feedback based on reason
        return false;
    } catch (Exception e) {
        Gdx.app.error("SkillSystem", "Unexpected error casting skill", e);
        return false;
    }
}
```

### Solution C: Keep Simple - Remove Try-Catch

**Effort:** Small
**Risk:** Low
**Pros:**
- Less code
- Let exceptions propagate (might be desired for debugging)
- Fails fast

**Cons:**
- No error handling at this level
- May crash the game
- Goes against plan spec

**Code Changes:**

```java
public boolean tryCastSkill() {
    if (mana == null || skill == null) return false;
    if (!mana.isFull()) return false;

    if (!stateMachine.getCurrent().isState(States.NORMAL_STATE)) {
        return false;
    }

    // Just call directly - let exceptions propagate
    skill.cast(this);
    mana.reset();
    return true;
}
```

## Recommended Action

**Adopt Solution A** - Use LibGDX logging with basic error type distinction. This:
- Provides proper logging framework
- Gives minimal error type info
- Follows LibGDX best practices
- Low risk

**Note:** Solution B (custom exceptions) is overkill for MVP. Consider for future iteration.

## Technical Details

### Affected Files

| File | Lines | Method |
|------|-------|--------|
| `BattleUnitBlackboard.java` | 205-223 | `tryCastSkill()` |

### LibGDX Logging API

```java
// Priority levels
Gdx.app.debug(String tag, String message)
Gdx.app.log(String tag, String message)
Gdx.app.error(String tag, String message)
Gdx.app.error(String tag, String message, Throwable exception)

// Tag should be descriptive
// Examples: "SkillSystem", "BattleSystem", "RenderSystem"
```

### Database Changes

None.

### API Changes

None (internal error handling only).

## Acceptance Criteria

- [ ] Exception handling uses `Gdx.app.error()` instead of `System.err`
- [ ] Error messages include descriptive tag ("SkillSystem")
- [ ] Basic error type distinction (NPE vs generic exception)
- [ ] Exception stack traces are logged when available
- [ ] Return value `false` is still returned on error

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found poor error handling | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:79`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/battle/BattleUnitBlackboard.java:205-223`
- **LibGDX Docs:** Application logging documentation
