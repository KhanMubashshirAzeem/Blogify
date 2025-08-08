package com.example.skills_plus.view.activity;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.skills_plus.databinding.ActivityUpdateDeleteBinding;
import com.example.skills_plus.viewmodel.BlogViewModel;

/**
 * Activity for updating or deleting an existing blog post.
 * (MVVM Concept: View) It retrieves blog details and delegates update/delete actions
 * to the BlogViewModel.
 */
public class UpdateDeleteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ActivityUpdateDeleteBinding binding;
    private BlogViewModel blogViewModel;

    private String blogId;
    private Uri newImageUri;
    private String previousImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateDeleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        blogViewModel = new ViewModelProvider(this).get(BlogViewModel.class);

        loadIntentData();
        setupListeners();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        blogId = intent.getStringExtra("blogId");
        previousImageUrl = intent.getStringExtra("image");

        binding.titleUpdate.setText(intent.getStringExtra("title"));
        binding.descriptionUpdate.setText(intent.getStringExtra("description"));
        binding.timeUpdate.setText(intent.getStringExtra("timestamp"));
        Glide.with(this).load(previousImageUrl).into(binding.imageUpdate);
    }

    private void setupListeners() {
        binding.updateBlog.setOnClickListener(v -> updateBlog());
        binding.deleteBlog.setOnClickListener(v -> deleteBlog());
        binding.imageUpdate.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            newImageUri = data.getData();
            binding.imageUpdate.setImageURI(newImageUri);
        }
    }

    private void updateBlog() {
        String updatedTitle = binding.titleUpdate.getText().toString().trim();
        String updatedDescription = binding.descriptionUpdate.getText().toString().trim();

        if (TextUtils.isEmpty(updatedTitle) || TextUtils.isEmpty(updatedDescription)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delegate update to ViewModel
        blogViewModel.updateBlog(blogId, updatedTitle, updatedDescription, newImageUri, previousImageUrl);
        Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteBlog() {
        // Delegate delete to ViewModel
        blogViewModel.deleteBlog(blogId);
        Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}