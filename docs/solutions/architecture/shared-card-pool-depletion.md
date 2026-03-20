---
title: Implementing Shared Card Pool with Tier-Based Depletion Tracking
category: architecture
date: 2026-03-19
tags:
  - libgdx
  - auto-chess
  - card-pool-management
  - game-mechanics
  - java
problem_type: feature-implementation
component: card-management-system
symptoms: Card pool system lacked depletion tracking, allowing infinite card availability and removing strategic depth from the game
---

# Shared Card Pool with Tier-Based Depletion Tracking

## Problem Statement

The auto-chess game's card pool system (`CardPool.getRandomCardsByLevel()`) drew from an infinite pool - buying a card didn't affect future availability. This eliminated core auto-chess strategic elements:

- **Card counting** to predict availability
- **Denying opponents** cards by buying first
- **Adapting strategy** when preferred cards are depleted

## Root Cause

Auto-chess games require shared, limited card pools where players compete for resources. Without depletion tracking, every player had access to unlimited copies of every card, removing the strategic competition that defines the genre.

## Solution

### Architecture Overview

Implemented a `SharedCardPool` singleton following the existing `PlayerLifeBlackboard` pattern:

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

### Implementation Files

| File | Change |
|------|--------|
| `SharedCardPool.java` | New model with copy tracking |
| `KzAutoChess.java` | Added SharedCardPool singleton |
| `CardManager.java` | Pool decrement/increment on buy/sell |
| `CardPool.java` | Pool-aware card filtering |
| `GameScreen.java` | Wired SharedCardPool to components |
| `CardRenderer.java` | Pool count badge display |
| `GameUIManager.java` | Pass pool counts to renderer |

### Key Code Examples

#### 1. SharedCardPool Model

```java
public class SharedCardPool {
    private static final int[] TIER_MAX_COPIES = {0, 15, 12, 9, 6, 3};

    private final Map<Integer, Integer> availableCopies;
    private final Map<Integer, Integer> maxCopies;

    public void initialize(CardPool cardPool) {
        availableCopies.clear();
        maxCopies.clear();

        for (Card card : cardPool.getAllCards()) {
            int cardId = card.getId();
            int maxCount = getMaxCopiesForTier(card.getTier());
            maxCopies.put(cardId, maxCount);
            availableCopies.put(cardId, maxCount);
        }
    }

    public boolean decrementCopies(int cardId) {
        int current = availableCopies.getOrDefault(cardId, 0);
        if (current <= 0) return false;
        availableCopies.put(cardId, current - 1);
        return true;
    }

    public boolean incrementCopies(int cardId) {
        int current = availableCopies.getOrDefault(cardId, 0);
        int max = maxCopies.getOrDefault(cardId, 0);
        if (current >= max) return false;
        availableCopies.put(cardId, current + 1);
        return true;
    }

    public boolean isCardAvailable(int cardId) {
        return getRemainingCopies(cardId) > 0;
    }
}
```

#### 2. CardManager Integration

```java
public class CardTransactionManager {
    private final SharedCardPool sharedCardPool;

    public boolean buyCard(Card card) {
        if (cardShop.buyCard(card)) {
            if (sharedCardPool != null) {
                sharedCardPool.decrementCopies(card.getId());
            }
            playerDeck.addCard(card);
            eventSystem.postEvent(new CardBuyEvent(card, card.getCost()));
            return true;
        }
        return false;
    }

    public boolean sellCard(Card card, int goldReceived) {
        if (playerDeck.getCardCount(card) > 0) {
            playerDeck.removeCard(card);
            if (sharedCardPool != null) {
                sharedCardPool.incrementCopies(card.getId());
            }
            eventSystem.postEvent(new CardSellEvent(card, goldReceived));
            return true;
        }
        return false;
    }
}
```

#### 3. Pool-Aware Card Filtering

