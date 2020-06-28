package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.scrollToPosition
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class StartRecordTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

    @Inject
    lateinit var timeMapper: TimeMapper

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
    }

    @Test
    fun changeTest() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
        val lastColorPosition = ColorMapper.availableColors.size - 1
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()
        val lastIconPosition = iconMapper.availableIconsNames.size - 1

        // Add activities
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        pressBack()
        clickOnViewWithText(R.string.change_record_type_color_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(firstColor))
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(firstIcon)))
        clickOnViewWithText(R.string.change_record_type_save)

        clickOnViewWithText(R.string.running_records_add_type)
        typeTextIntoView(R.id.etChangeRecordTypeName, newName)
        pressBack()
        clickOnViewWithText(R.string.change_record_type_color_hint)
        scrollToPosition(R.id.rvChangeRecordTypeColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        scrollToPosition(R.id.rvChangeRecordTypeIcon, lastIconPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(lastIcon)))
        clickOnViewWithText(R.string.change_record_type_save)

        // Start timer
        clickOnViewWithText(name)
        var currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatTime(currentTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTagValue(equalTo(firstIcon))),
                hasDescendant(withText(timeStarted))
            )
        )

        // Start timer
        clickOnViewWithText(newName)
        currentTime = System.currentTimeMillis()
        timeStarted = timeMapper.formatTime(currentTime)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.layoutRunningRecordItem),
                withCardColor(lastColor),
                hasDescendant(withText(newName)),
                hasDescendant(withTagValue(equalTo(lastIcon))),
                hasDescendant(withText(timeStarted))
            )
        )

        // Stop timer
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name))
        )
        checkViewDoesNotExist(
            allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name))
        )

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Stop timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(newName))
        )
        checkViewDoesNotExist(
            allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(newName))
        )

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))


    }
}
