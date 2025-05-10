package com.example.myroadr.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String, // same as Firebase UID
    val name: String,
    val email: String,
    val image: ByteArray? // nullable image
)
