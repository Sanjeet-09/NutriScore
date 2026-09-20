package com.example.neutri_score;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager handles temporary local session persistence.
 *
 * NOTE: This is a lightweight mock session store.
 * When AWS services (such as AWS Cognito / AWS Amplify Auth) are integrated later,
 * replace the internal implementation with AWS Auth SDK calls while preserving
 * the same high-level interface.
 */
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

    /**
     * Saves user details and marks session as active.
     */
    public void createLoginSession(String name, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Checks whether user is currently logged in.
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";

    /**
     * Updates user details (name and email).
     */
    public void updateUserDetails(String name, String email) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Saves selected profile image URI.
     */
    public void saveProfileImageUri(String uriStr) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PROFILE_IMAGE_URI, uriStr);
        editor.apply();
    }

    /**
     * Gets stored profile image URI string (returns null if none saved).
     */
    public String getProfileImageUri() {
        return preferences.getString(KEY_PROFILE_IMAGE_URI, null);
    }

    /**
     * Gets stored user name (defaults to "Friend" if not set).
     */
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "Friend");
    }

    /**
     * Gets stored user email.
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Clears session on logout.
     */
    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