```java
public List<Card> getAvailableCardsByTier(int tier) {
    List<Card> result = new ArrayList<>();
    for (Card card : allCards) {
        if (card.getTier() == tier) {
            if (sharedCardPool == null || sharedCardPool.isCardAvailable(card.getId())) {
                result.add(card);
            }
        }
    }
    return result;
}

public List<Card> getRandomCardsByLevel(int count, int playerLevel) {
    List<Card> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
        int tier = calculateTierByLevel(playerLevel);
        List<Card> tierCards = getAvailableCardsByTier(tier);
        if (!tierCards.isEmpty()) {
            result.add(tierCards.get(random.nextInt(tierCards.size())));
        } else {
            Card fallback = getFallbackCard(tier);
            if (fallback != null) result.add(fallback);
        }
    }
    return result;
}
```

#### 4. UI Pool Count Display

```java
private static void renderPoolCountBadge(ShapeRenderer shapeRenderer, SpriteBatch batch,
                                        float cardX, float cardY, float cardWidth, float cardHeight,
                                        int poolRemaining, int poolMax) {
    float badgeWidth = 50, badgeHeight = 16;
    float badgeX = cardX + (cardWidth - badgeWidth) / 2;
    float badgeY = cardY + 3;

    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    if (poolRemaining == 0) {
        shapeRenderer.setColor(0.6f, 0.2f, 0.2f, 0.9f); // Red - depleted
    } else if (poolRemaining <= poolMax / 3) {
        shapeRenderer.setColor(0.8f, 0.5f, 0.2f, 0.9f); // Orange - sparse
    } else {
        shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 0.9f); // Green - plentiful
    }
    shapeRenderer.rect(badgeX, badgeY, badgeWidth, badgeHeight);
    shapeRenderer.end();

    // Draw "X/Y" text
    batch.begin();
    cardFont.setColor(Color.WHITE);
    String poolText = poolRemaining + "/" + poolMax;
    GlyphLayout poolLayout = new GlyphLayout(cardFont, poolText);
    cardFont.draw(batch, poolLayout,
        badgeX + (badgeWidth - poolLayout.width) / 2,
        badgeY + (badgeHeight + poolLayout.height) / 2);
    batch.end();
}
```

## Verification

### Build & Test
- `gradle compileJava` - Passed
- `gradle test` - All existing tests pass

### Functional Verification
- [x] Pool initializes with correct tier-based counts
- [x] Buying decrements pool count
- [x] Selling increments pool count
- [x] Depleted cards don't appear in shop refresh
- [x] UI displays "X/Y" format with color coding

## Prevention Strategies

### 1. Pool Integrity Protection
The implementation includes validation:
- `decrementCopies()` returns `false` if count is 0
- `incrementCopies()` returns `false` if at max
- Unknown cards return 0 copies

### 2. Empty Tier Handling
`getFallbackCard()` searches adjacent tiers when a tier is depleted, preventing crashes.

### 3. UI Data Freshness
Pool counts are queried on each render cycle - no caching that could cause stale data.

### 4. Initialization Contract
Pool must be initialized via `sharedCardPool.initialize(cardPool)` before first use.

## Test Cases to Implement

1. **Normal Flow**
   - Buy card → count decreases by 1
   - Sell card → count increases by 1
   - Refresh shop → only shows cards with count > 0

2. **Boundary Conditions**
   - Buy last copy → count = 0, card removed from shop
   - Buy when count = 0 → operation fails gracefully
   - Sell when at max → operation fails gracefully

3. **Tier Depletion**
   - Deplete entire tier → fallback to adjacent tier
   - Deplete all tiers → no crash, empty shop

4. **Data Integrity**
   - Negative counts → impossible by design
   - Counts exceeding max → impossible by design

## Related Documentation

- **Origin Requirements**: `docs/brainstorms/2026-03-19-card-pool-depletion-requirements.md`
- **Implementation Plan**: `docs/plans/2026-03-19-001-feat-card-pool-depletion-plan.md`
- **Architecture Patterns**: `docs/CODEMAPS/architecture.md` (Model-Manager-Render separation)
- **Data Models**: `docs/CODEMAPS/data-models.md` (Card System)

## Future Considerations

- **Configuration externalization**: Move tier limits to config file
- **Multiplayer synchronization**: Would need atomic operations or server-authoritative pool
- **Cards returning on unit death**: Currently out of scope
- **Pool persistence**: Currently session-scoped, could add save/load
- **AI pool consumption**: When AI implemented, will share same pool

---

*Documented via Compound Engineering workflow - 2026-03-19*
