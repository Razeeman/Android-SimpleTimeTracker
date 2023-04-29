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
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR

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
        clickOnViewWithText(coreR.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName2))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        clickOnViewWithText(coreR.string.change_category_save)

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(coreR.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))

        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName2))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        clickOnViewWithText(coreR.string.change_category_save)
        pressBack()

        // Check first activity
        NavUtils.openRunningRecordsScreen()
        longClickOnView(withText(typeName1))
        clickOnViewWithText(coreR.string.category_hint)
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))

        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check second activity
        longClickOnView(withText(typeName2))
        clickOnViewWithText(coreR.string.category_hint)
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName1))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check first category again
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()

        longClickOnView(withText(categoryName1))
        clickOnViewWithText(coreR.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        pressBack()

        // Check second category
        longClickOnView(withText(categoryName2))
        clickOnViewWithText(coreR.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
    }
}
