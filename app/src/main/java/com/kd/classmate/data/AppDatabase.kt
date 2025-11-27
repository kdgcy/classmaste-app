// File: AppDatabase.kt (CRASH FIX)

package com.kd.classmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kd.classmate.data.subtaskdata.Subtask // Ensure this import is correct

/**
 * The Room Database for the Classmate app.
 * Version 2 now includes the Subtask entity.
 */
// Ensure entities and version are correct
@Database(entities = [Task::class, Subtask::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): com.kd.classmate.data.subtaskdata.SubtaskDao // Use full path for SubtaskDao

    // --- Singleton Setup ---
    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "classmate_database")
                    // 💥 FIX: Add the destructive migration fallback here 💥
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}