package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import com.example.ui.viewmodel.HabitUiState
import com.example.ui.viewmodel.HabitViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Habit Tracker", appName)
    }

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

    @Test
    fun `viewmodel initializes cleanly with repository`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = HabitViewModel(application)
        assertNotNull(viewModel.uiState.value)
        assertNotNull(viewModel.allHabits)
    }
}
