package com.kd.classmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The Room Database for the Classmate app.
 * Version 1 corresponds to the current Task entity structure.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract function to expose the DAO
    abstract fun taskDao(): TaskDao

    // --- Singleton Setup ---
    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // If Instance is not null, return it; otherwise, create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "classmate_database")
                    /**
                     * Allowing main thread queries is discouraged for production.
                     * We remove it here as all DAO functions are now 'suspend' or return 'Flow'.
                     */
                    .build()
                    .also { Instance = it }
            }
        }
    }
}