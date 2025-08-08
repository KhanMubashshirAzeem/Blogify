package com.example.skills_plus.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.skills_plus.model.User;
import com.example.skills_plus.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

/**
 * ViewModel for authentication-related UI components (LoginActivity, RegisterActivity, ProfileFragment).
 * (Android Concept: ViewModel) It stores and manages UI-related data in a lifecycle-conscious way.
 * This ViewModel allows data to survive configuration changes such as screen rotations.
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<User> userDetailsLiveData;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        userLiveData = authRepository.getUserLiveData();
        userDetailsLiveData = authRepository.getUserDetailsLiveData();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<User> getUserDetailsLiveData() {
        authRepository.loadUserDetails(); // Trigger loading user details
        return userDetailsLiveData;
    }

    public void register(String email, String password, String username) {
        authRepository.register(email, password, username);
    }

    public void login(String email, String password) {
        authRepository.login(email, password);
    }

    public void logout() {
        authRepository.logout();
    }

    public void uploadProfileImage(Uri imageUri) {
        authRepository.uploadProfileImage(imageUri);
    }
}