package com.example.myroadr.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityOnboarding1Binding

class Onboarding1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboarding1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        binding = ActivityOnboarding1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.skipButton.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)

        }
        binding.nextButton.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }



}