package com.example.javafx3.manager;

import com.example.javafx3.model.SkillCard;
import com.example.javafx3.model.SkillEffect;
import java.util.*;

/**
 * Comprehensive skill manager with rarity distribution, conflict resolution, and dynamic balance
 */
public class SkillManager {
    private Random random;
    private GameState gameState;

    // Skill generation tracking
    private Map<SkillEffect, Integer> skillCounts;
    private List<SkillEffect> availableSkills;

    // Dynamic balance system
    private double skillProbabilityReduction = 0.0;
    private int lastDecayResetScore = 0;

    // Rarity distribution by score ranges
    private static final Map<String, double[]> RARITY_DISTRIBUTION = new HashMap<>();
    static {
        RARITY_DISTRIBUTION.put("0-200", new double[]{0.50, 0.35, 0.15, 0.00});    // Common, Rare, Epic, Legendary
        RARITY_DISTRIBUTION.put("201-500", new double[]{0.45, 0.35, 0.18, 0.02});
        RARITY_DISTRIBUTION.put("501-1000", new double[]{0.40, 0.35, 0.20, 0.05});
        RARITY_DISTRIBUTION.put("1000+", new double[]{0.35, 0.35, 0.22, 0.08});
    }

    // Skill conflicts mapping
    private static final Map<SkillEffect, Set<SkillEffect>> SKILL_CONFLICTS = new HashMap<>();
    static {
        // Speed conflicts
        Set<SkillEffect> speedConflicts = new HashSet<>();
        speedConflicts.add(SkillEffect.SLOW_MOVEMENT);
        SKILL_CONFLICTS.put(SkillEffect.SLOW_MOVEMENT, speedConflicts);

        // Body size conflicts
        Set<SkillEffect> bodySizeConflicts = new HashSet<>();
        bodySizeConflicts.add(SkillEffect.BODY_GROWTH);
        bodySizeConflicts.add(SkillEffect.BODY_SPLIT);
        SKILL_CONFLICTS.put(SkillEffect.BODY_GROWTH, bodySizeConflicts);
        SKILL_CONFLICTS.put(SkillEffect.BODY_SPLIT, bodySizeConflicts);

        // Shield conflicts (basic shield is overridden by steel body)
        Set<SkillEffect> shieldConflicts = new HashSet<>();
        shieldConflicts.add(SkillEffect.BASIC_SHIELD);
        SKILL_CONFLICTS.put(SkillEffect.STEEL_BODY, shieldConflicts);
    }

    public SkillManager(GameState gameState) {
        this.gameState = gameState;
        this.random = new Random();
        this.skillCounts = new HashMap<>();
        this.availableSkills = new ArrayList<>();

        initializeAvailableSkills();
    }
    
    /**
     * Initialize the list of available skills
     */
    private void initializeAvailableSkills() {
        availableSkills.addAll(Arrays.asList(SkillEffect.values()));
        
        // Initialize skill counts
        for (SkillEffect skill : SkillEffect.values()) {
            skillCounts.put(skill, 0);
        }
    }
    
    /**
     * Generate a random skill card based on comprehensive rarity distribution and dynamic balance
     */
    public SkillCard generateRandomSkill() {
        // Filter out skills that have already been earned (most skills are unique)
        List<SkillEffect> eligibleSkills = getEligibleSkills();

        if (eligibleSkills.isEmpty()) {
            return null; // No more skills available
        }

        // Apply dynamic balance system
        updateDynamicBalance();

        // Get rarity distribution for current score
        double[] rarityWeights = getRarityDistributionForScore(gameState.getScore());

        // Apply skill probability reduction from dynamic balance
        for (int i = 0; i < rarityWeights.length; i++) {
            rarityWeights[i] *= (1.0 - skillProbabilityReduction);
        }

        // Select rarity tier
        int selectedRarity = selectRarityTier(rarityWeights);

        // Get available skills for selected rarity from eligible skills
        List<SkillEffect> candidateSkills = new ArrayList<>();
        for (SkillEffect skill : eligibleSkills) {
            if (skill.getRarity() == selectedRarity) {
                candidateSkills.add(skill);
            }
        }

        if (candidateSkills.isEmpty()) {
            // Fallback to any eligible skill
            candidateSkills = eligibleSkills;
        }

        // Select random skill from candidates
        SkillEffect selectedSkill = candidateSkills.get(random.nextInt(candidateSkills.size()));

        // Update skill count
        skillCounts.put(selectedSkill, skillCounts.get(selectedSkill) + 1);

        return new SkillCard(selectedSkill);
    }
    
    /**
     * Get skills that are still eligible to be earned
     */
    private List<SkillEffect> getEligibleSkills() {
        List<SkillEffect> eligible = new ArrayList<>();
        
        for (SkillEffect skill : availableSkills) {
            // Most skills can only be earned once
            if (skillCounts.get(skill) == 0) {
                eligible.add(skill);
            }
        }
        
        return eligible;
    }
    
