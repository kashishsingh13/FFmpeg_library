package com.example.randomapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.randomapplication.databinding.ActivityAudioTrimBinding
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.slider.RangeSlider
import java.io.File

class AudioTrimActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAudioTrimBinding
    private var selectedAudioPath: String? = null
    private var startTime: Int = 0
    private var endTime: Int = 300
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioTrimmed = false
    private var endTimeHandler: Handler? = null
    private var endTimeRunnable: Runnable? = null
    private fun generateUniqueFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "trimmed_audio_$timestamp.mp3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAudioTrimBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.selectAudioButton.setOnClickListener { openFilePicker() }
        binding.trimAudioButton.setOnClickListener { trimAudio() }
        binding.timeSeekbar.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {}

            override fun onStopTrackingTouch(slider: RangeSlider) {
                startTime = slider.values[0].toInt()
                endTime = slider.values[1].toInt()
                startAudioPlayback(startTime * 1000)


            }
        })

//        binding.startTimeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                startTime = progress
//                binding.startTimeLabel.text = "Start time: ${startTime}s"
//                // Ensure end time is always greater than start time
//                if (startTime >= endTime) {
//                    endTime = startTime + 1
//                    binding.endTimeSeekbar.progress = endTime
//                    binding.endTimeLabel.text = "End time: ${endTime}s"
//                }
//                isAudioTrimmed = false
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })
//
//        binding.endTimeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                endTime = progress
//                binding.endTimeLabel.text = "End time: ${endTime}s"
//                // Ensure end time is always greater than start time
//                if (endTime <= startTime) {
//                    startTime = endTime - 1
//                    binding.startTimeSeekbar.progress = startTime
//                   binding.startTimeLabel.text = "Start time: ${startTime}s"
//                }
//                isAudioTrimmed = false
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })

        checkPermissions()
    }
    private val selectAudioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    selectedAudioPath = getRealPathFromURI(uri)
                    binding.audioPathText.text = selectedAudioPath
//                    mediaPlayer = MediaPlayer().apply {
//                        setDataSource(selectedAudioPath)
//                        prepare()
//                        start()
//                        startAudioPlayback(startTime * 1000)
//                     stopAudioPlayback(endTime * 1000)
//                    }
                    initializeMediaPlayer()
                }
            }
        }
    private fun initializeMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(selectedAudioPath)
            prepareAsync()
            setOnPreparedListener {
                startAudioPlayback(startTime * 1000)
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
            type = "audio/*"
        }
        selectAudioLauncher.launch(intent)
    }
    private fun launchPlayMusicActivity(audiofile:String) {
        val timestamp = System.currentTimeMillis()
        val intent = Intent(this, PlayMusicActivity::class.java).apply {
            putExtra("audioPath", audiofile)
            putExtra("audioName", "trimmed_audio_$timestamp.mp3")
        }
        startActivity(intent)
    }


    private fun trimAudio() {
//        val outputFilePath = "${Environment.getExternalStorageDirectory()}/Download/Trim_audio12.mp3"
stopAudioPlayback()
//        val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/trimmed_audio15.mp3"
//        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val outputFilePath = File(downloadsDir, generateUniqueFileName()).absolutePath
        val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/${generateUniqueFileName()}"

        if (isAudioTrimmed && File(outputFilePath).exists()) {
            // If audio is already trimmed, just launch PlayMusicActivity
           launchPlayMusicActivity(outputFilePath)
            isAudioTrimmed=true
        } else {

            if (selectedAudioPath != null && startTime < endTime) {

                val cmd = arrayOf(
                    "-i", selectedAudioPath!!,
                    "-ss", startTime.toString(),
                    "-to", endTime.toString(),
                    "-c", "copy",
                    outputFilePath
                )


                FFmpeg.executeAsync(cmd) { _, returnCode ->
                    if (returnCode == Config.RETURN_CODE_SUCCESS) {
                        runOnUiThread {
                            launchPlayMusicActivity(outputFilePath)
                            isAudioTrimmed=true
                        }
                    } else {
                        runOnUiThread {
                            binding.audioPathText.text = "Failed to trim audio"
                        }
                    }
                }
            } else {
                binding.audioPathText.text =
                    "Please select a valid audio file and ensure start time is less than end time"
            }
        }
    }

//    private fun checkPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED ||
//            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                1
//            )
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permissions granted
//            } else {
//                // Permissions denied
//                Toast.makeText(this, "Permissions are required to select and save audio files", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
private fun checkPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 2)
        }
    } else {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

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
                // Permissions denied
                Toast.makeText(this, "Permissions are required to select and save audio files", Toast.LENGTH_SHORT).show()
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
//    private fun startAudioPlayback(startTimeMs: Int) {
//        mediaPlayer?.seekTo(startTimeMs)
//        mediaPlayer?.start()
//
//    }
//
//        private fun stopAudioPlayback() {
//            mediaPlayer?.seekTo(mediaPlayer?.duration ?: 0)
//            mediaPlayer?.pause()
//
//    }
private fun startAudioPlayback(startTimeMs: Int) {
    mediaPlayer?.seekTo(startTimeMs)
    mediaPlayer?.start()

//    endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
//    endTimeHandler = Handler(Looper.getMainLooper())
//    endTimeRunnable = Runnable {
//        mediaPlayer?.pause()
//    }
//    val endTimeMs = endTime * 1000
//    endTimeHandler?.postDelayed(endTimeRunnable!!, (endTimeMs - startTimeMs).toLong())
//
//    mediaPlayer?.setOnCompletionListener {
//        mediaPlayer?.seekTo(startTime * 1000)
//        mediaPlayer?.start()
//    }
    endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }

    // Initialize the handler and runnable
    endTimeHandler = Handler(Looper.getMainLooper())
    endTimeRunnable = Runnable {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(startTime * 1000)
        mediaPlayer?.start()
    }

    val endTimeMs = endTime * 1000
    val duration = endTimeMs - startTimeMs

    // Schedule the end time runnable
    endTimeHandler?.postDelayed(endTimeRunnable!!, duration.toLong())

    // Restart from start time when playback completes naturally
    mediaPlayer?.setOnCompletionListener {
        mediaPlayer?.seekTo(startTime * 1000)
        mediaPlayer?.start()
    }

}

    private fun stopAudioPlayback() {
        endTimeRunnable?.let { endTimeHandler?.removeCallbacks(it) }
        mediaPlayer?.pause()
    }

}

//    private val outputFileName = "trimmed_audio9.mp3"
//    private val outputFilePath: String by lazy {
//        val directory = getExternalFilesDir(Environment.ge)
//        val outputFile = File(directory, outputFileName)
//        if (!outputFile.exists()) {
//            outputFile.createNewFile()
//        }
//        outputFile.absolutePath
//    }