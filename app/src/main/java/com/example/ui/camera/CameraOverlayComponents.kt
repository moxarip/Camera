package com.example.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraSettingsState
import com.example.camera.GridSetting
import com.example.data.model.SceneType
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCoralRed
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraDarkSurfaceElevated
import com.example.ui.theme.AuraEmeraldGreen
import kotlin.math.abs

@Composable
fun SceneDetectionBadge(
    scene: SceneType,
    advice: String,
    lux: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xCC0B0E14))
            .border(1.dp, AuraCyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("scene_detection_badge"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val (icon, tint) = when (scene) {
                SceneType.NIGHT -> Icons.Default.NightsStay to AuraCyanAccent
                SceneType.PORTRAIT -> Icons.Default.Face to AuraAmberAccent
                SceneType.SUNSET -> Icons.Default.WbTwilight to AuraAmberAccent
                else -> Icons.Default.AutoAwesome to AuraCyanAccent
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = scene.titleAr,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "${lux.toInt()} Lux",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AuraCyanAccent,
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = advice,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
fun ObjectDetectionBoxes(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        state.detectedObjects.forEach { obj ->
            val left = totalWidth * obj.normalizedX
            val top = totalHeight * obj.normalizedY
            val boxWidth = totalWidth * obj.normalizedWidth
            val boxHeight = totalHeight * obj.normalizedHeight

            Box(
                modifier = Modifier
                    .offset { IntOffset(left.roundToPx(), top.roundToPx()) }
                    .size(boxWidth, boxHeight)
                    .border(1.5.dp, AuraCyanAccent.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .testTag("object_box_${obj.id}")
            ) {
                // Label tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (-20).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AuraCyanAccent.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = obj.labelAr,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(obj.confidence * 100).toInt()}%",
                        color = Color(0xFF0F172A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center crosshair
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val chLen = 12.dp.toPx()
                    drawLine(AuraCyanAccent, Offset(cx - chLen, cy), Offset(cx + chLen, cy), strokeWidth = 1.5f)
                    drawLine(AuraCyanAccent, Offset(cx, cy - chLen), Offset(cx, cy + chLen), strokeWidth = 1.5f)
                }
            }
        }
    }
}

@Composable
fun PoseGuideOverlay(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
    val pose = state.activePoseGuide ?: return

    Box(modifier = modifier.fillMaxSize()) {
        // Wireframe silhouette drawn on the viewfinder
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val stroke = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
            )

            val headCx = w * 0.5f
            val headCy = h * 0.35f
            val headRadius = w * 0.16f

            // Head circle
            drawCircle(
                color = AuraAmberAccent.copy(alpha = 0.85f),
                radius = headRadius,
                center = Offset(headCx, headCy),
                style = stroke
            )

            // Dynamic silhouette path according to pose wireframe
            val path = Path()
            when (pose.wireframeType) {
                "chin_rest" -> {
                    // Hand under chin guide
                    path.moveTo(headCx - headRadius * 0.8f, headCy + headRadius * 0.9f)
                    path.quadraticTo(
                        headCx, headCy + headRadius * 1.3f,
                        headCx + headRadius * 0.8f, headCy + headRadius * 0.9f
                    )
                    // Torso shoulders
                    path.moveTo(headCx - headRadius * 1.8f, headCy + headRadius * 2.2f)
                    path.quadraticTo(
                        headCx, headCy + headRadius * 1.5f,
                        headCx + headRadius * 1.8f, headCy + headRadius * 2.2f
                    )
                }
                "candid_side" -> {
                    // 45 degree tilt outline
                    path.moveTo(headCx - headRadius * 1.5f, headCy + headRadius * 2.5f)
                    path.lineTo(headCx - headRadius * 0.6f, headCy + headRadius * 1.2f)
                    path.lineTo(headCx + headRadius * 1.8f, headCy + headRadius * 2.3f)
                }
                "couple_smile" -> {
                    // Second head circle for couple pose
                    drawCircle(
                        color = AuraCyanAccent.copy(alpha = 0.85f),
                        radius = headRadius * 0.9f,
                        center = Offset(w * 0.68f, headCy + 20f),
                        style = stroke
                    )
                }
                else -> {
                    // Standard shoulders and body curve
                    path.moveTo(headCx - headRadius * 1.8f, headCy + headRadius * 2.2f)
                    path.quadraticTo(
                        headCx, headCy + headRadius * 1.4f,
                        headCx + headRadius * 1.8f, headCy + headRadius * 2.2f
                    )
                }
            }

            drawPath(path, color = AuraAmberAccent.copy(alpha = 0.85f), style = stroke)
        }

        // Coaching prompt banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xDD0F172A))
                .border(1.5.dp, AuraAmberAccent, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("pose_coaching_banner"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AuraAmberAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "مساعد الوضعيات الذكي: ${pose.titleAr}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = AuraAmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AuraEmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "زاوية مثالية 96%",
                        color = AuraEmeraldGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = pose.instructionAr,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun CinematicVideoOverlay(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Anamorphic 2.39:1 letterbox bars
        if (state.anamorphicLetterboxEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Black)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black)
            )
        }

        // Top info bar in cinematic mode
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (state.anamorphicLetterboxEnabled) 54.dp else 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xAA000000))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.isRecordingVideo) AuraCoralRed else AuraEmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isRecordingVideo) {
                        val m = state.videoRecordingDurationSec / 60
                        val s = state.videoRecordingDurationSec % 60
                        String.format("%02d:%02d REC", m, s)
                    } else "4K 24FPS CINEMATIC",
                    color = if (state.isRecordingVideo) AuraCoralRed else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // OIS badge
            Text(
                text = if (state.oisStabilizationEnabled) "OIS ثبات فائق" else "OIS إيقاف",
                color = if (state.oisStabilizationEnabled) AuraCyanAccent else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Audio level VU meter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = AuraEmeraldGreen,
                    modifier = Modifier.size(14.dp)
                )
                val bars = 5
                val activeBars = (state.audioPeakLevel * bars).toInt().coerceIn(1, bars)
                for (i in 1..bars) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((6 + i * 2).dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (i <= activeBars) {
                                    if (i >= 4) AuraCoralRed else AuraEmeraldGreen
                                } else Color(0x44FFFFFF)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun GridOverlay(
    gridSetting: GridSetting,
    modifier: Modifier = Modifier
) {
    if (gridSetting == GridSetting.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val lineColor = Color(0x44FFFFFF)

        when (gridSetting) {
            GridSetting.RULE_OF_THIRDS -> {
                // Vertical lines
                drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f)
                drawLine(lineColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 1f)
                // Horizontal lines
                drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f)
                drawLine(lineColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 1f)
            }
            GridSetting.GOLDEN_RATIO -> {
                val phi = 0.618f
                val invPhi = 1f - phi
                drawLine(lineColor, Offset(w * invPhi, 0f), Offset(w * invPhi, h), strokeWidth = 1f)
                drawLine(lineColor, Offset(w * phi, 0f), Offset(w * phi, h), strokeWidth = 1f)
                drawLine(lineColor, Offset(0f, h * invPhi), Offset(w, h * invPhi), strokeWidth = 1f)
                drawLine(lineColor, Offset(0f, h * phi), Offset(w, h * phi), strokeWidth = 1f)
            }
            else -> {}
        }
    }
}

