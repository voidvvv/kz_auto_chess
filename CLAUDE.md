# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an auto-chess game built on the LibGDX framework, providing cross-platform game support (desktop via LWJGL3). The game features a turn-based combat system with character collection, upgrades, level progression, and a comprehensive synergy system.

### Core Gameplay
- **Two-Phase System**: Placement phase (arrange your units) and Battle phase (automatic combat)
- **Card Collection**: Purchase units from a rotating shop, build your deck
- **Synergy System**: Combine units of the same class/race to activate powerful buffs
- **Economy System**: Gold management with interest, win streaks, and player leveling
- **Character Upgrade**: Combine 3 identical cards to create a 2-star unit, then 3-star
- **Tiled Rendering**: Dual rendering modes - geometric shapes and Tiled sprite textures (toggle with F5)

## Build Commands

```bash
# Build the project
./gradlew build

# Run the game (desktop)
./gradlew lwjgl3:run

# Compile specific modules
./gradlew :core:compileJava

# Run tests
./gradlew :core:test
```

## Technical Stack

### Core Dependencies
- **LibGDX** - Game framework for graphics, input, audio
- **LWJGL3** - OpenGL bindings for desktop platform
- **Ashley** - Entity-component system (included but not heavily used)
- **GDX-AI** - Behavior tree and AI utilities
- **GDX-Box2D** - 2D physics engine
- **GDX-FreeType** - Font rendering with Chinese character support

### Language & Build
- **Java 17** - Source and target compatibility
- **Gradle 8+** - Build system
- **JUnit 5** - Testing framework

## Code Architecture

### Architectural Patterns

The codebase follows several key architectural patterns:

#### 1. Model-Updater-Manager-Render Separation

**Model-Update-Render Pattern** - Data models contain only properties and getters/setters. Update logic lives in `updater/` package. Rendering logic is in `render/` package. Management coordination is in `manage/` package.

**Example - Projectile System**:
- `model/Projectile` - Pure data: position, speed, damage, type
- `updater/ProjectileUpdater` - Update logic: movement, tracking
- `manage/ProjectileManager` - Management: creation, collision, removal
- `render/ProjectileRenderer` - Visual rendering

#### 2. State Machine Pattern

Used for character behavior states via `sm/` package:
- `StateMachine<T>` - Generic state machine interface
- `BaseState<T>` - Base state class with enter/execute/exit methods
- Concrete states: `AttackState`, `MoveState`, `NormalState`

Each character has a state machine managed through `BattleUnitBlackboard`.

#### 3. Behavior Tree AI

LibGDX's behavior tree system for combat decision making:
- `UnitBehaviorTreeFactory` - Creates behavior trees for units
- Tasks: `FindEnemyTask`, `MoveToEnemyTask`, `AttackTargetTask`
- `BattleUnitBlackboard` - Shared context for behavior tree

#### 4. Event-Driven Architecture

Damage events and other game events flow through listener chains:
- `DamageEvent` - Event object with source/target/damage
- `DamageEventHolder` - Event collection
- `DamageEventListener` - Interface for handling events
- `DamageEventListenerHolder` - Listener management

#### 5. Manager Pattern

Centralized managers handle lifecycle and coordination:
- `ProjectileManager` - Projectiles lifecycle
- `SynergyManager` - Synergy calculation and application
- `CardShop` - Shop refresh and card purchasing
- `PlayerDeck` - Owned cards management

## Package Structure

### `model/` - Pure Data Structures
Data entities with minimal logic, only properties and getters/setters.

