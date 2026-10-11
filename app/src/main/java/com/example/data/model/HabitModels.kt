package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class HabitType {
    BUILD, // Mark each day completed
    QUIT   // Counts as clean until you log a slip (or stay under max)
}

enum class TrackingType {
    YES_NO,  // Simple checkmark
    COUNT,   // Reps, pages, glasses
    MEASURE  // Continuous amounts with unit (L, km, hours)
}

@Entity(
    tableName = "habits",
    indices = [Index(value = ["displayOrder"])]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconName: String = "check",
    val colorHex: Long = 0xFF8AE034,
    val habitType: HabitType = HabitType.BUILD,
    val trackingType: TrackingType = TrackingType.YES_NO,
    val targetValue: Float = 1.0f,
    val unit: String = "",
    val frequencyDaysPerWeek: Int = 7,
    val category: String = "All",
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(
    tableName = "habit_logs",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["habitId"]),
        Index(value = ["date"])
    ]
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // ISO date "YYYY-MM-DD"
    val value: Float = 1.0f,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_notes",
    indices = [
        Index(value = ["habitId", "date"]),
        Index(value = ["habitId"])
    ]
)
data class HabitNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // ISO date "YYYY-MM-DD"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_reminders",
    indices = [Index(value = ["habitId"])]
)
data class HabitReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon, 7=Sun
    val isEnabled: Boolean = true
)
