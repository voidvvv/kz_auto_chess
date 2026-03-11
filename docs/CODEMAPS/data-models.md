# Data Models

<!-- Generated: 2026-03-12 | Files scanned: ~25 | Token estimate: ~500 -->

## Core Models

### Card System
- `Card` - Card definition (id, name, cost, synergy, skillId)
- `CardPool` - All available cards
- `CardShop` - Current shop cards, refresh logic
- `PlayerDeck` - Owned cards, card counting
- `CardUpgradeLogic` - 3-card merge upgrade logic

### Economy
- `PlayerEconomy` - Gold, interest, player level
- `EconomyCalculator` - Round end calculations
- Income formula: base + interest (5 gold per 10 gold)

### Synergies
- `SynergyType` - Synergy categories (WARRIOR, MAGE, etc.)
- `SynergyEffect` - Effect definitions
- `SynergyManager` - Active synergy tracking, effect application

### Game State
- `GamePhase` - PLACEMENT, BATTLE, RESULT
- `LevelEnemyConfig` - Enemy configurations per level

## Battle Models

### Character
- `BattleCharacter` - Unit with position, stats, state
  - Components: ManaComponent, MoveComponent
  - States: enterBattle(), exitBattle(), reset()
  - Battle tracking: isDead(), isEnemy()

### Projectile
- `Projectile` - Flying attack
- `ProjectileManager` - Lifecycle management
- `ProjectileUpdater` - Movement updates

### Movement
- `MoveComponent` - Movement state
- `MovementEffect` - Temporary modifiers
- `MovementEffectType` - SPEED_BOOST, SLOW, etc.

### Visual Effects
- `Particle` - Visual particle
- `ParticleSystem` - Particle container
- `ParticleSpawner` - Particle creation
- `ParticleSystemUpdater` - Animation updates

## Key Files
- `model/Card.java` - Card definition
- `model/PlayerDeck.java` (130 lines) - Deck management
- `model/PlayerEconomy.java` (200 lines) - Economy logic
- `model/BattleCharacter.java` (400 lines) - Battle unit
- `model/Projectile.java` (130 lines) - Projectile
- `logic/SynergyManager.java` - Synergy effects
