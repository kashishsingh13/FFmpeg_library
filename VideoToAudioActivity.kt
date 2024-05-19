package com.example.randomapplication.Video

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.randomapplication.PlayMusicActivity
import com.example.randomapplication.R
import com.example.randomapplication.databinding.ActivityVideoToAudioBinding
import java.io.File

class VideoToAudioActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVideoToAudioBinding
    private var selectedVideoPath: String? = null
    private lateinit var selectedVideoUri: Uri
    private fun generateUniqueFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "extracted_audio_$timestamp.mp3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideoToAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectVideoButton.setOnClickListener { openFilePicker() }
        binding.videoToAudioButton.setOnClickListener { convertVideoToAudio() }
    }

    private val selectVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    selectedVideoUri = uri
                    selectedVideoPath = getRealPathFromURI(uri)
                    binding.videoPathText.text = selectedVideoPath
                    binding.video.visibility= View.VISIBLE
                    binding.video.setVideoURI(selectedVideoUri)
                    binding.video.start()
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

    private fun convertVideoToAudio() {
        val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/${generateUniqueFileName()}"
        if (selectedVideoPath != null) {
            val cmd = arrayOf(
                "-i", selectedVideoPath!!,
                "-q:a", "0",
                "-map", "a",
                outputFilePath
            )
            FFmpeg.executeAsync(cmd) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    runOnUiThread {
                        launchPlayMusicActivity(outputFilePath)
                    }
                } else {
                    runOnUiThread {
                        binding.videoPathText.text = "Failed to convert video to audio"
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please select a video file first", Toast.LENGTH_SHORT).show()
        }
    }



    private fun launchPlayMusicActivity(audioFilePath: String) {
        val intent = Intent(this, PlayMusicActivity::class.java).apply {
            putExtra("audioPath", audioFilePath)
            putExtra("audioName", File(audioFilePath).name)
        }
        startActivity(intent)
    }

}