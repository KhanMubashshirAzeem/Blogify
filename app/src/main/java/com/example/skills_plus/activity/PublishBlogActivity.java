package com.example.skills_plus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityPublishBlogBinding;
import com.example.skills_plus.modal.CommunityBlogModal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PublishBlogActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 200; // Request code for image picker
    private Uri selectedImageUri; // To store selected image URI

    ActivityPublishBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout (status bar transparent)
        EdgeToEdge.enable(this);

        // View binding setup
        binding = ActivityPublishBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply padding for system bars (status/nav bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set listener to select image
        binding.uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser(); // Open image picker
            }
        });

        // Set listener to upload blog post
        binding.publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDataToFirebase(); // Upload image + blog data
            }
        });
    }

    // Function to open image picker
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*"); // Only image files
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // Called after image is selected
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if image was selected successfully
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            selectedImageUri = data.getData(); // Get image URI
            if (selectedImageUri != null) {
                binding.displayImage.setImageURI(selectedImageUri); // Show preview
                binding.uploadImage.setVisibility(View.INVISIBLE);  // Hide upload button
            }
        }
    }

    // Upload image to Firebase Storage and then store blog info in Realtime Database
    private void uploadDataToFirebase() {
        if (selectedImageUri != null) {
            // Show progress bar while uploading
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.publishBtn.setVisibility(View.GONE);

            // Initialize Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            // Create unique filename
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child("images/" + fileName);

            // Upload image file
            imageRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // On success, get download URL of uploaded image
                    imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                storeDataInDatabase(downloadUri.toString()); // Save blog to database
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // If upload fails, show error
                    Toast.makeText(getApplicationContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No image selected
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    // Store blog details in Firebase Realtime Database
    private void storeDataInDatabase(String imageUrl) {
        String title = binding.addTitle.getText().toString();
        String description = binding.addDescription.getText().toString();
        String timestamp = getCurrentDate();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("blogs");

        // Generate unique blog ID
        String blogId = databaseRef.child("blogs").push().getKey();
        Log.d("PublishSkillActivity", "Generated blogId: " + blogId);

        if (blogId == null) {
            Toast.makeText(getApplicationContext(), "Failed to generate post ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create blog object
        CommunityBlogModal blog = new CommunityBlogModal(uid, blogId, title, description, imageUrl, timestamp);

        // Upload blog data to database
        databaseRef.child(blogId).setValue(blog).addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Blog added successfully", Toast.LENGTH_SHORT).show();
            finish(); // Close activity after upload
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Get current date in yyyy-MM-dd format
    String getCurrentDate() {
        long currentTimeMillis = System.currentTimeMillis(); // Get timestamp
        Date date = new Date(currentTimeMillis);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date); // Format and return date
    }
}
