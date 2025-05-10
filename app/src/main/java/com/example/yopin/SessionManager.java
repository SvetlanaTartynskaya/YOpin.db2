package com.example.yopin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Класс для управления сессией пользователя
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    // Константы
    private static final String PREF_NAME = "YopinSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    // Конструктор
    public SessionManager(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in SessionManager constructor");
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        this.context = context.getApplicationContext();
        try {
            pref = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SharedPreferences: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SessionManager", e);
        }
    }
    
    /**
     * Создание сессии входа
     */
    public void createLoginSession(int userId, String email, String username) {
        try {
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putInt(KEY_USER_ID, userId);
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_USERNAME, username);
            boolean success = editor.commit(); // Используем commit() вместо apply() для немедленной записи
            
            if (!success) {
                Log.e(TAG, "Failed to save session data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating login session: " + e.getMessage(), e);
        }
    }
    
    /**
     * Проверка статуса входа
     */
    public boolean isLoggedIn() {
        try {
            return pref.getBoolean(KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Получение ID текущего пользователя
     */
    public int getUserId() {
        try {
            return pref.getInt(KEY_USER_ID, -1);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Получение email текущего пользователя
     */
    public String getUserEmail() {
        try {
            return pref.getString(KEY_EMAIL, "");
        } catch (Exception e) {
            Log.e(TAG, "Error getting user email: " + e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Получение имени пользователя
     */
    public String getUsername() {
        try {
            return pref.getString(KEY_USERNAME, "");
        } catch (Exception e) {
            Log.e(TAG, "Error getting username: " + e.getMessage(), e);
            return "";
        }
    }
    
    /**
     * Выход пользователя
     */
    public void logoutUser() {
        try {
            // Очищаем все данные из Shared Preferences
            editor.clear();
            boolean success = editor.commit(); // Используем commit() вместо apply()
            
            if (!success) {
                Log.e(TAG, "Failed to clear session data during logout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
        }
    }
} 