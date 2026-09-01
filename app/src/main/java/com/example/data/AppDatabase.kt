package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PresetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "equalizer_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPresets(database.presetDao())
                    }
                }
            }
        }

        suspend fun populateInitialPresets(dao: PresetDao) {
            if (dao.getCount() > 0) return

            val factoryPresets = listOf(
                PresetEntity(name = "Flat", bandGains = "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", bassBoost = 0f, masterGain = 0f, virtualizer = 0f, stereoWidening = 0f, isCustom = false),
                PresetEntity(name = "Rock", bandGains = "5.5,4.0,3.0,0.5,-2.0,-1.5,1.0,3.5,5.0,6.0,6.5,7.0", bassBoost = 45f, masterGain = 1.0f, virtualizer = 25f, stereoWidening = 35f, isCustom = false),
                PresetEntity(name = "Pop", bandGains = "2.0,3.5,4.5,2.0,0.0,2.5,4.0,4.5,3.5,3.0,4.0,4.5", bassBoost = 30f, masterGain = 0.5f, virtualizer = 20f, stereoWidening = 25f, isCustom = false),
                PresetEntity(name = "Jazz", bandGains = "3.5,4.0,2.5,1.0,1.5,2.0,2.5,3.0,4.0,4.5,5.0,5.5", bassBoost = 25f, masterGain = 0f, virtualizer = 30f, stereoWidening = 40f, isCustom = false),
                PresetEntity(name = "Classical", bandGains = "4.0,3.0,2.0,1.0,0.0,0.0,1.5,2.5,3.5,4.5,5.5,6.0", bassBoost = 20f, masterGain = 0f, virtualizer = 40f, stereoWidening = 55f, isCustom = false),
                PresetEntity(name = "Bass Boost", bandGains = "7.5,6.0,4.5,2.5,0.0,0.0,0.0,1.0,2.0,3.0,3.5,4.0", bassBoost = 65f, masterGain = 1.5f, virtualizer = 15f, stereoWidening = 20f, isCustom = false),
                PresetEntity(name = "Bass Heavy", bandGains = "12.0,10.5,8.0,4.0,1.0,-1.0,-1.0,0.0,1.5,2.5,3.0,3.0", bassBoost = 85f, masterGain = 2.0f, virtualizer = 20f, stereoWidening = 15f, isCustom = false),
                PresetEntity(name = "EDM / Dance", bandGains = "9.0,8.0,6.0,2.0,-1.0,0.0,2.5,4.5,7.0,8.5,9.0,8.5", bassBoost = 75f, masterGain = 2.0f, virtualizer = 35f, stereoWidening = 45f, isCustom = false),
                PresetEntity(name = "Hip-Hop", bandGains = "10.0,9.5,7.0,3.0,0.0,1.0,2.0,2.5,4.0,6.0,6.5,7.0", bassBoost = 70f, masterGain = 1.5f, virtualizer = 20f, stereoWidening = 25f, isCustom = false),
                PresetEntity(name = "Acoustic", bandGains = "3.5,4.0,3.0,1.5,2.0,2.5,3.0,4.0,5.5,6.0,6.5,6.0", bassBoost = 20f, masterGain = 0f, virtualizer = 25f, stereoWidening = 35f, isCustom = false),
                PresetEntity(name = "Vocal Boost", bandGains = "-3.0,-2.0,-1.0,1.0,3.5,6.0,7.0,5.5,3.5,2.0,1.0,0.0", bassBoost = 10f, masterGain = 1.0f, virtualizer = 15f, stereoWidening = 20f, isCustom = false),
                PresetEntity(name = "Metal", bandGains = "7.0,6.0,4.0,0.0,-3.5,-3.0,1.5,4.5,7.0,8.5,9.0,9.5", bassBoost = 50f, masterGain = 2.0f, virtualizer = 30f, stereoWidening = 30f, isCustom = false),
                PresetEntity(name = "Treble Boost", bandGains = "-3.0,-2.5,-2.0,-1.0,0.0,1.0,3.0,5.5,7.5,9.5,11.0,12.0", bassBoost = 0f, masterGain = 0f, virtualizer = 15f, stereoWidening = 20f, isCustom = false),
                PresetEntity(name = "Electronic", bandGains = "8.0,7.0,4.5,1.0,0.0,2.0,3.5,5.0,6.5,8.0,9.0,8.0", bassBoost = 60f, masterGain = 1.5f, virtualizer = 40f, stereoWidening = 50f, isCustom = false),
                PresetEntity(name = "Studio Clean", bandGains = "1.0,1.0,0.5,0.0,0.0,0.5,1.0,1.5,2.0,2.5,2.5,2.0", bassBoost = 15f, masterGain = 0f, virtualizer = 10f, stereoWidening = 15f, isCustom = false)
            )

            dao.insertAll(factoryPresets)
        }
    }
}
