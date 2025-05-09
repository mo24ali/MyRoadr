package com.example.myroadr.DAO

import androidx.room.*
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.myroadr.Entity.UserProfile

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = :uid LIMIT 1")
    suspend fun getProfile(uid: String): UserProfile?
}
