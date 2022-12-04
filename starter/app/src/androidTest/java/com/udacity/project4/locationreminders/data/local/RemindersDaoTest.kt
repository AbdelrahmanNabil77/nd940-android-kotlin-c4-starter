package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @Test
    fun insertReminderGetById() = runBlockingTest {
        val reminder = ReminderDTO("title1", "desc1", "zagazig", 30.58, 31.49)
        database.reminderDao().saveReminder(reminder)
        val retrievedReminder = database.reminderDao().getReminderById(reminder.id) as ReminderDTO
        assertNotEquals(null, retrievedReminder)
        assertEquals(reminder.id, retrievedReminder.id)
        assertEquals(reminder.title, retrievedReminder.title)
        assertEquals(reminder.description, retrievedReminder.description)
        assertEquals(reminder.location, retrievedReminder.location)
        assertEquals(reminder.latitude, retrievedReminder.latitude)
        assertEquals(reminder.longitude, retrievedReminder.longitude)
    }

    fun insertRemindersGetAll() = runBlockingTest {
        val reminder1 = ReminderDTO("title1", "desc1", "zagazig", 30.58, 31.49)
        val reminder2 = ReminderDTO("title2", "desc2", "zagazig", 30.58, 31.49)
        val reminder3 = ReminderDTO("title3", "desc3", "zagazig", 30.58, 31.49)
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        val retrievedReminders = database.reminderDao().getReminders()
        assertEquals(3,retrievedReminders.size)
    }

    @Test
    fun return_no_data_error() = runBlockingTest {
        val reminder = ReminderDTO("title1", "desc1", "zagazig", 30.58, 31.49)
        database.reminderDao().saveReminder(reminder)
        val randomId = "777"
        val retrievedReminder = database.reminderDao().getReminderById(randomId)

        assertEquals(null,retrievedReminder)

    }

    @After
    fun closeDatabase() = database.close()

}