    /**
     * Select a skill based on rarity weights
     */
    private SkillEffect selectSkillByRarity(List<SkillEffect> eligibleSkills) {
        if (eligibleSkills.isEmpty()) {
            return null;
        }
        
        // Calculate total weight
        double totalWeight = 0.0;
        for (SkillEffect skill : eligibleSkills) {
            totalWeight += SkillEffect.getRarityWeight(skill.getRarity());
        }
        
        // Generate random value
        double randomValue = random.nextDouble() * totalWeight;
        
        // Select skill based on weight
        double currentWeight = 0.0;
        for (SkillEffect skill : eligibleSkills) {
            currentWeight += SkillEffect.getRarityWeight(skill.getRarity());
            if (randomValue <= currentWeight) {
                return skill;
            }
        }
        
        // Fallback to last skill if something goes wrong
        return eligibleSkills.get(eligibleSkills.size() - 1);
    }
    
    /**
     * Apply comprehensive skill effects to game mechanics
     */
    public void applySkillEffects(SnakeGameEffects gameEffects) {
        List<SkillCard> activeSkills = gameState.getActiveSkills();

        for (SkillCard skill : activeSkills) {
            if (!skill.isActive()) continue;

            switch (skill.getSkillEffect()) {
                // Common Skills
                case DOUBLE_SCORE:
                    gameEffects.applyScoreMultiplier(2.0);
                    break;

                case SLOW_MOVEMENT:
                    gameEffects.applySpeedMultiplier(0.5); // 50% slower
                    break;

                case BASIC_SHIELD:
                    gameEffects.enableBasicShield();
                    break;

                // Rare Skills
                case WALL_PASSER:
                    gameEffects.enableWallPhasing();
                    break;

                case STEEL_BODY:
                    gameEffects.enableSteelBody();
                    break;

                case FOOD_MAGNET:
                    gameEffects.enableFoodMagnet(3); // 3 cell radius
                    break;

                // Epic Skills
                case BODY_SPLIT:
                    gameEffects.shrinkSnake();
                    break;

                case BODY_GROWTH:
                    gameEffects.growSnake();
                    break;

                case TIME_REWIND:
                    gameEffects.enableTimeRewind();
                    break;

                // Legendary Skills
                case LUCKY_STAR:
                    gameEffects.enableLuckyStarFood();
                    break;

                case GHOST_MODE:
                    gameEffects.enableGhostMode();
                    break;

                case SCORE_FRENZY:
                    gameEffects.enableScoreFrenzy();
                    break;

                case WORLD_SHRINK:
                    gameEffects.enableWorldShrink();
                    break;

                case SECOND_CHANCE:
                    gameEffects.enableSecondChance();
                    break;

                // System Skills
                case SKILL_SLOT_EXPAND:
                    gameEffects.expandSkillSlots();
                    break;
            }
        }
    }
    
    /**
     * Handle instant skill effects
     */
    public void handleInstantSkill(SkillCard skill, SnakeGameEffects gameEffects) {
        switch (skill.getSkillEffect()) {
            case BODY_SPLIT:
                gameEffects.shrinkSnake();
                break;

            case BODY_GROWTH:
                gameEffects.growSnake();
                break;

            // Most skills are now handled through applySkillEffects
            // Instant skills are those that take immediate effect
            default:
                // No instant effect needed
                break;
        }
    }
    
    /**
     * Get skills by rarity tier
     */
    public List<SkillEffect> getSkillsByRarity(int rarity) {
        List<SkillEffect> skills = new ArrayList<>();
        for (SkillEffect skill : SkillEffect.values()) {
            if (skill.getRarity() == rarity) {
                skills.add(skill);
            }
        }
        return skills;
    }
    
    /**
     * Get skill statistics
     */
    public Map<Integer, Integer> getSkillRarityDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();
        
