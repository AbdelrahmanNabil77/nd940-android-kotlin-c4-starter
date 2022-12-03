package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.FakeDao
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    //    TODO: Add testing implementation to the RemindersLocalRepository.kt
    val reminderItem1 = ReminderDTO(
        "title1", "desc1",
        "location1", 1.1, 1.1, "id1"
    )
    val reminderItem2 = ReminderDTO(
        "title2", "desc2",
        "location2", 2.2, 2.2, "id2"
    )
    val reminderItem3 = ReminderDTO(
        "title3", "desc3",
        "location3", 3.3, 3.3, "id3"
    )


    private lateinit var remindersList: List<ReminderDTO>
    private lateinit var remindersDao: FakeDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun createRepository() {
        remindersList = listOf(reminderItem1, reminderItem2, reminderItem3)

        remindersDao = FakeDao(remindersList.toMutableList())

        remindersLocalRepository = RemindersLocalRepository(
            remindersDao,
            Dispatchers.Unconfined
        )
    }

    @Test
    fun getTask_reqestAllReminders() = runBlockingTest {
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(remindersList, reminders.data)
    }

    @Test
    fun deleteTask_deleteAllReminders() = runBlockingTest {
        remindersLocalRepository.deleteAllReminders()
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(0, reminders.data.size)
    }

    @Test
    fun saveTask_saveNewReminders() = runBlockingTest {
        remindersLocalRepository.saveReminder(
            ReminderDTO("title4","desc4",
            "location4",4.4,4.4)
        )
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(4, reminders.data.size)
    }

    @Test
    fun getTaskById_getOneReminder() = runBlockingTest {
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success
        val reminder=remindersLocalRepository.getReminder(reminders.data.get(0).id) as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(reminders.data[0], reminder.data)
    }

}