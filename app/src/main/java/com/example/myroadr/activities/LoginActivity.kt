package com.example.myroadr.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInText.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
        }

        binding.LoginButton.setOnClickListener {
            val email = binding.mailLogin.text?.toString()?.trim() ?: ""
            val password = binding.passwordLogin.text?.toString()?.trim() ?: ""

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 👉 Créer et afficher le dialog de chargement
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
            val loadingText = dialogView.findViewById<TextView>(R.id.loadingText)
            loadingText.text = if (email == "admin@admin.com") "Connexion admin..." else "Connexion en cours..."

            val loadingDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            loadingDialog.show()

            // 🔐 Connexion admin
            if (email == "admin@admin.com" && password == "adminadmin") {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loadingDialog.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Connexion admin réussie", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AdminDashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Échec de la connexion admin : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                // 🔐 Connexion utilisateur classique
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loadingDialog.dismiss()
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                                userRef.child("enLigne").setValue(true)
                            }

                            Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Échec de la connexion : ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
//
        }

        // Bouton retour
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
}
