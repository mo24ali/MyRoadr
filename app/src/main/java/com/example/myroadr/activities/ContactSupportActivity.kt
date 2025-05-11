package com.example.myroadr.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.databinding.ActivityContactSupportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ContactSupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactSupportBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        setupViews()
    }

    private fun setupViews() {
        // Handle back arrow click
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Handle submit button click
        binding.submitButtonMessage.setOnClickListener {
            validateAndSubmitMessage()
        }
    }

    private fun validateAndSubmitMessage() {
        val title = binding.titleInputMessage.text.toString().trim()
        val description = binding.descriptionInputMessage.text.toString().trim()

        when {
            title.isEmpty() -> {
                binding.titleInputMessage.error = "Please enter a title"
                binding.titleInputMessage.requestFocus()
            }
            description.isEmpty() -> {
                binding.descriptionInputMessage.error = "Please enter your message"
                binding.descriptionInputMessage.requestFocus()
            }
            else -> {
                submitMessageToFirebase(title, description)
            }
        }
    }

    private fun submitMessageToFirebase(title: String, description: String) {
        // Create a loading dialog
        val progressDialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Generate a unique ID for the message
        val messageId = database.child("support_messages").push().key

        // Get current date and time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val currentTime = sdfTime.format(Date())

        // Get current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "unknown"
        val userId = currentUser?.uid ?: "unknown_user"

        // Create message data
        val messageData = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to currentDate,
            "time" to currentTime,
            "timestamp" to "$currentDate $currentTime",
            "userId" to userId,
            "userEmail" to userEmail
        )

        if (messageId != null) {
            database.child("support_messages").child(messageId)
                .setValue(messageData)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        showSuccessDialog()
                        clearForm()
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send message: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed to generate message ID",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Message Sent Successfully")
            .setMessage("Thank you for contacting us! We'll respond to your message within 24 hours.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun clearForm() {
        binding.titleInputMessage.text?.clear()
        binding.descriptionInputMessage.text?.clear()
    }
}