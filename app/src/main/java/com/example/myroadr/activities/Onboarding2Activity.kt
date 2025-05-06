package com.example.myroadr.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myroadr.MainActivity
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityOnboarding1Binding
import com.example.myroadr.databinding.ActivityOnboarding2Binding

class Onboarding2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.skipButtonUnlock.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.nextButtonUnlock.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}