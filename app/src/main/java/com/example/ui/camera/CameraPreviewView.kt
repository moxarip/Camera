package com.example.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.CameraManager
import com.example.camera.CameraSettingsState
import com.example.data.model.SceneType
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraEmeraldGreen
import kotlinx.coroutines.launch

@Composable
fun CameraPreviewContainer(
    state: CameraSettingsState,
    cameraManager: CameraManager,
    onRequestPermission: () -> Unit,
    onTapFocus: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isCameraBound by remember { mutableStateOf(false) }

    val hasCameraPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusScale = remember { Animatable(1.5f) }
    val focusAlpha = remember { Animatable(1f) }

    // Rebind camera when lens or permission changes
    LaunchedEffect(hasCameraPermission, state.isFrontCamera, previewViewRef) {
        val pView = previewViewRef
        if (hasCameraPermission && pView != null) {
            cameraManager.bindCameraToLifecycle(
                lifecycleOwner = lifecycleOwner,
                previewView = pView,
                isFrontCamera = state.isFrontCamera,
                flashMode = state.flashMode
            ) { bound ->
                isCameraBound = bound
            }
        }
    }

    // Update zoom on real camera
    LaunchedEffect(state.zoomLevel) {
        cameraManager.setZoom(state.zoomLevel)
    }

    // Update flash mode on real camera
    LaunchedEffect(state.flashMode) {
        cameraManager.setFlashMode(state.flashMode)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusPoint = offset
                    previewViewRef?.let { pView ->
                        cameraManager.focusOnPoint(pView, offset.x, offset.y)
                    }
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
        if (!hasCameraPermission) {
            // Permission Request Card
            CameraPermissionBanner(
                onRequestPermission = onRequestPermission,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // REAL CameraX AndroidView is ALWAYS mounted and active
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                update = { pView ->
                    previewViewRef = pView
                },
                modifier = Modifier.fillMaxSize()
            )

            // If hardware camera sensor is not bound (e.g. headless emulator), show the fallback visualizer
            if (!isCameraBound) {
                CameraSimulatorView(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                )
            }
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
                drawCircle(
                    color,
                    radius = 36.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                )
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawLine(color, Offset(cx, cy - 36.dp.toPx()), Offset(cx, cy - 28.dp.toPx()), strokeWidth = 2.5f)
                drawLine(color, Offset(cx, cy + 28.dp.toPx()), Offset(cx, cy + 36.dp.toPx()), strokeWidth = 2.5f)
                drawLine(color, Offset(cx - 36.dp.toPx(), cy), Offset(cx - 28.dp.toPx(), cy), strokeWidth = 2.5f)
                drawLine(color, Offset(cx + 28.dp.toPx(), cy), Offset(cx + 36.dp.toPx(), cy), strokeWidth = 2.5f)
            }
        }

        // Live hardware camera status indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp, y = 56.dp)
                .clip(CircleShape)
                .background(Color(0x99000000))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isCameraBound) {
                    if (state.isFrontCamera) "كاميرا سيلفي نشطة ⏺" else "كاميرا خلفية 48MP ⏺"
                } else "محاكي الكاميرا الذكي ⏺",
                color = if (isCameraBound) AuraEmeraldGreen else AuraCyanAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CameraPermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xEE0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AuraCyanAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = AuraCyanAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "إذن الوصول للكاميرا مطلوب",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "لكي تتمكن من التقاط الصور والفيديوهات والاستفادة من ميزات الذكاء الاصطناعي وتتبع الحركة، يرجى منح الإذن للتطبيق.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8)
            ),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = AuraCyanAccent,
                contentColor = Color.Black
            ),
            modifier = Modifier.testTag("grant_camera_permission_button")
        ) {
            Text(text = "منح إذن الكاميرا الآن", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CameraSimulatorView(
    state: CameraSettingsState,
    modifier: Modifier = Modifier
) {
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
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF070B14), Color(0xFF111827), Color(0xFF1E293B))
                        )
                    )
                    drawCircle(Color(0x8800E5FF), radius = 45f, center = Offset(w * 0.25f, h * 0.45f))
                    drawCircle(Color(0x77F43F5E), radius = 60f, center = Offset(w * 0.70f, h * 0.50f))
                    drawCircle(Color(0x88FBBF24), radius = 40f, center = Offset(w * 0.45f, h * 0.40f))
                    drawCircle(Color(0x66A855F7), radius = 55f, center = Offset(w * 0.85f, h * 0.42f))
                }
                SceneType.SUNSET -> {
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
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0x00FBBF24))
                        ),
                        radius = w * 0.22f,
                        center = Offset(w * 0.5f, h * 0.42f)
                    )
                }
                SceneType.PORTRAIT -> {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A)),
                            center = Offset(w * 0.5f, h * 0.4f),
                            radius = w * 0.8f
                        )
                    )
                    drawCircle(
                        color = Color(0x66475569),
                        radius = w * 0.22f,
                        center = Offset(w * 0.5f, h * 0.36f)
                    )
                }
                else -> {
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
                    drawCircle(Color(0x55FFFFFF), radius = 70f, center = Offset(w * 0.2f, h * 0.18f))
                    drawCircle(Color(0x66FFFFFF), radius = 90f, center = Offset(w * 0.35f, h * 0.16f))
                }
            }
        }
    }
}
