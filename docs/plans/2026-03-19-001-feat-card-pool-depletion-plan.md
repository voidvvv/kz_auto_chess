---
title: Card Pool Depletion with Shared Pool
type: feat
status: active
date: 2026-03-19
origin: docs/brainstorms/2026-03-19-card-pool-depletion-requirements.md
---

# Card Pool Depletion with Shared Pool

## Overview

Implement a shared card pool with depletion tracking, enabling strategic card counting. When players buy cards, copies are removed from the shared pool; when they sell, copies return. This creates strategic competition for limited resources.

## Problem Statement

Currently, `CardPool.getRandomCardsByLevel()` draws from an infinite pool - buying a card doesn't affect future availability. This eliminates core auto-chess strategic elements:
- Card counting to predict availability
- Denying opponents cards by buying first
- Adapting strategy when preferred cards are depleted

## Proposed Solution

### Architecture

Following the `PlayerLifeBlackboard` pattern (see origin: docs/brainstorms), store `SharedCardPool` in `KzAutoChess` game class:

```
KzAutoChess (Game)
  └── SharedCardPool (singleton for game session)
        └── availableCopies: Map<CardId, Count>
        └── maxCopies: Map<CardId, MaxByTier>
```

### Tier-Based Copy Limits

| Tier | Copies Per Card | Rationale |
|------|-----------------|-----------|
| 1 | 15 | Common cards, abundant |
| 2 | 12 | Uncommon, moderately available |
| 3 | 9 | Rare, limited |
| 4 | 6 | Epic, scarce |
| 5 | 3 | Legendary, very rare |

### Data Flow

```
Buy Card:
  CardManager.buyCard() → SharedCardPool.decrementCopies(cardId) → CardPool.getAvailableCards()

Sell Card:
  CardManager.sellCard() → SharedCardPool.incrementCopies(cardId) → Pool updated

Shop Refresh:
  CardShop.refresh() → SharedCardPool.getAvailableCardsByTier(tier) → Filter depleted cards
```

## Technical Considerations

### Architecture Pattern

Follow existing `PlayerLifeBlackboard` pattern for global state:
- Store in `KzAutoChess` (main Game class)
- Access via `game.getSharedCardPool()`
- Reset when new game starts

### Integration Points

| Component | Current | Change Needed |
|-----------|---------|---------------|
| `KzAutoChess` | Holds `PlayerLifeBlackboard` | Add `SharedCardPool` |
| `CardPool` | Stateless random selection | Query `SharedCardPool` for availability |
| `CardManager` | Buy/sell without pool tracking | Call `SharedCardPool` operations |
| `CardRenderer` | No count display | Add pool count badge |
| `GameUIManager` | Renders shop cards | Pass pool count to renderer |

### Concurrency

LibGDX is single-threaded on the render thread. No synchronization needed for v1. Future multiplayer would require atomic operations.

## System-Wide Impact

### Interaction Graph

```
Player clicks shop card
  → GameInputHandler.handleMouseClick()
  → AutoChessGameMode.buyCard()
  → CardManager.buyCard()
  → SharedCardPool.decrementCopies(cardId) [NEW]
  → PlayerDeck.addCard(card)
  → CardBuyEvent posted
  → GameUIManager re-renders (count updated)
```

### State Lifecycle

- **Pool initialization**: `KzAutoChess.create()` or new game start
- **Pool persistence**: Session-scoped (resets between games)
- **Pool reset**: When returning to main menu or starting new session

## Acceptance Criteria

### Functional Requirements

- [ ] **R1.1** Shared pool singleton exists in `KzAutoChess`
- [ ] **R1.2** Tier-based copy limits implemented (T1=15, T2=12, T3=9, T4=6, T5=3)
- [ ] **R1.3** Pool tracks available copies per card ID
- [ ] **R2.1** Buying a card decrements pool count
- [ ] **R2.2** Cards with 0 copies don't appear in shop refresh
- [ ] **R2.3** Shop refresh only shows cards with 1+ copies remaining
- [ ] **R3.1** Selling a card increments pool count
- [ ] **R3.2** Sold cards immediately available for refresh
- [ ] **R4.1** Shop UI displays remaining count (e.g., "3/9")
- [ ] **R4.2** Depleted cards (0 remaining) don't appear in shop

### Quality Gates

- [ ] Unit tests for buy/sell pool operations
- [ ] Unit tests for depletion scenarios
- [ ] Manual verification of UI count display
- [ ] No negative pool counts possible
- [ ] No pool counts exceeding tier limits

## Implementation Phases

### Phase 1: Model & State (Foundation)

**Files to create/modify:**

1. **Create** `core/src/main/java/com/voidvvv/autochess/model/SharedCardPool.java`
   - Immutable model with copy tracking
   - Methods: `getRemainingCopies()`, `decrementCopies()`, `incrementCopies()`
   - Tier-based initialization

