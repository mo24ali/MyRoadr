package com.example.myroadr.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Update date text with current date
        updateCurrentDate()

        // Fetch statistics from Firebase
        fetchStatistics()

        // Set click listeners for action cards
        setupClickListeners()
    }

    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        binding.dateText.text = currentDate
    }

    private fun fetchStatistics() {
        // Fetch number of users
        database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCount = snapshot.childrenCount
                binding.valueNbUsers.text = userCount.toString()

                // ✅ Count users online
                var onlineCount = 0
                for (child in snapshot.children) {
                    val isOnline = child.child("enLigne").getValue(Boolean::class.java) ?: false
                    if (isOnline as Boolean) onlineCount++
                }
                binding.valueNbMetric4.text = onlineCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch number of events
        database.child("Events").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventCount = snapshot.childrenCount
                binding.valueNbPublications.text = eventCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        // Fetch number of events
        database.child("support_messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventCount = snapshot.childrenCount
                binding.valueNbAvertissements.text = eventCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminDashboardActivity, "Failed to load events: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        // Manage Messages click
        binding.messageshow.setOnClickListener {

            startActivity(Intent(this, ShowMessagesActivity::class.java))
        }

        // Logout click with confirmation dialog
        binding.logoutdahsbord.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                logoutUser()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish() // Close current activity
        // Start your LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
    }
}