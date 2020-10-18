package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsRangesTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test"

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        clickOnView(withText(R.string.change_record_type_save))

        // Start timer
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))

        // Statistics
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to week range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_week)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to month range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_month)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to overall range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_overall))
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(R.id.btnStatisticsContainerPrevious))
        checkViewIsNotDisplayed(withId(R.id.btnStatisticsContainerNext))
    }
}
