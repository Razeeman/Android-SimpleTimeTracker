package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
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
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsAllTest : BaseUiTest() {

    @Test
    fun recordsAll() {
        val name = "Test"

        // Add activity
        NavUtils.addActivity(name)

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
    fun recordsAllFilter() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Removed"
        val name4 = "WithRecords"

        // Add activity
        NavUtils.addActivity(name1)
        NavUtils.addActivity(name2)
        NavUtils.addActivity(name3)
        NavUtils.addActivity(name4)

        // Delete one activity
        longClickOnView(withText(name3))
        clickOnViewWithId(R.id.btnChangeRecordTypeDelete)

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name1)
        NavUtils.addRecord(name2)
        NavUtils.addRecord(name4)

        // Delete another activity
        NavUtils.openRunningRecordsScreen()
        longClickOnView(allOf(withText(name4), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnChangeRecordTypeDelete)

        // Open records all
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        Thread.sleep(1000)

        // Check records
        val record1 = allOf(withText(name1), isCompletelyDisplayed())
        val record2 = allOf(withText(name2), isCompletelyDisplayed())
        val record4 = allOf(withText(name4), isCompletelyDisplayed())

        checkViewIsDisplayed(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record4)

        // Change filter
        clickOnViewWithId(R.id.cardRecordsAllFilter)
        Thread.sleep(1000)
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))
        )
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
        checkViewDoesNotExist(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))
        )
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name4))
        )
        pressBack()

        // Check records
        checkViewIsDisplayed(record1)
        checkViewIsDisplayed(record2)
        checkViewDoesNotExist(record4)

        // Change filter
        clickOnViewWithId(R.id.cardRecordsAllFilter)
        Thread.sleep(1000)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()

        // Check records
        checkViewDoesNotExist(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record4)
        checkViewIsDisplayed(withText(R.string.records_empty))

        // Show all
        clickOnViewWithId(R.id.cardRecordsAllFilter)
        Thread.sleep(1000)
        clickOnViewWithId(R.id.btnTypesFilterShowAll)
        pressBack()

        // Check records
        checkViewIsDisplayed(record1)
        checkViewIsDisplayed(record2)
        checkViewIsDisplayed(record4)

        // Hide all
        clickOnViewWithId(R.id.cardRecordsAllFilter)
        Thread.sleep(1000)
        clickOnViewWithId(R.id.btnTypesFilterHideAll)
        pressBack()

        // Check records
        checkViewDoesNotExist(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record4)
        checkViewIsDisplayed(withText(R.string.records_empty))
    }

    @Test
    fun recordsAllDelete() {
        val name = "Test"

        // Add activity
        NavUtils.addActivity(name)

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
                withText(R.string.statistics_detail_times_tracked),
                hasSibling(withText("2"))
            )
        )

        // Open records all
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())

        // Records shown
        checkViewIsDisplayed(firstRecord)
        checkViewIsDisplayed(secondRecord)

        // Delete item
        longClickOnView(firstRecord)
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
                withText(R.string.statistics_detail_times_tracked),
                hasSibling(withText("1"))
            )
        )
    }
}
