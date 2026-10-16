// Location: app/src/main/java/com/vk/clockdin/ui/settings/SettingsScreen.kt
package com.vk.clockdin.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceMedium
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.MoodJoy
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard
import com.vk.clockdin.ui.theme.spaceBackground
import java.time.LocalDate

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    // Launcher for POST_NOTIFICATIONS permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setReminderEnabled(true)
        }
    }

    // SAF Export Document Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri)
        }
    }

    // SAF Import Document Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .spaceBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .glassCard(shape = CircleShape, fillColor = GlassSurfaceLow)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Feedback banner if message available
            uiState.feedbackMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .glassCard(shape = RoundedCornerShape(14.dp), fillColor = Color(0x3300E5FF))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { viewModel.dismissFeedback() }
                        .padding(14.dp)
                ) {
                    Text(
                        text = msg,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Section: Daily Mood Reminder
            SettingsSectionHeader(title = "DAILY CHECK-IN")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MoodJoy,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text("Daily Reminder", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("Quiet notification to paint today's orb", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = uiState.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setReminderEnabled(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = MoodJoy,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = GlassSurfaceUltraLow
                            )
                        )
                    }

                    if (uiState.reminderEnabled) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Reminder Time Presets", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val timePresets = listOf(
                                (20 to 0) to "8:00 PM",
                                (21 to 0) to "9:00 PM",
                                (22 to 0) to "10:00 PM"
                            )

                            timePresets.forEach { (time, label) ->
                                val (h, m) = time
                                val isSelected = uiState.reminderHour == h && uiState.reminderMinute == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .glassCard(shape = RoundedCornerShape(18.dp), fillColor = if (isSelected) MoodJoy.copy(alpha = 0.25f) else GlassSurfaceUltraLow)
                                        .border(1.dp, if (isSelected) MoodJoy else Color.Transparent, RoundedCornerShape(18.dp))
                                        .clickable { viewModel.setReminderTime(h, m) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MoodJoy else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Section: Sensory Feedback
            SettingsSectionHeader(title = "SENSORY")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Haptic Feedback", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Subtle physical vibration on brushes & actions", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    Switch(
                        checked = uiState.hapticsEnabled,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = Color(0xFF00E5FF),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = GlassSurfaceUltraLow
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Section: Cosmic Data Archive & Portability
            SettingsSectionHeader(title = "COSMIC DATA ARCHIVE")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Total Orbs Recorded: ${uiState.totalEntries}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Export Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .glassCard(shape = RoundedCornerShape(16.dp), fillColor = GlassSurfaceUltraLow)
                            .clickable(enabled = !uiState.isExporting) {
                                exportLauncher.launch("starscript_backup_${LocalDate.now()}.json")
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TextPrimary)
                            } else {
                                Icon(Icons.Default.FileUpload, contentDescription = null, tint = MoodJoy, modifier = Modifier.size(18.dp))
                            }
                            Text("Export Backup JSON ✦", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Import Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .glassCard(shape = RoundedCornerShape(16.dp), fillColor = GlassSurfaceUltraLow)
                            .clickable(enabled = !uiState.isImporting) {
                                importLauncher.launch(arrayOf("application/json"))
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (uiState.isImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TextPrimary)
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            }
                            Text("Restore from Backup ✦", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Clear All Data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE53935).copy(alpha = 0.20f))
                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.60f), RoundedCornerShape(16.dp))
                            .clickable { showClearDialog = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            Text("Erase Galaxy Archive", color = Color(0xFFFF8599), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Section: About
            SettingsSectionHeader(title = "ABOUT")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .glassCard(shape = RoundedCornerShape(22.dp), fillColor = GlassSurfaceLow)
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Starscript",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Version 1.0.0 (3D Cosmic Edition)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "by vk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFFFFC83D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Clear Confirmation Dialog
        if (showClearDialog) {
            ClearDataConfirmationDialog(
                onDismiss = { showClearDialog = false },
                onConfirmClear = {
                    showClearDialog = false
                    viewModel.clearAllData()
                }
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = TextTertiary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun ClearDataConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmClear: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .glassCard(shape = RoundedCornerShape(26.dp), fillColor = GlassSurfaceMedium)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Erase entire galaxy?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "This will permanently erase all painted mood orbs, reflection notes, and attached photos. This cannot be undone.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .glassCard(shape = RoundedCornerShape(24.dp), fillColor = GlassSurfaceLow)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    // Bold vivid high-contrast erase button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE53935), Color(0xFFD32F2F))
                                )
                            )
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(24.dp))
                            .clickable(onClick = onConfirmClear),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erase Galaxy",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
