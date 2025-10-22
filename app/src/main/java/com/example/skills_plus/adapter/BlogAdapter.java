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
import com.example.skills_plus.data.model.Blog;
import com.example.skills_plus.ui.activity.UpdateDeleteActivity;
import java.util.List;

/**
 * RecyclerView Adapter for displaying a user's own blogs (in WriteFragment).
 */
public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.CardViewHolder> {

    private final Context context;
    private List<Blog> blogList;

    public BlogAdapter(Context context, List<Blog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blog_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Blog blog = blogList.get(position);
        holder.bind(blog);
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    /**
     * Updates the list of blogs in the adapter and notifies the RecyclerView.
     * @param newBlogs The new list of blogs.
     */
    public void updateBlogs(List<Blog> newBlogs) {
        this.blogList.clear();
        this.blogList.addAll(newBlogs);
        notifyDataSetChanged();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final ImageView imageView;
        private final TextView timeStampTextView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_);
            descriptionTextView = itemView.findViewById(R.id.description);
            imageView = itemView.findViewById(R.id.image);
            timeStampTextView = itemView.findViewById(R.id.timeStamp);
        }

        void bind(final Blog blog) {
            titleTextView.setText(blog.getTitle());
            descriptionTextView.setText(blog.getDescription());
            timeStampTextView.setText(blog.getTimeStamp());
            Glide.with(context).load(blog.getImage()).placeholder(R.drawable.circle_loader).into(imageView);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, UpdateDeleteActivity.class);
                intent.putExtra("title", blog.getTitle());
                intent.putExtra("description", blog.getDescription());
                intent.putExtra("timestamp", blog.getTimeStamp());
                intent.putExtra("image", blog.getImage());
                intent.putExtra("blogId", blog.getBlogId());
                context.startActivity(intent);
            });
        }
    }
}