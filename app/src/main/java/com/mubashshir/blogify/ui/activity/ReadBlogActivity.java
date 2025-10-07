package com.mubashshir.blogify.ui.activity;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.mubashshir.blogify.databinding.ActivityReadBlogBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity to display the full content of a single blog post.
 * (MVVM Concept: View) This is a simple view that displays data passed via an Intent.
 * It does not require a ViewModel because it doesn't fetch or manage its own data;
 * it only displays what it's given and performs a self-contained UI operation (PDF creation).
 */
@AndroidEntryPoint
public class ReadBlogActivity extends AppCompatActivity {

    private ActivityReadBlogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadBlogData();
        setupListeners();
    }

    /**
     * Retrieves blog data passed via Intent and populates the views.
     */
    private void loadBlogData() {
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String timestamp = getIntent().getStringExtra("timestamp");

        binding.titleBD.setText(title);
        binding.descriptionBD.setText(description);
        binding.timeStampBD.setText(timestamp);

        Glide.with(this).load(imageUrl).into(binding.imageBD);
    }

    /**
     * Sets up click listeners for the toolbar and download button.
     */
    private void setupListeners() {
        binding.blogDetailToolbar.setOnClickListener(view -> onBackPressed());
        binding.downloadButton.setOnClickListener(v -> createPdf(binding.blogContentLayout));
    }

    /**
     * Creates a PDF from the provided ScrollView content and saves it to the device.
     * This is a UI-specific operation and can remain within the Activity.
     *
     * @param scrollView The ScrollView containing the content to be converted.
     */
    public void createPdf(ScrollView scrollView) {
        // Measure the total height of the scrollable content
        int totalHeight = scrollView.getChildAt(0).getHeight();
        int width = scrollView.getWidth();

        // Create a Bitmap of the entire scrollable content
        Bitmap bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, totalHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        savePdf(document);
    }

    /**
     * Saves the generated PDF document to the device's Downloads directory.
     * @param document The PdfDocument to save.
     */
    private void savePdf(PdfDocument document) {
        try {
            OutputStream outputStream;
            String fileName = "Blog_" + System.currentTimeMillis() + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ (API 29 and above), use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Failed to create new MediaStore record.");
                outputStream = getContentResolver().openOutputStream(uri);
            } else {
                // For older versions, use direct file path
                String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File file = new File(directoryPath, fileName);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream != null) {
                document.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(this, "PDF downloaded successfully", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}