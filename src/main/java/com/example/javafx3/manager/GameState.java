package com.example.javafx3.manager;

import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import java.util.*;

/**
 * Enhanced game state manager with comprehensive skill system support
 */
public class GameState {
    private int score;
    private int previousSkillScore; // Track last score when skill was awarded
    private boolean gameWon;
    private boolean gameLost;
    private boolean gameRunning;
    private long gameStartTime;
    private int skillsEarned;

    // Skill-related state
    private List<SkillCard> earnedSkills;
    private List<SkillCard> activeSkills;
    private int maxActivationSlots;
    private int usedActivationSlots;

    // Time rewind system
    private List<GameStateSnapshot> stateSnapshots;
    private int rewindThreshold = 5;
    private int foodsSinceLastSnapshot = 0;

    // Score frenzy system
    private int comboMultiplier = 0;
    private boolean scoreFrenzyActive = false;

    // Game constants
    private static final int POINTS_PER_SKILL = 60;
    private static final int WIN_SCORE = 900;
    private static final int MAX_SKILLS = 15;
    private static final int INITIAL_ACTIVATION_SLOTS = 3;
    private static final int MAX_STATE_SNAPSHOTS = 10;
    
    public GameState() {
        reset();
    }

    /**
     * Reset game state to initial values
     */
    public void reset() {
        score = 0;
        previousSkillScore = 0;
        gameWon = false;
        gameLost = false;
        gameRunning = false;
        gameStartTime = 0;
        skillsEarned = 0;

        earnedSkills = new ArrayList<>();
        activeSkills = new ArrayList<>();
        maxActivationSlots = INITIAL_ACTIVATION_SLOTS;
        usedActivationSlots = 0;

        // Reset time rewind system
        stateSnapshots = new ArrayList<>();
        foodsSinceLastSnapshot = 0;

        // Reset score frenzy
        comboMultiplier = 0;
        scoreFrenzyActive = false;
    }
    
    /**
     * Start the game
     */
    public void startGame() {
        gameRunning = true;
        gameStartTime = System.currentTimeMillis();
    }
    
