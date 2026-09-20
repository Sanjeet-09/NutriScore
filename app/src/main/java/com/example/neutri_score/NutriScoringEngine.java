package com.example.neutri_score;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NutriScoringEngine {

    private static final Object[][] INGREDIENT_DICT = {

        {"makhana",        10, 3, "Foxnut: Low calorie, high protein, FSSAI-approved whole food"},
        {"foxnut",         10, 3, "Whole foxnut — rich in plant protein and magnesium"},
        {"almonds",         9, 3, "Unsalted almonds — healthy monounsaturated fats, Vitamin E"},
        {"cashew",          9, 3, "Cashew nuts — high in zinc and heart-healthy unsaturated fat"},
        {"oats",            9, 3, "Whole grain oats — high soluble fiber (beta-glucan)"},
        {"ragi",            9, 3, "Finger millet — high calcium, iron, fiber; FSSAI-endorsed grain"},
        {"foxtail millet",  9, 3, "Ancient grain: low GI, high fiber, zero gluten"},
        {"jowar",           9, 3, "Sorghum millet — gluten-free whole grain, high fiber"},
        {"bajra",           9, 3, "Pearl millet — iron-rich, high in magnesium and fiber"},
        {"whole wheat",     8, 3, "Whole grain wheat — retains bran and germ, high fiber"},
        {"atta",            8, 3, "Whole wheat flour — better glycemic index than maida"},
        {"flaxseed",        9, 3, "Rich in Omega-3 ALA and lignans — anti-inflammatory"},
        {"chia",            9, 3, "High in Omega-3, fiber and plant protein"},
        {"sunflower seed",  8, 3, "Rich in Vitamin E and healthy polyunsaturated fat"},
        {"pumpkin seed",    8, 3, "High in zinc, magnesium, and plant-based protein"},
        {"sesame",          8, 3, "Good source of calcium and healthy fats"},
        {"olive oil",       8, 3, "Rich in monounsaturated fats, anti-inflammatory"},
        {"coconut oil",     7, 3, "Medium-chain triglycerides; use in moderation"},
        {"groundnut oil",   7, 3, "Peanut oil — acceptable unsaturated fat profile"},
        {"pudina",          9, 3, "Peppermint / Pudina — natural flavoring, antioxidant"},
        {"black pepper",    9, 3, "Natural spice — no additives, anti-inflammatory"},
        {"turmeric",        9, 3, "Curcumin-rich natural spice — FSSAI-approved coloring"},
        {"coriander",       9, 3, "Natural spice — antioxidant, digestive aid"},
        {"cumin",           9, 3, "Jeera — natural flavor, aids digestion"},
        {"ginger",          9, 3, "Natural anti-inflammatory spice"},
        {"amchur",          8, 3, "Dry mango powder — natural souring agent"},
        {"black salt",      7, 2, "Kala namak — lower sodium than table salt, mineral rich"},
        {"rock salt",       7, 2, "Unrefined salt — trace minerals retained"},
        {"protein",         8, 3, "Protein fortification — beneficial macro"},
        {"fiber",           8, 3, "Dietary fiber — supports gut health"},
        {"vitamin",         8, 3, "Added vitamin fortification — beneficial"},
        {"calcium",         8, 3, "Mineral fortification — bone health"},
        {"iron fortified",  8, 3, "Iron fortification — addresses Indian deficiency gap"},

        {"rice flour",      6, 2, "Refined starch — moderate glycemic index"},
        {"corn flour",      6, 2, "Refined starch — moderate glycemic index"},
        {"corn starch",     5, 2, "Thickener — neutral, used in small quantity"},
        {"tapioca starch",  5, 2, "Starch thickener — neutral in small amounts"},
        {"milk solids",     6, 2, "Natural dairy ingredient — source of calcium"},
        {"butter",          6, 2, "Saturated fat — acceptable in small quantity"},
        {"cream",           6, 2, "Dairy fat — moderate in small quantities"},
        {"whey",            6, 2, "Dairy protein — generally beneficial"},
        {"natural flavor",  5, 2, "FSSAI-permitted natural flavoring substance"},
        {"spices",          7, 3, "Natural spice blend — generally beneficial"},
        {"acidity regulator", 5, 2, "Permitted food acid (e.g. citric acid) — safe in limits"},
        {"ins 330",         5, 2, "Citric acid — natural food acid, generally safe"},
        {"ins 331",         5, 2, "Sodium citrate — safe emulsifier"},
        {"ins 322",         5, 2, "Lecithin — natural emulsifier from soy"},
        {"ins 471",         5, 2, "Mono/di-glycerides — common emulsifier, safe in limits"},
        {"ins 500",         5, 2, "Sodium bicarbonate — leavening agent, safe"},
        {"ins 412",         5, 2, "Guar gum — plant-based thickener, safe"},
        {"ins 415",         5, 2, "Xanthan gum — fermented thickener, safe"},
        {"ins 401",         5, 2, "Sodium alginate — seaweed extract, safe"},
        {"salt",            5, 2, "Table salt — necessary but watch total sodium per 100g"},
        {"sugar",           4, 1, "Refined sugar — empty calories, high glycemic index"},
        {"glucose",         4, 1, "Simple sugar — quickly raises blood glucose"},
        {"dextrose",        4, 1, "Simple sugar — raises blood glucose"},
        {"maltodextrin",    4, 1, "Highly processed starch — high GI, minimal nutrients"},

        {"palm oil",        2, 1, "High saturated fat; FSSAI flags palm oil in snacks"},
        {"palmolein",       2, 1, "Fractionated palm oil — high saturated fat content"},
        {"palm olein",      2, 1, "Fractionated palm oil — high saturated fat content"},
        {"maida",           3, 1, "Refined wheat flour — high GI, stripped of nutrients"},
        {"refined wheat flour", 3, 1, "Refined flour (maida) — high GI, low fiber"},
        {"hydrogenated",    1, 0, "Hydrogenated fat contains trans fats — FSSAI regulated"},
        {"vanaspati",       1, 0, "Vegetable ghee — contains trans fats, FSSAI restricted"},
        {"high fructose",   2, 1, "HFCS — strongly linked to obesity and metabolic syndrome"},
        {"fructose syrup",  2, 1, "High-fructose corn syrup — excess fructose"},
        {"invert sugar",    3, 1, "Refined sugar mixture — similar risks to white sugar"},
        {"golden syrup",    3, 1, "Refined sugar syrup — high GI"},
        {"sodium",          4, 1, "Sodium compound — watch cumulative sodium per 100g"},
        {"msg",             3, 1, "Monosodium glutamate — FSSAI-permitted but overconsumption debated"},
        {"monosodium glutamate", 3, 1, "MSG — FSSAI-permitted; sensitivity reported in some individuals"},

        {"ins 621",         2, 0, "MSG — FSSAI-permitted but flagged by FSSAI for children's food"},
        {"ins 627",         1, 0, "Disodium Guanylate — synthetic flavor enhancer; not allowed in infant food"},
        {"ins 631",         1, 0, "Disodium Inosinate — synthetic flavor enhancer; potentiates MSG effect"},
        {"ins 635",         1, 0, "Disodium Ribonucleotide — synthetic flavor enhancer combo"},
        {"ins 102",         1, 0, "Tartrazine (Yellow 5) — synthetic azo dye; FSSAI restricted"},
        {"ins 110",         1, 0, "Sunset Yellow — synthetic dye; FSSAI restricted"},
        {"ins 124",         1, 0, "Ponceau 4R — synthetic dye; European countries banned"},
        {"ins 129",         1, 0, "Allura Red — synthetic azo dye; hyperactivity link in children"},
        {"ins 211",         1, 0, "Sodium Benzoate — preservative; reacts with Vitamin C to form benzene"},
        {"ins 220",         2, 0, "Sulphur dioxide — preservative; triggers asthma in sensitive people"},
        {"ins 249",         1, 0, "Potassium Nitrite — curing agent; potentially carcinogenic at high doses"},
        {"ins 250",         1, 0, "Sodium Nitrite — curing agent; WHO classifies processed meat as Group 1 carcinogen"},
        {"trans fat",       0, 0, "Trans fat — FSSAI mandates <0.2g/100g; strongly linked to heart disease"},
        {"partially hydrogenated", 0, 0, "Source of trans fat — FSSAI restricted"},
        {"artificial color",  1, 0, "Synthetic food color — FSSAI-regulated; behavioral effects reported"},
        {"artificial flavour", 2, 0, "Synthetic flavoring — FSSAI-permitted but nutritionally zero"},
        {"permitted preservative", 2, 1, "Chemical preservative — safe in declared limits"},
        {"bha",             1, 0, "Butylated hydroxyanisole — synthetic antioxidant; potential carcinogen at high doses"},
        {"bht",             1, 0, "Butylated hydroxytoluene — synthetic antioxidant; FSSAI regulated"},
    };

    public static List<IngredientScore> parseAndScore(String ingredientsText) {
        List<IngredientScore> results = new ArrayList<>();
        if (ingredientsText == null || ingredientsText.trim().isEmpty()) return results;

        String[] tokens = ingredientsText.split("[,;.]+");

        for (String token : tokens) {
            String cleaned = token.trim();
            if (cleaned.isEmpty()) continue;

            IngredientScore scored = matchIngredient(cleaned);
            results.add(scored);
        }

        return results;
    }

    public static int computeHealthPercent(List<IngredientScore> scores) {
        if (scores == null || scores.isEmpty()) return 50;

        double weightedSum = 0;
        double totalWeight = 0;
        int n = scores.size();

        for (int i = 0; i < n; i++) {

            double weight = (n - i);
            weightedSum += scores.get(i).getScore() * weight;
            totalWeight += weight;
        }

        double avg = weightedSum / totalWeight;
        return (int) Math.round((avg / 10.0) * 100);
    }

    public static String computeGrade(int healthPercent, int sodiumMg,
                                      boolean hasTransFat, int insCount) {
        String grade = percentToGrade(healthPercent);

        if (hasTransFat) {
            grade = "E";
            return grade;
        }

        if (sodiumMg > 800) {
            grade = worstGrade(grade, "E");
        }

        else if (sodiumMg > 600) {
            grade = worstGrade(grade, "D");
        }

        if (insCount >= 3) {
            grade = dropOneGrade(grade);
        }

        return grade;
    }

    public static String buildVerdict(List<IngredientScore> scores) {
        int harmful = 0, caution = 0, neutral = 0, beneficial = 0;
        for (IngredientScore s : scores) {
            switch (s.getCategory()) {
                case HARMFUL:    harmful++;    break;
                case CAUTION:    caution++;    break;
                case NEUTRAL:    neutral++;    break;
                case BENEFICIAL: beneficial++; break;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (harmful > 0)    sb.append(harmful).append(" Harmful");
        if (caution > 0)    { if (sb.length() > 0) sb.append(" · "); sb.append(caution).append(" Caution"); }
        if (neutral > 0)    { if (sb.length() > 0) sb.append(" · "); sb.append(neutral).append(" Neutral"); }
        if (beneficial > 0) { if (sb.length() > 0) sb.append(" · "); sb.append(beneficial).append(" Beneficial"); }
        return sb.length() > 0 ? sb.toString() : "No detailed data";
    }

    public static int countInsAdditives(List<IngredientScore> scores) {
        int count = 0;
        for (IngredientScore s : scores) {
            if (s.getName().toLowerCase(Locale.ROOT).startsWith("ins ")) count++;
        }
        return count;
    }

    public static boolean detectTransFat(List<IngredientScore> scores) {
        for (IngredientScore s : scores) {
            String n = s.getName().toLowerCase(Locale.ROOT);
            if (n.contains("trans fat") || n.contains("hydrogenated")
                    || n.contains("vanaspati") || n.contains("partially hydrogenated")) {
                return true;
            }
        }
        return false;
    }

    public static int gradeColor(String grade) {
        switch (grade.toUpperCase(Locale.ROOT)) {
            case "A": return 0xFF4CAF50;
            case "B": return 0xFF8BC34A;
            case "C": return 0xFFFFC107;
            case "D": return 0xFFFF5722;
            default:  return 0xFFF44336;
        }
    }

    public static int gradeBadgeDrawable(String grade) {
        switch (grade.toUpperCase(Locale.ROOT)) {
            case "A": return R.drawable.bg_nutri_tile_a;
            case "B": return R.drawable.bg_nutri_tile_b;
            case "C": return R.drawable.bg_nutri_tile_c;
            case "D": return R.drawable.bg_nutri_tile_d;
            default:  return R.drawable.bg_nutri_tile_e;
        }
    }

    private static IngredientScore matchIngredient(String token) {
        String lower = token.toLowerCase(Locale.ROOT);

        for (Object[] entry : INGREDIENT_DICT) {
            String keyword   = (String) entry[0];
            int    score     = (Integer) entry[1];
            int    catOrdinal = (Integer) entry[2];
            String reason    = (String) entry[3];

            if (lower.contains(keyword)) {
                IngredientScore.Category cat = ordinalToCategory(catOrdinal);

                return new IngredientScore(capitalize(token), score, cat, reason);
            }
        }

        return new IngredientScore(
                capitalize(token), 5, IngredientScore.Category.NEUTRAL,
                "Ingredient not in FSSAI/Nutri-Score database — assumed neutral"
        );
    }

    private static IngredientScore.Category ordinalToCategory(int ord) {
        switch (ord) {
            case 3:  return IngredientScore.Category.BENEFICIAL;
            case 2:  return IngredientScore.Category.NEUTRAL;
            case 1:  return IngredientScore.Category.CAUTION;
            default: return IngredientScore.Category.HARMFUL;
        }
    }

    private static String percentToGrade(int pct) {
        if (pct >= 85) return "A";
        if (pct >= 65) return "B";
        if (pct >= 45) return "C";
        if (pct >= 25) return "D";
        return "E";
    }

    private static String worstGrade(String g1, String g2) {
        return (gradeOrdinal(g1) > gradeOrdinal(g2)) ? g1 : g2;
    }

    private static String dropOneGrade(String grade) {
        switch (grade) {
            case "A": return "B";
            case "B": return "C";
            case "C": return "D";
            case "D": return "E";
            default:  return "E";
        }
    }

    private static int gradeOrdinal(String g) {
        switch (g) { case "A": return 0; case "B": return 1; case "C": return 2; case "D": return 3; default: return 4; }
    }

    private static String capitalize(String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
