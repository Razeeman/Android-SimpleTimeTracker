package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import com.google.android.material.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteRecordTypeTest : BaseUiTest() {

    @Test
    fun deleteRecordType() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add item
        Thread.sleep(1000)
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecord(name)

        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordTypeItem),
                    hasDescendant(withText(name)),
                    hasDescendant(withTag(icon)),
                    hasDescendant(withCardColor(color))
                )
            )
        }

        // Archive item
        longClickOnView(withText(name))
        checkViewIsDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeDelete))
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeDelete)
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.change_record_type_archived),
                withId(R.id.snackbar_text)
            )
        )

        // Record type is deleted
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                withCardColor(color)
            )
        )

        // Delete
        NavUtils.openSettingsScreen()
        NavUtils.openArchiveScreen()
        clickOnViewWithText(name)
        clickOnViewWithText(coreR.string.archive_dialog_delete)
        clickOnViewWithText(coreR.string.archive_dialog_delete)
        checkViewDoesNotExist(withText(name))
        tryAction { checkViewIsDisplayed(withText(coreR.string.archive_empty)) }
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.archive_activity_deleted),
                withId(R.id.snackbar_text)
            )
        )
        pressBack()

        // Record removed
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(withText(name))
    }
}
