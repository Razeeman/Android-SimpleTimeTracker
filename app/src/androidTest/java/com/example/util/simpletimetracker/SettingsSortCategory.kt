package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsSortCategory : BaseUiTest() {

    @Test
    fun sortCategoryName() {
        val name1 = "1"
        val name2 = "2"
        val color1 = firstColor
        val color2 = lastColor

        // Add data
        testUtils.addCategory(tagName = name1, color = color2)
        testUtils.addCategory(tagName = name2, color = color1)

        // Check order
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        tryAction { checkOrder(name1, name2, ::isCompletelyLeftOf) }
        pressBack()

        // Check settings
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(R.string.settings_sort_order_category)
        settingsSelectorValueBesideText(
            R.string.settings_sort_order_category,
            withText(coreR.string.settings_sort_by_name),
        )
    }

    @Test
    fun sortCategoryColor() {
        val name1 = "1"
        val name2 = "2"
        val color1 = ColorMapper.getAvailableColors()[1]
        val color2 = ColorMapper.getAvailableColors()[5]

        // Add data
        testUtils.addCategory(tagName = name1, color = color2)
        testUtils.addCategory(tagName = name2, color = color1)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(R.string.settings_sort_order_category)
        clickOnSettingsSpinnerBesideText(R.string.settings_sort_order_category)
        clickOnViewWithText(coreR.string.settings_sort_by_color)
        settingsSelectorValueBesideText(
            R.string.settings_sort_order_category,
            withText(coreR.string.settings_sort_by_color),
        )

        // Check new order
        NavUtils.openCategoriesScreen()
        tryAction { checkOrder(name1, name2, ::isCompletelyRightOf) }
    }

    @Test
    fun sortOrderManual() {
        val name1 = "1"
        val name2 = "2"
        val name3 = "3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(R.string.settings_sort_order_category)
        clickOnSettingsSpinnerBesideText(R.string.settings_sort_order_category)
        clickOnViewWithText(coreR.string.settings_sort_manually)
        Thread.sleep(1000)

        // Check old order
        checkOrder(name1, name2, ::isCompletelyLeftOf)
        checkOrder(name2, name3, ::isCompletelyLeftOf)

        // Drag
        onView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(name2)))
            .perform(drag(Direction.LEFT, 300))

        // Check new order
        pressBack()
        settingsSelectorValueBesideText(
            R.string.settings_sort_order_category,
            withText(coreR.string.settings_sort_manually),
        )
        NavUtils.openCategoriesScreen()
        checkOrder(name2, name1, ::isCompletelyLeftOf)
        checkOrder(name1, name3, ::isCompletelyLeftOf)
    }

    private fun checkOrder(first: String, second: String, vararg matchers: (Matcher<View>) -> ViewAssertion) {
        matchers.forEach { matcher ->
            onView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(first))).check(
                matcher(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(second))),
            )
        }
    }
}
