package com.example.myroadr.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myroadr.Entity.CyclingEventEntity

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: CyclingEventEntity)

    @Query("SELECT * FROM cycling_events")
    suspend fun getAll(): List<CyclingEventEntity>
}
