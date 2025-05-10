package com.example.myroadr.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(tableName = "cycling_events")
//data class CyclingEventEntity(
//    @PrimaryKey(autoGenerate = true) val id: Int = 0,
//    val title: String,
//    val description: String,
//    val date: String,
//    val locationName: String
//)

@Entity(tableName = "cycling_events")
data class CyclingEventEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val date: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val createdBy: String,
    val participantsCount: Int
)