| Class | Purpose |
|--------|---------|
| `BattleCharacter` | Combat character with stats, position, state, collision, synergy effects |
| `Battlefield` | Battle arena with player/enemy zones, character management, projectile manager |
| `Card` | Unit card with type, cost, synergy, upgrade capability, Tiled resource key |
| `CardPool` | All available cards database |
| `CardShop` | Current shop cards and refresh logic |
| `PlayerDeck` | Player's owned cards collection |
| `CharacterStats` | Unit attributes (health, attack, defense, etc.) with config loading |
| `Projectile` | Projectile data (position, speed, damage, type) - NO update logic |
| `Particle` | Visual particle data |
| `SynergyType` | Enum for class/race synergies with activation thresholds |
| `SynergyEffect` | Buff data for active synergies |
| `GamePhase` | Game phase enum (PLACEMENT, BATTLE) |
| `PlayerEconomy` | Gold, experience, level, streaks, interest management |
| `LevelEnemyConfig` | Enemy configuration for each level with spawn logic |
| `ModelHolder<T>` | Generic holder for model collections |
| `BaseCollision` | Collision box data (base point, face rect, bottom rect) |
| `MoveComponent` | Movement capability data (speed, canWalk) |
| `DamageShowModel` | Visual damage display data |
| `battle/Damage` | Damage type and value (Physical, Magic, Real) |
| `battle/DamageEventHolder` | Collection of damage events |
| `battle/DamageEventListenerHolder` | Collection of damage event listeners |
| `event/DamageEvent` | Single damage event with source/target/damage/extra |

### `updater/` - Update Logic
Separation of update logic from models.

| Class | Purpose |
|--------|---------|
| `BattleCharacterUpdater` | Character movement and state updates |
| `BattleUpdater` | Battle-wide damage event processing and settlement |
| `ProjectileUpdater` | Projectile movement, tracking logic, direction updates |
| `DamageRenderUpdater` | Damage visual effect updates |
| `ParticleSystemUpdater` | Particle system updates |
| `BaseMyFunction` | Base function interface |
| `MyFunction` - Generic function interface |

**Pattern**: Each updater follows `update(Model model, float deltaTime)` signature.

### `manage/` - Manager Classes
Coordinators for entity lifecycles and system-wide operations.

| Class | Purpose |
|--------|---------|
| `ProjectileManager` | Creates, updates, removes projectiles, handles collisions |

### `battle/` - Behavior Tree and Combat AI
AI decision-making and behavior tree tasks.

| Class | Purpose |
|--------|---------|
| `BattleUnitBlackboard` | AI blackboard with state machine, message handling, target, cooldown |
| `UnitBehaviorTreeFactory` | Creates behavior trees for combat units |
| `FindEnemyTask` | Task to find nearest enemy |
| `MoveToEnemyTask` | Task to move toward target |
| `AttackTargetTask` - Task to attack current target |
| `BattleTelegraph` | Visual attack telegraphing |

**Behavior Tree Structure** (from `UnitBehaviorTreeFactory`):
```
Sequence
├── FindEnemyTask
├── MoveToEnemyTask
└── AttackTargetTask
```

### `render/` - Rendering Components
Visual rendering separated from game logic.

| Class | Purpose |
|--------|---------|
| `BattleFieldRender` | Renders battlefield with player/enemy zones, dual rendering mode |
| `ProjectileRenderer` | Renders projectiles and particles |
| `DamageLineRender` | Renders damage indicators |
| `ParticleSystem` | Visual particle system |
| `TiledBattleCharacterRender` | Renders characters using Tiled textures |

### `ui/` - UI Components
Scene2D-based user interface.

| Class | Purpose |
|--------|---------|
| `CardRenderer` | Renders card UI with hover, count, upgrade status |

### `sm/` - State Machine System
State machine implementation for character behavior.

| Package | Classes |
|----------|----------|
| `machine/` | `StateMachine`, `BaseStateMachine` |
| `state/` | `BaseState`, `States` |
| `state/common/` | `AttackState`, `MoveState`, `NormalState` |

**States Available**:
- `NormalState` - Default state, idle behavior
- `MoveState` - Moving toward target
- `AttackState` - Attacking, handles attack timing and damage window

### `listener/` - Event Listeners
Event handling chains for game events.

| Package | Classes |
|----------|----------|
| `damage/` | `DamageEventListener`, `DamageRenderListener`, `DamageSettlementListener` |

### `msg/` - Message System
Message passing system using LibGDX messaging.

| Class | Purpose |
|--------|---------|
| `MessageConstants` | Message type definitions (attack, doAttack, endAttack) |
| `KZConsumer` | Consumer interface for messages |
| `DefaultKZConsumer` - Default message consumer implementation |

**Message Types**:
- `MessageConstants.attack` - Initiate attack state
- `MessageConstants.doAttack` - Execute attack damage (at damage window)
- `MessageConstants.endAttack` - End attack animation, return to normal state

