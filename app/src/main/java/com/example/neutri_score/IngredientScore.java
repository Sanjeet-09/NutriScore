package com.example.neutri_score;

import java.io.Serializable;

/**
 * Represents the FSSAI-inspired health score for a single ingredient.
 *
 * Score scale (0–10):
 *   9-10 → Beneficial  (whole grains, seeds, natural spices)
 *   7-8  → Neutral-Good (healthy oils, unrefined salt, milk)
 *   5-6  → Neutral      (starch, emulsifiers, natural flavors)
 *   3-4  → Caution      (refined sugar, maida, palm oil)
 *   1-2  → Harmful      (INS flavor enhancers, artificial colors, trans fat)
 *   0    → Banned/Severely flagged by FSSAI
 */
public class IngredientScore implements Serializable {

    public enum Category {
        BENEFICIAL,    // Green — good for health
        NEUTRAL,       // Yellow — neither good nor bad
        CAUTION,       // Orange — consume in moderation
        HARMFUL        // Red — avoid
    }

    private final String name;         // Display name, e.g. "Palm Oil"
    private final int score;           // 0–10
    private final Category category;
    private final String reason;       // Short FSSAI/nutritional justification
    private final String icon;         // Emoji indicator

    public IngredientScore(String name, int score, Category category, String reason) {
        this.name = name;
        this.score = score;
        this.category = category;
        this.reason = reason;
        this.icon = resolveIcon(category);
    }

    private static String resolveIcon(Category cat) {
        switch (cat) {
            case BENEFICIAL:  return "🟢";
            case NEUTRAL:     return "🟡";
            case CAUTION:     return "🟠";
            case HARMFUL:     return "🔴";
            default:          return "⚪";
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()     { return name; }
    public int getScore()       { return score; }
    public Category getCategory() { return category; }
    public String getReason()   { return reason; }
    public String getIcon()     { return icon; }

    /** Returns the score as a formatted string, e.g. "7/10" */
    public String getScoreLabel() { return score + "/10"; }

    /**
     * Returns an Android color int matching the category.
     * Safe to use with View.setBackgroundColor().
     */
    public int getCategoryColor() {
        switch (category) {
            case BENEFICIAL: return 0xFF4CAF50; // Green
            case NEUTRAL:    return 0xFFFFC107; // Amber
            case CAUTION:    return 0xFFFF9800; // Orange
            case HARMFUL:    return 0xFFF44336; // Red
            default:         return 0xFF9E9E9E;
        }
    }
}
