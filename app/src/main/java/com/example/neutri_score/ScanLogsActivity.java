package com.example.neutri_score;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ScanLogsActivity extends AppCompatActivity {

    private LinearLayout llLogsContainer;
    private FrameLayout cardEmptyLogs;
    private TextView tvScanCountBadge;
    private TextView btnScanProductCTA;
    private TextView btnClearLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_scan_logs);

        llLogsContainer = findViewById(R.id.llLogsContainer);
        cardEmptyLogs = findViewById(R.id.cardEmptyLogs);
        tvScanCountBadge = findViewById(R.id.tvScanCountBadge);
        btnScanProductCTA = findViewById(R.id.btnScanProductCTA);
        btnClearLogs = findViewById(R.id.btnClearLogs);

        NeoBrutalistUtils.applyTactileFeedback(btnClearLogs);
        if (btnScanProductCTA != null) {
            NeoBrutalistUtils.applyTactileFeedback(btnScanProductCTA);
            btnScanProductCTA.setOnClickListener(v -> {
                Intent intent = new Intent(ScanLogsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            });
        }

        btnClearLogs.setOnClickListener(v -> {
            ProductRepository.getInstance().clearHistory();
            renderLogs();
            Toast.makeText(ScanLogsActivity.this, "Scan History Cleared", Toast.LENGTH_SHORT).show();
        });

        renderLogs();

        // Setup bottom navigation bar for LOGS tab
        BottomNavHelper.setupBottomNav(this, "LOGS");
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderLogs();
    }

    private void renderLogs() {
        llLogsContainer.removeAllViews();
        List<ScanLogItem> history = ProductRepository.getInstance().getScanHistory();

        tvScanCountBadge.setText(history.size() + " SCANS");

        if (history.isEmpty()) {
            cardEmptyLogs.setVisibility(View.VISIBLE);
            btnClearLogs.setVisibility(View.GONE);
            llLogsContainer.addView(cardEmptyLogs);
            return;
        }

        cardEmptyLogs.setVisibility(View.GONE);
        btnClearLogs.setVisibility(View.VISIBLE);

        float density = getResources().getDisplayMetrics().density;
        int padding12 = (int) (12 * density);

        for (ScanLogItem item : history) {
            LinearLayout itemCard = new LinearLayout(this);
            itemCard.setOrientation(LinearLayout.HORIZONTAL);
            itemCard.setBackgroundResource(R.drawable.bg_neo_card_sm);
            itemCard.setPadding(padding12, padding12, padding12 + (int)(3 * density), padding12 + (int)(3 * density));
            itemCard.setGravity(Gravity.CENTER_VERTICAL);
            itemCard.setClickable(true);
            itemCard.setFocusable(true);
            NeoBrutalistUtils.applyTactileFeedback(itemCard);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, (int) (10 * density));
            itemCard.setLayoutParams(lp);

            // Left text column
            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textCol.setLayoutParams(textLp);

            TextView tvName = new TextView(this);
            tvName.setText(item.getName());
            tvName.setTextColor(Color.BLACK);
            tvName.setTextSize(14);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvSub = new TextView(this);
            tvSub.setText(item.getTime() + " • " + item.getFlag());
            tvSub.setTextColor(Color.parseColor("#78716C"));
            tvSub.setTextSize(11);

            textCol.addView(tvName);
            textCol.addView(tvSub);

            // Right Grade badge
            TextView tvGrade = new TextView(this);
            int badgeSize = (int) (38 * density);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(badgeSize, badgeSize);
            tvGrade.setLayoutParams(badgeLp);
            tvGrade.setGravity(Gravity.CENTER);
            tvGrade.setText(item.getGrade());
            tvGrade.setTextSize(16);
            tvGrade.setTypeface(null, android.graphics.Typeface.BOLD);
            tvGrade.setTextColor(Color.WHITE);

            if ("A".equalsIgnoreCase(item.getGrade())) {
                tvGrade.setBackgroundResource(R.drawable.bg_nutri_tile_a);
            } else if ("B".equalsIgnoreCase(item.getGrade())) {
                tvGrade.setBackgroundResource(R.drawable.bg_nutri_tile_b);
            } else if ("C".equalsIgnoreCase(item.getGrade())) {
                tvGrade.setBackgroundResource(R.drawable.bg_nutri_tile_c);
                tvGrade.setTextColor(Color.BLACK);
            } else if ("D".equalsIgnoreCase(item.getGrade())) {
                tvGrade.setBackgroundResource(R.drawable.bg_nutri_tile_d);
            } else {
                tvGrade.setBackgroundResource(R.drawable.bg_nutri_tile_e);
            }

            itemCard.addView(textCol);
            itemCard.addView(tvGrade);

            // Click listener on item card to view analysis
            itemCard.setOnClickListener(v -> {
                ProductModel productModel = item.getProductModel();
                if (productModel == null) {
                    productModel = ProductRepository.getInstance().getProduct(item.getName().toLowerCase());
                }
                Intent intent = new Intent(ScanLogsActivity.this, ProductAnalysisActivity.class);
                intent.putExtra("PRODUCT_MODEL", productModel);
                startActivity(intent);
            });

            llLogsContainer.addView(itemCard);
        }
    }
}
