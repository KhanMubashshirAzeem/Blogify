package com.example.skills_plus.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityMainBinding;
import com.example.skills_plus.fragment.ProfileFragment;
import com.example.skills_plus.fragment.CommunityFragment;
import com.example.skills_plus.fragment.WriteFragment;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class MainActivity extends AppCompatActivity {

    // View binding for activity_main.xml
    ActivityMainBinding binding;

    // Reference to SmoothBottomBar from the layout
    SmoothBottomBar bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables drawing behind system bars (status bar, navigation bar)
        EdgeToEdge.enable(this);

        // Inflate the layout using ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply padding to account for system UI (status bar etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set default fragment when activity launches
        replace(new CommunityFragment());

        // Initialize the bottom navigation view
        bottomNavigationView = binding.bottomBar;

        // Setup click listeners for switching fragments
        buttomNavSwitcher();
    }

    // Handle navigation item selection using SmoothBottomBar
    private void buttomNavSwitcher() {
        bottomNavigationView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                switch (i) {
                    case 0:
                        // Navigate to Community tab
                        replace(new CommunityFragment());
                        break;
                    case 1:
                        // Navigate to Write tab
                        replace(new WriteFragment());
                        break;
                    case 2:
                        // Navigate to Profile tab
                        replace(new ProfileFragment());
                        break;
                }
                return true; // Return true to indicate the selection is handled
            }
        });
    }

    // Replaces the current fragment with the selected one in the FrameLayout
    public void replace(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment); // Replace current fragment
        transaction.commit(); // Commit the transaction
    }
}
