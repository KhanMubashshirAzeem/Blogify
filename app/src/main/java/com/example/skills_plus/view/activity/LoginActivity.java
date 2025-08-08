package com.example.skills_plus.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.skills_plus.databinding.ActivityLoginBinding;
import com.example.skills_plus.viewmodel.AuthViewModel;

/**
 * Activity for user login.
 * (MVVM Concept: View) This class's responsibility is now limited to displaying the login form,
 * capturing user input, and delegating the login action to the AuthViewModel.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeViewModel();
    }

    /**
     * Sets up click listeners.
     */
    private void setupListeners() {
        binding.btnLogin.setOnClickListener(view -> loginUser());
        binding.registerInLogin.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * Observes LiveData from the AuthViewModel.
     */
    private void observeViewModel() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            changeInProgress(false);
            if (firebaseUser != null) {
                // Login successful, navigate to MainActivity.
                Toast.makeText(getApplicationContext(), "Logged In Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                // You could have a separate LiveData for error messages to be more specific.
                // For now, we assume any null after a login attempt is a failure.
                // Note: This observer will also fire on initialization if the user is already logged out.
                // We handle this by only showing the toast after the button is clicked.
            }
        });
    }

    /**
     * Gathers credentials and delegates the login action to the ViewModel.
     */
    private void loginUser() {
        String email = binding.etEmailLogin.getText().toString().trim();
        String password = binding.etPasswordLogin.getText().toString().trim();

        if (validateData(email, password)) {
            changeInProgress(true);
            authViewModel.login(email, password);
        }
    }

    /**
     * Validates user input.
     * @return true if data is valid, false otherwise.
     */
    boolean validateData(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailLogin.setError("Email is invalid");
            return false;
        }
        if (password.length() < 6) {
            binding.etPasswordLogin.setError("Password length must be at least 6 characters");
            return false;
        }
        return true;
    }

    /**
     * Manages the visibility of the progress bar.
     */
    void changeInProgress(boolean inProgress) {
        binding.progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        binding.btnLogin.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}