package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteRunningRecordTest : BaseUiTest() {

    @Test
    fun deleteRunningRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add record
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }

        // Delete item
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordDelete)

        // Record is deleted
        checkViewDoesNotExist(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))
    }

    @Test
    fun deleteRunningRecordFromRecords() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add record
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }

        // Delete item
        NavUtils.openRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name), isCompletelyDisplayed()),
        )
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordDelete)

        // Record is deleted
        tryAction {
            checkViewDoesNotExist(
                allOf(
                    isDescendantOfA(withId(baseR.id.viewRunningRecordItem)),
                    withText(name),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }

    @Test
    fun deleteRunningRecordQuickAction() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add record
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(name) }

        // Delete item
        NavUtils.openRecordsScreen()
        longClickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name), isCompletelyDisplayed()),
        )
        checkViewIsDisplayed(withId(dialogsR.id.btnRecordQuickActionsDelete))
        clickOnViewWithId(dialogsR.id.btnRecordQuickActionsDelete)

        // Record is deleted
        tryAction {
            checkViewDoesNotExist(
                allOf(
                    isDescendantOfA(withId(baseR.id.viewRunningRecordItem)),
                    withText(name),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }
}
