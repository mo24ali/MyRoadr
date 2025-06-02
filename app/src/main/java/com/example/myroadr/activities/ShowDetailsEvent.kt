package com.example.myroadr.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myroadr.R
import com.example.myroadr.databinding.ActivityShowDetailsEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowDetailsEvent : AppCompatActivity() {

    private lateinit var binding: ActivityShowDetailsEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDetailsEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupération des données passées par Intent
        val eventTitle = intent.getStringExtra("title") ?: "Titre inconnu"
        val eventDescription = intent.getStringExtra("description") ?: ""
        val eventDistance = intent.getIntExtra("distance", 0)
        val eventId = intent.getStringExtra("id") ?: ""
        val imageResId = intent.getIntExtra("imageResId", R.drawable.bike_haibike)

        // Afficher dynamiquement le nombre de participants
        val joinedU = FirebaseDatabase.getInstance()
            .getReference("Events")
            .child(eventId)
            .child("joinedUsers")

        joinedU.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                binding.datepub.text = "$count members"
            }

            override fun onCancelled(error: DatabaseError) {
                binding.datepub.text = "0 members"
            }
        })


        // Remplissage de l'UI
        binding.imageSliderVp.setImageResource(imageResId)
        binding.distanceBadge.text = "Distance ${eventDistance} m"
        binding.titleTv.text = eventTitle
        binding.descriptionTv.text = eventDescription
        binding.titleLabelTv.text = eventTitle
        binding.descriptionLabelTv.text = "Description"
        binding.textviewdetailspost.text = "Details of Cycling Event"
        fetchOwnerName(eventId) { ownerName ->
            binding.donneurNomTv.text = ownerName
        }


        // Désactiver le bouton si déjà inscrit
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val checkRef = FirebaseDatabase.getInstance()
                .getReference("Events")
                .child(eventId)
                .child("joinedUsers")
                .child(currentUser.uid)

            checkRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.joinEventButton.isEnabled = false
                        binding.joinEventButton.text = "Déjà inscrit"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // Gestion du clic pour rejoindre l'événement
        binding.joinEventButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, "Connexion requise", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = currentUser.uid
            val joinedRef = FirebaseDatabase.getInstance()
                .getReference("Events")
                .child(eventId)
                .child("joinedUsers")
                .child(uid)

            joinedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@ShowDetailsEvent, "Vous êtes déjà inscrit à cet événement", Toast.LENGTH_SHORT).show()
                        binding.joinEventButton.isEnabled = false
                        binding.joinEventButton.text = "Aleardy Joined"
                    } else {
                        AlertDialog.Builder(this@ShowDetailsEvent)
                            .setTitle("Confirmation")
                            .setMessage("Voulez-vous vraiment rejoindre cet événement ?")
                            .setPositiveButton("Oui") { dialog, _ ->
                                joinedRef.setValue(true)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@ShowDetailsEvent, "Vous avez rejoint l'événement", Toast.LENGTH_SHORT).show()
                                        binding.joinEventButton.isEnabled = false
                                        binding.joinEventButton.text = "Déjà inscrit"
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this@ShowDetailsEvent, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                dialog.dismiss()
                            }
                            .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ShowDetailsEvent, "Erreur lors de la vérification : ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    }

    private fun fetchOwnerName(eventId: String, callback: (String) -> Unit) {
        val db = FirebaseDatabase.getInstance()
        val eventRef = db.getReference("Events").child(eventId)

        eventRef.child("createdBy").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ownerId = snapshot.getValue(String::class.java)
                if (ownerId != null && ownerId.length >= 20) { // Probable UID Firebase
                    val userRef = db.getReference("Users").child(ownerId).child("username")
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val username = userSnapshot.getValue(String::class.java)
                            callback(username ?: "Nom inconnu")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback("Erreur de chargement")
                        }
                    })
                } else {
                    // Si le champ createdBy est un nom direct (ex: "Ali"), on le retourne tel quel
                    callback(ownerId ?: "Auteur inconnu")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Erreur de chargement")
            }
        })
    }


