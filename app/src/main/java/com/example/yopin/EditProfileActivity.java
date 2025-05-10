package com.example.yopin;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Активность для редактирования профиля пользователя
 */
public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText etFullName, etEmail, etBirthDate, etNewPassword, etConfirmPassword;
    private Button btnSaveProfile, btnCancel;
    private ShapeableImageView ivProfilePhoto;
    private ImageView ivCameraIcon;
    private View viewPhotoOverlay;
    private DataRepository dataRepository;
    private SessionManager sessionManager;
    private Models.User currentUser;
    private Calendar calendar;
    private Uri selectedImageUri;
    
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    allGranted = allGranted && granted;
                }
                if (allGranted) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Необходимы разрешения для доступа к галерее", Toast.LENGTH_SHORT).show();
                }
            }
    );
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivProfilePhoto.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Инициализация компонентов UI
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancel = findViewById(R.id.btnCancel);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        ivCameraIcon = findViewById(R.id.ivCameraIcon);
        viewPhotoOverlay = findViewById(R.id.viewPhotoOverlay);

        // Инициализация вспомогательных классов
        dataRepository = DataRepository.getInstance(this);
        sessionManager = new SessionManager(this);
        calendar = Calendar.getInstance();

        // Загрузка данных пользователя
        loadUserData();

        // Настройка обработчиков событий
        etBirthDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        btnCancel.setOnClickListener(v -> finish());
        
        // Обработчик выбора фото профиля
        View.OnClickListener photoClickListener = v -> checkPermissionAndOpenGallery();
        findViewById(R.id.btnSelectPhoto).setOnClickListener(photoClickListener);
        
        // Добавляем эффекты при наведении на фото
        ivProfilePhoto.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    showPhotoOverlay(true);
                    break;
                case MotionEvent.ACTION_UP:
                    showPhotoOverlay(false);
                    // Обработка нажатия, открываем галерею
                    if (isPointInsideView(event.getRawX(), event.getRawY(), ivProfilePhoto)) {
                        checkPermissionAndOpenGallery();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    showPhotoOverlay(false);
                    break;
            }
            return true;
        });
    }

    /**
     * Загрузка данных пользователя для заполнения полей формы
     */
    private void loadUserData() {
        currentUser = dataRepository.getUser(sessionManager.getUserEmail());
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etBirthDate.setText(currentUser.getBirthDate());
            
            // Загрузка фото профиля, если есть
            String photoPath = currentUser.getPhotoPath();
            if (!TextUtils.isEmpty(photoPath)) {
                Uri photoUri = Uri.parse(photoPath);
                ivProfilePhoto.setImageURI(photoUri);
                selectedImageUri = photoUri;
            }
            
            // Email не редактируется, только для отображения
            etEmail.setEnabled(false);
        }
    }

    /**
     * Проверка разрешения и открытие галереи
     */
    private void checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                
            requestPermissionLauncher.launch(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        } else {
            openGallery();
        }
    }
    
    /**
     * Открытие галереи для выбора изображения
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Необходимо разрешение для доступа к галерее", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Отображение диалога выбора даты
     */
    private void showDatePickerDialog() {
        try {
            // Если у пользователя уже установлена дата рождения, используем её
            if (!TextUtils.isEmpty(etBirthDate.getText())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(sdf.parse(etBirthDate.getText().toString()));
            }
        } catch (Exception e) {
            // При ошибке используем текущую дату
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    updateBirthDateField();
                },
                year, month, day);

        // Установка максимальной даты (сегодня)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Обновление поля даты рождения
     */
    private void updateBirthDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etBirthDate.setText(sdf.format(calendar.getTime()));
    }

    /**
     * Сохранение изменений профиля
     */
    private void saveProfileChanges() {
        String fullName = etFullName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Проверка ФИО
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Пожалуйста, введите ФИО", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка даты рождения
        if (TextUtils.isEmpty(birthDate)) {
            Toast.makeText(this, "Пожалуйста, укажите дату рождения", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка паролей, если пользователь решил их изменить
        if (!TextUtils.isEmpty(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Сохранение изменений профиля
        boolean success = dataRepository.updateUserProfile(
                sessionManager.getUserId(),
                fullName,
                birthDate,
                newPassword.isEmpty() ? null : newPassword
        );
        
        // Сохранение фото профиля, если было выбрано
        if (selectedImageUri != null && !selectedImageUri.toString().equals(currentUser.getPhotoPath())) {
            boolean photoSuccess = dataRepository.updateUserProfilePhoto(
                    sessionManager.getUserId(),
                    selectedImageUri.toString()
            );
            
            if (!photoSuccess) {
                Toast.makeText(this, "Ошибка при сохранении фото профиля", Toast.LENGTH_SHORT).show();
            }
        }

        if (success) {
            Toast.makeText(this, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при обновлении профиля", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Проверяет, находится ли точка внутри view
     */
    private boolean isPointInsideView(float rawX, float rawY, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return (rawX >= x && rawX <= (x + view.getWidth()) &&
                rawY >= y && rawY <= (y + view.getHeight()));
    }
    
    /**
     * Показать/скрыть оверлей при наведении на фото
     */
    private void showPhotoOverlay(boolean show) {
        viewPhotoOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        ivCameraIcon.setVisibility(show ? View.VISIBLE : View.GONE);
    }
} 