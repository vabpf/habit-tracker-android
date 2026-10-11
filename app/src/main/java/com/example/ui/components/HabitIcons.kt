package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIconCatalog {
    val icons: Map<String, ImageVector> = mapOf(
        "run" to Icons.AutoMirrored.Filled.DirectionsRun,
        "water" to Icons.Filled.LocalDrink,
        "fitness" to Icons.Filled.FitnessCenter,
        "book" to Icons.AutoMirrored.Filled.MenuBook,
        "meditate" to Icons.Filled.SelfImprovement,
        "journal" to Icons.Filled.EditNote,
        "nosugar" to Icons.Filled.Block,
        "guitar" to Icons.Filled.MusicNote,
        "walk" to Icons.AutoMirrored.Filled.DirectionsWalk,
        "stretch" to Icons.Filled.AccessibilityNew,
        "sleep" to Icons.Filled.Bedtime,
        "bike" to Icons.AutoMirrored.Filled.DirectionsBike,
        "language" to Icons.Filled.Translate,
        "floss" to Icons.Filled.CleanHands,
        "coffee" to Icons.Filled.Coffee,
        "code" to Icons.Filled.Code,
        "work" to Icons.Filled.Laptop,
        "heart" to Icons.Filled.Favorite,
        "save" to Icons.Filled.Savings,
        "leaf" to Icons.Filled.Eco,
        "waterdrop" to Icons.Filled.WaterDrop,
        "check" to Icons.Filled.Check
    )

    fun getIcon(name: String): ImageVector {
        return icons[name.lowercase()] ?: Icons.Filled.Check
    }
}
