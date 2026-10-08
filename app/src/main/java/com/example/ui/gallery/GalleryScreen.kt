package com.example.ui.gallery

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CaptureMode
import com.example.data.model.PhotoItem
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCoralRed
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceElevated
import com.example.ui.theme.AuraEmeraldGreen
import com.example.ui.viewmodel.CameraViewModel

enum class GalleryFilter(val labelAr: String) {
    ALL("الكل"),
    RAW("صيغة RAW"),
    PORTRAIT("بورتريه"),
    NIGHT("ليلي"),
    HDR("دمج HDR"),
    VIDEOS("فيديو"),
    SYNCED("سحابي")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: CameraViewModel,
    onBackToCamera: () -> Unit,
    onEditPhoto: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToCamera() }

    val context = LocalContext.current
    val allPhotos by viewModel.allPhotos.collectAsState(initial = emptyList())
    val isSyncing by viewModel.isSyncing.collectAsState()

    var activeFilter by remember { mutableStateOf(GalleryFilter.ALL) }
    var selectedPhotoForDetail by remember { mutableStateOf<PhotoItem?>(null) }
    var photoForSocialShare by remember { mutableStateOf<PhotoItem?>(null) }

    val filteredPhotos = remember(allPhotos, activeFilter) {
        when (activeFilter) {
            GalleryFilter.ALL -> allPhotos
            GalleryFilter.RAW -> allPhotos.filter { it.isRaw }
            GalleryFilter.PORTRAIT -> allPhotos.filter { it.mode == CaptureMode.PORTRAIT.name }
            GalleryFilter.NIGHT -> allPhotos.filter { it.mode == CaptureMode.NIGHT.name }
            GalleryFilter.HDR -> allPhotos.filter { it.mode == CaptureMode.HDR_MERGE.name }
            GalleryFilter.VIDEOS -> allPhotos.filter { it.isVideo }
            GalleryFilter.SYNCED -> allPhotos.filter { it.isSynced }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "معرض AuraCam",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${filteredPhotos.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = AuraCyanAccent
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToCamera,
                        modifier = Modifier.testTag("gallery_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة للكاميرا",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerCloudSync() },
                        modifier = Modifier.testTag("gallery_sync_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "مزامنة سحابية",
                            tint = if (isSyncing) AuraCyanAccent else Color.White
                        )
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
            // Filter Pills bar
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(AuraDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GalleryFilter.entries.forEach { filter ->
                    val isSelected = activeFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AuraCyanAccent else AuraDarkSurfaceElevated)
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_chip_${filter.name}")
                    ) {
                        Text(
                            text = filter.labelAr,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0x66FFFFFF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد صور في هذا التصنيف بعد",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("gallery_grid")
                ) {
                    items(filteredPhotos, key = { it.id }) { photo ->
                        GalleryPhotoCard(
                            photo = photo,
                            onClick = { selectedPhotoForDetail = photo }
                        )
                    }
                }
            }
        }
    }

    // Photo Detail Inspector Modal Sheet
    selectedPhotoForDetail?.let { photo ->
        PhotoDetailSheet(
            photo = photo,
            onDismiss = { selectedPhotoForDetail = null },
            onEdit = {
                selectedPhotoForDetail = null
                onEditPhoto(photo)
            },
            onShare = {
                selectedPhotoForDetail = null
                photoForSocialShare = photo
            },
            onDelete = {
                viewModel.deletePhoto(photo.id)
                selectedPhotoForDetail = null
            }
        )
    }

    // Quick Social Share Bottom Sheet
    photoForSocialShare?.let { photo ->
        QuickSocialShareSheet(
            photo = photo,
            onDismiss = { photoForSocialShare = null },
            onShareToApp = { platform ->
                sharePhotoToSocialMedia(context, photo, platform)
                photoForSocialShare = null
            }
        )
    }
}

