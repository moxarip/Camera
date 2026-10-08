package com.example.ui.camera

import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.camera.CameraManager
import com.example.camera.CameraSettingsState
import com.example.data.model.CaptureMode
import com.example.data.model.SceneType
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCyanAccent
import kotlinx.coroutines.launch

@Composable
fun CameraPreviewContainer(
    state: CameraSettingsState,
    onTapFocus: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager(context) }
    var isHardwareActive by remember { mutableStateOf(false) }

    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusScale = remember { Animatable(1.5f) }
    val focusAlpha = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusPoint = offset
                    onTapFocus(offset)
                    coroutineScope.launch {
                        focusScale.snapTo(1.5f)
                        focusAlpha.snapTo(1f)
                        focusScale.animateTo(1f, tween(250))
                        focusAlpha.animateTo(0.3f, tween(1200))
                    }
                }
            }
            .testTag("camera_preview_container")
    ) {
        if (isHardwareActive) {
            // CameraX hardware preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    cameraManager.bindCameraToLifecycle(
                        lifecycleOwner,
                        previewView,
                        state.isFrontCamera
                    ) { bound ->
                        isHardwareActive = bound
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-fidelity Camera Simulator with dynamic scenes
            CameraSimulatorView(state = state)
        }

        // Tap to focus reticle
        focusPoint?.let { pt ->
            Canvas(
                modifier = Modifier
                    .offset { IntOffset((pt.x - 40.dp.toPx()).toInt(), (pt.y - 40.dp.toPx()).toInt()) }
                    .size(80.dp)
                    .scale(focusScale.value)
            ) {
                val alpha = focusAlpha.value
                val color = AuraAmberAccent.copy(alpha = alpha)
                drawCircle(color, radius = 36.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                // Reticle tick marks
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawLine(color, Offset(cx, cy - 36.dp.toPx()), Offset(cx, cy - 28.dp.toPx()), strokeWidth = 2f)
                drawLine(color, Offset(cx, cy + 28.dp.toPx()), Offset(cx, cy + 36.dp.toPx()), strokeWidth = 2f)
                drawLine(color, Offset(cx - 36.dp.toPx(), cy), Offset(cx - 28.dp.toPx(), cy), strokeWidth = 2f)
                drawLine(color, Offset(cx + 28.dp.toPx(), cy), Offset(cx + 36.dp.toPx(), cy), strokeWidth = 2f)
            }
        }
    }
}

@Composable
fun CameraSimulatorView(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
    // Dynamic simulated photography backdrop depending on scene and mode
    val zoomFactor = state.zoomLevel

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().scale(zoomFactor.coerceIn(0.8f, 2.5f))) {
            val w = size.width
            val h = size.height

            when (state.detectedScene) {
                SceneType.NIGHT -> {
                    // Deep night cityscape simulation with glowing neon lights
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF070B14), Color(0xFF111827), Color(0xFF1E293B))
                        )
                    )
                    // Neon city bokeh circles
                    drawCircle(Color(0x8800E5FF), radius = 45f, center = Offset(w * 0.25f, h * 0.45f))
                    drawCircle(Color(0x77F43F5E), radius = 60f, center = Offset(w * 0.70f, h * 0.50f))
                    drawCircle(Color(0x88FBBF24), radius = 40f, center = Offset(w * 0.45f, h * 0.40f))
                    drawCircle(Color(0x66A855F7), radius = 55f, center = Offset(w * 0.85f, h * 0.42f))
                }
                SceneType.SUNSET -> {
                    // Golden hour sunset sky gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF431407),
                                Color(0xFFC2410C),
                                Color(0xFFF97316),
                                Color(0xFFFBBF24),
                                Color(0xFF1E293B)
                            )
                        )
                    )
                    // Sun glowing orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0x00FBBF24))
                        ),
                        radius = w * 0.22f,
                        center = Offset(w * 0.5f, h * 0.42f)
                    )
                }
                SceneType.PORTRAIT -> {
                    // Studio portrait backdrop with soft lighting vignette
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
                            center = Offset(w * 0.5f, h * 0.4f),
                            radius = w * 0.8f
                        )
                    )
                    // Head & shoulders portrait silhouette
                    drawCircle(
                        color = Color(0x66475569),
                        radius = w * 0.22f,
                        center = Offset(w * 0.5f, h * 0.36f)
                    )
                }
                else -> {
                    // Modern architectural natural scene
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0284C7),
                                Color(0xFF38BDF8),
                                Color(0xFFBAE6FD),
                                Color(0xFF10B981),
                                Color(0xFF065F46)
                            )
                        )
                    )
                    // Soft clouds
                    drawCircle(Color(0x55FFFFFF), radius = 70f, center = Offset(w * 0.2f, h * 0.18f))
                    drawCircle(Color(0x66FFFFFF), radius = 90f, center = Offset(w * 0.35f, h * 0.16f))
                }
            }
        }

        // Lens status badge at top corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = 56.dp)
                .clip(CircleShape)
                .background(Color(0x88000000))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (state.isFrontCamera) "سيلفي أمامية AI" else "عدسة رئيسية 48MP",
                color = AuraCyanAccent,
                fontSize = 9.sp
            )
        }
    }
}
