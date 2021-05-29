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
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StatisticsDetailTest : BaseUiTest() {

    @Test
    fun statisticsDetailOverall() {
        val name = "TypeName"
        val tag = "TagName"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name, color, icon)
        testUtils.addRecordTag(name, tag)

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
            tagName = tag
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Check buttons
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailPrevious), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.btnStatisticsDetailNext), isCompletelyDisplayed()))

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
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
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "2h 0m", "67%")
        checkTagItem(R.color.colorUntracked, "Untagged", "1h 0m", "33%")

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
        testUtils.addActivity(name, color, icon)
        testUtils.addRecordTag(name, tag)

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
            tagName = tag
        )

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)

        // Bar chart
        checkViewDoesNotExist(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailSplitGrouping), isCompletelyDisplayed()))

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkNoTagItem(tag)
        checkTagItem(R.color.colorUntracked, "Untagged", "3h 0m", "100%")

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
        testUtils.addActivity(name, color, icon)
        testUtils.addRecordTag(name, tag)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagName = tag
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagName = tag
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
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_week)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag, "3h 0m", "100%")
        checkNoTagItem("Untagged")

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
        testUtils.addActivity(name, color, icon)
        testUtils.addRecordTag(name, tag1)
        testUtils.addRecordTag(name, tag2)

        // Add records
        var calendar = Calendar.getInstance()
            .apply { set(Calendar.HOUR_OF_DAY, 15) }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1),
            tagName = tag1
        )
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2),
            tagName = tag2
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
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_month)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailGrouping), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

        // All records
        checkAllRecords(3)

        // Tag split
        onView(withId(R.id.rvStatisticsDetailTagSplit)).perform(nestedScrollTo())
        checkTagItem(color, tag1, "1h 0m", "33%")
        checkTagItem(color, tag2, "2h 0m", "67%")
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
        tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }

        checkPreview(color, icon, name)

        // Switch range
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_year)

        // Bar chart
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetail), isCompletelyDisplayed()))
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        checkViewDoesNotExist(allOf(withText(R.string.statistics_detail_chart_yearly), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.buttonsStatisticsDetailLength), isCompletelyDisplayed()))

        // Cards
        checkCards()

        // Split chart
        onView(withId(R.id.chartStatisticsDetailSplit)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.chartStatisticsDetailSplit), isCompletelyDisplayed()))
        onView(withId(R.id.buttonsStatisticsDetailSplitGrouping)).perform(nestedScrollTo())
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_hourly)
        clickOnSplitChartGrouping(R.string.statistics_detail_chart_daily)

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
        checkViewIsDisplayed(allOf(withId(R.id.layoutStatisticsDetailItem), hasDescendant(withText(name1))))
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
        testUtils.addActivity(name1, categories = listOf(categoryName1))
        testUtils.addActivity(name2, categories = listOf(categoryName1))
        testUtils.addActivity(name3, categories = listOf(categoryName2))
        testUtils.addActivity(name4)

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
        checkViewIsDisplayed(allOf(withId(R.id.layoutStatisticsDetailItem), hasDescendant(withText(categoryName1))))
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
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(name1, tag1)
        testUtils.addRecordTag(name1, tag2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagName = tag1)
        testUtils.addRecord(name1, tagName = tag2)

        // Check detailed statistics
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withId(R.id.layoutStatisticsDetailItem), hasDescendant(withText(name1))))
        checkRecordsCard(3)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(R.string.change_record_untagged)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(2)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag1)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(1)

        // Change filter
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewCategoryItem)), withText(tag2)))
        pressBack()

        // Check detailed statistics
        checkRecordsCard(0)
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
                withId(R.id.layoutStatisticsItem),
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
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(name))
            )
        )
    }
}
