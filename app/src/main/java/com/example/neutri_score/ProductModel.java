package com.example.neutri_score;

import java.io.Serializable;
import java.util.List;

/**
 * Model representing a scanned/analyzed food product.
 * Extended with FSSAI-inspired ingredient breakdown and health scoring.
 */
public class ProductModel implements Serializable {
    private String id;
    private String name;
    private String brand;
    private String category;
    private String grade;                          // "A", "B", "C", "D", "E"
    private int gradeColor;
    private int sodiumVal;                         // 0-100 progress bar value
    private int fatVal;
    private int sugarVal;
    private int proteinVal;
    private String sodiumText;
    private String fatText;
    private String sugarText;
    private String proteinText;
    private String additives;
    private List<String> riskFlags;
    private String swapName;
    private String swapDesc;

    // ── NEW: Scoring fields ──────────────────────────────────────────────────
    private List<IngredientScore> ingredientBreakdown; // Per-ingredient FSSAI scores
    private int healthPercent;                         // 0–100 overall health %
    private String scoringVerdict;                     // e.g. "3 Harmful · 2 Caution"
    private int sodiumMgPer100g;                       // Raw mg value for FSSAI penalty

    public ProductModel(String id, String name, String brand, String category, String grade,
                        int gradeColor, int sodiumVal, int fatVal, int sugarVal, int proteinVal,
                        String sodiumText, String fatText, String sugarText, String proteinText,
                        String additives, List<String> riskFlags, String swapName, String swapDesc) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.grade = grade;
        this.gradeColor = gradeColor;
        this.sodiumVal = sodiumVal;
        this.fatVal = fatVal;
        this.sugarVal = sugarVal;
        this.proteinVal = proteinVal;
        this.sodiumText = sodiumText;
        this.fatText = fatText;
        this.sugarText = sugarText;
        this.proteinText = proteinText;
        this.additives = additives;
        this.riskFlags = riskFlags;
        this.swapName = swapName;
        this.swapDesc = swapDesc;
    }

    // ── Scoring injection (called by ProductRepository after scoring) ─────────
    public void applyScoring(List<IngredientScore> breakdown, int healthPercent,
                             String grade, String verdict, int sodiumMgPer100g) {
        this.ingredientBreakdown = breakdown;
        this.healthPercent = healthPercent;
        this.grade = grade;
        this.gradeColor = NutriScoringEngine.gradeColor(grade);
        this.scoringVerdict = verdict;
        this.sodiumMgPer100g = sodiumMgPer100g;
    }

    // ── Existing getters ─────────────────────────────────────────────────────
    public String getId()              { return id; }
    public String getName()            { return name; }
    public String getBrand()           { return brand; }
    public String getCategory()        { return category; }
    public String getGrade()           { return grade; }
    public int getGradeColor()         { return gradeColor; }
    public int getSodiumVal()          { return sodiumVal; }
    public int getFatVal()             { return fatVal; }
    public int getSugarVal()           { return sugarVal; }
    public int getProteinVal()         { return proteinVal; }
    public String getSodiumText()      { return sodiumText; }
    public String getFatText()         { return fatText; }
    public String getSugarText()       { return sugarText; }
    public String getProteinText()     { return proteinText; }
    public String getAdditives()       { return additives; }
    public List<String> getRiskFlags() { return riskFlags; }
    public String getSwapName()        { return swapName; }
    public String getSwapDesc()        { return swapDesc; }

    // ── New scoring getters ──────────────────────────────────────────────────
    public List<IngredientScore> getIngredientBreakdown() { return ingredientBreakdown; }
    public int getHealthPercent()      { return healthPercent; }
    public String getScoringVerdict()  { return scoringVerdict; }
    public int getSodiumMgPer100g()    { return sodiumMgPer100g; }
}
