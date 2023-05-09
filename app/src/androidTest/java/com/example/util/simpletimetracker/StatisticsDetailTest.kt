package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
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
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withPluralText
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_records_all.R as recordsAllR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR
import com.example.util.simpletimetracker.feature_views.R as viewsR

@Suppress("SameParameterValue")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsDetailTest : BaseUiTest() {

    @Test
    fun statisticsDetailCustomRange() {
        val name = "TypeName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add records
        val calendarToday = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2021)
            set(Calendar.MONTH, 6) // middle of a year
            set(Calendar.DAY_OF_MONTH, 14) // middle of a week and month
            set(Calendar.HOUR_OF_DAY, 12)
        }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendarToday.timeInMillis,
            timeEnded = calendarToday.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        val calendarYesterday = Calendar.getInstance()
            .apply { timeInMillis = calendarToday.timeInMillis }
            .apply { add(Calendar.DATE, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendarYesterday.timeInMillis,
            timeEnded = calendarYesterday.timeInMillis + TimeUnit.HOURS.toMillis(2),
        )
        val calendarPrevWeek = Calendar.getInstance()
            .apply { timeInMillis = calendarToday.timeInMillis }
            .apply { add(Calendar.DATE, -7) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendarPrevWeek.timeInMillis,
            timeEnded = calendarPrevWeek.timeInMillis + TimeUnit.HOURS.toMillis(3),
        )
        val calendarPrevMonth = Calendar.getInstance()
            .apply { timeInMillis = calendarToday.timeInMillis }
            .apply { add(Calendar.MONTH, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendarPrevMonth.timeInMillis,
            timeEnded = calendarPrevMonth.timeInMillis + TimeUnit.HOURS.toMillis(4),
        )
        val calendarPrevYear = Calendar.getInstance()
            .apply { timeInMillis = calendarToday.timeInMillis }
            .apply { add(Calendar.YEAR, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendarPrevYear.timeInMillis,
            timeEnded = calendarPrevYear.timeInMillis + TimeUnit.HOURS.toMillis(5),
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_overall)
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        // Check one day
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarToday.get(Calendar.YEAR),
            monthStarted = calendarToday.get(Calendar.MONTH),
            dayStarted = calendarToday.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.btnStatisticsDetailPrevious), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.btnStatisticsDetailNext), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.cardStatisticsDetailRangeAverage), isCompletelyDisplayed())
        )
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "1$hourString 0$minuteString")
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(1)

        // Check two days
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarYesterday.get(Calendar.YEAR),
            monthStarted = calendarYesterday.get(Calendar.MONTH),
            dayStarted = calendarYesterday.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "1$hourString 30$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "3$hourString 0$minuteString")
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(2)

        // Check weeks
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevWeek.get(Calendar.YEAR),
            monthStarted = calendarPrevWeek.get(Calendar.MONTH),
            dayStarted = calendarPrevWeek.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "45$minuteString",
            averageNonEmpty = "2$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "3$hourString 0$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "6$hourString 0$minuteString")
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(3)

        // Check months
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevMonth.get(Calendar.YEAR),
            monthStarted = calendarPrevMonth.get(Calendar.MONTH),
            dayStarted = calendarPrevMonth.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "19$minuteString",
            averageNonEmpty = "2$hourString 30$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "2$hourString 0$minuteString",
            averageNonEmpty = "3$hourString 20$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "5$hourString 0$minuteString",
            averageNonEmpty = "5$hourString 0$minuteString"
        )
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "10$hourString 0$minuteString")
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(4)

        // Check years
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevYear.get(Calendar.YEAR),
            monthStarted = calendarPrevYear.get(Calendar.MONTH),
            dayStarted = calendarPrevYear.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "2$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "16$minuteString",
            averageNonEmpty = "3$hourString 45$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "1$hourString 9$minuteString",
            averageNonEmpty = "5$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_yearly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "7$hourString 30$minuteString",
            averageNonEmpty = "7$hourString 30$minuteString"
        )
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "15$hourString 0$minuteString")
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(5)
    }

    @Test
    fun statisticsDetailOverall() {
        val name = "TypeName"
        val tag = "TagName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag, name)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.YEAR, -10) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagNames = listOf(tag)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_overall)

        // Check buttons
        checkViewDoesNotExist(allOf(withId(statisticsDetailR.id.btnStatisticsDetailPrevious), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(statisticsDetailR.id.btnStatisticsDetailNext), isCompletelyDisplayed()))

        // Bar chart
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed()))

        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        clickOnViewWithText(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        clickOnViewWithText(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        clickOnViewWithText(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(coreR.string.statistics_detail_chart_yearly)
        clickOnViewWithText(coreR.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "3$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_yearly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        clickOnViewWithText(coreR.string.statistics_detail_length_ten)

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // Tag split
        onView(withId(statisticsDetailR.id.rvStatisticsDetailSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "2$hourString 0$minuteString", "67%")
        checkTagItem(
            viewsR.color.colorUntracked,
            getString(coreR.string.change_record_untagged),
            "1$hourString 0$minuteString",
            "33%"
        )

        // All records
        checkAllRecords(4)
    }

    @Test
    fun statisticsDetailDay() {
        val name = "TypeName"
        val tag = "TagName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag, name)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNames = listOf(tag)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_day)

        // Bar chart
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.cardStatisticsDetailRangeAverage), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping), isCompletelyDisplayed())
        )

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(statisticsDetailR.id.rvStatisticsDetailSplit)).perform(nestedScrollTo())
        checkNoTagItem(tag)
        checkTagItem(
            viewsR.color.colorUntracked,
            getString(coreR.string.change_record_untagged),
            "3$hourString 0$minuteString",
            "100%"
        )

        // Next day
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailWeek() {
        val name = "TypeName"
        val tag = "TagName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag, name)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNames = listOf(tag)
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagNames = listOf(tag)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, -7) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "25$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "8$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed())
        )
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(statisticsDetailR.id.rvStatisticsDetailSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "3$hourString 0$minuteString", "100%")
        checkNoTagItem(getString(coreR.string.change_record_untagged))

        // Next week
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailMonth() {
        val name = "TypeName"
        val tag1 = "TagName1"
        val tag2 = "TagName2"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNames = listOf(tag1)
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagNames = listOf(tag2)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.MONTH, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_month)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed()))
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkViewDoesNotExist(
            allOf(withText(coreR.string.statistics_detail_chart_monthly), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withText(coreR.string.statistics_detail_chart_yearly), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(statisticsDetailR.id.rvStatisticsDetailSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag1, "1$hourString 0$minuteString", "33%")
        checkTagItem(color, tag2, "2$hourString 0$minuteString", "67%")
        checkNoTagItem("Untagged")

        // Next month
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailYear() {
        val name = "TypeName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.YEAR, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_year)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed()))
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "15$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailPrevious)
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(coreR.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_monthly,
            average = "5$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        clickOnChartGrouping(coreR.string.statistics_detail_chart_daily)
        checkViewDoesNotExist(
            allOf(withText(coreR.string.statistics_detail_chart_yearly), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed())
        )
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(3)

        // Next year
        clickOnViewWithId(statisticsDetailR.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailLastDays() {
        val name = "TypeName"
        val tag = "TagName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag, name)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagNames = listOf(tag)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, -6) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagNames = listOf(tag)
        )
        calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, -7) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_last)

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(statisticsDetailR.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )
        checkRangeAverages(
            rangeId = coreR.string.statistics_detail_chart_daily,
            average = "25$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed())
        )
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(coreR.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(4)

        // Tag split
        onView(withId(statisticsDetailR.id.rvStatisticsDetailSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "3$hourString 0$minuteString", "100%")
        checkNoTagItem(getString(coreR.string.change_record_untagged))
    }

    @Test
    fun rangeSaving() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name = name)
        testUtils.addRecord(name)

        // Open stat detail
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))

        // Change range
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        pressBack()

        // Range saved
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
    }

    @Test
    fun streaks() {
        val name = "name"

        // Add activity
        testUtils.addActivity(name)

        // Add records
        val difference = TimeUnit.HOURS.toMillis(1)
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 15) }

        fun addRecord(daysBefore: Int) {
            calendar.apply { add(Calendar.DATE, daysBefore) }
            testUtils.addRecord(
                typeName = name,
                timeStarted = calendar.timeInMillis,
                timeEnded = calendar.timeInMillis + difference,
            )
        }

        addRecord(0)
        addRecord(-1)
        addRecord(-1)

        addRecord(-2)
        addRecord(-1)
        addRecord(-1)
        addRecord(-1)
        addRecord(-1)

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }
        clickOnViewWithIdOnPager(statisticsDetailR.id.btnStatisticsDetailToday)
        clickOnViewWithText(coreR.string.range_overall)

        onView(withId(statisticsDetailR.id.cardStatisticsDetailStreaks)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_streaks_longest, "5")
        checkCard(coreR.string.statistics_detail_streaks_current, "3")

        // Streak type
        onView(withId(statisticsDetailR.id.buttonsStatisticsDetailStreaksType)).perform(nestedScrollTo())
        clickOnView(
            allOf(
                withText(coreR.string.statistics_detail_streaks_longest),
                isDescendantOfA(withId(statisticsDetailR.id.buttonsStatisticsDetailStreaksType))
            )
        )
        clickOnView(
            allOf(
                withText(coreR.string.statistics_detail_streaks_latest),
                isDescendantOfA(withId(statisticsDetailR.id.buttonsStatisticsDetailStreaksType))
            )
        )
    }

    private fun checkPreview(color: Int, icon: Int, name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                withCardColor(color),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun clickOnChartGrouping(withTextId: Int) {
        clickOnView(
            allOf(
                isDescendantOfA(withId(statisticsDetailR.id.buttonsStatisticsDetailGrouping)),
                withText(withTextId)
            )
        )
    }

    private fun clickOnSplitChartGrouping(withTextId: Int) {
        clickOnView(
            allOf(
                isDescendantOfA(withId(statisticsDetailR.id.buttonsStatisticsDetailSplitGrouping)),
                withText(withTextId)
            )
        )
    }

    private fun checkCard(cardTitleId: Int, text: String) {
        checkViewIsDisplayed(
            allOf(
                withText(cardTitleId),
                hasSibling(withText(text)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkRecordsCard(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkCards() {
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "3$hourString 0$minuteString")

        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(2)

        onView(withId(statisticsDetailR.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_shortest_record, "1$hourString 0$minuteString")
        checkCard(coreR.string.statistics_detail_average_record, "1$hourString 30$minuteString")
        checkCard(coreR.string.statistics_detail_longest_record, "2$hourString 0$minuteString")

        onView(withId(statisticsDetailR.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(coreR.string.statistics_detail_first_record))
        checkViewIsDisplayed(withText(coreR.string.statistics_detail_last_record))
    }

    private fun checkRangeAverages(
        rangeId: Int,
        average: String = "",
        checkAverage: Boolean = true,
        averageNonEmpty: String,
    ) {
        val range = getString(rangeId)
        val title = getString(coreR.string.statistics_detail_range_averages_title, range)

        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.cardStatisticsDetailRangeAverage),
                hasDescendant(withText(title)),
                if (checkAverage) {
                    hasDescendant(
                        allOf(
                            withText(coreR.string.statistics_detail_range_averages),
                            hasSibling(withText(average)),
                        )
                    )
                } else {
                    hasDescendant(withText(title))
                },
                hasDescendant(
                    allOf(
                        withText(coreR.string.statistics_detail_range_averages_non_empty),
                        hasSibling(withText(averageNonEmpty)),
                    )
                ),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkEmptyStatistics() {
        onView(withId(statisticsDetailR.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_total_duration, "0$secondString")

        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(0)

        onView(withId(statisticsDetailR.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_shortest_record, "-")
        checkCard(coreR.string.statistics_detail_average_record, "-")
        checkViewIsDisplayed(
            allOf(
                isDescendantOfA(withId(statisticsDetailR.id.cardStatisticsDetailAverage)),
                withText(coreR.string.statistics_detail_longest_record),
                hasSibling(withText("-")),
                isCompletelyDisplayed()
            )
        )

        onView(withId(statisticsDetailR.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkCard(coreR.string.statistics_detail_first_record, "-")
        checkCard(coreR.string.statistics_detail_last_record, "-")

        checkViewIsNotDisplayed(withId(statisticsDetailR.id.rvStatisticsDetailSplit))
    }

    private fun checkAllRecords(count: Int) {
        onView(withId(statisticsDetailR.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        tryAction { onView(withId(recordsAllR.id.rvRecordsAllList)).check(recyclerItemCount(count)) }
        pressBack()
    }

    private fun checkTagItem(color: Int, name: String, duration: String, percentage: String) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsTagItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withText(duration)),
                hasDescendant(withText(percentage))
            )
        )
    }

    private fun checkNoTagItem(name: String) {
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsTagItem),
                hasDescendant(withText(name))
            )
        )
    }
}
