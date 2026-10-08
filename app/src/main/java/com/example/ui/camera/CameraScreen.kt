package com.example.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptureMode
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraEmeraldGreen
import com.example.ui.viewmodel.CameraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    cameraManager: com.example.camera.CameraManager,
    onRequestPermission: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraState by viewModel.cameraState.collectAsState()
    val recentPhoto by viewModel.recentCapturedPhoto.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // Shutter flash animation
    val shutterFlashAlpha = remember { Animatable(0f) }
    var showSavedNotification by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.captureFlashEvent.collectLatest {
            shutterFlashAlpha.snapTo(0.85f)
            shutterFlashAlpha.animateTo(0f, tween(150))
        }
    }

    LaunchedEffect(recentPhoto) {
        if (recentPhoto != null) {
            showSavedNotification = true
            delay(2500)
            showSavedNotification = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("camera_screen_root")
    ) {
        // Camera Viewfinder (Live CameraX or Simulator)
        CameraPreviewContainer(
            state = cameraState,
            cameraManager = cameraManager,
            onRequestPermission = onRequestPermission,
            onTapFocus = { /* Handled with reticle feedback */ },
            modifier = Modifier.fillMaxSize()
        )

        // Overlays Layer: Grid lines
        GridOverlay(
            gridSetting = cameraState.gridSetting,
            modifier = Modifier.fillMaxSize()
        )

        // Object Detection boxes
        ObjectDetectionBoxes(
            state = cameraState,
            modifier = Modifier.fillMaxSize()
        )

        // Pose Guide overlay when in Portrait mode
        if (cameraState.isPoseOverlayVisible && cameraState.captureMode == CaptureMode.PORTRAIT) {
            PoseGuideOverlay(
                state = cameraState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Cinematic video overlay
        if (cameraState.captureMode == CaptureMode.CINEMATIC) {
            CinematicVideoOverlay(
                state = cameraState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Scene detection badge & horizon level
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SceneDetectionBadge(
                scene = cameraState.detectedScene,
                advice = cameraState.lightingAdviceAr,
                lux = cameraState.lightingLevelLux
            )

            Spacer(modifier = Modifier.height(8.dp))

            HorizonLevelIndicator(
                tiltDegrees = cameraState.horizonTiltDegrees
            )
        }

        // Top Control Bar
        CameraTopBar(
            state = cameraState,
            isSyncing = isSyncing,
            onCycleFlash = { viewModel.cycleFlashMode() },
            onToggleRaw = { viewModel.toggleRawMode() },
            onToggleHdr = { viewModel.toggleHdr() },
            onCycleGrid = { viewModel.cycleGrid() },
            onCycleTimer = {
                val next = when (cameraState.timerSeconds) {
                    0 -> 3
                    3 -> 10
                    else -> 0
                }
                viewModel.setTimerSeconds(next)
            },
            onOpenCloudSync = onNavigateToCloudSync,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Animated Capture Saved Banner
        AnimatedVisibility(
            visible = showSavedNotification,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE0F172A))
                    .border(1.5.dp, AuraEmeraldGreen, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("capture_saved_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AuraEmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "تم التقاط الصورة وحفظها بنجاح 📸",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Controls area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom Selector
            ZoomSelector(
                currentZoom = cameraState.zoomLevel,
                onSelectZoom = { viewModel.setZoomLevel(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Mode-specific parameter bar (PRO or Portrait Poses)
            if (cameraState.captureMode == CaptureMode.PRO) {
                ProControlsBar(
                    state = cameraState,
                    onIsoSelected = { viewModel.setProIso(it) },
                    onShutterSelected = { viewModel.setProShutter(it) },
                    onEvChanged = { viewModel.setProEv(it) }
                )
            } else if (cameraState.captureMode == CaptureMode.PORTRAIT) {
                PortraitPoseBar(
                    state = cameraState,
                    onSelectPose = { viewModel.selectPoseGuide(it) },
                    onSelectFilter = { viewModel.selectBeautyFilter(it) }
                )
            }

            // Mode carousel (Photo, Portrait, Night, Cinematic, HDR, PRO)
            ModeCarousel(
                currentMode = cameraState.captureMode,
                onSelectMode = { viewModel.setCaptureMode(it) }
            )

            // Shutter & Bottom buttons
            CameraBottomBar(
                state = cameraState,
                recentPhoto = recentPhoto,
                onShutterClick = { viewModel.triggerShutter(cameraManager) },
                onFlipCamera = { viewModel.toggleCameraLens() },
                onOpenGallery = onNavigateToGallery
            )
        }

        // Processing overlay for Multi-frame HDR & Night Stacking
        ProcessingFeedbackOverlay(
            state = cameraState,
            modifier = Modifier.fillMaxSize()
        )

        // Shutter white flash feedback
        if (shutterFlashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = shutterFlashAlpha.value))
            )
        }
    }
}
