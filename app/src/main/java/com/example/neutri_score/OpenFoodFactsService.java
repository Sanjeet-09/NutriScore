package com.example.neutri_score;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Multi-API Product Lookup Engine.
 *
 * Uses a multi-tier waterfall strategy across global and Indian food databases:
 * Tier 1: Open Food Facts Global API v2 (world.openfoodfacts.org)
 * Tier 2: Open Food Facts .net Mirror (world.openfoodfacts.net)
 * Tier 3: Open Food Facts India Regional API v2 (in.openfoodfacts.org)
 * Tier 4: Open Food Facts Legacy API v0 (world.openfoodfacts.org/api/v0)
 * Tier 5: UPCitemdb Trial API (api.upcitemdb.com - 500M+ barcodes)
 * Tier 6: Open Food Facts Search Endpoint (cgi/search.pl)
 *
 * Also provides fetchProductByQuery() for live name/brand search (e.g. Bingo Mad Angles, Amul).
 */
public class OpenFoodFactsService {

    private static final String TAG = "MultiApiProductService";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * Attempts to fetch product details across barcode variants and multi-API endpoints.
     *
     * @param rawBarcode  Scanned barcode string
     * @return            Scored ProductModel, or null if not found in any database
     */
    public static ProductModel fetchProduct(String rawBarcode) {
        if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
            return null;
        }

        List<String> variants = generateBarcodeVariants(rawBarcode);

        for (String code : variants) {
            Log.d(TAG, "Trying barcode variant: " + code);

            // Tier 1: Open Food Facts Global v2
            ProductModel p1 = fetchFromOFF("https://world.openfoodfacts.org/api/v2/product/" + code + ".json", code);
            if (p1 != null) return p1;

            // Tier 2: Open Food Facts .net Mirror
            ProductModel p2 = fetchFromOFF("https://world.openfoodfacts.net/api/v2/product/" + code + ".json", code);
            if (p2 != null) return p2;

            // Tier 3: Open Food Facts India Regional Endpoint
            ProductModel p3 = fetchFromOFF("https://in.openfoodfacts.org/api/v2/product/" + code + ".json", code);
            if (p3 != null) return p3;

            // Tier 4: Open Food Facts Legacy v0
            ProductModel p4 = fetchFromOFF("https://world.openfoodfacts.org/api/v0/product/" + code + ".json", code);
            if (p4 != null) return p4;

            // Tier 5: UPCitemdb API
            ProductModel p5 = fetchFromUpcItemDb(code);
            if (p5 != null) return p5;

            // Tier 6: Open Food Facts Search API
            ProductModel p6 = fetchFromOFFSearch(code);
            if (p6 != null) return p6;
        }

