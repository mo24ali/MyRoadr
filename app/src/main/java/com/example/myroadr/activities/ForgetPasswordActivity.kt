package com.example.myroadr.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityForgetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Utilisation du ViewBinding
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInText.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))

        }
        binding.forgotPasswordText.setOnClickListener {
            val email = binding.mailRecovering.text?.toString()?.trim() ?: ""

            if (email.isBlank()) {
                Toast.makeText(this, "Veuillez entrer votre adresse email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 👉 Créer le dialog de chargement
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
            val loadingText = dialogView.findViewById<TextView>(R.id.loadingText)
            loadingText.text = "Envoi du lien de réinitialisation..."
            val loadingDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            loadingDialog.show()

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    loadingDialog.dismiss()
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Un lien de réinitialisation a été envoyé à $email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Erreur : ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }
}
