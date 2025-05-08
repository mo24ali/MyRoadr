package com.example.myroadr.activities

import  com.example.myroadr.activities.SignupActivity
import  com.example.myroadr.activities.ForgetPasswordActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.MainActivity
import com.example.myroadr.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bouton Connexion
        binding.LoginButton.setOnClickListener {
            val email = binding.mailLogin.text?.toString()?.trim() ?: ""
            val password = binding.passwordLogin.text?.toString()?.trim() ?: ""

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()

                         startActivity(Intent(this, MainActivity::class.java))
                         finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Échec de la connexion : ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Aller à l'inscription
        binding.signInText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Mot de passe oublié
        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }

        // Bouton retour
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
