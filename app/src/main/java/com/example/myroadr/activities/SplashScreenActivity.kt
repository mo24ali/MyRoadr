package com.example.myroadr.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.MainActivity
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.getStartedButton.setOnClickListener {
            navigateToMainActivity()
        }
        binding.loginText.text= Html.fromHtml("Already have an account? <b>Log in</b>", Html.FROM_HTML_MODE_LEGACY)
        binding.loginText.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, Onboarding1Activity::class.java)
        startActivity(intent)
        finish()
    }
}