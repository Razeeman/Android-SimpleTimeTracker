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
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsTest : BaseUiTest() {

    @Test
    fun statistics() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()

        // Add activities
        NavUtils.addActivity(name, firstColor, firstIcon)
        NavUtils.addActivity(newName, lastColor, lastIcon)

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name)
        NavUtils.addRecord(newName)

        NavUtils.openStatisticsScreen()

        // Check day range
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed())
        )

        // Switch to week range
        clickOnView(allOf(withText(R.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_week)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed())
        )

        // Switch to month range
        clickOnView(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_month)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withText("1h 0m")),
                hasDescendant(withSubstring("%")),
                isCompletelyDisplayed())
        )

        // Switch to overall range
        clickOnView(allOf(withText(R.string.title_this_month), isCompletelyDisplayed()))
        unconstrainedClickOnView(withText(R.string.title_overall))
        Thread.sleep(1000)

        checkViewDoesNotExist(
            allOf(
                withText(R.string.untracked_time_name),
                isCompletelyDisplayed()
            )
        )
        checkViewDoesNotExist(allOf(withText("1h 0m"), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(name)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(newName)),
                hasDescendant(withSubstring("50%")),
                isCompletelyDisplayed())
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
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(categoryName1, firstColor)
        NavUtils.addCategory(categoryName2, lastColor)
        NavUtils.addCategory(categoryName3)
        pressBack()

        // Add activities
        NavUtils.openRunningRecordsScreen()
        NavUtils.addActivity(typeName1, categories = listOf(categoryName1))
        NavUtils.addActivity(typeName2, categories = listOf(categoryName1))
        NavUtils.addActivity(typeName3, categories = listOf(categoryName2))
        NavUtils.addActivity(typeName4, categories = listOf(categoryName1, categoryName2))

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(typeName1)
        NavUtils.addRecord(typeName2)
        NavUtils.addRecord(typeName3)
        NavUtils.addRecord(typeName4)

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()

        // Check day range
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withText("3h 0m")),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withText("2h 0m")),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )

        // Switch to week range
        clickOnView(allOf(withText(R.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_week)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withText("3h 0m")),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withText("2h 0m")),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )

        // Switch to month range
        clickOnView(allOf(withText(R.string.title_this_week), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.title_this_month)

        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withText("3h 0m")),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withText("2h 0m")),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )

        // Switch to overall range
        clickOnView(allOf(withText(R.string.title_this_month), isCompletelyDisplayed()))
        unconstrainedClickOnView(withText(R.string.title_overall))
        Thread.sleep(1000)

        checkViewDoesNotExist(allOf(withText("3h 0m"), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(firstColor),
                hasDescendant(withText(categoryName1)),
                hasDescendant(withSubstring("60%")),
                isCompletelyDisplayed()
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutStatisticsItem),
                withCardColor(lastColor),
                hasDescendant(withText(categoryName2)),
                hasDescendant(withSubstring("40%")),
                isCompletelyDisplayed())
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.layoutStatisticsItem),
                hasDescendant(withText(categoryName3))
            )
        )
    }
}
