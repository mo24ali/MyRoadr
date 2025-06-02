package com.example.myroadr.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivitySignupBinding
import com.google.android.gms.common.util.CollectionUtils.mapOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.signInText.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

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

            val dialog = layoutInflater.inflate(R.layout.dialog_add_event, null)
            val text_changed = dialog.findViewById<TextView>(R.id.loadingText)
            text_changed.text = "Inscription en cours ...."

            // 👉 Créer et afficher le loading dialog
            val loadingDialog = AlertDialog.Builder(this)
                .setView(dialog)
                .setCancelable(false)
                .create()
            loadingDialog.show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: run {
                            loadingDialog.dismiss()
                            return@addOnCompleteListener
                        }

                        val database = FirebaseDatabase.getInstance().reference
                        val userMap = mapOf(
                            "email" to email,
                            "username" to username,
                            "password" to password
                        )

                        database.child("Users").child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                loadingDialog.dismiss() // 👉 Fermer le loader
                                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                loadingDialog.dismiss()
                                Toast.makeText(this, "Erreur lors de l'enregistrement : ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(this, "Échec de l'inscription : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }
}
