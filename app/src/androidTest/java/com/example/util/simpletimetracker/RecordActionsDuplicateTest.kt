package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
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
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.nthChildOf
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
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
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsDuplicateTest : BaseUiTest() {

    @Test
    fun duplicateVisibility() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRecord(name)
        testUtils.addRunningRecord(name)
        Thread.sleep(1000)

        // Running record - not shown
        tryAction {
            longClickOnView(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed())
            )
        }
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(coreR.string.change_record_duplicate))
        pressBack()

        // Record - shown
        NavUtils.openRecordsScreen()
        clickOnView(
            allOf(withId(baseR.id.viewRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed())
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.change_record_duplicate))
    }

    @Test
    fun duplicateRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullName = "$name - $tag"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val difference = TimeUnit.MINUTES.toMillis(30)
        val timeStartedTimestamp = current - difference
        val timeStartedPreview = timeStartedTimestamp.formatTime()
        val timeEndedPreview = current.formatTime()
        val timeRangePreview = difference.formatInterval()

        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = name,
            timeStarted = timeStartedTimestamp,
            timeEnded = current,
            tagNames = listOf(tag),
            comment = comment
        )

        // Check record
        NavUtils.openRecordsScreen()
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(2))
        checkRecord(
            listOf(0),
            fullName,
            timeStartedPreview,
            timeEndedPreview,
            timeRangePreview,
            comment
        )

        // Continue
        clickOnViewWithText(fullName)
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo(), click())

        tryAction {
            onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
                .check(recyclerItemCount(3))
        }
        tryAction {
            checkRecord(
                listOf(0, 1),
                fullName,
                timeStartedPreview,
                timeEndedPreview,
                timeRangePreview,
                comment
            )
        }
    }

    @Test
    fun duplicateUntrackedRecord() {
        val name = "Name"
        val comment = "Some_comment"
        val calendar = Calendar.getInstance()

        // Setup
        testUtils.addActivity(name = name, color = firstColor, icon = firstIcon)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        val current = calendar.timeInMillis
        val startOfDay = calendar.getMillis(0, 0)
        val timeStartedPreview = startOfDay.formatTime()
        val timeEndedPreview = current.formatTime()
        val timeRangePreview = (current - startOfDay).formatInterval()

        // Open untracked time
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed()
            )
        )
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(2))
        clickOnViewWithText(coreR.string.untracked_time_name)

        // Duplicate untracked doesn't work
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo(), click())
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Duplicate
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo(), click())

        tryAction {
            onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
                .check(recyclerItemCount(3))
        }
        tryAction {
            checkRecord(
                listOf(0, 1),
                name,
                timeStartedPreview,
                timeEndedPreview,
                timeRangePreview,
                comment
            )
        }
    }

    @Test
    fun duplicateNewRecord() {
        val name = "Name"
        val comment = "Some_comment"
        val calendar = Calendar.getInstance()

        // Setup
        testUtils.addActivity(name = name, color = firstColor, icon = firstIcon)
        NavUtils.openRecordsScreen()

        val current = calendar.timeInMillis
        val difference = TimeUnit.HOURS.toMillis(1)
        val timeStartedTimestamp = current - difference
        val timeStartedPreview = timeStartedTimestamp.formatTime()
        val timeEndedPreview = current.formatTime()
        val timeRangePreview = difference.formatInterval()

        // Open add new record
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(1))
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Duplicate untracked doesn't work
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo(), click())
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Duplicate
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_duplicate)).perform(nestedScrollTo(), click())

        tryAction {
            onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
                .check(recyclerItemCount(3))
        }
        tryAction {
            checkRecord(
                listOf(0, 1),
                name,
                timeStartedPreview,
                timeEndedPreview,
                timeRangePreview,
                comment
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun checkRecord(
        indexes: List<Int>,
        name: String,
        timeStartedPreview: String,
        timeEndedPreview: String,
        timeRangePreview: String,
        comment: String,
    ) {
        indexes.forEach { index ->
            checkViewIsDisplayed(
                allOf(
                    nthChildOf(withId(recordsR.id.rvRecordsList), index),
                    withId(baseR.id.viewRecordItem),
                    withCardColor(firstColor),
                    hasDescendant(withText(name)),
                    hasDescendant(withTag(firstIcon)),
                    hasDescendant(withText(timeStartedPreview)),
                    hasDescendant(withText(timeEndedPreview)),
                    hasDescendant(withText(timeRangePreview)),
                    hasDescendant(withText(comment)),
                    isCompletelyDisplayed()
                )
            )
        }
    }
}
