package com.kd.classmate.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    // R - Read Operation (gets all tasks as a Flow)
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    // Change return type to Long
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    // U - Update Operation
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // D - Delete Operation
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // D - Delete By Id Operation
    suspend fun deleteById(taskId: Int) {
        taskDao.deleteById(taskId)
    }
}