package com.example.javafx3;

import com.example.javafx3.manager.GameState;
import com.example.javafx3.manager.SkillManager;
import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import com.example.javafx3.ui.SkillNotificationSystem;
import com.example.javafx3.ui.SimpleSkillMenuController;

import java.util.Scanner;

/**
 * Simple console-based Snake game with comprehensive skill system
 * This version works without JavaFX dependencies for testing purposes
 */
public class SimpleSnakeGame implements SkillManager.SnakeGameEffects {
    
    // Game state
    private GameState gameState;
    private SkillManager skillManager;
    private SkillNotificationSystem notificationSystem;
    private SimpleSkillMenuController menuController;
    
    // Game variables
    private int score = 0;
    private boolean gameRunning = false;
    
    // Skill effects state
    private double speedMultiplier = 1.0;
    private double scoreMultiplier = 1.0;
    private int foodMagnetRadius = 0;
    private boolean wallPhasingEnabled = false;
    private boolean ghostModeEnabled = false;
    private boolean basicShieldEnabled = false;
    private boolean steelBodyEnabled = false;
    private boolean secondChanceEnabled = false;
    private boolean timeRewindEnabled = false;
    private boolean luckyStarEnabled = false;
    private boolean scoreFrenzyEnabled = false;
    private boolean worldShrinkEnabled = false;
    private int shieldUses = 0;
    
    public SimpleSnakeGame() {
        // Initialize skill system
        gameState = new GameState();
        skillManager = new SkillManager(gameState);
        notificationSystem = new SkillNotificationSystem(null);
        menuController = new SimpleSkillMenuController();
    }
    
    public void startGame() {
        System.out.println("=== SIMPLE SNAKE GAME WITH SKILL SYSTEM ===");
        gameState.startGame();
        gameRunning = true;
        
        Scanner scanner = new Scanner(System.in);
        
        while (gameRunning) {
            displayGameStatus();
            System.out.println("\nCommands: [e]at food, [s]kill menu, [q]uit");
            System.out.print("Enter command: ");
            
            String command = scanner.nextLine().toLowerCase().trim();
            
            switch (command) {
                case "e":
                case "eat":
                    eatFood();
                    break;
                case "s":
                case "skill":
                    openSkillMenu();
                    break;
                case "q":
                case "quit":
                    gameRunning = false;
                    break;
                default:
                    System.out.println("Invalid command!");
            }
        }
        
        System.out.println("Game ended. Final score: " + score);
        scanner.close();
    }
    
    private void displayGameStatus() {
        System.out.println("\n--- GAME STATUS ---");
        System.out.println("Score: " + score);
        System.out.println("Speed Multiplier: " + speedMultiplier + "x");
        System.out.println("Score Multiplier: " + scoreMultiplier + "x");
        System.out.println("Active Skills: " + gameState.getActiveSkills().size() + "/" + gameState.getMaxActivationSlots());
        
        // Show skill progress
        double progress = gameState.getSkillProgress();
        int pointsToNext = gameState.getPointsToNextSkill();
        System.out.println("Next Skill: " + (int)(progress * 100) + "% (" + pointsToNext + " points)");
        
        // Show active effects
        if (basicShieldEnabled) System.out.println("🛡 Basic Shield Active (" + shieldUses + " uses)");
        if (steelBodyEnabled) System.out.println("⚔ Steel Body Active");
        if (ghostModeEnabled) System.out.println("👻 Ghost Mode Active");
        if (scoreFrenzyEnabled) System.out.println("🔥 Score Frenzy: " + gameState.getComboMultiplier() + "x");
        if (timeRewindEnabled) System.out.println("⏪ Time Rewind Ready");
        if (luckyStarEnabled) System.out.println("⭐ Lucky Star Active");
        if (worldShrinkEnabled) System.out.println("🔄 World Shrunk");
    }
    
