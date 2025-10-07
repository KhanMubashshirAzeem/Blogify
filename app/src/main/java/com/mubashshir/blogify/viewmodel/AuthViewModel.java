package com.mubashshir.blogify.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.mubashshir.blogify.model.User;
import com.mubashshir.blogify.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for authentication-related UI components (LoginActivity, RegisterActivity, ProfileFragment).
 * (Android Concept: ViewModel) It stores and manages UI-related data in a lifecycle-conscious way.
 * This ViewModel allows data to survive configuration changes such as screen rotations.
 */
@HiltViewModel
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<User> userDetailsLiveData;

    @Inject
    public AuthViewModel(@NonNull Application application, AuthRepository authRepository) {
        super(application);
        this.authRepository = authRepository;
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