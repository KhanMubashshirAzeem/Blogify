package com.example.skills_plus.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.skills_plus.R;
import com.example.skills_plus.adapter.CommunityBlogAdapter;
import com.example.skills_plus.databinding.FragmentProfileBinding;
import com.example.skills_plus.view.activity.LoginActivity;
import com.example.skills_plus.viewmodel.AuthViewModel;
import com.example.skills_plus.viewmodel.BlogViewModel;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private static final int SELECT_PICTURE = 200;
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private BlogViewModel blogViewModel;
    private CommunityBlogAdapter savedBlogsAdapter;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        blogViewModel = new ViewModelProvider(requireActivity()).get(BlogViewModel.class);

        setupListeners();
        setupSavedBlogsRecyclerView();
        observeViewModel();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.logoutBtn.setOnClickListener(v -> showLogoutConfirmationDialog());
        binding.imageProfile.setOnClickListener(v -> chooseImage());
    }

    private void setupSavedBlogsRecyclerView() {
        // We can reuse the CommunityBlogAdapter. Pass null for the click listener
        // as we don't need bookmarking functionality on this screen's list.
        savedBlogsAdapter = new CommunityBlogAdapter(getContext(), new ArrayList<>(), null);
        binding.saveBlogsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.saveBlogsRecyclerView.setAdapter(savedBlogsAdapter);
    }

    private void observeViewModel() {
        authViewModel.getUserDetailsLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.userNameProfile.setText(user.getUsername());
                binding.userEmailProfile.setText(user.getUseremail());
                if (user.getProfilePhoto() != null && getContext() != null) {
                    Glide.with(getContext()).load(user.getProfilePhoto()).into(binding.imageProfile);
                } else {
                    binding.imageProfile.setImageResource(R.drawable.person_icon_bold);
                }
            }
        });

        blogViewModel.getBookmarkedBlogs().observe(getViewLifecycleOwner(), blogs -> {
            binding.progressBarSave.setVisibility(View.GONE);
            if (blogs != null && !blogs.isEmpty()) {
                binding.saveBlogsRecyclerView.setVisibility(View.VISIBLE);
                savedBlogsAdapter.updateBlogs(blogs);
            } else {
                binding.saveBlogsRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_PICTURE && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.imageProfile.setImageURI(selectedImageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (selectedImageUri != null) {
            authViewModel.uploadProfileImage(selectedImageUri);
            Toast.makeText(getContext(), "Profile image updating...", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    authViewModel.logout();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
