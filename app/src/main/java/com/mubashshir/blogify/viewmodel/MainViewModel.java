package com.mubashshir.blogify.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for MainActivity.
 * (Android Concept: Shared ViewModel) This ViewModel is scoped to MainActivity and can be used by its
 * hosted fragments (CommunityFragment, WriteFragment, ProfileFragment) to share data or communicate
 * with each other, providing a more robust alternative to interface callbacks.
 * For now, it handles the currently selected navigation tab.
 */
@HiltViewModel
public class MainViewModel extends AndroidViewModel {

    // LiveData to hold the active fragment's navigation ID.
    private final MutableLiveData<Integer> navigationTab = new MutableLiveData<>();

    @Inject
    public MainViewModel(@NonNull Application application) {
        super(application);
        // Set the default tab when the ViewModel is created.
        navigationTab.setValue(0); // Default to CommunityFragment
    }

    public LiveData<Integer> getNavigationTab() {
        return navigationTab;
    }

    public void setNavigationTab(int tabIndex) {
        navigationTab.setValue(tabIndex);
    }
}