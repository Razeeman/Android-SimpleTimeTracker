package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseUiTest() {

    @Test
    fun showUntrackedSetting() {
        val name = "Test"
        val color = ColorMapper.availableColors.first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        NavUtils.addActivity(name, color, icon)

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Add record
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name)
        checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun allowMultitaskingSetting() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)
        NavUtils.addActivity(name3)

        // Start timers
        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        clickOnViewWithText(name3)
        var startTime = System.currentTimeMillis().let(timeMapper::formatTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name1)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(startTime))
            )
        )

        // Click on already running
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRecordTypeItem)), withText(name1))
        )
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name3), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))

        // Click on already running
        NavUtils.openRunningRecordsScreen()
        Thread.sleep(1000)
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRecordTypeItem)), withText(name1))
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name1)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.layoutRunningRecordItem), hasDescendant(withText(name2)))
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.layoutRunningRecordItem), hasDescendant(withText(name3)))
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRecordTypeItem)), withText(name2))
        )
        startTime = System.currentTimeMillis().let(timeMapper::formatTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.layoutRunningRecordItem), hasDescendant(withText(name1)))
        )

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))

        // Change setting back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        Thread.sleep(1000)
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRecordTypeItem)), withText(name3))
        )
        val newStartTime = System.currentTimeMillis().let(timeMapper::formatTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(newStartTime))
            )
        )

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
    }
}
