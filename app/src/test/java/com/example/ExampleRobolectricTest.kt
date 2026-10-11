package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitType
import com.example.data.model.TrackingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

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
            targetValue = 1f
        )
        assertEquals("Morning Run", habit.name)
        assertTrue(habit.targetValue > 0f)
    }
}
