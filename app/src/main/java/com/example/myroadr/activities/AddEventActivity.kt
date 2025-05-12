package com.example.myroadr.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.myroadr.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var dateInput: EditText
    private lateinit var locationInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dateInput = findViewById(R.id.dateInput)
        locationInput = findViewById(R.id.locationInput)

        // 🗓️ Auto-fill current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        dateInput.setText(dateFormat.format(Date()))

        // 📍 Auto-fill location if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addressList.isNullOrEmpty()) {
                        val city = addressList[0].locality ?: "Unknown"
                        locationInput.setText(city)
                    } else {
                        locationInput.setText("Unknown location")
                    }
                }
            }
        }

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val title = findViewById<EditText>(R.id.titleInput).text.toString()
            val desc = findViewById<EditText>(R.id.descriptionInput).text.toString()
            val date = dateInput.text.toString()
            val loc = locationInput.text.toString()

            if (title.isBlank() || desc.isBlank() || date.isBlank() || loc.isBlank()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.child("username").get().addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java) ?: "Unknown User"

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                    return@addOnSuccessListener
                }

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude

                        val eventId = FirebaseDatabase.getInstance().getReference("Events").push().key ?: return@addOnSuccessListener
                        val firebaseEvent = mapOf(
                            "id" to eventId,
                            "title" to title,
                            "description" to desc,
                            "date" to date,
                            "locationName" to loc,
                            "latitude" to lat,
                            "longitude" to long,
                            "createdBy" to userId,
                            "participants" to listOf<String>()
                        )

                        FirebaseDatabase.getInstance().getReference("Events")
                            .child(eventId)
                            .setValue(firebaseEvent)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Événement ajouté à Firebase", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erreur d'ajout Firebase", Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(this, "Impossible de récupérer la position", Toast.LENGTH_SHORT).show()
                    }
                }

            }.addOnFailureListener {
                Toast.makeText(this, "Erreur de récupération du nom utilisateur", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
