package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PhotoItem
import com.example.data.model.SyncDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE isRaw = 1 ORDER BY timestamp DESC")
    fun getRawPhotos(): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE mode = :mode ORDER BY timestamp DESC")
    fun getPhotosByMode(mode: String): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE isSynced = 1 ORDER BY timestamp DESC")
    fun getSyncedPhotos(): Flow<List<PhotoItem>>

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: String): PhotoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoItem)

    @Update
    suspend fun updatePhoto(photo: PhotoItem)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: String)

    @Query("UPDATE photos SET isSynced = :isSynced, syncProgress = 1.0 WHERE id = :id")
    suspend fun updateSyncStatus(id: String, isSynced: Boolean)

    @Query("UPDATE photos SET isSynced = 1, syncProgress = 1.0")
    suspend fun markAllSynced()

    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE isSynced = 1")
    fun getSyncedCount(): Flow<Int>
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_devices")
    fun getAllDevices(): Flow<List<SyncDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<SyncDevice>)

    @Update
    suspend fun updateDevice(device: SyncDevice)
}
