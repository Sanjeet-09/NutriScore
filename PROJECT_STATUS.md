# 🛡️ NEUTRI-SCORE // PROJECT HANDOVER & STATE MANIFEST

> **Target Audience:** Future AI Assistant Models & Developers  
> **Last Updated:** September 20, 2026  
> **Status:** ✅ FSSAI Scoring Engine + Open Food Facts API + Google ML Kit Camera Label OCR Live — Build Passing

---

## 1. 📌 Project Overview
- **App Name:** **NeutriScore**
- **Mission:** A food transparency app designed for Indian packaged foods. Scans any barcode via camera, fetches real product data from Open Food Facts (3M+ products), decodes ingredients using a FSSAI-inspired scoring engine (60+ ingredient dictionary), and grades food A–E with per-ingredient breakdown and healthy swap suggestions.
- **Tech Stack:**
  - **Language:** Native Java
  - **Min SDK:** 28 (Android 9.0) | **Target/Compile SDK:** 36 (Android 16)
  - **Frameworks:** AndroidX, Material3, AppCompat
  - **Libraries:** CameraX 1.4.2, ML Kit Barcode 17.3.0, OkHttp 4.12.0
  - **External API:** Open Food Facts (free, no key required)
  - **Build Tool:** Gradle 8.13

---

## 2. 🎨 Unified Design System & Palette (Strict Guidelines)

All screens share the **exact same Neo-Brutalist + Minimalist aesthetic**:

| Token Name | Hex Code | Usage |
|---|---|---|
| `nb_bg_cream` | `#FAFAF9` | Auth & Splash backgrounds |
| `brutal_yellow` | `#FFFFDE00` | Main App pages background (Scanner, Analysis, Logs, Profile) |
| `nb_card_white` | `#FFFFFF` | Card & surface fill |
| `nb_border` | `#111111` | 2dp solid black borders |
| `nb_shadow` | `#111111` | 3–4dp hard offset shadows (0 blur) |
| `nutri_a` | `#038141` | Grade A badge / primary CTA |
| `nutri_b` | `#85BB2F` | Grade B badge |
| `nutri_c` | `#FECB02` | Grade C badge |
| `nutri_d` | `#EE8100` | Grade D badge |
| `nutri_e` | `#E63E11` | Grade E badge |
| `nb_muted_gray` | `#78716C` | Subtitle and secondary text |

### Rules:
1. **LOCKED TO LIGHT MODE:** `AppCompatDelegate.MODE_NIGHT_NO` — no dark mode detection.
2. **STATUS BAR:** Always dark icons via `NeoBrutalistUtils.setupLightStatusBar(Activity)`.
3. **BOTTOM NAV:** Active tab = `bg_neo_tab_active` (black fill, white text). Inactive = transparent + black text.

---

## 3. 🗺️ Complete App Flow

```
SplashActivity (2.5s)
       │
       ├── If Logged In ────┐
       │                    ▼
       └── If Logged Out ──> SignInActivity <───> SignUpActivity
                                  │
                                  ▼
                          MainActivity (Scanner Hub)
                         ┌────────┼────────┐
                         ▼        ▼        ▼
                    Analysis    Logs    Profile
```

### Screen & File Breakdown:

