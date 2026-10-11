package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.data.model.HabitNote
import com.example.data.model.HabitReminder
import com.example.data.model.HabitType
import com.example.data.model.TrackingType

class Converters {
    @TypeConverter
    fun fromHabitType(value: HabitType): String = value.name

    @TypeConverter
    fun toHabitType(value: String): HabitType = try {
        HabitType.valueOf(value)
    } catch (e: Exception) {
        HabitType.BUILD
    }

    @TypeConverter
    fun fromTrackingType(value: TrackingType): String = value.name

    @TypeConverter
    fun toTrackingType(value: String): TrackingType = try {
        TrackingType.valueOf(value)
    } catch (e: Exception) {
        TrackingType.YES_NO
    }
}

@Database(
    entities = [
        Habit::class,
        HabitLog::class,
        HabitNote::class,
        HabitReminder::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitNoteDao(): HabitNoteDao
    abstract fun habitReminderDao(): HabitReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
