// Location: app/src/main/java/com/vk/clockdin/ui/paint/AddCustomColorDialog.kt
package com.vk.clockdin.ui.paint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vk.clockdin.domain.model.MoodCategory
import com.vk.clockdin.ui.theme.GlassBorderHairline
import com.vk.clockdin.ui.theme.GlassSurfaceLow
import com.vk.clockdin.ui.theme.GlassSurfaceUltraLow
import com.vk.clockdin.ui.theme.TextDisabled
import com.vk.clockdin.ui.theme.TextPrimary
import com.vk.clockdin.ui.theme.TextSecondary
import com.vk.clockdin.ui.theme.TextTertiary
import com.vk.clockdin.ui.theme.glassCard

private val PRESET_COSMIC_COLORS = listOf(
    "#FF8DA1", // Rose Nebula
    "#FF6B6B", // Coral Flare
    "#FF9F43", // Amber Sun
    "#FECA57", // Starlight Gold
    "#1DD1A1", // Cosmic Mint
    "#10AC84", // Aurora Emerald
    "#00E5FF", // Cyber Cyan
    "#00D2D3", // Electric Teal
    "#0ABDE3", // Deep Ocean
    "#2E86DE", // Star Sapphire
    "#7C4DFF", // Galactic Violet
    "#9D4DFF", // Nebula Purple
    "#B8A9E8", // Lavender Mist
    "#FF4081", // Radiant Pink
    "#FF1744", // Crimson Nova
    "#F1F2F6", // Pure Starlight
    "#708090", // Meteor Slate
    "#DC143C"  // Deep Crimson
)

@Composable
fun AddCustomColorDialog(
    onDismiss: () -> Unit,
    onAddColor: (name: String, hex: String, category: MoodCategory) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MoodCategory.POSITIVE) }
    var currentHex by remember { mutableStateOf("#00E5FF") }
    var hueSliderValue by remember { mutableFloatStateOf(180f) }

    val activeColor = remember(currentHex) {
        parseHexToColor(currentHex)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF141720))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "✦ Custom Mood Color",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Add your personal tone to the star palette",
                            fontSize = 11.sp,
                            color = Color(0xFFB0B5C6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FFFFFF))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Live Preview Sphere Swatch
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Radial glow aura
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        activeColor.copy(alpha = 0.55f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✦",
                                color = if (isLightColor(activeColor)) Color.Black else Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // 1. Name Input Field
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "COLOR / MOOD NAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0B5C6),
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .glassCard(shape = RoundedCornerShape(14.dp), fillColor = Color(0x33262A36))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (nameText.isEmpty()) {
                            Text(
                                text = "e.g. Euphoric, Serene, Electric...",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = nameText,
                            onValueChange = { if (it.length <= 24) nameText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 2. Category Selector Pills
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MOOD CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0B5C6),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoodCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            val label = when (cat) {
                                MoodCategory.POSITIVE -> "☀️ Positive"
                                MoodCategory.NEUTRAL -> "🌙 Neutral"
                                MoodCategory.NEGATIVE -> "⚡ Negative"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.20f) else Color(0x26262A36))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCategory = cat },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFB0B5C6)
                                )
                            }
                        }
                    }
                }

                // 3. Preset Celestial Hues Grid (18 options)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "CHOOSE COLOR TONE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0B5C6),
                        letterSpacing = 1.sp
                    )

                    // 3 rows of 6 color dots
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRESET_COSMIC_COLORS.chunked(6).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowColors.forEach { hex ->
                                    val c = parseHexToColor(hex)
                                    val isChosen = currentHex.equals(hex, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(
                                                width = if (isChosen) 2.5.dp else 1.dp,
                                                color = if (isChosen) Color.White else Color(0x33FFFFFF),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                currentHex = hex
                                                // Sync slider hue approximately
                                                val hsv = FloatArray(3)
                                                android.graphics.Color.colorToHSV(c.toArgb(), hsv)
                                                hueSliderValue = hsv[0]
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChosen) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Chosen",
                                                tint = if (isLightColor(c)) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Interactive Rainbow Hue Slider
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HUE SPECTRUM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0B5C6),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentHex,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }

                    // Rainbow background gradient slider track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Red,
                                        Color.Yellow,
                                        Color.Green,
                                        Color.Cyan,
                                        Color.Blue,
                                        Color.Magenta,
                                        Color.Red
                                    )
                                )
                            )
                    )

                    Slider(
                        value = hueSliderValue,
                        onValueChange = { hue ->
                            hueSliderValue = hue
                            val hsv = floatArrayOf(hue, 0.82f, 0.98f)
                            val rgb = android.graphics.Color.HSVToColor(hsv)
                            currentHex = String.format("#%06X", 0xFFFFFF and rgb)
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 5. "Add to Palette ✦" Action Button
                val canAdd = nameText.trim().isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (canAdd) {
                                Brush.horizontalGradient(
                                    listOf(
                                        activeColor.copy(alpha = 0.85f),
                                        activeColor.copy(alpha = 0.55f)
                                    )
                                )
                            } else {
                                SolidColor(Color(0x26FFFFFF))
                            }
                        )
                        .border(
                            1.dp,
                            if (canAdd) Color.White else Color(0x33FFFFFF),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = canAdd) {
                            onAddColor(nameText.trim(), currentHex, selectedCategory)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add to Palette ✦",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAdd) Color.White else TextDisabled,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun parseHexToColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorLong = clean.toLong(16)
        if (clean.length == 6) Color(colorLong or 0xFF000000) else Color(colorLong)
    } catch (_: Exception) {
        Color(0xFF00E5FF)
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance > 0.6f
}
