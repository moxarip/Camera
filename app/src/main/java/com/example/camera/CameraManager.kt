package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.data.model.DetectedObject
import com.example.data.model.SceneType
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
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private val _isHardwareCameraBound = MutableStateFlow(false)
    val isHardwareCameraBound: StateFlow<Boolean> = _isHardwareCameraBound.asStateFlow()

    private val _hasHardwareCamera = MutableStateFlow(true)
    val hasHardwareCamera: StateFlow<Boolean> = _hasHardwareCamera.asStateFlow()

    fun bindCameraToLifecycle(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean,
        onCameraBound: (Boolean) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                if (!cameraProvider.hasCamera(cameraSelector)) {
                    Log.w(tag, "No hardware camera available for selector, fallback to simulator")
                    _hasHardwareCamera.value = false
                    _isHardwareCameraBound.value = false
                    onCameraBound(false)
                    return@addListener
                }

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                _hasHardwareCamera.value = true
                _isHardwareCameraBound.value = true
                onCameraBound(true)
                Log.d(tag, "Camera successfully bound to lifecycle")
            } catch (e: Exception) {
                Log.e(tag, "Failed to bind camera: ${e.message}", e)
                _hasHardwareCamera.value = false
                _isHardwareCameraBound.value = false
                onCameraBound(false)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePicture(
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val capture = imageCapture
        if (capture == null || !_isHardwareCameraBound.value) {
            onError("Hardware camera not ready, will use software pipeline capture")
            return
        }

        val outputDir = context.cacheDir
        val photoFile = File(
            outputDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
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
                    onError(exception.message ?: "Failed to take photo")
                }
            }
        )
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
