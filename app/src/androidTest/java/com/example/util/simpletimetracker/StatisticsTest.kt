package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsTest : BaseUiTest() {

    @Test
    fun statistics() {
        val name = "Test1"
        val newName = "Test2"

        // Add activities
        testUtils.addActivity(name = name, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name = newName, color = lastColor, icon = lastIcon)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(name)
        testUtils.addRecord(newName)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 10)
        testUtils.addRecord(name, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()

        // Check day range
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to week range
        clickOnView(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_week)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to month range
        clickOnView(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_month)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to year range
        clickOnView(allOf(withText(coreR.string.title_this_month), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_year)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to last days range
        clickOnView(allOf(withText(coreR.string.title_this_year), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_last)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName, checkPrevious = false)

        // Switch to overall range
        clickOnView(allOf(withText(coreR.string.range_last), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_overall)
        Thread.sleep(1000)

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.untracked_time_name)
        pressBack()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.tvStatisticsInfoText),
                withText("2$hourString 0$minuteString"),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun statisticsCategories() {
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val typeName3 = "Type3"
        val typeName4 = "Type4"
        val typeName5 = "Type5"
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val categoryName3 = "Category3"

        // Add categories
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1, firstColor)
        NavUtils.addCategory(categoryName2, lastColor)
        NavUtils.addCategory(categoryName3)
        pressBack()

        // Add activities
        NavUtils.openRunningRecordsScreen()
        testUtils.addActivity(name = typeName1, categories = listOf(categoryName1))
        testUtils.addActivity(name = typeName2, categories = listOf(categoryName1))
        testUtils.addActivity(name = typeName3, categories = listOf(categoryName2))
        testUtils.addActivity(name = typeName4, categories = listOf(categoryName1, categoryName2))
        testUtils.addActivity(name = typeName5)

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName1)
        testUtils.addRecord(typeName2)
        testUtils.addRecord(typeName3)
        testUtils.addRecord(typeName4)
        testUtils.addRecord(typeName5)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 10)
        testUtils.addRecord(typeName1, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()

        // Check day range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to week range
        clickOnView(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_week)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to month range
        clickOnView(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_month)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to year range
        clickOnView(allOf(withText(coreR.string.title_this_month), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_year)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to last days range
        clickOnView(allOf(withText(coreR.string.title_this_year), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_last)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3, checkPrevious = false)

        // Switch to overall range
        clickOnView(allOf(withText(coreR.string.range_last), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_overall)
        Thread.sleep(1000)

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.untracked_time_name)
        pressBack()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withSubstring("33%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.uncategorized_time_name)),
                hasDescendant(withSubstring("17%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.tvStatisticsInfoText),
                withText("6$hourString 0$minuteString"),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(categoryName3)),
            ),
        )
    }

    @Test
    fun statisticsTags() {
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val typeName3 = "Type3"
        val typeName4 = "Type4"
        val tagName1 = "Tag1"
        val tagName2 = "Tag2"
        val tagName3 = "Tag3"

        // Add activities
        NavUtils.openRunningRecordsScreen()
        testUtils.addActivity(name = typeName1, color = firstColor)
        testUtils.addActivity(name = typeName2)
        testUtils.addActivity(name = typeName3)
        testUtils.addActivity(name = typeName4)

        // Add tags
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addRecordTag(tagName1, typeName1)
        NavUtils.addRecordTag(tagName2, null, lastColor)
        NavUtils.addRecordTag(tagName3)
        pressBack()

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName1, tagNames = listOf(tagName1))
        testUtils.addRecord(typeName2, tagNames = listOf(tagName2))
        testUtils.addRecord(typeName3, tagNames = listOf(tagName2))
        testUtils.addRecord(typeName1, tagNames = listOf(tagName1, tagName2))
        testUtils.addRecord(typeName4)
        val before = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 10)
        testUtils.addRecord(typeName1, timeStarted = before, timeEnded = before)

        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.record_tag_hint_short)
        pressBack()

        // Check day range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkTagRange(firstColor, lastColor, tagName1, tagName2, tagName3)

        // Switch to week range
        clickOnView(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_week)
        checkTagRange(firstColor, lastColor, tagName1, tagName2, tagName3)

        // Switch to month range
        clickOnView(allOf(withText(coreR.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_month)
        checkTagRange(firstColor, lastColor, tagName1, tagName2, tagName3)

        // Switch to year range
        clickOnView(allOf(withText(coreR.string.title_this_month), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_year)
        checkTagRange(firstColor, lastColor, tagName1, tagName2, tagName3)

        // Switch to last days range
        clickOnView(allOf(withText(coreR.string.title_this_year), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_last)
        checkTagRange(firstColor, lastColor, tagName1, tagName2, tagName3, checkPrevious = false)

        // Switch to overall range
        clickOnView(allOf(withText(coreR.string.range_last), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_overall)
        Thread.sleep(1000)

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("100%")),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.untracked_time_name)
        pressBack()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(tagName1)),
                hasDescendant(withSubstring("33%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(tagName2)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.change_record_untagged)),
                hasDescendant(withSubstring("17%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.tvStatisticsInfoText),
                withText("6$hourString 0$minuteString"),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(tagName3)),
            ),
        )
    }

    private fun checkRecordsRange(
        firstColor: Int,
        lastColor: Int,
        firstIcon: Int,
        lastIcon: Int,
        name: String,
        newName: String,
        checkPrevious: Boolean = true,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.layoutStatisticsInfoItem),
                hasDescendant(withText(coreR.string.statistics_total_tracked)),
                hasDescendant(withText("2$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        if (!checkPrevious) return
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText("100%")),
                hasDescendant(withSubstring("$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.layoutStatisticsInfoItem),
                hasDescendant(withText(coreR.string.statistics_total_tracked)),
                hasDescendant(withText("0$secondString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
    }

    private fun checkCategoryRange(
        firstColor: Int,
        lastColor: Int,
        categoryName1: String,
        categoryName2: String,
        categoryName3: String,
        checkPrevious: Boolean = true,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withText("3$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withText("2$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.uncategorized_time_name)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.tvStatisticsInfoText),
                withText("6$hourString 0$minuteString"),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(categoryName3)),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        if (!checkPrevious) return
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText("100%")),
                hasDescendant(withSubstring("$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.layoutStatisticsInfoItem),
                hasDescendant(withText(coreR.string.statistics_total_tracked)),
                hasDescendant(withText("0$secondString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
    }

    private fun checkTagRange(
        firstColor: Int,
        lastColor: Int,
        tagName1: String,
        tagName2: String,
        tagName3: String,
        checkPrevious: Boolean = true,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(tagName1)),
                hasDescendant(withText("2$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(tagName2)),
                hasDescendant(withText("3$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.change_record_untagged)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.tvStatisticsInfoText),
                withText("6$hourString 0$minuteString"),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(tagName3)),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        if (!checkPrevious) return
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText("100%")),
                hasDescendant(withSubstring("$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(statisticsR.id.layoutStatisticsInfoItem),
                hasDescendant(withText(coreR.string.statistics_total_tracked)),
                hasDescendant(withText("0$secondString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withText(coreR.string.statistics_hint),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
    }
}
