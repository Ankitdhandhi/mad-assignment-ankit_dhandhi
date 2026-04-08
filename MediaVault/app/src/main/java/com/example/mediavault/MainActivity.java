package com.example.mediavault;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private SeekBar seekBar;
    private TextView tvPosition, tvDuration, tvNowPlaying;
    private TextInputEditText etUrl;
    private TextInputLayout tilUrl;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAudioMode = true;

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                long pos = player.getCurrentPosition();
                long dur = Math.max(player.getDuration(), 0);

                seekBar.setMax((int) dur);
                seekBar.setProgress((int) pos);

                tvPosition.setText(formatMs(pos));
                tvDuration.setText(formatMs(dur));
            }
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player = new ExoPlayer.Builder(this).build();

        playerView = findViewById(R.id.playerView);
        playerView.setPlayer(player);

        seekBar = findViewById(R.id.seekBar);
        tvPosition = findViewById(R.id.tvPosition);
        tvDuration = findViewById(R.id.tvDuration);
        tvNowPlaying = findViewById(R.id.tvNowPlaying);
        etUrl = findViewById(R.id.etUrl);
        tilUrl = findViewById(R.id.tilUrl);

        MaterialButton btnModeAudio = findViewById(R.id.btnModeAudio);
        MaterialButton btnModeVideo = findViewById(R.id.btnModeVideo);
        MaterialButton btnOpenFile = findViewById(R.id.btnOpenFile);
        MaterialButton btnOpenUrl = findViewById(R.id.btnOpenUrl);
        MaterialButton btnPlay = findViewById(R.id.btnPlay);
        MaterialButton btnPause = findViewById(R.id.btnPause);
        MaterialButton btnStop = findViewById(R.id.btnStop);
        MaterialButton btnRestart = findViewById(R.id.btnRestart);

        // Mode switch
        btnModeAudio.setOnClickListener(v -> {
            isAudioMode = true;
            tilUrl.setVisibility(View.GONE);
            btnOpenUrl.setVisibility(View.GONE);
            btnOpenFile.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            openAudioPicker();
        });

        btnModeVideo.setOnClickListener(v -> {
            isAudioMode = false;
            tilUrl.setVisibility(View.VISIBLE);
            btnOpenUrl.setVisibility(View.VISIBLE);
            btnOpenFile.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
        });

        btnOpenFile.setOnClickListener(v -> openAudioPicker());

        btnOpenUrl.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (!url.isEmpty()) {
                loadMedia(Uri.parse(url), url);
            } else {
                Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show();
            }
        });

        // Controls
        btnPlay.setOnClickListener(v -> player.play());
        btnPause.setOnClickListener(v -> player.pause());
        btnStop.setOnClickListener(v -> player.stop());

        btnRestart.setOnClickListener(v -> {
            player.seekTo(0);
            player.play();
        });

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {}

            @Override
            public void onStopTrackingTouch(SeekBar sb) {}
        });

        handler.post(updateRunnable);
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String name = uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "audio file";
                loadMedia(uri, name);
            }
        }
    }

    private void loadMedia(Uri uri, String label) {
        MediaItem item = MediaItem.fromUri(uri);
        player.setMediaItem(item);
        player.prepare();
        player.play();

        tvNowPlaying.setText("Playing: " + label);
    }

    private String formatMs(long ms) {
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if (player != null) {
            player.release();
        }
    }
}