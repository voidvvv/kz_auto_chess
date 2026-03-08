---
title: Refactor Model-Update-Render Separation
type: refactor
status: completed
date: 2026-03-08
---

# Refactor Model-Update-Render Separation

## Overview

This plan addresses architectural violations of the Model-Update-Render (MUR) separation principle in the auto-chess game codebase. The current implementation has model classes containing rendering data, business logic, and resource loading - violating the clean separation of concerns.

## Problem Statement

The codebase is designed to follow a four-layer architecture (Model → Updater → Manager → Render), but several critical violations exist:

1. **Model classes contain rendering data** - `BattleCharacter.tiledTexture`, `Projectile.getColor()`
2. **Model classes contain business logic** - `Card.canUpgrade()`, `PlayerDeck.upgradeCard()`, `PlayerEconomy` methods
3. **Model classes perform resource loading** - `CharacterStats.Config.load()`
4. **Renderer classes contain update logic** - `ProjectileRenderer.spawnParticle()`

These violations lead to:
- Tight coupling between layers
- Difficult unit testing (models require rendering context)
- Poor code reusability
- Maintenance challenges

## Proposed Solution

### Phase 1: High-Priority Violation Fixes

#### 1.1 Remove Rendering Data from Models

**Target Files:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java`
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/Projectile.java`

**Actions:**
1. Remove `tiledTexture: TextureRegion` field from `BattleCharacter`
2. Remove `loadTiledResources()`, `getTiledTexture()`, `hasTiledTexture()` methods from `BattleCharacter`
3. Remove `getColor(): Color` method from `Projectile`

**New Class: `RenderDataManager.java`**

```java
package com.voidvvv.autochess.manage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.TextureRegion;
import com.voidvvv.autochess.model.BattleCharacter;
import com.voidvvv.autochess.model.Projectile;

/**
 * Manages rendering-specific data separate from model classes.
 * This ensures models remain pure data entities without rendering dependencies.
 */
public class RenderDataManager {
    private final Map<BattleCharacter, TextureRegion> characterTextures = new HashMap<>();
    private final Map<Projectile, Color> projectileColors = new HashMap<>();

    public TextureRegion getCharacterTexture(BattleCharacter character) {
        return characterTextures.get(character);
    }

    public void setCharacterTexture(BattleCharacter character, TextureRegion texture) {
        characterTextures.put(character, texture);
    }

    public Color getProjectileColor(Projectile projectile) {
        return projectileColors.get(projectile);
    }

    public void setProjectileColor(Projectile projectile, Color color) {
        projectileColors.put(projectile, color);
    }

    public void removeCharacter(BattleCharacter character) {
        characterTextures.remove(character);
    }

    public void removeProjectile(Projectile projectile) {
        projectileColors.remove(projectile);
    }
}
```

#### 1.2 Move Business Logic from Models

**Target Files:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/Card.java`
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/PlayerDeck.java`

**New Class: `CardUpgradeLogic.java`** (in `logic/` package)

```java
package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.Card;
import com.voidvvv.autochess.model.PlayerDeck;

/**
 * Handles card upgrade business logic.
 * Separated from model to keep models as pure data entities.
 */
public class CardUpgradeLogic {

    public boolean canUpgrade(Card card) {
        return card.getLevel() < 3 && card.getDuplicateCount() >= getUpgradeRequirement(card.getLevel());
    }

    public boolean canUpgradeCard(PlayerDeck deck, Card card) {
        if (!canUpgrade(card)) return false;
        // Additional deck-specific validation logic
        return true;
    }

    public Card createUpgradedCard(Card original) {
        if (!canUpgrade(original)) {
            throw new IllegalStateException("Card cannot be upgraded");
        }
        Card upgraded = new Card(original.getCharacterId(), original.getLevel() + 1);
        // Copy other properties as needed
        return upgraded;
    }

    public void upgradeCard(PlayerDeck deck, Card card) {
        if (!canUpgradeCard(deck, card)) {
            throw new IllegalStateException("Card cannot be upgraded in current state");
        }
        // Remove duplicates and upgrade card
        deck.removeDuplicatesForUpgrade(card);
        deck.replaceCard(card, createUpgradedCard(card));
    }

    private int getUpgradeRequirement(int currentLevel) {
        return switch (currentLevel) {
            case 0 -> 3;
            case 1 -> 3;
            case 2 -> 3;
            default -> throw new IllegalStateException("Invalid card level");
        };
    }
}
```

**Actions:**
1. Create `CardUpgradeLogic.java` in `logic/` package
2. Move `canUpgrade()`, `createUpgradedCard()` from `Card` to `CardUpgradeLogic`
3. Move `canUpgradeCard()`, `upgradeCard()`, `getUpgradableCards()` from `PlayerDeck` to `CardUpgradeLogic` or create helper methods in `PlayerDeck` that delegate to `CardUpgradeLogic`
4. Update all call sites to use `CardUpgradeLogic` instead of direct model methods

