package com.example.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun insert(preset: PresetEntity): Long = presetDao.insertPreset(preset)

    suspend fun update(preset: PresetEntity) = presetDao.updatePreset(preset)

    suspend fun deleteCustomPreset(id: Int) = presetDao.deleteCustomPreset(id)

    suspend fun ensureDefaultPresets() {
        AppDatabase.populateInitialPresets(presetDao)
    }
}
