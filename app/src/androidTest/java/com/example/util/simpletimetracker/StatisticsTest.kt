package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class StatisticsTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)

        testUtils.clearDatabase()
        testUtils.clearPrefs()
    }

    @Test
    fun test() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
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
        clickOnViewWithText(R.string.title_overall)

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
}
