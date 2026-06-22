package com.example.bai1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface OnReplyListener {
        void onReply(int position, ReviewModel review);
    }

    private final List<ReviewModel> reviewList;
    private OnReplyListener replyListener;

    public ReviewAdapter(List<ReviewModel> reviewList) {
        this.reviewList = reviewList;
    }

    public void setOnReplyListener(OnReplyListener l) {
        this.replyListener = l;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        holder.tvName.setText(review.getUserName());
        holder.tvComment.setText(review.getComment());
        holder.tvDate.setText(review.getDate());
        holder.tvAvatar.setText(initial(review.getUserName()));

        if (review.getRating() > 0f) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(review.getRating());
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        // Cấp 2: render các câu trả lời
        holder.llReplies.removeAllViews();
        List<ReviewModel> replies = review.getReplies();
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        for (ReviewModel r : replies) {
            View rv = inflater.inflate(R.layout.item_reply, holder.llReplies, false);
            ((TextView) rv.findViewById(R.id.tvReplyAvatar)).setText(initial(r.getUserName()));
            ((TextView) rv.findViewById(R.id.tvReplyUser)).setText(r.getUserName());
            ((TextView) rv.findViewById(R.id.tvReplyComment)).setText(r.getComment());
            ((TextView) rv.findViewById(R.id.tvReplyDate)).setText(r.getDate());
            holder.llReplies.addView(rv);
        }

        holder.btnReply.setOnClickListener(v -> {
            if (replyListener != null) {
                replyListener.onReply(holder.getAdapterPosition(), review);
            }
        });
    }

    private String initial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        return name.trim().substring(0, 1).toUpperCase();
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvComment, tvDate, tvAvatar, btnReply;
        RatingBar ratingBar;
        LinearLayout llReplies;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvReviewUser);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvAvatar = itemView.findViewById(R.id.tvReviewAvatar);
            btnReply = itemView.findViewById(R.id.btnReply);
            ratingBar = itemView.findViewById(R.id.reviewRating);
            llReplies = itemView.findViewById(R.id.llReplies);
        }
    }
}
