package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LinkDao {
    @Insert
    suspend fun insert(link: LinkEntity)

    @Query("SELECT * FROM links ORDER BY timestamp DESC")
    suspend fun getAll(): List<LinkEntity>
}
