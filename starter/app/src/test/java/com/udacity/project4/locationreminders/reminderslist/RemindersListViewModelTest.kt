package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    //TODO: provide testing to the RemindersListViewModel and its live data objects
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
    private lateinit var remindersLocalRepository: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun createSaveReminderViewModel() {
        remindersList = listOf(reminderItem1, reminderItem2, reminderItem3)
        remindersLocalRepository = FakeDataSource(remindersList.toMutableList())
        remindersListViewModel =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                remindersLocalRepository
            )
    }

    @Test
    fun showAndHideLoaderInLoadReminders_true() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertEquals(true, remindersListViewModel.showLoading.getOrAwaitValue())
        mainCoroutineRule.resumeDispatcher()
        assertEquals(false, remindersListViewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun clearReminders_zeroList() = runBlockingTest {
        remindersListViewModel.clearReminders()
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success
        assertEquals(0, reminders.data.size)
    }

    @Test
    fun getRemindersWithError_callErrorToDisplay() = runBlockingTest {
        remindersLocalRepository.setShouldReturnError(true)
        val reminders =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Error
        assertEquals("Testing error get reminders", reminders.message)
    }

    @Test
    fun getReminderWithError_callErrorToDisplay() = runBlockingTest {
        remindersLocalRepository.setShouldReturnError(true)
        val reminders =
            remindersLocalRepository.getReminder("no_id") as com.udacity.project4.locationreminders.data.dto.Result.Error
        assertEquals("Testing error get reminder", reminders.message)
    }

    @After
    fun stopDown() {
        stopKoin()
    }

}