---
title: refactor: gamescreen-domain-driven-architecture
type: refactor
status: active
date: 2026-03-12
origin: docs/brainstorms/2026-03-12-gamescreen-refactor-brainstorm.md
---

# Refactor: GameScreen Domain-Driven Architecture

## Overview

Comprehensive refactoring of the 1032-line `GameScreen` class to implement a domain-driven architecture with clear separation of concerns. The refactoring introduces `GameMode` abstraction for game logic extensibility, `RenderHolder` for centralized rendering context, and `BattleContext` for battle data sharing.

### Problem Statement

`GameScreen` currently mixes multiple responsibilities:
- **UI Rendering**: Scene2D layout, custom shop/deck rendering, drag preview
- **Input Handling**: Mouse/touch processing, drag-and-drop logic
- **Game Logic**: Battle lifecycle, economy management, card operations
- **State Management**: Game phases, character loading/unloading
- **Rendering Coordination**: Begin/end management for SpriteBatch and ShapeRenderer

This monolithic design creates:
- Difficulty in testing individual components
- High coupling making changes risky
- Inability to switch game modes (e.g., tutorial mode)
- Rendering begin/end scattered across the class

---

## Expert Review Summary

**Review Date:** 2026-03-12
**Reviewer:** Java Game Development Expert
**Plan Status:** Enhanced with critical fixes

### Critical Issues Fixed

| Issue | Original | Fix Applied |
|--------|-----------|-------------|
| **RenderHolder Viewport Management** | Holder managed viewport | Removed viewport from RenderHolder, managers handle their own |
| **ShapeRenderer State Missing** | Only flush SpriteBatch | Added ShapeRenderer flush, added isActive() validation |
| **Input Coordinate Ambiguity** | handleInput(float x, float y, boolean justTouched) | Created InputContext with both screen and world coordinates |
| **BattleContext Immutability** | final field with setPhase() | Full immutability, withPhase() for updates, BattleState for mutations |
| **Manager Circular Dependencies** | Managers directly reference each other | Fully event-driven, no direct manager-to-manager calls |
| **UIManager Rendering Confusion** | UIManager implements GameRenderer | UIManager independent, uses Stage.draw() separately |
| **InputHandler Missing** | Drag state scattered in GameScreen | Added Phase 6.5 for GameInputHandler creation |

### New Components Added

1. **InputContext** - Unified input handling with proper coordinate conversion
2. **BattleState** - Mutable state manager separate from immutable BattleContext
3. **PhaseTransitionEvent** - Event for phase state changes
4. **GameInputHandler** - Centralized input handling with drag state management
5. **ResourceDisposer** - Safe resource disposal with error handling
6. **EventPool** - Object pooling to reduce GC pressure
7. **RenderStateValidator** - Rendering state validation to catch bugs early

### Architecture Improvements

**Before:**
```
Managers → direct method calls → Managers
```

**After:**
```
Managers → GameEventSystem → Managers (fully decoupled)
```

**Rendering Flow Before:**
```
RenderHolder (manages viewport)
  ├─ BattleManager.render(holder)
  └─ UIManager.render(holder)  // Conflict!
```

**Rendering Flow After:**
```
GameMode.render(holder)  // Ends its batch
  ├─ BattleManager (manages own viewport and begin/end)
  └─ UIManager.draw()  // Independent, Stage manages its own batch
```

### Updated Phase Sequence

Original: 8 phases
Updated: 8.5 phases (added Phase 6.5 for InputHandler)

```
Phase 1: Foundation Architecture
Phase 2: BattleContext & BattleState
Phase 3: BattleManager Creation
Phase 4: EconomyManager Creation
Phase 5: CardManager Creation
Phase 6: GameUIManager Integration
Phase 6.5: GameInputHandler Creation ← NEW
Phase 7: RenderCoordination
Phase 8: GameScreen Cleanup
```

### Performance Considerations Added

- Event pooling to reduce GC pressure
- Lazy flush in RenderHolder
- Coordinate object reuse in render loops
- Viewport caching to avoid recalculation
- Performance metrics tracking (dispatch time, render time, GC frequency)

### Error Handling Patterns Added

- Resource disposal wrapper with error handling
- Asset loading with fallback textures
- Rendering state validation
- Event dispatch error isolation
- Input validation before processing

### Remaining Considerations

1. **Testing Strategy**: Need comprehensive tests for event-driven flows
2. **Multi-Viewport Support**: Current design assumes single viewport per manager
3. **Replay System**: Event-based architecture enables replay feature
4. **Save/Load**: Immutable BattleContext simplifies serialization

---

## Proposed Solution

### Core Abstractions

**1. GameMode Interface**
```java
/**
 * Game mode abstraction - decouples Screen from game logic
 * Enables easy extension for different game types (AutoChess, Tutorial, Survival, etc.)
 */
public interface GameMode {
    /**
     * Called when entering this game mode
     */
    void onEnter();

    /**
     * Update game logic
     */
    void update(float delta);

    /**
     * Render game content
     * @param holder Rendering context with SpriteBatch and ShapeRenderer
     */
    void render(RenderHolder holder);

    /**
     * Handle input events with unified context
     * InputContext provides both screen and world coordinates
     * Managers can choose which coordinate system to use based on their needs
     */
    void handleInput(InputContext context);

    /**
     * Pause game logic
     */
    void pause();

    /**
     * Resume game logic
     */
    void resume();

    /**
     * Called when exiting this game mode
     */
    void onExit();

    /**
     * Cleanup resources
     */
    void dispose();
}
```

**2. RenderHolder Class**
```java
/**
 * Centralized rendering context
 * Holds SpriteBatch and ShapeRenderer references for all rendering operations
 * IMPORTANT: Viewport management is handled by individual managers, not by RenderHolder
 */
public class RenderHolder {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;

    public RenderHolder(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    /**
     * Flush both spriteBatch and shapeRenderer to ensure state consistency between managers
     * CRITICAL: Must be called before switching between SpriteBatch and ShapeRenderer
     * CRITICAL: Must be called when manager rendering is complete
     */
    public void flush() {
        spriteBatch.flush();
        // ShapeRenderer flush - prevents state corruption when switching renderers
        if (shapeRenderer.isDrawing()) {
            shapeRenderer.flush();
        }
    }

    /**
     * Check if either renderer is currently active (drawing)
     * Used for validation and debugging
     */
    public boolean isActive() {
        return spriteBatch.isDrawing() || shapeRenderer.isDrawing();
    }
}
```

**2a. InputContext Class**
```java
/**
 * Unified input context with proper coordinate system handling
 * Provides both screen coordinates and world coordinates to avoid confusion
 */
public class InputContext {
    /** Screen coordinates (from Gdx.input.getX()/getY()) */
    public final float screenX;
    public final float screenY;

    /** World coordinates (unprojected by camera) */
    public final float worldX;
    public final float worldY;

    /** Touch/mouse state */
    public final boolean justTouched;
    public final int pointer;
    public final int button;

    /** Input source for debugging */
    public final InputType type;

    public enum InputType {
        TOUCH, MOUSE
    }

    public InputContext(float screenX, float screenY,
                     float worldX, float worldY,
                     boolean justTouched, int pointer, int button,
                     InputType type) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.worldX = worldX;
        this.worldY = worldY;
        this.justTouched = justTouched;
        this.pointer = pointer;
        this.button = button;
        this.type = type;
    }

    /**
     * Factory method to create InputContext from LibGDX Input
     */
    public static InputContext fromInput(Camera camera) {
        float screenX = Gdx.input.getX();
        float screenY = Gdx.input.getY();
        Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));

        boolean justTouched = Gdx.input.justTouched();
        InputType type = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)
            ? InputType.TOUCH : InputType.MOUSE;

        return new InputContext(screenX, screenY,
                            worldPos.x, worldPos.y,
                            justTouched, -1, -1,
                            type);
    }
}
```

