package com.example.myroadr.util

import com.example.myroadr.models.CyclingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FavoritesManager {

    private val favoriteEvents = mutableListOf<CyclingEvent>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("Users")

    fun add(event: CyclingEvent) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (!favoriteEvents.any { it.id == event.id }) {
            favoriteEvents.add(event)
            dbRef.child(uid).child("favorites").child(event.id).setValue(event)
        }
    }

    fun remove(event: CyclingEvent) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        favoriteEvents.removeAll { it.id == event.id }
        dbRef.child(uid).child("favorites").child(event.id).removeValue()
    }

    fun isFavorite(event: CyclingEvent): Boolean {
        return favoriteEvents.any { it.id == event.id }
    }

    fun getAll(): List<CyclingEvent> = favoriteEvents

    fun loadFromFirebase(onComplete: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        dbRef.child(uid).child("favorites").get().addOnSuccessListener { snapshot ->
            favoriteEvents.clear()
            for (item in snapshot.children) {
                item.getValue(CyclingEvent::class.java)?.let {
                    favoriteEvents.add(it)
                }
            }
            onComplete()
        }
    }
}
