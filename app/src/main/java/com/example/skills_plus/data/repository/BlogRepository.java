package com.example.skills_plus.data.repository;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skills_plus.di.BlogDbRef;
import com.example.skills_plus.di.UserDbRef;
import com.example.skills_plus.data.model.Blog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ==============================
 * BlogRepository
 * ==============================
 * This class handles all data operations related to blogs, including CRUD (Create, Read,
 * Update, Delete) and bookmarking.
 *
 * --- CONCEPTS USED ---
 * - OOP (Abstraction & Encapsulation): Hides the complex Firebase implementation details
 * behind a clean and simple API (e.g., getAllBlogs(), createBlog()).
 * - Android (Repository Pattern): Acts as the single source of truth for all blog data,
 * isolating the data sources from the rest of the app.
 * - Android (LiveData): Uses LiveData to provide reactive, lifecycle-aware data streams
 * that the UI can observe.
 * - MVVM (Repository Layer): This class represents the "Model" part of the architecture,
 * but it's more accurately the data layer that the ViewModel communicates with.
 */
@Singleton
public class BlogRepository {

    private final DatabaseReference blogDatabaseReference;
    private final DatabaseReference usersDatabaseReference;
    private final StorageReference storageReference;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<List<Blog>> allBlogsLiveData;
    private final MutableLiveData<List<Blog>> userBlogsLiveData;
    private final MutableLiveData<List<Blog>> bookmarkedBlogsLiveData;
    private final MutableLiveData<Map<String, Boolean>> bookmarkStatusLiveData;

    @Inject
    public BlogRepository(FirebaseAuth firebaseAuth,
                          @UserDbRef DatabaseReference usersDatabaseReference,
                          @BlogDbRef DatabaseReference blogDatabaseReference,
                          StorageReference storageReference) {
        this.blogDatabaseReference = blogDatabaseReference;
        this.usersDatabaseReference = usersDatabaseReference;
        this.storageReference = storageReference;
        this.firebaseAuth = firebaseAuth;
        allBlogsLiveData = new MutableLiveData<>();
        userBlogsLiveData = new MutableLiveData<>();
        bookmarkedBlogsLiveData = new MutableLiveData<>();
        bookmarkStatusLiveData = new MutableLiveData<>();
        listenForBookmarkChanges();
    }

    /**
     * Fetches all blogs from the Firebase Realtime Database.
     *
     * --- THREADING EXPLANATION ---
     * The `addValueEventListener` is an asynchronous, non-blocking call. The Firebase SDK
     * automatically performs the network request on its own background thread. You do NOT need
     * to wrap this in `new Thread()`. The `onDataChange` callback is then safely returned
     * to the main UI thread, where LiveData can be updated.
     */
    public LiveData<List<Blog>> getAllBlogs() {
        blogDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Blog> blogs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        blogs.add(blog);
                    }
                }
                allBlogsLiveData.postValue(blogs);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                allBlogsLiveData.postValue(null);
            }
        });
        return allBlogsLiveData;
    }

    /**
     * Fetches blogs published by the current user. This also runs on a background thread
     * managed by the Firebase SDK.
     */
    public LiveData<List<Blog>> getUserBlogs() {
        String userId = firebaseAuth.getUid();
        if (userId == null) return userBlogsLiveData;

        blogDatabaseReference.orderByChild("authorId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Blog> blogs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Blog blog = dataSnapshot.getValue(Blog.class);
                    if (blog != null) {
                        blogs.add(blog);
                    }
                }
                userBlogsLiveData.postValue(blogs);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userBlogsLiveData.postValue(null);
            }
        });
        return userBlogsLiveData;
    }

    public LiveData<Map<String, Boolean>> getBookmarkStatusLiveData() {
        return bookmarkStatusLiveData;
    }

    public LiveData<List<Blog>> getBookmarkedBlogs() {
        String userId = firebaseAuth.getUid();
        if (userId == null) {
            bookmarkedBlogsLiveData.postValue(new ArrayList<>());
            return bookmarkedBlogsLiveData;
        }

        DatabaseReference favoritesRef = usersDatabaseReference.child(userId).child("favorites");
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> favoriteIds = new ArrayList<>();
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    favoriteIds.add(idSnapshot.getKey());
                }
                fetchBlogsByIds(favoriteIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bookmarkedBlogsLiveData.postValue(null);
            }
        });
        return bookmarkedBlogsLiveData;
    }

    private void fetchBlogsByIds(List<String> blogIds) {
        List<Blog> blogs = new ArrayList<>();
        if (blogIds.isEmpty()) {
            bookmarkedBlogsLiveData.postValue(blogs);
            return;
        }
        final int[] blogsToFetch = {blogIds.size()};
        for (String id : blogIds) {
            blogDatabaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Blog blog = snapshot.getValue(Blog.class);
                    if (blog != null) {
                        blogs.add(blog);
                    }
                    blogsToFetch[0]--;
                    if (blogsToFetch[0] == 0) {
                        bookmarkedBlogsLiveData.postValue(blogs);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    blogsToFetch[0]--;
                    if (blogsToFetch[0] == 0) {
                        bookmarkedBlogsLiveData.postValue(blogs);
                    }
                }
            });
        }
    }

    public void toggleBookmarkStatus(String blogId) {
        String userId = firebaseAuth.getUid();
        if (userId == null || blogId == null) return;

        DatabaseReference favoritesRef = usersDatabaseReference.child(userId).child("favorites").child(blogId);
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favoritesRef.removeValue();
                } else {
                    favoritesRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForBookmarkChanges() {
        String userId = firebaseAuth.getUid();
        if (userId == null) return;

        DatabaseReference favoritesRef = usersDatabaseReference.child(userId).child("favorites");
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Boolean> statusMap = new HashMap<>();
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    statusMap.put(idSnapshot.getKey(), true);
                }
                bookmarkStatusLiveData.postValue(statusMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- CRUD methods ---
    public void createBlog(String title, String description, Uri imageUri) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null || imageUri == null) return;

        String uid = currentUser.getUid();
        final StorageReference imageRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String blogId = blogDatabaseReference.push().getKey();
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    Blog blog = new Blog(uid, blogId, title, description, downloadUri.toString(), timestamp);

                    if (blogId != null) {
                        blogDatabaseReference.child(blogId).setValue(blog);
                    }
                })
        );
    }

    public void updateBlog(String blogId, String title, String description, Uri imageUri, String existingImageUrl) {
        if (imageUri != null) {
            StorageReference newImageRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");
            newImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    newImageRef.getDownloadUrl().addOnSuccessListener(downloadUri ->
                            updateBlogData(blogId, title, description, downloadUri.toString())
                    )
            );
        } else {
            updateBlogData(blogId, title, description, existingImageUrl);
        }
    }

    private void updateBlogData(String blogId, String title, String description, String imageUrl) {
        DatabaseReference blogRef = blogDatabaseReference.child(blogId);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("image", imageUrl);
        updates.put("timeStamp", timestamp);

        blogRef.updateChildren(updates);
    }

    public void deleteBlog(String blogId) {
        if (blogId != null) {
            blogDatabaseReference.child(blogId).removeValue();
        }
    }
}