**3. GameRenderer Interface**
```java
/**
 * Unified rendering interface for all managers that need to render
 */
public interface GameRenderer {
    /**
     * Render content using provided rendering context
     * @param holder Contains SpriteBatch and ShapeRenderer
     */
    void render(RenderHolder holder);
}
```

**4. BattleContext Class**
```java
/**
 * Battle context - aggregates all battle-related objects for shared data access
 * Immutable context with mutable state managed separately
 *
 * DESIGN DECISION: Use immutable context snapshot + state manager pattern
 * - BattleContext is fully immutable (created via Builder)
 * - Phase mutations happen through BattleState manager
 * - This prevents inconsistent state and makes testing easier
 */
public final class BattleContext {
    private final Battlefield battlefield;
    private final List<BattleUnitBlackboard> bbList;
    private final GamePhase phase;  // Immutable phase at construction time
    private final PlayerEconomy playerEconomy;  // Reference, not owned
    private final SynergyManager synergyManager;  // Reference
    private final int roundNumber;

    private BattleContext(Builder builder) {
        this.battlefield = builder.battlefield;
        this.bbList = List.copyOf(builder.bbList);
        this.phase = builder.phase;
        this.playerEconomy = builder.playerEconomy;
        this.synergyManager = builder.synergyManager;
        this.roundNumber = builder.roundNumber;
    }

    public Battlefield getBattlefield() {
        return battlefield;
    }

    public List<BattleUnitBlackboard> getBbList() {
        return bbList;
    }

    public GamePhase getPhase() {
        return phase;  // Returns immutable phase snapshot
    }

    public PlayerEconomy getPlayerEconomy() {
        return playerEconomy;
    }

    public SynergyManager getSynergyManager() {
        return synergyManager;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Create a new context with updated phase (immutable pattern)
     * @return New BattleContext with updated phase
     */
    public BattleContext withPhase(GamePhase newPhase) {
        return new Builder()
                .setBattlefield(this.battlefield)
                .setBbList(new ArrayList<>(this.bbList))
                .setPhase(newPhase)
                .setPlayerEconomy(this.playerEconomy)
                .setSynergyManager(this.synergyManager)
                .setRoundNumber(this.roundNumber)
                .build();
    }

    /**
     * Builder for safe construction
     */
    public static class Builder {
        private Battlefield battlefield;
        private List<BattleUnitBlackboard> bbList = new ArrayList<>();
        private GamePhase phase;
        private PlayerEconomy playerEconomy;
        private SynergyManager synergyManager;
        private int roundNumber = 1;

        public Builder setBattlefield(Battlefield battlefield) {
            this.battlefield = battlefield;
            return this;
        }

        public Builder setBbList(List<BattleUnitBlackboard> bbList) {
            this.bbList = new ArrayList<>(bbList);
            return this;
        }

        public Builder setPhase(GamePhase phase) {
            this.phase = phase;
            return this;
        }

        public Builder setPlayerEconomy(PlayerEconomy playerEconomy) {
            this.playerEconomy = playerEconomy;
            return this;
        }

        public Builder setSynergyManager(SynergyManager synergyManager) {
            this.synergyManager = synergyManager;
            return this;
        }

        public Builder setRoundNumber(int roundNumber) {
            this.roundNumber = roundNumber;
            return this;
        }

        public BattleContext build() {
            // Validate required fields
            if (battlefield == null) {
                throw new IllegalStateException("Battlefield is required");
            }
            if (phase == null) {
                throw new IllegalStateException("GamePhase is required");
            }
            return new BattleContext(this);
        }
    }
}
```

**4a. BattleState Manager**
```java
/**
 * Manages mutable battle state separately from immutable context
 * Used by BattleManager to track state changes that need to be persisted
 */
public class BattleState {
    private BattleContext currentContext;
    private final GameEventSystem eventSystem;

    public BattleState(BattleContext initialContext, GameEventSystem eventSystem) {
        this.currentContext = initialContext;
        this.eventSystem = eventSystem;
    }

    /**
     * Transition to new phase and notify listeners
     */
    public void transitionTo(GamePhase newPhase) {
        GamePhase oldPhase = currentContext.getPhase();
        currentContext = currentContext.withPhase(newPhase);

        // Send phase transition event
        eventSystem.send(new PhaseTransitionEvent(oldPhase, newPhase));
    }

    /**
     * Get current immutable context snapshot
     */
    public BattleContext getContext() {
        return currentContext;
    }

    /**
     * Update context (e.g., after round complete)
     */
    public void updateContext(BattleContext newContext) {
        this.currentContext = newContext;
    }
}

/**
 * Event for phase transitions
 */
public class PhaseTransitionEvent extends GameEvent {
    public final GamePhase fromPhase;
    public final GamePhase toPhase;
    public final long timestamp;

    public PhaseTransitionEvent(GamePhase fromPhase, GamePhase toPhase) {
        this.fromPhase = fromPhase;
        this.toPhase = toPhase;
        this.timestamp = System.currentTimeMillis();
    }
}
```

**5. AutoChessGameMode**
```java
/**
 * AutoChess game mode implementation
 * Coordinates all battle-related managers via event system (no direct dependencies)
 *
 * DESIGN DECISION: Fully decoupled managers
 * - Managers communicate ONLY through GameEventSystem
 * - No direct method calls between managers
 * - Input handling delegated to GameInputHandler, not managers directly
 */
public class AutoChessGameMode implements GameMode, GameEventListener {
    private final BattleState battleState;
    private final BattleManager battleManager;
    private final EconomyManager economyManager;
    private final CardManager cardManager;
    private final RenderCoordinator renderCoordinator;
    private final GameEventSystem eventSystem;
    private final GameInputHandler inputHandler;

    public AutoChessGameMode(BattleContext battleContext,
                              BattleManager battleManager,
                              EconomyManager economyManager,
                              CardManager cardManager,
                              RenderCoordinator renderCoordinator,
                              GameEventSystem eventSystem,
                              GameInputHandler inputHandler) {
        this.battleState = new BattleState(battleContext, eventSystem);
        this.battleManager = battleManager;
        this.economyManager = economyManager;
        this.cardManager = cardManager;
        this.renderCoordinator = renderCoordinator;
        this.eventSystem = eventSystem;
        this.inputHandler = inputHandler;

        // Register managers as event listeners
        eventSystem.register(battleManager);
        eventSystem.register(economyManager);
        eventSystem.register(cardManager);
    }

    @Override
    public void onEnter() {
        // Initialize managers
        battleManager.onEnter();
        economyManager.onEnter();
        cardManager.onEnter();
        inputHandler.initialize(this);
    }

    @Override
    public void update(float delta) {
        // Dispatch events first (event-driven architecture)
        eventSystem.dispatch();

        // Update all managers independently
        battleManager.update(delta);
        economyManager.update(delta);
        cardManager.update(delta);
        inputHandler.update(delta);
    }

    @Override
    public void render(RenderHolder holder) {
        // Render all managers in layer order
        battleManager.render(holder);
        cardManager.render(holder);      // Optional: if card manager renders
        holder.flush();

        // Economy manager typically doesn't render
        // economyManager.render(holder);
    }

    @Override
    public void handleInput(InputContext context) {
        // Delegate to input handler for proper event dispatch
        inputHandler.handleInput(context);
    }

    @Override
    public void pause() {
        battleManager.pause();
        economyManager.pause();
        cardManager.pause();
        inputHandler.pause();
    }

    @Override
    public void resume() {
        battleManager.resume();
        economyManager.resume();
        cardManager.resume();
        inputHandler.resume();
    }

    @Override
    public void onExit() {
        battleManager.onExit();
        economyManager.onExit();
        cardManager.onExit();
        inputHandler.onExit();
    }

    @Override
    public void dispose() {
        // Unregister event listeners
        eventSystem.unregister(battleManager);
        eventSystem.unregister(economyManager);
        eventSystem.unregister(cardManager);

        battleManager.dispose();
        economyManager.dispose();
        cardManager.dispose();
        inputHandler.dispose();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        // AutoChessGameMode can respond to game-level events
        // Individual managers handle their own events via registration
    }

    // Accessor for managers (used by InputHandler)
    public BattleManager getBattleManager() {
        return battleManager;
    }

    public CardManager getCardManager() {
        return cardManager;
    }

    public BattleState getBattleState() {
        return battleState;
    }
}
```

