package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnSpinnerWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.slowHalfSwipe
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_running_records.R as runningRecordsR
import com.example.util.simpletimetracker.feature_settings.R as settingsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsCardOrder : BaseUiTest() {

    @Test
    fun cardSizeTest() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        tryAction { checkOrder(name1, name2, ::isCompletelyLeftOf) }
        checkOrder(name2, name3, ::isCompletelyLeftOf)

        // Open settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)

        // Check order
        checkOrder(name1, name2, ::isCompletelyLeftOf)
        checkOrder(name2, name3, ::isCompletelyLeftOf)

        // Change setting
        clickOnViewWithText("6")
        clickOnViewWithText("5")
        clickOnViewWithText("4")
        clickOnViewWithText("3")
        clickOnViewWithText("2")
        clickOnViewWithText("1")

        // Check new order
        checkOrder(name1, name2, ::isCompletelyAbove)
        checkOrder(name2, name3, ::isCompletelyAbove)

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        checkOrder(name1, name2, ::isCompletelyAbove)
        checkOrder(name2, name3, ::isCompletelyAbove)

        // Change back
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)
        checkOrder(name1, name2, ::isCompletelyAbove)
        checkOrder(name2, name3, ::isCompletelyAbove)
        clickOnViewWithText(coreR.string.card_size_default)

        // Check order
        checkOrder(name1, name2, ::isCompletelyLeftOf)
        checkOrder(name2, name3, ::isCompletelyLeftOf)
        pressBack()
        NavUtils.openRunningRecordsScreen()
        checkOrder(name1, name2, ::isCompletelyLeftOf)
        checkOrder(name2, name3, ::isCompletelyLeftOf)
    }

    @Test
    fun cardOrderByName() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = firstColor
        val color2 = lastColor

        // Add activities
        testUtils.addActivity(name = name1, color = color2)
        testUtils.addActivity(name = name2, color = color1)

        // Check order
        tryAction { checkOrder(name1, name2, ::isCompletelyLeftOf) }

        // Check settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(settingsR.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsRecordTypeSortValue), withText(coreR.string.settings_sort_by_name))
        )
    }

    @Test
    fun cardOrderByColor() {
        val name = "Test"

        val colors = ColorMapper.getAvailableColors()
        val black = colors.first()
        val blueGrey = colors.last()

        // Restore color color by moving some colors.
        val colorMap = colors.drop(1).dropLast(1)
            .map {
                it to false
            }
            .toMutableList()
            .apply {
                add(2, 0xffff00fc.toInt() to true) // custom color hue 300
                add(7, blueGrey to false)
                add(11, 0xff34664d.toInt() to true) // custom color hsv 150, 49, 40
                add(12, 0xff418061.toInt() to true) // custom color hsv 150, 49, 50
                add(13, 0xff4e9974.toInt() to true) // custom color hsv 150, 49, 60
                add(14, 0xff80ffc0.toInt() to true) // custom color hsv 150, 49, 100
                add(15, 0xff00ff81.toInt() to true) // custom color hsv 150, 100, 100
                add(21, 0xffffae00.toInt() to true) // custom color hue 40
                add(black to false)
            }.mapIndexed { index, color ->
                index to color
            }

        // Add activities
        colorMap.shuffled().forEach { (index, color) ->
            val colorId = color.first.takeUnless { color.second }
            val colorInt = color.first.takeIf { color.second }
            testUtils.addActivity(name = name + index, color = colorId, colorInt = colorInt)
        }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        NavUtils.openCardSizeScreen()
        clickOnViewWithText("1")
        pressBack()

        clickOnSpinnerWithId(settingsR.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(coreR.string.settings_sort_by_color)
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsRecordTypeSortValue), withText(coreR.string.settings_sort_by_color))
        )

        // Check new order
        NavUtils.openRunningRecordsScreen()

        colorMap.forEach { (index, _) ->
            if (index == 0) return@forEach

            val currentItem = name + index
            val previousItem = name + (index - 1)

            try {
                checkOrder(previousItem, currentItem, ::isCompletelyAbove)
            } catch (e: Throwable) {
                onView(withId(runningRecordsR.id.rvRunningRecordsList)).perform(slowHalfSwipe())
                tryAction { checkOrder(previousItem, currentItem, ::isCompletelyAbove) }
            }
        }
    }

    @Test
    fun cardOrderManual() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name3)
        testUtils.addActivity(name2)
        testUtils.addActivity(name1)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(coreR.string.settings_sort_manually)
        Thread.sleep(1000)

        // Check old order
        checkOrder(name1, name2, ::isCompletelyLeftOf)
        checkOrder(name2, name3, ::isCompletelyLeftOf)

        // Drag
        onView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
            .perform(drag(Direction.LEFT, 300))

        // Check new order
        pressBack()
        checkViewIsDisplayed(
            allOf(withId(settingsR.id.tvSettingsRecordTypeSortValue), withText(coreR.string.settings_sort_manually))
        )
        NavUtils.openRunningRecordsScreen()
        checkOrder(name2, name1, ::isCompletelyLeftOf)
        checkOrder(name1, name3, ::isCompletelyLeftOf)

        // Change order
        NavUtils.openSettingsScreen()
        onView(withId(settingsR.id.btnCardOrderManual)).perform(nestedScrollTo())
        clickOnViewWithId(settingsR.id.btnCardOrderManual)
        checkOrder(name2, name1, ::isCompletelyLeftOf)
        checkOrder(name1, name3, ::isCompletelyLeftOf)
        onView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
            .perform(drag(Direction.RIGHT, 300))

        // Check new order
        pressBack()
        NavUtils.openRunningRecordsScreen()
        checkOrder(name2, name3, ::isCompletelyLeftOf)
        checkOrder(name3, name1, ::isCompletelyLeftOf)
    }

    @Test
    fun cardOrderManual2() {
        val name = "Test"
        val cardsCount = 6

        // Add activities
        (1..cardsCount).forEach {
            testUtils.addActivity("$name$it")
        }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        NavUtils.openCardSizeScreen()
        tryAction { clickOnViewWithText("4") }
        pressBack()
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(coreR.string.settings_sort_by_color)
        clickOnSpinnerWithId(settingsR.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(coreR.string.settings_sort_manually)
        Thread.sleep(1000)

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Drag
        (1..cardsCount).forEach {
            onView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText("$name$it")))
                .perform(
                    drag(Direction.RIGHT, screenWidth),
                    drag(Direction.DOWN, screenHeight),
                )
        }

        // Check order in settings
        tryAction { checkManualOrder(name) }

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        tryAction { checkManualOrder(name) }
    }

    private fun checkOrder(first: String, second: String, vararg matchers: (Matcher<View>) -> ViewAssertion) {
        matchers.forEach { matcher ->
            onView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(first))).check(
                matcher(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(second)))
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun checkManualOrder(name: String) {
        checkOrder(name + 2, name + 1, ::isCompletelyRightOf, ::isTopAlignedWith)
        checkOrder(name + 3, name + 2, ::isCompletelyRightOf, ::isTopAlignedWith)
        checkOrder(name + 4, name + 3, ::isCompletelyRightOf, ::isTopAlignedWith)
        checkOrder(name + 5, name + 1, ::isCompletelyBelow)
        checkOrder(name + 6, name + 5, ::isCompletelyRightOf, ::isTopAlignedWith)
    }
}
