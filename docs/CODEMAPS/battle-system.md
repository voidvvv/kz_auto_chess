# Battle System

<!-- Generated: 2026-03-12 | Files scanned: ~20 | Token estimate: ~500 -->

## Components

### AI & Behavior
- `BattleUnitBlackboard` - Per-unit blackboard (state, target, battlefield ref)
- `UnitBehaviorTreeFactory` - Creates behavior trees
- `AttackTargetTask` - Attack action node
- `FindEnemyTask` - Target selection node
- `MoveToEnemyTask` - Movement action node

### State Machine
- `StateMachine<T>` - Generic state machine
- `BaseState` - Base state class
- `AttackState` - Attack state implementation
- State types: IDLE, MOVE, ATTACK, SKILL, DEAD

### Combat
- `Battlefield` - Battle area management, character placement
- `BattleCharacter` - Unit with stats, position, skills
- `CharacterStats` - Stat definitions loaded from JSON
- `LevelEnemyConfig` - Enemy spawning per level

### Damage System
- `DamageEvent` - Damage event payload
- `DamageEventHolder` - Event container
- `DamageEventListenerHolder` - Listener registry
- `DamageLineRender` - Visualizes damage lines

### Movement Effects
- `MovementEffect` - Temporary movement modifiers
- `MovementEffectType` - Effect types (SPEED_BOOST, etc.)
- `MovementEffectManager` - Effect lifecycle

## Combat Flow
```
Battle Start → Spawn Enemies → Init Behavior Trees
     ↓
Update Loop: BehaviorTree.step() → State Machine → Action
     ↓
Damage Events → DamageListener → Settlement
     ↓
Battle End → Calculate Rewards → Reset
```

## Skill System
- `Skill` - Skill definition
- `SkillType` - Active/Passive skill types
- `ManaComponent` - Mana management
- `TrySkillTask` - Skill activation AI

## Key Files
- `battle/BattleUnitBlackboard.java` (400 lines) - Core blackboard
- `battle/UnitBehaviorTreeFactory.java` - Tree builder
- `sm/machine/StateMachine.java` - State machine implementation
- `model/Battlefield.java` (200 lines) - Battlefield management
- `model/BattleCharacter.java` (400 lines) - Unit model
