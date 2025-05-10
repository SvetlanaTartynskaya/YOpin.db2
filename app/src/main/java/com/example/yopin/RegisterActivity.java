package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.regex.Pattern;

/**
 * Экран регистрации в приложении
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etFullName, etUsername, etBirthDate;
    private Button btnRegister;
    private TextView tvLogin;
    private DataRepository dataRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация объектов
        dataRepository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);
        
        // Инициализация UI элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        
        // Обработчик кнопки регистрации
        btnRegister.setOnClickListener(v -> registerUser());
        
        // Обработчик перехода на экран входа
        tvLogin.setOnClickListener(v -> finish());
    }
    
    /**
     * Метод для проверки валидности email
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Метод для проверки валидности даты рождения
     */
    private boolean isValidBirthDate(String birthDate) {
        // Простая проверка формата ДД.ММ.ГГГГ
        String regex = "^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(19|20)\\d\\d$";
        return Pattern.compile(regex).matcher(birthDate).matches();
    }
    
    /**
     * Метод для регистрации пользователя
     */
    private void registerUser() {
        // Получение и проверка введенных данных
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        
        // Проверка заполнения всех полей
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            fullName.isEmpty() || username.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверка валидности email
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Пожалуйста, введите корректный email", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверка валидности даты рождения
        if (!isValidBirthDate(birthDate)) {
            Toast.makeText(this, "Пожалуйста, введите дату рождения в формате ДД.ММ.ГГГГ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Регистрация пользователя в базе данных
        boolean registerSuccess = dataRepository.registerUser(email, password, fullName, username, birthDate);
        
        if (registerSuccess) {
            // Получаем данные пользователя
            Models.User user = dataRepository.getUser(email);
            if (user != null) {
                // Создаем сессию пользователя
                sessionManager.createLoginSession(user.getId(), email, user.getFullName());
                
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                
                // Переходим на главный экран
                Intent intent = new Intent(RegisterActivity.this, BaseActivity.class);
                intent.putExtra("screen_type", BaseActivity.SCREEN_BOOKS_LIST);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ошибка регистрации. Возможно, пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
        }
    }
}