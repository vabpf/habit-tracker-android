package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_freezes")
data class StreakFreezeState(
    @PrimaryKey
    val id: Long = 1L,
    val availableTokens: Int = 2,
    val maxTokens: Int = 3,
    val autoFreezeEnabled: Boolean = true,
    val frozenDates: String = "", // Comma-separated ISO-8601 date strings, e.g. "2026-10-10,2026-10-09"
    val totalFreezesUsed: Int = 0
) {
    fun getFrozenDatesList(): List<String> =
        if (frozenDates.isBlank()) emptyList() else frozenDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun isDateFrozen(date: String): Boolean =
        getFrozenDatesList().contains(date)
}
