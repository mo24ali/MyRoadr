package com.example.myroadr.models

data class CyclingEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // Format ISO 8601 (ex: "2025-05-06T10:00")
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdBy: String = "", // UID de l'utilisateur créateur
    val participants: List<String> = listOf() // UID des cyclistes inscrits
)

