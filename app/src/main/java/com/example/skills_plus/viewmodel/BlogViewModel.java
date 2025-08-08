package com.example.skills_plus.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.skills_plus.model.Blog;
import com.example.skills_plus.repository.BlogRepository;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for blog-related UI components (WriteFragment, CommunityFragment, etc.).
 * Its responsibility is to prepare and manage blog data for the UI, surviving configuration
 * changes and connecting the View to the Repository.
 */
public class BlogViewModel extends AndroidViewModel {

    private final BlogRepository blogRepository;

    public BlogViewModel(@NonNull Application application) {
        super(application);
        blogRepository = new BlogRepository();
    }

    // --- Blog List Getters ---
    public LiveData<List<Blog>> getAllBlogs() {
        return blogRepository.getAllBlogs();
    }

    public LiveData<List<Blog>> getUserBlogs() {
        return blogRepository.getUserBlogs();
    }

    // --- CRUD Methods ---
    public void createBlog(String title, String description, Uri imageUri) {
        blogRepository.createBlog(title, description, imageUri);
    }

    public void updateBlog(String blogId, String title, String description, Uri imageUri, String existingImageUrl) {
        blogRepository.updateBlog(blogId, title, description, imageUri, existingImageUrl);
    }

    public void deleteBlog(String blogId) {
        blogRepository.deleteBlog(blogId);
    }

    // --- Bookmarking Methods ---
    public LiveData<Map<String, Boolean>> getBookmarkStatusLiveData() {
        return blogRepository.getBookmarkStatusLiveData();
    }

    public LiveData<List<Blog>> getBookmarkedBlogs() {
        return blogRepository.getBookmarkedBlogs();
    }

    public void toggleBookmark(String blogId) {
        blogRepository.toggleBookmarkStatus(blogId);
    }
}
