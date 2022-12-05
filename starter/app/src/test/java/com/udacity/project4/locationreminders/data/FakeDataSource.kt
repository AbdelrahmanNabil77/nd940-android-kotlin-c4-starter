package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return com.udacity.project4.locationreminders.data.dto.Result.Error("Testing error get reminders")
        }
        return Result.Success(reminders.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return com.udacity.project4.locationreminders.data.dto.Result.Error("Testing error get reminder")
        }
        var reminderDTO = reminders.filter { it.id == id }
        return if (reminderDTO.size == 1) {
            Result.Success(reminderDTO.get(0))
        } else {
            Result.Error("not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}