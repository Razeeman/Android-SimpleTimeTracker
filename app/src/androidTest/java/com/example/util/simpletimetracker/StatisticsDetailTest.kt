package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

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
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.range_overall)
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        // Check one day
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarToday.get(Calendar.YEAR),
            monthStarted = calendarToday.get(Calendar.MONTH),
            dayStarted = calendarToday.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailPrevious), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailNext), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.cardStatisticsDetailRangeAverage), isCompletelyDisplayed()))
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "1$hourString 0$minuteString")
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(1)

        // Check two days
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarYesterday.get(Calendar.YEAR),
            monthStarted = calendarYesterday.get(Calendar.MONTH),
            dayStarted = calendarYesterday.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "1$hourString 30$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "3$hourString 0$minuteString")
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(2)

        // Check weeks
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevWeek.get(Calendar.YEAR),
            monthStarted = calendarPrevWeek.get(Calendar.MONTH),
            dayStarted = calendarPrevWeek.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "45$minuteString",
            averageNonEmpty = "2$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "3$hourString 0$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "6$hourString 0$minuteString")
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(3)

        // Check months
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevMonth.get(Calendar.YEAR),
            monthStarted = calendarPrevMonth.get(Calendar.MONTH),
            dayStarted = calendarPrevMonth.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "19$minuteString",
            averageNonEmpty = "2$hourString 30$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "2$hourString 0$minuteString",
            averageNonEmpty = "3$hourString 20$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "5$hourString 0$minuteString",
            averageNonEmpty = "5$hourString 0$minuteString"
        )
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "10$hourString 0$minuteString")
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(4)

        // Check years
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_custom)
        NavUtils.setCustomRange(
            yearStarted = calendarPrevYear.get(Calendar.YEAR),
            monthStarted = calendarPrevYear.get(Calendar.MONTH),
            dayStarted = calendarPrevYear.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendarToday.get(Calendar.YEAR),
            monthEnded = calendarToday.get(Calendar.MONTH),
            dayEnded = calendarToday.get(Calendar.DAY_OF_MONTH),
        )
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "2$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "16$minuteString",
            averageNonEmpty = "3$hourString 45$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "1$hourString 9$minuteString",
            averageNonEmpty = "5$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_yearly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_yearly,
            average = "7$hourString 30$minuteString",
            averageNonEmpty = "7$hourString 30$minuteString"
        )
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "15$hourString 0$minuteString")
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
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
        clickOnViewWithIdOnPager(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_overall)

        // Check buttons
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailPrevious), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailNext), isCompletelyDisplayed()))

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))

        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        clickOnViewWithText(R.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        clickOnViewWithText(R.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        clickOnViewWithText(R.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "36$secondString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )

        clickOnChartGrouping(R.string.statistics_detail_chart_yearly)
        clickOnViewWithText(R.string.statistics_detail_length_ten)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_yearly,
            average = "6$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_yearly,
            average = "3$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_yearly,
            average = "1$minuteString",
            averageNonEmpty = "1$hourString 30$minuteString"
        )
        clickOnViewWithText(R.string.statistics_detail_length_ten)

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(R.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed()))

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "2$hourString 0$minuteString", "67%")
        checkTagItem(
            R.color.colorUntracked,
            getString(R.string.change_record_untagged),
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
        clickOnViewWithIdOnPager(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_day)

        // Bar chart
        checkViewDoesNotExist(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.cardStatisticsDetailRangeAverage), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailSplitGrouping), isCompletelyDisplayed()))

        // Duration chart
        onView(withId(R.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed()))

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkNoTagItem(tag)
        checkTagItem(
            R.color.colorUntracked,
            getString(R.string.change_record_untagged),
            "3$hourString 0$minuteString",
            "100%"
        )

        // Next day
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
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
        clickOnViewWithIdOnPager(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_week)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "25$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            average = "8$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailNext)

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(R.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed()))

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "3$hourString 0$minuteString", "100%")
        checkNoTagItem(getString(R.string.change_record_untagged))

        // Next week
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
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
        clickOnViewWithIdOnPager(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_month)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkViewDoesNotExist(allOf(withText(R.string.statistics_detail_chart_monthly), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(R.string.statistics_detail_chart_yearly), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(R.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed()))

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag1, "1$hourString 0$minuteString", "33%")
        checkTagItem(color, tag2, "2$hourString 0$minuteString", "67%")
        checkNoTagItem("Untagged")

        // Next month
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
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
        clickOnViewWithIdOnPager(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_year)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "15$minuteString",
            averageNonEmpty = "3$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_daily,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_weekly,
            checkAverage = false,
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkRangeAverages(
            rangeId = R.string.statistics_detail_chart_monthly,
            average = "5$minuteString",
            averageNonEmpty = "1$hourString 0$minuteString"
        )
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        checkViewDoesNotExist(allOf(withText(R.string.statistics_detail_chart_yearly), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailSplitHint), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // Duration chart
        onView(withId(R.id.chartStatisticsDetailDurationSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailDurationSplit), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsDetailDurationSplitHint), isCompletelyDisplayed()))

        // All records
        checkAllRecords(3)

        // Next year
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailFilterByType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add activity
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withId(R.id.viewStatisticsDetailItem), hasDescendant(withText(name1))))
        checkRecordsCard(1)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(2)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(0)
    }

    @Test
    fun statisticsDetailFilterByCategory() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val name4 = "TypeName4"
        val categoryName1 = "CategoryName1"
        val categoryName2 = "CategoryName2"

        // Add data
        testUtils.addActivityTag(categoryName1)
        testUtils.addActivityTag(categoryName2)
        testUtils.addActivity(name = name1, categories = listOf(categoryName1))
        testUtils.addActivity(name = name2, categories = listOf(categoryName1))
        testUtils.addActivity(name = name3, categories = listOf(categoryName2))
        testUtils.addActivity(name = name4)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        testUtils.addRecord(name3)
        testUtils.addRecord(name4)

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter) }
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()
        tryAction { clickOnView(allOf(withText(categoryName1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withId(R.id.viewStatisticsDetailItem), hasDescendant(withText(categoryName1))))
        checkRecordsCard(2)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(categoryName2)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(3)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(categoryName1)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(categoryName2)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(0)
    }

    @Test
    fun statisticsDetailFilterByRecordTag() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2, tag3))

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withId(R.id.viewStatisticsDetailItem), hasDescendant(withText(name1))))
        checkRecordsCard(7)

        // Filter untagged records
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(R.string.change_record_untagged)))
        pressBack()
        checkRecordsCard(6)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(2)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        checkRecordsCard(1)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(0)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnViewWithText(R.string.types_filter_show_all)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(4)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(2)
    }

    private fun checkPreview(color: Int, icon: Int, name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsDetailItem),
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
                isDescendantOfA(withId(R.id.buttonsStatisticsDetailGrouping)),
                withText(withTextId)
            )
        )
    }

    private fun clickOnSplitChartGrouping(withTextId: Int) {
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsStatisticsDetailSplitGrouping)),
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
                withPluralText(R.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkCards() {
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "3$hourString 0$minuteString")

        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(2)

        onView(withId(R.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_shortest_record, "1$hourString 0$minuteString")
        checkCard(R.string.statistics_detail_average_record, "1$hourString 30$minuteString")
        checkCard(R.string.statistics_detail_longest_record, "2$hourString 0$minuteString")

        onView(withId(R.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.statistics_detail_first_record))
        checkViewIsDisplayed(withText(R.string.statistics_detail_last_record))
    }

    private fun checkRangeAverages(
        rangeId: Int,
        average: String = "",
        checkAverage: Boolean = true,
        averageNonEmpty: String,
    ) {
        val range = getString(rangeId)
        val title = getString(R.string.statistics_detail_range_averages_title, range)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.cardStatisticsDetailRangeAverage),
                hasDescendant(withText(title)),
                if (checkAverage) {
                    hasDescendant(
                        allOf(
                            withText(R.string.statistics_detail_range_averages),
                            hasSibling(withText(average)),
                        )
                    )
                } else {
                    hasDescendant(withText(title))
                },
                hasDescendant(
                    allOf(
                        withText(R.string.statistics_detail_range_averages_non_empty),
                        hasSibling(withText(averageNonEmpty)),
                    )
                ),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkEmptyStatistics() {
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, "0$secondString")

        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(0)

        onView(withId(R.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_shortest_record, "-")
        checkCard(R.string.statistics_detail_average_record, "-")
        checkCard(R.string.statistics_detail_longest_record, "-")

        onView(withId(R.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_first_record, "-")
        checkCard(R.string.statistics_detail_last_record, "-")

        checkViewIsNotDisplayed(withId(R.id.rvStatisticsDetailTagSplit))
    }

    private fun checkAllRecords(count: Int) {
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        tryAction { onView(withId(R.id.rvRecordsAllList)).check(recyclerItemCount(count)) }
        pressBack()
    }

    private fun checkTagItem(color: Int, name: String, duration: String, percentage: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsTagItem),
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
                withId(R.id.viewStatisticsTagItem),
                hasDescendant(withText(name))
            )
        )
    }
}
