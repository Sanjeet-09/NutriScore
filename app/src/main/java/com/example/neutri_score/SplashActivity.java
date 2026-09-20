package com.example.neutri_score;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Time in milliseconds to show the splash screen (2.5 seconds)
    private static final int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Disable automatic Dark Mode app-wide: keep consistent Neo-Brutalist Light look
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        NeoBrutalistUtils.setupLightStatusBar(this);
        setContentView(R.layout.activity_splash);

        // Wait for 2.5 seconds, then open MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // If user is already logged in, skip auth screens
                Intent intent;
                if (SessionManager.getInstance(SplashActivity.this).isLoggedIn()) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, SignInActivity.class);
                }
                startActivity(intent);

                // Close SplashActivity so back button won't return to splash
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}