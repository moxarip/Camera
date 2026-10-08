package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CaptureMode(val titleAr: String, val titleEn: String) {
    PHOTO("صورة", "Photo"),
    PORTRAIT("بورتريه", "Portrait"),
    NIGHT("ليلي فائق", "Night Sight"),
    CINEMATIC("سينمائي", "Cinematic"),
    HDR_MERGE("دمج HDR", "HDR Merge"),
    PRO("احترافي PRO", "PRO Mode")
}

enum class SceneType(val titleAr: String, val iconName: String, val adviceAr: String) {
    AUTO("ذكاء اصطناعي تلقائي", "AutoAwesome", "تحسين تلقائي لكافة الإعدادات"),
    PORTRAIT("بورتريه أشخاص", "Face", "تم تفعيل عزل الخلفية وتنعيم الملامح الطبيعي"),
    NIGHT("مشهد ليلي مظلم", "NightsStay", "إضاءة منخفضة - تم تفعيل تقليل الضوضاء متعدد الإطارات"),
    LANDSCAPE("طبيعة ومناظر", "Landscape", "زيادة التباين وتشبع الألوان الخضراء والزرقاء"),
    FOOD("طعام ومأكولات", "Restaurant", "تعزيز دفء الألوان وتفاصيل الأطعمة"),
    DOCUMENT("مستند ونصوص", "Description", "تحسين حدة النصوص وإزالة الظلال"),
    SUNSET("غروب وشروق", "WbTwilight", "إبراز التدرجات الذهبية والبرتقالية في السماء"),
    ACTION("حركة سريعة", "DirectionsRun", "سرعة غالق فائقة 1/1000s لتجميد الحركة"),
    MACRO("تصوير ماكرو دقيق", "FilterVintage", "تركيز فائق على المسافات القريبة جداً")
}

data class DetectedObject(
    val id: String,
    val labelAr: String,
    val labelEn: String,
    val confidence: Float,
    val normalizedX: Float,
    val normalizedY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float
)

data class PoseGuide(
    val id: String,
    val titleAr: String,
    val category: String,
    val instructionAr: String,
    val wireframeType: String // e.g. "head_tilt", "profile", "casual_stand", "chin_rest", "couple", "over_shoulder"
)

data class BeautyFilterPreset(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val smoothing: Float, // 0..1
    val toneWarmth: Float, // -1..1
    val brightnessBoost: Float // 0..1
)

@Entity(tableName = "photos")
data class PhotoItem(
    @PrimaryKey val id: String,
    val title: String,
    val filePath: String,
    val thumbnailUri: String,
    val timestamp: Long,
    val mode: String,
    val isRaw: Boolean = false,
    val rawExtension: String = "DNG",
    val sceneTag: String = "AUTO",
    val iso: Int = 100,
    val shutterSpeed: String = "1/250s",
    val aperture: String = "f/1.8",
    val focalLength: String = "24mm",
    val resolution: String = "4032 x 3024 (12MP)",
    val fileSizeFormatted: String = "3.4 MB",
    val isSynced: Boolean = false,
    val syncProgress: Float = 0f,
    val isVideo: Boolean = false,
    val videoDurationSec: Int = 0,
    val detectedObjectsSummary: String = "مشهد عام"
)

@Entity(tableName = "sync_devices")
data class SyncDevice(
    @PrimaryKey val id: String,
    val deviceName: String,
    val deviceType: String, // "phone", "tablet", "desktop"
    val lastSyncFormatted: String,
    val isCurrentDevice: Boolean,
    val status: String // "Online", "Synced", "Syncing"
)
