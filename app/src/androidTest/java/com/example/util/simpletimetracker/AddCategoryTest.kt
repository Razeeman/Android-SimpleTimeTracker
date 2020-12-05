package com.example.util.simpletimetracker

import android.view.View
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
        NavUtils.addActivity(typeName1)
        NavUtils.addActivity(typeName2)

        NavUtils.openSettingsScreen()
        clickOnViewWithText(R.string.settings_edit_categories)
        clickOnViewWithText(R.string.categories_add)

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
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsDisplayed(withId(R.id.rvChangeCategoryType))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        checkViewDoesNotExist(withCardColor(R.color.colorInactive))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        checkViewDoesNotExist(withCardColor(R.color.colorInactive))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))

        clickOnViewWithText(R.string.change_record_type_save)

        // Category type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
    }

    @Test
    fun addCategoryTypesEmpty() {
        NavUtils.openSettingsScreen()
        clickOnViewWithText(R.string.settings_edit_categories)
        clickOnViewWithText(R.string.categories_add)

        // Open activity chooser
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewIsDisplayed(withText(R.string.change_category_types_empty))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeCategory), matcher))
}
