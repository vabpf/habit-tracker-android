package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_badges")
data class UnlockedBadge(
    @PrimaryKey
    val badgeId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val category: String, // "Streak", "Consistency", "Volume", "Protection"
    val target: Int,
    val current: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
) {
    val progress: Float
        get() = if (target <= 0) 1f else (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}
