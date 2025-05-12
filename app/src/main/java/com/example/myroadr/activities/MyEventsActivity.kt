package com.example.myroadr.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
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
    private lateinit var database: DatabaseReference
    private lateinit var adapter: CyclingEventAdapter
    private val myEvents = mutableListOf<CyclingEvent>()
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

//        val adapter = CyclingEventAdapter(userLocation, onJoinClick = {}, onFavoriteClick = {}) {
//            override fun onBindViewHolder(holder: CyclingEventAdapter.EventViewHolder, position: Int) {
//                super.onBindViewHolder(holder, position)
//
//                // Montre uniquement ici le bouton de gestion
//                holder.itemView.findViewById<ImageButton>(R.id.btnManage)?.apply {
//                    visibility = View.VISIBLE
//                    setOnClickListener {
//                        showPopupMenu(it, getItem(position))
//                    }
//                }
//            }
//        }
        val adapter = CyclingEventAdapter(
            userLocation,
            onJoinClick = {},
            onFavoriteClick = {}
        )

        binding.recyclerViewMyOffers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMyOffers.adapter = adapter

        loadMyEvents(uid, adapter)
    }

    private fun loadMyEvents(uid: String, adapter: CyclingEventAdapter) {
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

                    // 💡 Une fois la liste mise à jour, active les boutons "..." visibles
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

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showPopupMenu(anchor: View, event: CyclingEvent) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_my_offer_options, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {
                    // Lancer un intent vers EditEventActivity
                    val i = Intent(this, AddEditEventActivity::class.java)
                    i.putExtra("eventId", event.id)
                    startActivity(i)
                    true
                }
                R.id.menu_delete -> {
                    val ref = FirebaseDatabase.getInstance()
                        .getReference("Events")
                        .child(event.id)
                    ref.removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Événement supprimé", Toast.LENGTH_SHORT).show()
                        recreate() // recharge la liste
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

}