#### 1.3 Move Economy Logic from PlayerEconomy Model

**Target File:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/PlayerEconomy.java`

**New Class: `EconomyCalculator.java`** (in `logic/` package)

```java
package com.voidvvv.autochess.logic;

import com.voidvvv.autochess.model.PlayerEconomy;

/**
 * Handles economy calculation logic.
 * Separated from model to keep models as pure data entities.
 */
public class EconomyCalculator {

    public int calculateInterest(int gold) {
        return (gold / 10) * 1; // 1 gold per 10 gold, max 5
    }

    public int calculateIncome(PlayerEconomy economy, int streak) {
        return economy.getRoundIncome() + calculateInterest(economy.getGold()) + streak;
    }

    public int updateExperienceRequirement(int level) {
        // Experience requirement curve
        return level * 2;
    }

    public boolean tryLevelUp(PlayerEconomy economy) {
        int requiredXp = updateExperienceRequirement(economy.getLevel());
        if (economy.getCurrentXp() >= requiredXp && economy.getGold() >= 4) {
            economy.setLevel(economy.getLevel() + 1);
            economy.setCurrentXp(economy.getCurrentXp() - requiredXp);
            economy.setGold(economy.getGold() - 4);
            return true;
        }
        return false;
    }
}
```

**Actions:**
1. Create `EconomyCalculator.java` in `logic/` package
2. Move `calculateInterest()`, `updateExperienceRequirement()`, `tryLevelUp()`, `addExperience()` logic to `EconomyCalculator`
3. Keep `PlayerEconomy` as pure data model with simple getters/setters
4. Update all call sites to use `EconomyCalculator`

#### 1.4 Extract Resource Loading from Models

**Target File:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/CharacterStats.java`

**New Class: `CharacterStatsLoader.java`** (in `logic/` or `utils/` package)

```java
package com.voidvvv.autochess.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.voidvvv.autochess.model.CharacterStats;

/**
 * Handles loading of character stats from JSON files.
 * Separated from model to keep models framework-independent.
 */
public class CharacterStatsLoader {

    private final Json json = new Json();

    public CharacterStats loadStats(String characterId) {
        FileHandle file = Gdx.files.internal("config/characters/" + characterId + ".json");
        return json.fromJson(CharacterStats.class, file);
    }

    public void loadAllStats() {
        // Load all character stats files
    }
}
```

**Actions:**
1. Create `CharacterStatsLoader.java`
2. Move `Config.load()` logic to `CharacterStatsLoader`
3. Remove static loading from `CharacterStats` model
4. Update call sites to use `CharacterStatsLoader`

### Phase 2: Moderate-Priority Fixes

#### 2.1 Clean Up Particle System

**Target File:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/render/ProjectileRenderer.java`

**Actions:**
1. Move `spawnParticle()` logic to `ParticleSystemUpdater` or a dedicated `ParticleSpawner` class
2. Remove update logic from renderer
3. Ensure renderer only reads data, never modifies model state

#### 2.2 Improve MoveComponent

**Target File:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java`

**Actions:**
1. Move `getTotalMoveVal()` calculation to updater or create a `MovementCalculator` utility
2. Keep `MoveComponent` as pure data container

### Phase 3: Architectural Improvements

#### 3.1 Introduce Updater Interface

Create a common interface for all updaters:

```java
package com.voidvvv.autochess.updater;

/**
 * Generic interface for updater components.
 * Defines the lifecycle contract for all update logic.
 */
public interface IUpdater<T> {
    void update(T target, float delta);
    void initialize(T target);
    void dispose();
    boolean isActive();
}
```

#### 3.2 Enhance GameScreen Structure

**Target File:**
- `C:/myFiles/dev/project/idea_projects/kz_auto_chess/core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java`

**Actions:**
1. Extract input handling to `GameInputHandler`
2. Extract UI rendering to `GameUIRenderer`
3. Extract game logic orchestration to `GameLogicController`
4. Reduce GameScreen to under 300 lines

## Technical Considerations

### Architecture Impacts

- **Interface Abstraction**: Introducing `IUpdater<T>` will require updating all existing updater implementations
- **Dependency Injection**: New logic classes will need proper initialization and lifecycle management
- **Data Flow**: Need to ensure clean data flow between layers without introducing circular dependencies

### Performance Considerations

- **Lookup Performance**: `RenderDataManager` uses HashMap which should be O(1) for lookups
- **Object Creation**: Avoid creating new objects in hot paths - reuse existing instances where possible
- **Memory**: Ensure proper cleanup in `dispose()` methods

### Testing Strategy

Before refactoring:
1. Write integration tests for current behavior
2. Create test coverage for affected game flows