## Technical Approach

### Architecture

```
GameScreen (Screen Coordinator)
├── GameMode (AutoChessGameMode)
│   ├── BattleState (Mutable state manager)
│   ├── BattleContext (Immutable data snapshot)
│   ├── GameInputHandler (Input coordination)
│   │   ├── Drag state management
│   │   ├── Coordinate conversion
│   │   └── Event dispatch
│   ├── BattleManager (GameRenderer + GameEventListener)
│   │   ├── BehaviorTreeManager (unit trees, behaviorTreeMap)
│   │   ├── CharacterLifecycleManager (loadCharacter, unloadCharacter)
│   │   └── BattlePhaseManager (startBattle, updateBattle, endBattle)
│   ├── EconomyManager (GameEventListener)
│   │   ├── GoldManager (gold operations)
│   │   └── RoundRewardCalculator (battle rewards)
│   └── CardManager (optional GameRenderer + GameEventListener)
│       ├── CardPoolManager (card pool, shop refresh)
│       └── CardTransactionManager (buy, sell, upgrade)
├── GameEventSystem (Event Bus)
│   └── All managers register as listeners
└── RenderCoordinator
    └── RenderHolder (SpriteBatch + ShapeRenderer)

CRITICAL: Managers communicate ONLY through GameEventSystem
NO direct method calls between managers
```

**Key Principles:**

1. **Single Responsibility**: Each manager handles one domain
2. **Dependency Injection**: Managers receive dependencies via constructor
3. **Event-Driven Communication**: All inter-manager communication via GameEventSystem (NO direct calls)
4. **Rendering Isolation**: Each manager manages its own begin/end and viewport
5. **Immutable Context**: BattleContext is immutable, mutations via BattleState.withPhase()
6. **Unified Input**: InputContext provides both screen and world coordinates
7. **No Circular Dependencies**: Managers communicate through events, not direct references

### Implementation Phases

#### Phase 1: Foundation Architecture

**Goals**: Create core abstractions and prepare for migration

**Tasks:**

- [x] Create `GameMode` interface with lifecycle methods (use InputContext for handleInput)
- [x] Create `RenderHolder` class (SpriteBatch + ShapeRenderer, no viewport management)
- [x] Create `GameRenderer` interface with single render(RenderHolder) method
- [x] Create `InputContext` class with screen/world coordinates
- [ ] Create `BattleContext` class with immutable fields and Builder pattern
- [ ] Create `BattleState` class for mutable phase management
- [ ] Create `PhaseTransitionEvent` class
- [ ] Create `RenderCoordinator` class skeleton
- [x] Create `AutoChessGameMode` class skeleton (event-driven, no direct manager dependencies)
- [ ] Create placeholder managers (BattleManager, EconomyManager, CardManager)
- [x] Update `GameScreen` to use `GameMode` pattern
- [x] Update `GameScreen` input methods to create InputContext

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/game/GameMode.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/RenderHolder.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/GameRenderer.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/input/InputContext.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/battle/BattleContext.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/battle/BattleState.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/PhaseTransitionEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/RenderCoordinator.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/manage/AutoChessGameMode.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [x] All new classes compile without errors
- [x] GameScreen can instantiate AutoChessGameMode with placeholder managers
- [x] RenderHolder correctly provides SpriteBatch and ShapeRenderer access
- [x] InputContext factory method works correctly
- [x] BattleContext is immutable with proper Builder

**Risk**: Breaking existing code before managers are ready
**Mitigation**: Keep old code path functional until Phase 3

---

### Performance Optimization & Error Handling Patterns

#### Performance Optimization

**1. Event System Optimization**
```java
/**
 * Event pool to reduce GC pressure
 * Reuse event objects instead of creating new ones each frame
 */
public class EventPool<T extends GameEvent> {
    private final Queue<T> pool = new ArrayDeque<>();

    public T obtain(Class<T> type) {
        T event = pool.poll();
        if (event == null) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create event", e);
            }
        }
        event.reset();  // Reset event state
        return event;
    }

    public void free(T event) {
        if (pool.size() < 100) {  // Limit pool size
            pool.offer(event);
        }
    }
}

/**
 * Batch event processing
 * Process multiple events in one dispatch call
 */
public class GameEventSystem {
    private final List<GameEvent> eventQueue = new ArrayList<>(32);
    private final EventPool<GameEvent> eventPool;

    public void send(GameEvent event) {
        eventQueue.add(event);
    }

    public void dispatch() {
        // Process all queued events
        for (int i = 0; i < eventQueue.size(); i++) {
            GameEvent event = eventQueue.get(i);
            for (GameEventListener listener : listeners) {
                if (listener.accepts(event.getClass())) {
                    listener.onGameEvent(event);
                }
            }
            // Free event back to pool
            eventPool.free(event);
        }
        eventQueue.clear();
    }
}
```

**2. Rendering Optimization**
```java
/**
 * Lazy flush - only flush when necessary
 */
public class RenderHolder {
    private boolean needsFlush = false;

    public void markDirty() {
        needsFlush = true;
    }

    public void flush() {
        if (needsFlush) {
            spriteBatch.flush();
            if (shapeRenderer.isDrawing()) {
                shapeRenderer.flush();
            }
            needsFlush = false;
        }
    }
}

/**
 * Coordinate object reuse in render loop
 */
public class BattleRenderer implements GameRenderer {
    private final Vector2 tempVec = new Vector2();  // Reuse
    private final Color tempColor = new Color();     // Reuse

    public void render(RenderHolder holder) {
        SpriteBatch batch = holder.getSpriteBatch();
        batch.begin();

        for (BattleUnit unit : units) {
            // Reuse objects instead of creating new ones
            tempVec.set(unit.x, unit.y);
            tempColor.set(unit.color);

            batch.draw(texture, tempVec.x, tempVec.y);
        }

        batch.end();
        holder.markDirty();
    }
}
```

**3. Viewport Caching**
```java
/**
 * Cache viewport matrices to avoid recalculating each frame
 */
public class ViewportManager {
    private final Map<String, Viewport> viewports = new HashMap<>();
    private Viewport currentViewport;

    public void applyViewport(String name) {
        Viewport viewport = viewports.get(name);
        if (viewport != currentViewport) {
            viewport.apply();
            currentViewport = viewport;
        }
    }
}
```

#### LibGDX Error Handling Patterns

**1. Resource Disposal Wrapper**
```java
/**
 * Safe resource disposal with error handling
 */
public class ResourceDisposer {
    public static void safeDispose(Disposable resource) {
        try {
            if (resource != null) {
                resource.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("ResourceDisposer",
                "Error disposing resource: " + resource.getClass().getSimpleName(), e);
        }
    }

    public static void disposeAll(Disposable... resources) {
        for (Disposable resource : resources) {
            safeDispose(resource);
        }
    }
}

// Usage in dispose()
@Override
public void dispose() {
    ResourceDisposer.disposeAll(
        spriteBatch,
        shapeRenderer,
        textureAtlas,
        stage
    );
}
```

**2. Asset Loading Error Handling**
```java
/**
 * Safe asset loading with fallback
 */
public class SafeAssetLoader {
    public static Texture loadTexture(String path, Color fallbackColor) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AssetLoader", "Failed to load: " + path, e);
            // Create fallback texture
            Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            pixmap.setColor(fallbackColor);
            pixmap.fill();
            Texture fallback = new Texture(pixmap);
            pixmap.dispose();
            return fallback;
        }
    }
}
```

