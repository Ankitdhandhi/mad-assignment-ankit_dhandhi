package com.example.photonest

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val path = intent.getStringExtra("path") ?: return finish()
        val file = File(path)

        val ivFull = findViewById<ImageView>(R.id.ivFull)
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvPath = findViewById<TextView>(R.id.tvPath)
        val tvSize = findViewById<TextView>(R.id.tvSize)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)

        Glide.with(this).load(file).into(ivFull)
        tvName.text = file.name
        tvPath.text = "📁 ${file.absolutePath}"
        tvSize.text = "📦 Size: ${formatSize(file.length())}"
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(file.lastModified()))
        tvDate.text = "📅 Date: $dateStr"

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Image?")
                .setMessage("\"${file.name}\" will be permanently deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    if (file.delete()) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Could not delete file", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }
}