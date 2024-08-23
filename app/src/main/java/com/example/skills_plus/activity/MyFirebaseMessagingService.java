package com.example.skills_plus.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.skills_plus.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Extract data from the incoming message
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        String imageUrl = remoteMessage.getData().get("image");

        // Use Glide to load the image from the URL into a Bitmap
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Once the image is loaded, build and display the notification
                        showNotification(title, message, resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle the case when the image load is cleared
                    }
                });
    }

    private void showNotification(String title, String message, Bitmap image) {
        // Create a NotificationCompat.Builder to build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                .setSmallIcon(R.drawable.add_icon) // Set the small icon for the notification
                .setContentTitle(title) // Set the title of the notification
                .setContentText(message) // Set the message text
                .setLargeIcon(image) // Set the large icon (image) for the notification
                .setStyle(new NotificationCompat.BigPictureStyle() // Use BigPictureStyle to show the image
                        .bigPicture(image)
                        .bigLargeIcon(image))
                .setPriority(NotificationCompat.PRIORITY_HIGH); // Set high priority for the notification

        // Ensure the app has permission to show notifications
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so do not proceed with showing the notification
            return;
        }

        // Use NotificationManagerCompat to display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build()); // Show the notification with a unique ID
    }
}