**3. Rendering State Validation**
```java
/**
 * Validate rendering state to catch begin/end mismatches
 */
public class RenderStateValidator {
    private int beginDepth = 0;

    public void begin() {
        beginDepth++;
        if (beginDepth > 1) {
            Gdx.app.error("RenderState", "Nested begin() detected");
        }
    }

    public void end() {
        beginDepth--;
        if (beginDepth < 0) {
            Gdx.app.error("RenderState", "Unmatched end() detected");
        }
    }

    public void assertIdle() {
        if (beginDepth != 0) {
            Gdx.app.error("RenderState", "Expected idle state, depth=" + beginDepth);
        }
    }
}
```

**4. Event Error Handling**
```java
/**
 * Safe event dispatch with error isolation
 */
public class GameEventSystem {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void dispatch() {
        for (GameEventListener listener : listeners) {
            try {
                for (GameEvent event : eventQueue) {
                    if (listener.accepts(event.getClass())) {
                        listener.onGameEvent(event);
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("EventSystem",
                    "Error in listener: " + listener.getClass().getSimpleName(), e);
                // Continue processing other listeners
            }
        }
        eventQueue.clear();
    }
}
```

**5. Input Validation**
```java
/**
 * Validate InputContext before processing
 */
public class InputValidator {
    public static boolean isValid(InputContext context) {
        if (Float.isNaN(context.screenX) || Float.isNaN(context.screenY)) {
            Gdx.app.error("Input", "Invalid screen coordinates");
            return false;
        }
        if (Float.isNaN(context.worldX) || Float.isNaN(context.worldY)) {
            Gdx.app.error("Input", "Invalid world coordinates");
            return false;
        }
        return true;
    }
}

// Usage in input handling
public void handleInput(InputContext context) {
    if (!InputValidator.isValid(context)) {
        return;  // Skip invalid input
    }
    // Process input...
}
```

#### Performance Metrics to Track

- **Event Dispatch Time**: < 5ms per frame (target: < 2ms)
- **Render Time**: < 16ms per frame (60 FPS)
- **GC Frequency**: < 1 per minute (target: < 1 per 5 minutes)
- **Memory Growth**: < 10MB per hour (target: stable)
- **Event Queue Size**: < 100 events per frame (warning threshold)

---

#### Phase 2: BattleContext & Data Migration

**Goals**: Migrate battle-related data from GameScreen to BattleContext

**Tasks:**

- [ ] Identify all battle-related fields in GameScreen:
  - [ ] `battlefield`
  - [ ] `bbList`
  - [ ] `unitTrees`
  - [ ] `behaviorTreeMap`
  - [ ] `characterMapping`
  - [ ] `phase`
  - [ ] `playerEconomy` (reference)
  - [ ] `synergyManager` (reference)
  - [ ] `roundNumber`
- [x] Create BattleContext with immutable fields
- [x] Implement BattleContext.Builder with validation
- [x] Implement `withPhase()` method for immutable updates
- [x] Create BattleState class for mutable state management
- [x] Create PhaseTransitionEvent class
- [x] Update GameScreen constructor to build BattleContext via Builder
- [ ] Migrate character loading/unloading logic to prepare for BattleManager

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/battle/BattleContext.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/battle/BattleState.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/PhaseTransitionEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- BattleContext is fully immutable (all fields final)
- BattleContext.Builder validates required fields
- BattleState manages mutable phase via withPattern()
- GameScreen creates BattleContext via Builder
- No compile errors
- BattleContext phase is immutable (mutations via withPhase())

**Risk**: Immutable pattern complexity
**Mitigation**: Start with simple implementation, add withPattern() after initial migration works

---

#### Phase 3: BattleManager Creation

**Goals**: Extract all battle-related logic from GameScreen

**Tasks:**

- [ ] Create `BattleManager` class implementing GameRenderer and GameEventListener
- [ ] Create `BattlePhaseManager` inner class for battle lifecycle
- [ ] Create `CharacterLifecycleManager` inner class for character loading/unloading
- [ ] Create `BehaviorTreeManager` inner class for behavior tree management
- [ ] Implement `startBattle()` method
- [ ] Implement `updateBattle()` method
- [ ] Implement `endBattle()` method
- [ ] Send BattleStartEvent, BattleEndEvent
- [ ] Update GameScreen to use BattleManager instead of direct battle calls

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/BattleManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- BattleManager handles all battle lifecycle
- Battle events are sent to GameEventSystem
- GameScreen battle logic is reduced to manager delegation
- Unit tests for battle state transitions

**Risk**: Breaking character loading logic
**Mitigation**: Preserve existing unit test behavior for loading/unloading

---

#### Phase 4: EconomyManager Creation

**Goals**: Extract economy-related logic from GameScreen

**Tasks:**

- [ ] Create `EconomyManager` class implementing GameEventListener
- [ ] Create `GoldManager` inner class for gold operations
- [ ] Create `RoundRewardCalculator` inner class
- [ ] Migrate `startBattleButton.setVisible()` logic
- [ ] Migrate gold spend operations (card purchase, refresh)
- [ ] Implement BattleEndEvent listener for round rewards
- [ ] Update GameScreen to delegate to EconomyManager

**Events to Send:**
- `GoldSpendEvent`
- `GoldEarnEvent`
- `RefreshEvent`

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/EconomyManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/Gold/GoldSpendEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/economy/GoldEarnEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/economy/RefreshEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- EconomyManager handles all gold operations
- Round rewards calculated correctly
- GoldSpendEvent sent on purchases
- GameScreen economy logic reduced to manager delegation

**Risk**: Economy calculation errors affecting gameplay
**Mitigation**: Preserve existing EconomyCalculator logic

---

#### Phase 5: CardManager Creation

**Goals**: Extract card-related logic from GameScreen

**Tasks:**

- [ ] Create `CardManager` class implementing GameRenderer and GameEventListener
- [ ] Create `CardPoolManager` inner class
- [ ] Create `CardTransactionManager` inner class
- [ ] Migrate `cardShop.refresh()` logic
- [ ] Migrate `cardShop.buyCard()` logic
- [ ] Migrate `isCardUpgradable()` and upgrade logic
- [ ] Send CardBuyEvent, CardSellEvent, CardUpgradeEvent

**Events to Send:**
- `CardBuyEvent`
- `CardSellEvent`
- `CardUpgradeEvent`

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/CardManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardBuyEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardSellEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardUpgradeEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- CardManager handles all card operations
- Card events sent to GameEventSystem
- Shop refresh and card purchase work correctly
- Card upgrade logic functional

**Risk**: Breaking card upgrade logic
**Mitigation**: Preserve existing CardUpgradeLogic

---

#### Phase 6: GameUIManager Integration

**Goals**: Ensure UIManager works with new architecture

**IMPORTANT: UIManager should NOT implement GameRenderer**
- Scene2D Stage has its own internal SpriteBatch management
- UIManager.render() calls Stage.draw() which handles begin/end internally
- UIManager rendering happens AFTER GameMode rendering, independently

**Tasks:**

