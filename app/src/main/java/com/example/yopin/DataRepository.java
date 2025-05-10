package com.example.yopin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Локальное хранилище данных
 */
public class DataRepository extends SQLiteOpenHelper {
    private static final String TAG = "DataRepository";
    private static DataRepository instance;

    // Параметры базы данных
    private static final String DATABASE_NAME = "YopinDatabase.db";
    private static final int DATABASE_VERSION = 2;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PASSWORD = "password";
    private static final String COLUMN_USER_FULLNAME = "fullname";
    private static final String COLUMN_USER_USERNAME = "username";
    private static final String COLUMN_USER_BIRTHDATE = "birthdate";
    private static final String COLUMN_USER_PHOTO = "photo_path";

    // Таблица книг
    private static final String TABLE_BOOKS = "books";
    private static final String COLUMN_BOOK_ID = "id";
    private static final String COLUMN_BOOK_TITLE = "title";
    private static final String COLUMN_BOOK_AUTHOR = "author";
    private static final String COLUMN_BOOK_GENRE = "genre";
    private static final String COLUMN_BOOK_YEAR = "year";

    // Таблица отзывов
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "id";
    private static final String COLUMN_REVIEW_USER_ID = "user_id";
    private static final String COLUMN_REVIEW_BOOK_ID = "book_id";
    private static final String COLUMN_REVIEW_TEXT = "review_text";
    private static final String COLUMN_REVIEW_RATING = "rating";
    private static final String COLUMN_REVIEW_DATE = "date";

    private Context context;
    
