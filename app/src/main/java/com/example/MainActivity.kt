package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PhotoItem
import com.example.ui.camera.CameraScreen
import com.example.ui.cloud.CloudSyncScreen
import com.example.ui.editor.PhotoEditorScreen
import com.example.ui.gallery.GalleryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CameraViewModel

sealed interface AppDestination {
    data object Camera : AppDestination
    data object Gallery : AppDestination
    data class Editor(val photo: PhotoItem) : AppDestination
    data object CloudSync : AppDestination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AuraCamMainApp()
            }
        }
    }
}

@Composable
fun AuraCamMainApp(
    viewModel: CameraViewModel = viewModel()
) {
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Camera) }
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (val dest = currentDestination) {
                is AppDestination.Camera -> {
                    CameraScreen(
                        viewModel = viewModel,
                        onNavigateToGallery = { currentDestination = AppDestination.Gallery },
                        onNavigateToCloudSync = { currentDestination = AppDestination.CloudSync }
                    )
                }

                is AppDestination.Gallery -> {
                    GalleryScreen(
                        viewModel = viewModel,
                        onBackToCamera = { currentDestination = AppDestination.Camera },
                        onEditPhoto = { photo ->
                            currentDestination = AppDestination.Editor(photo)
                        }
                    )
                }

                is AppDestination.Editor -> {
                    PhotoEditorScreen(
                        photo = dest.photo,
                        onBack = { currentDestination = AppDestination.Gallery },
                        onSaveSuccess = { updated ->
                            viewModel.updatePhoto(updated)
                            currentDestination = AppDestination.Gallery
                        },
                        onShare = { /* shared */ }
                    )
                }

                is AppDestination.CloudSync -> {
                    CloudSyncScreen(
                        viewModel = viewModel,
                        onBack = { currentDestination = AppDestination.Camera }
                    )
                }
            }
        }
    }
}