### `logic/` - Game Logic
High-level game systems.

| Class | Purpose |
|--------|---------|
| `SynergyManager` | Calculates synergies, applies buffs to characters |
| `CardUpgradeLogic` | Handles card upgrade mechanics (3 same cards → 1 upgraded card) |

**Synergies Available**:
| Type | Activation Thresholds | Effect |
|------|---------------------|---------|
| WARRIOR | 2, 4, 6 | Attack +5%/level, Defense +10%/level |
| MAGE | 3, 6, 9 | Magic +8%/level, Mana regen +15%/level |
| ARCHER | 2, 4, 6 | Attack speed +10%/level, Crit +3%/level |
| ASSASSIN | 3, 6 | Crit damage +15%/level, Dodge +5%/level |
| TANK | 2, 4, 6 | HP +10%/level, Damage reduction +3%/level |
| DRAGON | 2 | All stats +5%/level |
| BEAST | 2, 4 | Attack +5%/level, Life steal +4%/level |
| HUMAN | 2, 4, 6 | All stats +3%/level, EXP +10%/level |

### `screens/` - Game Screens
LibGDX Screen implementations.

| Class | Purpose |
|--------|---------|
| `GameScreen` | Main gameplay screen with shop, deck, battlefield, input handling |
| `StartScreen` | Title/start screen |
| `LevelSelectScreen` - Level selection screen |

### `utils/` - Utility Classes
Helper classes and shared utilities.

| Class | Purpose |
|--------|---------|
| `CharacterRenderer` | Renders characters as geometric shapes (fallback rendering) |
| `TiledAssetLoader` | Loads Tiled map assets and collision data from tilesets |
| `RenderConfig` | Global rendering mode toggle (F5 key: geometric ↔ Tiled) |
| `FontUtils` | Font loading and management |
| `I18N` | Internationalization system for multi-language support |
| `CameraController` | Camera input and zoom control |
| `ViewManagement` | Viewport management (UI and Game viewports) |
| `CharacterCamp` - Character camp enum (WHITE=player, BLACK=enemy) |
| `AutoChessController` - Game input coordination |

## Game Loop Flow (GameScreen.java)

### 1. Show Phase (`show()`)
- Initialize battle updater and renderers
- Load Tiled map resources via `TiledAssetLoader`
- Reset battlefield and register damage listeners

### 2. Update Phase (`render(float delta)`)
```
handleInput()
├── Screen → UI coordinates (shop, deck)
├── Screen → World coordinates (battlefield)
└── Process clicks, dragging

stage.act(delta)
cameraController.update(delta)

if (phase == BATTLE):
    updateBattle(delta)
    ├── Update behavior trees
    ├── Update blackboards and state machines
    ├── Process damage events (BattleUpdater)
    ├── Update projectiles (ProjectileManager → ProjectileUpdater)
    └── Update damage visuals (DamageRenderUpdater)
```

### 3. Render Phase
```
drawWorldContent()  // Game viewport
├── drawBattlefield()
│   ├── Draw player zone (blue tint)
│   ├── Draw enemy zone (red tint)
│   └── Draw characters (geometric OR Tiled textures via RenderConfig)
├── Render projectiles (ProjectileRenderer)
├── Render damage lines (DamageLineRender)
└── Render character state debug info

drawUIContent()  // UI viewport
├── Draw title and economy info
├── Draw synergy info
├── Draw shop area (shop cards with hover)
└── Draw deck area (owned cards with count, upgrade status)

drawDragging()  // With alpha blending
├── Draw dragging card (UI viewport)
└── Draw dragging character (game viewport)

stage.draw()  // Scene2D UI (buttons)
```

### 4. Battle Flow
```
startBattle()
├── phase = BATTLE
├── Spawn enemies from LevelEnemyConfig in enemy zone
├── For each character:
│   ├── Enter battle (init battle stats)
│   ├── Create BattleUnitBlackboard with state machine
│   └── Create behavior tree from UnitBehaviorTreeFactory

→ Automatic combat loop:
    ├── Behavior trees drive unit decisions (find enemy → move → attack)
    ├── State machines handle attack timing and state transitions
    ├── Units move based on MoveComponent
    ├── Units attack when in range (based on attackRange)
    ├── Projectiles created for ranged attacks (ARCHER, MAGE)
    ├── Direct damage for melee attacks
    ├── Damage events processed through listener chain
    └── Units die (enemies removed, players fade to 30% opacity)

→ End when one side eliminated:
    endBattle()
    ├── Remove all enemies (player side wins) or players (enemy wins)
    ├── Reset player units (revive to full HP, return to init positions)
    ├── Clear synergies and projectile manager
    ├── phase = PLACEMENT
    └── Economy settlement (gold, exp, streaks)
```

