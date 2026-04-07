package com.example.mediavault

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var tvPosition: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvNowPlaying: TextView
    private lateinit var etUrl: TextInputEditText
    private lateinit var tilUrl: TextInputLayout

    private val handler = Handler(Looper.getMainLooper())
    private var isAudioMode = true

    private val updateRunnable = object : Runnable {
        override fun run() {
            val pos = player.currentPosition
            val dur = player.duration.coerceAtLeast(0)

            seekBar.max = dur.toInt()
            seekBar.progress = pos.toInt()

            tvPosition.text = formatMs(pos)
            tvDuration.text = formatMs(dur)

            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = ExoPlayer.Builder(this).build()

        playerView = findViewById(R.id.playerView)
        playerView.player = player

        seekBar = findViewById(R.id.seekBar)
        tvPosition = findViewById(R.id.tvPosition)
        tvDuration = findViewById(R.id.tvDuration)
        tvNowPlaying = findViewById(R.id.tvNowPlaying)
        etUrl = findViewById(R.id.etUrl)
        tilUrl = findViewById(R.id.tilUrl)

        val btnModeAudio = findViewById<MaterialButton>(R.id.btnModeAudio)
        val btnModeVideo = findViewById<MaterialButton>(R.id.btnModeVideo)
        val btnOpenFile = findViewById<MaterialButton>(R.id.btnOpenFile)
        val btnOpenUrl = findViewById<MaterialButton>(R.id.btnOpenUrl)
        val btnPlay = findViewById<MaterialButton>(R.id.btnPlay)
        val btnPause = findViewById<MaterialButton>(R.id.btnPause)
        val btnStop = findViewById<MaterialButton>(R.id.btnStop)
        val btnRestart = findViewById<MaterialButton>(R.id.btnRestart)

        // Mode switch
        btnModeAudio.setOnClickListener {
            isAudioMode = true
            tilUrl.visibility = android.view.View.GONE
            btnOpenUrl.visibility = android.view.View.GONE
            btnOpenFile.visibility = android.view.View.VISIBLE
            playerView.visibility = android.view.View.GONE
            openAudioPicker()
        }

        btnModeVideo.setOnClickListener {
            isAudioMode = false
            tilUrl.visibility = android.view.View.VISIBLE
            btnOpenUrl.visibility = android.view.View.VISIBLE
            btnOpenFile.visibility = android.view.View.GONE
            playerView.visibility = android.view.View.VISIBLE
        }

        btnOpenFile.setOnClickListener { openAudioPicker() }

        btnOpenUrl.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                loadMedia(Uri.parse(url), url)
            } else {
                Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show()
            }
        }

        // Controls
        btnPlay.setOnClickListener { player.play() }
        btnPause.setOnClickListener { player.pause() }
        btnStop.setOnClickListener { player.stop() }

        btnRestart.setOnClickListener {
            player.seekTo(0)
            player.play()
        }

        // SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(p.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        handler.post(updateRunnable)
    }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val name = uri.lastPathSegment ?: "audio file"
                loadMedia(uri, name)
            }
        }
    }

    private fun loadMedia(uri: Uri, label: String) {
        val item = MediaItem.fromUri(uri)
        player.setMediaItem(item)
        player.prepare()
        player.play()

        tvNowPlaying.text = "Playing: $label"
    }

    private fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        player.release()
    }
}