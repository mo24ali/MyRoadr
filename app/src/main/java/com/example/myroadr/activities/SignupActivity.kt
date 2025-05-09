package com.example.myroadr.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.databinding.ActivitySignupBinding
import com.google.android.gms.common.util.CollectionUtils.mapOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString() ?: ""
            val password = binding.passwordEditText.text?.toString() ?: ""
            val repPassword = binding.passwordVerificationEditText.text?.toString() ?: ""
            val username = binding.usernameText.text?.toString() ?: ""

            if (email.isBlank() || password.isBlank() || repPassword.isBlank() || username.isBlank()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repPassword) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val database = FirebaseDatabase.getInstance().reference
                        val userMap = mapOf(
                            "email" to email,
                            "username" to username,
                            "password" to password
                        )

                        database.child("Users").child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                                 startActivity(Intent(this, MainActivity::class.java)) // facultatif
                                 finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erreur lors de l'enregistrement : ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Échec de l'inscription : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
