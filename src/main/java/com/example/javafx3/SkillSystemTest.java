package com.example.javafx3;

import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import com.example.javafx3.ui.SkillNotificationSystem;
import com.example.javafx3.ui.SimpleSkillMenuController;

/**
 * Test class to demonstrate the comprehensive skill system functionality
 * This runs without JavaFX dependencies and shows all the implemented features
 */
public class SkillSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE SKILL SYSTEM TEST ===\n");
        
        // Initialize the skill system
        GameState gameState = new GameState();
        SkillManager skillManager = new SkillManager(gameState);
        SkillNotificationSystem notificationSystem = new SkillNotificationSystem(null);
        SimpleSkillMenuController menuController = new SimpleSkillMenuController();
        
        // Start the game
        gameState.startGame();
        System.out.println("Game started!");
        
        // Test 1: Skill acquisition through score progression
        System.out.println("\n=== TEST 1: SKILL ACQUISITION ===");
        testSkillAcquisition(gameState, skillManager, notificationSystem);
        
        // Test 2: Skill activation and conflict resolution
        System.out.println("\n=== TEST 2: SKILL ACTIVATION & CONFLICTS ===");
        testSkillActivationAndConflicts(gameState, skillManager, notificationSystem, menuController);
        
        // Test 3: Dynamic balance system
        System.out.println("\n=== TEST 3: DYNAMIC BALANCE SYSTEM ===");
        testDynamicBalance(gameState, skillManager, notificationSystem);
        
        // Test 4: Rarity distribution
        System.out.println("\n=== TEST 4: RARITY DISTRIBUTION ===");
        testRarityDistribution(gameState, skillManager, notificationSystem);
        
        // Test 5: Special skill effects
        System.out.println("\n=== TEST 5: SPECIAL SKILL EFFECTS ===");
        testSpecialSkillEffects(gameState, skillManager, notificationSystem);
        
        // Final summary
        System.out.println("\n=== FINAL SUMMARY ===");
        menuController.initializeController(gameState, skillManager, notificationSystem, () -> {});
        menuController.displaySkillStatistics();
        
        System.out.println("\n=== SKILL SYSTEM TEST COMPLETED ===");
    }
    
    private static void testSkillAcquisition(GameState gameState, SkillManager skillManager, 
                                           SkillNotificationSystem notificationSystem) {
        System.out.println("Testing skill acquisition every 60 points...");
        
        // Simulate earning skills through score progression
        for (int i = 1; i <= 5; i++) {
            int targetScore = i * 60;
            gameState.updateScore(targetScore - gameState.getScore());
            
            if (gameState.shouldEarnSkill()) {
                SkillCard newSkill = skillManager.generateRandomSkill();
                if (newSkill != null) {
                    if (skillManager.isDuplicateSkill(newSkill)) {
                        int bonusScore = skillManager.convertDuplicateToScore(newSkill);
                        gameState.updateScore(bonusScore);
                        notificationSystem.showGameNotification("Duplicate Skill", 
                            "Converted to " + bonusScore + " bonus points!", "💰");
                    } else {
                        gameState.awardSkill(newSkill);
                        notificationSystem.showSkillUnlocked(newSkill);
                        System.out.println("Earned skill: " + newSkill.getName() + " (Rarity: " + newSkill.getRarityName() + ")");
                    }
                }
            }
        }
        
        System.out.println("Skills earned: " + gameState.getSkillsEarned());
        System.out.println("Current score: " + gameState.getScore());
    }
    
    private static void testSkillActivationAndConflicts(GameState gameState, SkillManager skillManager,
                                                      SkillNotificationSystem notificationSystem,
                                                      SimpleSkillMenuController menuController) {
        System.out.println("Testing skill activation and conflict resolution...");
        
        // Initialize menu controller
        menuController.initializeController(gameState, skillManager, notificationSystem, () -> {});
        
        // Try to activate some skills
        if (gameState.getSkillsEarned() > 0) {
            System.out.println("Attempting to activate first skill...");
            menuController.activateSkill(0);
            
            if (gameState.getSkillsEarned() > 1) {
                System.out.println("Attempting to activate second skill...");
                menuController.activateSkill(1);
            }
        }
        
        // Test conflict resolution by trying to activate conflicting skills
        // First, let's manually create some conflicting skills for testing
        SkillCard bodyGrowth = new SkillCard(SkillEffect.BODY_GROWTH);
        SkillCard bodySplit = new SkillCard(SkillEffect.BODY_SPLIT);
        
        gameState.awardSkill(bodyGrowth);
        gameState.awardSkill(bodySplit);
        
        System.out.println("Testing conflict resolution with BODY_GROWTH and BODY_SPLIT...");
        gameState.activateSkill(bodyGrowth);
        
        // This should trigger conflict resolution
        SkillManager.ConflictResolution conflict = skillManager.checkSkillConflicts(bodySplit);
        if (conflict.hasConflict()) {
            System.out.println("Conflict detected: " + conflict.getMessage());
            int compensation = skillManager.resolveConflict(bodySplit, conflict.getConflictingSkill());
            System.out.println("Compensation awarded: " + compensation + " points");
        }
    }
    
    private static void testDynamicBalance(GameState gameState, SkillManager skillManager,
                                         SkillNotificationSystem notificationSystem) {
        System.out.println("Testing dynamic balance system...");
        
        // Activate multiple skills to test negative effects
        int initialActiveSkills = gameState.getActiveSkills().size();
        System.out.println("Initial active skills: " + initialActiveSkills);
        
        // Try to activate more skills to trigger balance effects
        for (SkillCard skill : gameState.getEarnedSkills()) {
            if (!skill.isActive() && skill.canActivate() && gameState.getAvailableActivationSlots() > 0) {
                gameState.activateSkill(skill);
                System.out.println("Activated: " + skill.getName());
                
                if (gameState.getActiveSkills().size() >= 3) {
                    System.out.println("WARNING: 3+ skills active - negative effects may apply!");
                    break;
                }
            }
        }
        
        System.out.println("Final active skills: " + gameState.getActiveSkills().size());
    }
    
    private static void testRarityDistribution(GameState gameState, SkillManager skillManager,
                                             SkillNotificationSystem notificationSystem) {
        System.out.println("Testing rarity distribution at different score ranges...");
        
        // Test rarity distribution at different score levels
        int[] testScores = {100, 300, 600, 1200};
        
        for (int score : testScores) {
            gameState.updateScore(score - gameState.getScore());
            System.out.println("\nScore: " + score);
            
            // Generate several skills to see distribution
            for (int i = 0; i < 3; i++) {
                SkillCard skill = skillManager.generateRandomSkill();
                if (skill != null) {
                    System.out.println("  Generated: " + skill.getName() + " (" + skill.getRarityName() + ")");
                }
            }
        }
    }
    
    private static void testSpecialSkillEffects(GameState gameState, SkillManager skillManager,
                                              SkillNotificationSystem notificationSystem) {
        System.out.println("Testing special skill effects...");
        
        // Test score frenzy
        gameState.enableScoreFrenzy();
        gameState.incrementFoodCounter();
        gameState.incrementFoodCounter();
        System.out.println("Score frenzy multiplier: " + gameState.getScoreFrenzyMultiplier());
        
        // Test time rewind system
        gameState.saveStateSnapshot("test_snapshot_data");
        System.out.println("State snapshot saved");
        
        gameState.incrementFoodCounter();
        gameState.incrementFoodCounter();
        gameState.incrementFoodCounter();
        gameState.incrementFoodCounter();
        gameState.incrementFoodCounter();
        
        if (gameState.shouldTriggerTimeRewind()) {
            System.out.println("Time rewind should trigger!");
            GameState.GameStateSnapshot snapshot = gameState.getRewindSnapshot();
            if (snapshot != null) {
                System.out.println("Rewind snapshot available from: " + snapshot.timestamp);
            }
        }
        
        // Test notification system
        notificationSystem.showGameNotification("Test", "Special effects working!", "✨");
        System.out.println("Notification count: " + notificationSystem.getNotificationCount());
    }
}
