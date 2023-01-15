package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
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
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsMergeTest : BaseUiTest() {

    @Test
    fun mergeVisibility() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRecord(name)
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
            timeEnded = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
        )
        testUtils.addRunningRecord(name)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        Thread.sleep(1000)

        // Running record - not shown
        tryAction { longClickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name)))) }
        clickOnViewWithText(R.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(R.string.change_record_merge))
        pressBack()

        // Record - not shown
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(R.string.change_record_merge))
        pressBack()

        // New record - not shown
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(R.string.change_record_merge))
        pressBack()

        // Untracked and have prev record - shown
        clickOnView(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.change_record_actions_hint)
        onView(withText(R.string.change_record_merge)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.change_record_merge))
        pressBack()

        // Untracked and have no prev record - not shown
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnChangeRecordDelete)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        clickOnView(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(R.string.change_record_merge))
    }

    @Test
    fun merge() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val timeStartedTimestamp = current - TimeUnit.MINUTES.toMillis(15)
        val timeEndedTimestamp = current - TimeUnit.MINUTES.toMillis(5)
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp).formatInterval()
        val untrackedRangePreview = (current - timeEndedTimestamp).formatInterval()
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, timeStarted = timeStartedTimestamp, timeEnded = timeEndedTimestamp)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        // Check records
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordItem),
                    hasDescendant(withText(R.string.untracked_time_name)),
                    hasDescendant(withText(current.formatTime())),
                    hasDescendant(withText(timeEndedTimestamp.formatTime())),
                    hasDescendant(withText(untrackedRangePreview)),
                    isCompletelyDisplayed()
                )
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedTimestamp.formatTime())),
                hasDescendant(withText(timeEndedTimestamp.formatTime())),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed()
            )
        )

        // Merge
        clickOnView(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withText(untrackedRangePreview)),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithText(R.string.change_record_actions_hint)
        onView(withText(R.string.change_record_merge)).perform(nestedScrollTo(), click())

        // Check records
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withText(untrackedRangePreview)),
                isCompletelyDisplayed()
            )
        )
        timeRangePreview = (current - timeStartedTimestamp).formatInterval()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedTimestamp.formatTime())),
                hasDescendant(withText(current.formatTime())),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed()
            )
        )
    }
}
