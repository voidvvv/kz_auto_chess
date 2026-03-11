# CODEMAPS

<!-- Generated: 2026-03-12 | Token estimate: ~200 -->

Auto-battler game architecture documentation (token-lean for AI context).

## Available Codemaps

| File | Description | Tokens |
|------|-------------|---------|
| [architecture.md](architecture.md) | High-level system structure, modules, data flow | ~600 |
| [battle-system.md](battle-system.md) | Combat AI, state machines, damage, skills | ~500 |
| [ui-system.md](ui-system.md) | Screens, UI manager, viewport, input | ~500 |
| [data-models.md](data-models.md) | Cards, economy, synergies, projectiles | ~500 |

## Quick Reference

**Entry Point**: `Main.java` → `KzAutoChess.java`

**Screen Flow**: `StartScreen` → `LevelSelectScreen` → `GameScreen`

**Main Game Loop**: `GameScreen.render()` → `gameEventSystem.dispatch()` → `battleUpdater.update()` → `renderWorldContent()` → `gameUIManager.draw()`

**Key Principle**: Model/Updator/Manager/Render separation

## Architecture Patterns
- Blackboard pattern (`BattleUnitBlackboard`)
- State machine (`StateMachine<T>`)
- Event system (`GameEventSystem`)
- ECS (Ashley, but minimal usage)
