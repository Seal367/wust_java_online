package com.example.javafx3.model;

/**
 * Enumeration defining all 15 skill effects in the Snake game
 * Organized by rarity tiers with specific probabilities and effects
 */
public enum SkillEffect {
    // Common Skills (40% probability)
    DOUBLE_SCORE("Double Score", "Food score +100%", 1, "SCORING"),
    SLOW_MOVEMENT("Slow Movement", "Snake speed -50%", 1, "MOVEMENT"),
    BASIC_SHIELD("Basic Shield", "Immunity to 1 collision", 1, "SURVIVAL"),

    // Rare Skills (35% probability)
    WALL_PASSER("Wall Passer", "Can pass through boundaries", 2, "MOVEMENT"),
    STEEL_BODY("Steel Body", "Complete immunity to self-collision", 2, "SURVIVAL"),
    FOOD_MAGNET("Food Magnet", "Food moves toward snake head", 2, "FOOD"),

    // Epic Skills (20% probability)
    BODY_SPLIT("Body Split", "Snake body length halved", 3, "SIZE"),
    BODY_GROWTH("Body Growth", "Snake body length doubled", 3, "SIZE"),
    TIME_REWIND("Time Rewind", "Rewind 3 seconds every 5 food items", 3, "TIME"),

    // Legendary Skills (5% probability)
    LUCKY_STAR("Lucky Star", "25% chance to spawn double food", 4, "FOOD"),
    GHOST_MODE("Ghost Mode", "Collision volume reduced by 50%", 4, "SURVIVAL"),
    SCORE_FRENZY("Score Frenzy", "Consecutive food eating increases score", 4, "SCORING"),
    WORLD_SHRINK("World Shrink", "Game area shrinks by 30%", 4, "WORLD"),
    SECOND_CHANCE("Second Chance", "Revive on first death", 4, "SURVIVAL"),

    // Special skill for testing (will be removed in final version)
    SKILL_SLOT_EXPAND("Skill Slot Expand", "Adds one additional skill activation slot", 3, "SYSTEM");
    
    private final String name;
    private final String description;
    private final int rarity;
    private final String effectType;
    
    SkillEffect(String name, String description, int rarity, String effectType) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.effectType = effectType;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getRarity() { return rarity; }
    public String getEffectType() { return effectType; }
    
    /**
     * Get rarity color for UI display
     */
    public String getRarityColor() {
        switch (rarity) {
            case 1: return "#FFFFFF"; // White - Common
            case 2: return "#00FF00"; // Green - Uncommon  
            case 3: return "#0080FF"; // Blue - Rare
            case 4: return "#FF8000"; // Orange - Legendary
            default: return "#FFFFFF";
        }
    }
    
    /**
     * Get rarity name for display
     */
    public String getRarityName() {
        switch (rarity) {
            case 1: return "Common";
            case 2: return "Uncommon";
            case 3: return "Rare";
            case 4: return "Legendary";
            default: return "Unknown";
        }
    }
    
    /**
     * Get skill icon representation (Unicode symbols)
     */
    public String getIcon() {
        switch (this) {
            case DOUBLE_SCORE: return "✖";
            case SLOW_MOVEMENT: return "🐌";
            case BASIC_SHIELD: return "🛡";
            case WALL_PASSER: return "🚪";
            case STEEL_BODY: return "⚔";
            case FOOD_MAGNET: return "🧲";
            case BODY_SPLIT: return "✂";
            case BODY_GROWTH: return "📈";
            case TIME_REWIND: return "⏪";
            case LUCKY_STAR: return "⭐";
            case GHOST_MODE: return "👻";
            case SCORE_FRENZY: return "🔥";
            case WORLD_SHRINK: return "🔄";
            case SECOND_CHANCE: return "❤";
            case SKILL_SLOT_EXPAND: return "📦";
            default: return "?";
        }
    }
    
    /**
     * Get rarity weights for random selection
     */
    public static double getRarityWeight(int rarity) {
        switch (rarity) {
            case 1: return 0.50; // 50% chance
            case 2: return 0.30; // 30% chance
            case 3: return 0.15; // 15% chance
            case 4: return 0.05; // 5% chance
            default: return 0.0;
        }
    }
}
