package com.example.skills_plus.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Firebase authentication instance
    private FirebaseAuth auth;

    // ViewBinding for register activity layout
    private ActivityRegisterBinding binding;

    // Firebase Realtime Database reference
    private DatabaseReference databaseRef;

    // Constants for DB path
    private static final String USERS_PATH = "users";
    private static final String USER_DETAILS_PATH = "userDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables full screen layout drawing
        EdgeToEdge.enable(this);

        // Inflate layout using ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle system window insets (e.g. status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference(USERS_PATH);

        // Handle navigation from register to login screen
        navigateFromActivities();

        // Set listener on Register button
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    // This method collects user input and validates it before registration
    private void createAccount() {
        String username = binding.etUsernameRegister.getText().toString().trim();
        String email = binding.etEmailRegister.getText().toString().trim();
        String password = binding.etPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassRegister.getText().toString().trim();

        // Validate email and password fields
        boolean isValidated = validateData(email, password, confirmPassword);
        if (!isValidated) {
            return;
        }

        // Proceed with Firebase Authentication registration
        createAccountUsingFirebase(email, password, username);
    }

    // Creates Firebase account and handles success/failure
    private void createAccountUsingFirebase(String email, String password, String username) {
        changeInProgress(true); // Show progress bar

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        changeInProgress(false); // Hide progress bar

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Account Created", Toast.LENGTH_SHORT).show();
                            FirebaseUser currentUser = auth.getCurrentUser();
                            if (currentUser != null) {
                                // Store additional user details in Realtime DB
                                storeUsersDetail(currentUser, username, email);
                                // Show dialog to tell user to verify email
                                showAlertDialog();
                            }
                        } else {
                            // Show error message
                            Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Stores additional user data (username & email) in Firebase Realtime Database
    private void storeUsersDetail(FirebaseUser currentUser, String username, String email) {
        String uid = currentUser.getUid();
        DatabaseReference userDetailRef = databaseRef.child(uid).child(USER_DETAILS_PATH);

        // Map for user data
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("useremail", email);

        // Save data in database
        userDetailRef.setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "User details stored successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Failed to store user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validates email and password inputs
    boolean validateData(String email, String password, String confirmPassword) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailRegister.setError("Email is Invalid");
            return false;
        }
        if (password.length() < 6) {
            binding.etPasswordRegister.setError("Password length should be greater than 6");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassRegister.setError("Password not matched");
            return false;
        }
        return true;
    }

    // Show/hide progress bar and register button based on in-progress state
    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setVisibility(View.VISIBLE);
        }
    }

    // Switch to LoginActivity when the user clicks on "Login" text
    private void navigateFromActivities() {
        binding.loginInReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish(); // Finish current activity
            }
        });
    }

    // Show an AlertDialog after account creation asking user to verify email
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("Check your Email for verification");
        builder.setCancelable(false); // Can't cancel the dialog

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

                // Send email verification
                auth.getCurrentUser().sendEmailVerification();

                // Sign out the user
                auth.signOut();

                // Finish activity and return to login
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Handle back press if needed (currently default behavior)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
