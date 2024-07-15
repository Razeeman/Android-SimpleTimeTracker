package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ZeroDurationRecordTest : BaseUiTest() {

    @Test
    fun zeroDurationRecord() {
        val name = "Name"

        // Add activity
        testUtils.addActivity(name)

        // Add record
        NavUtils.openRecordsScreen()
        NavUtils.addRecordWithTime(
            name = name,
            hourStarted = 15,
            minutesStarted = 0,
            hourEnded = 15,
            minutesEnded = 0,
        )

        // Record added
        val record = allOf(
            withId(baseR.id.viewRecordItem),
            hasDescendant(withText(name)),
            hasDescendant(withText("0$minuteString")),
            isCompletelyDisplayed(),
        )
        checkViewIsDisplayed(record)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRecordTypeItem)),
                withText(coreR.string.untracked_time_name),
            ),
        )
        pressBack()

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.Total)
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1")),
            ),
        )

        // Check records all
        scrollStatDetailRecyclerToTag(StatisticsDetailBlock.Total)
        clickOnStatDetailRecycler(withPluralText(R.plurals.statistics_detail_times_tracked, 1))
        checkViewIsDisplayed(record)
    }
}
