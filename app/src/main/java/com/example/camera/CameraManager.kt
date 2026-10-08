package com.example.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {
    private val tag = "CameraManager"
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val _isHardwareCameraBound = MutableStateFlow(false)
    val isHardwareCameraBound: StateFlow<Boolean> = _isHardwareCameraBound.asStateFlow()

    private val _hasHardwareCamera = MutableStateFlow(true)
    val hasHardwareCamera: StateFlow<Boolean> = _hasHardwareCamera.asStateFlow()

    fun bindCameraToLifecycle(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        flashMode: FlashMode = FlashMode.AUTO,
        onCameraBound: (Boolean) -> Unit
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(tag, "Camera permission not granted yet")
            _isHardwareCameraBound.value = false
            onCameraBound(false)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageCaptureFlash = when (flashMode) {
                    FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                    FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                    else -> ImageCapture.FLASH_MODE_AUTO
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setFlashMode(imageCaptureFlash)
                    .build()

                // Determine best selector with fallback
                val preferredSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val selectedCamera = if (provider.hasCamera(preferredSelector)) {
                    preferredSelector
                } else {
                    val fallbackSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    if (provider.hasCamera(fallbackSelector)) {
                        fallbackSelector
                    } else {
                        Log.w(tag, "No camera sensors found on this device")
                        _hasHardwareCamera.value = false
                        _isHardwareCameraBound.value = false
                        onCameraBound(false)
                        return@addListener
                    }
                }

                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selectedCamera,
                    preview,
                    imageCapture
                )

                // Enable torch if requested
                if (flashMode == FlashMode.TORCH) {
                    camera?.cameraControl?.enableTorch(true)
                } else {
                    camera?.cameraControl?.enableTorch(false)
                }

                _hasHardwareCamera.value = true
                _isHardwareCameraBound.value = true
                onCameraBound(true)
                Log.d(tag, "Hardware camera successfully bound to lifecycle")
            } catch (e: Exception) {
                Log.e(tag, "Failed to bind camera to lifecycle: ${e.message}", e)
                _hasHardwareCamera.value = false
                _isHardwareCameraBound.value = false
                onCameraBound(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setZoom(zoomLevel: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(zoomLevel.coerceIn(1f, 8f))
        } catch (e: Exception) {
            Log.e(tag, "Failed to set zoom: ${e.message}")
        }
    }

    fun setFlashMode(flashMode: FlashMode) {
        try {
            val mode = when (flashMode) {
                FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                else -> ImageCapture.FLASH_MODE_AUTO
            }
            imageCapture?.flashMode = mode
            camera?.cameraControl?.enableTorch(flashMode == FlashMode.TORCH)
        } catch (e: Exception) {
            Log.e(tag, "Failed to set flash mode: ${e.message}")
        }
    }

    fun focusOnPoint(previewView: PreviewView, x: Float, y: Float) {
        try {
            val factory = previewView.meteringPointFactory
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point).build()
            camera?.cameraControl?.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.e(tag, "Failed to trigger autofocus: ${e.message}")
        }
    }

    fun takePicture(
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCapture
        if (capture == null || !_isHardwareCameraBound.value) {
            onError("Hardware camera not bound")
            return
        }

        val outputDir = File(context.filesDir, "photos").apply { mkdirs() }
        val photoFile = File(
            outputDir,
            "AURA_${SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSuccess(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(tag, "takePicture error: ${exception.message}", exception)
                    onError(exception.message ?: "Failed to take photo")
                }
            }
        )
    }

    fun shutdown() {
        try {
            cameraProvider?.unbindAll()
            cameraExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(tag, "Shutdown error: ${e.message}")
        }
    }
}
