package com.example.practiceapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.practiceapplication.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sharedpreference = getSharedPreferences("User", MODE_PRIVATE)
        var name = sharedpreference.getString("NAME", "")
        var age = sharedpreference.getInt("AGE", 0)
        var salary = sharedpreference.getFloat("SALARY", 0.0F)

        binding.tvResult.text="""
            NAME:$name
            AGE:$age
            SALARY:$salary
        """.trimIndent()

    }
}