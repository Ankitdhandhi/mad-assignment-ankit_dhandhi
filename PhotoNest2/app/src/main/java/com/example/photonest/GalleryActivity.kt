package com.example.photonest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var rvGallery: RecyclerView
    private lateinit var tvFolderPath: TextView
    private val FOLDER_REQ = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvGallery = findViewById(R.id.rvGallery)
        rvGallery.layoutManager = GridLayoutManager(this, 3)
        tvFolderPath = findViewById(R.id.tvFolderPath)

        val btnChooseFolder = findViewById<MaterialButton>(R.id.btnChooseFolder)
        btnChooseFolder.setOnClickListener { openFolderPicker() }

        // Auto-load PhotoNest folder
        val defaultFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "PhotoNest"
        )
        if (defaultFolder.exists()) loadImages(defaultFolder)
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, FOLDER_REQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FOLDER_REQ && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val path = uri.path ?: return
                val actualPath = path.replace("/tree/primary:", "/storage/emulated/0/")
                val folder = File(actualPath)
                if (folder.exists()) loadImages(folder)
                else Toast.makeText(this, "Cannot read folder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadImages(folder: File) {
        tvFolderPath.text = folder.absolutePath
        val images = folder.listFiles { f -> f.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (images.isEmpty()) {
            Toast.makeText(this, "No images in this folder", Toast.LENGTH_SHORT).show()
            return
        }

        rvGallery.adapter = ImageAdapter(images) { file ->
            val intent = Intent(this, ImageDetailActivity::class.java)
            intent.putExtra("path", file.absolutePath)
            startActivity(intent)
        }
    }
}

class ImageAdapter(
    private val files: List<File>,
    private val onClick: (File) -> Unit
) : RecyclerView.Adapter<ImageAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val iv: ImageView = view.findViewById(R.id.ivThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = files[position]
        Glide.with(holder.iv).load(file).centerCrop().into(holder.iv)
        holder.itemView.setOnClickListener { onClick(file) }
    }

    override fun getItemCount() = files.size
}
