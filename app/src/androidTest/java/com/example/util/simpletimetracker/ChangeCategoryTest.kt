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
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeCategoryTest : BaseUiTest() {

    @Test
    fun changeRecordType() {
        val name = "Test"
        val newName = "Updated"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()

        // Add activities
        NavUtils.addActivity(typeName1)
        NavUtils.addActivity(typeName2)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.addCategory(name, firstColor, listOf(typeName1))

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeCategoryDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryType))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeCategoryName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change item
        typeTextIntoView(R.id.etChangeCategoryName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        clickOnViewWithText(R.string.change_record_type_color_hint)
        scrollRecyclerToView(R.id.rvChangeCategoryColor, withCardColor(lastColor))
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )

        clickOnViewWithText(R.string.change_record_type_save)

        // Record type updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
        longClickOnView(withText(newName))
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeCategory), matcher))
}
