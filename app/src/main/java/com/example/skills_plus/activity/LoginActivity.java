package com.example.skills_plus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // ViewBinding instance to access views from activity_login.xml
    ActivityLoginBinding binding;

    // Firebase authentication instance
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables drawing edge-to-edge (status bar and nav bar transparent)
        EdgeToEdge.enable(this);

        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Set onClickListener on login button
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(); // Call method to handle login logic
            }
        });
    }

    // Get input from user and validate it before logging in
    private void loginUser() {
        String email = binding.etEmailLogin.getText().toString();
        String password = binding.etPasswordLogin.getText().toString();

        // Validate email and password inputs
        boolean isValidated = validateData(email, password);
        if (!isValidated) {
            return; // Stop if validation fails
        }

        // Proceed to login using Firebase Auth
        LoginAccountUsingFirebase(email, password);
    }

    // Use FirebaseAuth to log in the user
    private void LoginAccountUsingFirebase(String email, String password) {
        // Show progress bar, hide button
        changeInProgress(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress bar, show button
                        changeInProgress(false);

                        if (task.isSuccessful()) {
                            // If login is successful, navigate to MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            Toast.makeText(getApplicationContext(), "Logged In Successful", Toast.LENGTH_SHORT).show();
                            finish(); // Finish LoginActivity
                        } else {
                            // If login fails, show error message
                            Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Shows progress bar and hides login button while login is in progress
    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setVisibility(View.VISIBLE);
        }
    }

    // Validate email format and password length
    boolean validateData(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailLogin.setError("Email is Invalid");
            return false;
        }
        if (password.length() < 6) {
            binding.etPasswordLogin.setError("Password length should be greater than 6");
            return false;
        }
        return true;
    }

    // Optional: Handle back press if you want custom behavior (default used here)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // When "Register" text is clicked, go to RegisterActivity
    public void loginToRegisterAct(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        finish(); // Close LoginActivity
    }
}
