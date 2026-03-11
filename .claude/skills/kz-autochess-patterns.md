---
name: kz-autochess-patterns
description: Coding patterns extracted from KZ AutoChess (LibGDX auto-battler game)
version: 1.0.0
source: local-git-analysis
analyzed_commits: 200
---

# KZ AutoChess Patterns

## Commit Conventions

This project uses a **mixed commit style**. Recent commits follow conventional format:

- `feat(scope):` - New features (e.g., `feat(skill): Add combat skill system`)
- `fix(scope):` - Bug fixes
- `refactor(scope):` - Code restructuring (e.g., `refactor(screen): introduce GameUIManager`)
- `docs:` - Documentation updates
- `style:` - Code style fixes

**Scope categories**: `screen`, `battle`, `ui`, `model`, `economy`, `tiled`, `card`, `skill`, `movement`, `projectile`

Older commits use Chinese descriptions (e.g., `更新渲染`, `完善机制`).

## Code Architecture

### Core Principles
1. **Model/Updator/Manager/Render Separation** - Strict separation of concerns
2. **Blackboard Pattern** - Context aggregation for AI agents (`BattleUnitBlackboard`)
3. **State Machine** - Character behavior states
4. **Event System** - Decoupled communication via `GameEventSystem`

### Package Structure

```
com.voidvvv.autochess/
├── battle/          # Combat AI & blackboards
│   ├── BattleUnitBlackboard.java    # Per-unit AI context
│   ├── UnitBehaviorTreeFactory.java # Behavior tree builder
│   ├── AttackTargetTask.java        # AI action nodes
│   ├── FindEnemyTask.java
│   └── MoveToEnemyTask.java
├── event/           # Event system
│   ├── GameEventSystem.java         # Event dispatcher
│   ├── GameEvent.java              # Base event
│   └── drag/                      # Drag-related events
├── input/           # Input handling
│   └── GameInputHandler.java
├── logic/           # Game rules
│   ├── SynergyManager.java         # Synergy effects
│   ├── EconomyCalculator.java       # Economy math
│   ├── CardUpgradeLogic.java       # 3-card merge
│   ├── CharacterStatsLoader.java   # JSON config loader
│   └── MovementCalculator.java
├── manage/          # Lifecycle managers
│   ├── ProjectileManager.java
│   ├── ParticleSystem.java
│   ├── MovementEffectManager.java
│   └── RenderDataManager.java
├── model/           # Data models
│   ├── Card.java                    # Card definition
│   ├── CardPool.java
│   ├── CardShop.java
│   ├── PlayerDeck.java
│   ├── PlayerEconomy.java
│   ├── BattleCharacter.java         # Battle unit
│   ├── Battlefield.java
│   ├── Skill.java
│   ├── Projectile.java
│   └── MovementEffect.java
├── render/          # Rendering
│   ├── CardRenderer.java
│   ├── BattleCharacterRender.java
│   ├── BattleFieldRender.java
│   ├── ProjectileRenderer.java
│   └── ParticleSystem.java
├── screens/         # Game screens (LibGDX Screen interface)
│   ├── GameScreen.java             # Main game (1000+ lines)
│   ├── LevelSelectScreen.java
│   └── StartScreen.java
├── sm/              # State machine
│   ├── machine/                     # State machine impl
│   ├── state/                       # State implementations
│   └── state/common/                # Common states (Attack, Move, etc.)
├── ui/              # UI components
│   ├── GameUIManager.java           # Phase 2 unified UI
│   ├── ShapeRendererHelper.java      # Shape rendering helper
│   └── CardRenderer.java
├── updater/         # Update logic
│   ├── BattleUpdater.java
│   ├── BattleCharacterUpdater.java
│   ├── ProjectileUpdater.java
│   └── DamageRenderUpdater.java
└── utils/           # Utilities
    ├── ViewManagement.java          # Dual viewport system
    ├── CameraController.java
    ├── FontUtils.java
    ├── I18N.java
    ├── TiledAssetLoader.java
    └── CharacterRenderer.java
```

## Workflows

### Adding a New Card/Character

1. Add card definition to `CardPool.java` or JSON config
2. Update `CharacterStats.java` in `character_stats.json`
3. `CardRenderer` handles rendering automatically
4. Test purchase/deck logic with `CardUpgradeTest`

### Adding a New Skill

1. Add `SkillType` enum value
2. Implement skill logic in `TrySkillTask.java`
3. Update `BattleCharacter` to include skill
4. Add skill card to `CardPool`
5. Test mana mechanic interaction

### Screen Navigation Flow

```
Main.java
  ↓
KzAutoChess.java (setScreen)
  ↓
StartScreen → LevelSelectScreen → GameScreen
```

### Battle Loop Flow

```
GameScreen.render()
  ↓
gameEventSystem.dispatch()
  ↓
battleUpdater.update()
  ↓
BehaviorTree.step() per unit
  ↓
StateMachine.execute()
  ↓
Character action (attack/move/skill)
  ↓
Damage event → DamageListener → Settlement
```

### UI Event Flow (Phase 2)

```
Input → GameInputHandler → GameEventSystem
  ↓
GameUIManager.ButtonCallback
  ↓
GameScreen callback methods
```

## Testing Patterns

- **Framework**: JUnit 5 (Jupiter)
- **Test location**: `core/src/test/java/com/voidvvv/autochess/`
- **Test naming**: `*Test.java` (e.g., `PlayerEconomyTest.java`)
- **Test structure**:
  ```java
  @BeforeEach
  void setUp() {
      // Initialize test objects
  }

  @Test
  void testMethodUnderTest() {
      // Arrange, Act, Assert
      assertEquals(expected, actual, "message");
  }
  ```
- **Coverage targets**: Not enforced, but tests exist for critical logic (economy, card upgrade, character rendering)

## Important File Patterns

| Pattern | Files |
|---------|--------|
| **Most modified** | `GameScreen.java`, `BattleCharacter.java`, `BattleFieldRender.java`, `Card.java` |
| **UI refactoring** | `GameUIManager.java` (Phase 2), `GameScreen.java` |
| **Blackboard pattern** | `BattleUnitBlackboard.java` |
| **State machine** | `StateMachine.java`, `BaseState.java`, `AttackState.java` |
| **Event listeners** | `*Listener.java`, `*Holder.java` |

## Build & Run

- **Build**: `gradle` (local Gradle, NOT `./gradlew`)
- **Java version**: 25
- **LibGDX version**: 1.14.0

## Dependencies

- LibGDX core, AI, Box2D, FreeType
- Ashley (ECS)
- JUnit 5 (testing)

## Common Gotchas

1. **GC Pressure**: Reuse `GlyphLayout` instances instead of creating new ones per frame
2. **Viewport Coordination**: Use `ViewManagement.screenToUI()` / `screenToWorld()` for coordinate conversion
3. **Event System**: Always dispatch events via `GameEventSystem`, don't call listeners directly
4. **Render/Update Separation**: Use updater classes for logic, render classes for drawing
5. **Blackboard Access**: AI nodes should access data through blackboard, not direct model access
