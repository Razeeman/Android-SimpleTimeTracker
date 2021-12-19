package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordTypeCategoryTest : BaseUiTest() {

    @Test
    fun recordTypeCategory() {
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1)
        NavUtils.addCategory(categoryName2)
        pressBack()

        // Add activities
        NavUtils.openRunningRecordsScreen()
        testUtils.addActivity(name = typeName1, categories = listOf(categoryName1))
        testUtils.addActivity(name = typeName2, categories = listOf(categoryName2))

        // Check first category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()

        longClickOnView(withText(categoryName1))
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        clickOnViewWithText(R.string.change_category_save)

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnViewWithText(R.string.change_category_save)
        pressBack()

        // Check first activity
        NavUtils.openRunningRecordsScreen()
        longClickOnView(withText(typeName1))
        clickOnViewWithText(R.string.change_record_type_category_hint)
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnViewWithText(R.string.change_record_type_save)

        // Check second activity
        longClickOnView(withText(typeName2))
        clickOnViewWithText(R.string.change_record_type_category_hint)
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))

        clickOnViewWithText(R.string.change_record_type_save)

        // Check first category again
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()

        longClickOnView(withText(categoryName1))
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        pressBack()

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
    }
}
