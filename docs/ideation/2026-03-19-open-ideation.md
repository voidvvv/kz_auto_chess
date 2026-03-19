---
date: 2026-03-19
topic: open-ideation
focus: null
---

# Ideation: KzAutoChess Improvements

## Codebase Context

**Project Shape:**
- LibGDX 1.14.0 auto-chess game (JDK 17+) with Ashley ECS, Box2D, FreeType, gdx-ai
- Standard LibGDX layout: `core/` (game logic), `lwjgl3/` (desktop backend), `assets/`
- Strict model/updater/manager/render architectural separation enforced

**Notable Patterns:**
- Blackboard pattern for AI context aggregation (`BattleUnitBlackboard`, `PlayerLifeBlackboard`)
- Event-driven architecture via `GameEventSystem`
- Behavior trees + state machines for AI
- Key systems: card shop, battle, synergies, particle effects

**Pain Points:**
1. Windows-only font path (`C:\Windows\Fonts\msyh.ttc`) - fails on macOS/Linux
2. No test infrastructure - TDD requirements unmet (80% coverage rule)
3. 7 pending P1-P3 TODOs (null pointer bugs, overdraw issues)
4. README checklist shows incomplete features
5. Uncommitted changes in `GameUIManager.java`

**Past Learnings:**
- Critical patterns: model/updater/manager/render separation, Blackboard, Event System
- Common bugs: null check inversion, ShapeRenderer overdraw
- Manager lifecycle: onEnter/onExit/pause/resume pattern required
- Rendering: proper flush between SpriteBatch and ShapeRenderer

## Ranked Ideas

### 1. Fix P1 Critical Bugs (Null Check Inversion + ManaBarRenderer Overdraw)
**Description:** Fix the 4 inverted null checks in `BattleUnitBlackboard.java` (lines 181, 196, 371, 378) where `!= null` should be `== null`, and fix `ManaBarRenderer` to use `rectLine()` instead of `rect()` for borders.

**Rationale:** These bugs completely break mana system and make mana bars invisible. 5-minute fixes with massive immediate impact. The null check inversion is a "mental inversion" bug that compiles but causes silent failures. The overdraw bug occurs because `ShapeRenderer.rect()` draws filled shapes, not borders.

**Downsides:** None - pure bug fixes with no tradeoffs.

**Evidence:**
- `BattleUnitBlackboard.java` has 4 instances of inverted null checks following pattern: `if (mana != null) return;` should be `if (mana == null) return;`
- `ManaBarRenderer.java` draws border AFTER fill using `rect()`, which overwrites content instead of creating a border

**Confidence:** 100%
**Complexity:** Low
**Status:** Completed (2026-03-19 - bugs were already fixed in codebase)

---

### 2. Player HP and Round Damage System
**Description:** Implement player health that decreases when surviving enemy units deal damage at round end. Add win/lose conditions when HP reaches 0.

**Rationale:** Critical missing feature. Auto-chess requires player health and loss conditions. Currently players can't actually lose the game. Essential for complete game loop.

**Downsides:** Requires new UI for HP display, damage calculation logic based on surviving enemy units.

**Evidence:** `PlayerLifeBlackboard` exists but no HP loss on round defeat. `BattleManager` tracks round results but no damage to player.

**Confidence:** 95%
**Complexity:** Medium
**Status:** Completed (2026-03-19 - already fully implemented in codebase)

---

### 3. Card Pool Depletion with Shared Pool
**Description:** Track which cards have been drawn from a finite shared pool. When cards are sold or units die, return them to the pool. Create strategic scarcity.

**Rationale:** Core auto-chess mechanic. Currently `CardPool.getRandomCardsByLevel()` draws from infinite pool, making card counting impossible. Essential for strategic depth.

**Downsides:** Requires pool state tracking, integration with buy/sell flow.

**Evidence:** `CardPool.java` creates cards on-demand with no quantity tracking. No 'pool state' exists to track which cards have been drawn.

**Confidence:** 90%
**Complexity:** Medium
**Status:** Explored (brainstormed 2026-03-19)

---

### 4. Active Skill System with Mana Mechanics
**Description:** Complete the mana accumulation system so units cast skills during battle. Connect existing `Skill<T>` interface and `SkillType` enum to actual combat flow.

**Rationale:** Core gameplay feature. The code has `Skill` interface, `SkillType` enum, but skills never trigger. Battles lack tactical depth and visual spectacle. Partial implementation exists - needs completion.

**Downsides:** Requires visual feedback for skill activation, balancing skill power and mana costs.

**Evidence:** `Skill<T>` interface exists but is never called. `BattleCharacter` has no mana field in current implementation. `BattleManager.updateBattle()` only processes basic attacks.

