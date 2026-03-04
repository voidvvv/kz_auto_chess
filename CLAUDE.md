# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an auto-chess game built on the LibGDX framework, providing cross-platform game support (desktop via LWJGL3). The game features a turn-based combat system with character collection, upgrades, and level progression.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the game (desktop)
./gradlew lwjgl3:run

# Compile specific modules
./gradlew :core:compileJava
```

## Code Architecture

### Package Structure
The code follows a clear separation of concerns with the following package structure:

- **`model/`** - Pure data structures (entities, components, holders)
  - `BattleCharacter.java` - Combat character with stats and state
  - `Battlefield.java` - Battle arena management with player/enemy zones
  - `Projectile.java` - Projectile data (arrows, magic balls) - NO update logic
  - `Card.java`, `CardPool.java`, `CardShop.java` - Card system
  - `CharacterStats.java` - Character attributes
  - `battle/` - Battle-specific models (Damage, DamageEventHolder)

- **`updater/`** - Update logic separated from models
  - `ProjectileUpdater.java` - Projectile movement and tracking logic
  - `BattleCharacterUpdater.java` - Character movement updates
  - `BattleUpdater.java` - Battle-wide update logic
  - Pattern: `update(Model model, float deltaTime)` methods

- **`manage/`** - Manager classes coordinating multiple entities
  - `ProjectileManager.java` - Creates, updates, removes projectiles, handles collisions

- **`battle/`** - Behavior tree and combat AI
  - `BattleUnitBlackboard.java` - AI blackboard with state machine
  - `UnitBehaviorTreeFactory.java` - Behavior tree creation

- **`render/`** - Rendering components
  - `ProjectileRenderer.java` - Renders projectiles and particles
  - `BattleFieldRender.java` - Renders battlefield
  - `DamageLineRender.java` - Renders damage indicators

- **`screens/`** - Game screens (LibGDX Screen implementations)
  - `GameScreen.java` - Main gameplay screen
  - Manages game loop, input, rendering delegation

- **`sm/`** - State machine system
  - `machine/` - State machine interfaces and implementations
  - `state/` - Concrete states (AttackState, NormalState, etc.)

- **`listener/`** - Event listeners
  - `damage/` - Damage event handling chain

- **`msg/`** - Message system with consumers
  - `MessageConstants.java` - Message type definitions
  - Consumers handle game event messages

### Key Architectural Patterns

1. **Model-Update-Render Separation**: Data models contain only properties and getters/setters. Update logic lives in `updater/` package. Rendering logic in `render/` package.

2. **Manager Pattern**: Centralized managers (like `ProjectileManager`) handle lifecycle and coordination of multiple entities.

3. **State Machines**: Used for character behavior states (attack, normal, etc.) via the `sm/` package.

4. **Behavior Trees**: AI decision making for combat units.

5. **Event-Driven Architecture**: Damage events and other game events flow through listener chains.

### Game Loop Flow (GameScreen.java)

1. **Update Phase**:
   - Battlefield updates (characters, projectiles)
   - Behavior tree updates
   - State machine updates
   - ProjectileManager.update() → ProjectileUpdater.update()

2. **Render Phase**:
   - Delegates to specialized renderers
   - ProjectileRenderer.render() draws projectiles and particles
   - Separate rendering for battlefield, UI, cards

### Recent Refactoring (Projectile System)

The projectile system was recently refactored to separate concerns:
- `Projectile` (model package): Pure data - position, speed, damage, type
- `ProjectileUpdater` (updater package): Update logic - movement, tracking
- `ProjectileManager` (manage package): Management - creation, collision, removal

### Dependencies and Imports

When modifying code, ensure correct imports:
- `ProjectileManager` is now in `com.voidvvv.autochess.manage`
- `ProjectileUpdater` is in `com.voidvvv.autochess.updater`
- `Projectile` remains in `com.voidvvv.autochess.model`

### Development Notes

- Java 17 compatibility
- LibGDX handles graphics, input, and cross-platform concerns
- Battlefield uses player/enemy zone division (50% each vertically)
- Projectiles have types (ARROW, MAGIC_BALL) with different behaviors
- Damage system supports physical and magical damage types
- Game phases managed through `GamePhase` enum