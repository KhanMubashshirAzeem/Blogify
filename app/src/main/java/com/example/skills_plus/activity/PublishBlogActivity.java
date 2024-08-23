// PublishBlogActivity.java

package com.example.skills_plus.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityPublishBlogBinding;
import com.example.skills_plus.modal.AllBlogModal;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PublishBlogActivity extends AppCompatActivity {

    private static final String TAG = "PublishBlogActivity";
    private static final int SELECT_PICTURE = 200;
    private Uri selectedImageUri;
    private ActivityPublishBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for immersive UI experience
        EdgeToEdge.enable(this);

        // Inflate the layout using view binding
        binding = ActivityPublishBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle system window insets for proper layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set onClickListener to trigger image selection
        binding.uploadImage.setOnClickListener(view -> imageChooser());

        // Set onClickListener to upload data to Firebase when the publish button is clicked
        binding.publishBtn.setOnClickListener(v -> uploadDataToFirebase());
    }

    // Function to open the image chooser
    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // Function triggered when the user selects an image
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                binding.displayImage.setImageURI(selectedImageUri);
                binding.uploadImage.setVisibility(View.INVISIBLE);
            }
        }
    }

    // Function to upload data to Firebase
    private void uploadDataToFirebase() {
        if (selectedImageUri != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.publishBtn.setVisibility(View.GONE);

            // Upload the image to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            String fileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storageReference.child("images/" + fileName);

            imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    storeDataInDatabase(task.getResult().toString());
                }
            })).addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.publishBtn.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to store data in the Firebase Realtime Database
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
        String blogId = databaseRef.push().getKey();

        if (blogId == null) {
            Toast.makeText(getApplicationContext(), "Failed to generate post ID", Toast.LENGTH_SHORT).show();
            return;
        }

        AllBlogModal blog = new AllBlogModal(uid, blogId, title, description, imageUrl, timestamp);

        databaseRef.child(blogId).setValue(blog).addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Blog added successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Function to get the current date
    String getCurrentDate() {
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
