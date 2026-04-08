package com.example.photonest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private TextView tvFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 3));

        tvFolderPath = findViewById(R.id.tvFolderPath);

        MaterialButton btnChooseFolder = findViewById(R.id.btnChooseFolder);
        btnChooseFolder.setOnClickListener(v -> openFolderPicker());
    }

    // ✅ Modern Folder Picker
    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                    Uri uri = result.getData().getData();

                    // Persist permission
                    getContentResolver().takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );

                    tvFolderPath.setText(uri.getPath());

                    loadImagesFromUri(uri);
                }
            });

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    // ✅ Correct way using DocumentFile
    private void loadImagesFromUri(Uri uri) {

        DocumentFile folder = DocumentFile.fromTreeUri(this, uri);

        if (folder == null || !folder.isDirectory()) {
            Toast.makeText(this, "Invalid folder", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DocumentFile> images = new ArrayList<>();

        for (DocumentFile file : folder.listFiles()) {

            String name = file.getName();
            if (name == null) continue;

            name = name.toLowerCase();

            if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".webp")) {
                images.add(file);
            }
        }

        if (images.isEmpty()) {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
            return;
        }

        rvGallery.setAdapter(new ImageAdapterUri(images, file -> {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra("uri", file.getUri().toString());
            startActivity(intent);
        }));
    }
}