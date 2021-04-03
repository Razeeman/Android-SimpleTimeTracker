package com.example.util.simpletimetracker

import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withPluralText
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsDetailTest : BaseUiTest() {

    @Test
    fun statisticsDetail() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

        // Add record
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name)

        // Add record
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordAdd)

        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(15, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(17, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(R.string.change_record_save)

        // Check statistics
        NavUtils.openStatisticsScreen()

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(color),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsDetailItem),
                withCardColor(color),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(name))
            )
        )

        // Buttons
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)
        clickOnChartGrouping(R.string.statistics_detail_chart_weekly)
        clickOnChartGrouping(R.string.statistics_detail_chart_monthly)
        clickOnChartGrouping(R.string.statistics_detail_chart_yearly)
        clickOnChartGrouping(R.string.statistics_detail_chart_daily)

        clickOnViewWithText(R.string.statistics_detail_length_ten)
        clickOnViewWithText(R.string.statistics_detail_length_fifty)
        clickOnViewWithText(R.string.statistics_detail_length_hundred)
        clickOnViewWithText(R.string.statistics_detail_length_ten)

        // Total
        onView(withId(R.id.cardStatisticsDetailTotal)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_detail_total_duration),
                hasSibling(withText("3h 0m"))
            )
        )

        // Records
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 2),
                hasSibling(withText("2"))
            )
        )

        // Shortest
        onView(withId(R.id.cardStatisticsDetailAverage)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_detail_shortest_record),
                hasSibling(withText("1h 0m"))
            )
        )

        // Average
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_detail_average_record),
                hasSibling(withText("1h 30m"))
            )
        )

        // Longest
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_detail_longest_record),
                hasSibling(withText("2h 0m"))
            )
        )

        // First
        onView(withId(R.id.cardStatisticsDetailDates)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.statistics_detail_first_record))

        // Last
        checkViewIsDisplayed(withText(R.string.statistics_detail_last_record))
    }

    private fun clickOnChartGrouping(withTextId: Int) {
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsStatisticsDetailGrouping)),
                withText(withTextId)
            )
        )
    }
}
