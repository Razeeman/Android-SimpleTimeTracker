package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenEmptyTest : BaseUiTest() {

    @Test
    fun mainScreenEmpty() {
        val name = "Test"

        // Empty main
        tryAction { checkViewIsDisplayed(withText(R.string.running_records_types_empty)) }
        checkViewDoesNotExist(withText(R.string.running_records_empty))

        // Empty records
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(allOf(withText(R.string.records_hint), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("24h 0m")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(allOf(withText(R.string.records_hint), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(R.string.records_hint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Empty statistics
        NavUtils.openStatisticsScreen()
        checkRanges()

        // Week range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_week)
        checkRanges()

        // Month range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_month)
        checkRanges()

        // Year range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_year)
        checkRanges()

        // Overall range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_overall)
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Back to day range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_today)
        checkRanges()

        // Empty category statistics
        clickOnViewWithIdOnPager(R.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()

        // Day range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_today)
        checkCategoryRanges()

        // Week range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_week)
        checkCategoryRanges()

        // Month range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_month)
        checkCategoryRanges()

        // Year range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_year)
        checkCategoryRanges()

        // Overall range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_overall)
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Back to day range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_today)
        checkCategoryRanges()

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        clickOnView(withText(R.string.change_record_type_save))
        checkViewDoesNotExist(withText(R.string.running_records_types_empty))
        checkViewIsDisplayed(withText(R.string.running_records_empty))

        // Start timer
        clickOnViewWithText(name)
        checkViewDoesNotExist(withText(R.string.running_records_empty))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        checkViewIsDisplayed(withText(R.string.running_records_empty))
    }

    private fun checkRanges() {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
    }

    private fun checkCategoryRanges() {
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
    }
}