    private void eatFood() {
        System.out.println("Eating food...");
        
        // Apply skill effects
        skillManager.applySkillEffects(this);
        
        // Calculate points
        int basePoints = 10;
        double totalMultiplier = scoreMultiplier;
        
        if (scoreFrenzyEnabled) {
            totalMultiplier *= gameState.getScoreFrenzyMultiplier();
        }
        
        int points = (int)(basePoints * totalMultiplier);
        
        // Update score
        if (gameState.updateScore(points)) {
            System.out.println("🎉 VICTORY! You've reached the win condition!");
            gameRunning = false;
            return;
        }
        
        score = gameState.getScore();
        gameState.incrementFoodCounter();
        
        System.out.println("Gained " + points + " points!");
        
        // Check for skill unlock
        if (gameState.shouldEarnSkill()) {
            SkillCard newSkill = skillManager.generateRandomSkill();
            if (newSkill != null) {
                if (skillManager.isDuplicateSkill(newSkill)) {
                    int bonusScore = skillManager.convertDuplicateToScore(newSkill);
                    gameState.updateScore(bonusScore);
                    score = gameState.getScore();
                    notificationSystem.showGameNotification("Duplicate Skill", 
                        "Converted to " + bonusScore + " bonus points!", "💰");
                } else {
                    gameState.awardSkill(newSkill);
                    notificationSystem.showSkillUnlocked(newSkill);
                    System.out.println("🎯 NEW SKILL UNLOCKED: " + newSkill.getName() + " (" + newSkill.getRarityName() + ")");
                }
            }
        }
        
        // Check time rewind
        if (timeRewindEnabled && gameState.shouldTriggerTimeRewind()) {
            System.out.println("⏪ Time rewind available! (Auto-save triggered)");
            gameState.saveStateSnapshot("game_snapshot_" + System.currentTimeMillis());
        }
        
        // Lucky star effect
        if (luckyStarEnabled && Math.random() < 0.25) {
            System.out.println("⭐ Lucky Star triggered! Bonus food appeared!");
            eatFood(); // Recursive call for bonus food
        }
    }
    
    private void openSkillMenu() {
        System.out.println("\n=== OPENING SKILL MENU ===");
        menuController.initializeController(gameState, skillManager, notificationSystem, () -> {
            System.out.println("Skill menu closed");
        });
        
        Scanner scanner = new Scanner(System.in);
        boolean inMenu = true;
        
        while (inMenu) {
            System.out.println("\nSkill Menu Commands:");
            System.out.println("[a]ctivate <number> - Activate skill by number");
            System.out.println("[d]eactivate - Deactivate all skills");
            System.out.println("[s]tats - Show skill statistics");
            System.out.println("[b]ack - Return to game");
            System.out.print("Menu command: ");
            
            String command = scanner.nextLine().toLowerCase().trim();
            
            if (command.startsWith("a")) {
                try {
                    String[] parts = command.split(" ");
                    if (parts.length > 1) {
                        int skillIndex = Integer.parseInt(parts[1]) - 1;
                        menuController.activateSkill(skillIndex);
                    } else {
                        System.out.println("Usage: activate <number>");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid skill number!");
                }
            } else if (command.equals("d") || command.equals("deactivate")) {
                menuController.deactivateAllSkills();
            } else if (command.equals("s") || command.equals("stats")) {
                menuController.displaySkillStatistics();
            } else if (command.equals("b") || command.equals("back")) {
                inMenu = false;
                menuController.closeMenu();
            } else {
                System.out.println("Invalid menu command!");
            }
        }
    }
    
    // ===== SKILL EFFECTS IMPLEMENTATION =====
    
    @Override
    public void applySpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }
    
    @Override
    public void enableWallPhasing() {
        this.wallPhasingEnabled = true;
    }
    
    @Override
    public void enableFoodMagnet(int radius) {
        this.foodMagnetRadius = radius;
    }
    
    @Override
    public void enableDoubleFood() {
        System.out.println("🍎 Double food effect activated!");
    }
    
    @Override
    public void enableLuckyStarFood() {
        this.luckyStarEnabled = true;
    }
    
    @Override
    public void applyScoreMultiplier(double multiplier) {
        this.scoreMultiplier = multiplier;
    }
    
    @Override
    public void enableScoreFrenzy() {
        this.scoreFrenzyEnabled = true;
        gameState.enableScoreFrenzy();
    }
    
    @Override
    public void enableBasicShield() {
        this.basicShieldEnabled = true;
        this.shieldUses = 1;
    }
    
    @Override
    public void enableSteelBody() {
        this.steelBodyEnabled = true;
    }
    
    @Override
    public void enableGhostMode() {
        this.ghostModeEnabled = true;
    }
    
    @Override
    public void enableSecondChance() {
        this.secondChanceEnabled = true;
    }
    
    @Override
    public void enableInvincibility() {
        System.out.println("🛡 Invincibility activated!");
    }
    
    @Override
    public void enableTimeRewind() {
        this.timeRewindEnabled = true;
    }
    
    @Override
    public void enableTimeFreezeMode() {
        System.out.println("❄ Time freeze activated!");
    }
    
    @Override
    public void enableWorldShrink() {
        this.worldShrinkEnabled = true;
    }
    
    @Override
    public void expandSkillSlots() {
        notificationSystem.showGameNotification("Skill Slots", "Additional slot unlocked!", "📦");
    }
    
    @Override
    public void teleportSnake() {
        System.out.println("🌀 Snake teleported!");
    }
    
    @Override
    public void shrinkSnake() {
        System.out.println("✂ Snake body split in half!");
    }
    
    @Override
    public void growSnake() {
        System.out.println("📈 Snake body doubled in size!");
    }
    
    public static void main(String[] args) {
        SimpleSnakeGame game = new SimpleSnakeGame();
        game.startGame();
    }
}
