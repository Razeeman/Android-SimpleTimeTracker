package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsFilterTest : BaseUiTest() {

    @Test
    fun statisticsFilterType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Filter untracked
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(R.string.untracked_time_name)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Filter activity
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("1$hourString 0$minuteString")))

        // Filter all
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithIdOnPager(R.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(R.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Hide all
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
    }

    @Test
    fun statisticsFilterCategory() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addActivityTag(tag1)
        testUtils.addActivityTag(tag2)
        testUtils.addActivity(name = name1, categories = listOf(tag1))
        testUtils.addActivity(name = name2, categories = listOf(tag2))

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Switch filter
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()

        // All tags displayed
        tryAction { checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Filter tag
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("1$hourString 0$minuteString")))

        // Filter all
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithIdOnPager(R.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(R.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString")))

        // Hide all
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
    }
}
