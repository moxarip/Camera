package com.example.ui.cloud

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.data.model.SyncDevice
import com.example.ui.theme.AuraAmberAccent
import com.example.ui.theme.AuraCyanAccent
import com.example.ui.theme.AuraDarkBg
import com.example.ui.theme.AuraDarkSurface
import com.example.ui.theme.AuraDarkSurfaceElevated
import com.example.ui.theme.AuraEmeraldGreen
import com.example.ui.viewmodel.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncScreen(
    viewModel: CameraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgressPercent.collectAsState()
    val devices by viewModel.allDevices.collectAsState(initial = emptyList())

    var autoBackupEnabled by remember { mutableStateOf(true) }
    var wifiOnlyEnabled by remember { mutableStateOf(true) }
    var backupRawOriginals by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "التخزين السحابي والمزامنة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("cloud_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Quota Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_quota_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = AuraCyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مساحة التخزين السحابي الآمنة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Text(
                            text = "4.2 GB / 15.0 GB",
                            color = AuraCyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { 4.2f / 15.0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AuraCyanAccent,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "ملفات RAW: 2.8 GB", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(text = "صور وفيديوهات HDR: 1.4 GB", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            // Sync Status & Sync Now button
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSyncing) AuraAmberAccent else AuraEmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSyncing) "جارٍ المزامنة السحابية... ($syncProgress%)"
                                else "جميع الصور والملفات متزامنة بأمان",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (isSyncing) {
                        LinearProgressIndicator(
                            progress = { syncProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AuraAmberAccent,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerCloudSync() },
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraCyanAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_now_button")
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSyncing) "جارٍ المزامنة السريعة..." else "مزامنة الآن لجميع الأجهزة",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Backup Settings Toggles
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "خيارات النسخ الاحتياطي التلقائي",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    // Auto backup switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "نسخ احتياطي تلقائي للصور والملفات", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "رفع الصور فور التقاطها لضمان أمان البيانات وعدم فقدانها", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { autoBackupEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AuraCyanAccent
                            ),
                            modifier = Modifier.testTag("switch_auto_backup")
                        )
                    }

                    // Wi-Fi only switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "المزامنة عبر شبكات Wi-Fi فقط", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "توفير باقة بيانات الجوال أثناء السفر والتنقل", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = wifiOnlyEnabled,
                            onCheckedChange = { wifiOnlyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AuraCyanAccent
                            )
                        )
                    }

                    // RAW original backup switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "حفظ النسخ الأصلية RAW 16-bit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "نسخ ملفات DNG الخام كاملة بدون أي ضغط", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = backupRawOriginals,
                            onCheckedChange = { backupRawOriginals = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AuraCyanAccent
                            )
                        )
                    }
                }
            }

            // Multi-Device Sync Hub
            Card(
                colors = CardDefaults.cardColors(containerColor = AuraDarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connected_devices_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Devices, contentDescription = null, tint = AuraAmberAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الأجهزة المتصلة بالمزامنة السحابية",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    devices.forEach { device ->
                        DeviceItemRow(device = device)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AuraDarkSurfaceElevated)
                            .clickable { /* QR connect simulation */ }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = AuraCyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "ربط جهاز جديد عبر رمز QR الموحد", color = AuraCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Security & End-to-End Encryption Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2210B981))
                    .border(1.dp, AuraEmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AuraEmeraldGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "جميع بياناتك وصورك مشفرة بتقنية AES-256 E2EE ومحمية لضمان الخصوصية التامة.",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun DeviceItemRow(device: SyncDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AuraDarkSurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val icon = when (device.deviceType) {
                "tablet" -> Icons.Default.Tablet
                "desktop" -> Icons.Default.Computer
                else -> Icons.Default.PhoneAndroid
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (device.isCurrentDevice) AuraCyanAccent else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = device.deviceName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = device.lastSyncFormatted,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (device.isCurrentDevice) AuraEmeraldGreen.copy(alpha = 0.2f)
                    else Color(0x33FFFFFF)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (device.isCurrentDevice) "نشط الآن" else "متزامن",
                color = if (device.isCurrentDevice) AuraEmeraldGreen else Color(0xFFCBD5E1),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