@Composable
fun GalleryPhotoCard(
    photo: PhotoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("photo_item_${photo.id}")
    ) {
        // Thumbnail canvas art representing realistic photo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val gradient = when (photo.sceneTag) {
                "SUNSET" -> listOf(Color(0xFF9A3412), Color(0xFFF97316), Color(0xFFFBBF24))
                "NIGHT" -> listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF00E5FF))
                "PORTRAIT" -> listOf(Color(0xFF334155), Color(0xFF475569), Color(0xFFFDE047))
                else -> listOf(Color(0xFF0369A1), Color(0xFF38BDF8), Color(0xFF10B981))
            }
            drawRect(brush = Brush.linearGradient(gradient, start = Offset.Zero, end = Offset(w, h)))
            drawCircle(Color.White.copy(alpha = 0.4f), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
        }

        // Top Badges (RAW / Video)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (photo.isRaw) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(AuraCyanAccent)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = "RAW", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (photo.isVideo) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(AuraCoralRed)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }

        // Cloud sync status badge at bottom right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0x88000000))
                .padding(2.dp)
        ) {
            Icon(
                imageVector = if (photo.isSynced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                contentDescription = null,
                tint = if (photo.isSynced) AuraEmeraldGreen else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailSheet(
    photo: PhotoItem,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraDarkSurfaceElevated,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("photo_detail_sheet")
        ) {
            // Header with title and close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = photo.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "الوضع: ${photo.mode} • ${photo.fileSizeFormatted}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                if (photo.isRaw) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AuraCyanAccent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "48MP RAW", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EXIF Metadata Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetadataLabelValue("سرعة الغالق", photo.shutterSpeed)
                        MetadataLabelValue("الحساسية ISO", "${photo.iso}")
                        MetadataLabelValue("فتحة العدسة", photo.aperture)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetadataLabelValue("البعد البؤري", photo.focalLength)
                        MetadataLabelValue("الدقة", photo.resolution)
                        MetadataLabelValue("السحابية", if (photo.isSynced) "متزامن ✓" else "قيد الرفع")
                    }
                    MetadataLabelValue("التعرف على الكائنات", photo.detectedObjectsSummary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Edit, Quick Share, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyanAccent, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_edit_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تحرير متقدم", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = AuraAmberAccent, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_share_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مشاركة سريعة", fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33F43F5E))
                        .testTag("detail_delete_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = AuraCoralRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun MetadataLabelValue(label: String, value: String) {
    Column {
        Text(text = label, color = Color(0xFF64748B), fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSocialShareSheet(
    photo: PhotoItem,
    onDismiss: () -> Unit,
    onShareToApp: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AuraDarkSurfaceElevated,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("quick_social_share_sheet")
        ) {
            Text(
                text = "مشاركة سريعة عبر وسائل التواصل الاجتماعي",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "اختر المنصة لإرسال الصورة مباشرة مع وسم #AuraCamAI",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val platforms = listOf(
                Triple("instagram_feed", "Instagram Feed", Color(0xFFE1306C)),
                Triple("instagram_story", "Instagram Stories", Color(0xFFC13584)),
                Triple("whatsapp", "WhatsApp", Color(0xFF25D366)),
                Triple("twitter", "X (Twitter)", Color(0xFF1DA1F2)),
                Triple("telegram", "Telegram", Color(0xFF0088CC)),
                Triple("system_chooser", "المزيد / مشاركة النظام", AuraCyanAccent)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                platforms.forEach { (key, name, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraDarkSurface)
                            .clickable { onShareToApp(key) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("share_target_$key"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

fun sharePhotoToSocialMedia(context: Context, photo: PhotoItem, platform: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        val caption = "تم التقاطها بواسطة تطبيق AuraCam AI 📸✨\n" +
                "الوضع: ${photo.mode} • ${photo.resolution} • ${if (photo.isRaw) "RAW 16-bit" else "Ultra HDR"}\n" +
                "#AuraCamAI #SmartCamera #MobilePhotography"
        putExtra(Intent.EXTRA_TEXT, caption)
        putExtra(Intent.EXTRA_SUBJECT, photo.title)

        when (platform) {
            "instagram_feed", "instagram_story" -> setPackage("com.instagram.android")
            "whatsapp" -> setPackage("com.whatsapp")
            "twitter" -> setPackage("com.twitter.android")
            "telegram" -> setPackage("org.telegram.messenger")
            else -> {}
        }
    }

    try {
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة صورة AuraCam"))
    } catch (e: Exception) {
        // Fallback to general system chooser if specified app package is not installed
        val genericIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "تم التقاطها عبر AuraCam AI: ${photo.title}")
        }
        context.startActivity(Intent.createChooser(genericIntent, "مشاركة"))
    }
}
