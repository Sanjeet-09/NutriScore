package com.example.neutri_score;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * Helper to link the 4-tab bottom navigation bar across all main activities.
 */
public class BottomNavHelper {

    public static void setupBottomNav(Activity activity, String activeTab) {
        View tabScanner = activity.findViewById(R.id.tabBtnScanner);
        View tabAnalysis = activity.findViewById(R.id.tabBtnAnalysis);
        View tabLogs = activity.findViewById(R.id.tabBtnLogs);
        View tabUser = activity.findViewById(R.id.tabBtnUser);

        if (tabScanner == null) return;

        // Apply Neo-Brutalist tactile feedback to all tabs
        NeoBrutalistUtils.applyTactileFeedback(tabScanner);
        NeoBrutalistUtils.applyTactileFeedback(tabAnalysis);
        NeoBrutalistUtils.applyTactileFeedback(tabLogs);
        NeoBrutalistUtils.applyTactileFeedback(tabUser);

        // Click handlers
        tabScanner.setOnClickListener(v -> {
            if (!activeTab.equals("SCANNER")) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        tabAnalysis.setOnClickListener(v -> {
            if (!activeTab.equals("ANALYSIS")) {
                Intent intent = new Intent(activity, ProductAnalysisActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        tabLogs.setOnClickListener(v -> {
            if (!activeTab.equals("LOGS")) {
                Intent intent = new Intent(activity, ScanLogsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });

        tabUser.setOnClickListener(v -> {
            if (!activeTab.equals("PROFILE")) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        });
    }
}
