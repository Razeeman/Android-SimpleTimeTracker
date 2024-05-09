package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
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
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
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
class RecordActionsContinueTest : BaseUiTest() {

    @Test
    fun continueRecord() {
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
        val timeRangePreview = (current - timeStartedTimestamp).formatInterval()

        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = name,
            timeStarted = timeStartedTimestamp,
            timeEnded = current,
            tagNames = listOf(tag),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        checkRecord(fullName, timeStartedPreview, timeEndedPreview, timeRangePreview, comment)

        // Continue
        clickOnViewWithText(fullName)
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        // Check no record
        checkViewDoesNotExist(
            allOf(withText(fullName), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )

        // Check running record
        NavUtils.openRunningRecordsScreen()
        checkRunningRecord(fullName, timeStartedPreview, comment)
    }

    @Test
    fun continueUntrackedRecord() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val startOfDay = calendar.getMillis(0, 0)
        val timeStartedPreview = startOfDay.formatTime()
        val timeEndedPreview = current.formatTime()
        val timeRangePreview = (current - startOfDay).formatInterval()

        testUtils.addActivity(name)
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(typeName = name, timeStarted = yesterday, timeEnded = yesterday)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        // Open untracked time
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed(),
            ),
        )
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(2))
        clickOnViewWithText(coreR.string.untracked_time_name)

        // Continue untracked doesn't work
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Continue
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(2))

        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun continueNewRecord() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val difference = TimeUnit.HOURS.toMillis(1)
        val timeStartedTimestamp = current - difference
        val timeStartedPreview = timeStartedTimestamp.formatTime()

        testUtils.addActivity(name)
        NavUtils.openRecordsScreen()

        // Open add new record
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(1))
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Continue untracked doesn't work
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        clickOnViewWithText(coreR.string.change_record_actions_hint)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Continue
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        checkViewDoesNotExist(
            allOf(
                withText(name),
                isDescendantOfA(withId(baseR.id.viewRecordItem)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                isCompletelyDisplayed(),
            ),
        )
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(2))

        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedPreview)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun continueFutureRecord() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis

        testUtils.addActivity(name)
        testUtils.addRecord(
            typeName = name,
            timeStarted = current + TimeUnit.MINUTES.toMillis(5),
            timeEnded = current + TimeUnit.HOURS.toMillis(1),
        )
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }

        // Try continue record
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())
        // Snackbar is in the way of Add button
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        pressBack()
        pressBack()

        fun adjust(text: String) {
            val containerId = changeRecordR.id.containerChangeRecordTimeStartedAdjust
            clickOnView(allOf(isDescendantOfA(withId(containerId)), withText(text)))
        }

        // Try continue from add record
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        adjust("+30")
        adjust("+30")
        adjust("+5")
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        // Still on edit screen
        checkViewIsDisplayed(withText(coreR.string.change_record_save))
    }

    @Test
    fun continueRunningRecord() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        Thread.sleep(1000)
        tryAction {
            clickOnView(
                allOf(withId(baseR.id.viewRecordTypeItem), hasDescendant(withText(name))),
            )
        }
        tryAction {
            longClickOnView(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
            )
        }

        // Try continue record
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        checkViewIsNotDisplayed(withText(coreR.string.change_record_continue))
    }

    @Test
    fun continueRecordSameAsAlreadyRunning() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRunningRecord(name)
        NavUtils.openRecordsScreen()

        // Continue
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        // Running record stopped
        checkViewIsDisplayed(
            allOf(withText(name), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed()),
        )

        // New running record
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
        )
    }

    @Test
    fun continueRecordWhileMultitaskingDisabled() {
        val name1 = "Name1"
        val name2 = "Name2"

        // Setup
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRunningRecord(name1)
        runBlocking { prefsInteractor.setAllowMultitasking(false) }
        NavUtils.openRecordsScreen()

        // Continue
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name2))
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        onView(withText(coreR.string.change_record_continue)).perform(nestedScrollTo(), click())

        // Running record stopped
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))

        // New running record
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(
            allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name2)), isCompletelyDisplayed()),
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

    private fun Calendar.getMillis(hour: Int, minute: Int): Long {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        return timeInMillis
    }
}
