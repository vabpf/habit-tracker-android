package com.example.ui.components

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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.StreakFreezeState
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.LocalAppChrome

@Composable
fun StreakFreezeBanner(
    freezeState: StreakFreezeState?,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = freezeState ?: StreakFreezeState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LocalAppChrome.current.infoContainer)
            .border(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onOpenVault() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("streak_freeze_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF0288D1).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AcUnit,
                contentDescription = "Streak Freeze",
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Streak Shield Active",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "${state.availableTokens}/${state.maxTokens} freeze shields remaining",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LocalAppChrome.current.infoText
                )
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF0288D1).copy(alpha = 0.3f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "VAULT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4FC3F7),
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun StreakFreezeVaultDialog(
    freezeState: StreakFreezeState?,
    onToggleAutoFreeze: (Boolean) -> Unit,
    onUseFreezeToday: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = freezeState ?: StreakFreezeState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Streak Shield Vault",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Streak shields automatically protect your active habit streaks when you take a rest day or miss a daily check-in.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // Status card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LocalAppChrome.current.infoContainer)
                        .border(1.dp, Color(0xFF4FC3F7).copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Shields Available", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text("❄️ ${state.availableTokens} / ${state.maxTokens}", color = LocalAppChrome.current.infoText, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lifetime Rescues", color = LocalAppChrome.current.muted, fontSize = 13.sp)
                            Text("${state.totalFreezesUsed}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // Auto-freeze toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalAppChrome.current.containerAlt)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Rescue Missed Days",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Preserves streaks automatically without manual intervention",
                            style = MaterialTheme.typography.bodySmall.copy(color = LocalAppChrome.current.muted)
                        )
                    }
                    Switch(
                        checked = state.autoFreezeEnabled,
                        onCheckedChange = { onToggleAutoFreeze(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0288D1)
                        )
                    )
                }

                // Manual freeze button
                Button(
                    onClick = {
                        onUseFreezeToday()
                    },
                    enabled = state.availableTokens > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("freeze_today_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0288D1),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AcUnit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.availableTokens > 0) "Freeze & Protect Today" else "No Shields Left (Refills weekly)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = LocalAppChrome.current.infoText, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
