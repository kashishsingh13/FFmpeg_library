package com.example.randomapplication.Video

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.randomapplication.R
import com.example.randomapplication.databinding.ActivityTrimVideoBinding
import com.google.android.material.slider.RangeSlider
import java.io.File

class TrimVideoActivity : AppCompatActivity() {
    private lateinit var binding:ActivityTrimVideoBinding
    private var selectedVideoPath: String? = null
    private lateinit var selectedVideoUri: Uri
    private var startTime: Int = 0
    private var endTime: Int = 300
    private var mediaPlayer: MediaPlayer? = null
    private var isVideoTrimmed = false
    private var endTimeHandler: Handler? = null
    private var endTimeRunnable: Runnable? = null

    private fun generateUniqueFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "trimmed_video_$timestamp.mp4"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTrimVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectVideoButton.setOnClickListener { openFilePicker() }
        binding.trimVideoButton.setOnClickListener { trimVideo() }
        binding.timeSeekbar.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {}

            override fun onStopTrackingTouch(slider: RangeSlider) {
                startTime = slider.values[0].toInt()
                endTime = slider.values[1].toInt()
                startVideoPlayback(startTime * 1000)
            }
        })
        checkPermissions()
    }

    private val selectVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedVideoUri = uri
                selectedVideoPath = getRealPathFromURI(uri)
                binding.videoPathText.text = selectedVideoPath
                binding.video.visibility=View.VISIBLE
                binding.video.setVideoURI(selectedVideoUri)
                binding.video.start()
//                initializeMediaPlayer()
            }
        }
    }

    private fun initializeMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(selectedVideoPath)
            prepareAsync()
            setOnPreparedListener {
                startVideoPlayback(startTime * 1000)
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        var filePath: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                val fileName = it.getString(index)
                val file = File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                filePath = file.absolutePath
            }
        }
        return filePath
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }
        selectVideoLauncher.launch(intent)
    }

    private fun launchPlayVideoActivity(videoFile: String) {
        val timestamp = System.currentTimeMillis()
        val intent = Intent(this, PlayVideoActivity::class.java).apply {
            putExtra("videoPath", videoFile)
            putExtra("videoName", "trimmed_video_$timestamp.mp4")
        }
        startActivity(intent)
    }

    private fun trimVideo() {
        stopVideoPlayback()
        val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/${generateUniqueFileName()}"

        if (isVideoTrimmed && File(outputFilePath).exists()) {
            launchPlayVideoActivity(outputFilePath)
            isVideoTrimmed = true
        } else {
            if (selectedVideoPath != null && startTime < endTime) {
                val cmd = arrayOf(
                    "-i", selectedVideoPath!!,
                    "-ss", startTime.toString(),
                    "-to", endTime.toString(),
                    "-c", "copy",
                    outputFilePath
                )

                FFmpeg.executeAsync(cmd) { _, returnCode ->
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        runOnUiThread {
                            launchPlayVideoActivity(outputFilePath)
                            isVideoTrimmed = true
                        }
                    } else {
                        runOnUiThread {
                            binding.videoPathText.text = "Failed to trim video"
                        }
                    }
                }
            } else {
                binding.videoPathText.text = "Please select a valid video file and ensure start time is less than end time"
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 2)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
            } else {
                Toast.makeText(this, "Permissions are required to select and save video files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                } else {
                    Toast.makeText(this, "Manage External Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startVideoPlayback(startTimeMs: Int) {
//        mediaPlayer?.seekTo(startTimeMs)
//        mediaPlayer?.start()
//
//        endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
//        endTimeHandler = Handler(Looper.getMainLooper())
//        endTimeRunnable = Runnable {
//            mediaPlayer?.pause()
//            mediaPlayer?.seekTo(startTime * 1000)
//            mediaPlayer?.start()
//        }
//
//        val endTimeMs = endTime * 1000
//        val duration = endTimeMs - startTimeMs
//        endTimeHandler?.postDelayed(endTimeRunnable!!, duration.toLong())
//
//        mediaPlayer?.setOnCompletionListener {
//            mediaPlayer?.seekTo(startTime * 1000)
//            mediaPlayer?.start()
//        }
//    }
//
//    private fun stopVideoPlayback() {
//        endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
//        mediaPlayer?.pause()
//    }
    binding.video.seekTo(startTimeMs)
        binding.video.start()

    endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
    endTimeHandler = Handler(Looper.getMainLooper())
    endTimeRunnable = Runnable {
        binding. video.pause()
        binding.video.seekTo(startTime * 1000)
        binding.video.start()
    }

    val endTimeMs = endTime * 1000
    val duration = endTimeMs - startTimeMs
    endTimeHandler?.postDelayed(endTimeRunnable!!, duration.toLong())

        binding.video.setOnCompletionListener {
            binding.video.seekTo(startTime * 1000)
            binding.video.start()
    }
}

private fun stopVideoPlayback() {
    endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
    binding.video.pause()
}
}