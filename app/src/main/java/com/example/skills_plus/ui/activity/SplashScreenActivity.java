package com.example.skills_plus.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.skills_plus.R;
import com.example.skills_plus.ui.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The initial splash screen of the application.
 * (Android Concept: Activity Lifecycle) This activity is the entry point of the app.
 * It now uses a ViewModel to check the user's authentication state.
 */
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // (Android Concept: ViewModelProvider) Obtains the ViewModel instance scoped to this Activity.
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // A delay to show the splash screen, then check login status.
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, 1500);
    }

    /**
     * Observes the user's authentication state from the AuthViewModel.
     */
    private void checkUserSession() {
        // (Android Concept: Observer Pattern with LiveData) We observe the userLiveData.
        // The onChanged callback is triggered automatically when the data changes.
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            Intent intent;
            if (firebaseUser != null) {
                // If user is logged in, navigate to MainActivity.
                intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            } else {
                // If user is not logged in, navigate to the registration/login flow.
                intent = new Intent(SplashScreenActivity.this, RegisterActivity.class);
            }
            startActivity(intent);
            finish(); // Finish SplashScreenActivity so it's not on the back stack.
        });
    }
}