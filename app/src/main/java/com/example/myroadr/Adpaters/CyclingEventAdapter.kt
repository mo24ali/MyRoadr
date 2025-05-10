package com.example.myroadr.Adpaters

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myroadr.R
import com.example.myroadr.models.CyclingEvent

import kotlin.collections.*

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
        //val participants = view.findViewById<TextView>(R.id.textViewParticipants)
        val heart = view.findViewById<ImageView>(R.id.imageViewFavorite)
        val joinBtn = view.findViewById<Button>(R.id.buttonJoinEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cycling_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title
        //holder.participants.text = "${event.participants?.size ?: 0} members"

        val eventLocation = Location("eventProvider")
        eventLocation.latitude = event.latitude
        eventLocation.longitude = event.longitude

        val distanceMeters = userLocation.distanceTo(eventLocation).toInt()
        holder.distance.text = "$distanceMeters m"

        holder.joinBtn.setOnClickListener { onJoinClick(event) }
        holder.heart.setOnClickListener { onFavoriteClick(event) }
    }

    override fun getItemCount() = events.size

    fun updateData(newEvents: List<CyclingEvent>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
