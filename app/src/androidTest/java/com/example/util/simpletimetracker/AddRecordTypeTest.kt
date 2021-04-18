package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddRecordTypeTest : BaseUiTest() {

    @Test
    fun addRecordType() {
        val name = "Test"
        val categoryName1 = "category1"
        val categoryName2 = "category2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1
        val firstIcon = iconImageMapper.availableIconsNames.values.first()
        val lastIcon = iconImageMapper.availableIconsNames.values.last()
        val lastIconPosition = iconImageMapper.availableIconsNames.size - 1

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1)
        NavUtils.addCategory(categoryName2)

        pressBack()
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Name is not selected
        clickOnViewWithText(R.string.change_record_type_save)

        // Typing name
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(R.string.change_record_type_color_hint)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))

        // Selecting color
        scrollRecyclerToPosition(R.id.rvChangeRecordTypeColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        // Open icon chooser
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting icon
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Selecting icon
        scrollRecyclerToPosition(R.id.rvChangeRecordTypeIcon, lastIconPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        // Open category chooser
        clickOnViewWithText(R.string.change_record_type_category_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeCategories))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.change_record_type_selected_categories_empty))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        // Selecting category
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        checkViewIsDisplayed(withText(R.string.change_record_type_selected_categories_hint))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.change_record_type_selected_categories_hint))
        checkViewDoesNotExist(withId(R.id.viewDividerItem))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))

        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.change_record_type_selected_categories_empty))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnViewWithText(R.string.change_record_type_category_hint)

        // Selecting goal time
        clickOnViewWithId(R.id.groupChangeRecordTypeGoalTime)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("10m"))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))

        // Check categories saved
        longClickOnView(withText(name))
        clickOnViewWithText(R.string.change_record_type_category_hint)
        checkViewIsDisplayed(withText(R.string.change_record_type_selected_categories_hint))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
    }

    @Test
    fun addRecordTypeCategoriesEmpty() {
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Goal time is disabled
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvChangeRecordTypeGoalTimeTime),
                withText(R.string.change_record_type_goal_time_disabled)
            )
        )

        // Open category chooser
        clickOnViewWithText(R.string.change_record_type_category_hint)
        checkViewIsDisplayed(withText(R.string.change_record_type_categories_empty))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), matcher))
}
