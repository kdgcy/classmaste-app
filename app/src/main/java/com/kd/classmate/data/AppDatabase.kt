package com.kd.classmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kd.classmate.Converters
import com.kd.classmate.data.subtaskdata.Subtask

@TypeConverters(Converters::class)
// 2. Add Subtask::class and INCREMENT VERSION to 3
@Database(entities = [Task::class, Subtask::class], version = 3, exportSchema = false)
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
                    // Destructive migration is required for version change 2 -> 3
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}