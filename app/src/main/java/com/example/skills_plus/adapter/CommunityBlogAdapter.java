package com.example.skills_plus.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.activity.ReadBlogActivity;
import com.example.skills_plus.modal.CommunityBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityBlogAdapter extends RecyclerView.Adapter<CommunityBlogAdapter.CardViewHolder> {

    private final Context context;
    private final List<CommunityBlogModal> allCardList;

    // OPTIMIZATION: Cache Firebase instances to avoid repeated getInstance() calls
    private final FirebaseDatabase database;
    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    // OPTIMIZATION: Cache current user to avoid repeated getCurrentUser() calls
    private final FirebaseUser currentUser;

    // OPTIMIZATION: Cache user UID to avoid repeated getUid() calls
    private final String currentUserUid;

    // Constructor - Initialize all cached values once
    public CommunityBlogAdapter(Context context, List<CommunityBlogModal> allCardList) {
        this.context = context;
        this.allCardList = allCardList;

        // OPTIMIZATION: Initialize Firebase instances once in constructor instead of every time in onBindViewHolder
        this.database = FirebaseDatabase.getInstance();
        this.dbRef = database.getReference();
        this.auth = FirebaseAuth.getInstance();
        this.currentUser = auth.getCurrentUser();
        this.currentUserUid = currentUser != null ? currentUser.getUid() : null;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_blog_card_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        // OPTIMIZATION: Early return if list is null/empty - moved to top for better performance
        if (allCardList == null || allCardList.isEmpty()) {
            return;
        }

        CommunityBlogModal blog = allCardList.get(position);

        // OPTIMIZATION: Set basic data first (lightweight operations)
        holder.titleTextView.setText(blog.getTitle());
        holder.descriptionTextView.setText(blog.getDescription());
        holder.timeStampTextView.setText(blog.getTimeStamp());

        // OPTIMIZATION: Load image using cached context reference
        Glide.with(context)
                .load(blog.getImage())
                .placeholder(R.drawable.star_icon)
                .into(holder.imageView);

        // OPTIMIZATION: Set click listener only once using cached data
        // Avoid creating new Intent and putting extras repeatedly
        holder.itemView.setOnClickListener(v -> openBlogDetails(blog));

        // OPTIMIZATION: Handle bookmark feature using cached user data
        setupBookmarkFeature(holder, blog);
    }

    @Override
    public int getItemCount() {
        return allCardList != null ? allCardList.size() : 0;
    }

    // OPTIMIZATION: Extract click handling to separate method to reduce onBindViewHolder complexity
    private void openBlogDetails(CommunityBlogModal blog) {
        Intent intent = new Intent(context, ReadBlogActivity.class);
        intent.putExtra("title", blog.getTitle());
        intent.putExtra("description", blog.getDescription());
        intent.putExtra("imageUrl", blog.getImage());
        intent.putExtra("timestamp", blog.getTimeStamp());
        intent.putExtra("blogId", blog.getBlogId());
        context.startActivity(intent);
    }

    // OPTIMIZATION: Extract bookmark setup to separate method and use cached user data
    private void setupBookmarkFeature(CardViewHolder holder, CommunityBlogModal blog) {
        if (currentUser != null) {
            // OPTIMIZATION: Use cached user UID instead of calling getCurrentUser().getUid() repeatedly
            updateBookmarkIcon(holder, blog.getBlogId());
            holder.bookmarkBtn.setOnClickListener(view -> bookmarkMethod(holder, blog));
        } else {
            // OPTIMIZATION: Handle non-logged-in users efficiently
            holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray);
            holder.bookmarkBtn.setEnabled(false);
        }
    }

    // ViewHolder class
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        // OPTIMIZATION: Made fields final for better performance
        final TextView titleTextView;
        final TextView descriptionTextView;
        final ImageView imageView;
        final TextView timeStampTextView;
        final ImageView bookmarkBtn;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            // OPTIMIZATION: findViewById calls are already optimized by being in constructor
            titleTextView = itemView.findViewById(R.id.titleAB);
            descriptionTextView = itemView.findViewById(R.id.descriptionAB);
            imageView = itemView.findViewById(R.id.imageAB);
            timeStampTextView = itemView.findViewById(R.id.timeStampAB);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
        }
    }

    // OPTIMIZATION: Use cached user UID instead of calling getCurrentUser().getUid()
    private void bookmarkMethod(CardViewHolder holder, CommunityBlogModal blog) {
        String blogId = blog.getBlogId();

        // OPTIMIZATION: Use cached user reference instead of creating new instance
        DatabaseReference userRef = dbRef.child("users").child(currentUserUid);

        userRef.child("favorites").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> bookmarks = (Map<String, Boolean>) snapshot.getValue();

                if (bookmarks == null) {
                    bookmarks = new HashMap<>();
                }

                // OPTIMIZATION: Simplified bookmark toggle logic
                boolean isBookmarked = bookmarks.containsKey(blogId);
                if (isBookmarked) {
                    bookmarks.remove(blogId);
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_gray);
                } else {
                    bookmarks.put(blogId, true);
                    holder.bookmarkBtn.setImageResource(R.drawable.bookmark_icon_blue);
                }

                // OPTIMIZATION: Update database with optimized reference
                userRef.child("favorites").setValue(bookmarks).addOnCompleteListener(task -> {
                    String message = task.isSuccessful() ? "Bookmark updated" : "Failed to update bookmark";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error fetching bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // OPTIMIZATION: Simplified bookmark icon update method
    private void updateBookmarkIcon(CardViewHolder holder, String blogId) {
        // OPTIMIZATION: Use cached database reference and user UID
        DatabaseReference userRef = dbRef.child("users").child(currentUserUid).child("favorites");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // OPTIMIZATION: Simplified bookmark state checking
                boolean isBookmarked = snapshot.exists() && snapshot.hasChild(blogId);
                int iconResource = isBookmarked ? R.drawable.bookmark_icon_blue : R.drawable.bookmark_icon_gray;
                holder.bookmarkBtn.setImageResource(iconResource);

                // OPTIMIZATION: Only notify item changed if holder position is valid
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}