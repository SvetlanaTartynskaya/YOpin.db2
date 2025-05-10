package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Экран входа в приложение
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DataRepository dataRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Инициализация объектов
        try {
            dataRepository = DataRepository.getInstance(this);
            if (dataRepository == null) {
                Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_LONG).show();
                return;
            }
            sessionManager = new SessionManager(this);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Если пользователь уже вошел, перенаправляем на главный экран
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
            intent.putExtra("screen_type", BaseActivity.SCREEN_BOOKS_LIST);
            startActivity(intent);
            finish();
            return;
        }
        
        // Инициализация UI элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        
        // Обработчик кнопки входа
        btnLogin.setOnClickListener(v -> loginUser());
        
        // Обработчик перехода на экран регистрации
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
    
    /**
     * Метод для авторизации пользователя
     */
    private void loginUser() {
        // Получение и проверка введенных данных
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Показываем индикатор загрузки
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        
        try {
            // Проверка пользователя в локальной базе данных
            boolean loginSuccess = dataRepository.loginUser(email, password);
            
            if (loginSuccess) {
                // Получаем данные пользователя
                Models.User user = dataRepository.getUser(email);
                if (user != null) {
                    // Создаем сессию пользователя
                    sessionManager.createLoginSession(user.getId(), email, user.getFullName());
                    
                    // Переходим на главный экран
                    Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
                    intent.putExtra("screen_type", BaseActivity.SCREEN_BOOKS_LIST);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_LONG).show();
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}