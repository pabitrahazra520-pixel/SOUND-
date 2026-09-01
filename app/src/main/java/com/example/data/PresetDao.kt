package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY isCustom ASC, id ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<PresetEntity>)

    @Update
    suspend fun updatePreset(preset: PresetEntity)

    @Query("DELETE FROM presets WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomPreset(id: Int)

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getCount(): Int
}