    /**
     * Update score and check for skill unlock
     */
    public boolean updateScore(int points) {
        if (!gameRunning || gameWon || gameLost) {
            return false;
        }
        
        score += points;
        
        // Check win condition
        if (score >= WIN_SCORE) {
            gameWon = true;
            gameRunning = false;
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if player should earn a new skill
     */
    public boolean shouldEarnSkill() {
        if (!gameRunning || gameWon || gameLost) {
            return false;
        }
        
        int skillsEarnedByScore = score / POINTS_PER_SKILL;
        return skillsEarnedByScore > skillsEarned && skillsEarned < MAX_SKILLS;
    }
    
    /**
     * Award a skill to the player
     */
    public void awardSkill(SkillCard skill) {
        if (skill != null && skillsEarned < MAX_SKILLS) {
            earnedSkills.add(skill);
            skillsEarned++;
            previousSkillScore = (skillsEarned * POINTS_PER_SKILL);
        }
    }
    
    /**
     * Activate a skill
     */
    public boolean activateSkill(SkillCard skill) {
        if (!gameRunning || skill == null || !earnedSkills.contains(skill)) {
            return false;
        }
        
        // Check if we have available activation slots
        if (usedActivationSlots >= maxActivationSlots) {
            return false;
        }
        
        // Check if skill can be activated
        if (!skill.canActivate()) {
            return false;
        }
        
        // Activate the skill
        if (skill.activate()) {
            activeSkills.add(skill);
            usedActivationSlots++;
            
            // Handle special skill effects
            handleSpecialSkillEffects(skill);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle special skill effects that modify game state
     */
    private void handleSpecialSkillEffects(SkillCard skill) {
        switch (skill.getSkillEffect()) {
            case SKILL_SLOT_EXPAND:
                maxActivationSlots++;
                break;
            // Other special effects can be handled here
        }
    }
    
    /**
     * Update active skills and remove expired ones
     */
    public void updateActiveSkills() {
        Iterator<SkillCard> iterator = activeSkills.iterator();
        while (iterator.hasNext()) {
            SkillCard skill = iterator.next();
            if (skill.hasExpired()) {
                skill.deactivate();
                iterator.remove();
                usedActivationSlots--;
            }
        }
    }
    
    /**
     * Remove active skill (for conflict resolution)
     */
    public void removeActiveSkill(SkillCard skill) {
        if (activeSkills.remove(skill)) {
            usedActivationSlots--;
        }
    }

    /**
     * Save game state snapshot for time rewind
     */
    public void saveStateSnapshot(Object gameData) {
        GameStateSnapshot snapshot = new GameStateSnapshot(
            score,
            new ArrayList<>(earnedSkills),
            new ArrayList<>(activeSkills),
            System.currentTimeMillis(),
            gameData
        );

        stateSnapshots.add(snapshot);

        // Keep only recent snapshots
        if (stateSnapshots.size() > MAX_STATE_SNAPSHOTS) {
            stateSnapshots.remove(0);
        }

        foodsSinceLastSnapshot = 0;
    }

    /**
     * Check if time rewind should trigger
     */
    public boolean shouldTriggerTimeRewind() {
        return foodsSinceLastSnapshot >= rewindThreshold && !stateSnapshots.isEmpty();
    }

    /**
     * Get most recent state snapshot for rewind
     */
    public GameStateSnapshot getRewindSnapshot() {
        if (stateSnapshots.isEmpty()) {
            return null;
        }

        // Return snapshot from 3 seconds ago (or closest available)
        long currentTime = System.currentTimeMillis();
        GameStateSnapshot bestSnapshot = stateSnapshots.get(stateSnapshots.size() - 1);

        for (int i = stateSnapshots.size() - 1; i >= 0; i--) {
            GameStateSnapshot snapshot = stateSnapshots.get(i);
            if (currentTime - snapshot.timestamp >= 3000) { // 3 seconds
                bestSnapshot = snapshot;
                break;
            }
        }

        return bestSnapshot;
    }

    /**
     * Apply time rewind
     */
    public void applyTimeRewind(GameStateSnapshot snapshot) {
        this.score = snapshot.score;
        this.earnedSkills = new ArrayList<>(snapshot.earnedSkills);
        this.activeSkills = new ArrayList<>(snapshot.activeSkills);
        this.usedActivationSlots = activeSkills.size();

        // Reset rewind counter
        foodsSinceLastSnapshot = 0;
    }

    /**
     * Increment food counter for time rewind and score frenzy
     */
    public void incrementFoodCounter() {
        foodsSinceLastSnapshot++;

        if (scoreFrenzyActive) {
            comboMultiplier++;
        }
    }

    /**
     * Enable score frenzy mode
     */
    public void enableScoreFrenzy() {
        scoreFrenzyActive = true;
        comboMultiplier = 0;
    }

    /**
     * Get current score multiplier for frenzy mode
     */
    public double getScoreFrenzyMultiplier() {
        if (!scoreFrenzyActive) {
            return 1.0;
        }
        return 1.0 + (comboMultiplier * 0.1); // +10% per combo
    }

    /**
     * Reset score frenzy combo
     */
    public void resetScoreFrenzyCombo() {
        comboMultiplier = 0;
    }

    /**
     * End game with loss condition
     */
    public void endGameWithLoss() {
        gameLost = true;
        gameRunning = false;
    }
    
    /**
     * Check if specific skill is active
     */
    public boolean isSkillActive(SkillEffect effect) {
        return activeSkills.stream()
                .anyMatch(skill -> skill.getSkillEffect() == effect && skill.isActive());
    }
    
    /**
     * Get active skill of specific type
     */
    public SkillCard getActiveSkill(SkillEffect effect) {
        return activeSkills.stream()
                .filter(skill -> skill.getSkillEffect() == effect && skill.isActive())
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get game duration in seconds
     */
    public long getGameDuration() {
        if (gameStartTime == 0) return 0;
        return (System.currentTimeMillis() - gameStartTime) / 1000;
    }
    
    /**
     * Get progress to next skill (0.0 to 1.0)
     */
    public double getSkillProgress() {
        if (skillsEarned >= MAX_SKILLS) return 1.0;
        
        int nextSkillScore = (skillsEarned + 1) * POINTS_PER_SKILL;
        int progressScore = score - (skillsEarned * POINTS_PER_SKILL);
        
        return Math.min(1.0, (double) progressScore / POINTS_PER_SKILL);
    }
    
    // Getters
    public int getScore() { return score; }
    public boolean isGameWon() { return gameWon; }
    public boolean isGameLost() { return gameLost; }
    public boolean isGameRunning() { return gameRunning; }
    public int getSkillsEarned() { return skillsEarned; }
    public List<SkillCard> getEarnedSkills() { return new ArrayList<>(earnedSkills); }
    public List<SkillCard> getActiveSkills() { return new ArrayList<>(activeSkills); }
    public int getMaxActivationSlots() { return maxActivationSlots; }
    public int getUsedActivationSlots() { return usedActivationSlots; }
    public int getAvailableActivationSlots() { return maxActivationSlots - usedActivationSlots; }
    public int getPointsToNextSkill() {
        if (skillsEarned >= MAX_SKILLS) return 0;
        return ((skillsEarned + 1) * POINTS_PER_SKILL) - score;
    }
    public int getPointsToWin() { return Math.max(0, WIN_SCORE - score); }
    public int getComboMultiplier() { return comboMultiplier; }
    public boolean isScoreFrenzyActive() { return scoreFrenzyActive; }

    // Constants getters
    public static int getPointsPerSkill() { return POINTS_PER_SKILL; }
    public static int getWinScore() { return WIN_SCORE; }
    public static int getMaxSkills() { return MAX_SKILLS; }

    /**
     * Game state snapshot for time rewind functionality
     */
    public static class GameStateSnapshot {
        public final int score;
        public final List<SkillCard> earnedSkills;
        public final List<SkillCard> activeSkills;
        public final long timestamp;
        public final Object gameData; // Snake position, food position, etc.

        public GameStateSnapshot(int score, List<SkillCard> earnedSkills,
                               List<SkillCard> activeSkills, long timestamp, Object gameData) {
            this.score = score;
            this.earnedSkills = earnedSkills;
            this.activeSkills = activeSkills;
            this.timestamp = timestamp;
            this.gameData = gameData;
        }
    }
}
