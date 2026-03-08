package com.kd.classmate.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Correct: Flow will automatically emit new lists whenever the table changes
    @Query("SELECT * FROM taskTable ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    // Optimization: Standard Insert is fine, but ensure the Long return is used for scheduling
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM taskTable WHERE id = :taskId")
    suspend fun deleteById(taskId: Int)
}