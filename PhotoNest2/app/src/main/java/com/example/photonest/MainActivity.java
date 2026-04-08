package com.example.photonest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Uri photoUri;
    private static final int CAMERA_REQ = 1001;
    private static final int PERM_REQ = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        MaterialButton btnTakePhoto = findViewById(R.id.btnTakePhoto);
        MaterialButton btnBrowse = findViewById(R.id.btnBrowseGallery);
        ImageView ivPreview = findViewById(R.id.ivPreview);
        TextView tvRecentLabel = findViewById(R.id.tvRecentLabel);

        // ❌ REMOVED setSupportActionBar(toolbar) → fixes crash

        btnTakePhoto.setOnClickListener(v -> checkPermissionsAndTakePhoto());

        btnBrowse.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GalleryActivity.class))
        );
    }

    private void checkPermissionsAndTakePhoto() {

        ArrayList<String> perms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.CAMERA);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    perms.toArray(new String[0]), PERM_REQ);
        } else {
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == PERM_REQ) {
            boolean allGranted = true;
            for (int res : results) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) takePhoto();
        }
    }

    private void takePhoto() {

        File folder = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "PhotoNest"
        );

        if (!folder.exists()) folder.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());

        File photoFile = new File(folder, "IMG_" + timestamp + ".jpg");

        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                photoFile
        );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        startActivityForResult(intent, CAMERA_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK) {

            ImageView ivPreview = findViewById(R.id.ivPreview);
            TextView tvRecentLabel = findViewById(R.id.tvRecentLabel);

            ivPreview.setVisibility(View.VISIBLE);
            tvRecentLabel.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(photoUri)
                    .into(ivPreview);
        }
    }
}