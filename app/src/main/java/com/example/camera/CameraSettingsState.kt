package com.example.camera

import com.example.data.model.BeautyFilterPreset
import com.example.data.model.CaptureMode
import com.example.data.model.DetectedObject
import com.example.data.model.PoseGuide
import com.example.data.model.SceneType

enum class FlashMode(val titleAr: String, val iconName: String) {
    OFF("إيقاف", "FlashOff"),
    AUTO("تلقائي", "FlashAuto"),
    ON("تشغيل", "FlashOn"),
    TORCH("إضاءة مستمرة", "Highlight")
}

enum class AspectRatioSetting(val label: String, val ratioFloat: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("FULL", 20f / 9f)
}

enum class GridSetting(val labelAr: String) {
    NONE("بدون شبكة"),
    RULE_OF_THIRDS("أثلاث 3x3"),
    GOLDEN_RATIO("النسبة الذهبية")
}

data class CameraSettingsState(
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val isRawEnabled: Boolean = false,
    val isHdrEnabled: Boolean = true,
    val flashMode: FlashMode = FlashMode.AUTO,
    val aspectRatio: AspectRatioSetting = AspectRatioSetting.RATIO_4_3,
    val zoomLevel: Float = 1.0f,
    val isFrontCamera: Boolean = false,
    val gridSetting: GridSetting = GridSetting.NONE,
    val timerSeconds: Int = 0,
    val detectedScene: SceneType = SceneType.AUTO,
    val detectedObjects: List<DetectedObject> = emptyList(),
    val lightingLevelLux: Float = 420f,
    val lightingAdviceAr: String = "إضاءة مثالية متوازنة في الوقت الفعلي",
    val activePoseGuide: PoseGuide? = null,
    val isPoseOverlayVisible: Boolean = false,
    val activeBeautyFilter: BeautyFilterPreset? = null,
    val beautySmoothing: Float = 0.5f,
    val oisStabilizationEnabled: Boolean = true,
    val motionTrackingEnabled: Boolean = true,
    val anamorphicLetterboxEnabled: Boolean = true,
    val isRecordingVideo: Boolean = false,
    val videoRecordingDurationSec: Int = 0,
    val audioPeakLevel: Float = 0.45f,
    // PRO Mode settings
    val proIso: Int = 100,
    val proShutter: String = "1/250s",
    val proEv: Float = 0.0f,
    val proWb: String = "AUTO",
    val proManualFocus: Float = 0.8f,
    // Processing / Stacking states
    val isProcessingHdrStack: Boolean = false,
    val isNightLongExposure: Boolean = false,
    val nightExposureRemainingSec: Int = 0,
    val horizonTiltDegrees: Float = 0.5f
)
