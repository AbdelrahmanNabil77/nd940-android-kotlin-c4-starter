package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDao(var reminders:MutableList<ReminderDTO> = mutableListOf()) : RemindersDao {

    //    TODO: Create a fake data source to act as a double to the real data source
    override suspend fun getReminders(): List<ReminderDTO> {
        return  reminders.toList()
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        val reminderDTO=reminders.filter { it.id == reminderId}
        return if (reminderDTO.size==1){
            reminderDTO.get(0)
        }else{
            null
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}