- [ ] Review GameUIManager rendering - ensure Stage.draw() is used correctly
- [ ] Remove duplicate shop/deck rendering from GameScreen (if any)
- [ ] Ensure UIManager receives necessary data (BattleContext, CardManager) for card access
- [ ] Update UIManager to send card events directly to GameEventSystem
- [ ] Update drag preview - let UIManager handle Scene2D rendering
- [ ] Add BattleContext reference to UIManager for phase-based UI state

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/ui/GameUIManager.java` (MODIFY)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- UIManager does NOT implement GameRenderer (uses Stage.draw() independently)
- No duplicate rendering code in GameScreen
- UIManager sends appropriate events (CardBuyEvent, CardSellEvent)
- Drag preview works correctly (handled by UIManager)
- UIManager receives BattleContext for phase-based UI updates

**Risk**: Scene2D batch conflicts with GameMode batch
**Mitigation**: Ensure GameMode.render() ends its SpriteBatch before UIManager.draw() is called

**GameScreen.render() pattern after Phase 6:**
```java
@Override
public void render(float delta) {
    // Clear screen
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Update and render game mode
    gameMode.update(delta);
    gameMode.render(holder);  // Ends SpriteBatch

    // Render UI independently (Stage manages its own batch)
    uiManager.act(delta);
    uiManager.draw();
}
```

---

#### Phase 6.5: GameInputHandler Creation

**Goals**: Create centralized input handler to manage drag state and coordinate conversion

**Why This Phase is Critical:**
- Drag state (draggedCard, dragPosition) currently scattered in GameScreen
- Input handling needs unified InputContext for proper coordinate conversion
- Input events should trigger GameEventSystem events (not direct method calls)
- Single source of truth for input state

**Tasks:**

- [ ] Create `GameInputHandler` class
- [ ] Create `InputContext` class with screen/world coordinates
- [ ] Implement drag state management:
  - [ ] `draggedCard` (currently in GameScreen)
  - [ ] `dragPosition` (currently in GameScreen)
  - [ ] `dragSource` (deck/shop/battlefield)
  - [ ] `dragTarget` (valid drop positions)
- [ ] Implement InputContext.fromInput() factory method
- [ ] Implement handleInput() to dispatch appropriate events:
  - [ ] `DragStartEvent`
  - [ ] `DragMoveEvent`
  - [ ] `DragDropEvent`
  - [ ] `DragCancelEvent`
  - [ ] `CardClickEvent`
  - [ ] `BattlefieldClickEvent`
- [ ] Implement pause/resume/lifecycle methods
- [ ] Update GameScreen to delegate input to GameInputHandler
- [ ] Remove drag state from GameScreen

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/input/GameInputHandler.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/input/InputContext.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/DragStartEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/DragMoveEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/DragDropEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/DragCancelEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/CardClickEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**GameInputHandler structure:**
```java
public class GameInputHandler {
    private final AutoChessGameMode gameMode;
    private final GameEventSystem eventSystem;
    private final Camera camera;

    // Drag state (single source of truth)
    private Card draggedCard;
    private Vector2 dragPosition;
    private DragSource dragSource;

    public void handleInput(InputContext context) {
        // Handle drag operations
        if (context.justTouched) {
            handleTouchStart(context);
        } else if (Gdx.input.isTouched()) {
            handleTouchMove(context);
        } else {
            handleTouchEnd(context);
        }
    }

    private void handleTouchStart(InputContext context) {
        // Check for card hit
        if (hitCard(context.worldX, context.worldY)) {
            draggedCard = hitCard;
            dragPosition.set(context.worldX, context.worldY);
            eventSystem.send(new DragStartEvent(draggedCard, dragPosition));
        }
    }

    private void handleTouchMove(InputContext context) {
        if (draggedCard != null) {
            dragPosition.set(context.worldX, context.worldY);
            eventSystem.send(new DragMoveEvent(draggedCard, dragPosition));
        }
    }

    private void handleTouchEnd(InputContext context) {
        if (draggedCard != null) {
            eventSystem.send(new DragDropEvent(draggedCard, dragPosition));
            draggedCard = null;
        }
    }
}
```

**Success Criteria:**
- GameInputHandler handles all input processing
- Drag state is centralized (not in GameScreen)
- InputContext provides both screen and world coordinates
- All input operations send events (not direct manager calls)
- GameScreen delegates input to GameInputHandler
- Drag operations work correctly

**Risk**: Breaking input handling
**Mitigation**: Keep old input code in comments until Phase 8 cleanup

---

#### Phase 7: RenderCoordination

**Goals**: Implement RenderCoordinator with RenderHolder pattern

**IMPORTANT: Each manager manages its own viewport and begin/end**

**Tasks:**

- [ ] Create `RenderHolder` class (SpriteBatch + ShapeRenderer, no viewport management)
- [ ] Implement `flush()` method for both spriteBatch and shapeRenderer
- [ ] Create `GameRenderer` interface with single `render(RenderHolder)` method
- [ ] Create `RenderCoordinator` class (holds RenderHolder, manages render order)
- [ ] Implement `renderAll(List<GameRenderer>)` with flush between each manager
- [ ] Each manager implements GameRenderer:
  - [ ] BattleManager calls holder.flush() after its rendering
  - [ ] CardManager calls holder.flush() after its rendering
  - [ ] Each manager manages its own viewport (not RenderHolder)
- [ ] Update AutoChessGameMode to use RenderCoordinator
- [ ] Update GameScreen to create RenderHolder and pass to gameMode.render()

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/render/RenderHolder.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/GameRenderer.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/RenderCoordinator.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/manage/BattleManager.java` (MODIFY - implement GameRenderer)
- `core/src/main/java/com/voidvvv/autochess/manage/CardManager.java` (MODIFY - implement GameRenderer)
- `core/src/main/java/com/voidvvv/autochess/manage/AutoChessGameMode.java` (MODIFY)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**RenderManager rendering pattern:**
```java
public void render(RenderHolder holder) {
    SpriteBatch batch = holder.getSpriteBatch();
    ShapeRenderer shapes = holder.getShapeRenderer();

    // Manager manages its own viewport
    batch.setProjectionMatrix(camera.combined);

    // Manager manages its own begin/end
    batch.begin();
    try {
        // Rendering operations
        renderUnits(batch);
        renderProjectiles(batch);
    } finally {
        batch.end();
        // Flush to ensure next manager gets clean state
        holder.flush();
    }
}
```

**RenderCoordinator pattern:**
```java
public void renderAll(List<GameRenderer> renderers, float delta) {
    // Clear screen
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Render each manager in layer order
    for (GameRenderer renderer : renderers) {
        renderer.render(holder);
        // Flush ensures state consistency between managers
        holder.flush();
    }
}
```

**Success Criteria:**
- RenderCoordinator calls managers in correct order
- Each manager manages its own begin/end and viewport
- flush() called after each manager renders
- Rendering works without begin/end conflicts
- ShapeRenderer state properly flushed

**Risk**: Rendering order issues, state corruption
**Mitigation**:
- Add assertion: `assert !holder.isActive()` before each manager renders
- Log warning if begin/end mismatch detected
- Test with multiple managers rendering different content types

---

#### Phase 8: GameScreen Cleanup

**Goals**: Remove migrated code and simplify GameScreen

**Tasks:**

- [ ] Remove all battle logic methods from GameScreen
- [ ] Remove all economy logic methods from GameScreen
- [ ] Remove all card logic methods from GameScreen
- [ ] Remove duplicate UI rendering code
- [ ] Remove drag state from GameScreen (moved to InputHandler in Phase 6.5)
- [ ] Remove input handling code from GameScreen (moved to InputHandler)
- [ ] Update GameScreen.render() to use GameMode pattern + UIManager
- [ ] Update GameScreen lifecycle methods (delegate to GameMode)
- [ ] Ensure GameScreen only coordinates GameMode, UIManager, and InputHandler

**GameScreen final structure (~150-200 lines):**
```java
public class GameScreen implements Screen {
    private final KzAutoChess game;
    private GameMode gameMode;
    private GameUIManager uiManager;
    private GameInputHandler inputHandler;
    private RenderHolder renderHolder;

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and render game mode
        gameMode.update(delta);
        gameMode.render(renderHolder);

        // Render UI independently (Scene2D manages its own batch)
        uiManager.act(delta);
        uiManager.draw();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        InputContext context = InputContext.fromInput(camera);
        gameMode.handleInput(context);
        return true;
    }

    // Similar for touchDragged, touchUp, etc.

    @Override
    public void resize(int width, int height) {
        uiManager.resize(width, height);
    }

    @Override
    public void pause() {
        gameMode.pause();
    }

    @Override
    public void resume() {
        gameMode.resume();
    }

    @Override
    public void hide() {
        gameMode.onExit();
    }

    @Override
    public void show() {
        gameMode.onEnter();
    }

    @Override
    public void dispose() {
        gameMode.dispose();
        uiManager.dispose();
    }
}
```

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY - remove ~800 lines)

