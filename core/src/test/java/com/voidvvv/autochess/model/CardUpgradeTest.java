package com.voidvvv.autochess.model;

import com.voidvvv.autochess.logic.CardUpgradeLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Card upgrade behavior.
 * Tests capture current behavior before refactoring to ensure regression prevention.
 */
public class CardUpgradeTest {

    private PlayerDeck deck;
    private Card cardLevel1;
    private Card cardLevel2;

    @BeforeEach
    void setUp() {
        deck = new PlayerDeck();

        // Create level 1 card
        cardLevel1 = new Card(1, "TestCard", "Test description", 3, 1, Card.CardType.WARRIOR);
        cardLevel1.setStarLevel(1);
        cardLevel1.setBaseCardId(1);

        // Create level 2 card
        cardLevel2 = new Card(1001, "TestCard★", "Test description", 4, 1, Card.CardType.WARRIOR);
        cardLevel2.setStarLevel(2);
        cardLevel2.setBaseCardId(1);
    }

    @Test
    void testCardCanUpgradeLevel1() {
        assertTrue(CardUpgradeLogic.canUpgrade(cardLevel1),
            "Level 1 card should be upgradeable");
    }

    @Test
    void testCardCanUpgradeLevel2() {
        assertTrue(CardUpgradeLogic.canUpgrade(cardLevel2),
            "Level 2 card should be upgradeable");
    }

    @Test
    void testCardCannotUpgradeLevel3() {
        Card level3 = new Card(2001, "TestCard★★", "Test description", 5, 1, Card.CardType.WARRIOR);
        level3.setStarLevel(3);
        level3.setBaseCardId(1);

        assertFalse(CardUpgradeLogic.canUpgrade(level3),
            "Level 3 card should NOT be upgradeable");
    }

    @Test
    void testCreateUpgradedCardFromLevel1() {
        Card upgraded = CardUpgradeLogic.createUpgradedCard(cardLevel1);

        assertNotNull(upgraded, "Upgraded card should not be null");
        assertEquals(1001, upgraded.getId(), "Upgraded card ID should be 1001");
        assertEquals(2, upgraded.getStarLevel(), "Upgraded card should be level 2");
        assertEquals("TestCard★", upgraded.getName(), "Upgraded card name should have star");
        assertEquals(4, upgraded.getCost(), "Upgraded card cost should increase by 1");
        assertEquals(1, upgraded.getBaseCardId(), "Base card ID should remain the same");
    }

    @Test
    void testCreateUpgradedCardFromLevel2() {
        Card upgraded = CardUpgradeLogic.createUpgradedCard(cardLevel2);

        assertNotNull(upgraded, "Upgraded card should not be null");
        assertEquals(2001, upgraded.getId(), "Upgraded card ID should be 2001");
        assertEquals(3, upgraded.getStarLevel(), "Upgraded card should be level 3");
        assertEquals("TestCard★★", upgraded.getName(), "Upgraded card name should have two stars");
    }

    @Test
    void testCreateUpgradedCardFromLevel3ReturnsNull() {
        Card level3 = new Card(2001, "TestCard★★", "Test description", 5, 1, Card.CardType.WARRIOR);
        level3.setStarLevel(3);
        level3.setBaseCardId(1);

        Card upgraded = CardUpgradeLogic.createUpgradedCard(level3);
        assertNull(upgraded, "Upgrading level 3 card should return null");
    }

    @Test
    void testDeckCanUpgradeCardWithInsufficientCopies() {
        // Add only 2 copies instead of 3
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        assertFalse(CardUpgradeLogic.canUpgradeCard(deck, cardLevel1),
            "Cannot upgrade with only 2 copies");
    }

    @Test
    void testDeckCanUpgradeCardWithExactCopies() {
        // Add exactly 3 copies
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        assertTrue(CardUpgradeLogic.canUpgradeCard(deck, cardLevel1),
            "Can upgrade with exactly 3 copies");
    }

    @Test
    void testDeckCanUpgradeCardWithExtraCopies() {
        // Add 6 copies (should still be upgradeable)
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        assertTrue(CardUpgradeLogic.canUpgradeCard(deck, cardLevel1),
            "Can upgrade with more than 3 copies");
    }

    @Test
    void testDeckCannotUpgradeDifferentCard() {
        Card differentCard = new Card(2, "OtherCard", "Test", 3, 1, Card.CardType.MAGE);
        differentCard.setStarLevel(1);
        differentCard.setBaseCardId(2);

        // Add 3 copies of cardLevel1
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        assertFalse(CardUpgradeLogic.canUpgradeCard(deck, differentCard),
            "Cannot upgrade a different card");
    }

    @Test
    void testDeckUpgradeCardRemovesThreeCopies() {
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        int beforeCount = deck.getTotalCardCount();
        assertEquals(3, beforeCount, "Should have 3 cards before upgrade");

        Card upgraded = CardUpgradeLogic.upgradeCard(deck, cardLevel1);

        assertNotNull(upgraded, "Upgrade should succeed");
        assertEquals(1, deck.getTotalCardCount(), "Should have 1 card after upgrade");
        assertEquals(2, upgraded.getStarLevel(), "Upgraded card should be level 2");
    }

    @Test
    void testDeckUpgradeCardReturnsNullWhenNotUpgradable() {
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1); // Only 2 copies

        Card upgraded = CardUpgradeLogic.upgradeCard(deck, cardLevel1);
        assertNull(upgraded, "Upgrade should fail with insufficient copies");
    }

    @Test
    void testDeckGetUpgradableCardsReturnsEmpty() {
        List<Card> upgradable = CardUpgradeLogic.getUpgradableCards(deck);
        assertTrue(upgradable.isEmpty(), "Empty deck should have no upgradable cards");
    }

    @Test
    void testDeckGetUpgradableCardsWithUpgradeableCard() {
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        List<Card> upgradable = CardUpgradeLogic.getUpgradableCards(deck);
        assertEquals(1, upgradable.size(), "Should find 1 upgradable card");
        assertTrue(upgradable.contains(cardLevel1), "Should contain the upgradeable card");
    }

    @Test
    void testDeckGetUpgradableCardsWithMultipleUpgradeable() {
        Card anotherCard = new Card(2, "OtherCard", "Test", 3, 1, Card.CardType.MAGE);
        anotherCard.setStarLevel(1);
        anotherCard.setBaseCardId(2);

        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(anotherCard);
        deck.addCard(anotherCard);
        deck.addCard(anotherCard);

        List<Card> upgradable = CardUpgradeLogic.getUpgradableCards(deck);
        assertEquals(2, upgradable.size(), "Should find 2 upgradable cards");
    }

    @Test
    void testDeckGetUpgradableCardsDoesNotDuplicate() {
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);
        deck.addCard(cardLevel1);

        List<Card> upgradable = CardUpgradeLogic.getUpgradableCards(deck);
        assertEquals(1, upgradable.size(), "Should not have duplicates in upgradable list");
    }
}
