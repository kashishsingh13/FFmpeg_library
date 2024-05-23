package com.example.randomapplication

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.randomapplication.databinding.ActivityChooseImageBinding

class ChooseImageActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChooseImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChooseImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.chooseImage.setOnClickListener {
            startActivity(Intent(this,SelectImageActivity::class.java))
        }

    }
    override fun onResume() {
        super.onResume()
        loadImageFromPreferences()
    }

    private fun loadImageFromPreferences() {
        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
        val imagePath = sharedPreferences.getString("imagePath", null)
        val imageUri = sharedPreferences.getString("imageUri", null)

        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            binding.setImage.setImageBitmap(bitmap)
        }

        imageUri?.let {
            val uri = Uri.parse(it)
            binding.setImage.setImageURI(uri)
        }
    }
//    override fun onResume() {
//        super.onResume()
//        loadImageFromModel()
//    }

//    private fun loadImageFromPreferences() {
//        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
//        val imagePath = sharedPreferences.getString("imagePath", null)
//        imagePath?.let {
//            val bitmap = BitmapFactory.decodeFile(it)
//            binding.setImage.setImageBitmap(bitmap)
//        }


//    private fun loadImageFromModel() {
//        val imageUri = ImageModel.imageUri
//        imageUri?.let {
//            binding.setImage.setImageURI(it)
//        }

//    private fun loadImageFromPreferences() {
//        val sharedPreferences = getSharedPreferences("ImagePref", Context.MODE_PRIVATE)
//        val imagePath = sharedPreferences.getString("imagePath", null)
//        val imageUri = sharedPreferences.getString("imageUri", null)
//
//        imagePath?.let {
//            val bitmap = BitmapFactory.decodeFile(it)
//            binding.setImage.setImageBitmap(bitmap)
//        }
//
//        imageUri?.let {
//            val uri = Uri.parse(it)
//            binding.setImage.setImageURI(uri)
//        }
//



}

//select
//
//package com.example.crudsqlite.imageview
//
//import android.graphics.Bitmap
//import android.net.Uri
//
//object DataPass {
//    var bitmap: Bitmap? = null
//    var uri: Uri? = null
//}
//package com.example.crudsqlite.imageview
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.widget.Button
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.crudsqlite.R
//
//class ImageviewActivity : AppCompatActivity() {
//    private lateinit var myButton: Button
//    private lateinit var img1: ImageView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_imageview)
//
//        img1 = findViewById(R.id.img1)
//        myButton = findViewById(R.id.myButton)
//
//        myButton.setOnClickListener {
//            val intent = Intent(this, BothImageviewActivity::class.java)
//            startActivity(intent)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val bitmap = DataPass.bitmap
//        if (bitmap != null) {
//            img1.setImageBitmap(bitmap)
//        }
//        val uri = DataPass.uri
//        if (uri != null) {
//            img1.setImageURI(uri)
//        }
//    }
//}
// package com.example.crudsqlite.imageview
//
//import android.app.Activity
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.crudsqlite.R
//
//class BothImageviewActivity : AppCompatActivity() {
//    private lateinit var camera: ImageView
//    private lateinit var gallery: ImageView
//    private val CAMERA_REQ_CODE = 100
//    private val GALLERY_REQ_CODE = 101
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_both_imageview)
//
//        camera = findViewById(R.id.camera)
//        gallery = findViewById(R.id.gallery)
//
//        camera.setOnClickListener {
//            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(cameraIntent, CAMERA_REQ_CODE)
//        }
//
//        gallery.setOnClickListener {
//            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(galleryIntent, GALLERY_REQ_CODE)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == GALLERY_REQ_CODE && data != null) {
//                val imageUri: Uri? = data.data
//                DataPass.uri = imageUri
//            } else if (requestCode == CAMERA_REQ_CODE && data != null) {
//                val photo: Bitmap? = data.extras?.get("data") as? Bitmap
//                DataPass.bitmap = photo
//            }
//            finish()
//        }
//    }
//}
//select activity
//class SelectImageActivity : AppCompatActivity() {
//    private lateinit var binding: ActivitySelectImageBinding
//
//    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
//        if (bitmap != null) {
//            val intent = Intent().apply {
//                putExtra("imageBitmap", bitmap)
//            }
//            setResult(Activity.RESULT_OK, intent)
//            finish()
//        }
//    }
//
//    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        if (uri != null) {
//            val intent = Intent().apply {
//                putExtra("imageUri", uri.toString())
//            }
//            setResult(Activity.RESULT_OK, intent)
//            finish()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivitySelectImageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.camera.setOnClickListener {
//            cameraLauncher.launch(null)
//        }
//
//        binding.gallery.setOnClickListener {
//            galleryLauncher.launch("image/*")
//        }
//    }
//}

//choose activity
//class ChooseImageActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityChooseImageBinding
//
//    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            result.data?.let { intent ->
//                intent.getParcelableExtra<Bitmap>("imageBitmap")?.let { bitmap ->
//                    binding.setImage.setImageBitmap(bitmap)
//                }
//                intent.getStringExtra("imageUri")?.let { uriString ->
//                    val uri = Uri.parse(uriString)
//                    binding.setImage.setImageURI(uri)
//                }
//            }
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityChooseImageBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.chooseImage.setOnClickListener {
//            val intent = Intent(this, SelectImageActivity::class.java)
//            selectImageLauncher.launch(intent)
//        }
//    }
//}