package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRecordTypeTest : BaseUiTest() {

    @Test
    fun changeRecordType() {
        val name = "Test"
        val newName = "Updated"

        // Add item
        NavUtils.addActivity(name, firstColor, firstIcon)

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeDelete))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeIcon))
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.etChangeRecordTypeName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Change item
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(firstColor)))
        )
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(lastColor)))
        )
        clickOnViewWithText(coreR.string.change_record_type_color_hint)

        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        onView(withId(changeRecordTypeR.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeIcon, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeIcon, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type updated
        tryAction { checkViewIsDisplayed(withText(newName)) }
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))

        // Change again
        longClickOnView(withText(newName))
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnChangeRecordTypeIconSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint)
            )
        )
        clickOnViewWithText(firstEmoji)

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(firstEmoji)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type updated
        tryAction { checkViewIsDisplayed(withText(firstEmoji)) }
    }

    @Test
    fun runningRecordChangeRecordType() {
        val name = "name"
        val newName = "new name"

        // Add activity
        NavUtils.addActivity(name, firstColor, firstIcon)

        // Start timer
        tryAction { clickOnViewWithText(name) }
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon))
            )
        )
        checkViewDoesNotExist(withSubstring(getString(coreR.string.change_record_type_session_goal_time).lowercase()))

        // Change activity
        longClickOnView(
            allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRecordTypeItem)), withText(name))
        )

        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, newName)

        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnViewWithText(coreR.string.change_record_type_color_hint)

        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        onView(withId(changeRecordTypeR.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeIcon, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeIcon, withTag(lastIcon))
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)

        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
        clickOnViewWithId(changeRecordTypeR.id.groupChangeRecordTypeSessionGoalTime)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsDisplayed(withText("10$minuteString")) }
        clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record running record updated
        tryAction {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRunningRecordItem)), withText(newName))
            )
        }
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewRunningRecordItem), withCardColor(lastColor))
        )
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRunningRecordItem)), withTag(lastIcon))
        )
        checkViewIsDisplayed(withSubstring(getString(coreR.string.change_record_type_session_goal_time).lowercase()))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.previewChangeRecordType), matcher))
}
