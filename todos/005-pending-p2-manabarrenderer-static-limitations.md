---
status: pending
priority: p2
issue_id: 005
tags:
  - code-review
  - architecture
  - extensibility
dependencies: []
---

# P2: ManaBarRenderer Static Design Limits Extensibility

## Problem Statement

`ManaBarRenderer` is a static utility class with hardcoded values (`BAR_HEIGHT`, `BAR_WIDTH`, `Y_OFFSET`). This design limits future flexibility and makes it difficult to:
1. Have different mana bar styles for different unit types
2. Support multiple mana bars (e.g., separate energy/magic bars)
3. Adjust mana bar size based on character size
4. Customize colors per unit type or skill

## Findings

### Evidence from Code

**ManaBarRenderer.java**
```java
public class ManaBarRenderer {

    private static final float BAR_HEIGHT = 6f;      // ❌ Hardcoded
    private static final float BAR_WIDTH = 40f;       // ❌ Hardcoded
    private static final float Y_OFFSET = 10f;        // ❌ Hardcoded

    public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
        // ... uses hardcoded values throughout ...
        float x = character.getX() - BAR_WIDTH / 2f;
        float y = character.getY() - character.getSize() - Y_OFFSET;

        // Hardcoded colors
        shapeRenderer.setColor(Color.DARK_GRAY);  // Background
        shapeRenderer.setColor(Color.BLUE);       // Fill
        shapeRenderer.setColor(Color.WHITE);       // Border
    }
}
```

### Comparison with Existing Patterns

Looking at other renderers in the codebase for consistency:

| Renderer | Design Style | Configurable |
|----------|--------------|--------------|
| `ManaBarRenderer` | Static utility | ❌ No |
| `BattleCharacterRender` | Static utility | ❌ No |
| `TiledBattleCharacterRender` | Static utility | Yes (via params) |
| `CharacterRenderer` | Static utility | Yes (uses character data) |

### Future Extensibility Concerns

| Requirement | Current Design | Gap |
|-------------|----------------|-----|
| Different colors per skill type | All use BLUE | ❌ |
| Size scales with character size | Fixed 40x6 | ❌ |
| Support secondary mana bar | Single static method | ❌ |
| Boss units get larger bars | Fixed width | ❌ |
| Custom positions per unit | Fixed Y_OFFSET | ❌ |

## Proposed Solutions

### Solution A: Add Configuration Parameters (Recommended)

**Effort:** Small
**Risk:** Low
**Pros:**
- Backward compatible (can add overload)
- Allows per-unit customization
- Maintains static utility pattern

**Cons:**
- Slightly more verbose API

**Code Changes:**

```java
public class ManaBarRenderer {

    private static final float DEFAULT_BAR_HEIGHT = 6f;
    private static final float DEFAULT_BAR_WIDTH = 40f;
    private static final float DEFAULT_Y_OFFSET = 10f;

    // Existing method - for backward compatibility
    public static void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
        render(shapeRenderer, blackboard,
               DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_Y_OFFSET,
               Color.BLUE, Color.DARK_GRAY, Color.WHITE);
    }

    // New configurable method
    public static void render(ShapeRenderer shapeRenderer,
                             BattleUnitBlackboard blackboard,
                             float barWidth,
                             float barHeight,
                             float yOffset,
                             Color fillColor,
                             Color bgColor,
                             Color borderColor) {
        BattleCharacter character = blackboard.getSelf();

        if (character.isDead()) {
            return;
        }

        float currentMana = blackboard.getCurrentMana();
        float maxMana = blackboard.getMaxMana();
        if (maxMana <= 0f) {
            return;
        }

        float x = character.getX() - barWidth / 2f;
        float y = character.getY() - character.getSize() - yOffset;

        float ratio = Math.min(1f, currentMana / maxMana);
        float filledWidth = barWidth * ratio;

        // Background
        shapeRenderer.setColor(bgColor);
        shapeRenderer.rect(x, y, barWidth, barHeight);

        // Fill
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, filledWidth, barHeight);

        // Border
        shapeRenderer.setColor(borderColor);
        float borderThickness = 1f;
        shapeRenderer.rectLine(x, y, x + barWidth, y, borderThickness);
        shapeRenderer.rectLine(x, y, x, y + barHeight, borderThickness);
        shapeRenderer.rectLine(x + barWidth, y, x + barWidth, y + barHeight, borderThickness);
        shapeRenderer.rectLine(x, y + barHeight, x + barWidth, y + barHeight, borderThickness);
    }
}
```

