package com.example.neutri_score;

import java.io.Serializable;
import java.util.List;

public class ProductModel implements Serializable {
    private String id;
    private String name;
    private String brand;
    private String category;
    private String grade;
    private int gradeColor;
    private int sodiumVal;
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

    private List<IngredientScore> ingredientBreakdown;
    private int healthPercent;
    private String scoringVerdict;
    private int sodiumMgPer100g;

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

    public void applyScoring(List<IngredientScore> breakdown, int healthPercent,
                             String grade, String verdict, int sodiumMgPer100g) {
        this.ingredientBreakdown = breakdown;
        this.healthPercent = healthPercent;
        this.grade = grade;
        this.gradeColor = NutriScoringEngine.gradeColor(grade);
        this.scoringVerdict = verdict;
        this.sodiumMgPer100g = sodiumMgPer100g;
    }

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

    public List<IngredientScore> getIngredientBreakdown() { return ingredientBreakdown; }
    public int getHealthPercent()      { return healthPercent; }
    public String getScoringVerdict()  { return scoringVerdict; }
    public int getSodiumMgPer100g()    { return sodiumMgPer100g; }
}
