package com.example.randomapplication.Video

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.example.randomapplication.R
import com.example.randomapplication.databinding.ActivityPlayVideoBinding
import com.example.randomapplication.databinding.ActivityTrimVideoBinding

class PlayVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayVideoBinding
    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlayVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val videoPath = intent.getStringExtra("videoPath")
        val videoName = intent.getStringExtra("videoName")

        binding.videoName.text = videoName
        binding.video.setVideoURI(Uri.parse(videoPath))

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@PlayVideoActivity, Uri.parse(videoPath))
            prepare()
        }

        binding.video.setVideoURI(Uri.parse(videoPath))
        binding.video.start()

        binding.video.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.setOnVideoSizeChangedListener { mp, _, _ ->
                val videoRatio = mp.videoWidth / mp.videoHeight.toFloat()
                val screenRatio = binding.video.width / binding.video.height.toFloat()
                val scale = videoRatio / screenRatio
                if (scale >= 1f) {
                    binding.video.scaleX = scale
                } else {
                    binding.video.scaleY = 1f / scale
                }
            }
//            binding.video.start() // Start video playback by default
//            binding.playPauseButton.text = "play" // Set initial button text to "Pause"
            updateSeekBar()
        }
        binding.playPauseButton.text = "Play"


        binding.playPauseButton.setOnClickListener {
            if (binding.video.isPlaying) {
                binding.video.pause()
                binding.playPauseButton.text = "Pause"
            } else {
                binding.video.start()
                binding.playPauseButton.text = "Play"
                updateSeekBar()
            }
        }

        binding.time.max = mediaPlayer?.duration ?: 0
        binding.time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.video.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

//        mediaPlayer?.setOnCompletionListener {
//            binding.playPauseButton.text = "Play"
//            binding.time.progress = 0
//            mediaPlayer?.start()
//            updateSeekBar()
//
//        }
        binding.video.setOnCompletionListener {
            binding.playPauseButton.text = "Play"
            binding.time.progress = 0
            binding.video.start()
        // Restart the video
            updateSeekBar()
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed({
            binding.video.currentPosition.let {
                binding.time.progress = it
            }
            if (binding.video.isPlaying) {
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