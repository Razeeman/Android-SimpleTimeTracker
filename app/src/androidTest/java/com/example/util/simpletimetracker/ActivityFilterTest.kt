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
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterR
import com.example.util.simpletimetracker.feature_views.R as viewsR

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

        tryAction { clickOnViewWithText(coreR.string.running_records_add_filter) }
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.btnChangeActivityFilterDelete))
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterType))

        // Name is not selected
        clickOnViewWithText(coreR.string.change_category_save)

        // Typing name
        typeTextIntoView(changeActivityFilterR.id.etChangeActivityFilterName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterType))

        // Selecting color
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))
        checkViewIsDisplayed(
            allOf(withId(changeActivityFilterR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )

        // Selecting color
        scrollRecyclerToPosition(changeActivityFilterR.id.rvChangeActivityFilterColor, lastColorPosition)
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeActivityFilterR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )
        clickOnViewWithText(coreR.string.change_category_color_hint)

        // Open activity chooser
        clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterColor))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))

        // Selecting activity
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(typeName1))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))

        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewDoesNotExist(withId(changeActivityFilterR.id.viewDividerItem))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))

        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(typeName1))
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(typeName1))

        // Open category chooser
        clickOnViewWithText(coreR.string.category_hint)
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterColor))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))

        // Selecting category
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName1))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))

        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewDoesNotExist(withId(changeActivityFilterR.id.viewDividerItem))
        checkViewIsDisplayed(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))

        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName1))
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName1))

        // Types are preserved when switching filter type
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.activity_hint),
            ),
        )
        onView(withText(typeName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.category_hint),
            ),
        )
        onView(withText(categoryName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))

        // Save
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.activity_hint),
            ),
        )
        clickOnViewWithText(coreR.string.change_activity_filter_save)

        // Filter added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check types saved
        longClickOnView(withText(name))
        clickOnViewWithText(coreR.string.activity_hint)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(changeActivityFilterR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
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

        tryAction { longClickOnView(withText(name)) }

        // View is set up
        checkViewIsDisplayed(withId(changeActivityFilterR.id.btnChangeActivityFilterDelete))
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterColor))
        checkViewIsNotDisplayed(withId(changeActivityFilterR.id.rvChangeActivityFilterType))
        checkViewIsDisplayed(allOf(withId(changeActivityFilterR.id.etChangeActivityFilterName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change name
        typeTextIntoView(changeActivityFilterR.id.etChangeActivityFilterName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        // Change color
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsDisplayed(
            allOf(withId(changeActivityFilterR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )
        scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeActivityFilterR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )
        clickOnViewWithText(coreR.string.change_category_color_hint)

        // Change types
        clickOnViewWithText(coreR.string.activity_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.category_hint),
            ),
        )
        clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(categoryName2))
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))

        // Save
        clickOnViewWithText(coreR.string.change_activity_filter_save)

        // Filter updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
        longClickOnView(withText(newName))
        clickOnViewWithText(coreR.string.activity_hint)
        onView(withText(categoryName1)).check(isCompletelyBelow(withId(changeActivityFilterR.id.viewDividerItem)))
        onView(withText(categoryName2)).check(isCompletelyAbove(withId(changeActivityFilterR.id.viewDividerItem)))
    }

    @Test
    fun addActivityFilterEmpty() {
        showActivityFilters()

        tryAction { clickOnViewWithText(coreR.string.running_records_add_filter) }
        clickOnView(withText(coreR.string.activity_hint))

        // Activities empty
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.activity_hint),
            ),
        )
        checkViewIsDisplayed(withText(coreR.string.record_types_empty))

        // Categories empty
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                withText(coreR.string.category_hint),
            ),
        )
        checkViewIsDisplayed(withText(coreR.string.change_record_type_categories_empty))
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
            names = listOf(typeName1, typeName3),
        )
        testUtils.addActivityFilter(
            name = filterByActivity2,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
            names = listOf(typeName5),
        )

        // Add category filters
        testUtils.addActivityFilter(
            name = filterByCategory1,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName1),
        )
        testUtils.addActivityFilter(
            name = filterByCategory2,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName2),
        )
        testUtils.addActivityFilter(
            name = filterByCategoryAll,
            type = ActivityFilter.Type.Category,
            color = firstColor,
            names = listOf(categoryName1, categoryName2),
        )

        // Check filtering
        tryAction { checkFilter(filterByActivity1, viewsR.color.colorFiltered) }
        checkFilter(filterByActivity2, viewsR.color.colorFiltered)
        checkFilter(filterByCategory1, viewsR.color.colorFiltered)
        checkFilter(filterByCategory2, viewsR.color.colorFiltered)
        checkFilter(filterByCategoryAll, viewsR.color.colorFiltered)
        checkFilter(filterEmpty, viewsR.color.colorFiltered)
        checkTypes(displayed = availableTypes, available = availableTypes)

        clickOnViewWithText(filterByActivity1)
        checkFilter(filterByActivity1, firstColor)
        checkTypes(
            displayed = listOf(typeName1, typeName3),
            available = availableTypes,
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
    fun filteringAllowMultipleFilters() {
        showActivityFilters()

        val filter1 = "filterByActivity1"
        val filter2 = "filterByActivity2"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        val availableTypes = listOf(typeName1, typeName2)

        // Add data
        testUtils.addActivity(name = typeName1)
        testUtils.addActivity(name = typeName2)
        testUtils.addActivityFilter(
            name = filter1,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
            names = listOf(typeName1),
        )
        testUtils.addActivityFilter(
            name = filter2,
            type = ActivityFilter.Type.Activity,
            color = lastColor,
            names = listOf(typeName2),
        )
        Thread.sleep(1000)

        // Check filtering
        tryAction { checkFilter(filter1, viewsR.color.colorFiltered) }
        checkFilter(filter2, viewsR.color.colorFiltered)
        checkTypes(displayed = availableTypes, available = availableTypes)

        // Select two
        clickOnViewWithText(filter1)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, viewsR.color.colorFiltered)
        checkTypes(displayed = listOf(typeName1), available = availableTypes)
        clickOnViewWithText(filter2)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, lastColor)
        checkTypes(displayed = listOf(typeName1, typeName2), available = availableTypes)
        clickOnViewWithText(filter1)
        clickOnViewWithText(filter2)

        // Change setting
        runBlocking { prefsInteractor.setAllowMultipleActivityFilters(false) }

        // Check
        clickOnViewWithText(filter1)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, viewsR.color.colorFiltered)
        checkTypes(displayed = listOf(typeName1), available = availableTypes)

        clickOnViewWithText(filter2)
        checkFilter(filter1, viewsR.color.colorFiltered)
        checkFilter(filter2, lastColor)
        checkTypes(displayed = listOf(typeName2), available = availableTypes)

        clickOnViewWithText(filter1)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, viewsR.color.colorFiltered)
        checkTypes(displayed = listOf(typeName1), available = availableTypes)
        clickOnViewWithText(filter1)

        // Change back
        runBlocking { prefsInteractor.setAllowMultipleActivityFilters(true) }

        // Check
        clickOnViewWithText(filter1)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, viewsR.color.colorFiltered)
        checkTypes(displayed = listOf(typeName1), available = availableTypes)
        clickOnViewWithText(filter2)
        checkFilter(filter1, firstColor)
        checkFilter(filter2, lastColor)
        checkTypes(displayed = listOf(typeName1, typeName2), available = availableTypes)
        clickOnViewWithText(filter1)
        clickOnViewWithText(filter2)
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
        scrollSettingsRecyclerToText(R.string.settings_show_activity_filters)
        clickOnSettingsCheckboxBesideText(R.string.settings_show_activity_filters)

        // Filtering appears
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(typeName1))
        checkViewDoesNotExist(withText(typeName2))

        // Change setting
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(R.string.settings_show_activity_filters)
        clickOnSettingsCheckboxBesideText(R.string.settings_show_activity_filters)

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
        checkViewIsDisplayed(allOf(withId(changeActivityFilterR.id.previewChangeActivityFilter), matcher))
}
