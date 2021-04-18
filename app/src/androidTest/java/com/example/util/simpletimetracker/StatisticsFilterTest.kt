package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsFilterTest : BaseUiTest() {

    @Test
    fun statisticsFilter() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val firstIcon = iconImageMapper.availableIconsNames.values.first()
        val lastIcon = iconImageMapper.availableIconsNames.values.last()

        // Add activities
        testUtils.addActivity(name, firstColor, firstIcon)
        testUtils.addActivity(newName, lastColor, lastIcon)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name)
        testUtils.addRecord(newName)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))

        // Filter untracked
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.viewRecordTypeItem)),
                withText(R.string.untracked_time_name)
            )
        )
        pressBack()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))

        // Filter activity
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name)))
        pressBack()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("1h 0m")))

        // Filter all
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(newName)))
        pressBack()
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withId(R.id.tvStatisticsInfoText), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Remove filters
        clickOnViewWithIdOnPager(R.id.btnStatisticsEmptyFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.viewRecordTypeItem)),
                withText(R.string.untracked_time_name)
            )
        )
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(newName)))
        pressBack()
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))
    }
}
