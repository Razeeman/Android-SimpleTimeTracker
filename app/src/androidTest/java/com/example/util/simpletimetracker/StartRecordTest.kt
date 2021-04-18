package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
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
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class StartRecordTest : BaseUiTest() {

    @Test
    fun startRecord() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val firstIcon = iconImageMapper.availableIconsNames.values.first()
        val lastIcon = iconImageMapper.availableIconsNames.values.last()
        val firstGoalTime = TimeUnit.MINUTES.toSeconds(10)

        // Add activities
        testUtils.addActivity(name, firstColor, firstIcon, firstGoalTime)
        testUtils.addActivity(newName, lastColor, lastIcon)

        // Start timer
        tryAction { clickOnViewWithText(name) }

        var currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatTime(currentTime, true)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(timeStarted)),
                hasDescendant(withText("goal 10m"))
            )
        )

        // Start timer
        clickOnViewWithText(newName)
        currentTime = System.currentTimeMillis()
        timeStarted = timeMapper.formatTime(currentTime, true)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                withCardColor(lastColor),
                hasDescendant(withText(newName)),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(timeStarted))
            )
        )
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(newName)),
                hasDescendant(withSubstring("goal"))
            )
        )

        // Click on already running
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name)))
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))

        // Stop timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        checkViewDoesNotExist(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Stop timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(newName)))
        checkViewDoesNotExist(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(newName)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
    }

    @Test
    fun commentTransferFromTimerToRecord() {
        val name = "Test"
        val comment = "comment"

        // Add activities
        NavUtils.addActivity(name)

        // Start timer
        clickOnViewWithText(name)

        // Add comment
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        typeTextIntoView(R.id.etChangeRunningRecordComment, comment)
        clickOnViewWithText(R.string.change_running_record_save)

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        // Check record
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed()
            )
        )
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordComment), withText(comment)))
    }
}
