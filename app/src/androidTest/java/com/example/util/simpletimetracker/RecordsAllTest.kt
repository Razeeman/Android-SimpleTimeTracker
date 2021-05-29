package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.withPluralText
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsAllTest : BaseUiTest() {

    @Test
    fun recordsAll() {
        val name = "Test"
        val comment = "comment"

        // Add activity
        testUtils.addActivity(name)

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecordWithTime(
            name = name,
            hourStarted = 15,
            minutesStarted = 0,
            hourEnded = 16,
            minutesEnded = 0
        )

        val firstRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(name)),
            hasDescendant(withText("1h 0m")),
            isCompletelyDisplayed()
        )

        // Record added
        checkViewIsDisplayed(firstRecord)

        // Add another record
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        NavUtils.addRecordWithTime(
            name = name,
            hourStarted = 17,
            minutesStarted = 0,
            hourEnded = 19,
            minutesEnded = 0,
            comment = comment
        )

        val secondRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(name)),
            hasDescendant(withText("2h 0m")),
            hasDescendant(withText(comment)),
            isCompletelyDisplayed()
        )

        // Another record added
        checkViewIsDisplayed(secondRecord)

        // Open statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        // Open records all
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())

        // Records shown
        checkViewIsDisplayed(firstRecord)
        checkViewIsDisplayed(secondRecord)
        onView(firstRecord).check(isCompletelyAbove(secondRecord))

        // Sort by duration
        clickOnViewWithId(R.id.spinnerRecordsAllSort)
        clickOnViewWithText(R.string.records_all_sort_duration)

        // Check new order
        onView(secondRecord).check(isCompletelyAbove(firstRecord))
    }

    @Test
    fun recordsAllDelete() {
        val name = "Test"

        // Add activity
        testUtils.addActivity(name)

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecordWithTime(
            name = name,
            hourStarted = 15,
            minutesStarted = 0,
            hourEnded = 16,
            minutesEnded = 0
        )

        val firstRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(name)),
            hasDescendant(withText("1h 0m")),
            isCompletelyDisplayed()
        )

        // Record added
        checkViewIsDisplayed(firstRecord)

        // Add another record
        NavUtils.addRecordWithTime(
            name = name,
            hourStarted = 17,
            minutesStarted = 0,
            hourEnded = 19,
            minutesEnded = 0
        )

        val secondRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(name)),
            hasDescendant(withText("2h 0m")),
            isCompletelyDisplayed()
        )

        // Another record added
        checkViewIsDisplayed(secondRecord)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 2),
                hasSibling(withText("2"))
            )
        )

        // Open records all
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())

        // Records shown
        checkViewIsDisplayed(firstRecord)
        checkViewIsDisplayed(secondRecord)

        // Delete item
        clickOnView(firstRecord)
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        clickOnViewWithId(R.id.btnChangeRecordDelete)

        // TODO Check message

        // Record is deleted
        checkViewDoesNotExist(firstRecord)
        checkViewIsDisplayed(secondRecord)

        // Check detailed statistics
        pressBack()
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                hasSibling(withText("1"))
            )
        )
    }

    @Test
    fun recordsAllCategories() {
        val typeName1 = "type1"
        val typeName2 = "type2"
        val typeName3 = "type3"
        val color1 = ColorMapper.getAvailableColors()[0]
        val color2 = ColorMapper.getAvailableColors()[1]
        val color3 = ColorMapper.getAvailableColors()[2]
        val categoryName1 = "category1"
        val categoryName2 = "category2"

        // Add categories
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1)
        NavUtils.addCategory(categoryName2)
        pressBack()

        // Add activity
        NavUtils.openRunningRecordsScreen()
        testUtils.addActivity(typeName1, color1, categories = listOf(categoryName1))
        testUtils.addActivity(typeName2, color2, categories = listOf(categoryName1))
        testUtils.addActivity(typeName3, color3, categories = listOf(categoryName2))

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(typeName3)
        NavUtils.addRecordWithTime(
            name = typeName1,
            hourStarted = 15,
            minutesStarted = 0,
            hourEnded = 16,
            minutesEnded = 0
        )

        val firstRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(typeName1)),
            hasDescendant(withText("1h 0m")),
            isCompletelyDisplayed()
        )

        // Record added
        checkViewIsDisplayed(firstRecord)

        // Add another record
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        NavUtils.addRecordWithTime(
            name = typeName2,
            hourStarted = 17,
            minutesStarted = 0,
            hourEnded = 19,
            minutesEnded = 0
        )

        val secondRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(typeName2)),
            hasDescendant(withText("2h 0m")),
            isCompletelyDisplayed()
        )

        // Another record added
        checkViewIsDisplayed(secondRecord)

        // Open statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()
        clickOnView(allOf(withText(categoryName1), isCompletelyDisplayed()))

        // Open records all
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())

        // Records shown
        checkViewIsDisplayed(firstRecord)
        checkViewIsDisplayed(secondRecord)
        onView(firstRecord).check(isCompletelyAbove(secondRecord))

        // Sort by duration
        clickOnViewWithId(R.id.spinnerRecordsAllSort)
        clickOnViewWithText(R.string.records_all_sort_duration)

        // Check new order
        onView(secondRecord).check(isCompletelyAbove(firstRecord))
    }
}
