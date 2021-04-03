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
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.RecyclerItemCount
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withPluralText
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StatisticsDetailTest : BaseUiTest() {

    @Test
    fun statisticsDetailOverall() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

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
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkPreview(color, icon, name)

        // Check buttons
        checkViewDoesNotExist(
            allOf(withId(R.id.btnStatisticsDetailPrevious), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.btnStatisticsDetailNext), isCompletelyDisplayed())
        )

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        clickOnChartGrouping(R.string.statistics_detail_chart_yearly)

        clickOnViewWithText(R.string.statistics_detail_length_ten)
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        clickOnViewWithText(R.string.statistics_detail_length_ten)

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(4)
    }

    @Test
    fun statisticsDetailDay() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

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
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.title_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_today)

        // Bar chart
        checkViewDoesNotExist(
            allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailSplitGrouping), isCompletelyDisplayed())
        )

        // All records
        checkAllRecords(3)

        // Next day
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailWeek() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

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
            .apply { add(Calendar.DATE, -7) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.title_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_week)

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(3)

        // Next week
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailMonth() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

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
            .apply { add(Calendar.MONTH, -1) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.title_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_month)

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(3)

        // Next month
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    @Test
    fun statisticsDetailYear() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

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
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.title_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_year)

        // Bar chart
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed())
        )
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkViewDoesNotExist(
            allOf(withText(R.string.statistics_detail_chart_yearly), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed())
        )

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed())
        )
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(3)

        // Next year
        clickOnViewWithId(R.id.btnStatisticsDetailNext)
        checkEmptyStatistics()
    }

    private fun checkPreview(color: Int, icon: Int, name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsDetailItem),
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
        checkCard(R.string.statistics_detail_total_duration, "3h 0m")

        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(2)

        onView(withId(R.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_shortest_record, "1h 0m")
        checkCard(R.string.statistics_detail_average_record, "1h 30m")
        checkCard(R.string.statistics_detail_longest_record, "2h 0m")

        onView(withId(R.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.statistics_detail_first_record))
        checkViewIsDisplayed(withText(R.string.statistics_detail_last_record))
    }

    private fun checkEmptyStatistics() {
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_total_duration, " 0m")

        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkRecordsCard(0)

        onView(withId(R.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_shortest_record, "-")
        checkCard(R.string.statistics_detail_average_record, "-")
        checkCard(R.string.statistics_detail_longest_record, "-")

        onView(withId(R.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkCard(R.string.statistics_detail_first_record, "-")
        checkCard(R.string.statistics_detail_last_record, "-")
    }

    private fun checkAllRecords(count: Int) {
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        onView(withId(R.id.rvRecordsAllList)).check(RecyclerItemCount(count))
        pressBack()
    }
}
