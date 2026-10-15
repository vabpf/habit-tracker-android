package com.example.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BrandPrimary

/**
 * Shared background editor used by both widget configuration activities:
 * on/off switch, preset color swatches, opacity slider and a live preview
 * rendered like the real card (color × opacity over a wallpaper stand-in).
 */
@Composable
fun WidgetAppearanceEditor(
    appearance: WidgetAppearance,
    onChange: (WidgetAppearance) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // On / off
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Card background",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = if (appearance.backgroundEnabled) "Tinted card" else "Fully transparent",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Switch(
                checked = appearance.backgroundEnabled,
                onCheckedChange = { onChange(appearance.copy(backgroundEnabled = it)) }
            )
        }

        if (appearance.backgroundEnabled) {
            Spacer(modifier = Modifier.height(14.dp))

            // Color swatches
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetAppearance.SWATCHES.forEach { colorInt ->
                    val isSelected = appearance.backgroundColor == colorInt
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) BrandPrimary
                                else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable {
                                onChange(appearance.copy(backgroundColor = colorInt))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Opacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Opacity",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "${appearance.backgroundOpacity}%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimary
                    )
                )
            }
            Slider(
                value = appearance.backgroundOpacity.toFloat(),
                onValueChange = {
                    onChange(appearance.copy(backgroundOpacity = it.toInt()))
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = BrandPrimary,
                    activeTrackColor = BrandPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Preview (same math as the real card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2B3A67),
                                Color(0xFF101828)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Color(appearance.backgroundColor).copy(
                                alpha = appearance.backgroundOpacity / 100f
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8AE034))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Preview",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transparent cards can be hard to read over bright wallpapers.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