**Success Criteria:**
- GameScreen is under 200 lines (target: ~150 lines)
- Only coordinates GameMode, UIManager, and InputHandler
- All business logic delegated
- No commented-out old code
- All drag and input state removed

**Risk**: Accidental deletion of needed code
**Mitigation**: Compile and run tests before final cleanup
**Rollback**: Keep git tag `phase-7-complete` before cleanup

---

## System-Wide Impact

### Interaction Graph

```
User Input
  → GameInputHandler
    → GameEventSystem
        → BattleManager
        → Battlefield updates
        → BehaviorTree step()
    → GameUIManager
        → UI updates
    → CardManager
        → Shop refresh

Drag Operations
  → GameInputHandler
    → DragStartedEvent
        → GameUIManager (drag preview)
    → DragMovedEvent
        → GameUIManager (drag preview update)
    → DroppedEvent
        → BattleManager (character placement)
        → CardManager (card return)

Battle Events
  → BattleManager
    → BattleStartEvent
        → GameUIManager (hide battle button)
    → BattleEndEvent
        → EconomyManager (calculate rewards)
        → GameUIManager (show battle button)
        → Battlefield (reset)
```

### Error & Failure Propagation

**Error Classes:**
1. **ManagerInitializationError** - Thrown when a manager receives null dependencies
2. **BattleContextError** - Thrown when battle state is inconsistent
3. **RenderingError** - Thrown when begin/end mismatch occurs

**Error Handling Strategy:**
- Try-catch around manager update() calls
- Log errors with context (manager name, delta)
- Graceful degradation (skip update, continue game)
- Display error message to user via UIManager

**Retry Conflicts:**
- None expected - managers should fail fast, not retry

### State Lifecycle Risks

**State Objects:**
1. **BattleContext** - Shared across BattleManager, EconomyManager
2. **RenderHolder** - Shared across all managers in render
3. **GamePhase** - Mutated by BattleManager, read by others

**Cleanup Mechanisms:**
- BattleContext is final (immutable) except phase
- RenderHolder is final (immutable)
- Phase is only mutable via BattleContext.setPhase()
- All managers dispose in AutoChessGameMode.dispose()

**Race Condition Prevention:**
- Event dispatch happens once per frame before any updates
- BattleContext phase updates happen in BattleManager only
- RenderHolder.flush() ensures spriteBatch state between managers

### API Surface Parity

**Interfaces Requiring Updates:**

| Interface | Current Location | New Location | Changes |
|-----------|----------------|-------------|---------|
| GameEventListener | GameEventSystem | Managers implement directly | Add onGameEvent() to managers |
| GameRenderer | None | New interface | Implement in managers and UIManager |
| GameMode | None | New interface | AutoChessGameMode implements |
| RenderHolder | None | New class | Created as rendering context |

### Integration Test Scenarios

**Scenario 1: Full Battle Flow**
1. Player starts game (PLACEMENT phase)
2. Buys cards from shop
3. Places characters on battlefield
4. Clicks start battle
5. Battle runs (BATTLE phase)
6. Battle ends (win/lose)
7. Gold awarded
8. Player returns to PLACEMENT phase

**Expected Behavior:**
- Shop cards available in PLACEMENT
- Characters visible and draggable in PLACEMENT
- Shop disabled in BATTLE
- Battle button hidden during battle
- Gold updated after battle
- Characters reset after battle

**Test Data:**
- Initial gold: 10
- Card cost: 3
- Battle reward: +5 (win) or +2 (lose)

---

**Scenario 2: Drag and Drop**
1. Player drags card from deck
2. Drops on battlefield
3. Character created and loaded
4. Player drags character on battlefield
5. Drops back to deck
6. Character unloaded and returned to deck

**Expected Behavior:**
- Card visible during drag
- Character follows mouse
- Valid drop positions highlighted
- Invalid drops rejected
- Deck updated after character return

---

**Scenario 3: Shop Refresh**
1. Player clicks refresh button
2. Gold deducted
3. New cards generated
4. Shop UI updated

**Expected Behavior:**
- Refresh button disabled if insufficient gold
- Gold decreases
- Shop shows new cards
- Refresh button shows updated cost

---

**Scenario 4: Battle During Window Resize**
1. Battle in progress
2. Player resizes window
3. Viewport updates
4. Characters continue rendering correctly

**Expected Behavior:**
- Battlefield stays centered
- Characters don't clip
- Battle continues normally

---

**Scenario 5: Multiple Rapid Events**
1. Many damage events in single frame
2. Rapid shop operations
3. Fast drag movements

**Expected Behavior:**
- All events processed in order
- No event overflow
- Performance remains acceptable

---

## Acceptance Criteria

### Functional Requirements

- [ ] GameScreen implements Screen interface with GameMode abstraction
- [ ] AutoChessGameMode correctly initializes all managers
- [ ] RenderHolder provides access to SpriteBatch and ShapeRenderer
- [ ] BattleContext holds all battlefield-related data
- [ ] BattleManager handles battle lifecycle (start, update, end)
- [ ] EconomyManager handles gold operations and round rewards
- [ ] CardManager handles card pool, shop, and transactions
- [ ] UIManager implements GameRenderer and manages UI independently
- [ ] RenderCoordinator calls managers in correct layer order
- [ ] Each manager manages its own begin/end
- [ ] Event-driven communication between all components
- [ ] All drag operations work correctly
- [ ] Battle transitions work correctly (PLACEMENT ↔ BATTLE)

### Non-Functional Requirements

**Performance:**
- [ ] No memory leaks (dispose all resources)
- [ ] No GC pressure from per-frame allocations
- [ ] Event dispatch completes within 5ms per frame
- [ ] Rendering maintains 60 FPS target

**Code Quality:**
- [ ] No code duplication between managers
- [ ] All classes follow project conventions
- [ ] Public APIs are well-documented with JavaDoc
- [ ] Error handling with try-catch around manager updates

**Testing:**
- [ ] Unit tests for each manager
- [ ] Integration tests for event-driven flows
- [ ] UI tests for drag-and-drop
- [ ] Performance tests for large battles

### Quality Gates

- [ ] Code review approval required before merge
- [ ] Minimum 80% test coverage
- [ ] No SonarQube critical issues
- [ ] Documentation updated for new classes
- [ ] Feature flag for easy rollback

## Success Metrics

**Code Metrics:**
- GameScreen reduced from 1032 to < 200 lines
- Each manager < 300 lines
- Average method length < 50 lines
- Maximum nesting depth ≤ 4 levels
- 0 circular dependencies between managers

**Performance Metrics:**
- Event dispatch time < 5ms per frame (target: < 2ms)
- Render time < 16ms per frame (60 FPS target)
- GC frequency < 1 per minute (target: < 1 per 5 minutes)
- Memory growth < 10MB per hour (target: stable)
- Event queue size < 100 events per frame (warning threshold)
- No object allocation in render loops
- No GC spikes in profiling

---

## Enhancement Summary

**Deepened on:** 2026-03-12
**Sections enhanced:** 10 sections (Overview, Technical Approach/Architecture, Implementation Phases, System-Wide Impact, Acceptance Criteria, Success Metrics, Dependencies & Risks, Resource Requirements, Future Considerations, Documentation Plan)
**Research agents used:** 4 parallel research agents (Architecture patterns, Java best practices, testing strategies, LibGDX practices)

### Key Improvements

1. **Architecture Patterns Documentation**
   - Added specific file references for each pattern (BattleUnitBlackboard.java:410, GameEventSystem.java:14-60, GameUIManager.java:58-67)
   - Added JavaDoc documentation examples for all new classes

2. **Domain-Driven Design (DDD) Application**
   - Recommended creating bounded contexts for different game domains
   - Documented service layer architecture with dependency injection
   - Example code for BattleDomain interface with service pattern

3. **Service Layer Architecture**
   - Recommended service interfaces for domain operations
   - Documented Dependency Injection (without Spring) using constructor injection
   - Example service registry pattern

4. **State Machine Integration**
   - Recommended integrating LibGDX-AI state machine with guard clauses
   - Documented transition rules and state guard pattern