During refactoring:
1. Run tests after each change
2. Maintain backward compatibility where possible

After refactoring:
1. Add unit tests for new logic classes
2. Add unit tests for pure model classes
3. Verify visual output remains unchanged

## Acceptance Criteria

### Functional Requirements

- [x] `BattleCharacter` no longer contains `tiledTexture` field or rendering-related methods
- [x] `Projectile` no longer contains `getColor()` method
- [x] `Card` no longer contains `canUpgrade()` and `createUpgradedCard()` methods
- [x] `PlayerDeck` no longer contains `upgradeCard()` and `canUpgradeCard()` methods
- [x] `PlayerEconomy` no longer contains calculation methods
- [x] `CharacterStats` no longer performs file I/O
- [x] `ProjectileRenderer` no longer contains particle spawning logic
- [x] New logic classes created in `logic/` package
- [x] All rendering data managed by `RenderDataManager`
- [x] Game launches and runs without errors
- [x] Visual output matches pre-refactor state

### Non-Functional Requirements

- [x] Model classes remain pure data entities (no business logic, no framework dependencies)
- [x] Unit tests added for new logic classes
- [x] Code follows existing project conventions
- [ ] No performance degradation during gameplay

## Dependencies & Risks

### Dependencies

- **Existing Code**: Changes affect multiple model classes used throughout the codebase
- **Render System**: Existing renderers depend on model structure
- **Game Flow**: Card upgrades and economy calculations are core gameplay mechanics

### Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Breaking existing functionality | Medium | High | Write integration tests before refactoring |
| Performance regression from lookups | Low | Medium | Profile before and after changes |
| Circular dependencies | Medium | Medium | Careful design of data flow |
| Large number of call sites to update | High | High | Incremental refactoring with tests |

## Implementation Phases

### Phase 1: Foundation (High Priority)
- [x] Create `RenderDataManager` class
- [x] Move rendering data from `BattleCharacter` and `Projectile`
- [x] Update renderers to use `RenderDataManager`
- [x] Run tests and verify

### Phase 2: Business Logic Extraction (High Priority)
- [x] Create `CardUpgradeLogic` class
- [x] Create `EconomyCalculator` class
- [x] Move business logic from models
- [x] Update call sites
- [x] Run tests and verify

### Phase 3: Resource Loading (Medium Priority)
- [x] Create `CharacterStatsLoader` class
- [x] Move resource loading from `CharacterStats`
- [x] Update initialization code
- [x] Run tests and verify

### Phase 4: Clean Up (Medium Priority)
- [x] Clean up particle system
- [x] Improve `MoveComponent`
- [x] Run tests and verify

### Phase 5: Architecture Improvements (Low Priority)
- [x] Introduce `IUpdater<T>` interface
- [ ] Enhance GameScreen structure
- [x] Add comprehensive unit tests

## Sources & References

### Internal References

- **Architecture Overview**: `C:/myFiles/dev/project/idea_projects/kz_auto_chess/CLAUDE.md` (lines 24-52)
- **Technical Review**: `C:/myFiles/dev/project/idea_projects/kz_auto_chess/review.md` (lines 29-84)
- **Previous Plans**: `C:/myFiles/dev/project/idea_projects/kz_auto_chess/.claude/plans/enhance1.md`

### Files to Modify

#### Model Package
- `core/src/main/java/com/voidvvv/autochess/model/BattleCharacter.java:372-396`
- `core/src/main/java/com/voidvvv/autochess/model/Projectile.java:139-150`
- `core/src/main/java/com/voidvvv/autochess/model/Card.java:111-128`
- `core/src/main/java/com/voidvvv/autochess/model/PlayerDeck.java:122-163`
- `core/src/main/java/com/voidvvv/autochess/model/PlayerEconomy.java:62-194`
- `core/src/main/java/com/voidvvv/autochess/model/CharacterStats.java:56-87`
- `core/src/main/java/com/voidvvv/autochess/model/MoveComponent.java:16-23`

#### Render Package
- `core/src/main/java/com/voidvvv/autochess/render/TiledBattleCharacterRender.java`
- `core/src/main/java/com/voidvvv/autochess/render/ProjectileRenderer.java:150-196`

#### New Files to Create
- `core/src/main/java/com/voidvvv/autochess/manage/RenderDataManager.java`
- `core/src/main/java/com/voidvvv/autochess/logic/CardUpgradeLogic.java`
- `core/src/main/java/com/voidvvv/autochess/logic/EconomyCalculator.java`
- `core/src/main/java/com/voidvvv/autochess/logic/CharacterStatsLoader.java`
- `core/src/main/java/com/voidvvv/autochess/updater/IUpdater.java`

### External References

- **LibGDX Best Practices**: https://libgdx.com/wiki/
- **Clean Architecture Principles**: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