    // Интерфейс для обратных вызовов
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private DataRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in getInstance");
            return null;
        }
        
        try {
            if (instance == null) {
                instance = new DataRepository(context.getApplicationContext());
            }
            return instance;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в getInstance: " + e.getMessage(), e);
            try {
                return new DataRepository(context.getApplicationContext());
            } catch (Exception ex) {
                Log.e(TAG, "Critical error creating DataRepository: " + ex.getMessage(), ex);
                return null;
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Таблица пользователей
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_FULLNAME + " TEXT,"
                + COLUMN_USER_USERNAME + " TEXT,"
                + COLUMN_USER_BIRTHDATE + " TEXT,"
                + COLUMN_USER_PHOTO + " TEXT" + ")";

        // Таблица книг
        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + "("
                + COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOK_TITLE + " TEXT,"
                + COLUMN_BOOK_AUTHOR + " TEXT,"
                + COLUMN_BOOK_GENRE + " TEXT,"
                + COLUMN_BOOK_YEAR + " TEXT,"
                + "UNIQUE(" + COLUMN_BOOK_TITLE + ", " + COLUMN_BOOK_AUTHOR + ")" + ")";

        // Таблица отзывов
        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REVIEW_USER_ID + " INTEGER,"
                + COLUMN_REVIEW_BOOK_ID + " INTEGER,"
                + COLUMN_REVIEW_TEXT + " TEXT,"
                + COLUMN_REVIEW_RATING + " INTEGER DEFAULT 0,"
                + COLUMN_REVIEW_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_REVIEW_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_REVIEW_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + ")" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_BOOKS_TABLE);
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }
    
    // ===== МЕТОДЫ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ =====
    
    /**
     * Регистрация нового пользователя
     */
    public boolean registerUser(String email, String password, String fullName, String username, String birthDate) {
        return addUser(email, password, fullName, username, birthDate);
    }
    
    /**
     * Проверка учетных данных пользователя
     */
    public boolean loginUser(String email, String password) {
        return checkUser(email, password);
    }

    /**
     * Добавление пользователя в БД
     */
    public boolean addUser(String email, String password, String fullName, String username, String birthDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_FULLNAME, fullName);
        values.put(COLUMN_USER_USERNAME, username);
        values.put(COLUMN_USER_BIRTHDATE, birthDate);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Проверка пользователя
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Получение пользователя по email
     */
    public Models.User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID, COLUMN_USER_EMAIL, COLUMN_USER_FULLNAME, COLUMN_USER_USERNAME, COLUMN_USER_BIRTHDATE, COLUMN_USER_PHOTO};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = null;
        Models.User user = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                int emailIndex = cursor.getColumnIndex(COLUMN_USER_EMAIL);
                int fullNameIndex = cursor.getColumnIndex(COLUMN_USER_FULLNAME);
                int birthDateIndex = cursor.getColumnIndex(COLUMN_USER_BIRTHDATE);
                int photoIndex = cursor.getColumnIndex(COLUMN_USER_PHOTO);
                
                // Проверяем, что индексы колонок найдены
                if (idIndex != -1 && emailIndex != -1 && fullNameIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String userEmail = cursor.getString(emailIndex);
                    String fullName = cursor.getString(fullNameIndex);
                    // Безопасно получаем дату рождения, если она есть
                    String birthDate = birthDateIndex != -1 ? cursor.getString(birthDateIndex) : "";
                    // Безопасно получаем путь к фото профиля, если он есть
                    String photoPath = photoIndex != -1 ? cursor.getString(photoIndex) : "";
                    
                    user = new Models.User(id, userEmail, fullName, birthDate, photoPath);
                } else {
                    Log.e(TAG, "Ошибка: не все колонки найдены в таблице пользователей");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении пользователя: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return user;
    }

    /**
     * Обновление даты рождения пользователя
     */
    public boolean updateUserBirthDate(int userId, String birthDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_BIRTHDATE, birthDate);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }
    
    /**
     * Проверка существования пользователя
     */
    public boolean userExists(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
    
    /**
     * Получение имени пользователя по ID
     */
    public String getUsernameById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = "";
        
        String[] columns = {COLUMN_USER_USERNAME};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                int usernameIndex = cursor.getColumnIndex(COLUMN_USER_USERNAME);
                if (usernameIndex >= 0) {
                    username = cursor.getString(usernameIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return username;
    }
    
    // ===== МЕТОДЫ РАБОТЫ С КНИГАМИ =====
    
    /**
     * Добавление новой книги
     */
    public long addBook(String title, String author, String genre, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, title);
        values.put(COLUMN_BOOK_AUTHOR, author);
        values.put(COLUMN_BOOK_GENRE, genre);
        values.put(COLUMN_BOOK_YEAR, year);

        // Проверяем, существует ли уже такая книга
        long bookId = -1;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BOOKS,
                    new String[]{COLUMN_BOOK_ID},
                    COLUMN_BOOK_TITLE + " = ? AND " + COLUMN_BOOK_AUTHOR + " = ?",
                    new String[]{title, author}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                if (columnIndex >= 0) {
                    bookId = cursor.getLong(columnIndex);
                }
            } else {
                bookId = db.insert(TABLE_BOOKS, null, values);
            }
            return bookId;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Получение книги по ID
     */
    public Models.Book getBookById(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_GENRE, COLUMN_BOOK_YEAR};
        String selection = COLUMN_BOOK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(bookId)};

        Cursor cursor = null;
        Models.Book book = null;
        try {
            cursor = db.query(TABLE_BOOKS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                int genreIndex = cursor.getColumnIndex(COLUMN_BOOK_GENRE);
                int yearIndex = cursor.getColumnIndex(COLUMN_BOOK_YEAR);

                if (idIndex >= 0 && titleIndex >= 0 && authorIndex >= 0 && genreIndex >= 0 && yearIndex >= 0) {
                    book = new Models.Book(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(authorIndex),
                            cursor.getString(genreIndex),
                            cursor.getString(yearIndex)
                    );
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return book;
    }

    /**
     * Получение книги по названию и автору
     */
    public Models.Book getBookByTitleAndAuthor(String title, String author) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_BOOK_ID, COLUMN_BOOK_TITLE, COLUMN_BOOK_AUTHOR, COLUMN_BOOK_GENRE, COLUMN_BOOK_YEAR};
        String selection = COLUMN_BOOK_TITLE + " = ? AND " + COLUMN_BOOK_AUTHOR + " = ?";
        String[] selectionArgs = {title, author};

        Cursor cursor = null;
        Models.Book book = null;
        try {
            cursor = db.query(TABLE_BOOKS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                int genreIndex = cursor.getColumnIndex(COLUMN_BOOK_GENRE);
                int yearIndex = cursor.getColumnIndex(COLUMN_BOOK_YEAR);

                if (idIndex >= 0 && titleIndex >= 0 && authorIndex >= 0 && genreIndex >= 0 && yearIndex >= 0) {
                    book = new Models.Book(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(authorIndex),
                            cursor.getString(genreIndex),
                            cursor.getString(yearIndex)
                    );
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return book;
    }

    /**
     * Получение всех книг
     */
    public List<Models.Book> getAllBooks() {
        List<Models.Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BOOKS, null, null, null, null, null, COLUMN_BOOK_TITLE);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);
                int genreIndex = cursor.getColumnIndex(COLUMN_BOOK_GENRE);
                int yearIndex = cursor.getColumnIndex(COLUMN_BOOK_YEAR);

                if (idIndex >= 0 && titleIndex >= 0 && authorIndex >= 0 && genreIndex >= 0 && yearIndex >= 0) {
                    do {
                        Models.Book book = new Models.Book(
                                cursor.getInt(idIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(authorIndex),
                                cursor.getString(genreIndex),
                                cursor.getString(yearIndex)
                        );
                        books.add(book);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return books;
    }
    
    // ===== МЕТОДЫ РАБОТЫ С ОТЗЫВАМИ =====
    
    /**
     * Добавление отзыва
     */
    public boolean addReview(int userId, int bookId, String reviewText, int rating, String reviewDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_USER_ID, userId);
        values.put(COLUMN_REVIEW_BOOK_ID, bookId);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_RATING, rating);
        values.put(COLUMN_REVIEW_DATE, reviewDate);

        long result = db.insert(TABLE_REVIEWS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Получение всех отзывов для книги
     */
    public List<Models.Review> getReviewsForBook(int bookId) {
        List<Models.Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT r.*, u." + COLUMN_USER_FULLNAME + " FROM " + TABLE_REVIEWS + " r "
                + "JOIN " + TABLE_USERS + " u ON r." + COLUMN_REVIEW_USER_ID + " = u." + COLUMN_USER_ID
                + " WHERE r." + COLUMN_REVIEW_BOOK_ID + " = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_REVIEW_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int fullnameIndex = cursor.getColumnIndex(COLUMN_USER_FULLNAME);

                if (idIndex >= 0 && userIdIndex >= 0 && bookIdIndex >= 0 && textIndex >= 0 && ratingIndex >= 0
                        && dateIndex >= 0 && fullnameIndex >= 0) {
                    do {
                        Models.Review review = new Models.Review(
                                cursor.getInt(idIndex),
                                cursor.getInt(userIdIndex),
                                cursor.getInt(bookIdIndex),
                                cursor.getString(textIndex),
                                cursor.getInt(ratingIndex),
                                cursor.getString(dateIndex),
                                cursor.getString(fullnameIndex)
                        );
                        reviews.add(review);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return reviews;
    }

    /**
     * Получение всех отзывов пользователя
     */
    public List<Models.Review> getUserReviews(int userId) {
        List<Models.Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT r.*, b." + COLUMN_BOOK_TITLE + ", b." + COLUMN_BOOK_AUTHOR + " FROM " + TABLE_REVIEWS + " r "
                + "JOIN " + TABLE_BOOKS + " b ON r." + COLUMN_REVIEW_BOOK_ID + " = b." + COLUMN_BOOK_ID
                + " WHERE r." + COLUMN_REVIEW_USER_ID + " = ?";
        
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_REVIEW_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);

                if (idIndex >= 0 && userIdIndex >= 0 && bookIdIndex >= 0 && textIndex >= 0 && ratingIndex >= 0
                        && dateIndex >= 0 && titleIndex >= 0 && authorIndex >= 0) {
                    do {
                        Models.Review review = new Models.Review(
                                cursor.getInt(idIndex),
                                cursor.getInt(userIdIndex),
                                cursor.getInt(bookIdIndex),
                                cursor.getString(textIndex),
                                cursor.getInt(ratingIndex),
                                cursor.getString(dateIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(authorIndex)
                        );
                        reviews.add(review);
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return reviews;
    }

    /**
     * Проверка, написал ли пользователь отзыв на книгу
     */
    public boolean hasUserReviewedBook(int userId, int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_REVIEW_ID};
        String selection = COLUMN_REVIEW_USER_ID + " = ? AND " + COLUMN_REVIEW_BOOK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId), String.valueOf(bookId)};
        
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_REVIEWS, columns, selection, selectionArgs, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Обновление отзыва
     */
    public boolean updateReview(int reviewId, String reviewText, int rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_RATING, rating);

        int rowsAffected = db.update(TABLE_REVIEWS, values,
                COLUMN_REVIEW_ID + " = ?",
                new String[]{String.valueOf(reviewId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Получение отзыва по ID
     */
    public Models.Review getReviewById(int reviewId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT r.*, u." + COLUMN_USER_FULLNAME + ", b." + COLUMN_BOOK_TITLE + ", b." + COLUMN_BOOK_AUTHOR
                + " FROM " + TABLE_REVIEWS + " r "
                + "JOIN " + TABLE_USERS + " u ON r." + COLUMN_REVIEW_USER_ID + " = u." + COLUMN_USER_ID + " "
                + "JOIN " + TABLE_BOOKS + " b ON r." + COLUMN_REVIEW_BOOK_ID + " = b." + COLUMN_BOOK_ID + " "
                + "WHERE r." + COLUMN_REVIEW_ID + " = ?";
        
        Cursor cursor = null;
        Models.Review review = null;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(reviewId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_REVIEW_ID);
                int userIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_USER_ID);
                int bookIdIndex = cursor.getColumnIndex(COLUMN_REVIEW_BOOK_ID);
                int textIndex = cursor.getColumnIndex(COLUMN_REVIEW_TEXT);
                int ratingIndex = cursor.getColumnIndex(COLUMN_REVIEW_RATING);
                int dateIndex = cursor.getColumnIndex(COLUMN_REVIEW_DATE);
                int fullnameIndex = cursor.getColumnIndex(COLUMN_USER_FULLNAME);
                int titleIndex = cursor.getColumnIndex(COLUMN_BOOK_TITLE);
                int authorIndex = cursor.getColumnIndex(COLUMN_BOOK_AUTHOR);

                if (idIndex >= 0 && userIdIndex >= 0 && bookIdIndex >= 0 && textIndex >= 0 && ratingIndex >= 0
                        && dateIndex >= 0 && fullnameIndex >= 0 && titleIndex >= 0 && authorIndex >= 0) {
                    review = new Models.Review(
                            cursor.getInt(idIndex),
                            cursor.getInt(userIdIndex),
                            cursor.getInt(bookIdIndex),
                            cursor.getString(textIndex),
                            cursor.getInt(ratingIndex),
                            cursor.getString(dateIndex),
                            cursor.getString(fullnameIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(authorIndex),
                            null, null
                    );
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return review;
    }

    /**
     * Получение среднего рейтинга книги
     */
    public float getBookAverageRating(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + COLUMN_REVIEW_RATING + ") as average_rating FROM " + TABLE_REVIEWS
                + " WHERE " + COLUMN_REVIEW_BOOK_ID + " = ? AND " + COLUMN_REVIEW_RATING + " > 0";
        
        Cursor cursor = null;
        float averageRating = 0.0f;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});
            if (cursor != null && cursor.moveToFirst()) {
                averageRating = cursor.getFloat(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return averageRating;
    }

    /**
     * Получение количества отзывов о книге
     */
    public int getBookReviewCount(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_REVIEWS + " WHERE " + COLUMN_REVIEW_BOOK_ID + " = ?";
        
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return count;
    }

    /**
     * Обновление профиля пользователя
     */
    public boolean updateUserProfile(int userId, String fullName, String birthDate, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_USER_FULLNAME, fullName);
        values.put(COLUMN_USER_BIRTHDATE, birthDate);
        
        // Обновляем пароль только если он был предоставлен
        if (password != null && !password.isEmpty()) {
            values.put(COLUMN_USER_PASSWORD, password);
        }
        
        String whereClause = COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    /**
     * Обновление фото профиля пользователя
     */
    public boolean updateUserProfilePhoto(int userId, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_USER_PHOTO, photoPath);
        
        String whereClause = COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};
        
        int result = db.update(TABLE_USERS, values, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    /**
     * Удаление отзыва по ID
     */
    public boolean deleteReview(int reviewId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_REVIEW_ID + " = ?";
        String[] whereArgs = {String.valueOf(reviewId)};
        
        int result = db.delete(TABLE_REVIEWS, whereClause, whereArgs);
        db.close();
        
        return result > 0;
    }

    /**
     * Поиск книг по автору или названию
     * @param query Поисковый запрос
     * @return Список найденных книг
     */
    public List<Models.Book> searchBooks(String query) {
        List<Models.Book> results = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();
        
        // Поиск по всем книгам
        for (Models.Book book : getAllBooks()) {
            // Проверяем, содержит ли название или автор поисковый запрос
            if (book.getTitle().toLowerCase().contains(lowercaseQuery) || 
                book.getAuthor().toLowerCase().contains(lowercaseQuery)) {
                results.add(book);
            }
        }
        
        return results;
    }
} 