package com.example.neutri_score;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductRepository {

    private static ProductRepository instance;
    private final Map<String, ProductModel> productsMap = new HashMap<>();
    private final List<ScanLogItem> scanHistory = new ArrayList<>();

    private ProductRepository() {
        initPresets();
        initHistory();
    }

    public static synchronized ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }

    private void initPresets() {

        ProductModel lays = new ProductModel(
                "lays",
                "LAYS INDIA'S MAGIC MASALA",
                "PEPSICO INDIA",
                "POTATO CHIPS",
                "D", 0xFFF44336,
                85, 75, 35, 20,
                "780 mg (VERY HIGH)", "11.5 g (HIGH)", "3.2 g (MODERATE)", "2.1 g (LOW)",
                "Potatoes, Palmolein Oil, Salt, Sugar, Spices, INS 627 (Disodium Guanylate), INS 631 (Disodium Inosinate), Acidity Regulator (INS 330), Natural Flavour",
                Arrays.asList("HIDDEN PALM OIL (PALMOLEIN)", "INS 627 + 631 FLAVOR ENHANCER", "HIGH SODIUM (780mg/100g)"),
                "TAALI ROASTED PUDINA MAKHANA",
                "70% less fat, popped foxnuts in Pudina spices with zero palm oil. High in plant protein and fiber."
        );
        applyEngineScoring(lays, 780);
        productsMap.put("lays", lays);

        ProductModel kurkure = new ProductModel(
                "kurkure",
                "KURKURE MASALA MUNCH",
                "PEPSICO INDIA",
                "EXTRUDED CORN SNACK",
                "E", 0xFFF44336,
                95, 88, 25, 15,
                "890 mg (CRITICAL)", "14.2 g (HIGH)", "2.5 g (LOW)", "1.8 g (LOW)",
                "Rice Meal, Corn Meal, Palmolein Oil, Salt, Sugar, Spices, INS 627, INS 631, Tartaric Acid (INS 334), Colour (INS 110), Artificial Flavour",
                Arrays.asList("PALMOLEIN FAT BASE", "INS 627/631 FLAVOR SPIKE", "CRITICAL SODIUM LEVEL", "ARTIFICIAL COLOR INS 110"),
                "SOULFULL RAGI BITES CHIPS",
                "Baked multi-grain snack with 50%+ ancient grain Ragi. Zero palm oil, high calcium."
        );
        applyEngineScoring(kurkure, 890);
        productsMap.put("kurkure", kurkure);

        ProductModel maggi = new ProductModel(
                "maggi",
                "MAGGI 2-MINUTE NOODLES",
                "NESTLÉ INDIA",
                "INSTANT NOODLES",
                "D", 0xFFFF5722,
                90, 80, 20, 40,
                "920 mg (VERY HIGH)", "12.8 g (HIGH)", "2.1 g (LOW)", "4.2 g (MODERATE)",
                "Refined Wheat Flour (Maida), Palm Oil, Salt, Wheat Gluten, Sugar, INS 508, INS 412, INS 500(i), Spices",
                Arrays.asList("REFINED MAIDA & PALM OIL", "EXCESS SODIUM PER SERVING", "SYNTHETIC BINDERS"),
                "SLURRP FARM MILLET NOODLES",
                "100% Whole Grain Foxtail Millet & Oats Noodle cake with sundried spices and zero refined palm oil."
        );
        applyEngineScoring(maggi, 920);
        productsMap.put("maggi", maggi);

        ProductModel sprite = new ProductModel(
                "sprite",
                "SPRITE LEMON-LIME DRINK",
                "THE COCA-COLA COMPANY",
                "CARBONATED SOFT DRINK",
                "C", 0xFFFFC107,
                20, 10, 85, 5,
                "45 mg (LOW)", "0.0 g (ZERO)", "10.6 g (HIGH)", "0.0 g (ZERO)",
                "Carbonated Water, Sugar, Acidity Regulators (330, 331(iii)), Preservative (211), Flavours (Natural Flavours)",
                Arrays.asList("HIGH ADDED SUGARS (10.6g/100ml)", "ACIDITY REGULATORS (INS 330, 331)", "SODIUM BENZOATE PRESERVATIVE (INS 211)"),
                "HIKER SPARKLING WATER (LEMON)",
                "Zero calorie carbonated water infused with natural lemon oil, zero sugar, zero synthetic preservatives."
        );
        applyEngineScoring(sprite, 45);
        productsMap.put("sprite", sprite);
    }

    private void applyEngineScoring(ProductModel model, int sodiumMgPer100g) {
        List<IngredientScore> breakdown = NutriScoringEngine.parseAndScore(model.getAdditives());
        int healthPercent = NutriScoringEngine.computeHealthPercent(breakdown);
        boolean hasTransFat = NutriScoringEngine.detectTransFat(breakdown);
        int insCount = NutriScoringEngine.countInsAdditives(breakdown);
        String grade = NutriScoringEngine.computeGrade(healthPercent, sodiumMgPer100g, hasTransFat, insCount);
        String verdict = NutriScoringEngine.buildVerdict(breakdown);

        model.applyScoring(breakdown, healthPercent, grade, verdict, sodiumMgPer100g);
    }

    private void initHistory() {

    }

    public ProductModel getProduct(String key) {
        return productsMap.containsKey(key) ? productsMap.get(key) : productsMap.get("lays");
    }

    public List<ScanLogItem> getScanHistory() { return new ArrayList<>(scanHistory); }

    public void clearHistory() { scanHistory.clear(); }

    public void addScanLog(ProductModel model) {
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        String flag = (model.getRiskFlags() != null && !model.getRiskFlags().isEmpty())
                ? model.getRiskFlags().get(0) : "Scanned Item";
        scanHistory.add(0, new ScanLogItem(model.getName(), model.getGrade(), time, flag, model));
    }

    public ProductModel analyzeRawIngredients(String input) {
        return analyzeRawIngredients(null, input);
    }

    public ProductModel analyzeRawIngredients(String customName, String input) {

        String ingredientsOnly = cleanOcrTextToIngredientsOnly(input);
        if (ingredientsOnly.isEmpty()) ingredientsOnly = input;

        int estimatedSodiumMg = extractEstimatedSodium(ingredientsOnly);

        String productName = customName != null ? customName.trim() : "";
        if (productName.isEmpty()) {
            productName = inferProductNameFromText(input);
        }

        ProductModel customModel = new ProductModel(
                "custom_" + System.currentTimeMillis(),
                productName.toUpperCase(Locale.ROOT),
                "DECODED PRODUCT",
                "PACKAGED FOOD",
                "C", 0xFFFFC107,
                estimatedSodiumMg > 600 ? 80 : 50,
                ingredientsOnly.toLowerCase().contains("palm") ? 70 : 40,
                ingredientsOnly.toLowerCase().contains("sugar") ? 50 : 25,
                30,
                estimatedSodiumMg + " mg (ESTIMATED)",
                ingredientsOnly.toLowerCase().contains("palm") ? "HIGH (palm-based)" : "MODERATE",
                ingredientsOnly.toLowerCase().contains("sugar") ? "ADDED SUGARS" : "LOW",
                "3.0 g (STANDARD)",
                ingredientsOnly,
                new ArrayList<>(),
                "TAALI ROASTED PUDINA MAKHANA",
                "Clean popped foxnuts with zero palm oil or synthetic INS flavor enhancers."
        );

        applyEngineScoring(customModel, estimatedSodiumMg);

        List<String> flags = new ArrayList<>();
        if (customModel.getIngredientBreakdown() != null) {
            for (IngredientScore s : customModel.getIngredientBreakdown()) {
                if (s.getCategory() == IngredientScore.Category.HARMFUL
                        || s.getCategory() == IngredientScore.Category.CAUTION) {
                    String flag = s.getName().toUpperCase(Locale.ROOT);
                    if (!flags.contains(flag)) flags.add(flag);
                    if (flags.size() >= 4) break;
                }
            }
        }

        ProductModel finalModel = new ProductModel(
                customModel.getId(), customModel.getName(), customModel.getBrand(),
                customModel.getCategory(), customModel.getGrade(), customModel.getGradeColor(),
                customModel.getSodiumVal(), customModel.getFatVal(), customModel.getSugarVal(),
                customModel.getProteinVal(), customModel.getSodiumText(), customModel.getFatText(),
                customModel.getSugarText(), customModel.getProteinText(), input, flags,
                customModel.getSwapName(), customModel.getSwapDesc()
        );
        finalModel.applyScoring(
                customModel.getIngredientBreakdown(),
                customModel.getHealthPercent(),
                customModel.getGrade(),
                customModel.getScoringVerdict(),
                estimatedSodiumMg
        );

        addScanLog(finalModel);
        return finalModel;
    }

    private int extractEstimatedSodium(String lower) {
        lower = lower.toLowerCase(Locale.ROOT);
        if (lower.contains("salt") && lower.contains("sodium")) return 750;
        if (lower.contains("sodium") || lower.contains("salt"))  return 500;
        return 200;
    }

    private String inferProductNameFromText(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "DECODED INGREDIENT ITEM";
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("bingo") && lower.contains("angles")) return "BINGO! MAD ANGLES";
        if (lower.contains("bingo")) return "BINGO! PACKAGED SNACK";
        if (lower.contains("lays") || lower.contains("lay's")) return "LAY'S POTATO CHIPS";
        if (lower.contains("kurkure")) return "KURKURE MASALA MUNCH";
        if (lower.contains("maggi")) return "MAGGI INSTANT NOODLES";
        if (lower.contains("amul")) return "AMUL DAIRY PRODUCT";
        if (lower.contains("haldiram")) return "HALDIRAM'S NAMKEEN";

        String[] lines = raw.split("\n");
        String firstLine = lines[0].trim();
        if (firstLine.contains(":")) {
            String titleCandidate = firstLine.split(":")[0].trim();
            if (titleCandidate.length() >= 3 && titleCandidate.length() <= 40) {
                return titleCandidate;
            }
        } else if (firstLine.length() >= 3 && firstLine.length() <= 35 && !firstLine.toLowerCase().contains("ingredients")) {
            return firstLine;
        }

        return "DECODED INGREDIENT ITEM";
    }

    public String cleanOcrTextToIngredientsOnly(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";

        String upper = raw.toUpperCase(Locale.ROOT);
        String cleaned = raw;

        int idx = upper.indexOf("INGREDIENTS:");
        if (idx == -1) idx = upper.indexOf("INGREDIENTS :");
        if (idx == -1) idx = upper.indexOf("INGREDIENTS");
        if (idx == -1) idx = upper.indexOf("CONTAINS:");
        if (idx == -1) idx = upper.indexOf("SAMAGRI:");

        if (idx != -1) {
            cleaned = raw.substring(idx);

            cleaned = cleaned.replaceAll("(?i)^(INGREDIENTS|CONTAINS|SAMAGRI)\\s*[:\\-]", "").trim();
        }

        String[] footers = {
            "NUTRITIONAL INFORMATION", "NUTRITION FACTS", "MFG", "EXP", "BATCH",
            "NET WT", "NET WEIGHT", "MRP", "FSSAI", "LIC NO", "MARKETED BY",
            "MANUFACTURED BY", "CUSTOMER CARE", "STORE IN", "KEEP IN", "MADE IN INDIA"
        };
        String cleanedUpper = cleaned.toUpperCase(Locale.ROOT);
        for (String footer : footers) {
            int stopIdx = cleanedUpper.indexOf(footer);
            if (stopIdx > 15) {
                cleaned = cleaned.substring(0, stopIdx).trim();
                cleanedUpper = cleaned.toUpperCase(Locale.ROOT);
            }
        }

        String[] lines = cleaned.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            String lUpper = l.toUpperCase(Locale.ROOT);

            if (lUpper.contains("LIC NO") || lUpper.contains("FSSAI") || lUpper.contains("CUSTOMER CARE")
                    || lUpper.contains("RS.") || lUpper.contains("MRP") || lUpper.contains("WWW.")
                    || lUpper.contains("@") || lUpper.matches(".*\\d{10}.*")) {
                continue;
            }
            sb.append(l).append(" ");
        }

        String result = sb.toString().trim();
        return result.isEmpty() ? raw.trim() : result;
    }
}
