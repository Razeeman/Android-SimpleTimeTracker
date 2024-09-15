package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordTypeDefaultDurationTest : BaseUiTest() {

    @Test
    fun change() {
        val type1 = "type1"

        // Add
        clickOnViewWithText(R.string.running_records_add_type)
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, type1)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue),
                withText(R.string.change_record_type_goal_time_disabled),
            ),
        )
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Change
        longClickOnView(withText(type1))
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        clickOnViewWithId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString"))
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Disable
        longClickOnView(withText(type1))
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        clickOnViewWithText("1$minuteString")
        clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete)
        clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete)
        clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue),
                withText(R.string.change_record_type_goal_time_disabled),
            ),
        )
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Check
        longClickOnView(withText(type1))
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue),
                withText(R.string.change_record_type_goal_time_disabled),
            ),
        )
    }

    @Test
    fun start() {
        val type1 = "type1"
        val type2 = "type2"

        // Add data
        testUtils.addActivity(type1, defaultDuration = TimeUnit.MINUTES.toSeconds(1))
        testUtils.addActivity(type2)
        Thread.sleep(1000)

        // Start
        clickOnViewWithText(type1)
        clickOnViewWithText(type2)
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(type1)),
                hasDescendant(withText("1$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(type2)),
                isCompletelyDisplayed(),
            ),
        )
    }
}
