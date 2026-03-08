package com.voidvvv.autochess.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PlayerEconomy calculation behavior.
 * Tests capture current behavior before refactoring to ensure regression prevention.
 */
public class PlayerEconomyTest {

    private PlayerEconomy economy;

    @BeforeEach
    void setUp() {
        economy = new PlayerEconomy();
    }

    @Test
    void testInitialEconomyState() {
        assertEquals(10, economy.getGold(), "Initial gold should be 10");
        assertEquals(0, economy.getInterest(), "Initial interest should be 0");
        assertEquals(0, economy.getWinStreak(), "Initial win streak should be 0");
        assertEquals(0, economy.getLoseStreak(), "Initial lose streak should be 0");
        assertEquals(1, economy.getPlayerLevel(), "Initial level should be 1");
        assertEquals(0, economy.getExperience(), "Initial experience should be 0");
    }

    @Test
    void testAddGoldIncreasesGold() {
        economy.addGold(5);
        assertEquals(15, economy.getGold(), "Gold should increase to 15");
    }

    @Test
    void testAddGoldUpdatesInterest() {
        economy.addGold(10); // Gold becomes 20, interest should be 2
        assertEquals(2, economy.getInterest(), "Interest should be 2 for 20 gold");
    }

    @Test
    void testAddGoldCapsInterestAtFive() {
        economy.addGold(50); // Gold becomes 60, interest capped at 5
        assertEquals(5, economy.getInterest(), "Interest should be capped at 5");
    }

    @Test
    void testSpendGoldDecreasesGold() {
        assertTrue(economy.spendGold(3), "Spending 3 gold should succeed");
        assertEquals(7, economy.getGold(), "Gold should decrease to 7");
    }

    @Test
    void testSpendGoldFailsWithInsufficientGold() {
        assertFalse(economy.spendGold(15), "Spending 15 gold should fail");
        assertEquals(10, economy.getGold(), "Gold should remain unchanged");
    }

    @Test
    void testSpendGoldUpdatesInterest() {
        economy.spendGold(5); // Gold becomes 5, interest should be 0
        assertEquals(0, economy.getInterest(), "Interest should be 0 for 5 gold");
    }

    @Test
    void testTryLevelUpWithInsufficientExperience() {
        assertFalse(economy.tryLevelUp(), "Cannot level up with 0 experience");
        assertEquals(1, economy.getPlayerLevel(), "Level should remain 1");
    }

    @Test
    void testTryLevelUpWithSufficientExperience() {
        // addExperience automatically calls tryLevelUp, so we need to test directly
        // Manually set experience without triggering auto level up
        economy = new PlayerEconomy();
        economy.addExperience(4); // This triggers level up automatically
        assertEquals(2, economy.getPlayerLevel(), "Level should be 2 after adding 4 EXP");
    }

    @Test
    void testTryLevelUpConsumesExperience() {
        economy.addExperience(4);
        economy.tryLevelUp();
        assertEquals(0, economy.getExperience(), "Experience should be consumed");
    }

    @Test
    void testTryLevelUpFailsAtMaxLevel() {
        economy = new PlayerEconomy(100, 10); // Already at max level
        economy.addExperience(10);
        assertFalse(economy.tryLevelUp(), "Cannot level up beyond 10");
    }

    @Test
    void testTryLevelUpFailsWithoutGold() {
        economy.addExperience(4);
        economy.spendGold(8); // Spend all but 2 gold
        assertFalse(economy.tryLevelUp(), "Cannot level up without 4 gold");
    }

    @Test
    void testTryLevelUpRewardsGold() {
        // addExperience automatically triggers level up and gives gold reward
        int beforeGold = economy.getGold(); // 10 initial
        economy.addExperience(4); // Level up, adds 1 gold reward
        assertEquals(beforeGold + 1, economy.getGold(), "Should reward 1 gold for leveling up");
    }

    @Test
    void testAddExperienceTriggersLevelUp() {
        economy.addExperience(10); // Enough for multiple level ups
        assertTrue(economy.getPlayerLevel() > 1, "Should level up with sufficient experience");
    }

    @Test
    void testAddExperienceMultipleLevelUps() {
        economy.addExperience(30); // Enough for multiple level ups
        // Level 1->2: 4 EXP, remaining 26
        // Level 2->3: 6 EXP, remaining 20
        // Level 3->4: 8 EXP, remaining 12
        // Level 4->5: 10 EXP, remaining 2
        assertEquals(5, economy.getPlayerLevel(), "Should reach level 5");
    }

