package com.example.myroadr.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myroadr.Adpaters.CyclingEventAdapter
import com.example.myroadr.databinding.ActivityMyEventsBinding
import com.example.myroadr.models.CyclingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.myroadr.R

class MyEventsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyEventsBinding
    private lateinit var adapter: CyclingEventAdapter
    private lateinit var userLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        userLocation = Location("default").apply {
            latitude = 33.5731
            longitude = -7.5898
        }

        adapter = CyclingEventAdapter(
            userLocation,
            onJoinClick = {},
            onFavoriteClick = {}
        )

        binding.recyclerViewMyOffers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMyOffers.adapter = adapter
        loadMyEvents(uid)
    }

    private fun loadMyEvents(uid: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Events")
        dbRef.orderByChild("createdBy").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<CyclingEvent>()
                    for (child in snapshot.children) {
                        child.getValue(CyclingEvent::class.java)?.let {
                            list.add(it)
                        }
                    }

                    adapter.updateData(list)

                    // Affiche le message vide si aucun événement
                    binding.textViewEmpty.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE

                    // Affiche les boutons "..." pour chaque item
                    binding.recyclerViewMyOffers.post {
                        for (i in list.indices) {
                            val viewHolder = binding.recyclerViewMyOffers.findViewHolderForAdapterPosition(i)
                            val event = list[i]
                            val btnManage = viewHolder?.itemView?.findViewById<ImageButton>(R.id.btnManage)
                            btnManage?.apply {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    showPopupMenu(it, event)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyEventsActivity, "Erreur de chargement", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showPopupMenu(anchor: View, event: CyclingEvent) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_my_offer_options, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {
                    Log.d("MYEVENTS", "✏️ Modification demandée pour l’événement ID = ${event.id}")
                    val intent = Intent(this, AddEditEventActivity::class.java)
                    intent.putExtra("eventId", event.id)
                    startActivity(intent)
                    true
                }


                R.id.menu_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Confirmation de suppression")
                        .setMessage("Voulez-vous vraiment supprimer cet événement ?")
                        .setPositiveButton("Oui") { dialog, _ ->
                            FirebaseDatabase.getInstance()
                                .getReference("Events")
                                .child(event.id)
                                .removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Événement supprimé", Toast.LENGTH_SHORT).show()
                                    recreate() // recharge la liste
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erreur : ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                            dialog.dismiss()
                        }
                        .setNegativeButton("Annuler") { dialog, _ -> dialog.dismiss() }
                        .show()
                    true
                }

                else -> false
            }
        }
        popup.show()
    }
}
