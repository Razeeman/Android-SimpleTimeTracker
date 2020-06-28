package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.typeTextIntoView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)

        testUtils.clearDatabase()
    }

    @Test
    fun test() {
        val name = "Test"

        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        pressBack()
        clickOnView(withText(R.string.change_record_type_save))
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))

        NavUtils.openRecordsScreen()

        onView(withId(R.id.btnRecordsContainerPrevious)).perform(click())
        onView(withId(R.id.btnRecordsContainerPrevious)).perform(click())
        onView(withId(R.id.btnRecordsContainerToday)).perform(longClick())
        onView(withId(R.id.btnRecordsContainerNext)).perform(click())
        onView(withId(R.id.btnRecordsContainerNext)).perform(click())

        onView(withId(R.id.btnRecordAdd)).perform(click())
        pressBack()

        NavUtils.openStatisticsScreen()

        onView(withId(R.id.btnStatisticsContainerPrevious)).perform(click())
        onView(withId(R.id.btnStatisticsContainerPrevious)).perform(click())
        onView(withId(R.id.btnStatisticsContainerToday)).perform(longClick())
        onView(withId(R.id.btnStatisticsContainerNext)).perform(click())
        onView(withId(R.id.btnStatisticsContainerNext)).perform(click())

        NavUtils.openSettingsScreen()
    }
}
