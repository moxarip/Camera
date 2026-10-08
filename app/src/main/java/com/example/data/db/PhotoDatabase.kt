package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PhotoItem
import com.example.data.model.SyncDevice

@Database(
    entities = [PhotoItem::class, SyncDevice::class],
    version = 1,
    exportSchema = false
)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun syncDao(): SyncDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoDatabase? = null

        fun getInstance(context: Context): PhotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoDatabase::class.java,
                    "auracam_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
