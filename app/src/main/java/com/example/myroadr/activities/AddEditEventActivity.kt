package com.example.myroadr.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.databinding.ActivityAddEditEventBinding
import com.example.myroadr.models.CyclingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddEditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEventBinding
    private var eventId: String? = null
    private val dbRef = FirebaseDatabase.getInstance().getReference("Events")

    // Pour stocker les coordonnées si déjà existantes
    private var existingLatitude = 0.0
    private var existingLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra("eventId")
        Log.d("ADDEDIT", "🛠️ AddEditEventActivity lancé avec eventId = $eventId")


        if (eventId != null) {
            title = "Modifier l'événement"
            loadEventData(eventId!!)
        } else {
            title = "Ajouter un événement"
        }

        binding.dateInput.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                if (eventId == null) {
                    createEvent()
                } else {
                    updateEvent()
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
                binding.dateInput.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun createEvent() {
        val id = dbRef.push().key ?: return
        val event = getEventFromForm(id)
        dbRef.child(id).setValue(event).addOnSuccessListener {
            Toast.makeText(this, "Événement ajouté", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEvent() {
        val updatedEvent = getEventFromForm(eventId!!)
        dbRef.child(eventId!!).setValue(updatedEvent).addOnSuccessListener {
            Toast.makeText(this, "Événement mis à jour", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadEventData(id: String) {
        dbRef.child(id).get().addOnSuccessListener { snapshot ->
            val event = snapshot.getValue(CyclingEvent::class.java)
            event?.let {
                binding.titleInput.setText(it.title)
                binding.descriptionInput.setText(it.description)
                binding.dateInput.setText(it.date)
                binding.locationNameInput.setText(it.locationName)
                existingLatitude = it.latitude
                existingLongitude = it.longitude
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erreur de chargement : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEventFromForm(id: String): CyclingEvent {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return CyclingEvent(
            id = id,
            title = binding.titleInput.text.toString().trim(),
            description = binding.descriptionInput.text.toString().trim(),
            date = binding.dateInput.text.toString().trim(),
            locationName = binding.locationNameInput.text.toString().trim(),
            latitude = existingLatitude,
            longitude = existingLongitude,
            createdBy = uid,
            participants = listOf()
        )
    }

    private fun validateForm(): Boolean {
        return when {
            binding.titleInput.text.isNullOrBlank() -> {
                binding.titleInput.error = "Titre requis"
                false
            }
            binding.descriptionInput.text.isNullOrBlank() -> {
                binding.descriptionInput.error = "Description requise"
                false
            }
            binding.dateInput.text.isNullOrBlank() -> {
                binding.dateInput.error = "Date requise"
                false
            }
            binding.locationNameInput.text.isNullOrBlank() -> {
                binding.locationNameInput.error = "Lieu requis"
                false
            }
            else -> true
        }
    }
}
