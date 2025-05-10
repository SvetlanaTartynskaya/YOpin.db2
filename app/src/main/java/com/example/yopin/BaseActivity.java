package com.example.yopin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class BaseActivity extends AppCompatActivity {
    public static final int SCREEN_BOOKS_LIST = 0;
    public static final int SCREEN_BOOK_REVIEWS = 1;
    public static final int SCREEN_USER_REVIEWS = 2;
    public static final int SCREEN_PROFILE = 3;
    public static final int SCREEN_ADD_EDIT_BOOK = 4;
    public static final int SCREEN_ADD_EDIT_REVIEW = 5;
    
    protected DataRepository dataRepository;
    protected SessionManager sessionManager;
    protected int screenType;
    protected int itemId;
    protected RecyclerView recyclerView;
    protected UniversalAdapter adapter;
    protected EditText searchEditText;
    protected ImageButton clearSearchButton;
    protected String currentSearchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            dataRepository = DataRepository.getInstance(this);
            if (dataRepository == null) {
                Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }
            
            try {
                sessionManager = new SessionManager(this);
                if (!sessionManager.isLoggedIn()) {
                    Intent intent = new Intent(this, LoginActivity.class); // экран входа
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Ошибка сессии: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }

            screenType = getIntent().getIntExtra("screen_type", SCREEN_BOOKS_LIST);
            itemId = getIntent().getIntExtra("item_id", -1);
            setupScreen();
        } catch (Exception e) {
            Toast.makeText(this, "Произошла ошибка при загрузке: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            try {
                // По умолчанию показываем список книг
                screenType = SCREEN_BOOKS_LIST;
                setContentView(R.layout.activity_books);
                
                // Настройка RecyclerView
                recyclerView = findViewById(R.id.recyclerViewBooks);
                if (recyclerView != null) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                }
            } catch (Exception ex) {
                // Критическая ошибка, перенаправляем на экран входа
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadData();
    }
    
    /**
     * Настройка UI в зависимости от типа экрана
     */
    protected void setupScreen() {
        switch (screenType) {
            case SCREEN_BOOKS_LIST:
                setContentView(R.layout.activity_books);
                
                // Настройка RecyclerView
                recyclerView = findViewById(R.id.recyclerViewBooks);
                if (recyclerView != null) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                } else {
                    Toast.makeText(this, "Ошибка инициализации списка книг", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Настройка поиска
                setupSearch();
                
                // Настройка FAB для добавления книги
                findViewById(R.id.fabAddBook).setOnClickListener(v -> openAddEditBook(-1));
                
                // Настройка навигационных кнопок
                findViewById(R.id.btnMyReviews).setOnClickListener(v -> openUserReviews(sessionManager.getUserId()));
                findViewById(R.id.btnProfile).setOnClickListener(v -> openProfile());
                break;
                
            case SCREEN_BOOK_REVIEWS:
                setContentView(R.layout.activity_book_reviews);
                
                // Настройка RecyclerView
                recyclerView = findViewById(R.id.recyclerViewReviews);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                
                // Загружаем информацию о книге
                loadBookDetails();
                
                // Настройка кнопки добавления отзыва
                findViewById(R.id.btnAddReview).setOnClickListener(v -> {
                    // Проверяем, не писал ли пользователь уже отзыв на эту книгу
                    if (dataRepository.hasUserReviewedBook(sessionManager.getUserId(), itemId)) {
                        Toast.makeText(this, "Вы уже написали отзыв на эту книгу", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Переходим к добавлению отзыва
                    openAddEditReview(-1, itemId);
                });
                break;
                
            case SCREEN_USER_REVIEWS:
                setContentView(R.layout.activity_user_reviews);
                
                // Настройка RecyclerView
                recyclerView = findViewById(R.id.recyclerViewUserReviews);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                break;
                
            case SCREEN_PROFILE:
                setContentView(R.layout.activity_profile);
                
                // Загружаем данные пользователя
                Models.User user = dataRepository.getUser(sessionManager.getUserEmail());
                if (user != null) {
                    // Получаем имя пользователя из базы данных
                    String username = dataRepository.getUsernameById(user.getId());
                    // Отображаем имя пользователя под иконкой
                    ((TextView) findViewById(R.id.tvFullName)).setText(username);
                    ((TextView) findViewById(R.id.tvUsername)).setText(username);
                    ((TextView) findViewById(R.id.tvEmail)).setText(user.getEmail());
                    ((TextView) findViewById(R.id.tvBirthDate)).setText(user.getBirthDate());
                    ((TextView) findViewById(R.id.tvFullNameData)).setText(user.getFullName());
                }
                
                // Кнопка редактирования профиля
                findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
                    Intent intent = new Intent(BaseActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                });
                
                // Кнопка перехода к списку книг
                findViewById(R.id.btnViewBooks).setOnClickListener(v -> openBooksList());
                
                // Кнопка для просмотра отзывов пользователя
                findViewById(R.id.btnMyReviews).setOnClickListener(v -> openUserReviews(sessionManager.getUserId()));
                
                // Кнопка выхода
                findViewById(R.id.btnLogout).setOnClickListener(v -> {
                    sessionManager.logoutUser();
                    openLoginScreen();
                });
                break;
                
            case SCREEN_ADD_EDIT_BOOK:
                setContentView(R.layout.activity_add_book);
                
                // Если это редактирование книги, заполняем поля
                if (itemId > 0) {
                    Models.Book book = dataRepository.getBookById(itemId);
                    if (book != null) {
                        ((TextView) findViewById(R.id.etBookTitle)).setText(book.getTitle());
                        ((TextView) findViewById(R.id.etBookAuthor)).setText(book.getAuthor());
                        ((TextView) findViewById(R.id.etBookGenre)).setText(book.getGenre());
                        ((TextView) findViewById(R.id.etBookYear)).setText(book.getYear());
                    }
                    // Меняем заголовок
                    ((TextView) findViewById(R.id.tvTitle)).setText("Редактирование книги");
                }
                
                // Кнопка сохранения
                findViewById(R.id.btnSaveBook).setOnClickListener(v -> saveBook());
                break;
                
            case SCREEN_ADD_EDIT_REVIEW:
                setContentView(R.layout.activity_add_review);
                
                // Если это редактирование отзыва, заполняем поля
                if (itemId > 0) {
                    Models.Review review = dataRepository.getReviewById(itemId);
                    if (review != null) {
                        ((TextView) findViewById(R.id.etReviewText)).setText(review.getReviewText());
                        if (findViewById(R.id.ratingBar) != null) {
                            ((android.widget.RatingBar) findViewById(R.id.ratingBar)).setRating(review.getRating());
                        }
                        
                        // Показываем информацию о книге
                        if (findViewById(R.id.tvBookInfo) != null) {
                            ((TextView) findViewById(R.id.tvBookInfo)).setText(
                                    review.getBookTitle() + " - " + review.getBookAuthor());
                        }
                    }
                    // Меняем заголовок
                    if (findViewById(R.id.tvTitle) != null) {
                        ((TextView) findViewById(R.id.tvTitle)).setText("Редактирование отзыва");
                    }
                } else {
                    // Это новый отзыв, показываем информацию о книге
                    int bookId = getIntent().getIntExtra("book_id", -1);
                    if (bookId > 0 && findViewById(R.id.tvBookInfo) != null) {
                        Models.Book book = dataRepository.getBookById(bookId);
                        if (book != null) {
                            ((TextView) findViewById(R.id.tvBookInfo)).setText(
                                    book.getTitle() + " - " + book.getAuthor());
                        }
                    }
                }
                
                // Кнопка сохранения
                findViewById(R.id.btnSaveReview).setOnClickListener(v -> saveReview());
                break;
        }
    }
    
    /**
     * Настройка функциональности поиска
     */
    private void setupSearch() {
        searchEditText = findViewById(R.id.etSearchBooks);
        clearSearchButton = findViewById(R.id.btnClearSearch);
        
        if (searchEditText == null || clearSearchButton == null) {
            return;
        }
        
        // Настраиваем обработчик для кнопки очистки поиска
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchButton.setVisibility(View.GONE);
            currentSearchQuery = "";
            loadData(); // Загружаем все книги
        });
        
        // Обработчик изменения текста в поле поиска
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Не используется
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Показываем или скрываем кнопку очистки
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Сохраняем текущий запрос
                currentSearchQuery = s.toString().trim();
                
                // Если строка поиска пуста, показываем все книги
                if (currentSearchQuery.isEmpty()) {
                    loadData();
                    return;
                }
                
                // Если строка не пуста и длиннее 2 символов, выполняем поиск
                if (currentSearchQuery.length() >= 2) {
                    performSearch(currentSearchQuery);
                }
            }
        });
        
        // Обработчик нажатия кнопки "Поиск" на клавиатуре
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString().trim());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Выполнение поиска книг по запросу
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            loadData();
            return;
        }
        
        // Поиск книг по автору или названию
        List<Models.Book> searchResults = dataRepository.searchBooks(query);
        
        // Обновляем адаптер с результатами поиска
        if (adapter == null) {
            adapter = new UniversalAdapter(this, searchResults, UniversalAdapter.VIEW_TYPE_BOOK, sessionManager.getUserId());
            recyclerView.setAdapter(adapter);
            
            // Добавляем обработчик нажатия на карточку книги
            adapter.setOnItemClickListener(bookId -> openBookReviews(bookId));
        } else {
            adapter.updateData(searchResults);
            
            // Обновляем обработчик нажатия
            adapter.setOnItemClickListener(bookId -> openBookReviews(bookId));
        }
        
        // Показываем сообщение, если ничего не найдено
        TextView noResultsText = findViewById(R.id.tvNoBooksMessage);
        if (noResultsText != null) {
            if (searchResults.isEmpty()) {
                noResultsText.setText("По запросу \"" + query + "\" ничего не найдено");
                noResultsText.setVisibility(View.VISIBLE);
            } else {
                noResultsText.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Загрузка данных для текущего экрана
     */
    protected void loadData() {
        try {
            switch (screenType) {
                case SCREEN_BOOKS_LIST:
                    List<Models.Book> books;
                    
                    // Если есть поисковый запрос, используем его
                    if (!currentSearchQuery.isEmpty()) {
                        books = dataRepository.searchBooks(currentSearchQuery);
                    } else {
                        books = dataRepository.getAllBooks();
                    }
                    
                    // Обновляем адаптер
                    if (adapter == null) {
                        adapter = new UniversalAdapter(this, books, UniversalAdapter.VIEW_TYPE_BOOK, sessionManager.getUserId());
                        recyclerView.setAdapter(adapter);
                        
                        // Добавляем обработчик нажатия на карточку книги
                        adapter.setOnItemClickListener(bookId -> openBookReviews(bookId));
                    } else {
                        adapter.updateData(books);
                        
                        // Обновляем обработчик нажатия
                        adapter.setOnItemClickListener(bookId -> openBookReviews(bookId));
                    }
                    
                    // Показываем сообщение, если нет книг
                    TextView noResultsText = findViewById(R.id.tvNoBooksMessage);
                    if (noResultsText != null) {
                        if (books.isEmpty()) {
                            if (!currentSearchQuery.isEmpty()) {
                                noResultsText.setText("По запросу \"" + currentSearchQuery + "\" ничего не найдено");
                            } else {
                                noResultsText.setText("Нет доступных книг");
                            }
                            noResultsText.setVisibility(View.VISIBLE);
                        } else {
                            noResultsText.setVisibility(View.GONE);
                        }
                    }
                    break;
                
                case SCREEN_BOOK_REVIEWS:
                    // Получаем отзывы для книги
                    List<Models.Review> reviews = dataRepository.getReviewsForBook(itemId);
                    adapter = new UniversalAdapter(this, reviews, UniversalAdapter.VIEW_TYPE_REVIEW, sessionManager.getUserId());
                    recyclerView.setAdapter(adapter);
                    
                    // Настраиваем обработчики
                    adapter.setOnActionClickListener((reviewId, actionType) -> {
                        if (actionType == UniversalAdapter.ACTION_EDIT) {
                            openAddEditReview(reviewId, itemId);
                        }
                    });
                    
                    // Проверяем, есть ли отзывы
                    if (reviews.isEmpty() && findViewById(R.id.tvNoReviews) != null) {
                        findViewById(R.id.tvNoReviews).setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else if (findViewById(R.id.tvNoReviews) != null) {
                        findViewById(R.id.tvNoReviews).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    
                    // Проверяем, написал ли пользователь отзыв
                    boolean hasReviewed = dataRepository.hasUserReviewedBook(sessionManager.getUserId(), itemId);
                    if (hasReviewed && findViewById(R.id.btnAddReview) != null) {
                        ((android.widget.Button) findViewById(R.id.btnAddReview)).setText("Вы уже оставили отзыв");
                        findViewById(R.id.btnAddReview).setEnabled(false);
                    }
                    break;
                
                case SCREEN_USER_REVIEWS:
                    // Получаем отзывы пользователя
                    List<Models.Review> userReviews = dataRepository.getUserReviews(sessionManager.getUserId());
                    adapter = new UniversalAdapter(this, userReviews, UniversalAdapter.VIEW_TYPE_USER_REVIEW, sessionManager.getUserId());
                    recyclerView.setAdapter(adapter);
                    
                    // Настраиваем обработчики
                    adapter.setOnActionClickListener((reviewId, actionType) -> {
                        if (actionType == UniversalAdapter.ACTION_EDIT) {
                            Models.Review review = dataRepository.getReviewById(reviewId);
                            if (review != null) {
                                openAddEditReview(reviewId, review.getBookId());
                            }
                        }
                    });
                    
                    // Проверяем, есть ли отзывы
                    if (userReviews.isEmpty() && findViewById(R.id.tvNoReviews) != null) {
                        findViewById(R.id.tvNoReviews).setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else if (findViewById(R.id.tvNoReviews) != null) {
                        findViewById(R.id.tvNoReviews).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при загрузке данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Загрузка информации о книге
     */
    private void loadBookDetails() {
        Models.Book book = dataRepository.getBookById(itemId);
        if (book != null) {
            ((TextView) findViewById(R.id.tvBookTitle)).setText(book.getTitle());
            ((TextView) findViewById(R.id.tvBookAuthor)).setText(book.getAuthor());
            ((TextView) findViewById(R.id.tvBookGenre)).setText("Жанр: " + book.getGenre());
            ((TextView) findViewById(R.id.tvBookYear)).setText("Год: " + book.getYear());
        } else {
            Toast.makeText(this, "Ошибка загрузки информации о книге", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * Сохранение книги
     */
    private void saveBook() {
        String title = ((TextView) findViewById(R.id.etBookTitle)).getText().toString().trim();
        String author = ((TextView) findViewById(R.id.etBookAuthor)).getText().toString().trim();
        String genre = ((TextView) findViewById(R.id.etBookGenre)).getText().toString().trim();
        String year = ((TextView) findViewById(R.id.etBookYear)).getText().toString().trim();
        
        if (title.isEmpty() || author.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните название и автора", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Сохраняем книгу
        long result = dataRepository.addBook(title, author, genre, year);
        
        if (result != -1) {
            // Проверяем, была ли найдена существующая книга
            Models.Book existingBook = dataRepository.getBookByTitleAndAuthor(title, author);
            if (existingBook != null && existingBook.getId() == result) {
                // Книга уже существовала, перенаправляем на ее страницу
                Toast.makeText(this, "Такая книга уже существует", Toast.LENGTH_SHORT).show();
                openBookReviews((int) result);
            } else {
                // Новая книга успешно добавлена
                Toast.makeText(this, "Книга сохранена", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Ошибка при сохранении книги", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Сохранение отзыва
     */
    private void saveReview() {
        String reviewText = ((android.widget.EditText) findViewById(R.id.etReviewText)).getText().toString().trim();
        int rating = (int) ((android.widget.RatingBar) findViewById(R.id.ratingBar)).getRating();
        int bookId = getIntent().getIntExtra("book_id", -1);

        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, напишите отзыв", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rating <= 0) {
            Toast.makeText(this, "Пожалуйста, поставьте оценку книге", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookId <= 0) {
            Toast.makeText(this, "Ошибка: не указана книга", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (itemId > 0) {
            // Редактирование существующего отзыва - сохраняем только текст и рейтинг, дату не меняем
            success = dataRepository.updateReview(itemId, reviewText, rating);
        } else {
            // Добавление нового отзыва - указываем текущую дату
            String currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            success = dataRepository.addReview(sessionManager.getUserId(), bookId, reviewText, rating, currentDate);
        }

        if (success) {
            Toast.makeText(this, "Отзыв успешно сохранен", Toast.LENGTH_SHORT).show();
            // Вернуться к списку отзывов книги
            finish();
        } else {
            Toast.makeText(this, "Ошибка при сохранении отзыва", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Открыть экран книг
     */
    public void openBooksList() {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_BOOKS_LIST);
        startActivity(intent);
    }
    
    /**
     * Открыть экран отзывов книги
     */
    public void openBookReviews(int bookId) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_BOOK_REVIEWS);
        intent.putExtra("item_id", bookId);
        startActivity(intent);
    }
    
    /**
     * Открыть экран отзывов пользователя
     */
    public void openUserReviews(int userId) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_USER_REVIEWS);
        intent.putExtra("item_id", userId);
        startActivity(intent);
    }
    
    /**
     * Открыть экран профиля
     */
    public void openProfile() {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_PROFILE);
        startActivity(intent);
    }
    
    /**
     * Открыть экран добавления/редактирования книги
     */
    public void openAddEditBook(int bookId) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_ADD_EDIT_BOOK);
        if (bookId > 0) {
            intent.putExtra("item_id", bookId);
        }
        startActivity(intent);
    }
    
    /**
     * Открыть экран добавления/редактирования отзыва
     */
    public void openAddEditReview(int reviewId, int bookId) {
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("screen_type", SCREEN_ADD_EDIT_REVIEW);
        if (reviewId > 0) {
            intent.putExtra("item_id", reviewId);
        }
        intent.putExtra("book_id", bookId);
        startActivity(intent);
    }

    public void openLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 