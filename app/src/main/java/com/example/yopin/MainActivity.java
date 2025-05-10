package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Точка входа в приложение, служит для инициализации и перенаправления
 */
public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Инициализация менеджера сессий
        sessionManager = new SessionManager(this);
        
        // Инициализируем репозиторий данных для предварительной загрузки
        DataRepository.getInstance(this);
        
        // Анимации для элементов интерфейса
        ImageView logoImage = findViewById(R.id.ivAppLogo);
        TextView appName = findViewById(R.id.tvAppName);
        TextView appSlogan = findViewById(R.id.tvAppSlogan);
        TextView versionText = findViewById(R.id.tvVersion);
        
        // Загружаем анимации
        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        Animation textAnimationDelayed = AnimationUtils.loadAnimation(this, R.anim.text_animation);
        textAnimationDelayed.setStartOffset(500);
        
        // Применяем анимации
        logoImage.startAnimation(logoAnimation);
        appName.startAnimation(textAnimation);
        appSlogan.startAnimation(textAnimationDelayed);
        versionText.setAlpha(0f);
        versionText.animate().alpha(1f).setStartDelay(1000).setDuration(500).start();
        
        // Небольшая задержка для отображения логотипа/сплеш-скрина
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Проверяем, авторизован ли пользователь
            if (sessionManager.isLoggedIn()) {
                // Если да, то открываем главный экран
                Intent intent = new Intent(MainActivity.this, BaseActivity.class);
                intent.putExtra("screen_type", BaseActivity.SCREEN_BOOKS_LIST);
                startActivity(intent);
            } else {
                // Если нет, то открываем экран входа
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            
            // Закрываем активность, чтобы пользователь не мог вернуться на сплеш-скрин
            finish();
        }, 1500); // Увеличенная задержка для анимаций
    }
}