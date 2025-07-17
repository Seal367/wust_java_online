package com.example.javafx3.model;

/**
 * Represents a skill card in the Snake game
 * Contains all properties and methods for skill management
 */
public class SkillCard {
    private final String id;
    private final SkillEffect skillEffect;
    private int remainingActivations;
    private boolean isActive;
    private long activationTime;
    private long duration; // Duration in milliseconds, -1 for permanent
    
    /**
     * Constructor for creating a new skill card
     */
    public SkillCard(SkillEffect skillEffect) {
        this.id = generateId();
        this.skillEffect = skillEffect;
        this.remainingActivations = getMaxActivations();
        this.isActive = false;
        this.activationTime = 0;
        this.duration = getSkillDuration();
    }
    
    /**
     * Generate unique ID for the skill card
     */
    private String generateId() {
        return "SKILL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * Get maximum activations for this skill type
     */
    private int getMaxActivations() {
        // Most skills can only be used once, except some special ones
        switch (skillEffect) {
            // One-time use skills
            case WALL_PASSER:
            case BASIC_SHIELD:
            case SECOND_CHANCE:
            case BODY_SPLIT:
            case BODY_GROWTH:
            case SKILL_SLOT_EXPAND:
                return 1;
            // Permanent effect skills (can be activated once)
            case DOUBLE_SCORE:
            case SLOW_MOVEMENT:
            case STEEL_BODY:
            case FOOD_MAGNET:
            case TIME_REWIND:
            case LUCKY_STAR:
            case GHOST_MODE:
            case SCORE_FRENZY:
            case WORLD_SHRINK:
                return 1;
            default:
                return 1;
        }
    }
    
    /**
     * Get skill duration in milliseconds
     */
    private long getSkillDuration() {
        switch (skillEffect) {
            // Common skills
            case DOUBLE_SCORE: return -1; // Permanent
            case SLOW_MOVEMENT: return -1; // Permanent
            case BASIC_SHIELD: return -1; // One-time use

            // Rare skills
            case WALL_PASSER: return -1; // One-time use
            case STEEL_BODY: return -1; // Permanent
            case FOOD_MAGNET: return -1; // Permanent

            // Epic skills
            case BODY_SPLIT: return -1; // Instant
            case BODY_GROWTH: return -1; // Instant
            case TIME_REWIND: return -1; // Permanent (triggers automatically)

            // Legendary skills
            case LUCKY_STAR: return -1; // Permanent
            case GHOST_MODE: return 30000; // 30 seconds
            case SCORE_FRENZY: return -1; // Permanent (combo-based)
            case WORLD_SHRINK: return -1; // Permanent
            case SECOND_CHANCE: return -1; // One-time use

            // System skills
            case SKILL_SLOT_EXPAND: return -1; // Permanent

            default: return -1;
        }
    }
    
    /**
     * Activate the skill
     */
    public boolean activate() {
        if (remainingActivations <= 0 || isActive) {
            return false;
        }
        
        isActive = true;
        activationTime = System.currentTimeMillis();
        remainingActivations--;
        
        return true;
    }
    
    /**
     * Check if skill effect has expired
     */
    public boolean hasExpired() {
        if (!isActive || duration == -1) {
            return false;
        }
        
        return System.currentTimeMillis() - activationTime >= duration;
    }
    
    /**
     * Deactivate the skill
     */
    public void deactivate() {
        isActive = false;
        activationTime = 0;
    }
    
    /**
     * Get remaining duration in seconds
     */
    public int getRemainingDuration() {
        if (!isActive || duration == -1) {
            return -1;
        }
        
        long remaining = duration - (System.currentTimeMillis() - activationTime);
        return Math.max(0, (int)(remaining / 1000));
    }
    
    /**
     * Check if skill can be activated
     */
    public boolean canActivate() {
        return remainingActivations > 0 && !isActive;
    }
    
    // Getters
    public String getId() { return id; }
    public SkillEffect getSkillEffect() { return skillEffect; }
    public String getName() { return skillEffect.getName(); }
    public String getDescription() { return skillEffect.getDescription(); }
    public int getRarity() { return skillEffect.getRarity(); }
    public String getIcon() { return skillEffect.getIcon(); }
    public String getEffectType() { return skillEffect.getEffectType(); }
    public int getRemainingActivations() { return remainingActivations; }
    public boolean isActive() { return isActive; }
    public long getActivationTime() { return activationTime; }
    public long getDuration() { return duration; }
    public String getRarityColor() { return skillEffect.getRarityColor(); }
    public String getRarityName() { return skillEffect.getRarityName(); }
    
    /**
     * Get display text for UI
     */
    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getIcon()).append(" ").append(getName());
        
        if (isActive && getRemainingDuration() > 0) {
            sb.append(" (").append(getRemainingDuration()).append("s)");
        } else if (remainingActivations > 0) {
            sb.append(" (").append(remainingActivations).append(" uses)");
        } else {
            sb.append(" (Used)");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("SkillCard{id='%s', effect=%s, active=%s, remaining=%d}", 
                           id, skillEffect.getName(), isActive, remainingActivations);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SkillCard skillCard = (SkillCard) obj;
        return id.equals(skillCard.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
