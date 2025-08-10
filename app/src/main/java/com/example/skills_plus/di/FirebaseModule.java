package com.example.skills_plus.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class FirebaseModule {

    @Provides
    @Singleton
    public FirebaseAuth provideFirebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public DatabaseReference provideDatabaseReference(){
        return FirebaseDatabase.getInstance().getReference("users");
    }

    @Provides
    @Singleton
    public StorageReference provideStorageReference() {
        return FirebaseStorage.getInstance().getReference();
    }

}
