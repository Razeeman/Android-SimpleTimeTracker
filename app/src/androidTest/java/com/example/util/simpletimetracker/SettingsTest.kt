package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
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
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseUiTest() {

    @Test
    fun showUntrackedSetting() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        NavUtils.addActivity(name, color, icon)

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
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
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
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
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name1)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(startTime))
            )
        )

        // Click on already running
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))
        )
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name3), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))

        // Click on already running
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name1)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3)))
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
        startTime = System.currentTimeMillis().let(timeMapper::formatTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1)))
        )

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))

        // Change setting back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))
        )
        val newStartTime = System.currentTimeMillis().let(timeMapper::formatTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name2)),
                hasDescendant(withText(startTime))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
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

    @Test
    fun cardSizeTest() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)
        NavUtils.addActivity(name3)

        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Open settings
        NavUtils.openSettingsScreen()
        onView(withText(R.string.settings_change_card_size)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.settings_change_card_size)
        Thread.sleep(1000)

        // Check order
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Change setting
        clickOnViewWithText("6")
        clickOnViewWithText("5")
        clickOnViewWithText("4")
        clickOnViewWithText("3")
        clickOnViewWithText("2")
        clickOnViewWithText("1")

        // Check new order
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Change back
        NavUtils.openSettingsScreen()
        onView(withText(R.string.settings_change_card_size)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.settings_change_card_size)
        Thread.sleep(1000)
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyAbove(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))
        clickOnViewWithText(R.string.card_size_default)

        // Check order
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))
        pressBack()
        NavUtils.openRunningRecordsScreen()
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))
    }

    @Test
    fun cardOrderByName() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = ColorMapper.getAvailableColors().first()
        val color2 = ColorMapper.getAvailableColors().last()

        // Add activities
        NavUtils.addActivity(name1, color2)
        NavUtils.addActivity(name2, color1)

        // Check order
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))

        // Check settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                isDescendantOfA(withId(R.id.spinnerSettingsRecordTypeSort)),
                withId(R.id.tvCustomSpinner),
                withText(R.string.settings_sort_by_name)
            )
        )
    }

    @Test
    fun cardOrderByColor() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = ColorMapper.getAvailableColors().first()
        val color2 = ColorMapper.getAvailableColors().last()

        // Add activities
        NavUtils.addActivity(name1, color2)
        NavUtils.addActivity(name2, color1)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_by_color)

        // Check new order
        NavUtils.openRunningRecordsScreen()
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))))
    }

    @Test
    fun cardOrderManual() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)
        NavUtils.addActivity(name3)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)

        // Check old order
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Drag
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .perform(drag(Direction.LEFT, 300))

        // Check new order
        pressBack()
        NavUtils.openRunningRecordsScreen()
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))

        // Change order
        NavUtils.openSettingsScreen()
        onView(withId(R.id.btnCardOrderManual)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.btnCardOrderManual)
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .perform(drag(Direction.RIGHT, 300))

        // Check new order
        pressBack()
        NavUtils.openRunningRecordsScreen()
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))))
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3)))
            .check(isCompletelyLeftOf(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))))
    }

    @Test
    fun enableNotifications() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)

        // Start one timer
        clickOnViewWithText(name1)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))

        // Stop first timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Start another timer
        clickOnViewWithText(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
    }

    @Test
    fun enableEnableDarkMode() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)

        // Start one timer
        clickOnViewWithText(name1)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()
        NavUtils.openSettingsScreen()
    }
}
