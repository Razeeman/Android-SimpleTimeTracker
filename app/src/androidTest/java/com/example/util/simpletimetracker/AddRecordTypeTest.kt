package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddRecordTypeTest : BaseUiTest() {

    @Test
    fun addRecordType() {
        val name = "Test"
        val categoryName1 = "category1"
        val categoryName2 = "category2"
        val note = "note"
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1)
        NavUtils.addCategory(categoryName2)

        pressBack()
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(coreR.string.running_records_add_type)

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeArchive))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeDelete))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeStatistics))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvIconSelection))

        // Name is not selected
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Typing name
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewIsDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvIconSelection))

        // Selecting color
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withCardColor(firstColor)))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )

        // Selecting color
        scrollRecyclerToPosition(changeRecordTypeR.id.rvChangeRecordTypeColor, lastColorPosition)
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withCardColor(lastColor)))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )

        // Open icon chooser
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeColor))
        checkViewIsDisplayed(withId(changeRecordTypeR.id.rvIconSelection))

        // Selecting icon
        clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Selecting icon
        onView(withId(changeRecordTypeR.id.rvIconSelection)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        // Open category chooser
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        clickOnViewWithText(coreR.string.category_hint)
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvIconSelection))
        checkViewIsDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeCategories))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        onView(withText(categoryName1)).check(isCompletelyBelow(withText(coreR.string.nothing_selected)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withText(coreR.string.nothing_selected)))
        onView(withText(categoryName2)).check(isTopAlignedWith(withText(categoryName1)))

        // Selecting category
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName1))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        onView(withText(categoryName1)).check(isCompletelyBelow(withText(coreR.string.something_selected)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withText(categoryName1)))

        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        onView(withText(categoryName2)).check(isTopAlignedWith(withText(categoryName1)))

        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        onView(withText(categoryName1)).check(isCompletelyBelow(withText(coreR.string.nothing_selected)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withText(coreR.string.nothing_selected)))
        onView(withText(categoryName2)).check(isTopAlignedWith(withText(categoryName1)))

        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnViewWithText(coreR.string.category_hint)

        // Selecting goal time
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalSession)),
                withId(changeRecordTypeR.id.fieldChangeRecordTypeGoalDuration),
            ),
        )
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("10$minuteString"))
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)

        // Adding note
        onView(withId(changeRecordTypeR.id.etChangeRecordTypeNote)).perform(nestedScrollTo())
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeNote, note)

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))

        // Check categories saved
        longClickOnView(withText(name))
        clickOnViewWithText(coreR.string.category_hint)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        onView(withText(categoryName1)).check(isCompletelyBelow(withText(coreR.string.something_selected)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withText(categoryName1)))
        clickOnViewWithText(coreR.string.category_hint)
        onView(withId(changeRecordTypeR.id.etChangeRecordTypeNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.etChangeRecordTypeNote), withText(note)))
    }

    @Test
    fun addRecordTypeEmpty() {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }
        closeSoftKeyboard()

        // Goal time is disabled
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
        checkViewIsDisplayed(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalSession)),
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue),
                withText(coreR.string.change_record_type_goal_time_disabled),
            ),
        )
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)

        // Open category chooser
        clickOnViewWithText(coreR.string.category_hint)
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
    }

    @Test
    fun addRecordTypeSameName() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)

        // Add new activity
        clickOnViewWithText(coreR.string.running_records_add_type)
        closeSoftKeyboard()

        // No error
        checkViewDoesNotExist(withText(coreR.string.change_record_message_name_exist))

        // Check same name
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)
        checkViewIsDisplayed(withText(coreR.string.change_record_message_name_exist))

        // Check other name
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, "$name+")
        checkViewDoesNotExist(withText(coreR.string.change_record_message_name_exist))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.previewChangeRecordType), matcher))
}
