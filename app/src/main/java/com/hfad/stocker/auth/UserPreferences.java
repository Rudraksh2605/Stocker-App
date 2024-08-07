package com.hfad.stocker.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREF_NAME = "user_credentials";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private SharedPreferences prefs;

    public UserPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserCredentials(String username, String password) {
        prefs.edit().putString(KEY_USERNAME, username).putString(KEY_PASSWORD, password).apply();
    }

    public boolean isLoggedIn() {
        String username = prefs.getString(KEY_USERNAME, "");
        String password = prefs.getString(KEY_PASSWORD, "");
        return!username.isEmpty() &&!password.isEmpty();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    public void clearUserCredentials() {
        prefs.edit().clear().apply();
    }
}
