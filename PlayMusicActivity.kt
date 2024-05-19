package com.example.randomapplication

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.Toast
import com.example.randomapplication.databinding.ActivityPlayMusicBinding

class PlayMusicActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPlayMusicBinding
    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlayMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val audioPath = intent.getStringExtra("audioPath")
        val audioName = intent.getStringExtra("audioName")

        binding.musicName.text = audioName

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@PlayMusicActivity, Uri.parse(audioPath))
            prepare()
        }

//
        binding.playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                binding.playButton.text = "Play"
            } else {
                mediaPlayer?.start()
                binding.playButton.text = "Pause"
                updateSeekBar()
            }
        }

        binding.time.max = mediaPlayer?.duration ?: 0
        binding.time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mediaPlayer?.setOnCompletionListener {
            binding.playButton.text = "Play"
            binding.time.progress = 0
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed({
            mediaPlayer?.currentPosition?.let {
                binding.time.progress = it
            }
            if (mediaPlayer?.isPlaying == true) {
                updateSeekBar()
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}