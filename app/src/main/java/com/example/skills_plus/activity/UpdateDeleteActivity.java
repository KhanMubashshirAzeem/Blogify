package com.example.skills_plus.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityUpdateDeleteBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateDeleteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;  // Request code for image picker
    private static final String TAG = "UpdateDeleteActivity"; // Tag for debugging

    ActivityUpdateDeleteBinding binding;

    private DatabaseReference blogRef;        // Reference to the blog node in Firebase
    private String blogId;                    // Blog ID passed from previous screen
    private Uri imageUri;                     // New image URI if user selects one
    private String previousImageUrl;          // Existing image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUpdateDeleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve blog details from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        previousImageUrl = getIntent().getStringExtra("image");
        String timestamp = getIntent().getStringExtra("timestamp");
        blogId = getIntent().getStringExtra("blogId");

        // If blogId is missing, stop and exit activity
        if (blogId == null) {
            Log.e(TAG, "blogId is null. Cannot proceed.");
            Toast.makeText(this, "Blog ID is missing. Cannot proceed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate UI with blog data
        binding.titleUpdate.setText(title);
        binding.descriptionUpdate.setText(description);
        binding.timeUpdate.setText(timestamp);
        Glide.with(this).load(previousImageUrl).into(binding.imageUpdate);

        // Set reference to this blog in Firebase
        blogRef = FirebaseDatabase.getInstance().getReference().child("blogs").child(blogId);

        // Set click listeners
        binding.updateBlog.setOnClickListener(v -> updateBlog());
        binding.deleteBlog.setOnClickListener(v -> deleteBlog());
        binding.imageUpdate.setOnClickListener(v -> openImagePicker());
    }

    // Open image picker when user clicks on image
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Only images
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle selected image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(binding.imageUpdate); // Preview selected image
        }
    }

    // Update blog data
    private void updateBlog() {
        String updatedTitle = binding.titleUpdate.getText().toString().trim();
        String updatedDescription = binding.descriptionUpdate.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(updatedTitle) || TextUtils.isEmpty(updatedDescription)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // If user selected a new image, upload it
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + blogId);
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Get URL of new image
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String newImageUrl = uri.toString();
                    updateBlogInDatabase(updatedTitle, updatedDescription, newImageUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to get new image URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
            });
        } else {
            // If no new image selected, use previous image
            updateBlogInDatabase(updatedTitle, updatedDescription, previousImageUrl);
        }
    }

    // Function to update data in Firebase Realtime Database
    private void updateBlogInDatabase(String title, String description, String imageUrl) {
        blogRef.child("title").setValue(title);
        blogRef.child("description").setValue(description);
        blogRef.child("imageUrl").setValue(imageUrl);

        // Update current timestamp
        String updatedTimestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        blogRef.child("timestamp").setValue(updatedTimestamp);

        Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close activity after update
    }

    // Function to delete blog from Firebase
    private void deleteBlog() {
        blogRef.removeValue(); // Remove entire blog node
        Toast.makeText(this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();
        finish(); // Close activity
    }
}