5. **Thread Safety Patterns**
   - Recommended using immutable game state snapshots
   - Documented concurrent collection usage for event queues
   - Reference: Java Virtual Threads and Structured Concurrency

6. **Performance Optimization System**
   - Comprehensive LibGDX best practices guide
   - SpriteBatch usage patterns (begin/end management, batch optimization)
   - ShapeRenderer performance best practices
   - Memory management and GC pressure reduction
   - Performance profiling tools and metrics

### Anti-Patterns to Avoid

**1. Creating Objects in Render Loop** - Critical for performance
   - Using Color object per allocation (BAD pattern)
   - Reuse Color object instead of creating new each frame

2. **Not Disposing Resources** - Critical for memory leaks
   - Failing to call dispose() on Disposable resources

3. **Multiple Begin/End Calls** - Performance killer
   - Not properly batching drawing operations

4. **Not Updating Viewport on Resize** - Breaking camera functionality

5. **Mixing SpriteBatch and ShapeRenderer** - Performance impact
   - Caution about SpriteBatch/ShapeRenderer order without flush()

### Code Examples with Anti-Patterns

**Proper: Reused Color object in render loop**
```java
private final Color tempColor = new Color(1, 0, 0, 1);

public void render() {
    batch.begin();
    for (int i = 0; i < 100; i++) {
        batch.setColor(tempColor); // Reuse same object
    }
    batch.end();
}
```

**Proper: Begin/end management in managers**
```java
public void render() {
    batch.begin();   // BattleManager
    // Drawing operations
    batch.end();   // EconomyManager (if renders)
    // CardManager (if renders)
    batch.begin();   // CardManager (if renders)
    // ...
    batch.end();   // UI Manager (separate batch)
}
```

**Proper: Disposing resources properly**
```java
@Override
public void dispose() {
    texture.dispose();
    batch.dispose();
    shapeRenderer.dispose();
    assetManager.dispose();
}
```

---

## 2. Implementation Phases

### Phase 1: Foundation Architecture

**Tasks:**
- [ ] Create `GameMode` interface with lifecycle methods
- [ ] Create `RenderHolder` class with SpriteBatch and ShapeRenderer
- [ ] Create `GameRenderer` interface
- [ ] Create `BattleContext` class with Builder pattern
- [ ] Create `RenderCoordinator` class skeleton
- [ ] Create `AutoChessGameMode` class skeleton
- [ ] Update `GameScreen` to use `GameMode` pattern

**Code Examples:**
```java
// RenderHolder with viewport management
public class RenderHolder {
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private Viewport currentViewport;

    public void applyViewport(Viewport viewport) {
        this.currentViewport = viewport;
        viewport.apply();
    }
}
```

```java
// GameRenderer interface
public interface GameRenderer {
    void render(RenderHolder holder);
}
```

```java
// BattleContext with thread-safe design
public final class BattleContext {
    private final Battlefield battlefield;
    private final List<BattleUnitBlackboard> bbList;
    private final GamePhase phase;
    private final PlayerEconomy playerEconomy;  // Reference, not owned
    private final SynergyManager synergyManager;

    private BattleContext(Builder builder) {
        this.battlefield = builder.battlefield;
        this.bbList = List.copyOf(builder.bbList);
        this.phase = builder.phase;
        this.playerEconomy = builder.playerEconomy;
        this.synergyManager = builder.synergyManager;
    }
}
```

### Phase 2: BattleContext & Data Migration

**Tasks:**
- [ ] Identify all battle-related fields in GameScreen
- [ ] Create BattleContext fields with proper accessors
- [ ] Implement BattleContext.Builder
- [ ] Update GameScreen constructor to build BattleContext

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/battle/BattleContext.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] BattleContext holds all battlefield data
- [ ] GameScreen creates BattleContext via Builder
- [ ] No compile errors
- [ ] BattleContext phase is immutable (only updatable via setPhase)

**Risk:** Data ownership confusion
**Mitigation:** Document which objects are owned vs referenced in BattleContext

---

### Phase 3: BattleManager Creation

**Tasks:**
- [ ] Create `BattleManager` class implementing GameRenderer and GameEventListener
- [ ] Create `BattlePhaseManager` inner class for battle lifecycle
- [ ] Create `CharacterLifecycleManager` inner class for character loading/unloading
- [ ] Create `BehaviorTreeManager` inner class for behavior tree management
- [ ] Implement `startBattle()` method
- [ ] Implement `updateBattle()` method
- [ ] Implement `endBattle()` method
- [ ] Send BattleStartEvent, BattleEndEvent
- [ ] Update GameScreen to use BattleManager instead of direct battle calls

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/BattleManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] BattleManager handles all battle lifecycle
- [ ] Battle events are sent to GameEventSystem
- [ ] GameScreen battle logic is reduced to manager delegation
- [ ] Unit tests for battle state transitions

**Risk:** Breaking character loading logic
**Mitigation:** Preserve existing unit test behavior for loading/unloading

---

### Phase 4: EconomyManager Creation

**Tasks:**
- [ ] Create `EconomyManager` class implementing GameEventListener
- [ ] Create `GoldManager` inner class for gold operations
- [ ] Create `RoundRewardCalculator` inner class
- [ ] Migrate `startBattleButton.setVisible()` logic
- [ ] Migrate gold spend operations (card purchase, refresh)
- [ ] Implement BattleEndEvent listener for round rewards
- [ ] Update GameScreen to delegate to EconomyManager

**Events to Send:**
- `GoldSpendEvent`
- `GoldEarnEvent`
- `RefreshEvent`

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/EconomyManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/gold/GoldSpendEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/economy/GoldEarnEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/economy/RefreshEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] EconomyManager handles all gold operations
- [ ] Round rewards calculated correctly
- [ ] GoldSpendEvent sent on purchases
- [ ] GameScreen economy logic reduced to manager delegation

**Risk:** Economy calculation errors affecting gameplay
**Mitigation:** Preserve existing EconomyCalculator logic

---

### Phase 5: CardManager Creation

**Tasks:**
- [ ] Create `CardManager` class implementing GameRenderer and GameEventListener
- [ ] Create `CardPoolManager` inner class
- [ ] Create `CardTransactionManager` inner class
- [ ] Migrate `cardShop.refresh()` logic
- [ ] Migrate `cardShop.buyCard()` logic
- [ ] Migrate `isCardUpgradable()` and upgrade logic
- [ ] Send CardBuyEvent, CardSellEvent, CardUpgradeEvent

**Events to Send:**
- `CardBuyEvent`
- `CardSellEvent`
- `CardUpgradeEvent`

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/manage/CardManager.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardBuyEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardSellEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/event/card/CardUpgradeEvent.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] CardManager handles all card operations
- [ ] Card events sent to GameEventSystem
- [ ] Shop refresh and card purchase work correctly
- [ ] Card upgrade logic functional

**Risk:** Breaking card upgrade logic
**Mitigation:** Preserve existing CardUpgradeLogic

---

### Phase 6: GameUIManager Integration

**Tasks:**
- [ ] Implement `GameRenderer` interface in UIManager
- [ ] Add `render(RenderHolder holder)` method to UIManager
- [ ] Remove duplicate shop/deck rendering from GameScreen
- [ ] Ensure UIManager receives BattleContext for card access
- [ ] Update UIManager to send card events directly
- [ ] Update drag preview to use RenderHolder

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/ui/GameUIManager.java` (MODIFY)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] UIManager implements GameRenderer
- [ ] No duplicate rendering code in GameScreen
- [ ] UIManager sends appropriate events
- [ ] Drag preview works correctly

**Risk:** Breaking existing UI functionality
**Mitigation:** Thorough UI testing after changes

---

### Phase 7: RenderCoordination

**Tasks:**
- [ ] Implement `RenderCoordinator` class
- [ ] Implement `renderAll(List<GameRenderer>)` method
- [ ] Ensure `flush()` called after each manager render
- [ ] Create specific renderer classes if needed:
  - [ ] BattleRenderer
  - [ ] CardRenderer (if card manager renders)
- [ ] Update AutoChessGameMode to use RenderCoordinator
- [ ] Update GameScreen to pass RenderHolder to gameMode.render()

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/render/RenderCoordinator.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/GameRenderer.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/render/BattleRenderer.java` (NEW)
- `core/src/main/java/com/voidvvv/autochess/manage/AutoChessGameMode.java` (MODIFY)
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY)

