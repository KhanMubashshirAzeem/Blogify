package com.example.skills_plus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.model.Blog;
import com.example.skills_plus.ui.activity.ReadBlogActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityBlogAdapter extends RecyclerView.Adapter<CommunityBlogAdapter.CardViewHolder> {

    public interface OnBookmarkClickListener {
        void onBookmarkClick(String blogId);
    }

    private final Context context;
    private List<Blog> blogList;
    private Map<String, Boolean> bookmarkStatusMap;
    private final OnBookmarkClickListener bookmarkClickListener;

    public CommunityBlogAdapter(Context context, List<Blog> blogList, OnBookmarkClickListener listener) {
        this.context = context;
        this.blogList = blogList;
        this.bookmarkClickListener = listener;
        this.bookmarkStatusMap = new HashMap<>();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_blog_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        boolean isBookmarked = bookmarkStatusMap.getOrDefault(blog.getBlogId(), false);
        holder.bind(blog, isBookmarked);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public void updateBlogs(List<Blog> newBlogs) {
        this.blogList.clear();
        this.blogList.addAll(newBlogs);
        notifyDataSetChanged();
    }

    public void updateBookmarkStatus(Map<String, Boolean> newStatusMap) {
        this.bookmarkStatusMap = newStatusMap;
        notifyDataSetChanged();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;
        final TextView descriptionTextView;
        final ImageView imageView;
        final TextView timeStampTextView;
        final ImageView bookmarkBtn;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleAB);
            descriptionTextView = itemView.findViewById(R.id.descriptionAB);
            imageView = itemView.findViewById(R.id.imageAB);
            timeStampTextView = itemView.findViewById(R.id.timeStampAB);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
        }

        void bind(final Blog blog, final boolean isBookmarked) {
            titleTextView.setText(blog.getTitle());
            descriptionTextView.setText(blog.getDescription());
            timeStampTextView.setText(blog.getTimeStamp());
            Glide.with(context).load(blog.getImage()).placeholder(R.drawable.star_icon).into(imageView);

            if (isBookmarked) {
                bookmarkBtn.setImageResource(R.drawable.bookmark_icon_blue);
            } else {
                bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray);
            }

            bookmarkBtn.setOnClickListener(v -> {
                if (bookmarkClickListener != null) {
                    bookmarkClickListener.onBookmarkClick(blog.getBlogId());
                }
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReadBlogActivity.class);
                intent.putExtra("title", blog.getTitle());
                intent.putExtra("description", blog.getDescription());
                intent.putExtra("imageUrl", blog.getImage());
                intent.putExtra("timestamp", blog.getTimeStamp());
                intent.putExtra("blogId", blog.getBlogId());
                context.startActivity(intent);
            });
        }
    }
}
