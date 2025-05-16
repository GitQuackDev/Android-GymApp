package com.example.aimingfitness.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Helper class to manage application theme switching between light and dark mode
 */
public class ThemeHelper {
    private static final String THEME_PREFS = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    // Default to system theme if not specified
    private static final int MODE_DEFAULT = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    
    /**
     * Apply the saved theme mode or use the system default if none is saved.
     */
    public static void applyTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, MODE_DEFAULT);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    
    /**
     * Toggle between light and dark mode, and save the preference.
     * @return true if the new mode is dark, false for light.
     */
    public static boolean toggleTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        
        int newMode;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            // If currently following system, check the system and choose the opposite
            boolean systemNightMode = isSystemInDarkTheme(context);
            newMode = systemNightMode ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
        }
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME_MODE, newMode);
        editor.apply();
        
        AppCompatDelegate.setDefaultNightMode(newMode);
        return newMode == AppCompatDelegate.MODE_NIGHT_YES;
    }
    
    /**
     * Check if the current theme mode is dark.
     */
    public static boolean isDarkThemeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        int themeMode = sharedPreferences.getInt(KEY_THEME_MODE, MODE_DEFAULT);
        
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                return true;
            case AppCompatDelegate.MODE_NIGHT_NO:
                return false;
            default:
                return isSystemInDarkTheme(context);
        }
    }
    
    /**
     * Check if the system is currently using dark theme.
     */
    private static boolean isSystemInDarkTheme(Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return uiMode == Configuration.UI_MODE_NIGHT_YES;
    }
}