package com.example.skills_plus.ui.activity;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.example.skills_plus.R;
import com.example.skills_plus.databinding.ActivityMainBinding;
import com.example.skills_plus.ui.fragment.CommunityFragment;
import com.example.skills_plus.ui.fragment.ProfileFragment;
import com.example.skills_plus.ui.fragment.WriteFragment;
import com.example.skills_plus.viewmodel.MainViewModel;

import dagger.hilt.android.AndroidEntryPoint;
import me.ibrahimsn.lib.OnItemSelectedListener;

/**
 * The main container Activity that hosts the primary fragments of the app.
 * (MVVM Concept: View) Its role is to manage the bottom navigation and swap fragments
 * based on state changes observed from the MainViewModel.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupBottomNavigation();
        observeViewModel();
    }

    /**
     * Sets up the listener for the bottom navigation bar.
     */
    private void setupBottomNavigation() {
        binding.bottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {
            // When an item is selected, we notify the ViewModel of the change.
            // We don't perform the fragment transaction directly here.
            mainViewModel.setNavigationTab(i);
            return true;
        });
    }

    /**
     * Observes the navigation state from the MainViewModel.
     * The UI (fragment replacement) reacts to changes in this state.
     */
    private void observeViewModel() {
        mainViewModel.getNavigationTab().observe(this, tabIndex -> {
            Fragment selectedFragment;
            switch (tabIndex) {
                case 1:
                    selectedFragment = new WriteFragment();
                    break;
                case 2:
                    selectedFragment = new ProfileFragment();
                    break;
                case 0:
                default:
                    selectedFragment = new CommunityFragment();
                    break;
            }
            replaceFragment(selectedFragment);
        });
    }

    /**
     * Replaces the current fragment in the FrameLayout.
     * @param fragment The fragment to display.
     */
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }
}