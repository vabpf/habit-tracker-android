package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ContributionGrid
import com.example.ui.components.HabitIconCatalog
import com.example.ui.theme.LocalAppChrome
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.HabitAmber
import com.example.ui.theme.HabitCoral
import com.example.ui.theme.HabitSky
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HabitViewModel

@Composable
fun SettingsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val context = LocalContext.current
    val currentTheme by viewModel.themeMode.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()

    val featuredHabit = habits.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            // Content flows behind the transparent nav bar (no dead band);
            // bottom padding keeps the last item clear of the gesture pill.
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LocalAppChrome.current.container)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Theme Mode Card (Dark / Light / System)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandPrimary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Palette,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Appearance & Theme",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Choose how the app looks on your device.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val modes = listOf(
                            Triple(ThemeMode.DARK, "Dark Slate", Icons.Filled.DarkMode to BrandPrimary),
                            Triple(ThemeMode.LIGHT, "Light", Icons.Filled.LightMode to HabitAmber),
                            Triple(ThemeMode.SYSTEM, "System", Icons.Filled.DesktopWindows to HabitSky)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            modes.forEach { (mode, label, iconAndTint) ->
                                val (icon, tint) = iconAndTint
                                val isSelected = currentTheme == mode
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) BrandPrimary.copy(alpha = 0.10f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) BrandPrimary
                                                else LocalAppChrome.current.border,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { viewModel.setThemeMode(mode) }
                                            .padding(vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(tint.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = tint,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 6.dp, y = (-6).dp)
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(BrandPrimary)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Home Screen Widgets Simulator / Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF26D3EB).copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Widgets,
                                    contentDescription = null,
                                    tint = Color(0xFF26D3EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Home Screen Widgets",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Keep your habit grids in sight on your home screen.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Widget Preview mockup
                        if (featuredHabit != null) {
                            val fLogs = allLogs.filter { it.habitId == featuredHabit.id }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(1.dp, LocalAppChrome.current.border, RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(featuredHabit.colorHex).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = HabitIconCatalog.getIcon(featuredHabit.iconName),
                                                contentDescription = null,
                                                tint = Color(featuredHabit.colorHex),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = featuredHabit.name,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .border(
                                                    1.dp,
                                                    LocalAppChrome.current.border,
                                                    CircleShape
                                                )
                                                .clickable {
                                                    viewModel.navigateTo(
                                                        AppScreen.NOTES,
                                                        featuredHabit.id
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Widget Preview",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    ContributionGrid(
                                        habit = featuredHabit,
                                        logs = fLogs,
                                        columns = 16,
                                        rows = 5,
                                        tileHeight = 7.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Private By Design Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF3BD9A5).copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF3BD9A5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Private by Design",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "No account. No sign-up. No ads. Your habit data stays securely on your device, and the app works completely offline.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 21.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(HabitSky.copy(alpha = 0.16f))
                                    .border(
                                        1.dp,
                                        HabitSky.copy(alpha = 0.35f),
                                        RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = HabitSky,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Room Database Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HabitCoral.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Storage,
                                    contentDescription = null,
                                    tint = HabitCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Room Database Management",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Manage local SQLite persistent data on this device.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clear Database Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalAppChrome.current.dangerContainer)
                                .border(1.dp, LocalAppChrome.current.dangerBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.clearAllData()
                                    Toast.makeText(context, "Room database cleared! Starting blank.", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HabitCoral.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = HabitCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clear All Data (Start Blank)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LocalAppChrome.current.dangerText
                                    )
                                )
                                Text(
                                    text = "Deletes all stored habits, logs, and notes.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LocalAppChrome.current.dangerSubText
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = LocalAppChrome.current.dangerText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
