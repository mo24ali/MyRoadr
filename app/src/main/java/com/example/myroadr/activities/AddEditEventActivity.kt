package com.example.myroadr.activities

import android.app.DatePickerDialog
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra("eventId")

        if (eventId != null) {
            // Mode édition
            title = "Modifier l'événement"
            loadEventData(eventId!!)
        } else {
            // Mode ajout
            title = "Ajouter un événement"
        }

        binding.dateInput.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener {
            if (eventId == null) {
                createEvent()
            } else {
                updateEvent()
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
        }
    }

    private fun updateEvent() {
        val updatedEvent = getEventFromForm(eventId!!)
        dbRef.child(eventId!!).setValue(updatedEvent).addOnSuccessListener {
            Toast.makeText(this, "Événement mis à jour", Toast.LENGTH_SHORT).show()
            finish()
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
                // TODO: Charger latitude/longitude si tu veux les éditer aussi
            }
        }
    }

    private fun getEventFromForm(id: String): CyclingEvent {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return CyclingEvent(
            id = id,
            title = binding.titleInput.text.toString(),
            description = binding.descriptionInput.text.toString(),
            date = binding.dateInput.text.toString(),
            locationName = binding.locationNameInput.text.toString(),
            latitude = 0.0, // tu peux ajouter un champ si besoin
            longitude = 0.0,
            createdBy = uid,
            participants = listOf()
        )
    }
}
