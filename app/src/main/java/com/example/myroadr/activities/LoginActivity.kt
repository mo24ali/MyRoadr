package com.example.myroadr.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
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

            // Check for admin credentials
            if (email == "admin@admin.com" && password == "admin") {
                // Sign in with Firebase using admin credentials
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Connexion admin réussie", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this,
                                "Échec de la connexion admin : ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                // Regular user login
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
        }
        // Aller à l'inscription
        binding.signInText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Mot de passe oublié
        binding.forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
//            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        Toast.makeText(
//                            this,
//                            "Un lien de réinitialisation a été envoyé à $email",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Erreur : ${task.exception?.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
        }

        // Bouton retour
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
