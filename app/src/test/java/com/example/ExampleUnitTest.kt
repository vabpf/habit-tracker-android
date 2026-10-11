package com.example

import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import com.example.ui.viewmodel.HabitUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `habit creation and streak logic`() {
        val habit = Habit(
            id = 1,
            name = "Morning Run",
            habitType = HabitType.BUILD,
            trackingType = TrackingType.YES_NO,
            targetValue = 1f,
            isArchived = false
        )
        assertEquals("Morning Run", habit.name)
        assertTrue(habit.targetValue > 0f)
        assertFalse(habit.isArchived)
    }

    @Test
    fun `unified ui state model contains expected defaults`() {
        val state = HabitUiState()
        assertEquals("All", state.selectedCategory)
        assertTrue(state.availableCategories.contains("Archived"))
        assertTrue(state.habits.isEmpty())
        assertFalse(state.isLoading)
    }
}