### Solution B: ManaBarConfig Class

**Effort:** Medium
**Risk:** Low
**Pros:**
- Cleaner API
- Easy to create presets
- Extensible for more options

**Cons:**
- Additional class
- More code to maintain

**Code Changes:**

```java
public class ManaBarConfig {
    private final float width;
    private final float height;
    private final float yOffset;
    private final Color fillColor;
    private final Color bgColor;
    private final Color borderColor;

    public static final ManaBarConfig DEFAULT = new ManaBarConfig(
        40f, 6f, 10f, Color.BLUE, Color.DARK_GRAY, Color.WHITE
    );

    public static final ManaBarConfig MAGE = new ManaBarConfig(
        50f, 8f, 12f, Color.CYAN, Color.NAVY, Color.WHITE
    );

    // Constructor, getters...

    public static class Builder {
        // Builder pattern for custom configs
    }
}

public class ManaBarRenderer {
    public static void render(ShapeRenderer shapeRenderer,
                             BattleUnitBlackboard blackboard) {
        render(shapeRenderer, blackboard, ManaBarConfig.DEFAULT);
    }

    public static void render(ShapeRenderer shapeRenderer,
                             BattleUnitBlackboard blackboard,
                             ManaBarConfig config) {
        // Use config values...
    }
}
```

### Solution C: Instance-Based Renderer (Non-Static)

**Effort:** Medium
**Risk:** Medium
**Pros:**
- Full encapsulation
- Stateful if needed
- Matches OOP patterns

**Cons:**
- Breaks existing pattern (most renderers are static)
- Requires instantiation

**Code Changes:**

```java
public class ManaBarRenderer {
    private final float width;
    private final float height;
    private final float yOffset;
    private final Color fillColor;
    private final Color bgColor;
    private final Color borderColor;

    public ManaBarRenderer() {
        this(40f, 6f, 10f, Color.BLUE, Color.DARK_GRAY, Color.WHITE);
    }

    public ManaBarRenderer(float width, float height, float yOffset,
                          Color fillColor, Color bgColor, Color borderColor) {
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
        this.fillColor = fillColor;
        this.bgColor = bgColor;
        this.borderColor = borderColor;
    }

    public void render(ShapeRenderer shapeRenderer, BattleUnitBlackboard blackboard) {
        // Use instance fields...
    }
}
```

## Recommended Action

**Adopt Solution A** - Add configuration parameters to the static method. This:
- Maintains existing API (backward compatible)
- Allows future customization
- Follows existing patterns in the codebase
- Minimal impact

## Technical Details

### Affected Files

| File | Lines | Method |
|------|-------|--------|
| `ManaBarRenderer.java` | 13-58 | Entire class |

### Configuration Ideas for Future

```java
// Different colors for different skill types
public Color getManaColor(SkillType type) {
    switch (type) {
        case HEAL:   return Color.GREEN;
        case AOE:    return Color.RED;
        case BUFF:   return Color.GOLD;
        case DEBUFF: return Color.PURPLE;
        default:     return Color.BLUE;
    }
}

// Scale bar with character size
float scaledWidth = character.getSize() * 0.8f;
```

### Database Changes

None.

### API Changes

New overloaded method (backward compatible).

## Acceptance Criteria

- [ ] Existing `render()` method continues to work unchanged
- [ ] New overloaded `render()` method accepts configuration parameters
- [ ] Configuration includes: width, height, yOffset, colors
- [ ] All rendering uses configurable values
- [ ] Documentation added for new parameters

## Work Log

| Date | Action | Result |
|------|--------|--------|
| 2026-03-11 | Initial review found limitation | Created todo |

## Resources

- **Plan Document:** `docs/plans/2026-03-11-feat-add-combat-skill-system-plan.md:293-307`
- **Code File:** `core/src/main/java/com/voidvvv/autochess/render/ManaBarRenderer.java`
- **Similar Pattern:** `CharacterRenderer` for configuration approach