## Import Path Guidelines

When modifying code, ensure correct imports:

### Model-Updater-Manager Pattern
- `BattleCharacter` → `com.voidvvv.autochess.model`
- `Projectile` → `com.voidvvv.autochess.model`
- `ProjectileManager` → `com.voidvvv.autochess.manage`
- `ProjectileUpdater` → `com.voidvvv.autochess.updater`

### State Machine
- `StateMachine` → `com.voidvvv.autochess.sm.machine`
- `BaseState` → `com.voidvvv.autochess.sm.state`
- `States` → `com.voidvvv.autochess.sm.state.common`
- `AttackState`, `NormalState`, `MoveState` → `com.voidvvv.autochess.sm.state.common`

### Behavior Tree
- `UnitBehaviorTreeFactory` → `com.voidvvv.autochess.battle`
- `BattleUnitBlackboard` → `com.voidvvv.autochess.battle`
- Behavior tree tasks → `com.voidvvv.autochess.battle`

### Rendering
- `BattleFieldRender` → `com.voidvvv.autochess.render`
- `ProjectileRenderer` → `com.voidvvv.autochess.render`
- `TiledBattleCharacterRender` → `com.voidvvv.autochess.render`
- `CardRenderer` → `com.voidvvv.autochess.ui`

## Tiled Map Integration

The game supports Tiled map rendering for characters with dual rendering modes.

### Key Classes
- `TiledAssetLoader` - Loads tilesets and extracts collision data
- `TiledBattleCharacterRender` - Renders characters using Tiled textures
- `RenderConfig` - Toggle between geometric and Tiled rendering

### Resource Key Format
Cards reference Tiled resources via `tiledResourceKey` (format: `"tilesetId+tileId"`)

### Toggle Rendering
Press **F5** during gameplay to switch between:
- Geometric rendering (shapes via `CharacterRenderer`)
- Tiled texture rendering (sprites via `TiledBattleCharacterRender`)

### Collision Data
Tiled tiles can define collision objects:
- `base` - Point map object for center position
- `face` - Rectangle for face/visual area
- `bottom` - Rectangle for ground collision

### Loading Process
```java
// In GameScreen.show()
tiledAssetLoader = new TiledAssetLoader();
tiledMap = new TmxMapLoader().load("tiled/demo/2.tmx");

// Load tilesets
for (TiledMapTileSet tileSet : tiledMap.getTileSets()) {
    tiledAssetLoader.loadBaseCollision(tileSet);
}

// Load resources for characters
character.loadTiledResources(tiledAssetLoader);
```

## Internationalization

### I18N System
- `I18N` utility class provides multi-language support
- Resource files in `assets/i18n/`
- Supported locales: Chinese, English, Japanese

### Usage
```java
// Simple lookup
String text = I18N.get("shop");

// With default fallback
String text = I18N.get("unknown_key", "Default Text");

// Formatted string
String text = I18N.format("refresh_cost", cost);
```

### Supported Languages
- `I18N.CHINESE` - Simplified Chinese (default)
- `I18N.ENGLISH` - English
- `I18N.JAPANESE` - Japanese

### Key Areas with I18N
- UI labels (buttons, headers)
- Card types and synergy names
- Economy info display
- Battlefield labels

## Development Notes

### Java Compatibility
- **Java 17** required for compilation and runtime
- Use modern Java features appropriately
- Ensure null-safety where possible

### LibGDX Specifics
- **Coordinate System**: Bottom-left origin (standard LibGDX)
- **Viewport Management**: Two separate viewports (UI and Game world)
  - UI viewport: Fixed screen coordinates for menus/cards
  - Game viewport: World coordinates for battlefield/characters
