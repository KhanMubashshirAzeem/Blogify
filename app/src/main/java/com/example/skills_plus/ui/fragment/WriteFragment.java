package com.example.skills_plus.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.skills_plus.ui.adapter.BlogAdapter;
import com.example.skills_plus.databinding.FragmentWriteBinding;
import com.example.skills_plus.ui.activity.PublishBlogActivity;
import com.example.skills_plus.viewmodel.BlogViewModel;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WriteFragment extends Fragment {

    private FragmentWriteBinding binding;
    private BlogViewModel blogViewModel;
    private BlogAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWriteBinding.inflate(inflater, container, false);
        blogViewModel = new ViewModelProvider(this).get(BlogViewModel.class);

        setupRecyclerView();
        observeUserBlogs();

        binding.floatingBtn.setOnClickListener(v ->
                startActivity(new Intent(getContext(), PublishBlogActivity.class))
        );

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new BlogAdapter(getContext(), new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    private void observeUserBlogs() {
        binding.progressBar.setVisibility(View.VISIBLE);
        blogViewModel.getUserBlogs().observe(getViewLifecycleOwner(), blogs -> {
            adapter.updateBlogs(blogs);
            binding.progressBar.setVisibility(View.GONE);
        });
    }
}