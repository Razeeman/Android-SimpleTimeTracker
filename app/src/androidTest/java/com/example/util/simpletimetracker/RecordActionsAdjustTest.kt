package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsAdjustTest : BaseUiTest() {

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
            timeEnded = calendar.getMillis(hour = 16, minute = 0),
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
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
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
                isCompletelyDisplayed(),
            ),
        )
        adjust(isStart = true, buttonText = "-1")
        adjust(isStart = true, buttonText = "-1")
        adjust(isStart = false, buttonText = "+1")
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name2))
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
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
            allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name2), isCompletelyDisplayed()),
        )
        clickOnViewWithText("-5")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
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

    @Test
    fun adjustWithOverlap() {
        val name1 = "Name1"
        val name2 = "Name2"
        val name3 = "Name3"
        val calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        testUtils.addRecord(
            typeName = name1, timeStarted = calendar.getMillis(hour = 15), timeEnded = calendar.getMillis(hour = 16),
        )
        testUtils.addRecord(
            typeName = name2, timeStarted = calendar.getMillis(hour = 16), timeEnded = calendar.getMillis(hour = 17),
        )
        testUtils.addRecord(
            typeName = name3, timeStarted = calendar.getMillis(hour = 17), timeEnded = calendar.getMillis(hour = 18),
        )

        // Check records
        NavUtils.openRecordsScreen()
        checkRecord(name1, calendar.getMillis(hour = 15).formatTime(), calendar.getMillis(hour = 16).formatTime())
        checkRecord(name2, calendar.getMillis(hour = 16).formatTime(), calendar.getMillis(hour = 17).formatTime())
        checkRecord(name3, calendar.getMillis(hour = 17).formatTime(), calendar.getMillis(hour = 18).formatTime())

        // Change record
        longClickOnView(allOf(withText(name2), isCompletelyDisplayed()))
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_adjust)

        // Check records
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name1)),
                isCompletelyDisplayed(),
            ),
        )
        checkRecord(
            name2,
            calendar.getMillis(hour = 15, minute = 0).formatTime(),
            calendar.getMillis(hour = 18, minute = 0).formatTime(),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name3)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun adjustSelection() {
        val name1 = "Name1"
        val name2 = "Name2"
        val name3 = "Name3"
        val name4 = "Name4"
        val name5 = "Name5"
        val calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)
        testUtils.addActivity(name4)
        testUtils.addActivity(name5)

        testUtils.addRecord(
            typeName = name1, timeStarted = calendar.getMillis(hour = 15), timeEnded = calendar.getMillis(hour = 16),
        )
        testUtils.addRecord(
            typeName = name2, timeStarted = calendar.getMillis(hour = 16), timeEnded = calendar.getMillis(hour = 17),
        )
        testUtils.addRecord(
            typeName = name3, timeStarted = calendar.getMillis(hour = 17), timeEnded = calendar.getMillis(hour = 18),
        )
        testUtils.addRecord(
            typeName = name4, timeStarted = calendar.getMillis(hour = 18), timeEnded = calendar.getMillis(hour = 19),
        )
        testUtils.addRecord(
            typeName = name5, timeStarted = calendar.getMillis(hour = 19), timeEnded = calendar.getMillis(hour = 20),
        )

        // Check records
        NavUtils.openRecordsScreen()
        checkRecord(name1, calendar.getMillis(hour = 15).formatTime(), calendar.getMillis(hour = 16).formatTime())
        checkRecord(name2, calendar.getMillis(hour = 16).formatTime(), calendar.getMillis(hour = 17).formatTime())
        checkRecord(name3, calendar.getMillis(hour = 17).formatTime(), calendar.getMillis(hour = 18).formatTime())
        checkRecord(name4, calendar.getMillis(hour = 18).formatTime(), calendar.getMillis(hour = 19).formatTime())
        checkRecord(name5, calendar.getMillis(hour = 19).formatTime(), calendar.getMillis(hour = 20).formatTime())

        // Change record
        longClickOnView(allOf(withText(name3), isCompletelyDisplayed()))
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")

        fun onAdjustPreview(typeName: String): ViewInteraction {
            return onView(
                allOf(
                    withId(changeRecordR.id.checkChangeRecordPreviewItem),
                    hasSibling(
                        hasDescendant(withText(typeName)),
                    ),
                ),
            )
        }

        // Deselect
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        onAdjustPreview(name5).perform(nestedScrollTo())
        onAdjustPreview(name5).check(matches(isChecked()))

        onAdjustPreview(name4).perform(nestedScrollTo())
        onAdjustPreview(name4).check(matches(isChecked()))

        onAdjustPreview(name2).perform(nestedScrollTo())
        onAdjustPreview(name2).check(matches(isChecked()))
        onAdjustPreview(name2).perform(click())
        onAdjustPreview(name2).check(matches(isNotChecked()))

        onAdjustPreview(name1).perform(nestedScrollTo())
        onAdjustPreview(name1).check(matches(isChecked()))
        onAdjustPreview(name1).perform(click())
        onAdjustPreview(name1).check(matches(isNotChecked()))

        onView(withText(coreR.string.change_record_adjust)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_adjust)

        // Check records
        checkRecord(
            name1,
            calendar.getMillis(hour = 15, minute = 0).formatTime(),
            calendar.getMillis(hour = 16, minute = 0).formatTime(),
        )
        checkRecord(
            name2,
            calendar.getMillis(hour = 16, minute = 0).formatTime(),
            calendar.getMillis(hour = 17, minute = 0).formatTime(),
        )
        checkRecord(
            name3,
            calendar.getMillis(hour = 15, minute = 30).formatTime(),
            calendar.getMillis(hour = 19, minute = 30).formatTime(),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name4)),
                isCompletelyDisplayed(),
            ),
        )
        checkRecord(
            name5,
            calendar.getMillis(hour = 19, minute = 30).formatTime(),
            calendar.getMillis(hour = 20, minute = 0).formatTime(),
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
                isCompletelyDisplayed(),
            ),
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
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun adjust(
        isStart: Boolean,
        buttonText: String,
    ) {
        val containerId = if (isStart) {
            changeRecordR.id.containerChangeRecordTimeStartedAdjust
        } else {
            changeRecordR.id.containerChangeRecordTimeEndedAdjust
        }
        clickOnView(allOf(isDescendantOfA(withId(containerId)), withText(buttonText)))
    }
}
