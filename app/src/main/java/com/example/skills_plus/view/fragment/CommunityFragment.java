package com.example.skills_plus.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.skills_plus.adapter.CommunityBlogAdapter;
import com.example.skills_plus.databinding.FragmentCommunityBinding;
import com.example.skills_plus.model.Blog;
import com.example.skills_plus.viewmodel.BlogViewModel;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

public class CommunityFragment extends Fragment implements CommunityBlogAdapter.OnBookmarkClickListener {

    private FragmentCommunityBinding binding;
    private BlogViewModel blogViewModel;
    private CommunityBlogAdapter adapter;
    private List<Blog> allBlogsList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        blogViewModel = new ViewModelProvider(this).get(BlogViewModel.class);

        setupRecyclerView();
        setupSearchView();
        observeViewModel();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new CommunityBlogAdapter(getContext(), new ArrayList<>(), this);
        binding.rvAllBlogs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAllBlogs.setAdapter(adapter);
    }

    private void observeViewModel() {
        binding.progressBar.setVisibility(View.VISIBLE);

        blogViewModel.getAllBlogs().observe(getViewLifecycleOwner(), blogs -> {
            if (blogs != null) {
                allBlogsList.clear();
                allBlogsList.addAll(blogs);
                adapter.updateBlogs(allBlogsList);
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        blogViewModel.getBookmarkStatusLiveData().observe(getViewLifecycleOwner(), statusMap -> {
            if (statusMap != null) {
                adapter.updateBookmarkStatus(statusMap);
            }
        });
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBlogs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBlogs(newText);
                return false;
            }
        });
    }

    private void filterBlogs(String query) {
        List<Blog> filteredList = new ArrayList<>();
        for (Blog blog : allBlogsList) {
            if (blog.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(blog);
            }
        }
        adapter.updateBlogs(filteredList);
    }

    @Override
    public void onBookmarkClick(String blogId) {
        blogViewModel.toggleBookmark(blogId);
    }
}