| # | Screen | Java Class | XML Layout | Key Components |
|---|---|---|---|---|
| **1** | **Splash** | [`SplashActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/SplashActivity.java) | [`activity_splash.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_splash.xml) | Logo, tag badge, progress bar, session router. |
| **2** | **Sign In** | [`SignInActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/SignInActivity.java) | [`activity_sign_in.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_sign_in.xml) | Email & password validation, eye toggle, session creation. |
| **3** | **Sign Up** | [`SignUpActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/SignUpActivity.java) | [`activity_sign_up.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_sign_up.xml) | Full Name, Email, Password, Confirm Password; dual eye toggles. |
| **4** | **Scanner Hub** | [`MainActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/MainActivity.java) | [`activity_main.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_main.xml) | CameraX barcode scanner, image picker, Live Product/Brand Search bar, 4 preset food cards, ingredient decoder, 4-tab nav. |
| **5** | **Analysis** | [`ProductAnalysisActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/ProductAnalysisActivity.java) | [`activity_product_analysis.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_product_analysis.xml) | Dynamic product scoring view (Grade badge, health % bar, verdict, per-ingredient breakdown, nutrient meters, swap card) + NO PRODUCT ANALYZED empty state card with CTA. |
| **6** | **Scan Logs** | [`ScanLogsActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/ScanLogsActivity.java) | [`activity_scan_logs.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_scan_logs.xml) | Clean 0-scan empty state card with CTA, dynamic scan counter badge, interactive tap-to-view scan item card. |
| **7** | **Profile** | [`ProfileActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/ProfileActivity.java) | [`activity_profile.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_profile.xml)<br>[`dialog_edit_profile.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/dialog_edit_profile.xml) | Interactive photo avatar picker, Profile photo local session persistence, Edit Name/Email Neo-Brutalist dialog, stat cards, Sign Out. |
| **8** | **Barcode Scanner** | [`BarcodeScannerActivity.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/BarcodeScannerActivity.java) | [`activity_barcode_scanner.xml`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/res/layout/activity_barcode_scanner.xml) | Full-screen CameraX preview, ML Kit real-time barcode detection, yellow reticle overlay. |

---

## 4. ⚙️ Supporting Code & Architecture

### Core Logic Files