@Composable
fun HorizonLevelIndicator(
    tiltDegrees: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(tiltDegrees) < 1.0f
    val color = if (isLevel) AuraEmeraldGreen else AuraCoralRed

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x88000000))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(24.dp, 12.dp)) {
            val cy = size.height / 2f
            drawLine(color, Offset(0f, cy), Offset(size.width, cy), strokeWidth = 2f)
            drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width / 2f, cy))
        }
        Text(
            text = if (isLevel) "مستوٍ 0°" else "${tiltDegrees.toInt()}°",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProcessingFeedbackOverlay(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isProcessingHdrStack || state.isNightLongExposure,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xEE0F172A))
                    .border(1.5.dp, AuraCyanAccent, RoundedCornerShape(24.dp))
                    .padding(28.dp)
            ) {
                if (state.isNightLongExposure) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (4 - state.nightExposureRemainingSec) / 3f },
                            modifier = Modifier.size(72.dp),
                            color = AuraAmberAccent,
                            trackColor = Color(0x33FFFFFF),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${state.nightExposureRemainingSec}s",
                            color = AuraAmberAccent,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "وضع التصوير الليلي المتقدم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "حافظ على ثبات يدك - جاري دمج التعريضات وتقليل الضوضاء",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    )
                } else if (state.isProcessingHdrStack) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = AuraCyanAccent,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "دمج HDR متعدد الإطارات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "محاذاة 3 إطارات للحصول على أعلى تفاصيل إضاءة وظلال ممكنة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
