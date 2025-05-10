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
import com.example.myroadr.DB.AppDatabase
import com.example.myroadr.Entity.CyclingEventEntity
import com.example.myroadr.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var dateInput: EditText
    private lateinit var locationInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        db = AppDatabase.getDatabase(this)
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

            // Récupérer nom utilisateur
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

            userRef.child("username").get().addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java) ?: "Unknown User"

                // Récupérer localisation
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

                        val event = CyclingEventEntity(
                            id = 0, // auto-incrementé par Room
                            title = title,
                            description = desc,
                            date = date,
                            locationName = loc,
                            latitude = lat,
                            longitude = long,
                            createdBy = username,
                            participantsCount = 0
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            db.eventDao().insert(event)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@AddEventActivity, "Événement ajouté", Toast.LENGTH_SHORT).show()
                                finish()
                            }
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
