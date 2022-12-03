package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {
    //TODO: provide testing to the SaveReminderView and its live data objects
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
    private lateinit var saveRemindersViewModel: SaveReminderViewModel

    @Before
    fun createSaveReminderViewModel() {
        remindersList = listOf(reminderItem1, reminderItem2, reminderItem3)
        remindersLocalRepository = FakeDataSource(remindersList.toMutableList())
        saveRemindersViewModel =
            SaveReminderViewModel(getApplicationContext(), remindersLocalRepository)
    }

    @Test
    fun validateEnteredData_nullTitle_False() {
        val isValid = saveRemindersViewModel.validateEnteredData(
            ReminderDataItem(
                null, "desc", "location",
                0.0, 0.0
            )
        )

        assertEquals(false, isValid)
    }

    @Test
    fun validateEnteredData_nullDesc_False() {
        val isValid = saveRemindersViewModel.validateEnteredData(
            ReminderDataItem(
                "title", null, "location",
                0.0, 0.0
            )
        )

        assertEquals(false, isValid)
    }

    @Test
    fun validateEnteredData_nullLocation_False() {
        val isValid = saveRemindersViewModel.validateEnteredData(
            ReminderDataItem(
                "title", "desc", null,
                0.0, 0.0
            )
        )

        assertEquals(false, isValid)
    }

    @Test
    fun validateEnteredData_allFilled_true() {
        val isValid = saveRemindersViewModel.validateEnteredData(
            ReminderDataItem(
                "title", "desc", "location",
                0.0, 0.0
            )
        )

        assertEquals(true, isValid)
    }

    @Test
    fun saveReminder_allFilled_reminderListFourItems() = runBlockingTest {
        saveRemindersViewModel.saveReminder(
            ReminderDataItem(
                "title", "desc", "location",
                0.0, 0.0
            )
        )

        val remindersList =
            remindersLocalRepository.getReminders() as com.udacity.project4.locationreminders.data.dto.Result.Success

        assertEquals(4, remindersList.data.size)
    }

    @After
    fun stopDown() {
        stopKoin()
    }
}