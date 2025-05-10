package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Универсальный адаптер для отображения книг и отзывов
 */
public class UniversalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // Константы для типов элементов
    public static final int VIEW_TYPE_BOOK = 0;
    public static final int VIEW_TYPE_REVIEW = 1;
    public static final int VIEW_TYPE_USER_REVIEW = 2;

    private Context context;
    private int viewType;
    private int userId;
    private List<?> items; // Список элементов (books или reviews)
    private OnItemClickListener onItemClickListener;
    private OnActionClickListener onActionClickListener;
    private DecimalFormat ratingFormat = new DecimalFormat("0.0");
    private DataRepository dataRepository;

    // Интерфейсы для обратных вызовов
    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }

    public interface OnActionClickListener {
        void onActionClick(int itemId, int actionType);
    }

    // Константы для типов действий
    public static final int ACTION_EDIT = 0;
    public static final int ACTION_WRITE_REVIEW = 1;

    /**
     * Конструктор адаптера
     * @param context контекст
     * @param items список элементов (книги или отзывы)
     * @param viewType тип представления (VIEW_TYPE_BOOK, VIEW_TYPE_REVIEW, VIEW_TYPE_USER_REVIEW)
     * @param userId ID текущего пользователя
     */
    public UniversalAdapter(Context context, List<?> items, int viewType, int userId) {
        this.context = context;
        this.items = items;
        this.viewType = viewType;
        this.userId = userId;
        this.dataRepository = DataRepository.getInstance(context);
    }

    /**
     * Установка обработчика нажатий на элементы
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Установка обработчика нажатий на кнопки действий
     */
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.onActionClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (this.viewType) {
            case VIEW_TYPE_BOOK:
                View bookView = inflater.inflate(R.layout.item_book, parent, false);
                return new BookViewHolder(bookView);
            
            case VIEW_TYPE_REVIEW:
                View reviewView = inflater.inflate(R.layout.item_review, parent, false);
                return new ReviewViewHolder(reviewView);
            
            case VIEW_TYPE_USER_REVIEW:
                View userReviewView = inflater.inflate(R.layout.item_user_review, parent, false);
                return new UserReviewViewHolder(userReviewView);
            
            default:
                throw new IllegalArgumentException("Неизвестный тип представления: " + this.viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (viewType) {
            case VIEW_TYPE_BOOK:
                bindBookViewHolder((BookViewHolder) holder, (Models.Book) items.get(position));
                break;
            
            case VIEW_TYPE_REVIEW:
                bindReviewViewHolder((ReviewViewHolder) holder, (Models.Review) items.get(position));
                break;
            
            case VIEW_TYPE_USER_REVIEW:
                bindUserReviewViewHolder((UserReviewViewHolder) holder, (Models.Review) items.get(position));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Привязка данных к представлению книги
     */
    private void bindBookViewHolder(BookViewHolder holder, Models.Book book) {
        holder.tvBookTitle.setText(book.getTitle());
        holder.tvBookAuthor.setText(book.getAuthor());
        
        // Жанр и год с учетом пустых значений
        if (book.getGenre() != null && !book.getGenre().isEmpty()) {
            holder.tvBookGenre.setText(book.getGenre());
            holder.tvBookGenre.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookGenre.setVisibility(View.GONE);
        }
        
        if (book.getYear() != null && !book.getYear().isEmpty()) {
            holder.tvBookYear.setText(book.getYear());
            holder.tvBookYear.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookYear.setVisibility(View.GONE);
        }
        
        // Отображение рейтинга книги если элементы есть в разметке
        if (holder.ratingBar != null && holder.tvRatingValue != null && holder.tvReviewCount != null) {
            float rating = dataRepository.getBookAverageRating(book.getId());
            int reviewCount = dataRepository.getBookReviewCount(book.getId());
            
            holder.ratingBar.setRating(rating);
            holder.tvRatingValue.setText(ratingFormat.format(rating));
            
            // Отображение количества отзывов
            String reviewText = context.getResources().getQuantityString(
                R.plurals.review_count, reviewCount, reviewCount);
            holder.tvReviewCount.setText(reviewText);
            
            // Анимация иконки книги если она есть
            if (holder.ivBookIcon != null) {
                holder.ivBookIcon.setAlpha(0.9f);
                holder.ivBookIcon.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .start();
            }
        }

        // Обработка нажатия на элемент
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(book.getId());
            }
        });
        
        // Кнопка "Написать отзыв" если она есть
        if (holder.btnWriteReview != null) {
            holder.btnWriteReview.setOnClickListener(v -> {
                if (onActionClickListener != null) {
                    onActionClickListener.onActionClick(book.getId(), ACTION_WRITE_REVIEW);
                }
            });
        }
    }

    /**
     * Привязка данных к представлению отзыва
     */
    private void bindReviewViewHolder(ReviewViewHolder holder, Models.Review review) {
        holder.tvReviewUsername.setText(review.getUsername());
        holder.tvReviewDate.setText(review.getFormattedDate());
        holder.tvReviewText.setText(review.getReviewText());
        
        // Отображение информации о книге
        String bookInfo = review.getBookTitle();
        if (review.getBookAuthor() != null && !review.getBookAuthor().isEmpty()) {
            bookInfo += " - " + review.getBookAuthor();
        }
        holder.tvReviewBookInfo.setText(bookInfo);
        
        // Установка рейтинга
        holder.rbReviewRating.setRating(review.getRating());
        
        // Отображение кнопок редактирования/удаления только для отзывов текущего пользователя
        if (userId == review.getUserId()) {
            holder.layoutReviewActions.setVisibility(View.VISIBLE);
            holder.btnEditReview.setVisibility(View.VISIBLE);
            holder.btnDeleteReview.setVisibility(View.VISIBLE);
            
            // Обработка нажатия на кнопку редактирования
            holder.btnEditReview.setOnClickListener(v -> {
                if (onActionClickListener != null) {
                    onActionClickListener.onActionClick(review.getId(), ACTION_EDIT);
                }
            });
            
            // Обработка нажатия на кнопку удаления
            holder.btnDeleteReview.setOnClickListener(v -> {
                // TODO: добавить диалог подтверждения удаления
                if (dataRepository.deleteReview(review.getId())) {
                    Toast.makeText(context, "Отзыв удален", Toast.LENGTH_SHORT).show();
                    // Обновляем список
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Ошибка при удалении отзыва", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.layoutReviewActions.setVisibility(View.GONE);
        }
        
        // Обработка нажатия на весь элемент
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(review.getId());
            }
        });
    }

    /**
     * Привязка данных к представлению отзыва пользователя
     */
    private void bindUserReviewViewHolder(UserReviewViewHolder holder, Models.Review review) {
        if (review.getBookId() > 0) {
            holder.tvBookInfo.setText(review.getBookTitle() + " - " + review.getBookAuthor());
            holder.tvBookInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookInfo.setVisibility(View.GONE);
        }
        
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvReviewDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());
        
        // Показываем рейтинг только если он установлен
        if (review.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Обработка нажатия на элемент
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(review.getId());
            }
        });
        
        // Обработка долгого нажатия для редактирования
        holder.itemView.setOnLongClickListener(v -> {
            if (onActionClickListener != null) {
                onActionClickListener.onActionClick(review.getId(), ACTION_EDIT);
                return true;
            }
            return false;
        });
    }

    /**
     * ViewHolder для книги
     */
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitle, tvBookAuthor, tvBookGenre, tvBookYear, tvRatingValue, tvReviewCount;
        RatingBar ratingBar;
        Button btnWriteReview;
        ImageView ivBookIcon;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookGenre = itemView.findViewById(R.id.tvBookGenre);
            tvBookYear = itemView.findViewById(R.id.tvBookYear);
            
            // Необязательные элементы (могут отсутствовать в простой разметке item_book)
            ratingBar = itemView.findViewById(R.id.ratingBarBook);
            tvRatingValue = itemView.findViewById(R.id.tvRatingValue);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            ivBookIcon = itemView.findViewById(R.id.ivBookIcon);
        }
    }

    /**
     * ViewHolder для отзыва
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewUsername, tvReviewDate, tvReviewText, tvReviewBookInfo;
        RatingBar rbReviewRating;
        Button btnEditReview, btnDeleteReview;
        View layoutReviewActions;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewUsername = itemView.findViewById(R.id.tvReviewUsername);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvReviewBookInfo = itemView.findViewById(R.id.tvReviewBookInfo);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
            btnEditReview = itemView.findViewById(R.id.btnEditReview);
            btnDeleteReview = itemView.findViewById(R.id.btnDeleteReview);
            layoutReviewActions = itemView.findViewById(R.id.layoutReviewActions);
        }
    }

    /**
     * ViewHolder для отзыва пользователя
     */
    public static class UserReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookInfo, tvReviewText, tvReviewDate;
        RatingBar ratingBar;

        public UserReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookInfo = itemView.findViewById(R.id.tvBookInfo);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

    /**
     * Обновляет данные в адаптере
     * @param newData Новый список данных
     */
    public void updateData(List<?> newData) {
        this.items = newData;
        notifyDataSetChanged();
    }
} 