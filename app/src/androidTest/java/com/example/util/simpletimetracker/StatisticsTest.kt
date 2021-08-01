package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
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
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsTest : BaseUiTest() {

    @Test
    fun statistics() {
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

        // Check day range
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to week range
        clickOnView(allOf(withText(R.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_week)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to month range
        clickOnView(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_month)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to year range
        clickOnView(allOf(withText(R.string.title_this_month), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_year)
        checkRecordsRange(firstColor, lastColor, firstIcon, lastIcon, name, newName)

        // Switch to overall range
        clickOnView(allOf(withText(R.string.title_this_year), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_overall)
        Thread.sleep(1000)

        checkViewDoesNotExist(
            allOf(
                withText(R.string.untracked_time_name),
                isCompletelyDisplayed()
            )
        )
        checkViewDoesNotExist(allOf(withText("1$hourString 0m"), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvStatisticsInfoText),
                withText("2$hourString 0$minuteString")
            )
        )
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_hint),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun statisticsCategories() {
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val typeName3 = "Type3"
        val typeName4 = "Type4"
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val categoryName3 = "Category3"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()

        // Add categories
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1, firstColor)
        NavUtils.addCategory(categoryName2, lastColor)
        NavUtils.addCategory(categoryName3)
        pressBack()

        // Add activities
        NavUtils.openRunningRecordsScreen()
        testUtils.addActivity(typeName1, categories = listOf(categoryName1))
        testUtils.addActivity(typeName2, categories = listOf(categoryName1))
        testUtils.addActivity(typeName3, categories = listOf(categoryName2))
        testUtils.addActivity(typeName4, categories = listOf(categoryName1, categoryName2))

        // Add records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName1)
        testUtils.addRecord(typeName2)
        testUtils.addRecord(typeName3)
        testUtils.addRecord(typeName4)

        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()

        // Check day range
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to week range
        clickOnView(allOf(withText(R.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_week)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to month range
        clickOnView(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_month)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to year range
        clickOnView(allOf(withText(R.string.title_this_month), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_year)
        checkCategoryRange(firstColor, lastColor, categoryName1, categoryName2, categoryName3)

        // Switch to overall range
        clickOnView(allOf(withText(R.string.title_this_year), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_overall)
        Thread.sleep(1000)

        checkViewDoesNotExist(allOf(withText("3$hourString 0$minuteString"), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvStatisticsInfoText),
                withText("5$hourString 0$minuteString")
            )
        )
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_hint),
                isCompletelyDisplayed()
            )
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )
    }

    private fun checkRecordsRange(
        firstColor: Int,
        lastColor: Int,
        firstIcon: Int,
        lastIcon: Int,
        name: String,
        newName: String
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsInfoItem),
                hasDescendant(withText(R.string.statistics_total_tracked)),
                hasDescendant(withText("2$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_hint),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withText("100%")),
                hasDescendant(withSubstring("$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsInfoItem),
                hasDescendant(withText(R.string.statistics_total_tracked)),
                hasDescendant(withText(" 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
        checkViewDoesNotExist(
            allOf(
                withText(R.string.statistics_hint),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
    }

    private fun checkCategoryRange(
        firstColor: Int,
        lastColor: Int,
        categoryName1: String,
        categoryName2: String,
        categoryName3: String
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withText("3$hourString 0$minuteString")),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withText("2$hourString 0$minuteString")),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvStatisticsInfoText),
                withText("5$hourString 0$minuteString")
            )
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_hint),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withText(R.string.statistics_empty),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
    }
}