        Log.w(TAG, "All API tiers exhausted for barcode: " + rawBarcode);
        return null;
    }

    /**
     * Searches Open Food Facts live databases by product name or brand (e.g. "Bingo Mad Angles", "ITC Bingo", "Amul").
     *
     * @param query Product or brand name string
     * @return Scored ProductModel if found, or null if no matching product entry
     */
    public static ProductModel fetchProductByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String cleanedQuery = query.trim();
        Log.i(TAG, "Executing Live Search for product/brand query: " + cleanedQuery);

        String encodedQuery = Uri.encode(cleanedQuery);

        // Try global search endpoint
        ProductModel p1 = searchOFFByTerm("https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedQuery + "&search_simple=1&action=process&json=1");
        if (p1 != null) return p1;

        // Try India search endpoint
        ProductModel p2 = searchOFFByTerm("https://in.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedQuery + "&search_simple=1&action=process&json=1");
        if (p2 != null) return p2;

        return null;
    }

    private static ProductModel searchOFFByTerm(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "NeutriScore-Android/1.0 (contact@neutriscore.app)")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            String json = response.body().string();
            JSONObject root = new JSONObject(json);

            JSONArray products = root.optJSONArray("products");
            if (products == null || products.length() == 0) {
                return null;
            }

            // Find best matching product that has ingredients or product name
            for (int i = 0; i < products.length(); i++) {
                JSONObject p = products.getJSONObject(i);
                String code = p.optString("code", "query_item");

                JSONObject wrapper = new JSONObject();
                wrapper.put("status", 1);
                wrapper.put("product", p);

                ProductModel model = parseOFFJson(code, wrapper.toString());
                if (model != null) {
                    Log.i(TAG, "Search API matched product: " + model.getName());
                    return model;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Search API error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Generates barcode format variations (e.g. handling leading zeros for EAN-13 vs UPC-A vs GTIN-14).
     */
    private static List<String> generateBarcodeVariants(String raw) {
        List<String> list = new ArrayList<>();
        String cleanCode = raw.trim();
        list.add(cleanCode);

        // Variant without leading zeros
        if (cleanCode.startsWith("0")) {
            String stripped = cleanCode.replaceAll("^0+", "");
            if (!stripped.isEmpty() && !list.contains(stripped)) {
                list.add(stripped);
            }
        }

        // 12-digit UPC padded to 13-digit EAN-13
        if (cleanCode.length() == 12) {
            String padded = "0" + cleanCode;
            if (!list.contains(padded)) {
                list.add(padded);
            }
        }

        return list;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Open Food Facts Parsing
    // ─────────────────────────────────────────────────────────────────────────

    private static ProductModel fetchFromOFF(String urlString, String barcode) {
        String url = urlString;

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "NeutriScore-Android/1.0 (contact@neutriscore.app)")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            String json = response.body().string();
            return parseOFFJson(barcode, json);

        } catch (IOException e) {
            Log.e(TAG, "OFF fetch error for " + url + ": " + e.getMessage());
            return null;
        }
    }

    private static ProductModel parseOFFJson(String barcode, String json) {
        try {
            JSONObject root = new JSONObject(json);
            if (root.optInt("status", 0) == 0) {
                return null;
            }

            JSONObject p = root.getJSONObject("product");

            // ── Enhanced Product Name & Brand Extraction ──────────────────────
            String name = p.optString("product_name", "");
            if (name.trim().isEmpty()) name = p.optString("product_name_en", "");
            if (name.trim().isEmpty()) name = p.optString("product_name_in", "");
            if (name.trim().isEmpty()) name = p.optString("product_name_hi", "");
            if (name.trim().isEmpty()) name = p.optString("generic_name", "");
            if (name.trim().isEmpty()) name = p.optString("generic_name_en", "");
            if (name.trim().isEmpty()) name = p.optString("abbreviated_product_name", "");
            if (name.trim().isEmpty()) name = p.optString("title", "");
            if (name.trim().isEmpty()) name = p.optString("product_name_local", "");

            // Extract brand (filter out 1-char typos like "a" or "A")
            String brand = clean(p.optString("brands", "")).toUpperCase(Locale.ROOT);
            if (brand.length() <= 1) brand = clean(p.optString("brand_owner", "")).toUpperCase(Locale.ROOT);
            if (brand.length() <= 1) brand = clean(p.optString("brand_owner_imported", "")).toUpperCase(Locale.ROOT);
            if (brand.length() <= 1) {
                JSONArray bTags = p.optJSONArray("brands_tags");
                if (bTags != null && bTags.length() > 0) {
                    brand = clean(bTags.optString(0, "")).replaceAll("en:", "").replaceAll("-", " ").toUpperCase(Locale.ROOT);
                }
            }
            if (brand.length() <= 1) brand = "PACKAGED FOOD";

            String category = extractCategory(p.optString("categories", "PACKAGED FOOD"));
            String ingredients = clean(p.optString("ingredients_text", ""));
            if (ingredients.isEmpty()) ingredients = "No ingredients data available.";

            // If product name is missing or generic fallback, check tag arrays
            if (name.trim().isEmpty() || name.startsWith("PACKAGED FOOD ITEM")) {
                JSONArray pTags = p.optJSONArray("product_name_tags");
                if (pTags != null && pTags.length() > 0) {
                    name = clean(pTags.optString(0, "")).replaceAll("en:", "").replaceAll("-", " ").toUpperCase(Locale.ROOT);
                } else {
                    JSONArray cTags = p.optJSONArray("categories_tags");
                    if (cTags != null && cTags.length() > 0) {
                        String catTag = cTags.optString(cTags.length() - 1, "");
                        name = clean(catTag).replaceAll("en:", "").replaceAll("-", " ").toUpperCase(Locale.ROOT);
                    }
                }
            }

            // Universal multi-category ingredient & metadata inference engine
            if (name.trim().isEmpty() || name.startsWith("PACKAGED FOOD ITEM") || name.equals("PACKAGED FOOD")) {
                name = inferUniversalProductName(brand, category, ingredients, barcode);
            } else {
                name = clean(name).toUpperCase(Locale.ROOT);
            }

            String quantity = p.optString("quantity", "");
            if (!quantity.isEmpty() && !name.contains(quantity.toUpperCase(Locale.ROOT))) {
                name = name + " (" + quantity.toUpperCase(Locale.ROOT) + ")";
            }

            JSONObject n = p.optJSONObject("nutriments");
            double sodiumPer100g = n != null ? n.optDouble("sodium_100g", 0.0) : 0.0;
            double satFatPer100g = n != null ? n.optDouble("saturated-fat_100g", 0.0) : 0.0;
            double sugarPer100g = n != null ? n.optDouble("sugars_100g", 0.0) : 0.0;
            double proteinPer100g = n != null ? n.optDouble("proteins_100g", 0.0) : 0.0;

            int sodiumMg = (int) Math.round(sodiumPer100g * 1000);
            int sodiumVal = clamp((int) (sodiumMg / 10), 100);
            int fatVal = clamp((int) (satFatPer100g * 5), 100);
            int sugarVal = clamp((int) (sugarPer100g * 2), 100);
            int proteinVal = clamp((int) (proteinPer100g * 4), 100);

            String sodiumText = sodiumMg + " mg " + sodiumLevel(sodiumMg);
            String fatText = satFatPer100g + " g " + fatLevel(satFatPer100g);
            String sugarText = sugarPer100g + " g " + sugarLevel(sugarPer100g);
            String proteinText = proteinPer100g + " g " + proteinLevel(proteinPer100g);

            List<IngredientScore> breakdown = NutriScoringEngine.parseAndScore(ingredients);
            int healthPercent = NutriScoringEngine.computeHealthPercent(breakdown);
            boolean transF = NutriScoringEngine.detectTransFat(breakdown);
            int insCount = NutriScoringEngine.countInsAdditives(breakdown);
            String grade = NutriScoringEngine.computeGrade(healthPercent, sodiumMg, transF, insCount);
            String verdict = NutriScoringEngine.buildVerdict(breakdown);

            List<String> riskFlags = buildRiskFlags(breakdown, sodiumMg, transF, insCount);
            String[] swap = suggestSwap(grade, category);

            ProductModel model = new ProductModel(
                    barcode, name, brand, category,
                    grade, NutriScoringEngine.gradeColor(grade),
                    sodiumVal, fatVal, sugarVal, proteinVal,
                    sodiumText, fatText, sugarText, proteinText,
                    ingredients, riskFlags,
                    swap[0], swap[1]
            );

            model.applyScoring(breakdown, healthPercent, grade, verdict, sodiumMg);
            return model;

        } catch (Exception e) {
            Log.e(TAG, "OFF JSON parse error: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPCitemdb API Parsing
    // ─────────────────────────────────────────────────────────────────────────

    private static ProductModel fetchFromUpcItemDb(String barcode) {
        String url = "https://api.upcitemdb.com/prod/trial/lookup?upc=" + barcode;

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "NeutriScore-Android/1.0 (contact@neutriscore.app)")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            String json = response.body().string();
            JSONObject root = new JSONObject(json);

            if (!"OK".equalsIgnoreCase(root.optString("code")) || root.optInt("total", 0) == 0) {
                return null;
            }

            JSONArray items = root.optJSONArray("items");
            if (items == null || items.length() == 0) {
                return null;
            }

            JSONObject item = items.getJSONObject(0);
            String title = clean(item.optString("title", "Scanned Product")).toUpperCase(Locale.ROOT);
            String brand = clean(item.optString("brand", "Packaged Goods")).toUpperCase(Locale.ROOT);
            String category = extractCategory(item.optString("category", "PACKAGED FOOD"));
            String description = clean(item.optString("description", ""));

            String ingredients = !description.isEmpty() ? description : title;

            List<IngredientScore> breakdown = NutriScoringEngine.parseAndScore(ingredients);
            int healthPercent = NutriScoringEngine.computeHealthPercent(breakdown);
            boolean transF = NutriScoringEngine.detectTransFat(breakdown);
            int insCount = NutriScoringEngine.countInsAdditives(breakdown);

            int estimatedSodiumMg = ingredients.toLowerCase().contains("salt") ? 650 : 250;
            String grade = NutriScoringEngine.computeGrade(healthPercent, estimatedSodiumMg, transF, insCount);
            String verdict = NutriScoringEngine.buildVerdict(breakdown);

            List<String> riskFlags = buildRiskFlags(breakdown, estimatedSodiumMg, transF, insCount);
            String[] swap = suggestSwap(grade, category);

            ProductModel model = new ProductModel(
                    barcode, title, brand, category,
                    grade, NutriScoringEngine.gradeColor(grade),
                    50, 40, 30, 30,
                    estimatedSodiumMg + " mg (ESTIMATED)", "6.0 g (ESTIMATED)", "4.0 g (ESTIMATED)", "3.0 g (STANDARD)",
                    ingredients, riskFlags,
                    swap[0], swap[1]
            );

            model.applyScoring(breakdown, healthPercent, grade, verdict, estimatedSodiumMg);
            Log.i(TAG, "Successfully matched barcode via UPCitemdb API: " + title);
            return model;

        } catch (Exception e) {
            Log.e(TAG, "UPCitemdb error for " + barcode + ": " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OFF Search API Fallback
    // ─────────────────────────────────────────────────────────────────────────

    private static ProductModel fetchFromOFFSearch(String barcode) {
        String url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + barcode + "&search_simple=1&action=process&json=1";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "NeutriScore-Android/1.0 (contact@neutriscore.app)")
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            String json = response.body().string();
            JSONObject root = new JSONObject(json);

            JSONArray products = root.optJSONArray("products");
            if (products == null || products.length() == 0) {
                return null;
            }

            JSONObject p = products.getJSONObject(0);
            JSONObject wrapper = new JSONObject();
            wrapper.put("status", 1);
            wrapper.put("product", p);

            return parseOFFJson(barcode, wrapper.toString());

        } catch (Exception e) {
            Log.e(TAG, "OFF Search fallback error for " + barcode + ": " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static List<String> buildRiskFlags(List<IngredientScore> breakdown,
                                                int sodiumMg, boolean transF, int insCount) {
        List<String> flags = new ArrayList<>();
        if (transF) flags.add("TRANS FAT DETECTED");
        if (sodiumMg > 800) flags.add("SODIUM CRITICAL (" + sodiumMg + "mg/100g)");
        else if (sodiumMg > 600) flags.add("HIGH SODIUM (" + sodiumMg + "mg/100g)");
        if (insCount >= 3) flags.add(insCount + " INS SYNTHETIC ADDITIVES");

        for (IngredientScore s : breakdown) {
            if (s.getCategory() == IngredientScore.Category.HARMFUL) {
                String f = s.getName().toUpperCase(Locale.ROOT);
                if (!flags.contains(f)) flags.add(f);
                if (flags.size() >= 5) break;
            }
        }
        if (flags.isEmpty()) flags.add("NO MAJOR ADDITIVES DETECTED");
        return flags;
    }

    private static String extractCategory(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "PACKAGED FOOD";
        String[] parts = raw.split("[,>]");
        String last = parts[parts.length - 1].trim();
        if (last.contains(":")) last = last.split(":")[1].trim();
        return last.toUpperCase(Locale.ROOT).replaceAll("-", " ");
    }

    private static String[] suggestSwap(String grade, String category) {
        switch (grade) {
            case "A":
                return new String[]{"YOU'RE EATING CLEAN!", "This product already meets FSSAI healthy eating guidelines. Great choice!"};
            case "B":
                return new String[]{"TAALI ROASTED MAKHANA", "Upgrade to foxnuts for even higher plant protein and zero synthetic additives."};
            case "C":
                return new String[]{"SOULFULL RAGI BITES", "Try baked multi-grain snacks — lower sodium, zero palm oil, high ancient grain fiber."};
            case "D":
                return new String[]{"SLURRP FARM MILLET NOODLES", "Replace with whole-grain millet snacks — lower GI, no synthetic flavor enhancers."};
            default:
                return new String[]{"TAALI ROASTED PUDINA MAKHANA", "Clean popped foxnuts — zero palm oil, zero INS additives, high plant protein."};
        }
    }

    private static String sodiumLevel(int mg) {
        if (mg > 800) return "(CRITICAL)";
        if (mg > 600) return "(VERY HIGH)";
        if (mg > 400) return "(HIGH)";
        if (mg > 200) return "(MODERATE)";
        return "(LOW)";
    }

    private static String fatLevel(double g) {
        if (g > 15) return "(VERY HIGH)";
        if (g > 8) return "(HIGH)";
        if (g > 4) return "(MODERATE)";
        return "(LOW)";
    }

    private static String sugarLevel(double g) {
        if (g > 20) return "(VERY HIGH)";
        if (g > 10) return "(HIGH)";
        if (g > 5) return "(MODERATE)";
        return "(LOW)";
    }

    private static String proteinLevel(double g) {
        if (g > 15) return "(HIGH)";
        if (g > 8) return "(MODERATE)";
        if (g > 3) return "(LOW)";
        return "(VERY LOW)";
    }

    private static int clamp(int val, int max) {
        return Math.min(Math.max(val, 0), max);
    }

    private static String clean(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

    /**
     * Universal Product Name inference engine across all food & beverage categories.
     */
    private static String inferUniversalProductName(String brand, String category, String ingredients, String barcode) {
        String lowerIng = ingredients.toLowerCase(Locale.ROOT);
        String lowerCat = category.toLowerCase(Locale.ROOT);
        String lowerBrand = brand.toLowerCase(Locale.ROOT);

        // 1. Carbonated Beverages & Juices
        if (lowerIng.contains("carbonated water") || lowerCat.contains("carbonated") || lowerCat.contains("soda")) {
            if (lowerIng.contains("lemon") || lowerIng.contains("lime") || lowerIng.contains("330") || lowerBrand.contains("sprite")) {
                return "SPRITE / LEMON-LIME CARBONATED DRINK";
            }
            if (lowerIng.contains("cola") || lowerBrand.contains("coca") || lowerBrand.contains("pepsi") || lowerBrand.contains("thums")) {
                return "CARBONATED COLA SOFT DRINK";
            }
            return "CARBONATED SOFT DRINK";
        }
        if (lowerIng.contains("fruit pulp") || lowerIng.contains("concentrate") || lowerCat.contains("juice")) {
            return "PACKAGED FRUIT JUICE DRINK";
        }

        // 2. Chips, Wafers & Savory Snacks
        if (lowerIng.contains("potato") && (lowerIng.contains("palmolein") || lowerIng.contains("oil") || lowerIng.contains("salt"))) {
            return "POTATO CHIPS SNACK";
        }
        if (lowerIng.contains("corn meal") || lowerIng.contains("rice meal") || lowerCat.contains("extruded")) {
            return "EXTRUDED CORN / RICE SNACK";
        }
        if (lowerIng.contains("makhana") || lowerIng.contains("foxnut")) {
            return "ROASTED MAKHANA SNACK";
        }

        // 3. Biscuits, Cookies & Wafers
        if (lowerIng.contains("biscuit") || lowerIng.contains("cookie") || lowerCat.contains("biscuit") || lowerIng.contains("wheat flour") && lowerIng.contains("sugar") && lowerIng.contains("shortening")) {
            return "PACKAGED BISCUITS & COOKIES";
        }

        // 4. Chocolates & Confectionery
        if (lowerIng.contains("cocoa") || lowerIng.contains("chocolate") || lowerCat.contains("chocolate")) {
            return "CHOCOLATE CONFECTIONERY";
        }

        // 5. Instant Noodles & Pasta
        if (lowerIng.contains("noodle") || lowerIng.contains("tastemaker") || lowerCat.contains("noodle") || lowerCat.contains("pasta")) {
            return "INSTANT NOODLES / PASTA";
        }

        // 6. Dairy & Milk Products
        if (lowerIng.contains("milk") || lowerIng.contains("whey") || lowerCat.contains("dairy") || lowerCat.contains("cheese") || lowerCat.contains("butter")) {
            return "PACKAGED DAIRY PRODUCT";
        }

        // 7. Cereals & Millets
        if (lowerIng.contains("oats") || lowerIng.contains("ragi") || lowerIng.contains("millet") || lowerCat.contains("cereal")) {
            return "PACKAGED BREAKFAST CEREAL";
        }

        // 8. Brand & Category Combinations
        if (!brand.isEmpty() && !"PACKAGED FOOD".equals(brand)) {
            if (!category.isEmpty() && !"PACKAGED FOOD".equals(category)) {
                return brand + " " + category;
            }
            return brand + " PACKAGED FOOD";
        }

        if (!category.isEmpty() && !"PACKAGED FOOD".equals(category)) {
            return category + " ITEM";
        }

        return "SCANNED FOOD ITEM (" + barcode + ")";
    }
}
