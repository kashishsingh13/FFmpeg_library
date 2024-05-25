package com.example.practiceapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.practiceapplication.Model.User
import com.example.practiceapplication.databinding.ActivityMainBinding
import kotlin.math.truncate

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private var user = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            var name = binding.etName.text.toString().trim()
            var age = binding.etAge.text.toString().trim().toInt()
            var salary = binding.etSalary.text.toString().trim().toDouble()

            saveData(name, age, salary)
        }
        binding.btnGet.setOnClickListener {
           var intent= Intent(this, DetailsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun saveData(name: String, age: Int, salary: Double) {
        try {

            var sharePreference = getSharedPreferences("User", MODE_PRIVATE)
            var editor = sharePreference.edit()
            editor.putString("NAME", name)
            editor.putInt("AGE", age)
            editor.putFloat("SALARY", salary.toFloat())
            editor.commit()
            Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
        


    }
    


}