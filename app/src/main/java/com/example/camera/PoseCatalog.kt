package com.example.camera

import com.example.data.model.BeautyFilterPreset
import com.example.data.model.PoseGuide

object PoseCatalog {
    val poses = listOf(
        PoseGuide(
            id = "pose_chin_rest",
            titleAr = "سيلفي يد على الذقن",
            category = "سيلفي",
            instructionAr = "ارفع الذقن 5 درجات وضع يدك برقة أسفل الفك لخلق ظل طبيعي جذاب",
            wireframeType = "chin_rest"
        ),
        PoseGuide(
            id = "pose_candid_side",
            titleAr = "نظرة جانبية عفوية (Candid)",
            category = "بورتريه",
            instructionAr = "انظر بزاوية 45 درجة بعيداً عن العدسة مع ابتسامة خفيفة وإرخاء الكتفين",
            wireframeType = "candid_side"
        ),
        PoseGuide(
            id = "pose_casual_stand",
            titleAr = "وقفة أنيقة متوازنة",
            category = "أزياء ومظهر",
            instructionAr = "ضع وزناً أكبر على إحدى القدمين، مع وضع يد في الجيب والأخرى مرتاحة",
            wireframeType = "casual_stand"
        ),
        PoseGuide(
            id = "pose_over_shoulder",
            titleAr = "نظرة من فوق الكتف",
            category = "بورتريه",
            instructionAr = "أدر ظهرك قليلاً نحو الكاميرا والتفت برأسك للخلف لنظرة سينمائية درامية",
            wireframeType = "over_shoulder"
        ),
        PoseGuide(
            id = "pose_golden_silhouette",
            titleAr = "ظل الغروب الذهبي",
            category = "غروب وإضاءة",
            instructionAr = "قف مباشرة بمواجهة مصدر الضوء للحصول على حدود ساطعة لشعرك وملامحك",
            wireframeType = "golden_silhouette"
        ),
        PoseGuide(
            id = "pose_couple_smile",
            titleAr = "وضعية ثنائي متناغمة",
            category = "ثنائي",
            instructionAr = "تقارب مع ميلان الرأسين بلطف نحو المركز لالتقاط لقطة مليئة بالدفء",
            wireframeType = "couple_smile"
        )
    )

    val beautyFilters = listOf(
        BeautyFilterPreset(
            id = "filter_none",
            nameAr = "طبيعي أصلي",
            nameEn = "Original",
            smoothing = 0f,
            toneWarmth = 0f,
            brightnessBoost = 0f
        ),
        BeautyFilterPreset(
            id = "filter_glow",
            nameAr = "إشراقة حريرية",
            nameEn = "Silk Glow",
            smoothing = 0.55f,
            toneWarmth = 0.15f,
            brightnessBoost = 0.12f
        ),
        BeautyFilterPreset(
            id = "filter_studio",
            nameAr = "إضاءة استوديو ناعمة",
            nameEn = "Soft Studio",
            smoothing = 0.45f,
            toneWarmth = -0.05f,
            brightnessBoost = 0.20f
        ),
        BeautyFilterPreset(
            id = "filter_sunset_warm",
            nameAr = "دفء الغروب",
            nameEn = "Sunset Amber",
            smoothing = 0.40f,
            toneWarmth = 0.35f,
            brightnessBoost = 0.08f
        ),
        BeautyFilterPreset(
            id = "filter_porcelain",
            nameAr = "بورسلين باريسي",
            nameEn = "Porcelain",
            smoothing = 0.70f,
            toneWarmth = -0.10f,
            brightnessBoost = 0.25f
        ),
        BeautyFilterPreset(
            id = "filter_noir",
            nameAr = "أبيض وأسود فاخر",
            nameEn = "Classic Noir",
            smoothing = 0.35f,
            toneWarmth = 0f,
            brightnessBoost = -0.05f
        )
    )
}
