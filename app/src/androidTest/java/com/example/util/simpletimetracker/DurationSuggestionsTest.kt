package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.nestedScrollTo
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_goals.R as changeGoalsR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DurationSuggestionsTest : BaseUiTest() {

    private val defaultValues = listOf(
        "1$minuteString",
        "15$minuteString",
        "1$hourString",
    )

    @Test
    fun default() {
        openTypeEdit()
        openDialog()

        defaultValues.forEach { checkViewIsDisplayed(withText(it)) }
    }

    @Test
    fun selecting() {
        openTypeEdit()

        defaultValues.forEach {
            openDialog()
            clickOnViewWithText(it)
            clickOnViewWithText(coreR.string.duration_dialog_save)
            checkViewIsDisplayed(
                allOf(
                    GoalsTestUtils.visibleGoalField(changeGoalsR.id.tvChangeRecordTypeGoalDurationValue),
                    withText(it),
                ),
            )
        }
    }

    @Test
    fun deleting() {
        openTypeEdit()
        openDialog()

        defaultValues.forEach {
            longClickOnView(withText(it))
            checkViewDoesNotExist(withText(it))
        }
    }

    @Test
    fun addAndSelect() {
        val text = "13$minuteString"

        openTypeEdit()
        openDialog()

        // Adding
        checkViewDoesNotExist(withText(text))
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard3)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.running_records_add_type)

        // Selecting
        checkViewIsDisplayed(withText(text))
        longClickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete)
        clickOnViewWithText(text)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(
            allOf(
                GoalsTestUtils.visibleGoalField(changeGoalsR.id.tvChangeRecordTypeGoalDurationValue),
                withText(text),
            ),
        )

        // Deleting
        openDialog()
        longClickOnView(withText(text))
        checkViewDoesNotExist(withText(text))
    }

    private fun openTypeEdit() {
        clickOnViewWithText(coreR.string.running_records_add_type)
        closeSoftKeyboard()
        onView(withText(coreR.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
        GoalsTestUtils.addGoal()
    }

    private fun openDialog() {
        clickOnView(GoalsTestUtils.visibleGoalField(changeGoalsR.id.fieldChangeRecordTypeGoalDuration))
    }
}
