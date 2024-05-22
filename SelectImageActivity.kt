package com.example.randomapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.randomapplication.databinding.ActivitySelectImageBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectImageBinding
    private lateinit var imageUri: Uri
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.camera.setOnClickListener {
            openCamera()
        }

        binding.gallery.setOnClickListener {
            openGallery()
        }
    }
//    private fun openCamera() {
//        val imageFile = File.createTempFile("tmp_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//        val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        }
//        startActivity(takePictureIntent)
//        saveImagePathAndFinish(imageFile.absolutePath)
//    }
//
////    private fun openGallery() {
////        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
////        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST_CODE)
////    }
//private fun openGallery() {
//    if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST_CODE)
//    } else {
//
//        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
//    }
//}
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openGallery()
//            } else {
//                // Permission denied, handle accordingly
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
//            val dataUri = data?.data
//            if (dataUri != null) {
//                saveUriAndFinish(dataUri.toString())
//            }
//        }
//    }
//
//    private fun saveUriAndFinish(uri: String) {
//        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
//        with(sharedPreferences.edit()) {
//            putString("imageUri", uri)
//            apply()
//        }
//        finish()
//    }
//
//    private fun saveImagePathAndFinish(imagePath: String) {
//        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
//        with(sharedPreferences.edit()) {
//            putString("imagePath", imagePath)
//            apply()
//        }
//        finish()
//    }
//private fun openCamera() {
//    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//
//}
private fun openCamera() {
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // Ensure there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(packageManager) != null) {
        // Create a file to save the image
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.e("SelectImageActivity", "Error creating image file: ${ex.message}")
            null
        }
        // Continue only if the file was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
}

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
            Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            imageUri = Uri.fromFile(this)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Image captured and saved to imageUri specified in the Intent
                    saveImagePathAndFinish(imageUri.path!!)
                }
                REQUEST_IMAGE_PICK -> {
                    // Image picked from gallery
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val filePath = getRealPathFromURI(it)
                        if (filePath != null) {
                            saveImagePathAndFinish(filePath)
                        }
                    }
                }
            }
        }
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            cursor?.getString(columnIndex!!)
        } finally {
            cursor?.close()
        }
    }

    private fun saveImagePathAndFinish(imagePath: String) {
        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("imagePath", imagePath)
            apply()
        }
        finish()
    }
//    companion object {
//        private const val GALLERY_REQUEST_CODE = 1
//        private const val PERMISSION_REQUEST_CODE = 1001
//    }

//    private fun handleGalleryImage() {
//        val selectedImageUri = viewModel.selectedImageUri.value
//        selectedImageUri?.let {
////            binding.setImage.setImageURI(it)
//        }

}

    //private fun openCamera() {
//    val imageFile = File.createTempFile("tmp_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//    imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
//    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//        putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//    }
//    startActivity(takePictureIntent)
//    saveImagePathAndFinish(imageFile.absolutePath)
//}
//
//private fun openGallery() {
//    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//    startActivity(pickPhotoIntent)
//    saveImagePathAndFinish(imageUri.path!!)
//}
//
//private fun saveImagePathAndFinish(imagePath: String) {
//    val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
//    with(sharedPreferences.edit()) {
//        putString("imagePath", imagePath)
//        apply()
//    }
//    finish()
//}
//
//    private fun openCamera() {
//        val imageFile = File.createTempFile(
//            "tmp_image",
//            ".jpg",
//            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        )
//        val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        }
//        startActivity(takePictureIntent)
//        saveImagePath(imageFile.absolutePath)
//    }
//
//    private fun openGallery() {
//        val pickPhotoIntent =
//            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivity(pickPhotoIntent)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        handleGalleryImage()
//    }
//
//    private fun handleGalleryImage() {
//        val dataUri = intent?.data
//        if (dataUri != null) {
//            ImageModel.imageUri = dataUri
//            finish()
//        }
//    }
//
//    private fun saveImagePath(imagePath: String) {
//        ImageModel.imageUri = Uri.fromFile(File(imagePath))
//        finish()
//    }
//    private fun openCamera() {
//        val imageFile = File.createTempFile("tmp_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//        imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//        }
//        startActivity(takePictureIntent)
//        ImageModel.imageUri = imageUri
//        finish()
//    }
//
//    private fun openGallery() {
//        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivity(pickPhotoIntent)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        handleGalleryImage()
//    }
//
//    private fun handleGalleryImage() {
//        val dataUri = intent?.data
//        if (dataUri != null) {
//            ImageModel.imageUri = dataUri
//            finish()
//        }
//    }
