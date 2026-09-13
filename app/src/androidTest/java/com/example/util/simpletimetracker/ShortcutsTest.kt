package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ShortcutsTest : BaseUiTest() {

    @Test
    fun actionVisibility() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRecord(name)
        calendar.timeInMillis = System.currentTimeMillis()
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
            timeEnded = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
        )
        testUtils.addRunningRecord(name)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        Thread.sleep(1000)

        // Running record - shown
        tryAction {
            longClickOnView(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
            )
        }
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Record - shown
        NavUtils.openRecordsScreen()
        clickOnView(
            allOf(withId(baseR.id.viewRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
        )
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Untracked - shown
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()
    }

    @Test
    fun add() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullRecordName = "$name - $tag"
        val fullShortcutName = "$name - $tag - $comment"

        // Setup
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(
            typeName = name,
            tagNames = listOf(tag),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        clickOnViewWithText(fullRecordName)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_shortcut)),
        )
        clickOnViewWithText(coreR.string.change_record_shortcut)

        // Check shortcut
        NavUtils.openRunningRecordsScreen()
        checkRecordShortcut(fullShortcutName)

        // Delete
        longClickOnView(withText(fullShortcutName))
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnViewWithText(R.string.ok)
        checkViewDoesNotExist(withText(fullShortcutName))
    }

    @Test
    fun startRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val fullRecordName = "$name - $tag"
        val fullShortcutName = "$name - $tag - $comment"

        // Setup
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag)
        testUtils.addShortcut(typeName = name, tagNames = listOf(tag), comment = comment)
        Thread.sleep(1000)

        // Check
        tryAction { checkRecordShortcut(fullShortcutName) }
        clickOnViewWithText(fullShortcutName)
        checkRunningRecord(fullRecordName, comment)
    }

    @Test
    fun changeSetting() {
        val name = "name"
        val multitaskingShortcutName = getString(coreR.string.settings_allow_multitasking)
        val retroactiveSettingShortcutName = getString(coreR.string.settings_retroactive_tracking_mode)
        val categoriesShortcutName = getString(coreR.string.categories_title)
        val archiveShortcutName = getString(coreR.string.settings_archive)
        val dataEditShortcutName = getString(coreR.string.settings_data_edit)
        val shortcutsShortcutName = getString(coreR.string.change_record_shortcut)
        val remindersShortcutName = getString(coreR.string.settings_reminders_title)

        // Add data
        runBlocking {
            prefsInteractor.setAllowMultitasking(false)
            prefsInteractor.setRetroactiveMultitaskingHintWasHidden(true)
        }
        testUtils.addActivity(name)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.Multitasking)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.RetroactiveMode)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.Categories)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.Archive)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.DataEdit)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.Shortcuts)
        testUtils.addSettingShortcut(RecordShortcut.SettingAction.Reminders)
        Thread.sleep(1000)

        // Check shortcuts
        tryAction {
            checkSettingShortcut(name = multitaskingShortcutName, isEnabled = false)
            checkSettingShortcut(name = retroactiveSettingShortcutName, isEnabled = false)
            checkSettingShortcut(name = categoriesShortcutName, isEnabled = false)
            checkSettingShortcut(name = archiveShortcutName, isEnabled = false)
            checkSettingShortcut(name = dataEditShortcutName, isEnabled = false)
            checkSettingShortcut(name = shortcutsShortcutName, isEnabled = false)
            checkSettingShortcut(name = remindersShortcutName, isEnabled = false)
        }

        // Multitasking
        clickOnView(allOf(withText(multitaskingShortcutName), isCompletelyDisplayed()))
        Thread.sleep(500) // Because of throttling
        checkSettingShortcut(name = multitaskingShortcutName, isEnabled = true)

        // Retroactive
        clickOnView(allOf(withText(retroactiveSettingShortcutName), isCompletelyDisplayed()))
        Thread.sleep(500) // Because of throttling
        checkSettingShortcut(name = retroactiveSettingShortcutName, isEnabled = true)
        tryAction {
            checkViewIsDisplayed(withText(R.string.retroactive_tracking_mode_hint))
        }

        // Categories
        clickOnView(allOf(withText(categoriesShortcutName), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        }
        pressBack()

        // Archive
        clickOnView(allOf(withText(archiveShortcutName), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.archive_empty))
        }
        pressBack()

        // Data edit
        clickOnView(allOf(withText(dataEditShortcutName), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.data_edit_select_records))
        }
        pressBack()

        // Shortcuts
        clickOnView(allOf(withText(shortcutsShortcutName), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.change_record_shortcut_hint))
        }
        pressBack()

        // Reminders
        clickOnView(allOf(withText(remindersShortcutName), isCompletelyDisplayed()))
        tryAction {
            checkViewIsDisplayed(withText(coreR.string.settings_reminders_hint))
        }
        pressBack()
    }

    @Suppress("SameParameterValue")
    private fun checkRecordShortcut(
        name: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordShortcutItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkSettingShortcut(
        name: String,
        isEnabled: Boolean,
    ) {
        val color = if (isEnabled) R.color.colorSecondary else R.color.colorInactive
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordShortcutItem),
                withCardColor(color),
                hasDescendant(withText(name)),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRunningRecord(
        name: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }
}
