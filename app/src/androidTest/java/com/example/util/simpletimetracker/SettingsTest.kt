package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
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
}
