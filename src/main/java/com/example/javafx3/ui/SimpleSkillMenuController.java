package com.example.javafx3.ui;

import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple skill menu controller that works without JavaFX dependencies
 * This is a console-based implementation for testing the skill system logic
 */
public class SimpleSkillMenuController {
    
    private GameState gameState;
    private SkillManager skillManager;
    private SkillNotificationSystem notificationSystem;
    private SkillCard selectedSkill;
    private Runnable onCloseCallback;
    
    /**
     * Initialize the controller with game state and skill manager
     */
    public void initializeController(GameState gameState, SkillManager skillManager,
                                   SkillNotificationSystem notificationSystem, Runnable onCloseCallback) {
        this.gameState = gameState;
        this.skillManager = skillManager;
        this.notificationSystem = notificationSystem;
        this.onCloseCallback = onCloseCallback;
        
        System.out.println("Skill Menu Controller initialized");
        displaySkillMenu();
    }
    
    /**
     * Display skill menu in console
     */
    public void displaySkillMenu() {
        System.out.println("\n=== SKILL MENU ===");
        System.out.println("Score: " + gameState.getScore());
        System.out.println("Skills Earned: " + gameState.getSkillsEarned() + "/" + GameState.getMaxSkills());
        System.out.println("Active Slots: " + gameState.getUsedActivationSlots() + "/" + gameState.getMaxActivationSlots());
        
        // Display skill progress
        double progress = gameState.getSkillProgress();
        int pointsToNext = gameState.getPointsToNextSkill();
        System.out.println("Progress to Next Skill: " + (int)(progress * 100) + "% (" + pointsToNext + " points needed)");
        
        // Display active skills
        System.out.println("\n--- ACTIVE SKILLS ---");
        List<SkillCard> activeSkills = gameState.getActiveSkills();
        if (activeSkills.isEmpty()) {
            System.out.println("No active skills");
        } else {
            for (SkillCard skill : activeSkills) {
                System.out.println("• " + skill.getDisplayText());
            }
        }
        
        // Display available skills
        System.out.println("\n--- AVAILABLE SKILLS ---");
        List<SkillCard> earnedSkills = gameState.getEarnedSkills();
        if (earnedSkills.isEmpty()) {
            System.out.println("No skills earned yet");
        } else {
            for (int i = 0; i < earnedSkills.size(); i++) {
                SkillCard skill = earnedSkills.get(i);
                String status = skill.isActive() ? "[ACTIVE]" : 
                               skill.canActivate() ? "[READY]" : "[USED]";
                System.out.println((i + 1) + ". " + skill.getDisplayText() + " " + status);
            }
        }
    }
    
    /**
     * Activate skill by index
     */
    public boolean activateSkill(int skillIndex) {
        List<SkillCard> earnedSkills = gameState.getEarnedSkills();
        if (skillIndex < 0 || skillIndex >= earnedSkills.size()) {
            System.out.println("Invalid skill index!");
            return false;
        }
        
        SkillCard skill = earnedSkills.get(skillIndex);
        return activateSelectedSkill(skill);
    }
    
    /**
     * Activate selected skill with conflict resolution
     */
    private boolean activateSelectedSkill(SkillCard skill) {
        if (skill == null || gameState == null) {
            return false;
        }
        
        // Check for conflicts first
        SkillManager.ConflictResolution conflict = skillManager.checkSkillConflicts(skill);
        
        if (conflict.hasConflict()) {
            // Handle conflict resolution
            int compensation = skillManager.resolveConflict(skill, conflict.getConflictingSkill());
            gameState.updateScore(compensation);
            
            if (notificationSystem != null) {
                notificationSystem.showSkillConflict(conflict.getMessage() + 
                    " (+" + compensation + " compensation points)");
            }
            
            System.out.println("Conflict resolved: " + conflict.getMessage());
            System.out.println("Compensation: +" + compensation + " points");
        }
        
        if (gameState.activateSkill(skill)) {
            if (notificationSystem != null) {
                notificationSystem.showSkillActivated(skill);
            }
            
            System.out.println("Skill activated: " + skill.getName());
            return true;
        } else {
            System.out.println("Cannot activate skill: " + skill.getName());
            return false;
        }
    }
    
    /**
     * Deactivate all active skills
     */
    public void deactivateAllSkills() {
        List<SkillCard> activeSkills = gameState.getActiveSkills();
        for (SkillCard skill : activeSkills) {
            skill.deactivate();
        }
        
        System.out.println("All skills deactivated");
        if (notificationSystem != null) {
            notificationSystem.showGameNotification("Skills", "All skills deactivated", "🔄");
        }
    }
    
    /**
     * Filter skills by rarity
     */
    public List<SkillCard> filterSkillsByRarity(int rarity) {
        return gameState.getEarnedSkills().stream()
            .filter(skill -> skill.getRarity() == rarity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get skill statistics
     */
    public void displaySkillStatistics() {
        System.out.println("\n=== SKILL STATISTICS ===");
        
        // Count skills by rarity
        List<SkillCard> earnedSkills = gameState.getEarnedSkills();
        long commonCount = earnedSkills.stream().filter(s -> s.getRarity() == 1).count();
        long rareCount = earnedSkills.stream().filter(s -> s.getRarity() == 2).count();
        long epicCount = earnedSkills.stream().filter(s -> s.getRarity() == 3).count();
        long legendaryCount = earnedSkills.stream().filter(s -> s.getRarity() == 4).count();
        
        System.out.println("Common: " + commonCount);
        System.out.println("Rare: " + rareCount);
        System.out.println("Epic: " + epicCount);
        System.out.println("Legendary: " + legendaryCount);
        
        // Display recent notifications
        if (notificationSystem != null && notificationSystem.hasUnreadNotifications()) {
            System.out.println("\n--- RECENT NOTIFICATIONS ---");
            for (SkillNotificationSystem.NotificationMessage msg : notificationSystem.getRecentNotifications()) {
                System.out.println("• " + msg.message);
            }
        }
    }
    
    /**
     * Close menu
     */
    public void closeMenu() {
        System.out.println("Skill menu closed");
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
