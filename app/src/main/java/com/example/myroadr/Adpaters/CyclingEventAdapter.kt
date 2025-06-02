package com.example.myroadr.Adpaters

import android.content.Intent
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.R
import com.example.myroadr.activities.ShowDetailsEvent
import com.example.myroadr.models.CyclingEvent
import com.example.myroadr.util.FavoritesManager



class CyclingEventAdapter(
    private val userLocation: Location,
    private val onJoinClick: (CyclingEvent) -> Unit,
    private val onFavoriteClick: (CyclingEvent) -> Unit
) : RecyclerView.Adapter<CyclingEventAdapter.EventViewHolder>() {

    private val events = mutableListOf<CyclingEvent>()

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageBike = view.findViewById<ImageView>(R.id.imageViewBike)
        val title = view.findViewById<TextView>(R.id.textViewTitle)
        val distance = view.findViewById<TextView>(R.id.textViewDistance)
        val heart = view.findViewById<ImageView>(R.id.imageViewFavorite)
        val joinBtn = view.findViewById<Button>(R.id.buttonJoinEvent)
        val nbmembres = view.findViewById<TextView>(R.id.textViewParticipants)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cycling_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        holder.title.text = event.title

        val eventLocation = Location("eventProvider").apply {
            latitude = event.latitude
            longitude = event.longitude
        }

        val distanceMeters = userLocation.distanceTo(eventLocation).toInt()
        holder.distance.text = "$distanceMeters m"

        val isFav = FavoritesManager.isFavorite(event)
        holder.heart.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        holder.heart.setOnClickListener {
            if (isFav) {
                FavoritesManager.remove(event)
            } else {
                FavoritesManager.add(event)
            }
            notifyItemChanged(position)
            onFavoriteClick(event)
        }

        // ✅ Show number of members
        fetchNumberOfMembers(event.id) { memberText ->
            holder.nbmembres.text = memberText
        }

        holder.joinBtn.setOnClickListener { onJoinClick(event) }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ShowDetailsEvent::class.java).apply {
                putExtra("title", event.title)
                putExtra("description", event.description)
                putExtra("distance", distanceMeters)
                putExtra("id", event.id)
                putExtra("imageResId", R.drawable.bike_haibike)
            }
            context.startActivity(intent)
        }
    }



    override fun getItemCount() = events.size
    private fun fetchNumberOfMembers(eventId: String, callback: (String) -> Unit) {
        val ref = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("Events")
            .child(eventId)
            .child("joinedUsers")

        ref.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                callback("$count membres")
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                callback("0 membres")
            }
        })
    }


    fun updateData(newEvents: List<CyclingEvent>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