        for (SkillCard skill : gameState.getEarnedSkills()) {
            int rarity = skill.getRarity();
            distribution.put(rarity, distribution.getOrDefault(rarity, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * Check if all skills of a rarity have been earned
     */
    public boolean isRarityCompleted(int rarity) {
        List<SkillEffect> raritySkills = getSkillsByRarity(rarity);
        
        for (SkillEffect skill : raritySkills) {
            if (skillCounts.get(skill) == 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Update dynamic balance system
     */
    private void updateDynamicBalance() {
        int currentScore = gameState.getScore();
        int activeSkillCount = gameState.getActiveSkills().size();

        // Each activated skill reduces new skill probability by 10%
        skillProbabilityReduction = Math.min(0.5, activeSkillCount * 0.1);

        // Reset decay every 100 points
        if (currentScore - lastDecayResetScore >= 100) {
            skillProbabilityReduction = 0.0;
            lastDecayResetScore = currentScore;
        }

        // Apply negative effects for multiple skills
        if (activeSkillCount >= 3) {
            // This would be handled in the main game class
            // -10% snake speed for 3+ skills
        }

        // Apply legendary skill penalties
        boolean hasLegendarySkill = gameState.getActiveSkills().stream()
            .anyMatch(skill -> skill.getRarity() == 4);
        if (hasLegendarySkill) {
            // This would be handled in the main game class
            // -20% food spawn rate
        }
    }

    /**
     * Get rarity distribution based on current score
     */
    private double[] getRarityDistributionForScore(int score) {
        if (score <= 200) {
            return RARITY_DISTRIBUTION.get("0-200");
        } else if (score <= 500) {
            return RARITY_DISTRIBUTION.get("201-500");
        } else if (score <= 1000) {
            return RARITY_DISTRIBUTION.get("501-1000");
        } else {
            return RARITY_DISTRIBUTION.get("1000+");
        }
    }

    /**
     * Select rarity tier based on weighted probabilities
     */
    private int selectRarityTier(double[] weights) {
        double totalWeight = 0.0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue <= cumulativeWeight) {
                return i + 1; // Rarity tiers are 1-based
            }
        }

        return 1; // Default to common
    }

    /**
     * Check if skill is duplicate and convert to score bonus if needed
     */
    public boolean isDuplicateSkill(SkillCard skill) {
        List<SkillCard> earnedSkills = gameState.getEarnedSkills();
        for (SkillCard earnedSkill : earnedSkills) {
            if (earnedSkill.getSkillEffect() == skill.getSkillEffect()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert duplicate skill to score bonus
     */
    public int convertDuplicateToScore(SkillCard skill) {
        // Base bonus varies by rarity
        int baseBonus = switch (skill.getRarity()) {
            case 1 -> 30;  // Common
            case 2 -> 50;  // Rare
            case 3 -> 80;  // Epic
            case 4 -> 120; // Legendary
            default -> 30;
        };

        return baseBonus;
    }

    /**
     * Check and resolve skill conflicts
     */
    public ConflictResolution checkSkillConflicts(SkillCard newSkill) {
        Set<SkillEffect> conflicts = SKILL_CONFLICTS.get(newSkill.getSkillEffect());
        if (conflicts == null) {
            return new ConflictResolution(false, null, null);
        }

        List<SkillCard> activeSkills = gameState.getActiveSkills();
        for (SkillCard activeSkill : activeSkills) {
            if (conflicts.contains(activeSkill.getSkillEffect())) {
                return new ConflictResolution(true, activeSkill, generateConflictMessage(newSkill, activeSkill));
            }
        }

        return new ConflictResolution(false, null, null);
    }

    /**
     * Generate conflict message
     */
    private String generateConflictMessage(SkillCard newSkill, SkillCard conflictingSkill) {
        return String.format("%s conflicts with %s. The old skill will be deactivated.",
                           newSkill.getName(), conflictingSkill.getName());
    }

    /**
     * Resolve skill conflict by deactivating conflicting skill
     */
    public int resolveConflict(SkillCard newSkill, SkillCard conflictingSkill) {
        // Deactivate conflicting skill
        conflictingSkill.deactivate();
        gameState.removeActiveSkill(conflictingSkill);

        // Calculate compensation (50% of score value)
        int compensation = switch (conflictingSkill.getRarity()) {
            case 1 -> 15;  // Common
            case 2 -> 25;  // Rare
            case 3 -> 40;  // Epic
            case 4 -> 60;  // Legendary
            default -> 15;
        };

        return compensation;
    }

    /**
     * Reset skill manager for new game
     */
    public void reset() {
        skillCounts.clear();
        for (SkillEffect skill : SkillEffect.values()) {
            skillCounts.put(skill, 0);
        }
        skillProbabilityReduction = 0.0;
        lastDecayResetScore = 0;
    }
    
    /**
     * Interface for game effects that skills can modify - Updated for comprehensive skill system
     */
    public interface SnakeGameEffects {
        // Movement effects
        void applySpeedMultiplier(double multiplier);
        void enableWallPhasing();
        void teleportSnake();

        // Food effects
        void enableFoodMagnet(int radius);
        void enableDoubleFood();
        void enableLuckyStarFood();

        // Scoring effects
        void applyScoreMultiplier(double multiplier);
        void enableScoreFrenzy();

        // Survival effects
        void enableBasicShield();
        void enableSteelBody();
        void enableGhostMode();
        void enableSecondChance();
        void enableInvincibility();

        // Size effects
        void shrinkSnake();
        void growSnake();

        // Time effects
        void enableTimeRewind();
        void enableTimeFreezeMode();

        // World effects
        void enableWorldShrink();

        // System effects
        void expandSkillSlots();
    }

    /**
     * Conflict resolution result
     */
    public static class ConflictResolution {
        private final boolean hasConflict;
        private final SkillCard conflictingSkill;
        private final String message;

        public ConflictResolution(boolean hasConflict, SkillCard conflictingSkill, String message) {
            this.hasConflict = hasConflict;
            this.conflictingSkill = conflictingSkill;
            this.message = message;
        }

        public boolean hasConflict() { return hasConflict; }
        public SkillCard getConflictingSkill() { return conflictingSkill; }
        public String getMessage() { return message; }
    }
    
    // Getters
    public Map<SkillEffect, Integer> getSkillCounts() { 
        return new HashMap<>(skillCounts); 
    }
    
    public List<SkillEffect> getAvailableSkills() { 
        return new ArrayList<>(availableSkills); 
    }
}
