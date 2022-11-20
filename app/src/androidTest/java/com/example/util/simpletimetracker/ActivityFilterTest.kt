package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ActivityFilterTest : BaseUiTest() {

    @Test
    fun addActivityFilter() {
        showActivityFilters()

        val name = "TestFilter"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1

        // Add activities
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)
        testUtils.addCategory(categoryName1)
        testUtils.addCategory(categoryName2)

        tryAction { clickOnViewWithText(R.string.running_records_add_filter) }
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeActivityFilterDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterType))

        // Name is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Typing name
        typeTextIntoView(R.id.etChangeActivityFilterName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(R.string.change_category_color_hint)
        checkViewIsDisplayed(withId(R.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterType))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeActivityFilterColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(firstColor))))

        // Selecting color
        scrollRecyclerToPosition(R.id.rvChangeActivityFilterColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(lastColor))))

        // Open activity chooser
        clickOnViewWithText(R.string.activity_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterColor))
        checkViewIsDisplayed(withId(R.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(typeName1))
        checkViewIsDisplayed(withText(R.string.something_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(typeName2))
        checkViewIsDisplayed(withText(R.string.something_selected))
        checkViewDoesNotExist(withId(R.id.viewDividerItem))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))

        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(typeName2))
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(typeName1))

        // Open category chooser
        clickOnViewWithText(R.string.category_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterColor))
        checkViewIsDisplayed(withId(R.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        // Selecting category
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName1))
        checkViewIsDisplayed(withText(R.string.something_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.something_selected))
        checkViewDoesNotExist(withId(R.id.viewDividerItem))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))

        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName1))
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName2))
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName1))

        // Types are preserved when switching filter type
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)), withText(R.string.activity_hint))
        )
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)), withText(R.string.category_hint))
        )
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))

        // Save
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)), withText(R.string.activity_hint))
        )
        clickOnViewWithText(R.string.change_activity_filter_save)

        // Filter added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check types saved
        longClickOnView(withText(name))
        clickOnViewWithText(R.string.activity_hint)
        checkViewIsDisplayed(withText(R.string.something_selected))
        checkViewIsDisplayed(withId(R.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
    }

    @Test
    fun changeActivityFilter() {
        showActivityFilters()

        val name = "TestFilter"
        val newName = "UpdatedFilter"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"

        // Add activities
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)
        testUtils.addCategory(categoryName1)
        testUtils.addCategory(categoryName2)

        // Add category
        NavUtils.addActivityFilter(name = name, color = firstColor, activities = listOf(typeName1))

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeActivityFilterDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeActivityFilterName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change name
        typeTextIntoView(R.id.etChangeActivityFilterName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        // Change color
        clickOnViewWithText(R.string.change_category_color_hint)
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(firstColor))))
        scrollRecyclerToView(R.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        clickOnRecyclerItem(R.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(lastColor))))

        // Change types
        clickOnViewWithText(R.string.activity_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)), withText(R.string.category_hint))
        )
        clickOnRecyclerItem(R.id.rvChangeActivityFilterType, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))

        // Save
        clickOnViewWithText(R.string.change_activity_filter_save)

        // Filter updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
        longClickOnView(withText(newName))
        clickOnViewWithText(R.string.activity_hint)
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
    }

    @Test
    fun addActivityFilterEmpty() {
        showActivityFilters()

        tryAction { clickOnViewWithText(R.string.running_records_add_filter) }
        clickOnView(withText(R.string.activity_hint))

        // Activities empty
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)),
                withText(R.string.activity_hint)
            )
        )
        checkViewIsDisplayed(withText(R.string.record_types_empty))

        // Categories empty
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsChangeActivityFilterType)),
                withText(R.string.category_hint)
            )
        )
        checkViewIsDisplayed(withText(R.string.change_record_type_categories_empty))
    }

    @Test
    fun filtering() {
        showActivityFilters()

        val filterByActivity1 = "filterByActivity1"
        val filterByActivity2 = "filterByActivity2"
        val filterByCategory1 = "filterByCategory1"
        val filterByCategory2 = "filterByCategory2"
        val filterByCategoryAll = "filterByCategoryAll"
        val filterEmpty = "filterEmpty"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val typeName3 = "Type3"
        val typeName4 = "Type4"
        val typeName5 = "Type5"
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"

        val availableTypes = listOf(typeName1, typeName2, typeName3, typeName4, typeName5)

        // Add data
        testUtils.addCategory(categoryName1)
        testUtils.addCategory(categoryName2)
        testUtils.addActivity(name = typeName1, categories = listOf(categoryName1))
        testUtils.addActivity(name = typeName2, categories = listOf(categoryName1))
        testUtils.addActivity(name = typeName3, categories = listOf(categoryName2))
        testUtils.addActivity(name = typeName4, categories = listOf(categoryName2))
        testUtils.addActivity(name = typeName5)

        // Add empty filter
        testUtils.addActivityFilter(
            name = filterEmpty,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
        )

        // Add activity filters
        testUtils.addActivityFilter(
            name = filterByActivity1,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
            names = listOf(typeName1, typeName3)
        )
        testUtils.addActivityFilter(
            name = filterByActivity2,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
            names = listOf(typeName5)
        )

        // Add category filters
        testUtils.addActivityFilter(
            name = filterByCategory1,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName1)
        )
        testUtils.addActivityFilter(
            name = filterByCategory2,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName2)
        )
        testUtils.addActivityFilter(
            name = filterByCategoryAll,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName1, categoryName2)
        )

        // Check filtering
        tryAction {
            checkFilter(filterByActivity1, R.color.colorInactive)
            checkFilter(filterByActivity2, R.color.colorInactive)
            checkFilter(filterByCategory1, R.color.colorInactive)
            checkFilter(filterByCategory2, R.color.colorInactive)
            checkFilter(filterByCategoryAll, R.color.colorInactive)
            checkFilter(filterEmpty, R.color.colorInactive)
            checkTypes(displayed = availableTypes, available = availableTypes)
        }

        clickOnViewWithText(filterByActivity1)
        checkFilter(filterByActivity1, firstColor)
        checkTypes(
            displayed = listOf(typeName1, typeName3),
            available = availableTypes
        )
        clickOnViewWithText(filterByActivity1)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByActivity2)
        checkFilter(filterByActivity2, firstColor)
        checkTypes(
            displayed = listOf(typeName5),
            available = availableTypes,
        )
        clickOnViewWithText(filterByActivity2)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByCategory1)
        checkFilter(filterByCategory1, firstColor)
        checkTypes(
            displayed = listOf(typeName1, typeName2),
            available = availableTypes,
        )
        clickOnViewWithText(filterByCategory1)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByCategory2)
        checkFilter(filterByCategory2, firstColor)
        checkTypes(
            displayed = listOf(typeName3, typeName4),
            available = availableTypes,
        )
        clickOnViewWithText(filterByCategory2)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByCategoryAll)
        checkFilter(filterByCategoryAll, firstColor)
        checkTypes(
            displayed = listOf(typeName1, typeName2, typeName3, typeName4),
            available = availableTypes,
        )
        clickOnViewWithText(filterByCategoryAll)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterEmpty)
        checkFilter(filterEmpty, firstColor)
        checkTypes(
            displayed = listOf(),
            available = availableTypes,
        )
        clickOnViewWithText(filterEmpty)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByActivity1)
        clickOnViewWithText(filterByCategory1)
        checkFilter(filterByActivity1, firstColor)
        checkFilter(filterByCategory1, firstColor)
        checkTypes(
            displayed = listOf(typeName1, typeName2, typeName3),
            available = availableTypes,
        )
        clickOnViewWithText(filterByActivity1)
        clickOnViewWithText(filterByCategory1)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByActivity2)
        clickOnViewWithText(filterByCategory2)
        checkFilter(filterByActivity2, firstColor)
        checkFilter(filterByCategory2, firstColor)
        checkTypes(
            displayed = listOf(typeName3, typeName4, typeName5),
            available = availableTypes,
        )
        clickOnViewWithText(filterByActivity2)
        clickOnViewWithText(filterByCategory2)
        checkTypes(displayed = availableTypes, available = availableTypes)

        // All filters
        clickOnViewWithText(filterByActivity1)
        clickOnViewWithText(filterByActivity2)
        clickOnViewWithText(filterByCategory1)
        clickOnViewWithText(filterByCategory2)
        clickOnViewWithText(filterByCategoryAll)
        clickOnViewWithText(filterEmpty)
        checkFilter(filterByActivity1, firstColor)
        checkFilter(filterByActivity2, firstColor)
        checkFilter(filterByCategory1, firstColor)
        checkFilter(filterByCategory2, firstColor)
        checkFilter(filterEmpty, firstColor)
        checkTypes(
            displayed = availableTypes,
            available = availableTypes,
        )
    }

    @Test
    fun filteringDisabledIfFiltersNotShown() {
        val filterName = "Filter"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add data
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)
        testUtils.addActivityFilter(
            name = filterName,
            type = ActivityFilter.Type.Activity,
            names = listOf(typeName1),
            selected = true,
        )

        // No filtering
        tryAction {
            checkViewIsDisplayed(withText(typeName1))
            checkViewIsDisplayed(withText(typeName2))
        }

        // Change setting
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        onView(withId(R.id.checkboxSettingsShowActivityFilters)).perform(nestedScrollTo())
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowActivityFilters))

        // Filtering appears
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(typeName1))
        checkViewDoesNotExist(withText(typeName2))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowActivityFilters)).perform(nestedScrollTo())
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowActivityFilters))

        // No filtering
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
    }

    private fun checkFilter(
        name: String,
        color: Int,
    ) {
        checkViewIsDisplayed(allOf(withCardColor(color), hasDescendant(withText(name))))
    }

    @Suppress("ConvertArgumentToSet")
    private fun checkTypes(
        displayed: List<String>,
        available: List<String>,
    ) {
        displayed.forEach { checkViewIsDisplayed(withText(it)) }
        (available - displayed).forEach { checkViewDoesNotExist(withText(it)) }
    }

    private fun showActivityFilters() = runBlocking {
        prefsInteractor.setShowActivityFilters(true)
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeActivityFilter), matcher))
}