- **SpriteBatch**: Always call `begin()`/`end()` pairs
- **ShapeRenderer**: Separate batch operations from shape rendering
- **Scene2D**: Used for buttons and interactive UI elements

### Performance Considerations
- **Object Pooling**: Consider pooling for frequently created objects (projectiles, particles)
- **Batch Optimization**: Minimize SpriteBatch/ShapeRenderer state changes
- **Asset Loading**: Assets loaded via TiledAssetLoader for reuse

### Testing
- Test files in `core/src/test/java/`
- Use JUnit 5 annotations (`@Test`, `@BeforeEach`)
- Mock LibGDX components as needed

## Game Configuration

### Battlefield Zones
- **Player Zone Ratio**: 0.5 (50% of battlefield height)
- Player units placed in bottom half (y from bottom to split line)
- Enemy units spawn in top half (y from split line to top)

### Projectile Types
| Type | Behavior | Speed | Max Distance | Tracking | Color |
|------|-----------|--------|-------------|----------|--------|
| ARROW | Linear, no tracking | 400 px/s | 700 px | No | Brown |
| MAGIC_BALL | Tracks target | 250 px/s | 600 px | Yes (180°/s) | Purple |

### Damage Types
- **Physical** (`PhySic`) - Reduced by 50% of defense
- **Magic** - Reduced by 25% of defense
- **Real** - Ignores defense completely

### Economic System
- **Initial Gold**: 10
- **Base Round Income**: 5 gold per round
- **Interest**: 1 gold per 10 gold held (max 5 gold)
- **Win Streaks**: Bonuses at 2+, 4+, 6+ consecutive wins
- **Lose Streaks**: Compensation at 2+, 4+ consecutive losses
- **Player Levels**: 1-10 with increasing XP requirements
  - Level 1-2: 4 XP
  - Level 2-3: 6 XP
  - Level 3-4: 8 XP
  - ...up to 22 XP for level 9-10
- **Level-up Rewards**: +1 gold at most levels
- **XP Sources**: +2 XP for win, +1 XP base per round

### Card Costs
Based on card tier (1-5):
- Tier 1: Lower cost units
- Tier 5: Higher cost, more powerful units

### Attack Ranges (by CardType)
- **ARCHER**: 150f (ranged)
- **MAGE**: 120f (mid-range)
- **WARRIOR, ASSASSIN, TANK**: 70f (melee)

### Attack Timing
- `progressCouldDamage`: 0.15f - Point in attack animation where damage can be dealt
- `maxAttackActProgress`: 0.25f - Attack animation duration
- `attackCooldown`: 1.0f - Time between attacks

## Common Tasks

### Adding a New Card
1. Add card data to `CardPool` initialization in constructor
2. Add `CharacterStats` config in `CharacterStats.Config.load()` (via properties or code)
3. Set `CardType` and add `SynergyType` list
4. Optionally add `tiledResourceKey` for Tiled rendering

### Adding a New Synergy
1. Add enum value to `SynergyType` with display name and thresholds
2. Define activation thresholds list
3. Add effect method in `SynergyManager` (e.g., `applyNewSynergy()`)
4. Update `applySynergyEffect()` switch statement
5. Add display name to i18n resources

### Adding a New State
1. Extend `BaseState<BattleUnitBlackboard>`
2. Implement `enter()`, `execute()`, `exit()` methods
3. Add singleton instance pattern if needed
4. Add reference to `States` class

### Adding a New Behavior Task
1. Extend `Task<BattleUnitBlackboard>` (from LibGDX-AI)
2. Implement `execute()` method, return `Status.SUCCESS/FAILURE/RUNNING`
3. Add to `UnitBehaviorTreeFactory.create()` sequence

### Modifying Damage Calculation
Edit `BattleUpdater.damageSettlement()`:
- Current logic: `Math.max(1, raw - def / 2)` for physical
- Modify formula as needed for game balance

## File Path Format (Important)

**Always use absolute Windows paths with drive letters and backslashes**:

```
C:/myFiles/dev/project/idea_projects/kz_auto_chess/src/main/java/com/voidvvv/autochess/Example.java
```

**Do NOT use**:
```
./src/main/java/...
/c/Users/...
```

This is required to avoid a file modification bug in the development environment.
