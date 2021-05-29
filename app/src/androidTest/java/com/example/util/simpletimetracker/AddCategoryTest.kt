package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
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
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddCategoryTest : BaseUiTest() {

    @Test
    fun addCategory() {
        val name = "Test"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1

        // Add activities
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(R.string.categories_record_type_hint))
        clickOnViewWithText(R.string.categories_add_activity_tag)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeCategoryDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryType))

        // Name is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Typing name
        typeTextIntoView(R.id.etChangeCategoryName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(R.string.change_category_color_hint)
        checkViewIsDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryType))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))

        // Selecting color
        scrollRecyclerToPosition(R.id.rvChangeCategoryColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        // Open activity chooser
        clickOnViewWithText(R.string.change_category_types_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsDisplayed(withId(R.id.rvChangeCategoryType))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        checkViewIsDisplayed(withText(R.string.change_category_selected_types_empty))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        checkViewIsDisplayed(withText(R.string.change_category_selected_types_hint))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        checkViewIsDisplayed(withText(R.string.change_category_selected_types_hint))
        checkViewDoesNotExist(withId(R.id.viewDividerItem))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))

        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        checkViewIsDisplayed(withText(R.string.change_category_selected_types_empty))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnViewWithText(R.string.change_record_type_save)

        // Category type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check types saved
        longClickOnView(withText(name))
        clickOnViewWithText(R.string.change_category_types_hint)
        checkViewIsDisplayed(withText(R.string.change_category_selected_types_hint))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
    }

    @Test
    fun addCategoryTypesEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_activity_tag)

        // Open activity chooser
        clickOnViewWithText(R.string.change_category_types_hint)
        checkViewIsDisplayed(withText(R.string.record_types_empty))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeCategory), matcher))
}
