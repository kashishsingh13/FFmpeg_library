package com.example.randomapplication.Video

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.randomapplication.PlayMusicActivity
import com.example.randomapplication.R
import com.example.randomapplication.databinding.ActivityMergeAudioBinding
import java.io.File

class MergeAudioActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMergeAudioBinding
    private var audio1Path: String? = null
    private var audio2Path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMergeAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.selectFilesButton.setOnClickListener { openFilePicker() }
        binding.addAudioToVideoButton.setOnClickListener { mergeAudio() }
    }

    private val selectFilesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.clipData?.let { clipData ->
                if (clipData.itemCount == 2) {
                    audio1Path = getRealPathFromURI(clipData.getItemAt(0).uri)
                    audio2Path = getRealPathFromURI(clipData.getItemAt(1).uri)
                    binding.audio1PathText.text = audio1Path
                    binding.audio2PathText.text = audio2Path
                } else {
                    Toast.makeText(this, "Please select two audio files", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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

    private fun mergeAudio() {
        if (audio1Path != null && audio2Path != null) {
            val outputFilePath = "${getExternalFilesDir(null)?.absolutePath}/${generateUniqueFileName()}"
//            val outputFilePath = File(Environment.getExternalStorageDirectory().absolutePath , "${generateUniqueFileName()}"

            val cmd = arrayOf(
                "-i", audio1Path!!,
                "-i", audio2Path!!,
                "-filter_complex", "[0:a][1:a]amix=inputs=2:duration=first:dropout_transition=3", // Merge audio
                "-c:a", "libmp3lame", // Output codec
                outputFilePath
            )
            FFmpeg.executeAsync(cmd) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    runOnUiThread { launchPlayMusicActivity(outputFilePath) }
                } else {
                    runOnUiThread { Toast.makeText(this, "Failed to merge audio", Toast.LENGTH_SHORT).show() }
                }
            }
        } else {
            Toast.makeText(this, "Please select two audio files", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateUniqueFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "merged_audio_$timestamp.mp3"
    }

    private fun launchPlayMusicActivity(audioPath: String) {
        val timestamp = System.currentTimeMillis()
        val intent = Intent(this, PlayMusicActivity::class.java).apply {
            putExtra("audioPath", audioPath)
            putExtra("audioName", "merged_audio_$timestamp.mp3")
        }
        startActivity(intent)
    }
}