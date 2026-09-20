package com.example.neutri_score;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Utility helpers for Neo-Brutalist UI interactions.
 */
public class NeoBrutalistUtils {

    /**
     * Ensures the status bar text and icons (time, battery, Wi-Fi) are dark
     * so they are clearly visible on the light cream background.
     */
    public static void setupLightStatusBar(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decorView);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    /**
     * Applies a physical press-down tactile effect to Neo-Brutalist buttons.
     * When pressed, the button shifts diagonally down-right toward its shadow,
     * and snaps back when released.
     */
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
            return false; // allow onClickListener to still fire
        });
    }
}
