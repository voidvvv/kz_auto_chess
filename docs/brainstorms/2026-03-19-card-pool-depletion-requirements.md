---
date: 2026-03-19
topic: card-pool-depletion
---

# Card Pool Depletion with Shared Pool

## Problem Frame

In auto-chess games, strategic card counting is essential. Currently, `CardPool.getRandomCardsByLevel()` draws from an infinite pool - buying a card doesn't affect future availability. This eliminates a core strategic element: knowing which cards are taken and adapting strategy accordingly.

Players cannot:
- Count cards to predict what's available
- Deny opponents specific cards by buying them first
- Adapt strategy when preferred cards are depleted

## Requirements

### R1. Shared Pool Architecture
- R1.1 The card pool SHALL be a singleton shared across all players (human and AI)
- R1.2 Each card ID SHALL have a limited number of copies based on tier:
  - Tier 1: 15 copies per card
  - Tier 2: 12 copies per card
  - Tier 3: 9 copies per card
  - Tier 4: 6 copies per card
  - Tier 5: 3 copies per card
- R1.3 The pool SHALL track how many copies of each card are currently available

### R2. Card Depletion
- R2.1 When a card is purchased, one copy SHALL be removed from the shared pool
- R2.2 Cards SHALL NOT appear in shop refresh if all copies are depleted
- R2.3 Shop refresh SHALL only show cards with at least 1 copy remaining

### R3. Card Return on Sell
- R3.1 When a player sells a card from their deck, one copy SHALL return to the shared pool
- R3.2 Sold cards SHALL immediately become available for future shop refreshes

### R4. UI Feedback
- R4.1 The shop UI SHALL display remaining copy count for each card (e.g., "3/9")
- R4.2 Depleted cards (0 remaining) SHALL NOT appear in shop

## Success Criteria

- Player can see how many copies of each card remain in the pool
- Buying a card reduces its availability for future refreshes
- Selling a card makes it available again
- Multiple players compete for limited card copies
- Strategic card counting becomes possible

## Scope Boundaries

- NOT included: Cards returning to pool on unit death (future consideration)
- NOT included: Cards returning to pool on round end (future consideration)
- NOT included: Pool reset between games (handled by existing game reset)

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Shared pool vs separate pools | Creates strategic competition; standard in auto-chess genre |
| Tier-based copy limits | Higher-tier cards are rarer, creating value differentiation |
| Sell-only return | Simpler initial implementation; death-return can be added later |
| Show remaining count | Enables strategic play without overwhelming UI |

## Dependencies / Assumptions

- Assumes single-game session (pool resets when new game starts)
- Assumes AI will also draw from shared pool (when AI opponent is implemented)
- Card ID is the unique identifier for tracking copies

## Outstanding Questions

### Deferred to Planning
- [Affects R1.1][Technical] Where should the shared pool singleton live? (Game class, static, or dependency injection?)
- [Affects R2.1][Technical] How to integrate depletion with existing `CardShop.buyCard()` flow?
- [Affects R3.1][Technical] Where is the sell functionality currently implemented?
- [Affects R4.1][Technical] How is the shop UI currently rendered? What changes needed for count display?

## Next Steps

→ `/ce:plan` for structured implementation planning
