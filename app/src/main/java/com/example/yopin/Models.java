package com.example.yopin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Класс-контейнер для всех моделей данных приложения
 */
public class Models {
    
    /**
     * Модель пользователя
     */
    public static class User {
        private int id;
        private String email;
        private String fullName;
        private String birthDate;
        private String photoPath;

        public User() {}

        public User(int id, String email, String fullName, String birthDate) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.birthDate = birthDate;
            this.photoPath = "";
        }

        public User(int id, String email, String fullName, String birthDate, String photoPath) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.birthDate = birthDate;
            this.photoPath = photoPath;
        }

        public int getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getBirthDate() { return birthDate; }
        public String getPhotoPath() { return photoPath; }

        public void setId(int id) { this.id = id; }
        public void setEmail(String email) { this.email = email; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
        public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    }

    /**
     * Модель книги
     */
    public static class Book {
        private int id;
        private String title;
        private String author;
        private String genre;
        private String year;
        private List<Review> reviews;
        private float averageRating;
        private String coverImage;

        public Book() {
            this.reviews = new ArrayList<>();
            this.averageRating = 0.0f;
        }

        public Book(int id, String title, String author, String genre, String year) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.year = year;
            this.reviews = new ArrayList<>();
            this.averageRating = 0.0f;
        }

        public Book(int id, String title, String author, String genre, String year, 
                    List<Review> reviews, float averageRating, String coverImage) {
            this(id, title, author, genre, year);
            this.reviews = reviews != null ? reviews : new ArrayList<>();
            this.averageRating = averageRating;
            this.coverImage = coverImage;
        }

        // Геттеры
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getGenre() { return genre; }
        public String getYear() { return year; }
        public List<Review> getReviews() { return reviews; }
        public float getAverageRating() { return averageRating; }
        public String getCoverImage() { return coverImage; }
        
        // Сеттеры
        public void setId(int id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setGenre(String genre) { this.genre = genre; }
        public void setYear(String year) { this.year = year; }
        public void setReviews(List<Review> reviews) { this.reviews = reviews; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
        
        public void addReview(Review review) {
            if (this.reviews == null) {
                this.reviews = new ArrayList<>();
            }
            this.reviews.add(review);
            updateAverageRating();
        }
        
        public void updateAverageRating() {
            if (reviews == null || reviews.isEmpty()) {
                this.averageRating = 0.0f;
                return;
            }
            
            float totalRating = 0.0f;
            int count = 0;
            
            for (Review review : reviews) {
                if (review.getRating() > 0) {
                    totalRating += review.getRating();
                    count++;
                }
            }
            
            this.averageRating = count > 0 ? totalRating / count : 0.0f;
        }
        
        public String getFormattedInfo() {
            StringBuilder info = new StringBuilder();
            info.append(title);
            
            if (author != null && !author.isEmpty()) {
                info.append(" - ").append(author);
            }
            
            if (genre != null && !genre.isEmpty()) {
                info.append(" (").append(genre).append(")");
            }
            
            if (year != null && !year.isEmpty()) {
                info.append(", ").append(year);
            }
            
            return info.toString();
        }
        
        public int getReviewCount() {
            return reviews != null ? reviews.size() : 0;
        }
    }

    /**
     * Модель отзыва
     */
    public static class Review {
        private int id;
        private int userId;
        private int bookId;
        private String reviewText;
        private int rating;
        private String date;
        private String username;
        private String bookTitle;
        private String bookAuthor;

        public Review() {}

        public Review(int id, int userId, int bookId, String reviewText, int rating, String date) {
            this.id = id;
            this.userId = userId;
            this.bookId = bookId;
            this.reviewText = reviewText;
            this.rating = rating;
            this.date = date;
        }

        public Review(int id, int userId, int bookId, String reviewText, int rating, String date, String username) {
            this(id, userId, bookId, reviewText, rating, date);
            this.username = username;
        }

        public Review(int id, int userId, int bookId, String reviewText, int rating, String date, 
                      String bookTitle, String bookAuthor) {
            this(id, userId, bookId, reviewText, rating, date);
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
        }

        public Review(int id, int userId, int bookId, String reviewText, int rating, String date, 
                      String username, String bookTitle, String bookAuthor, String unused1, String unused2) {
            this(id, userId, bookId, reviewText, rating, date, username);
            this.bookTitle = bookTitle;
            this.bookAuthor = bookAuthor;
        }

        // Геттеры
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public int getBookId() { return bookId; }
        public String getReviewText() { return reviewText; }
        public int getRating() { return rating; }
        public String getDate() { return date; }
        public String getUsername() { return username; }
        public String getBookTitle() { return bookTitle; }
        public String getBookAuthor() { return bookAuthor; }
        
        // Сеттеры
        public void setId(int id) { this.id = id; }
        public void setUserId(int userId) { this.userId = userId; }
        public void setBookId(int bookId) { this.bookId = bookId; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }
        public void setRating(int rating) { this.rating = rating; }
        public void setDate(String date) { this.date = date; }
        public void setUsername(String username) { this.username = username; }
        public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
        public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }
        
        public String getFormattedDate() {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
                Date parsedDate = inputFormat.parse(date);
                if (parsedDate != null) {
                    return outputFormat.format(parsedDate);
                }
            } catch (Exception e) {
                // В случае ошибки возвращаем оригинальную дату
            }
            return date;
        }
        
        public String getRatingStars() {
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < rating; i++) {
                stars.append("★");
            }
            for (int i = rating; i < 5; i++) {
                stars.append("☆");
            }
            return stars.toString();
        }
    }
    
    /**
     * Интерфейс для обратных вызовов
     */
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
} 