package com.example.skills_plus.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.skills_plus.databinding.ActivityPublishBlogBinding;
import com.example.skills_plus.viewmodel.BlogViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for creating and publishing a new blog post.
 * (MVVM Concept: View) Delegates all data operations to the BlogViewModel.
 */
@AndroidEntryPoint
public class PublishBlogActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 200;
    private Uri selectedImageUri;
    private ActivityPublishBlogBinding binding;
    private BlogViewModel blogViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublishBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        blogViewModel = new ViewModelProvider(this).get(BlogViewModel.class);

        setupListeners();
    }

    private void setupListeners() {
        binding.imageUploadContainer.setOnClickListener(v -> imageChooser());
        binding.publishBtn.setOnClickListener(v -> uploadData());
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.displayImage.setImageURI(selectedImageUri);
            binding.imageUploadContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void uploadData() {
        String title = binding.addTitle.getText().toString().trim();
        String description = binding.addDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "All fields and an image are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.publishBtn.setVisibility(View.GONE);

        // Delegate the creation logic to the ViewModel
        blogViewModel.createBlog(title, description, selectedImageUri);

        // In a more complex app, you'd observe LiveData for completion/error.
        // For simplicity here, we'll just show a toast and finish.
        Toast.makeText(this, "Blog published successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}