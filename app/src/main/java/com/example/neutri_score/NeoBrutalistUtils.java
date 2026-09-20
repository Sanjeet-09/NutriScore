package com.example.neutri_score;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import androidx.core.view.WindowInsetsControllerCompat;

public class NeoBrutalistUtils {

    public static void setupLightStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decorView);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    public static void applyTactileFeedback(View view) {
        float offset = view.getContext().getResources().getDisplayMetrics().density * 2.5f;

        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setTranslationX(offset);
                    v.setTranslationY(offset);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setTranslationX(0f);
                    v.setTranslationY(0f);
                    break;
            }
            return false;
        });
    }
}
