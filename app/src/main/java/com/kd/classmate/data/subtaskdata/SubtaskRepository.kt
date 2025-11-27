package com.kd.classmate.data.subtaskdata

import kotlinx.coroutines.flow.Flow

class SubtaskRepository(private val subtaskDao: SubtaskDao) {

    fun getSubtasksForTask(parentTaskId: Int): Flow<List<Subtask>> {
        return subtaskDao.getSubtasksForTask(parentTaskId)
    }

    suspend fun insertSubtask(subtask: Subtask) {
        subtaskDao.insertSubtask(subtask)
    }

    suspend fun updateSubtask(subtask: Subtask) {
        subtaskDao.updateSubtask(subtask)
    }

    // NEW: Delete Operation
    suspend fun deleteSubtask(subtask: Subtask) {
        subtaskDao.deleteSubtask(subtask)
    }
}