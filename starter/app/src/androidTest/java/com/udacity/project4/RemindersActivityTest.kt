package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var remindersActivity: RemindersActivity

    @get:Rule
    var activityTestRule: ActivityTestRule<RemindersActivity> =
        ActivityTestRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        remindersActivity = activityTestRule.activity
        repository = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun saveReminder_showToast_clearReminders_noDataView() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("title1"))
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("desc1"))
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Thread.sleep(2000)
        Espresso.onView(withId(R.id.map)).perform(ViewActions.longClick())
        Espresso.onView(withId(R.id.confirm_poi_button)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withText(R.string.reminder_saved))
            .inRoot(RootMatchers.withDecorView(CoreMatchers.not(remindersActivity.window.decorView)))
            .check(
                ViewAssertions.matches(isDisplayed())
            )
        Espresso.onView(withId(R.id.clearRemindersFAB)).perform(ViewActions.click())
        Espresso.onView(withText(R.string.no_data)).check(ViewAssertions.matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun checkSnackBar_test() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        Espresso.onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_title)))
        Thread.sleep(4000)
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("title1"))
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_enter_desc)))
        Thread.sleep(4000)
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("desc1"))
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.snackbar_text))
            .check(ViewAssertions.matches(withText(R.string.err_select_location)))
        activityScenario.close()
    }

    @After
    fun stopDown() {
        stopKoin()
    }
}
