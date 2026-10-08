package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.AspectRatioSetting
import com.example.camera.CameraSettingsState
import com.example.camera.FlashMode
import com.example.camera.GridSetting
import com.example.camera.PoseCatalog
import com.example.data.model.BeautyFilterPreset
import com.example.data.model.CaptureMode
import com.example.data.model.DetectedObject
import com.example.data.model.PhotoItem
import com.example.data.model.PoseGuide
import com.example.data.model.SceneType
import com.example.data.model.SyncDevice
import com.example.data.repository.PhotoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PhotoRepository(application)

    val allPhotos = repository.allPhotos
    val rawPhotos = repository.rawPhotos
    val syncedPhotos = repository.syncedPhotos
    val allDevices = repository.allDevices
    val isSyncing = repository.isSyncing
    val syncProgressPercent = repository.syncProgressPercent

    private val _cameraState = MutableStateFlow(CameraSettingsState())
    val cameraState: StateFlow<CameraSettingsState> = _cameraState.asStateFlow()

    private val _captureFlashEvent = MutableSharedFlow<Unit>()
    val captureFlashEvent: SharedFlow<Unit> = _captureFlashEvent.asSharedFlow()

    private val _recentCapturedPhoto = MutableStateFlow<PhotoItem?>(null)
    val recentCapturedPhoto: StateFlow<PhotoItem?> = _recentCapturedPhoto.asStateFlow()

    private var videoRecordingJob: Job? = null
    private var sceneSimulationJob: Job? = null

    init {
        startRealTimeSceneAnalysis()
    }

    private fun startRealTimeSceneAnalysis() {
        sceneSimulationJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                val currentMode = _cameraState.value.captureMode
                val newScene = when (currentMode) {
                    CaptureMode.PORTRAIT -> SceneType.PORTRAIT
                    CaptureMode.NIGHT -> SceneType.NIGHT
                    CaptureMode.CINEMATIC -> SceneType.ACTION
                    CaptureMode.HDR_MERGE -> SceneType.LANDSCAPE
                    CaptureMode.PRO -> SceneType.AUTO
                    CaptureMode.PHOTO -> {
                        // Alternate between common realistic scenes
                        val scenes = listOf(
                            SceneType.AUTO,
                            SceneType.PORTRAIT,
                            SceneType.LANDSCAPE,
                            SceneType.SUNSET,
                            SceneType.FOOD
                        )
                        scenes.random()
                    }
                }

                val detectedObjects = when (newScene) {
                    SceneType.PORTRAIT -> listOf(
                        DetectedObject("obj_face", "وجه وشخص", "Person Face", 0.97f, 0.28f, 0.25f, 0.44f, 0.50f)
                    )
                    SceneType.SUNSET -> listOf(
                        DetectedObject("obj_sun", "شمس وغروب", "Sunset Sky", 0.94f, 0.20f, 0.15f, 0.60f, 0.40f)
                    )
                    SceneType.FOOD -> listOf(
                        DetectedObject("obj_dish", "طبق طعام", "Gourmet Dish", 0.91f, 0.25f, 0.35f, 0.50f, 0.45f)
                    )
                    SceneType.LANDSCAPE -> listOf(
                        DetectedObject("obj_nature", "طبيعة وسماء", "Landscape", 0.88f, 0.10f, 0.20f, 0.80f, 0.60f)
                    )
                    else -> listOf(
                        DetectedObject("obj_main", "هدف رئيسي", "Subject", 0.92f, 0.30f, 0.30f, 0.40f, 0.40f)
                    )
                }

                val lux = when (newScene) {
                    SceneType.NIGHT -> 45f
                    SceneType.SUNSET -> 260f
                    else -> 550f
                }

                val advice = when {
                    lux < 80f -> "إضاءة منخفضة (45 Lux) - تم تجهيز الوضع الليلي وتقليل الضوضاء"
                    newScene == SceneType.SUNSET -> "إضاءة غروب درامية - تم تفعيل الحفاظ على تدرجات الضوء العالي HDR"
                    newScene == SceneType.PORTRAIT -> "إضاءة ناعمة على الوجه - عزل الخلفية AI Bokeh جاهز"
                    else -> "إضاءة مثالية متوازنة في الوقت الفعلي (550 Lux)"
                }

                _cameraState.update {
                    it.copy(
                        detectedScene = newScene,
                        detectedObjects = detectedObjects,
                        lightingLevelLux = lux,
                        lightingAdviceAr = advice
                    )
                }
            }
        }
    }

    fun setCaptureMode(mode: CaptureMode) {
        _cameraState.update { current ->
            current.copy(
                captureMode = mode,
                isPoseOverlayVisible = (mode == CaptureMode.PORTRAIT),
                activePoseGuide = if (mode == CaptureMode.PORTRAIT && current.activePoseGuide == null) {
                    PoseCatalog.poses.first()
                } else current.activePoseGuide,
                activeBeautyFilter = if (mode == CaptureMode.PORTRAIT && current.activeBeautyFilter == null) {
                    PoseCatalog.beautyFilters[1] // Glow filter
                } else current.activeBeautyFilter
            )
        }
    }

    fun toggleRawMode() {
        _cameraState.update { it.copy(isRawEnabled = !it.isRawEnabled) }
    }

    fun toggleHdr() {
        _cameraState.update { it.copy(isHdrEnabled = !it.isHdrEnabled) }
    }

    fun cycleFlashMode() {
        val next = when (_cameraState.value.flashMode) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
        _cameraState.update { it.copy(flashMode = next) }
    }

    fun setZoomLevel(zoom: Float) {
        _cameraState.update { it.copy(zoomLevel = zoom) }
    }

    fun toggleCameraLens() {
        _cameraState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun setAspectRatio(ratio: AspectRatioSetting) {
        _cameraState.update { it.copy(aspectRatio = ratio) }
    }

    fun cycleGrid() {
        val next = when (_cameraState.value.gridSetting) {
            GridSetting.NONE -> GridSetting.RULE_OF_THIRDS
            GridSetting.RULE_OF_THIRDS -> GridSetting.GOLDEN_RATIO
            GridSetting.GOLDEN_RATIO -> GridSetting.NONE
        }
        _cameraState.update { it.copy(gridSetting = next) }
    }

    fun setTimerSeconds(seconds: Int) {
        _cameraState.update { it.copy(timerSeconds = seconds) }
    }

    fun selectPoseGuide(pose: PoseGuide?) {
        _cameraState.update {
            it.copy(
                activePoseGuide = pose,
                isPoseOverlayVisible = (pose != null)
            )
        }
    }

    fun selectBeautyFilter(filter: BeautyFilterPreset?) {
        _cameraState.update { it.copy(activeBeautyFilter = filter) }
    }

    fun setBeautySmoothing(smoothing: Float) {
        _cameraState.update { it.copy(beautySmoothing = smoothing) }
    }

    fun toggleOisStabilization() {
        _cameraState.update { it.copy(oisStabilizationEnabled = !it.oisStabilizationEnabled) }
    }

    fun toggleMotionTracking() {
        _cameraState.update { it.copy(motionTrackingEnabled = !it.motionTrackingEnabled) }
    }

    fun toggleAnamorphicBars() {
        _cameraState.update { it.copy(anamorphicLetterboxEnabled = !it.anamorphicLetterboxEnabled) }
    }

    fun setProIso(iso: Int) {
        _cameraState.update { it.copy(proIso = iso) }
    }

    fun setProShutter(shutter: String) {
        _cameraState.update { it.copy(proShutter = shutter) }
    }

    fun setProEv(ev: Float) {
        _cameraState.update { it.copy(proEv = ev) }
    }

    fun setProWb(wb: String) {
        _cameraState.update { it.copy(proWb = wb) }
    }

    fun triggerShutter() {
        val state = _cameraState.value
        viewModelScope.launch {
            if (state.timerSeconds > 0) {
                delay(state.timerSeconds * 1000L)
            }

            when (state.captureMode) {
                CaptureMode.CINEMATIC -> {
                    // Video recording toggle
                    if (state.isRecordingVideo) {
                        stopVideoRecording()
                    } else {
                        startVideoRecording()
                    }
                }
                CaptureMode.HDR_MERGE -> {
                    // Simulate multi-bracket capture & alignment
                    _cameraState.update { it.copy(isProcessingHdrStack = true) }
                    _captureFlashEvent.emit(Unit)
                    delay(400)
                    _captureFlashEvent.emit(Unit)
                    delay(400)
                    _captureFlashEvent.emit(Unit)
                    delay(800)
                    _cameraState.update { it.copy(isProcessingHdrStack = false) }
                    saveCapturedPhoto()
                }
                CaptureMode.NIGHT -> {
                    // Multi-frame Night Sight long exposure countdown
                    _cameraState.update {
                        it.copy(isNightLongExposure = true, nightExposureRemainingSec = 3)
                    }
                    _captureFlashEvent.emit(Unit)
                    for (sec in 3 downTo 1) {
                        _cameraState.update { it.copy(nightExposureRemainingSec = sec) }
                        delay(1000)
                    }
                    _cameraState.update {
                        it.copy(isNightLongExposure = false, nightExposureRemainingSec = 0)
                    }
                    saveCapturedPhoto()
                }
                else -> {
                    // Standard / Portrait / PRO single shot
                    _captureFlashEvent.emit(Unit)
                    saveCapturedPhoto()
                }
            }
        }
    }

    private fun startVideoRecording() {
        _cameraState.update { it.copy(isRecordingVideo = true, videoRecordingDurationSec = 0) }
        videoRecordingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _cameraState.update {
                    it.copy(
                        videoRecordingDurationSec = it.videoRecordingDurationSec + 1,
                        audioPeakLevel = 0.3f + Random.nextFloat() * 0.5f
                    )
                }
            }
        }
    }

    private fun stopVideoRecording() {
        videoRecordingJob?.cancel()
        videoRecordingJob = null
        val duration = _cameraState.value.videoRecordingDurationSec
        _cameraState.update { it.copy(isRecordingVideo = false, videoRecordingDurationSec = 0) }

        viewModelScope.launch {
            val saved = repository.savePhoto(
                mode = CaptureMode.CINEMATIC,
                isRaw = false,
                scene = SceneType.ACTION,
                customTitle = "AURA_CINEMATIC_${System.currentTimeMillis()}.MP4",
                detectedSummary = "فيديو سينمائي 4K 24fps مع ثبات بصري وتتبع حركة"
            )
            _recentCapturedPhoto.value = saved
        }
    }

    private suspend fun saveCapturedPhoto() {
        val state = _cameraState.value
        val summary = state.detectedObjects.joinToString("، ") { "${it.labelAr} (${(it.confidence * 100).toInt()}%)" }
            .ifEmpty { "مشهد ${state.detectedScene.titleAr}" }

        val saved = repository.savePhoto(
            mode = state.captureMode,
            isRaw = state.isRawEnabled,
            scene = state.detectedScene,
            isoVal = if (state.captureMode == CaptureMode.PRO) state.proIso else 100,
            shutterVal = if (state.captureMode == CaptureMode.PRO) state.proShutter else "1/250s",
            detectedSummary = summary
        )
        _recentCapturedPhoto.value = saved
    }

    fun triggerCloudSync() {
        repository.triggerCloudSync()
    }

    fun deletePhoto(id: String) {
        viewModelScope.launch {
            repository.deletePhoto(id)
        }
    }

    fun updatePhoto(photo: PhotoItem) {
        viewModelScope.launch {
            repository.updatePhoto(photo)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sceneSimulationJob?.cancel()
        videoRecordingJob?.cancel()
    }
}
