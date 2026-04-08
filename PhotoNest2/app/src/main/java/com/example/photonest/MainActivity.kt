package com.example.photonest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var photoUri: Uri? = null
    private val CAMERA_REQ = 1001
    private val PERM_REQ = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val btnTakePhoto = findViewById<MaterialButton>(R.id.btnTakePhoto)
        val btnBrowse = findViewById<MaterialButton>(R.id.btnBrowseGallery)
        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val tvRecentLabel = findViewById<TextView>(R.id.tvRecentLabel)

        setSupportActionBar(toolbar)

        btnTakePhoto.setOnClickListener { checkPermissionsAndTakePhoto() }
        btnBrowse.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
    }

    private fun checkPermissionsAndTakePhoto() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (perms.isNotEmpty()) ActivityCompat.requestPermissions(this, perms.toTypedArray(), PERM_REQ)
        else takePhoto()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == PERM_REQ && results.all { it == PackageManager.PERMISSION_GRANTED }) takePhoto()
    }

    private fun takePhoto() {
        val folder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "PhotoNest"
        )
        if (!folder.exists()) folder.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(folder, "IMG_$timestamp.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, CAMERA_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK) {
            val ivPreview = findViewById<ImageView>(R.id.ivPreview)
            val tvRecentLabel = findViewById<TextView>(R.id.tvRecentLabel)
            ivPreview.visibility = android.view.View.VISIBLE
            tvRecentLabel.visibility = android.view.View.VISIBLE
            Glide.with(this).load(photoUri).into(ivPreview)
        }
    }
}