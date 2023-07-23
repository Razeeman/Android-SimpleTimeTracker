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
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

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
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name1, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString"))
        )

        // Filter untracked
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(coreR.string.untracked_time_name))
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString"))
        )

        // Filter activity
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("1$hourString 0$minuteString"))
        )

        // Filter all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(coreR.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString"))
        )

        // Hide all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
    }

    @Test
    fun statisticsFilterCategory() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addCategory(tag1)
        testUtils.addCategory(tag2)
        testUtils.addActivity(name = name1, categories = listOf(tag1))
        testUtils.addActivity(name = name2, categories = listOf(tag2))
        testUtils.addActivity(name = name3)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        testUtils.addRecord(name3)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name1, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Switch filter
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()

        // All tags displayed
        tryAction { checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.uncategorized_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Filter untracked
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.untracked_time_name))
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.uncategorized_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Filter uncategorized
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.uncategorized_time_name))
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.uncategorized_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString"))
        )

        // Filter tag
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("1$hourString 0$minuteString"))
        )

        // Filter all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(coreR.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.uncategorized_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Hide all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.uncategorized_time_name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
    }

    @Test
    fun statisticsFilterTag() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)
        testUtils.addActivity(name = name1)
        testUtils.addActivity(name = name2)
        testUtils.addActivity(name = name3)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name2, tagNames = listOf(tag2))
        testUtils.addRecord(name3)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(name1, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Switch filter
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.record_tag_hint_short)
        pressBack()

        // All tags displayed
        tryAction { checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.change_record_untagged), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Filter untracked
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.untracked_time_name))
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.change_record_untagged), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Filter untagged
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.change_record_untagged))
        )
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.change_record_untagged), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("2$hourString 0$minuteString"))
        )

        // Filter tag
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("1$hourString 0$minuteString"))
        )

        // Filter all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))

        // Show all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(coreR.string.types_filter_show_all)
        pressBack()
        tryAction { checkViewIsDisplayed(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.change_record_untagged), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.tvStatisticsInfoText), withText("3$hourString 0$minuteString"))
        )

        // Hide all
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.types_filter_hide_all)
        pressBack()
        tryAction { checkViewDoesNotExist(allOf(withText(tag1), isCompletelyDisplayed())) }
        checkViewDoesNotExist(allOf(withText(tag2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.change_record_untagged), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsR.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.no_data), isCompletelyDisplayed()))
    }
}
