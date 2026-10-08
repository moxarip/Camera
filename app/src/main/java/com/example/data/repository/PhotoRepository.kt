package com.example.data.repository

import android.content.Context
import com.example.data.db.PhotoDatabase
import com.example.data.model.CaptureMode
import com.example.data.model.PhotoItem
import com.example.data.model.SceneType
import com.example.data.model.SyncDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PhotoRepository(context: Context) {
    private val database = PhotoDatabase.getInstance(context)
    private val photoDao = database.photoDao()
    private val syncDao = database.syncDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val allPhotos: Flow<List<PhotoItem>> = photoDao.getAllPhotos()
    val rawPhotos: Flow<List<PhotoItem>> = photoDao.getRawPhotos()
    val syncedPhotos: Flow<List<PhotoItem>> = photoDao.getSyncedPhotos()
    val allDevices: Flow<List<SyncDevice>> = syncDao.getAllDevices()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgressPercent = MutableStateFlow(100)
    val syncProgressPercent: StateFlow<Int> = _syncProgressPercent.asStateFlow()

    init {
        coroutineScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val count = database.openHelper.readableDatabase.compileStatement("SELECT COUNT(*) FROM photos").simpleQueryForLong()
        if (count == 0L) {
            val now = System.currentTimeMillis()
            val samplePhotos = listOf(
                PhotoItem(
                    id = "sample_raw_1",
                    title = "AURA_20261007_RAW_001.DNG",
                    filePath = "mock://sample_raw_1",
                    thumbnailUri = "mock://sample_raw_1",
                    timestamp = now - 1000 * 60 * 15,
                    mode = CaptureMode.PRO.name,
                    isRaw = true,
                    rawExtension = "DNG 16-bit",
                    sceneTag = SceneType.SUNSET.name,
                    iso = 64,
                    shutterSpeed = "1/500s",
                    aperture = "f/1.6",
                    focalLength = "28mm",
                    resolution = "8064 x 6048 (48MP RAW)",
                    fileSizeFormatted = "48.2 MB",
                    isSynced = true,
                    syncProgress = 1f,
                    detectedObjectsSummary = "غروب شمس، سماء ذهبية، جبال"
                ),
                PhotoItem(
                    id = "sample_night_2",
                    title = "AURA_20261007_NIGHT_002.JPG",
                    filePath = "mock://sample_night_2",
                    thumbnailUri = "mock://sample_night_2",
                    timestamp = now - 1000 * 60 * 45,
                    mode = CaptureMode.NIGHT.name,
                    isRaw = false,
                    rawExtension = "JPG",
                    sceneTag = SceneType.NIGHT.name,
                    iso = 1250,
                    shutterSpeed = "2.5s (Multi-Stack)",
                    aperture = "f/1.8",
                    focalLength = "24mm",
                    resolution = "4032 x 3024 (12MP)",
                    fileSizeFormatted = "4.8 MB",
                    isSynced = true,
                    syncProgress = 1f,
                    detectedObjectsSummary = "أضواء المدينة، سماء ليلية، مباني"
                ),
                PhotoItem(
                    id = "sample_portrait_3",
                    title = "AURA_20261007_PORTRAIT_003.JPG",
                    filePath = "mock://sample_portrait_3",
                    thumbnailUri = "mock://sample_portrait_3",
                    timestamp = now - 1000 * 60 * 120,
                    mode = CaptureMode.PORTRAIT.name,
                    isRaw = false,
                    rawExtension = "JPG",
                    sceneTag = SceneType.PORTRAIT.name,
                    iso = 100,
                    shutterSpeed = "1/320s",
                    aperture = "f/1.4 (AI Bokeh)",
                    focalLength = "50mm eq",
                    resolution = "4032 x 3024 (12MP)",
                    fileSizeFormatted = "3.9 MB",
                    isSynced = false,
                    syncProgress = 0f,
                    detectedObjectsSummary = "شخص (98%)، وضعية أنيقة، إضاءة استوديو"
                ),
                PhotoItem(
                    id = "sample_hdr_4",
                    title = "AURA_20261007_HDR_004.JPG",
                    filePath = "mock://sample_hdr_4",
                    thumbnailUri = "mock://sample_hdr_4",
                    timestamp = now - 1000 * 60 * 360,
                    mode = CaptureMode.HDR_MERGE.name,
                    isRaw = true,
                    rawExtension = "DNG + HDR",
                    sceneTag = SceneType.LANDSCAPE.name,
                    iso = 50,
                    shutterSpeed = "Bracket (3 إطارات)",
                    aperture = "f/2.2",
                    focalLength = "13mm Ultra-Wide",
                    resolution = "4032 x 3024 (12MP)",
                    fileSizeFormatted = "22.5 MB",
                    isSynced = true,
                    syncProgress = 1f,
                    detectedObjectsSummary = "طبيعة خضراء، بحيرة، غيوم متباينة"
                )
            )

            samplePhotos.forEach { photoDao.insertPhoto(it) }

            val sampleDevices = listOf(
                SyncDevice(
                    id = "dev_current",
                    deviceName = "هذا الهاتف (AuraCam Client)",
                    deviceType = "phone",
                    lastSyncFormatted = "متزامن الآن",
                    isCurrentDevice = true,
                    status = "Online"
                ),
                SyncDevice(
                    id = "dev_tablet",
                    deviceName = "Galaxy Tab S9 Ultra",
                    deviceType = "tablet",
                    lastSyncFormatted = "منذ 15 دقيقة",
                    isCurrentDevice = false,
                    status = "Synced"
                ),
                SyncDevice(
                    id = "dev_desktop",
                    deviceName = "MacBook Pro 16\" Studio",
                    deviceType = "desktop",
                    lastSyncFormatted = "منذ ساعة",
                    isCurrentDevice = false,
                    status = "Synced"
                )
            )
            syncDao.insertDevices(sampleDevices)
        }
    }

    suspend fun savePhoto(
        mode: CaptureMode,
        isRaw: Boolean,
        scene: SceneType,
        isoVal: Int = 100,
        shutterVal: String = "1/250s",
        customTitle: String? = null,
        detectedSummary: String = "مشهد ذكي"
    ): PhotoItem {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val extension = if (isRaw) "DNG" else "JPG"
        val title = customTitle ?: "AURA_${timestamp}_${mode.name}.$extension"

        val photo = PhotoItem(
            id = id,
            title = title,
            filePath = "content://auracam/media/$id",
            thumbnailUri = "content://auracam/media/$id",
            timestamp = timestamp,
            mode = mode.name,
            isRaw = isRaw,
            rawExtension = if (isRaw) "DNG 16-bit Raw" else "JPG",
            sceneTag = scene.name,
            iso = isoVal,
            shutterSpeed = shutterVal,
            aperture = if (mode == CaptureMode.PORTRAIT) "f/1.4 (Bokeh)" else "f/1.8",
            focalLength = "24mm",
            resolution = if (isRaw) "8064 x 6048 (48MP)" else "4032 x 3024 (12MP)",
            fileSizeFormatted = if (isRaw) "36.8 MB" else "3.6 MB",
            isSynced = false,
            syncProgress = 0f,
            isVideo = mode == CaptureMode.CINEMATIC,
            videoDurationSec = if (mode == CaptureMode.CINEMATIC) 8 else 0,
            detectedObjectsSummary = detectedSummary
        )

        photoDao.insertPhoto(photo)
        return photo
    }

    suspend fun getPhotoById(id: String): PhotoItem? {
        return photoDao.getPhotoById(id)
    }

    suspend fun deletePhoto(id: String) {
        photoDao.deletePhotoById(id)
    }

    suspend fun updatePhoto(photo: PhotoItem) {
        photoDao.updatePhoto(photo)
    }

    fun triggerCloudSync() {
        if (_isSyncing.value) return
        coroutineScope.launch {
            _isSyncing.value = true
            for (progress in 10..100 step 15) {
                _syncProgressPercent.value = progress
                delay(200)
            }
            photoDao.markAllSynced()
            _isSyncing.value = false
            _syncProgressPercent.value = 100
        }
    }
}
