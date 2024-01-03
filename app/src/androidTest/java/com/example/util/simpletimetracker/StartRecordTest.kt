package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StartRecordTest : BaseUiTest() {

    @Test
    fun startRecord() {
        val name = "Test1"
        val newName = "Test2"
        val firstGoalTime = TimeUnit.MINUTES.toSeconds(10)

        // Add activities
        testUtils.addActivity(
            name = name,
            color = firstColor,
            icon = firstIcon,
            goals = listOf(
                RecordTypeGoal(
                    idData = RecordTypeGoal.IdData.Type(0),
                    range = RecordTypeGoal.Range.Session,
                    type = RecordTypeGoal.Type.Duration(firstGoalTime),
                    daysOfWeek = emptyList(),
                ),
            ),
        )
        testUtils.addActivity(name = newName, color = lastColor, icon = lastIcon)
        Thread.sleep(1000)

        // Start timer
        tryAction { clickOnViewWithText(name) }

        var currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatTime(time = currentTime, useMilitaryTime = true, showSeconds = false)
        val goalString = getString(coreR.string.change_record_type_session_goal_time).lowercase() + " 9$minuteString"
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(timeStarted)),
                hasDescendant(withSubstring(goalString)),
            ),
        )

        // Start timer
        clickOnViewWithText(newName)
        currentTime = System.currentTimeMillis()
        timeStarted = timeMapper.formatTime(time = currentTime, useMilitaryTime = true, showSeconds = false)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(lastColor),
                hasDescendant(withText(newName)),
                hasDescendant(withTag(lastIcon)),
                hasDescendant(withText(timeStarted)),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(newName)),
                hasDescendant(withSubstring(getString(coreR.string.change_record_type_session_goal_time))),
            ),
        )

        // Stop timer by clicking on running record
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name), isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name), isCompletelyDisplayed(),
            ),
        )

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withText(name), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed(),
            ),
        )

        // Stop timer by clicking on record type
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(newName), isCompletelyDisplayed(),
            ),
        )
        checkViewDoesNotExist(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(newName), isCompletelyDisplayed(),
            ),
        )

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withText(newName), isDescendantOfA(withId(baseR.id.viewRecordItem)), isCompletelyDisplayed(),
            ),
        )
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
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
        clickOnViewWithText(coreR.string.change_record_comment_field)
        clickOnViewWithText(coreR.string.change_record_save)

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))

        // Check record
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.etChangeRecordComment), withText(comment)))
    }

    @Test
    fun tagTransferFromTimerToRecord() {
        val name = "TypeName"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val fullName = "$name - $tag1, $tag2"

        // Add activities
        testUtils.addActivity(name)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2)

        // Start timer
        tryAction { clickOnViewWithText(name) }

        // Add tag
        tryAction { longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name))) }
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag1))
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnViewWithText(coreR.string.change_record_save)

        // Stop timer
        tryAction { clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(fullName))) }

        // Check record
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(fullName)),
                isCompletelyDisplayed(),
            ),
        )
        clickOnView(allOf(withText(fullName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), hasDescendant(withText(fullName))))
    }
}
