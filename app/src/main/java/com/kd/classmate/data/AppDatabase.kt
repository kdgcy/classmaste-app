package com.kd.classmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter // NEW IMPORT for TypeConverter functions
import androidx.room.TypeConverters
import com.kd.classmate.Converters
import com.kd.classmate.data.subtaskdata.Subtask


//New Type Converter for the Enum
class TaskTypeConverter {
    @TypeConverter
    fun fromTaskType(value: TaskType): String {
        return value.name
    }

    @TypeConverter
    fun toTaskType(value: String): TaskType {
        return TaskType.valueOf(value)
    }
}

@TypeConverters(Converters::class, TaskTypeConverter::class)
@Database(entities = [Task::class, Subtask::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): com.kd.classmate.data.subtaskdata.SubtaskDao

    // --- Singleton Setup ---
    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "classmate_database")
                    // Destructive migration is required for version change
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}