2. **Modify** `core/src/main/java/com/voidvvv/autochess/KzAutoChess.java`
   - Add `SharedCardPool` field
   - Add `getSharedCardPool()` getter
   - Initialize in `create()` or reset method

**Validation:** Pool initializes with correct counts, accessible from game instance.

### Phase 2: Integration (Core Logic)

**Files to modify:**

1. **Modify** `core/src/main/java/com/voidvvv/autochess/manage/CardManager.java`
   - `CardTransactionManager.buyCard()` → call `sharedCardPool.decrementCopies()`
   - `CardTransactionManager.sellCard()` → call `sharedCardPool.incrementCopies()`
   - Inject `SharedCardPool` via constructor

2. **Modify** `core/src/main/java/com/voidvvv/autochess/model/CardPool.java`
   - `getRandomCardsByLevel()` → filter by pool availability
   - Add `getAvailableCardsByTier()` that respects pool counts

3. **Modify** `core/src/main/java/com/voidvvv/autochess/model/CardShop.java`
   - Inject `SharedCardPool` reference
   - `refresh()` uses pool-aware card selection

4. **Modify** `core/src/main/java/com/voidvvv/autochess/screens/GameScreen.java`
   - Wire `SharedCardPool` into `CardManager`, `CardShop`, `CardPool`

**Validation:** Buying decreases count, selling increases count, depleted cards don't appear.

### Phase 3: UI (Display)

**Files to modify:**

1. **Modify** `core/src/main/java/com/voidvvv/autochess/ui/CardRenderer.java`
   - Add `poolRemainingCount` and `poolMaxCount` parameters
   - Reuse existing count badge pattern (lines 125-128)
   - Display format: "3/9" in corner of card

2. **Modify** `core/src/main/java/com/voidvvv/autochess/ui/GameUIManager.java`
   - Get `SharedCardPool` from game instance
   - Pass pool counts to `CardRenderer.render()`
   - Update `renderShopContent()` method

**Validation:** Shop cards show remaining count, count updates on buy/sell.

### Phase 4: Testing & Polish

**Tasks:**
- [ ] Write unit tests for `SharedCardPool`
- [ ] Test depletion scenario (buy all copies)
- [ ] Test return scenario (sell, then buy again)
- [ ] Test empty tier scenario
- [ ] Manual playtest full game with depletion

## Success Metrics

| Metric | Target |
|--------|--------|
| Pool count accuracy | 100% (no negative/overflow) |
| UI count display | Shows on all shop cards |
| Depletion behavior | No crashes when pool empty |
| Sell return | Immediate availability |

## Dependencies & Prerequisites

### Dependencies
- Existing `CardPool`, `CardShop`, `CardManager` classes
- Existing `CardRenderer` count badge pattern
- `PlayerLifeBlackboard` pattern as reference

### Prerequisites
- Understanding of LibGDX game lifecycle
- Understanding of event-driven architecture (existing pattern)

## Risk Analysis

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Pool count goes negative | Medium | High | Add validation in `decrementCopies()` |
| Pool count exceeds max | Low | Medium | Add validation in `incrementCopies()` |
| Empty pool crash | Low | Critical | Add empty check in `getRandomCardsByLevel()` |
| UI shows stale count | Low | Low | Ensure UI queries pool on each render |

## Future Considerations

- **Multiplayer synchronization**: Would need atomic operations or server-authoritative pool
- **Cards returning on unit death**: Currently out of scope, can be added later
- **Pool persistence**: Currently session-scoped, could add save/load
- **AI pool consumption**: When AI implemented, will share same pool
- **Configuration file for copy limits**: Currently hardcoded, could externalize

## Sources & References

### Origin

- **Origin document:** [docs/brainstorms/2026-03-19-card-pool-depletion-requirements.md](../brainstorms/2026-03-19-card-pool-depletion-requirements.md)
- **Key decisions carried forward:**
  - Shared pool across all players (origin: R1.1)
  - Tier-based copy limits T1=15, T2=12, T3=9, T4=6, T5=3 (origin: R1.2)
  - Sell-only return (origin: Key Decisions)
  - Show remaining count in UI (origin: R4.1)

### Internal References

- Global state pattern: `KzAutoChess.java:40-48` (PlayerLifeBlackboard)
- CardPool current implementation: `CardPool.java:91-107`
- CardManager buy flow: `CardManager.java:77-84`
- CardRenderer count badge: `CardRenderer.java:125-128`
- Shop rendering: `GameUIManager.java:456-502`

### Architecture Pattern

- Model-Manager-Render separation (CLAUDE.md)
- Event-driven architecture (GameEventSystem)
- Blackboard pattern (PlayerLifeBlackboard)