| File | Purpose |
|---|---|
| [`IngredientScore.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/IngredientScore.java) | Serializable POJO: ingredient name, 0–10 score, BENEFICIAL/NEUTRAL/CAUTION/HARMFUL category, reason text, emoji icon. |
| [`NutriScoringEngine.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/NutriScoringEngine.java) | **FSSAI Scoring Engine** — 60+ Indian ingredient dictionary. Parses ingredients, position-weighted scoring, FSSAI penalty rules (sodium caps, trans fat floor, INS count drop), A–E grade computation, verdict builder. |
| [`OpenFoodFactsService.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/OpenFoodFactsService.java) | **Multi-API Lookup & Live Search Engine** — Barcode normalization + 6-tier waterfall API search (OFF Global v2, OFF .net, OFF India v2, OFF v0, UPCitemdb 500M+ API, OFF Search API) + `fetchProductByQuery()` for live brand/product search (e.g. *Bingo Mad Angles*, *Amul*). Runs `NutriScoringEngine` on fetched ingredients. Returns fully scored `ProductModel`. |
| [`ProductModel.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/ProductModel.java) | Serializable product data class. Fields: name, brand, category, grade, gradeColor, nutrient bars, ingredients, riskFlags, swap, **+** ingredientBreakdown, healthPercent, scoringVerdict, sodiumMgPer100g. |
| [`ProductRepository.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/ProductRepository.java) | 4 preset Indian products (Lays/Kurkure/Maggi/Makhana) auto-scored by engine. `analyzeRawIngredients()` delegates fully to `NutriScoringEngine`. |
| [`SessionManager.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/SessionManager.java) | SharedPreferences-based session. Placeholder for **AWS Cognito** (future sprint). |
| [`BottomNavHelper.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/BottomNavHelper.java) | 4-tab bottom nav coordinator with tactile animations. |
| [`NeoBrutalistUtils.java`](file:///c:/Users/HP/AndroidStudioProject/NeutriScore/app/src/main/java/com/example/neutri_score/NeoBrutalistUtils.java) | Button press feedback + dark status bar icons via `WindowInsetsControllerCompat`. |

---

## 5. 🔍 FSSAI Scoring Engine — How It Works

```
Ingredient String (from barcode scan or user input)
         │
         ▼
  NutriScoringEngine.parseAndScore()
  → Splits by comma/semicolon
  → Matches each token against 60+ ingredient dictionary
  → Assigns 0–10 score per FSSAI/Nutri-Score criteria:
      10 = Makhana, Ragi, Oats, Almonds
       8 = Whole Wheat, Olive Oil, Spices
       5 = Corn Starch, Milk Solids, INS 500
       3 = Sugar, Maida, Palmolein
       1 = INS 627, INS 631, INS 110 (Dye)
       0 = Trans Fat, Vanaspati
         │
         ▼
  computeHealthPercent() — position-weighted average
  (first ingredient = highest weight per FSSAI quantity rule)
         │
         ▼
  computeGrade() — apply FSSAI penalty rules:
      • Sodium > 600mg → cap at D
      • Sodium > 800mg → cap at E
      • Trans fat detected → force E
      • 3+ INS additives → drop one grade level
         │
         ▼
  Grade A–E  +  Health %  +  Verdict string
```

---

## 6. 🌐 Barcode Scan Flow (Live API)

```
Camera scan any barcode
         │
         ├── Local preset? (Lays/Kurkure/Maggi) → instant open
         │
         └── Unknown barcode
                  │
                  ▼
      Open Food Facts API (async, OkHttp)
      GET world.openfoodfacts.org/api/v2/product/{barcode}.json
                  │
            ┌─────┴──────┐
          Found         Not Found
            │                │
      Real product      Dialog: "Paste
      name + brand +    ingredients below"
      ingredients +
      nutrition data
            │
      NutriScoringEngine
            │
      Full Analysis page
      with real data
```

---

## 7. 🔮 Future Sprints (Not Yet Implemented)

| Feature | Technology | Status |
|---|---|---|
| Real user authentication | **AWS Cognito / Amplify** | 🔲 Pending |
| Cloud scan history | **AWS DynamoDB** | 🔲 Pending |
| Label image OCR | **AWS Rekognition** | 🔲 Pending |
| Product photo display | **AWS S3** | 🔲 Pending |
| QR code product lookup | Open Food Facts (same API) | ✅ Already works |

---

## 8. 🛠️ Build & Verification
```powershell
.\gradlew assembleDebug
# Generated: app/build/outputs/apk/debug/app-debug.apk
```
Latest build: **BUILD SUCCESSFUL** — 0 errors.

### Permissions declared in AndroidManifest:
- `CAMERA` — barcode scanning & CameraX ML Kit Label OCR
- `INTERNET` — Open Food Facts API calls & AWS network calls

---

## 9. 🚀 Play Store Release & AWS Cloud Backend Task Checklist

### 🅰️ AWS Cloud Backend Integration Tasks (Next Phase)
- [ ] **Task A1: AWS Cognito User Authentication**
  - Add `com.amazonaws:aws-android-sdk-cognitoidentityprovider` to `app/build.gradle`.
  - Connect `SignUpActivity.java` (user signup with email verification OTP/link).
  - Connect `SignInActivity.java` (user login with JWT session token generation).
- [ ] **Task A2: Amazon DynamoDB Database Integration**
  - Add `com.amazonaws:aws-android-sdk-ddb` to `app/build.gradle`.
  - Create `NeutriScore_Users` table for user profiles (`userId`, `name`, `email`, `profilePicUrl`).
  - Create `NeutriScore_ScanHistory` table for cloud scan logs (`userId`, `timestamp`, `productName`, `grade`, `scannedVia`).
  - Create `NeutriScore_CustomProducts` table for crowdsourced barcode items (`barcode`, `productName`, `brand`, `ingredients`).
- [ ] **Task A3: Amazon S3 Profile & Label Storage**
  - Add `com.amazonaws:aws-android-sdk-s3` to `app/build.gradle`.
  - Upload user profile photos and pack label captures to `neutriscore-app-media` S3 bucket.
- [ ] **Task A4: Amazon API Gateway & AWS Lambda**
  - Build RESTful HTTPS backend endpoints for profile sync and custom product submissions.

### 🅱️ Google Play Store Publication Tasks
- [ ] **Task B1: Production Release Build Configuration**
  - Change `applicationId` to production domain (e.g. `com.neutriscore.app`).
  - Configure `release` build type in `app/build.gradle` with `minifyEnabled true` and `shrinkResources true`.
  - Generate Production App Bundle (`.aab`) with release keystore (`upload-keystore.jks`).
- [ ] **Task B2: Legal & Privacy Policy Documentation**
  - Publish HTTPS Privacy Policy URL detailing Camera and Account Data usage.
  - Add non-medical health disclaimer banner to `ProductAnalysisActivity`.
  - Complete Google Play Console **Data Safety Form** and **IARC Content Rating**.
- [ ] **Task B3: Play Store Listing Graphic Assets**
  - Create 512x512 high-res app icon.
  - Create 1024x500 feature graphic banner.
  - Capture 4–8 phone screenshots showcasing Scanner, Analysis, Logs, and Profile screens.