**Confidence:** 85%
**Complexity:** Medium
**Status:** Partially implemented

---

### 5. AI Opponent with Strategic Card Selection
**Description:** Create AI that buys cards from the shared pool, builds synergies, and positions units strategically. Replace static `LevelEnemyConfig` with dynamic AI drafting.

**Rationale:** Essential for single-player. Currently only preset enemy configurations exist. Dynamic AI that drafts creates meaningful gameplay and enables filling multiplayer lobbies.

**Downsides:** Complex AI decision-making, requires synergy evaluation logic and economy management.

**Evidence:** `LevelEnemyConfig` provides static enemy spawns. No AI drafting logic exists. Game is strictly PvE with scripted encounters.

**Confidence:** 75%
**Complexity:** High
**Status:** Unexplored

---

### 6. Round Timer with Preparation Phase
**Description:** Add countdown timer during placement phase that auto-starts battle. Include visual countdown and audio cues.

**Rationale:** Improves game flow. All successful auto-chess games have time pressure during drafting. Currently game waits indefinitely for "Start Battle" button.

**Downsides:** Requires timer UI component, auto-battle trigger logic.

**Evidence:** `GamePhase.PLACEMENT` has no timer. `GameUIManager` shows startBattleButton with no time constraint.

**Confidence:** 90%
**Complexity:** Low
**Status:** Unexplored

---

### 7. Minimal Test Infrastructure
**Description:** Set up headless LibGDX test backend. Start with unit tests for `BattleUnitBlackboard` mana logic. Target 40% coverage first, not 80%.

**Rationale:** Foundation for future work. Only 4 test files exist for 142 production files. Can't refactor safely without tests.

**Downsides:** Setup effort, LibGDX testing has learning curve.

**Evidence:** Only `PlayerEconomyTest.java`, `CardUpgradeTest.java`, `BattleCharacterRenderingTest.java` exist. No tests for SynergyManager, CardUpgradeLogic, CollisionDetector.

**Confidence:** 80%
**Complexity:** Medium
**Status:** Unexplored

## Rejection Summary

| # | Idea | Reason Rejected |
|---|------|-----------------|
| 1 | Cross-Platform Font Bundling | Important but not urgent; current fallback works on macOS |
| 2 | ShapeRenderer Batch Consolidation | Premature optimization; no performance problems reported |
| 3 | Null-Safe Model Access Layer | Over-engineering; direct null check fixes are sufficient |
| 4 | Declarative Card Data Pipeline | Developer convenience; current JSON works |
| 5 | Event-Driven Debug Overlay | Nice-to-have; core loop incomplete |
| 6 | Model Immutability Migration | Large refactor; core loop incomplete |
| 7 | Responsive UI Layout | Premature; game targets desktop only currently |
| 8 | Viewport Context Stack | Engineering exercise; manual switching works |
| 9 | Manager Lifecycle Orchestrator | Over-abstraction for current scale |
| 10 | Unified Effect/Modifier System | Can wait; current SynergyEffect works |
| 11 | Blackboard Composition Framework | Over-engineering; current pattern works |
| 12 | Event-Driven Stat Pipeline | Large refactor; stats calculate correctly now |
| 13 | Object Pool Infrastructure | Premature optimization; no GC issues reported |
| 14 | Card Attribute Schema System | Over-abstraction; 12 fields manageable |
| 15 | Command/Replay Pattern | Interesting but not essential for v1 |
| 16 | Card Rarity/Level Probability | Subset of Card Pool Depletion; merge |
| 17 | Pure Data Blackboards | Refactor; core loop incomplete |
| 18 | Data-Driven Card Upgrades | Developer convenience; current system works |
| 19 | Card Sell/Refund | Lower priority than core mechanics |
| 20 | Formation Bonuses | Nice-to-have depth; core loop incomplete |
| 21 | Game Progression Persistence | Can wait until core loop complete |
| 22 | Buff/Debuff Visual System | Polish; active skills more important |
| 23 | Comprehensive Status Effect Framework | Future feature; core loop incomplete |
| 24 | Reactive Combat Engine | Large refactor; core loop incomplete |
| 25 | Deterministic Game Core | Interesting but not essential for v1 |

## Session Log
- 2026-03-19: Initial ideation — 31 candidates generated, 7 survived adversarial filtering
- 2026-03-19: Brainstorm for idea #1 (Fix P1 Critical Bugs) - Discovered bugs already fixed in codebase, updated TODO status to resolved
- 2026-03-19: Brainstorm for idea #2 (Player HP) - Already fully implemented in codebase
- 2026-03-19: Brainstorm for idea #3 (Card Pool Depletion) - Requirements doc created