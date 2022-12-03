package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
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
    fun setup(){
        remindersList = listOf(reminderItem1, reminderItem2, reminderItem3)
        remindersLocalRepository = FakeDataSource(remindersList.toMutableList())
        remindersListViewModel =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                remindersLocalRepository
            )
        stopKoin()
        val myModule = module {
            single {
                remindersListViewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }
//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    @Test
    fun reminderList_displayedUI(){
        val reminderDataItem = ReminderDataItem("title","disc","loc",
        0.0,0.0)
    launchFragmentInContainer <ReminderListFragment>(Bundle(),R.style.AppTheme)
    Thread.sleep(2000)
    }
}