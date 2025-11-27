package com.kd.classmate.data.subtaskdata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Query("SELECT * FROM subtaskTable WHERE parentTaskId = :parentTaskId ORDER BY id ASC")
    fun getSubtasksForTask(parentTaskId: Int): Flow<List<Subtask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: Subtask)

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    // Delete functionality will be added in a later RUD step
}