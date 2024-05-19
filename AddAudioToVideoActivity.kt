package com.example.randomapplication.Video

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.randomapplication.R
import com.example.randomapplication.databinding.ActivityAddAudioToVideoBinding
import java.io.File

class AddAudioToVideoActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddAudioToVideoBinding
    private var selectedVideoPath: String? = null
    private var selectedAudioPath: String? = null
    private var isSelectingVideo = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddAudioToVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectFilesButton.setOnClickListener { openFilePicker() }
        binding.addAudioToVideoButton.setOnClickListener { addAudioToVideo() }
    }

    private val selectFilesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val mimeType = contentResolver.getType(uri)
                    if (mimeType?.startsWith("video/") == true) {
                        selectedVideoPath = getRealPathFromURI(uri)
                        binding.videoPathText.text = selectedVideoPath
                    } else if (mimeType?.startsWith("audio/") == true) {
                        selectedAudioPath = getRealPathFromURI(uri)
                        binding.audioPathText.text = selectedAudioPath
                    }
                }
            } ?: data?.data?.let { uri ->
                val mimeType = contentResolver.getType(uri)
                if (mimeType?.startsWith("video/") == true) {
                    selectedVideoPath = getRealPathFromURI(uri)
                    binding.videoPathText.text = selectedVideoPath
                } else if (mimeType?.startsWith("audio/") == true) {
                    selectedAudioPath = getRealPathFromURI(uri)
                    binding.audioPathText.text = selectedAudioPath
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "audio/*"))
        }
        selectFilesLauncher.launch(intent)
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

    private fun addAudioToVideo() {
        if (selectedVideoPath != null && selectedAudioPath != null) {
            val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/${generateUniqueFileName()}"
            val cmd = arrayOf(
                "-i", selectedVideoPath!!,
                "-i", selectedAudioPath!!,
                "-c:v", "copy",
                "-map", "0:v:0",
                "-map", "1:a:0",
                outputFilePath
            )
            FFmpeg.executeAsync(cmd) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    runOnUiThread { launchPlayVideoActivity(outputFilePath) }
                } else {
                    runOnUiThread { Toast.makeText(this, "Failed to add audio to video", Toast.LENGTH_SHORT).show() }
                }
            }
        } else {
            Toast.makeText(this, "Please select both video and audio files", Toast.LENGTH_SHORT).show()
        }
    }



    private fun generateUniqueFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "output_video_$timestamp.mp4"
    }

    private fun launchPlayVideoActivity(videoPath: String) {
        val timestamp = System.currentTimeMillis()
        val intent = Intent(this, PlayVideoActivity::class.java).apply {
            putExtra("videoPath", videoPath)
            putExtra("videoName", "trimmed_video_$timestamp.mp4")
        }
        startActivity(intent)
    }
}