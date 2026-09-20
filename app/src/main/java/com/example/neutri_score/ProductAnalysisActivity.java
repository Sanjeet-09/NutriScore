package com.example.neutri_score;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ProductAnalysisActivity extends AppCompatActivity {

    private FrameLayout cardEmptyAnalysis;
    private TextView btnScanAnalysisCTA;
    private LinearLayout llAnalysisContentContainer;

    private TextView tvAnalysisCategory;
    private TextView tvAnalysisBrand;
    private TextView tvAnalysisProductName;
    private TextView tvAnalysisGradeBadge;
    private TextView tvHealthPercent;
    private ProgressBar pbHealthScore;
    private TextView tvScoringVerdict;
    private TextView tvRiskFlags;

    private LinearLayout containerIngredients;

    private TextView tvSodiumLabel;
    private ProgressBar pbSodium;
    private TextView tvFatLabel;
    private ProgressBar pbFat;
    private TextView tvSugarLabel;
    private ProgressBar pbSugar;
    private TextView tvProteinLabel;
    private ProgressBar pbProtein;
    private TextView tvAdditivesList;

    private TextView btnScanAnother;
    private TextView btnBackToScannerTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_product_analysis);

        initViews();
        loadProductData();
        BottomNavHelper.setupBottomNav(this, "ANALYSIS");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductData();
    }

    private void initViews() {
        cardEmptyAnalysis          = findViewById(R.id.cardEmptyAnalysis);
        btnScanAnalysisCTA         = findViewById(R.id.btnScanAnalysisCTA);
        llAnalysisContentContainer = findViewById(R.id.llAnalysisContentContainer);

        tvAnalysisCategory    = findViewById(R.id.tvAnalysisCategory);
        tvAnalysisBrand       = findViewById(R.id.tvAnalysisBrand);
        tvAnalysisProductName = findViewById(R.id.tvAnalysisProductName);
        tvAnalysisGradeBadge  = findViewById(R.id.tvAnalysisGradeBadge);
        tvHealthPercent       = findViewById(R.id.tvHealthPercent);
        pbHealthScore         = findViewById(R.id.pbHealthScore);
        tvScoringVerdict      = findViewById(R.id.tvScoringVerdict);
        tvRiskFlags           = findViewById(R.id.tvRiskFlags);

        containerIngredients  = findViewById(R.id.containerIngredients);

        tvSodiumLabel         = findViewById(R.id.tvSodiumLabel);
        pbSodium              = findViewById(R.id.pbSodium);
        tvFatLabel            = findViewById(R.id.tvFatLabel);
        pbFat                 = findViewById(R.id.pbFat);
        tvSugarLabel          = findViewById(R.id.tvSugarLabel);
        pbSugar               = findViewById(R.id.pbSugar);
        tvProteinLabel        = findViewById(R.id.tvProteinLabel);
        pbProtein             = findViewById(R.id.pbProtein);
        tvAdditivesList       = findViewById(R.id.tvAdditivesList);

        btnScanAnother        = findViewById(R.id.btnScanAnother);
        btnBackToScannerTop   = findViewById(R.id.btnBackToScannerTop);

        if (btnScanAnalysisCTA != null) {
            NeoBrutalistUtils.applyTactileFeedback(btnScanAnalysisCTA);
            btnScanAnalysisCTA.setOnClickListener(v -> {
                Intent intent = new Intent(ProductAnalysisActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }

        NeoBrutalistUtils.applyTactileFeedback(btnScanAnother);
        NeoBrutalistUtils.applyTactileFeedback(btnBackToScannerTop);

        btnScanAnother.setOnClickListener(v -> {
            Intent intent = new Intent(ProductAnalysisActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });
        btnBackToScannerTop.setOnClickListener(v -> finish());
    }

    private void loadProductData() {
        ProductModel product = (ProductModel) getIntent().getSerializableExtra("PRODUCT_MODEL");
        if (product == null) {
            product = (ProductModel) getIntent().getSerializableExtra("product");
        }

        if (product == null) {

            if (cardEmptyAnalysis != null) cardEmptyAnalysis.setVisibility(View.VISIBLE);
            if (llAnalysisContentContainer != null) llAnalysisContentContainer.setVisibility(View.GONE);
            return;
        }

        if (cardEmptyAnalysis != null) cardEmptyAnalysis.setVisibility(View.GONE);
        if (llAnalysisContentContainer != null) llAnalysisContentContainer.setVisibility(View.VISIBLE);

        tvAnalysisCategory.setText(product.getCategory());
        tvAnalysisBrand.setText(product.getBrand());
        tvAnalysisProductName.setText(product.getName());

        String grade = product.getGrade();
        tvAnalysisGradeBadge.setText(grade);
        tvAnalysisGradeBadge.setTextColor(Color.WHITE);
        tvAnalysisGradeBadge.setBackgroundResource(NutriScoringEngine.gradeBadgeDrawable(grade));
        if ("C".equalsIgnoreCase(grade)) {
            tvAnalysisGradeBadge.setTextColor(Color.BLACK);
        }

        int pct = product.getHealthPercent();
        tvHealthPercent.setText(pct + "%");
        pbHealthScore.setProgress(pct);
        pbHealthScore.setProgressTintList(
                ColorStateList.valueOf(NutriScoringEngine.gradeColor(grade)));

        String verdict = product.getScoringVerdict();
        tvScoringVerdict.setText(verdict != null ? verdict : "No detailed data");

        List<String> flags = product.getRiskFlags();
        if (flags != null && !flags.isEmpty()) {
            StringBuilder sb = new StringBuilder("⚠️ ");
            for (int i = 0; i < flags.size(); i++) {
                sb.append(flags.get(i));
                if (i < flags.size() - 1) sb.append(" • ");
            }
            tvRiskFlags.setText(sb.toString());
        } else {
            tvRiskFlags.setText("✅ No critical risk flags detected");
        }

        List<IngredientScore> breakdown = product.getIngredientBreakdown();
        containerIngredients.removeAllViews();
        if (breakdown != null && !breakdown.isEmpty()) {
            for (IngredientScore ing : breakdown) {
                containerIngredients.addView(buildIngredientRow(ing));
            }
        }

        tvSodiumLabel.setText("SODIUM: " + product.getSodiumText());
        pbSodium.setProgress(product.getSodiumVal());

        tvFatLabel.setText("SATURATED FAT: " + product.getFatText());
        pbFat.setProgress(product.getFatVal());

        tvSugarLabel.setText("ADDED SUGARS: " + product.getSugarText());
        pbSugar.setProgress(product.getSugarVal());

        tvProteinLabel.setText("PROTEIN & FIBER: " + product.getProteinText());
        pbProtein.setProgress(product.getProteinVal());

        tvAdditivesList.setText(product.getAdditives());
    }

    private View buildIngredientRow(IngredientScore ing) {
        int dp4  = dp(4);
        int dp6  = dp(6);
        int dp8  = dp(8);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dp4, 0, dp4);
        row.setLayoutParams(rowParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvIcon = new TextView(this);
        tvIcon.setText(ing.getIcon());
        tvIcon.setTextSize(16);
        tvIcon.setPadding(0, 0, dp8, 0);

        TextView tvName = new TextView(this);
        tvName.setText(ing.getName());
        tvName.setTextColor(Color.parseColor("#1A1A1A"));
        tvName.setTextSize(12);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvName.setLayoutParams(nameParams);

        TextView tvScore = new TextView(this);
        tvScore.setText(ing.getScoreLabel());
        tvScore.setTextColor(Color.WHITE);
        tvScore.setTextSize(11);
        tvScore.setTypeface(null, android.graphics.Typeface.BOLD);
        tvScore.setBackgroundColor(ing.getCategoryColor());
        tvScore.setPadding(dp8, dp4, dp8, dp4);

        topRow.addView(tvIcon);
        topRow.addView(tvName);
        topRow.addView(tvScore);

        TextView tvReason = new TextView(this);
        tvReason.setText(ing.getReason());
        tvReason.setTextColor(Color.parseColor("#888888"));
        tvReason.setTextSize(10);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        reasonParams.setMargins(dp(32), dp4, 0, 0);
        tvReason.setLayoutParams(reasonParams);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        divParams.setMargins(0, dp6, 0, 0);
        divider.setLayoutParams(divParams);

        row.addView(topRow);
        row.addView(tvReason);
        row.addView(divider);

        return row;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
