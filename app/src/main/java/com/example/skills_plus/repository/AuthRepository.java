package com.example.skills_plus.repository;

import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skills_plus.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ==============================
 * AuthRepository
 * ==============================
 * PURPOSE:
 * This repository handles all authentication-related logic and data retrieval/updating
 * for the authenticated user. It acts as a single source of truth for user-related
 * operations in the MVVM architecture.
 *
 * ==============================
 * CONCEPTS USED:
 * ------------------------------
 * OOP:
 *  - Encapsulation: All Firebase authentication and database logic is hidden
 *    inside this repository, exposing only necessary public methods.
 *  - Abstraction: The ViewModel interacts only with these methods, without knowing
 *    how Firebase is implemented internally.
 *
 * Android:
 *  - LiveData: Lifecycle-aware observable that updates the UI when the data changes.
 *  - Repository Pattern: Separates data logic from the ViewModel and UI.
 *
 * Java:
 *  - Threading: Uses background threads for data loading to keep the UI responsive.
 *
 * MVVM:
 *  - Repository is the "Model" layer that communicates with Firebase.
 *  - ViewModel calls repository methods to fetch/update data.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;

    private final MutableLiveData<FirebaseUser> userLiveData;     // Holds logged-in Firebase user
    private final MutableLiveData<User> userDetailsLiveData;      // Holds custom User object details
    private final MutableLiveData<Boolean> isLogoutComplete;      // Tracks logout status

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storageReference = FirebaseStorage.getInstance().getReference();

        userLiveData = new MutableLiveData<>();
        userDetailsLiveData = new MutableLiveData<>();
        isLogoutComplete = new MutableLiveData<>();

        // Automatically set current user if already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            userLiveData.postValue(firebaseAuth.getCurrentUser());
            loadUserDetails();
        }
    }

    // LiveData getters
    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<User> getUserDetailsLiveData() { return userDetailsLiveData; }
    public LiveData<Boolean> getIsLogoutComplete() { return isLogoutComplete; }

    /**
     * Register a new user with Firebase Authentication and store details in Realtime Database.
     */
    public void register(String email, String password, String username) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("useremail", email);

                            databaseReference.child(uid).child("userDetail").setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            userLiveData.postValue(firebaseUser);
                                        }
                                    });
                        }
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Login an existing user and load their details from Firebase Database.
     */
    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.postValue(firebaseAuth.getCurrentUser());
                        loadUserDetails();
                    } else {
                        userLiveData.postValue(null);
                    }
                });
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        firebaseAuth.signOut();
        userLiveData.postValue(null);
        userDetailsLiveData.postValue(null);
        isLogoutComplete.postValue(true);
    }

    /**
     * Load user details in a background thread for better performance.
     */
    public void loadUserDetails() {
        new Thread(() -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                DatabaseReference userDetailRef = databaseReference.child(uid).child("userDetail");

                userDetailRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                user.setUid(uid);
                                userDetailsLiveData.postValue(user);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }
        }).start();
    }

    /**
     * Upload profile image and update the user's database record.
     */
    public void uploadProfileImage(Uri imageUri) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null || imageUri == null) return;

        String uid = firebaseUser.getUid();
        StorageReference imageRef = storageReference.child("profile/" + UUID.randomUUID() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            databaseReference.child(uid).child("userDetail").child("profilePhoto")
                                    .setValue(downloadUri.toString());
                        }));
    }
}
