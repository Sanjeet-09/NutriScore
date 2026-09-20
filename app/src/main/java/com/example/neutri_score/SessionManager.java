package com.example.neutri_score;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "neutriscore_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private static SessionManager instance;
    private final SharedPreferences preferences;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void createLoginSession(String name, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";

    public void updateUserDetails(String name, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public void saveProfileImageUri(String uriStr) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PROFILE_IMAGE_URI, uriStr);
        editor.apply();
    }

    public String getProfileImageUri() {
        return preferences.getString(KEY_PROFILE_IMAGE_URI, null);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "Friend");
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
