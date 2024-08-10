package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordsOptionsTest : BaseUiTest() {

    @Test
    fun buttonsVisibility() {
        NavUtils.openRecordsScreen()

        // Check not visible
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerOptions))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerFilter))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerShare))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))

        // Show options
        clickOnViewWithId(recordsR.id.btnRecordsContainerOptions)
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerOptions))
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerFilter))
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerShare))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))

        // Switch tabs
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()

        // Still shown
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerOptions))
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerFilter))
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerShare))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))

        // Hide options
        clickOnViewWithId(recordsR.id.btnRecordsContainerOptions)
        checkViewIsDisplayed(withId(recordsR.id.btnRecordsContainerOptions))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerFilter))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerShare))
        checkViewIsNotDisplayed(withId(recordsR.id.btnRecordsContainerCalendarSwitch))
    }

    @Test
    fun filterType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name1, timeStarted = before, timeEnded = before)
        testUtils.addRecord(name2, timeStarted = before, timeEnded = before)

        // All records displayed
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))

        // Filter untracked
        clickOnViewWithId(recordsR.id.btnRecordsContainerOptions)
        clickOnViewWithId(recordsR.id.btnRecordsContainerFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(coreR.string.untracked_time_name)),
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))

        // Filter activity
        clickOnViewWithId(recordsR.id.btnRecordsContainerFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))

        // Filter all
        clickOnViewWithId(recordsR.id.btnRecordsContainerFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithId(recordsR.id.btnRecordsContainerFilter)
        clickOnViewWithText(coreR.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))

        // Hide all
        clickOnViewWithId(recordsR.id.btnRecordsContainerFilter)
        clickOnViewWithText(coreR.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
    }

    @Test
    fun showRecordsCalendar() {
        val name = "Test"

        // Add data
        runBlocking { prefsInteractor.setShowCalendarButtonOnRecordsTab(true) }
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Record is shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        clickOnViewWithId(recordsR.id.btnRecordsContainerOptions)
        clickOnViewWithId(recordsR.id.btnRecordsContainerCalendarSwitch)

        // Record is not shown
        checkViewIsDisplayed(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        clickOnViewWithId(recordsR.id.btnRecordsContainerCalendarSwitch)

        // Record is shown
        checkViewDoesNotExist(allOf(withId(recordsR.id.viewRecordsCalendar), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(recordsR.id.tvRecordsCalendarHint), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }
}