    @Test
    void testEndRoundWithWin() {
        economy.endRound(true);
        assertTrue(economy.getWinStreak() > 0, "Win streak should increase");
        assertEquals(0, economy.getLoseStreak(), "Lose streak should be reset");
    }

    @Test
    void testEndRoundWithLoss() {
        economy.endRound(false);
        assertTrue(economy.getLoseStreak() > 0, "Lose streak should increase");
        assertEquals(0, economy.getWinStreak(), "Win streak should be reset");
    }

    @Test
    void testEndRoundAddsBaseIncome() {
        // Start with a new economy to have clean state
        PlayerEconomy freshEconomy = new PlayerEconomy();
        int beforeGold = freshEconomy.getGold(); // 10
        freshEconomy.endRound(true); // +5 base, gold becomes 15, interest becomes 1, final gold = 15
        // Note: The interest for initial gold (10 -> 1) is calculated AFTER the base income is added,
        // not before. So the interest is earned on the final gold (15), not the initial gold (10).
        // This is the expected behavior based on the implementation.
        assertEquals(15, freshEconomy.getGold(), "Should have gold=15 (10 initial + 5 base income)");
        assertEquals(1, freshEconomy.getInterest(), "Interest should be 1 for 15 gold");
    }

    @Test
    void testEndRoundAddsInterest() {
        economy.addGold(10); // Gold = 20, interest = 2
        int beforeGold = economy.getGold();
        economy.endRound(true);
        assertEquals(beforeGold + 5 + 2, economy.getGold(), "Should add base income + interest");
    }

    @Test
    void testEndRoundWinStreakBonus() {
        economy.endRound(true);
        economy.endRound(true); // 2 wins in a row
        int beforeGold = economy.getGold();
        economy.endRound(true);
        // Should have streak bonus (3 wins)
        assertTrue(economy.getGold() > beforeGold + 5, "Should include streak bonus");
    }

    @Test
    void testEndRoundLoseStreakCompensation() {
        economy.endRound(false);
        economy.endRound(false); // 2 losses in a row
        int beforeGold = economy.getGold();
        economy.endRound(false);
        // Should have compensation (3 losses)
        assertTrue(economy.getGold() > beforeGold + 5, "Should include compensation");
    }

    @Test
    void testEndRoundAddsExperience() {
        economy.endRound(true);
        assertTrue(economy.getExperience() > 0, "Should add experience for winning");
    }

    @Test
    void testEndRoundAddsBaseExperienceEvenOnLoss() {
        economy.endRound(false);
        assertEquals(1, economy.getExperience(), "Should add 1 base experience even on loss");
    }

    @Test
    void testResetStreaks() {
        economy.endRound(true);
        economy.endRound(true);
        assertTrue(economy.getWinStreak() > 0, "Should have win streak");

        economy.resetStreaks();
        assertEquals(0, economy.getWinStreak(), "Win streak should be reset");
        assertEquals(0, economy.getLoseStreak(), "Lose streak should be reset");
    }

    @Test
    void testGetRemainingExperience() {
        economy.addExperience(2);
        assertEquals(2, economy.getRemainingExperience(), "Should need 2 more EXP");
    }

    @Test
    void testGetExperiencePercentage() {
        economy.addExperience(2);
        assertEquals(50f, economy.getExperiencePercentage(), 0.01f, "Should be 50% progress");
    }

    @Test
    void testGetRoundIncomePreview() {
        economy.addGold(10); // Gold = 20, interest = 2
        int income = economy.getRoundIncomePreview(true);
        assertEquals(7, income, "Should be base income (5) + interest (2)");
    }

    @Test
    void testGetRoundIncomePreviewWithWinStreak() {
        economy.endRound(true);
        economy.endRound(true); // 2 wins
        int income = economy.getRoundIncomePreview(true);
        assertTrue(income > 7, "Should include win streak bonus");
    }

    @Test
    void testConstructorWithStartingGold() {
        PlayerEconomy custom = new PlayerEconomy(50, 3);
        assertEquals(50, custom.getGold(), "Should start with custom gold");
        assertEquals(3, custom.getPlayerLevel(), "Should start at custom level");
    }

    @Test
    void testNegativeGoldIsClamped() {
        PlayerEconomy custom = new PlayerEconomy(-10, 1);
        assertEquals(0, custom.getGold(), "Negative gold should be clamped to 0");
    }

    @Test
    void testLevelAbove10IsClamped() {
        PlayerEconomy custom = new PlayerEconomy(100, 15);
        assertEquals(10, custom.getPlayerLevel(), "Level above 10 should be clamped");
    }
}
