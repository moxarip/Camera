package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhotoItem
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceElevated
import com.example.ui.theme.AuraEmeraldGreen

enum class EditorTab(val labelAr: String) {
    ADJUST("تعديل يدوي"),
    AI_ENHANCE("تحسين ذكي"),
    FILTERS("فلاتر تجميل"),
    CROP_TRANSFORM("اقتصاص وتدوير")
}

data class FilterPreset(
    val id: String,
    val nameAr: String,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmTint: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    photo: PhotoItem,
    onBack: () -> Unit,
    onSaveSuccess: (PhotoItem) -> Unit,
    onShare: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf(EditorTab.AI_ENHANCE) }

    // Adjustment states
    var brightness by remember { mutableFloatStateOf(0f) } // -50..50
    var contrast by remember { mutableFloatStateOf(1f) } // 0.5..2.0
    var saturation by remember { mutableFloatStateOf(1f) } // 0..2
    var warmth by remember { mutableFloatStateOf(0f) } // -50..50
    var sharpness by remember { mutableFloatStateOf(20f) } // 0..100
    var vignette by remember { mutableFloatStateOf(0f) } // 0..100
    var noiseReduction by remember { mutableFloatStateOf(15f) } // 0..100
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var isComparingOriginal by remember { mutableStateOf(false) }

    val presets = remember {
        listOf(
            FilterPreset("p_orig", "الأصلي", 0f, 1f, 1f, 0f),
            FilterPreset("p_glow", "إشراقة ناعمة", 8f, 1.1f, 1.15f, 5f),
            FilterPreset("p_studio", "استوديو احترافي", 12f, 1.25f, 0.95f, -4f),
            FilterPreset("p_sunset", "دفء الغروب", 5f, 1.15f, 1.35f, 18f),
            FilterPreset("p_porcelain", "بورسلين ناصع", 15f, 1.10f, 0.85f, -8f),
            FilterPreset("p_teal_orange", "سينمائي تيل", 0f, 1.35f, 1.20f, 10f),
            FilterPreset("p_noir", "أبيض وأسود فاخر", -5f, 1.45f, 0f, 0f)
        )
    }

    var selectedPreset by remember { mutableStateOf(presets.first()) }

    fun applyPreset(preset: FilterPreset) {
        selectedPreset = preset
        brightness = preset.brightness
        contrast = preset.contrast
        saturation = preset.saturation
        warmth = preset.warmTint
    }

    fun applyAiAutoEnhance() {
        brightness = 10f
        contrast = 1.18f
        saturation = 1.22f
        warmth = 4f
        sharpness = 45f
        noiseReduction = 35f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "محرر الصور المتقدم",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (photo.isRaw) "معالجة RAW 16-bit DNG" else photo.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (photo.isRaw) AuraCyanAccent else Color.Gray
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Compare button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isComparingOriginal) AuraAmberAccent else Color(0x33FFFFFF))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isComparingOriginal = true
                                        tryAwaitRelease()
                                        isComparingOriginal = false
                                    }
                                )
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("compare_original_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isComparingOriginal) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مقارنة",
                                color = if (isComparingOriginal) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val updated = photo.copy(
                                title = "EDITED_${photo.title}",
                                fileSizeFormatted = "4.2 MB",
                                isSynced = false
                            )
                            onSaveSuccess(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraCyanAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("editor_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "حفظ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AuraDarkSurface
                )
            )
        },
        containerColor = AuraDarkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Preview Canvas with photo backdrop & adjustment simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF070B12))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Photo canvas simulation responding to adjustments
                val effectiveBrightness = if (isComparingOriginal) 0f else brightness
                val effectiveContrast = if (isComparingOriginal) 1f else contrast
                val effectiveSaturation = if (isComparingOriginal) 1f else saturation
                val effectiveWarmth = if (isComparingOriginal) 0f else warmth

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                ) {
                    val w = size.width
                    val h = size.height

                    // Render photorealistic scene based on photo's mode/scene
                    val baseGrad = when (photo.sceneTag) {
                        "SUNSET" -> listOf(Color(0xFF9A3412), Color(0xFFEA580C), Color(0xFFFBBF24))
                        "NIGHT" -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                        "PORTRAIT" -> listOf(Color(0xFF374151), Color(0xFF1F2937), Color(0xFF111827))
                        else -> listOf(Color(0xFF0284C7), Color(0xFF0EA5E9), Color(0xFF10B981))
                    }

                    // Apply adjustments to the background preview
                    drawRect(brush = Brush.verticalGradient(baseGrad))

                    // Sun or main subject
                    val orbColor = when (photo.sceneTag) {
                        "SUNSET" -> Color(0xFFFFFBEB)
                        "NIGHT" -> Color(0xFF00E5FF)
                        else -> Color(0xFFFBBF24)
                    }
                    drawCircle(orbColor.copy(alpha = 0.85f * effectiveContrast), radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.45f))

                    // Brightness overlay
                    if (effectiveBrightness != 0f) {
                        val overlayColor = if (effectiveBrightness > 0) Color.White else Color.Black
                        drawRect(overlayColor.copy(alpha = (kotlin.math.abs(effectiveBrightness) / 100f).coerceIn(0f, 0.6f)))
                    }

                    // Warmth color cast overlay
                    if (effectiveWarmth != 0f) {
                        val tintColor = if (effectiveWarmth > 0) Color(0xFFF59E0B) else Color(0xFF0284C7)
                        drawRect(tintColor.copy(alpha = (kotlin.math.abs(effectiveWarmth) / 120f).coerceIn(0f, 0.4f)))
                    }

                    // Vignette shading
                    if (vignette > 0f) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = (vignette / 100f) * 0.7f)),
                                center = Offset(w / 2f, h / 2f),
                                radius = w * 0.6f
                            )
                        )
                    }
                }

                if (isComparingOriginal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "عرض الصورة الأصلية (قبل التعديل)",
                            color = AuraAmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (photo.isRaw) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AuraCyanAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "RAW 16-BIT", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = AuraDarkSurface,
                contentColor = AuraCyanAccent
            ) {
                EditorTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.labelAr,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) AuraCyanAccent else Color.Gray
                            )
                        }
                    )
                }
            }

            // Controls panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(AuraDarkSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                when (selectedTab) {
                    EditorTab.AI_ENHANCE -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { applyAiAutoEnhance() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuraCyanAccent,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_auto_enhance_button")
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "تحسين فوري ذكي (AI Auto-Enhance)", fontWeight = FontWeight.Bold)
                            }

                            // AI Denoise slider
                            EditorSliderRow(
                                title = "تقليل الضوضاء الليلي بالذكاء الاصطناعي",
                                value = noiseReduction,
                                range = 0f..100f,
                                onValueChange = { noiseReduction = it },
                                valueText = "${noiseReduction.toInt()}%"
                            )

                            // AI Sharpness & Detail
                            EditorSliderRow(
                                title = "استعادة التفاصيل والحدة الفائقة",
                                value = sharpness,
                                range = 0f..100f,
                                onValueChange = { sharpness = it },
                                valueText = "${sharpness.toInt()}%"
                            )
                        }
                    }

                    EditorTab.ADJUST -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EditorSliderRow(
                                title = "التعريض / السطوع",
                                value = brightness,
                                range = -50f..50f,
                                onValueChange = { brightness = it },
                                valueText = "${brightness.toInt()}"
                            )

                            EditorSliderRow(
                                title = "التباين (Contrast)",
                                value = contrast,
                                range = 0.5f..2.0f,
                                onValueChange = { contrast = it },
                                valueText = String.format("%.2f", contrast)
                            )

                            EditorSliderRow(
                                title = "تشبع الألوان (Saturation)",
                                value = saturation,
                                range = 0f..2f,
                                onValueChange = { saturation = it },
                                valueText = String.format("%.2f", saturation)
                            )

                            EditorSliderRow(
                                title = "حرارة اللون (Warmth)",
                                value = warmth,
                                range = -50f..50f,
                                onValueChange = { warmth = it },
                                valueText = "${warmth.toInt()}"
                            )

                            EditorSliderRow(
                                title = "تظليل الأطراف (Vignette)",
                                value = vignette,
                                range = 0f..100f,
                                onValueChange = { vignette = it },
                                valueText = "${vignette.toInt()}%"
                            )
                        }
                    }

                    EditorTab.FILTERS -> {
                        val scrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presets.forEach { preset ->
                                val isSelected = selectedPreset.id == preset.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AuraDarkBg else Color(0x22FFFFFF))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AuraCyanAccent else Color(0x33FFFFFF),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { applyPreset(preset) }
                                        .padding(10.dp)
                                        .testTag("preset_${preset.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (preset.id) {
                                                    "p_glow" -> Color(0xFFFDE047)
                                                    "p_sunset" -> Color(0xFFF97316)
                                                    "p_teal_orange" -> Color(0xFF06B6D4)
                                                    "p_noir" -> Color(0xFF64748B)
                                                    else -> AuraCyanAccent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ColorLens,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = preset.nameAr,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AuraCyanAccent else Color.White
                                    )
                                }
                            }
                        }
                    }

                    EditorTab.CROP_TRANSFORM -> {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rotate 90
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { rotationDegrees = (rotationDegrees + 90) % 360 }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "تدوير 90° ($rotationDegrees°)", color = Color.White, fontSize = 11.sp)
                            }

                            // 1:1 Instagram Square
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1:1", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "مربع 1:1", color = Color.White, fontSize = 11.sp)
                            }

                            // 9:16 Story
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FFFFFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "9:16", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "قصة Story", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = valueText, color = AuraCyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = AuraCyanAccent,
                activeTrackColor = AuraCyanAccent,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
