package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordTypeCategoryTest : BaseUiTest() {

    @Test
    fun recordTypeCategory() {
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add category
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1)
        NavUtils.addCategory(categoryName2)
        pressBack()

        // Add activities
        NavUtils.openRunningRecordsScreen()
        NavUtils.addActivity(typeName1, categories = listOf(categoryName1))
        NavUtils.addActivity(typeName2, categories = listOf(categoryName2))

        // Check first category
        NavUtils.openCategoriesScreen()

        longClickOnView(withText(categoryName1))
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
        clickOnViewWithText(R.string.change_category_save)

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnViewWithText(R.string.change_category_save)
        pressBack()

        // Check first activity
        NavUtils.openRunningRecordsScreen()
        longClickOnView(withText(typeName1))
        clickOnViewWithText(R.string.change_record_type_category_hint)
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(categoryName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(categoryName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(categoryName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(categoryName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnViewWithText(R.string.change_record_type_save)

        // Check second activity
        longClickOnView(withText(typeName2))
        clickOnViewWithText(R.string.change_record_type_category_hint)
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(categoryName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(categoryName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(categoryName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(categoryName2)), withCardColor(R.color.colorFiltered))
        )
        clickOnViewWithText(R.string.change_record_type_save)

        // Check first category again
        NavUtils.openCategoriesScreen()

        longClickOnView(withText(categoryName1))
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
        pressBack()

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(R.string.change_category_type_hint)
        checkViewDoesNotExist(
            allOf(hasDescendant(withText(typeName1)), withCardColor(R.color.colorFiltered))
        )
        checkViewIsDisplayed(
            allOf(hasDescendant(withText(typeName2)), withCardColor(R.color.colorFiltered))
        )
    }
}
