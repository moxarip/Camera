package com.example.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.AspectRatioSetting
import com.example.camera.CameraSettingsState
import com.example.camera.FlashMode
import com.example.camera.PoseCatalog
import com.example.data.model.CaptureMode
import com.example.data.model.PhotoItem
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCoralRed
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceElevated
import com.example.ui.theme.AuraEmeraldGreen

@Composable
fun CameraTopBar(
    state: CameraSettingsState,
    isSyncing: Boolean,
    onCycleFlash: () -> Unit,
    onToggleRaw: () -> Unit,
    onToggleHdr: () -> Unit,
    onCycleGrid: () -> Unit,
    onCycleTimer: () -> Unit,
    onOpenCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash toggle
        IconButton(
            onClick = onCycleFlash,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .testTag("flash_toggle_button")
        ) {
            val (icon, color) = when (state.flashMode) {
                FlashMode.OFF -> Icons.Default.FlashOff to Color.White
                FlashMode.AUTO -> Icons.Default.FlashAuto to AuraAmberAccent
                FlashMode.ON -> Icons.Default.FlashOn to AuraAmberAccent
                FlashMode.TORCH -> Icons.Default.Highlight to AuraCyanAccent
            }
            Icon(
                imageVector = icon,
                contentDescription = "Flash ${state.flashMode.titleAr}",
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        // RAW Badge toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (state.isRawEnabled) AuraCyanAccent else Color(0x66000000))
                .border(
                    1.dp,
                    if (state.isRawEnabled) AuraCyanAccent else Color(0x66FFFFFF),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onToggleRaw() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("raw_toggle_button")
        ) {
            Text(
                text = "RAW",
                color = if (state.isRawEnabled) Color.Black else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // HDR toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (state.isHdrEnabled) AuraAmberAccent else Color(0x66000000))
                .border(
                    1.dp,
                    if (state.isHdrEnabled) AuraAmberAccent else Color(0x66FFFFFF),
                    RoundedCornerShape(8.dp)
                )
                .clickable { onToggleHdr() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("hdr_toggle_button")
        ) {
            Text(
                text = "HDR",
                color = if (state.isHdrEnabled) Color.Black else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Grid toggle
        IconButton(
            onClick = onCycleGrid,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .testTag("grid_toggle_button")
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Grid",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Timer toggle
        IconButton(
            onClick = onCycleTimer,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .testTag("timer_toggle_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = if (state.timerSeconds > 0) AuraAmberAccent else Color.White,
                    modifier = Modifier.size(18.dp)
                )
                if (state.timerSeconds > 0) {
                    Text(
                        text = "${state.timerSeconds}s",
                        color = AuraAmberAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Cloud sync status button
        IconButton(
            onClick = onOpenCloudSync,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x66000000))
                .testTag("cloud_sync_button")
        ) {
            Icon(
                imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                contentDescription = "Cloud Sync",
                tint = if (isSyncing) AuraCyanAccent else AuraEmeraldGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ZoomSelector(
    currentZoom: Float,
    onSelectZoom: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x77000000))
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("zoom_selector"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val zoomLevels = listOf(0.5f, 1.0f, 2.0f, 5.0f)
        zoomLevels.forEach { zoom ->
            val isSelected = currentZoom == zoom
            val label = if (zoom == 0.5f) ".5" else "${zoom.toInt()}"
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AuraCyanAccent else Color.Transparent)
                    .clickable { onSelectZoom(zoom) }
                    .testTag("zoom_btn_$label"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${label}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ModeCarousel(
    currentMode: CaptureMode,
    onSelectMode: (CaptureMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("camera_mode_carousel"),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaptureMode.entries.forEach { mode ->
            val isSelected = currentMode == mode
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSelectMode(mode) }
                    .padding(vertical = 4.dp)
                    .testTag("mode_tab_${mode.name}")
            ) {
                Text(
                    text = mode.titleAr,
                    color = if (isSelected) AuraAmberAccent else Color(0xFF94A3B8),
                    fontSize = if (isSelected) 14.sp else 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AuraAmberAccent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun ProControlsBar(
    state: CameraSettingsState,
    onIsoSelected: (Int) -> Unit,
    onShutterSelected: (String) -> Unit,
    onEvChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC090C10))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("pro_controls_bar")
    ) {
        // ISO options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ISO", color = AuraCyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val isoList = listOf(50, 100, 200, 400, 800, 1600, 3200)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                isoList.forEach { iso ->
                    val isSelected = state.proIso == iso
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AuraCyanAccent else Color(0x33FFFFFF))
                            .clickable { onIsoSelected(iso) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$iso",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Shutter speed options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "غالق S", color = AuraAmberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val shutterList = listOf("1/2000s", "1/500s", "1/250s", "1/60s", "1/15s", "1s")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                shutterList.forEach { s ->
                    val isSelected = state.proShutter == s
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AuraAmberAccent else Color(0x33FFFFFF))
                            .clickable { onShutterSelected(s) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = s,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PortraitPoseBar(
    state: CameraSettingsState,
    onSelectPose: (com.example.data.model.PoseGuide?) -> Unit,
    onSelectFilter: (com.example.data.model.BeautyFilterPreset?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC090C10))
            .padding(vertical = 6.dp)
            .testTag("portrait_pose_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pose guide chips
            PoseCatalog.poses.forEach { pose ->
                val isSelected = state.activePoseGuide?.id == pose.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) AuraAmberAccent else Color(0x33FFFFFF))
                        .clickable {
                            onSelectPose(if (isSelected) null else pose)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "وضعية: ${pose.titleAr}",
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CameraBottomBar(
    state: CameraSettingsState,
    recentPhoto: PhotoItem?,
    onShutterClick: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val shutterPressScale = remember { Animatable(1f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag("camera_bottom_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gallery Thumbnail button with live preview badge
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (recentPhoto != null) Color(0xFF0F172A) else Color(0x44FFFFFF))
                .border(
                    width = 2.dp,
                    color = if (recentPhoto != null) AuraCyanAccent else Color(0x88FFFFFF),
                    shape = CircleShape
                )
                .clickable { onOpenGallery() }
                .testTag("gallery_thumbnail_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "معرض الصور",
                tint = if (recentPhoto != null) AuraCyanAccent else Color.White,
                modifier = Modifier.size(26.dp)
            )

            if (recentPhoto != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(AuraEmeraldGreen)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }

        // Shutter Button with tactile animation
        val isVideo = state.captureMode == CaptureMode.CINEMATIC
        val isNight = state.captureMode == CaptureMode.NIGHT

        val transition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by transition.animateFloat(
            initialValue = 1f,
            targetValue = if (state.isRecordingVideo) 1.15f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(shutterPressScale.value)
                .border(
                    width = 4.dp,
                    color = when {
                        isVideo -> AuraCoralRed
                        isNight -> AuraAmberAccent
                        else -> Color.White
                    },
                    shape = CircleShape
                )
                .padding(6.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable {
                    coroutineScope.launch {
                        shutterPressScale.animateTo(0.85f, tween(80))
                        shutterPressScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 500f))
                    }
                    onShutterClick()
                }
                .testTag("shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (state.isRecordingVideo) 28.dp else 60.dp)
                    .scale(pulseScale)
                    .clip(
                        if (state.isRecordingVideo) RoundedCornerShape(6.dp)
                        else CircleShape
                    )
                    .background(
                        when {
                            isVideo -> AuraCoralRed
                            isNight -> AuraAmberAccent
                            state.captureMode == CaptureMode.PORTRAIT -> Color(0xFFFDE047)
                            else -> Color.White
                        }
                    )
            )
        }

        // Flip Camera button
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0x44FFFFFF))
                .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                .clickable { onFlipCamera() }
                .testTag("flip_camera_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "تبديل الكاميرا",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
