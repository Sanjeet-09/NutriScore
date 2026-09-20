package com.example.neutri_score;

import java.io.Serializable;

public class IngredientScore implements Serializable {

    public enum Category {
        BENEFICIAL,
        NEUTRAL,
        CAUTION,
        HARMFUL
    }

    private final String name;
    private final int score;
    private final Category category;
    private final String reason;
    private final String icon;

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

    public String getName()     { return name; }
    public int getScore()       { return score; }
    public Category getCategory() { return category; }
    public String getReason()   { return reason; }
    public String getIcon()     { return icon; }

    public String getScoreLabel() { return score + "/10"; }

    public int getCategoryColor() {
        switch (category) {
            case BENEFICIAL: return 0xFF4CAF50;
            case NEUTRAL:    return 0xFFFFC107;
            case CAUTION:    return 0xFFFF9800;
            case HARMFUL:    return 0xFFF44336;
            default:         return 0xFF9E9E9E;
        }
    }
}
