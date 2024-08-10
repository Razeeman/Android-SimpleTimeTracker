package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.setPickerTime
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
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
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsSplitTest : BaseUiTest() {

    @Test
    fun changeSplitTime() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Add data
        val timeStartedTimestamp = calendar.getMillis(hour = 15, minute = 16)
        val timeEndedTimestamp = calendar.getMillis(hour = 17, minute = 18)
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, timeStarted = timeStartedTimestamp, timeEnded = timeEndedTimestamp)

        // Open record
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Check new time set
        val newHour = 16
        val newMinute = 30
        clickOnViewWithId(changeRecordR.id.tvChangeRecordTimePreviewItem)
        setPickerTime(newHour, newMinute)
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        var timePreview = calendar.getMillis(newHour, newMinute).formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))

        // Check time adjust
        clickOnAdjustment("-5")
        timePreview = calendar.apply { add(Calendar.MINUTE, -5) }.timeInMillis.formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))

        clickOnAdjustment("+5")
        timePreview = calendar.apply { add(Calendar.MINUTE, +5) }.timeInMillis.formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))
    }

    @Test
    fun splitTimeLimits() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Add data
        var timeStartedTimestamp = calendar.getMillis(15, 0)
        val timeEndedTimestamp = calendar.getMillis(16, 0)
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, timeStarted = timeStartedTimestamp, timeEnded = timeEndedTimestamp)

        // Check record limits
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        repeat(4) { clickOnAdjustment("-30") }
        var timePreview = timeStartedTimestamp.formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))
        repeat(4) { clickOnAdjustment("+30") }
        timePreview = timeEndedTimestamp.formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))
        pressBack()
        pressBack()

        // Check running record limits
        timeStartedTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        testUtils.addRunningRecord(typeName = name, timeStarted = timeStartedTimestamp)
        NavUtils.openRunningRecordsScreen()
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        repeat(4) { clickOnAdjustment("-30") }
        timePreview = timeStartedTimestamp.formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))
        repeat(4) { clickOnAdjustment("+30") }
        timePreview = System.currentTimeMillis().formatDateTime()
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimePreviewItem), withText(timePreview)))
    }

    @Test
    fun recordSplit() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullName = "$name - $tag"
        val calendar = Calendar.getInstance()

        // Add data
        val timeStartedTimestamp = calendar.getMillis(hour = 15, minute = 16)
        val timeEndedTimestamp = calendar.getMillis(hour = 17, minute = 18)
        var timeStartedPreview = timeStartedTimestamp.formatTime()
        var timeEndedPreview = timeEndedTimestamp.formatTime()
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp).formatInterval()

        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = name,
            timeStarted = timeStartedTimestamp,
            timeEnded = timeEndedTimestamp,
            tagNames = listOf(tag),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        checkRecord(fullName, timeStartedPreview, timeEndedPreview, timeRangePreview, comment)

        // Check time split set to time started
        clickOnViewWithText(fullName)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        timeStartedPreview = timeStartedTimestamp.formatDateTime()
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordR.id.tvChangeRecordTimePreviewItem),
                withText(timeStartedPreview),
            ),
        )

        // Divide
        clickOnAdjustment("+30")
        clickOnViewWithText(coreR.string.change_record_split)

        // Check that two records created
        val difference = TimeUnit.MINUTES.toMillis(30)
        timeStartedPreview = timeStartedTimestamp.formatTime()
        timeEndedPreview = (timeStartedTimestamp + difference).formatTime()
        timeRangePreview = difference.formatInterval()

        checkRecord(fullName, timeStartedPreview, timeEndedPreview, timeRangePreview, comment)

        timeStartedPreview = (timeStartedTimestamp + difference).formatTime()
        timeEndedPreview = timeEndedTimestamp.formatTime()
        timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp - difference).formatInterval()

        checkRecord(fullName, timeStartedPreview, timeEndedPreview, timeRangePreview, comment)
    }

    @Test
    fun recordUntrackedSplit() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(typeName = name, timeStarted = yesterday, timeEnded = yesterday)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        // Open untracked time
        onView(
            allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()),
        ).check(
            recyclerItemCount(2),
        )
        clickOnViewWithText(coreR.string.untracked_time_name)

        // Split untracked doesn't work
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        clickOnViewWithText(coreR.string.change_record_split)
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Split
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        clickOnAdjustment("+1")
        clickOnViewWithText(coreR.string.change_record_split)

        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(
            allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()),
        ).check(
            recyclerItemCount(3),
        )
    }

    @Test
    fun recordNewSplit() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        NavUtils.openRecordsScreen()

        // Open untracked time
        onView(
            allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()),
        ).check(
            recyclerItemCount(1),
        )
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Split untracked doesn't work
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        clickOnViewWithText(coreR.string.change_record_split)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Split
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        clickOnViewWithText(coreR.string.change_record_split)

        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(
            allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()),
        ).check(
            recyclerItemCount(3),
        )
    }

    @Test
    fun runningRecordSplit() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullName = "$name - $tag"

        // Add data
        val currentTime = System.currentTimeMillis()
        var timeStartedPreview = currentTime.formatTime()

        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRunningRecord(
            typeName = name,
            timeStarted = currentTime,
            tagNames = listOf(tag),
            comment = comment,
        )
        Thread.sleep(1000)

        // Check record
        tryAction { checkRunningRecord(fullName, timeStartedPreview, comment) }
        longClickOnView(withText(fullName))
        clickOnViewWithText("-30")
        clickOnViewWithText("-30")

        // Check time split set to time started
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        timeStartedPreview = currentTime.formatDateTime()
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordR.id.tvChangeRecordTimePreviewItem),
                withText(timeStartedPreview),
            ),
        )

        // Divide
        clickOnAdjustment("-30")
        clickOnViewWithText(coreR.string.change_record_split)

        // Check that two records created
        val difference = TimeUnit.MINUTES.toMillis(30)
        timeStartedPreview = (currentTime - difference).formatTime()
        checkRunningRecord(fullName, timeStartedPreview, comment)
        NavUtils.openRecordsScreen()
        timeStartedPreview = (currentTime - 2 * difference).formatTime()
        val timeEndedPreview = (currentTime - difference).formatTime()
        val timeRangePreview = difference.formatInterval()
        checkRecord(fullName, timeStartedPreview, timeEndedPreview, timeRangePreview, comment)
    }

    private fun clickOnAdjustment(text: String) {
        clickOnView(
            allOf(
                isDescendantOfA(
                    allOf(
                        withTag(ChangeRecordActionsBlock.SplitTimeAdjustment),
                        withId(changeRecordR.id.containerChangeRecordTimeAdjustmentItem),
                    ),
                ),
                withText(text),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRecord(
        name: String,
        timeStartedPreview: String,
        timeEndedPreview: String,
        timeRangePreview: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRunningRecord(
        name: String,
        timeStartedPreview: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }
}
