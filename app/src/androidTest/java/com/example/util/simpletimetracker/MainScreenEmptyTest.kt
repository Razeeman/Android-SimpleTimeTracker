package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
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
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainScreenEmptyTest : BaseUiTest() {

    @Test
    fun mainScreenEmpty() {
        val name = "Test"

        tryAction { checkViewIsDisplayed(withText(coreR.string.running_records_types_empty)) }
        checkViewDoesNotExist(withText(coreR.string.running_records_empty))

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(coreR.string.running_records_add_type))
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)
        closeSoftKeyboard()
        clickOnView(withText(coreR.string.change_record_type_save))
        checkViewDoesNotExist(withText(coreR.string.running_records_types_empty))
        checkViewIsDisplayed(withText(coreR.string.running_records_empty))

        // Start timer
        clickOnViewWithText(name)
        checkViewDoesNotExist(withText(coreR.string.running_records_empty))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))
        tryAction { checkViewIsDisplayed(withText(coreR.string.running_records_empty)) }
    }

    @Test
    fun recordsEmpty() {
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }

        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(allOf(withText(coreR.string.records_hint), isCompletelyDisplayed()))
        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("24$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(allOf(withText(coreR.string.records_hint), isCompletelyDisplayed()))
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.records_empty), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.records_hint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
    }

    @Test
    fun statisticsEmpty() {
        NavUtils.openStatisticsScreen()
        checkRanges()

        // Week range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        checkRanges()

        // Month range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)
        checkRanges()

        // Year range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_year)
        checkRanges()

        // Overall range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_overall)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed()
            )
        )

        // Back to day range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkRanges()

        // Empty category statistics
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsEmptyFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)

        // Day range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkRanges()

        // Week range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        checkRanges()

        // Month range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)
        checkRanges()

        // Year range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_year)
        checkRanges()

        // Overall range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_overall)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed()
            )
        )

        // Back to day range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkRanges()
    }

    private fun checkRanges() {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))
    }
}