**Success Criteria:**
- [ ] RenderCoordinator calls managers in correct order
- [ ] Each manager manages its own begin/end
- [ ] flush() called between manager renders
- [ ] Rendering works without begin/end conflicts

**Risk:** Rendering order issues
**Mitigation:** Test rendering with multiple managers simultaneously

---

### Phase 8: GameScreen Cleanup

**Tasks:**
- [ ] Remove all battle logic methods from GameScreen
- [ ] Remove all economy logic methods from GameScreen
- [ ] Remove all card logic methods from GameScreen
- [ ] Remove duplicate UI rendering code
- [ ] Remove drag state from GameScreen (moved to InputHandler)
- [ ] Update GameScreen.render() to use GameMode pattern
- [ ] Update GameScreen lifecycle methods
- [ ] Ensure GameScreen only coordinates GameMode and UIManager

**Files Modified:**
- `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java` (MODIFY - remove ~700 lines)

**Success Criteria:**
- [ ] GameScreen is under 200 lines
- [ ] Only coordinates GameMode and UIManager
- [ ] All business logic delegated
- [ ] No commented-out old code

**Risk:** Accidental deletion of needed code
**Mitigation:** Compile and run tests before final cleanup

**Quality Metrics:**
- 0 critical bugs after refactoring
- 90%+ test coverage
- All public APIs documented

## Dependencies & Risks

### Dependencies

**New Dependencies (None):**
This refactoring creates new abstractions but introduces **no external dependencies**.

**Internal Dependencies:**
- Existing GameEventSystem
- Existing GameInputHandler
- Existing GameUIManager
- Existing ViewManagement
- Existing ModelHolder pattern

**Critical Path:**
Phase 2 (BattleContext & BattleState) → Phase 3 (BattleManager) → Phase 4 (EconomyManager) → Phase 5 (CardManager) → Phase 6.5 (InputHandler) → Phase 7 (RenderCoordination) → Phase 8 (Cleanup)

**Risk:** Manager initialization order must be correct
**Mitigation:** Use Builder pattern and validate dependencies in constructors

### Risk Analysis

| Risk | Likelihood | Impact | Mitigation Strategy |
|-------|-------------|--------|-------------------|
| Breaking battle logic | Medium | High | Preserve unit test behavior, add integration tests |
| UI rendering regression | Medium | High | Comprehensive UI testing, keep old code path |
| Event timing issues | Low | Medium | Batch events, validate order, add logging |
| State synchronization | Medium | High | Immutable BattleContext, clear ownership documentation |
| Performance regression | Low | Medium | Profile before/after, keep RenderHolder.flush() |
| Drag state corruption | Low | High | Single source of truth in InputHandler |
| Migration rollback difficulty | Low | Medium | Feature flags per phase, git tags |

### Risk Mitigation Summary

1. **Feature Flags**: Add `config/FeatureFlags.java` with toggle for each phase
2. **Git Tags**: Tag after each phase (`phase-1-battle-context`, `phase-3-battle-manager`, etc.)
3. **Rollback Strategy**: Keep old code in comments for 2 phases before deletion
4. **Testing**: Comprehensive testing after each phase before proceeding
5. **Monitoring**: Add logging at critical points (battle start/end, manager errors)

## Resource Requirements

**Development:**
- 1 developer experienced in Java/LibGDX
- Estimated effort: 2-3 weeks
- Branch: `screen_refine_everything1`

**Testing:**
- Unit tests: ~20 test classes
- Integration tests: ~5 scenarios
- Manual testing time: 2 days

**Documentation:**
- JavaDoc for all new public classes
- Architecture decision document
- Migration guide for team members

## Future Considerations

### Extensibility

**Game Mode Switching:**
- After this refactoring, adding new game modes (Tutorial, Survival) requires only:
  1. Implement GameMode interface
  2. Create new mode class
  3. Add factory or registry

**Example:**
```java
public class TutorialGameMode implements GameMode {
    // Tutorial-specific logic
}
```

**Manager Reusability:**
- BattleManager could be extended for different battle rules (PvP, co-op)
- EconomyManager could support multiple currencies
- CardManager could support different card sets

### Potential Enhancements

**Save/Load System:**
- Add `saveState()` and `loadState()` to GameMode
- Serialize BattleContext and PlayerEconomy

**Debug Mode:**
- Add `isDebug()` flag to managers
- Render debug overlays (hit boxes, event counts)

**Replay System:**
- Record event stream for replay
- Restore game state from snapshot

## Documentation Plan

**To Create:**
1. JavaDoc for all new classes (RenderHolder, GameMode, BattleContext, managers)
2. Architecture diagram showing component relationships
3. Migration checklist for existing code patterns
4. Event type catalog

**To Update:**
1. CLAUDE.md - Add RenderHolder pattern to conventions
2. CODEMAPS - Update architecture section
3. Create GameScreen refactoring guide

## Sources & References

### Origin

**Brainstorm document:** [docs/brainstorms/2026-03-12-gamescreen-refactor-brainstorm.md](docs/brainstorms/2026-03-12-gamescreen-refactor-brainstorm.md)

**Key decisions carried forward:**
- **GameMode abstraction** - Decouples Screen from game logic for extensibility
- **BattleContext** - Aggregates battle objects for shared data access with builder pattern
- **RenderHolder pattern** - Centralized rendering context with SpriteBatch and ShapeRenderer, flush() for state management
- **GameRenderer interface** - Unified rendering interface for managers
- **Event-driven communication** - All inter-manager communication via GameEventSystem
- **Rendering begin/end isolation** - Each manager manages its own begin/end

### Internal References

**Architecture patterns:**
- [docs/CODEMAPS/architecture.md](docs/CODEMAPS/architecture.md) - Model/Update-Render separation principle
- [docs/CODEMAPS/battle-system.md](docs/CODEMAPS/battle-system.md) - Battle architecture patterns
- [.claude/skills/kz-autochess-patterns.md](.claude/skills/kz-autochess-patterns.md) - Project conventions

**Existing implementations:**
- `GameUIManager.java:58-67` - GameEventListener pattern
- `BattleUnitBlackboard.java:1-409` - Blackboard pattern
- `GameEventSystem.java` - ModelHolder pattern, event dispatching

**Related refactor plans:**
- [docs/plans/2026-03-11-refactor-screen-component-based-deepened-plan.md](docs/plans/2026-03-11-refactor-screen-component-based-deepened-plan.md) - Screen component-based refactoring
- [docs/plans/2026-03-08-refactor-model-update-render-separation-plan.md](docs/plans/2026-03-08-refactor-model-update-render-separation-plan.md) - Model/Update/Render separation

### External References

**LibGDX Documentation:**
- [Screen API](https://libgdx.com/wiki/Screen-interface) - Lifecycle methods
- [SpriteBatch](https://libgdx.com/wiki/SpriteBatch) - Rendering best practices
- [InputMultiplexer](https://libgdx.com/wiki/InputMultiplexer) - Input handling

**Design Patterns:**
- [Blackboard Pattern](https://en.wikipedia.org/wiki/Blackboard_(design_pattern)) - Context aggregation
- [Builder Pattern](https://refactoring.guru/design-patterns/builder) - Safe object construction
- [Observer Pattern](https://refactoring.guru/design-patterns/observer) - Event-driven communication

### Related Work

- Previous GameScreen refactor (Phase 1 & 2): Event system and UI management
- Battle system refactoring: BehaviorTree integration
- Model/Update/Render separation: RenderDataManager creation
