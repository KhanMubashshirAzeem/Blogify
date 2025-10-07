package com.mubashshir.blogify.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.mubashshir.blogify.databinding.ActivityRegisterBinding;
import com.mubashshir.blogify.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for user registration.
 * (Android Concept: ViewBinding) Replaces findViewById to provide compile-time safe access to views.
 * (MVVM Concept: View) This class is now a "View" which is responsible for rendering UI and
 * delegating user actions to the ViewModel.
 */
@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private void setupListeners() {
        binding.btnRegister.setOnClickListener(view -> createAccount());
        binding.loginInReg.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Observes LiveData from the AuthViewModel to react to state changes.
     */
    private void observeViewModel() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            changeInProgress(false);
            if (firebaseUser != null) {
                // Registration was successful.
                Toast.makeText(getApplicationContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                showEmailVerificationDialog();
            }
            // We can add another LiveData for error messages to show specific registration errors.
        });
    }

    /**
     * Gathers user input and triggers the registration process via the ViewModel.
     */
    private void createAccount() {
        String username = binding.etUsernameRegister.getText().toString().trim();
        String email = binding.etEmailRegister.getText().toString().trim();
        String password = binding.etPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassRegister.getText().toString().trim();

        if (validateData(email, password, confirmPassword)) {
            changeInProgress(true);
            // Delegate the registration logic to the ViewModel.
            authViewModel.register(email, password, username);
        }
    }

    /**
     * Shows a dialog to inform the user to verify their email.
     */
    private void showEmailVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Attention!")
                .setMessage("Check your Email for verification.")
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Sign out the user so they have to log in after verification.
                    authViewModel.logout();
                    // Navigate to Login screen.
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .create()
                .show();
    }

    /**
     * Validates user input fields. This is a UI-level concern.
     * @return true if data is valid, false otherwise.
     */
    private boolean validateData(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailRegister.setError("Email is invalid");
            return false;
        }
        if (password.length() < 6) {
            binding.etPasswordRegister.setError("Password length must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassRegister.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    /**
     * Manages the visibility of the progress bar.
     */
    private void changeInProgress(boolean inProgress) {
        binding.progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.btnRegister.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}