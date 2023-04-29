package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordAdjustTimeTest : BaseUiTest() {

    @Test
    fun adjustRecordTime() {
        val name1 = "Name1"
        val name2 = "Name2"
        val name3 = "Name3"
        val calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.getMillis(hour = 15, minute = 0),
            timeEnded = calendar.getMillis(hour = 16, minute = 0)
        )
        testUtils.addRecord(
            typeName = name2,
            timeStarted = calendar.getMillis(hour = 16, minute = 0),
            timeEnded = calendar.getMillis(hour = 18, minute = 0),
        )
        testUtils.addRecord(
            typeName = name3,
            timeStarted = calendar.getMillis(hour = 18, minute = 0),
            timeEnded = calendar.getMillis(hour = 21, minute = 0),
        )

        // Check records
        NavUtils.openRecordsScreen()
        checkRecord(
            name1,
            calendar.getMillis(hour = 15, minute = 0).formatTime(),
            calendar.getMillis(hour = 16, minute = 0).formatTime(),
        )
        checkRecord(
            name2,
            calendar.getMillis(hour = 16, minute = 0).formatTime(),
            calendar.getMillis(hour = 18, minute = 0).formatTime(),
        )
        checkRecord(
            name3,
            calendar.getMillis(hour = 18, minute = 0).formatTime(),
            calendar.getMillis(hour = 21, minute = 0).formatTime(),
        )

        // Change record
        longClickOnView(allOf(withText(name2), isCompletelyDisplayed()))
        clickOnViewWithText("-30")
        unconstrainedClickOnView(withId(changeRecordR.id.btnChangeRecordTimeEndedAdjust))
        tryAction { clickOnViewWithText("+30") }
        clickOnViewWithText("+30")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
        onView(withId(changeRecordR.id.containerChangeRecordAction)).perform(swipeUp())
        clickOnViewWithText(coreR.string.change_record_adjust)

        // Check records
        checkRecord(
            name1,
            calendar.getMillis(hour = 15, minute = 0).formatTime(),
            calendar.getMillis(hour = 15, minute = 30).formatTime(),
        )
        checkRecord(
            name2,
            calendar.getMillis(hour = 15, minute = 30).formatTime(),
            calendar.getMillis(hour = 19, minute = 0).formatTime(),
        )
        checkRecord(
            name3,
            calendar.getMillis(hour = 19, minute = 0).formatTime(),
            calendar.getMillis(hour = 21, minute = 0).formatTime(),
        )
    }

    @Test
    fun adjustUntrackedRecordTime() {
        val name1 = "Name1"
        val name2 = "Name2"
        val name3 = "Name3"
        val calendar = Calendar.getInstance()

        // Add data
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        val current = calendar.timeInMillis
        val timeEndedTimeStamp2 = current - TimeUnit.MINUTES.toMillis(1)
        val timeStartedTimeStamp2 = current - TimeUnit.MINUTES.toMillis(3)
        val timeEndedTimeStamp1 = current - TimeUnit.MINUTES.toMillis(6)
        val timeStartedTimeStamp1 = current - TimeUnit.MINUTES.toMillis(10)

        testUtils.addRecord(
            typeName = name3,
            timeStarted = timeStartedTimeStamp2,
            timeEnded = timeEndedTimeStamp2,
        )
        testUtils.addRecord(
            typeName = name1,
            timeStarted = timeStartedTimeStamp1,
            timeEnded = timeEndedTimeStamp1,
        )

        // Check records
        NavUtils.openRecordsScreen()
        checkRecord(
            name = name3,
            timeStartedPreview = timeStartedTimeStamp2.formatTime(),
            timeEndedPreview = timeEndedTimeStamp2.formatTime(),
        )
        checkRecord(
            name = getString(coreR.string.untracked_time_name),
            timeStartedPreview = timeEndedTimeStamp1.formatTime(),
            timeEndedPreview = timeStartedTimeStamp2.formatTime(),
        )
        checkRecord(
            name = name1,
            timeStartedPreview = timeStartedTimeStamp1.formatTime(),
            timeEndedPreview = timeEndedTimeStamp1.formatTime(),
        )

        // Change record
        longClickOnView(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText(timeEndedTimeStamp1.formatTime())),
                hasDescendant(withText(timeStartedTimeStamp2.formatTime())),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithText("-1")
        clickOnViewWithText("-1")
        unconstrainedClickOnView(withId(changeRecordR.id.btnChangeRecordTimeEndedAdjust))
        tryAction { clickOnViewWithText("+1") }
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name2))
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
        onView(withId(changeRecordR.id.containerChangeRecordAction)).perform(swipeUp())
        clickOnViewWithText(coreR.string.change_record_adjust)

        // Check records
        val newUntrackedTimeStarted = timeEndedTimeStamp1 - TimeUnit.MINUTES.toMillis(2)
        val newUntrackedTimeEnded = timeStartedTimeStamp2 + TimeUnit.MINUTES.toMillis(1)
        checkRecord(
            name = name3,
            timeStartedPreview = newUntrackedTimeEnded.formatTime(),
            timeEndedPreview = timeEndedTimeStamp2.formatTime(),
        )
        checkRecord(
            name = name2,
            timeStartedPreview = newUntrackedTimeStarted.formatTime(),
            timeEndedPreview = newUntrackedTimeEnded.formatTime(),
        )
        checkRecord(
            name = name1,
            timeStartedPreview = timeStartedTimeStamp1.formatTime(),
            timeEndedPreview = newUntrackedTimeStarted.formatTime(),
        )
    }

    @Test
    fun adjustRunningRecordTime() {
        val name1 = "Name1"
        val name2 = "Name2"
        val calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        val current = calendar.timeInMillis
        val timeStartedTimeStamp = (current - TimeUnit.HOURS.toMillis(5))

        testUtils.addRecord(
            typeName = name1,
            timeStarted = timeStartedTimeStamp,
            timeEnded = current,
        )
        testUtils.addRunningRecord(
            typeName = name2,
            timeStarted = current,
        )

        // Check record
        NavUtils.openRecordsScreen()
        checkRecord(
            name = name1,
            timeStartedPreview = timeStartedTimeStamp.formatTime(),
            timeEndedPreview = current.formatTime(),
        )
        NavUtils.openRunningRecordsScreen()
        checkRunningRecord(
            name = name2,
            timeStartedPreview = current.formatTime(),
        )

        // Change record
        longClickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name2), isCompletelyDisplayed())
        )
        clickOnViewWithText("-5")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
        onView(withId(changeRecordR.id.containerChangeRecordAction)).perform(swipeUp())
        clickOnViewWithText(coreR.string.change_record_adjust)

        // Check records
        val newTimeEnded = current - TimeUnit.MINUTES.toMillis(5)
        checkRunningRecord(
            name = name2,
            timeStartedPreview = newTimeEnded.formatTime(),
        )
        NavUtils.openRecordsScreen()
        checkRecord(
            name = name1,
            timeStartedPreview = timeStartedTimeStamp.formatTime(),
            timeEndedPreview = newTimeEnded.formatTime(),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRecord(
        name: String,
        timeStartedPreview: String,
        timeEndedPreview: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                isCompletelyDisplayed()
            )
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRunningRecord(
        name: String,
        timeStartedPreview: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                isCompletelyDisplayed()
            )
        )
    }
}
