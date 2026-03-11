# Architecture Overview

<!-- Generated: 2026-03-12 | Files scanned: ~120 | Token estimate: ~600 -->

## Project Type
LibGDX-based auto-battler game using Java 25, ECS (Ashley), Box2D physics

## Technology Stack
- **Framework**: LibGDX 1.14.0
- **ECS**: Ashley (entity-component-system)
- **Physics**: Box2D
- **AI**: LibGDX AI (behavior trees)
- **Text**: FreeType
- **Build**: Gradle

## Module Structure
```
kz_auto_chess/
├── core/           # Main game logic
│   ├── battle/     # Combat AI & blackboards
│   ├── event/      # Event system
│   ├── input/      # Input handling
│   ├── logic/      # Game rules
│   ├── manage/     # Managers (particles, projectiles)
│   ├── model/      # Data models
│   ├── render/     # Rendering
│   ├── screens/    # Game screens
│   ├── sm/         # State machine
│   ├── ui/         # UI components
│   ├── updater/    # Update logic
│   └── utils/      # Utilities
└── lwjgl3/         # Desktop launcher
```

## Architecture Principles
1. **Model/Updator/Manager/Render Separation**
2. **Blackboard Pattern** for battle units
3. **State Machine** for character behaviors
4. **Event System** for decoupled communication

## Key Entry Points
- `Main.java` - Application entry
- `KzAutoChess.java` - Game lifecycle manager
- `StartScreen.java` → `LevelSelectScreen.java` → `GameScreen.java`

## Data Flow
```
Input → GameInputHandler → GameEventSystem
                             ↓
                         Blackboard → Behavior Tree → State Machine → Character Actions
                             ↓
                         Model Updates → Render Updates
```
