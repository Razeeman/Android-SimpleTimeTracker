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
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordsAllTest : BaseUiTest() {

    @Test
    fun recordsAll() {
        val name = "TypeName"
        val comment = "comment"
        val tag = "TagName"
        val fullName = "$name - $tag"

        // Add activity
        testUtils.addActivity(name)
        testUtils.addRecordTag(name, tag)

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
            comment = comment,
            tag = tag
        )

        val secondRecord = allOf(
            withId(R.id.viewRecordItem),
            hasDescendant(withText(fullName)),
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
        tryAction { onView(secondRecord).check(isCompletelyAbove(firstRecord)) }
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

        // Check message
        checkViewIsDisplayed(
            allOf(
                withText("Record $name removed"),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )

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
        tryAction { onView(secondRecord).check(isCompletelyAbove(firstRecord)) }
    }

    @Test
    fun recordsAllFilter() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activity
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        testUtils.addRecord(name3)

        // Open records all
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())

        // Check records
        val record1 = allOf(withText(name1), isCompletelyDisplayed())
        val record2 = allOf(withText(name2), isCompletelyDisplayed())
        val record3 = allOf(withText(name3), isCompletelyDisplayed())

        checkViewIsDisplayed(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record3)

        // Change filter
        pressBack()
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()

        // Check records
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(record1)
        checkViewIsDisplayed(record2)
        checkViewDoesNotExist(record3)

        // Change filter
        pressBack()
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()

        // Check records
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        checkViewDoesNotExist(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record3)
        checkViewIsDisplayed(withText(R.string.records_empty))

        // Show all
        pressBack()
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnViewWithId(R.id.btnTypesFilterShowAll)
        pressBack()

        // Check records
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(record1)
        checkViewIsDisplayed(record2)
        checkViewIsDisplayed(record3)

        // Hide all
        pressBack()
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        clickOnViewWithId(R.id.btnTypesFilterHideAll)
        pressBack()

        // Check records
        onView(withId(R.id.cardStatisticsDetailRecords)).perform(nestedScrollTo(), click())
        checkViewDoesNotExist(record1)
        checkViewDoesNotExist(record2)
        checkViewDoesNotExist(record3)
        checkViewIsDisplayed(withText(R.string.records_empty))
    }
}
