package com.example.myroadr.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroadr.Entity.CyclingEventEntity

@Dao
interface CyclingEventDao {

    @Query("SELECT * FROM cycling_events")
    suspend fun getAll(): List<CyclingEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CyclingEventEntity)
}
