package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.model.RepeatButtonType
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.R as coreR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordRepeatTest : BaseUiTest() {

    @Test
    fun visibility() {
        val type = "type"

        // Add data
        testUtils.addActivity(type)
        Thread.sleep(1000)

        // No records - no button
        checkViewIsDisplayed(withText(coreR.string.running_records_add_type))
        checkViewDoesNotExist(withText(coreR.string.running_records_repeat))

        // Add record
        testUtils.addRecord(type)
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(type), isCompletelyDisplayed()))

        // Button is shown
        NavUtils.openRunningRecordsScreen()
        checkViewIsDisplayed(withText(coreR.string.running_records_repeat))
    }

    @Test
    fun click() {
        val type = "type"
        val tag = "tag"
        val comment = "comment"
        val fullName = "$type - $tag"

        // Add data
        testUtils.addActivity(type)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = type,
            tagNames = listOf(tag),
            comment = comment,
        )
        Thread.sleep(1000)

        // Check
        clickOnViewWithText(coreR.string.running_records_repeat)
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(fullName)),
                    hasDescendant(withText(comment)),
                ),
            )
        }

        // Already started message
        clickOnViewWithText(coreR.string.running_records_repeat)
        clickOnViewWithText(coreR.string.running_records_repeat_already_tracking)

        // No records message
        runBlocking { prefsInteractor.setRepeatButtonType(RepeatButtonType.RepeatBeforeLast) }
        clickOnViewWithText(coreR.string.running_records_repeat)
        clickOnViewWithText(coreR.string.running_records_repeat_no_prev_record)
    }
}
