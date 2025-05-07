package com.example.myroadr.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityLoginBinding
import com.example.myroadr.databinding.ActivityOnboarding1Binding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInText.setOnClickListener {
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
        }
        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this,ForgetPasswordActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }
}