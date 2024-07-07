package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
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
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteRecordTest : BaseUiTest() {

    @Test
    fun deleteRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Add record
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name)

        // Delete item
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordDelete)

        // Check message
        checkViewIsDisplayed(
            allOf(
                withText(getString(coreR.string.record_removed, name)),
                withId(com.google.android.material.R.id.snackbar_text),
            ),
        )

        // Record is deleted
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withCardColor(color), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withTag(icon), isCompletelyDisplayed()))

        // Check undo
        clickOnViewWithText(coreR.string.record_removed_undo)

        // Record is back
        checkViewIsDisplayed(
            CoreMatchers.allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun deleteRecordQuickAction() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecord(typeName = name)

        // Delete item
        NavUtils.openRecordsScreen()
        longClickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(withId(dialogsR.id.btnRecordQuickActionsDelete))
        clickOnViewWithId(dialogsR.id.btnRecordQuickActionsDelete)

        // Check message
        checkViewIsDisplayed(
            allOf(
                withText(getString(coreR.string.record_removed, name)),
                withId(com.google.android.material.R.id.snackbar_text),
            ),
        )

        // Record is deleted
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withCardColor(color), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withTag(icon), isCompletelyDisplayed()))

        // Check undo
        clickOnViewWithText(coreR.string.record_removed_undo)

        // Record is back
        checkViewIsDisplayed(
            CoreMatchers.allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                isCompletelyDisplayed(),
            ),
        )
    }
}
