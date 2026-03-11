---
status: pending
priority: p1
issue_id: 002
tags:
  - code-review
  - rendering
  - bug
dependencies: []
---

# P1: ManaBarRenderer Overdraw Bug - Magic Bar Invisible

## Problem Statement

`ManaBarRenderer.render()` draws rectangles in the wrong order, causing the magic (blue) bar to be overwritten by the border. This results in the mana bar appearing invisible to players.

## Findings

### Evidence from Code

**Location:** `ManaBarRenderer.java:47-57`

```java
// Render background (dark gray)
shapeRenderer.setColor(Color.DARK_GRAY);
shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

// Render filled part (blue)
shapeRenderer.setColor(Color.BLUE);
shapeRenderer.rect(x, y, filledWidth, BAR_HEIGHT);

// Render border (white)
shapeRenderer.setColor(Color.WHITE);
shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);  // ❌ OVERWRITES EVERYTHING!
```

### Visual Impact

```
Step 1: Draw Dark Background
┌────────────────────┐
│ ░░░░░░░░░░░░░░░░ │  <- Gray background (BAR_WIDTH)
└────────────────────┘

Step 2: Draw Blue Fill
┌────────────────────┐
│ ▓▓▓▓▓▓▓░░░░░░░░ │  <- Blue partial fill
└────────────────────┘

Step 3: Draw Border (OVERWRITES!)
┌────────────────────┐
│ █████████████████ │  <- White border (same rect!)
└────────────────────┘
```

**Result:** Players see only a white rectangle with no visible mana progression.

### Root Cause

LibGDX's `ShapeRenderer.rect()` doesn't draw a "hollow" border - it draws a **filled rectangle**. The developer likely expected `rectLine()` or `rect()` to draw a border, but both methods in ShapeRenderer draw filled shapes.

## Proposed Solutions

### Solution A: Use rectLine() for Border (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Minimal change
- Renders actual border lines
- More performant than drawing 4 rectangles

**Cons:**
- `rectLine()` draws a 1px thick line (fixed thickness)

**Code Changes:**

```java
// ManaBarRenderer.java
public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
    // ... existing code up to line 45 ...

    // Render background (dark gray)
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

    // Render filled part (blue)
    shapeRenderer.setColor(Color.BLUE);
    shapeRenderer.rect(x, y, filledWidth, BAR_HEIGHT);

    // Render border using 4 lines
    shapeRenderer.setColor(Color.WHITE);
    float borderThickness = 1f;
    shapeRenderer.rectLine(x, y, x + BAR_WIDTH, y, borderThickness);              // Top
    shapeRenderer.rectLine(x, y, x, y + BAR_HEIGHT, borderThickness);             // Left
    shapeRenderer.rectLine(x + BAR_WIDTH, y, x + BAR_WIDTH, y + BAR_HEIGHT, borderThickness);  // Right
    shapeRenderer.rectLine(x, y + BAR_HEIGHT, x + BAR_WIDTH, y + BAR_HEIGHT, borderThickness);  // Bottom
}
```

### Solution B: Draw Border as 4 Separate Rectangles

**Effort:** Small
**Risk:** Low
**Pros:**
- Thicker borders possible
- More explicit control

**Cons:**
- More verbose
- Slightly more GPU draws

**Code Changes:**

```java
public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
    // ... existing code up to line 45 ...

    // Render background (dark gray)
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

    // Render filled part (blue)
    shapeRenderer.setColor(Color.BLUE);
    shapeRenderer.rect(x, y, filledWidth, BAR_HEIGHT);

    // Render border (white) - 4 rectangles
    shapeRenderer.setColor(Color.WHITE);
    float borderThickness = 1f;

    // Top border
    shapeRenderer.rect(x, y + BAR_HEIGHT - borderThickness, BAR_WIDTH, borderThickness);
    // Bottom border
    shapeRenderer.rect(x, y, BAR_WIDTH, borderThickness);
    // Left border
    shapeRenderer.rect(x, y, borderThickness, BAR_HEIGHT);
    // Right border
    shapeRenderer.rect(x + BAR_WIDTH - borderThickness, y, borderThickness, BAR_HEIGHT);
}
```

### Solution C: No Border - Minimalist Approach

**Effort:** Small
**Risk:** Low
**Pros:**
- Simplest solution
- Performance optimized
- Modern UI style

**Cons:**
- Less visual definition
- May not match design spec

**Code Changes:**

```java
public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
    // ... existing code up to line 45 ...

    // Render background (dark gray)
    shapeRenderer.setColor(Color.DARK_GRAY);
    shapeRenderer.rect(x, y, BAR_WIDTH, BAR_HEIGHT);

    // Render filled part (blue)
    shapeRenderer.setColor(Color.BLUE);
    shapeRenderer.rect(x, y, filledWidth, BAR_HEIGHT);

    // No border - minimalist approach
}
```

## Recommended Action

**Adopt Solution A** - Use `rectLine()` for the border. This is the standard approach for drawing borders in LibGDX and produces the expected visual result.

## Technical Details

### Affected Files

| File | Lines | Method |
|------|-------|--------|
| `ManaBarRenderer.java` | 56-58 | `render()` |

### Related Components

- `BattleCharacterRender` (calls ManaBarRenderer)
- `ShapeRenderer` (LibGDX rendering API)

### LibGDX ShapeRenderer API Reference

```java
// Filled rectangle - draws filled shape
void rect(float x, float y, float width, float height);

// Hollow line - draws border line with specified thickness
void rectLine(float x1, float y1, float x2, float y2, float lineWidth);
```

### Database Changes

None.

### API Changes

None (internal rendering only).

## Acceptance Criteria

- [ ] Mana bar background (dark gray) is visible
- [ ] Mana fill (blue) is visible and represents actual mana ratio
- [ ] Border (white) is drawn as lines, not a filled rectangle
- [ ] Blue fill area is NOT overwritten by border
- [ ] Visual verification in game shows correct mana progression

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found bug | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:294-307`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/render/ManaBarRenderer.java`
- **LibGDX Docs:** ShapeRenderer API documentation
