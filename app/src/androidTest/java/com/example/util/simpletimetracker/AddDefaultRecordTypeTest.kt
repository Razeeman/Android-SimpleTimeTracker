package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddDefaultRecordTypeTest : BaseUiTest() {

    @Test
    fun default() {
        val name1 = "Games"
        val name1next = "Tv"
        val color1 = ColorMapper.getAvailableColors()[1]
        val name2 = "Chores"
        val name2next = "Cleaning"
        val color2 = ColorMapper.getAvailableColors()[5]

        tryAction {
            checkViewIsDisplayed(
                withText(
                    getString(
                        coreR.string.running_records_types_empty,
                        getString(coreR.string.running_records_add_type),
                        getString(coreR.string.running_records_add_default),
                    ),
                ),
            )
        }
        checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
        checkViewIsDisplayed(withText(coreR.string.running_records_add_default))

        // Open dialog
        clickOnViewWithText(coreR.string.running_records_add_default)
        Thread.sleep(1000)
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkActivity(name1, color1)
        checkActivity(name2, color2)
        checkOrder(name1, name1next, ::isCompletelyLeftOf, ::isTopAlignedWith)
        checkOrder(name2, name2next, ::isCompletelyLeftOf, ::isTopAlignedWith)

        // Close without saving
        pressBack()
        checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
        checkViewIsDisplayed(withText(coreR.string.running_records_add_default))
        checkViewDoesNotExist(withText(name1))
        checkViewDoesNotExist(withText(name2))

        // Check selection
        clickOnViewWithText(coreR.string.running_records_add_default)
        clickOnViewWithText(name1)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkOrder(name1, name1next, ::isCompletelyAbove)
        checkOrder(name2, name2next, ::isCompletelyLeftOf, ::isTopAlignedWith)

        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkOrder(name1, name1next, ::isCompletelyLeftOf, ::isTopAlignedWith)
        checkOrder(name2, name2next, ::isCompletelyAbove)

        clickOnViewWithText(coreR.string.select_all)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkOrder(name1, name1next, ::isCompletelyLeftOf, ::isTopAlignedWith)
        checkOrder(name2, name2next, ::isCompletelyLeftOf, ::isTopAlignedWith)

        clickOnViewWithText(coreR.string.select_nothing)
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkOrder(name1, name1next, ::isCompletelyLeftOf, ::isTopAlignedWith)
        checkOrder(name2, name2next, ::isCompletelyLeftOf, ::isTopAlignedWith)

        // Try to save when nothing selected
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText(coreR.string.duration_dialog_save))

        // Save
        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        Thread.sleep(1000)

        // Types added
        checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
        checkViewDoesNotExist(withText(coreR.string.running_records_add_default))
        checkActivity(name1, color1)
        checkActivity(name2, color2)
    }

    private fun checkActivity(
        name: String,
        color: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withCardColor(color)),
                hasDescendant(withText(name)),
            ),
        )
    }

    private fun checkOrder(
        first: String,
        second: String,
        vararg matchers: (Matcher<View>) -> ViewAssertion,
    ) {
        matchers.forEach { matcher ->
            onView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(first))).check(
                matcher(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(second)))
            )
        }
    }
}
