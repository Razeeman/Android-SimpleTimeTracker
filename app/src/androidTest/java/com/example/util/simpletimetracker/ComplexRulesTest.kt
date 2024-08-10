package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.DayOfWeek
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
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import com.example.util.simpletimetracker.feature_complex_rules.R as complexRulesR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ComplexRulesTest : BaseUiTest() {

    @Test
    fun list() {
        val checkActionAllowMultitaskingType = "checkActionAllowMultitaskingType"
        val checkActionDisallowMultitaskingType = "checkActionDisallowMultitaskingType"
        val checkActionAssignTagType = "checkActionAssignTagType"
        val checkStartingType1 = "checkStartingType1"
        val checkStartingType2 = "checkStartingType2"
        val checkCurrentType1 = "checkCurrentType1"
        val checkCurrentType2 = "checkCurrentType2"
        val checkDaysType = "checkDaysType"
        val checkAllType1 = "checkAllType1"
        val checkAllType2 = "checkAllType2"
        val tagName1 = "tagName1"
        val tagName2 = "tagName2"
        val tagName3 = "tagName3"

        // Add data
        testUtils.addActivity(checkActionAllowMultitaskingType)
        testUtils.addActivity(checkActionDisallowMultitaskingType)
        testUtils.addActivity(checkActionAssignTagType)
        testUtils.addActivity(checkStartingType1)
        testUtils.addActivity(checkStartingType2)
        testUtils.addActivity(checkCurrentType1)
        testUtils.addActivity(checkCurrentType2)
        testUtils.addActivity(checkDaysType)
        testUtils.addActivity(checkAllType1)
        testUtils.addActivity(checkAllType2)
        testUtils.addRecordTag(tagName1)
        testUtils.addRecordTag(tagName2)
        testUtils.addRecordTag(tagName3)

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        NavUtils.openComplexRules()

        clickOnViewWithText(R.string.running_records_add_type)

        // Check actions
        clickOnViewWithText(R.string.change_complex_rule_choose_action)
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkActionAllowMultitaskingType)
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.settings_allow_multitasking,
            startingTypeNames = listOf(checkActionAllowMultitaskingType),
        )

        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.settings_disallow_multitasking)
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkActionAllowMultitaskingType)
        clickOnViewWithText(checkActionDisallowMultitaskingType)
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.settings_disallow_multitasking,
            startingTypeNames = listOf(checkActionDisallowMultitaskingType),
        )

        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnViewWithText(R.string.settings_disallow_multitasking)
        clickOnViewWithText(R.string.change_complex_action_assign_tag)
        clickOnViewWithText(tagName1)
        clickOnViewWithText(R.string.change_activity_filter_save)
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkActionDisallowMultitaskingType)
        clickOnViewWithText(checkActionAssignTagType)
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf(tagName1),
            startingTypeNames = listOf(checkActionAssignTagType),
        )

        // Check starting types
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnView(withSubstring(getString(R.string.change_complex_action_assign_tag)))
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkActionAssignTagType)
        clickOnViewWithText(checkStartingType1)
        clickOnViewWithText(checkStartingType2)
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.settings_allow_multitasking,
            startingTypeNames = listOf(checkStartingType1, checkStartingType2),
        )

        // Check current types
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkStartingType1)
        clickOnViewWithText(checkStartingType2)
        pressBack()
        clickOnViewWithText(R.string.change_complex_previous_activity)
        clickOnView(allOf(withText(checkCurrentType1), isCompletelyDisplayed()))
        clickOnView(allOf(withText(checkCurrentType2), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.settings_allow_multitasking,
            currentTypeNames = listOf(checkCurrentType1, checkCurrentType2),
        )

        // Check days
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnViewWithText(R.string.change_complex_previous_activity)
        clickOnViewWithText(checkCurrentType1)
        clickOnViewWithText(checkCurrentType2)
        pressBack()
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnView(allOf(withText(checkDaysType), isCompletelyDisplayed()))
        pressBack()
        clickOnViewWithText(R.string.range_day)
        clickOnViewWithText(timeMapper.toShortDayOfWeekName(DayOfWeek.MONDAY))
        clickOnViewWithText(timeMapper.toShortDayOfWeekName(DayOfWeek.WEDNESDAY))
        clickOnViewWithText(timeMapper.toShortDayOfWeekName(DayOfWeek.FRIDAY))
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.settings_allow_multitasking,
            startingTypeNames = listOf(checkDaysType),
            daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )

        // Check all
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.change_complex_action_assign_tag)
        clickOnViewWithText(tagName1)
        clickOnViewWithText(tagName2)
        clickOnViewWithText(tagName3)
        clickOnViewWithText(R.string.change_activity_filter_save)
        clickOnViewWithText(R.string.change_complex_starting_activity)
        clickOnViewWithText(checkDaysType)
        clickOnViewWithText(checkAllType1)
        pressBack()
        clickOnViewWithText(R.string.change_complex_previous_activity)
        clickOnView(allOf(withText(checkAllType2), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.change_activity_filter_save)
        checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf(tagName1, tagName2, tagName3),
            startingTypeNames = listOf(checkAllType1),
            currentTypeNames = listOf(checkAllType2),
            daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        )
    }

    @Test
    fun actionAllowMultitasking() {
        val check1 = "check1"
        val check2 = "check2"
        val byStarting1 = "byStarting1"
        val byStarting2 = "byStarting2"
        val byCurrent1 = "byCurrent1"
        val byCurrent2 = "byCurrent2"
        val byDay1 = "byDay1"
        val byDay2 = "byDay2"
        val byStartingAndCurrent1 = "byStartingAndCurrent1"
        val byStartingAndCurrent2 = "byStartingAndCurrent2"
        val byStartingAndCurrent3 = "byStartingAndCurrent3"

        // Add data
        runBlocking { prefsInteractor.setAllowMultitasking(false) }
        testUtils.addActivity(check1)
        testUtils.addActivity(check2)
        testUtils.addActivity(byStarting1)
        testUtils.addActivity(byStarting2)
        testUtils.addActivity(byCurrent1)
        testUtils.addActivity(byCurrent2)
        testUtils.addActivity(byDay1)
        testUtils.addActivity(byDay2)
        testUtils.addActivity(byStartingAndCurrent1)
        testUtils.addActivity(byStartingAndCurrent2)
        testUtils.addActivity(byStartingAndCurrent3)
        Thread.sleep(1000)

        // Check disabled
        tryAction { checkNoRunningRecord(check1) }
        checkNoRunningRecord(check2)
        tryAction { clickOnViewWithText(check1) }
        checkRunningRecord(check1)
        clickOnViewWithText(check2)
        checkNoRunningRecord(check1)
        checkRunningRecord(check2)
        clickOnViewWithText(byStarting1)
        checkNoRunningRecord(check2)

        // Check starting by type
        testUtils.addComplexRule(
            action = ComplexRule.Action.AllowMultitasking,
            startingTypeNames = listOf(byStarting2),
        )
        clickOnViewWithText(byStarting2)
        checkRunningRecord(byStarting1)
        checkRunningRecord(byStarting2)
        clickOnViewWithText(byCurrent1)
        checkNoRunningRecord(byStarting1)
        checkNoRunningRecord(byStarting1)

        // Check by current type
        testUtils.addComplexRule(
            action = ComplexRule.Action.AllowMultitasking,
            currentTypeNames = listOf(byCurrent1),
        )
        clickOnViewWithText(byCurrent2)
        checkRunningRecord(byCurrent1)
        checkRunningRecord(byCurrent2)
        stopRunningRecord(byCurrent1)
        clickOnViewWithText(byDay1)
        checkNoRunningRecord(byCurrent1)
        checkNoRunningRecord(byCurrent2)

        // Check by day
        val currentDay = timeMapper.getDayOfWeek(
            timestamp = System.currentTimeMillis(),
            calendar = Calendar.getInstance(),
            startOfDayShift = 0,
        )
        testUtils.addComplexRule(
            action = ComplexRule.Action.AllowMultitasking,
            daysOfWeek = listOf(currentDay),
        )
        clickOnViewWithText(byDay2)
        checkRunningRecord(byDay1)
        checkRunningRecord(byDay2)
        runBlocking { complexRuleRepo.clear() }
        clickOnViewWithText(byStartingAndCurrent1)
        checkNoRunningRecord(byDay1)
        checkNoRunningRecord(byDay2)

        // Check by starting and current
        testUtils.addComplexRule(
            action = ComplexRule.Action.AllowMultitasking,
            startingTypeNames = listOf(byStartingAndCurrent3),
            currentTypeNames = listOf(byStartingAndCurrent1),
        )
        clickOnViewWithText(byStartingAndCurrent2)
        checkNoRunningRecord(byStartingAndCurrent1)
        checkRunningRecord(byStartingAndCurrent2)
        clickOnViewWithText(byStartingAndCurrent1)
        checkNoRunningRecord(byStartingAndCurrent2)
        checkRunningRecord(byStartingAndCurrent1)
        clickOnViewWithText(byStartingAndCurrent3)
        checkRunningRecord(byStartingAndCurrent1)
        checkRunningRecord(byStartingAndCurrent3)
    }

    @Test
    fun actionDisallowMultitasking() {
        val check1 = "check1"
        val check2 = "check2"
        val byStarting1 = "byStarting1"
        val byStarting2 = "byStarting2"
        val byCurrent1 = "byCurrent1"
        val byCurrent2 = "byCurrent2"
        val byDay1 = "byDay1"
        val byDay2 = "byDay2"
        val byStartingAndCurrent1 = "byStartingAndCurrent1"
        val byStartingAndCurrent2 = "byStartingAndCurrent2"
        val byStartingAndCurrent3 = "byStartingAndCurrent3"

        // Add data
        runBlocking { prefsInteractor.setAllowMultitasking(true) }
        testUtils.addActivity(check1)
        testUtils.addActivity(check2)
        testUtils.addActivity(byStarting1)
        testUtils.addActivity(byStarting2)
        testUtils.addActivity(byCurrent1)
        testUtils.addActivity(byCurrent2)
        testUtils.addActivity(byDay1)
        testUtils.addActivity(byDay2)
        testUtils.addActivity(byStartingAndCurrent1)
        testUtils.addActivity(byStartingAndCurrent2)
        testUtils.addActivity(byStartingAndCurrent3)
        Thread.sleep(1000)

        // Check allowed
        tryAction { checkNoRunningRecord(check1) }
        checkNoRunningRecord(check2)
        tryAction { clickOnViewWithText(check1) }
        clickOnViewWithText(check2)
        checkRunningRecord(check1)
        checkRunningRecord(check2)
        stopRunningRecord(check1)
        stopRunningRecord(check2)

        // Check starting by type
        testUtils.addComplexRule(
            action = ComplexRule.Action.DisallowMultitasking,
            startingTypeNames = listOf(byStarting2),
        )
        clickOnViewWithText(byStarting1)
        clickOnViewWithText(byStarting2)
        checkNoRunningRecord(byStarting1)
        checkRunningRecord(byStarting2)
        clickOnViewWithText(byStarting1)
        checkRunningRecord(byStarting1)
        checkRunningRecord(byStarting2)
        stopRunningRecord(byStarting1)
        stopRunningRecord(byStarting2)

        // Check by current type
        testUtils.addComplexRule(
            action = ComplexRule.Action.DisallowMultitasking,
            currentTypeNames = listOf(byCurrent1),
        )
        clickOnViewWithText(byCurrent1)
        clickOnViewWithText(byCurrent2)
        checkNoRunningRecord(byCurrent1)
        checkRunningRecord(byCurrent2)
        clickOnViewWithText(byCurrent1)
        checkRunningRecord(byCurrent1)
        checkRunningRecord(byCurrent2)
        stopRunningRecord(byCurrent1)
        stopRunningRecord(byCurrent2)

        // Check by day
        val currentDay = timeMapper.getDayOfWeek(
            timestamp = System.currentTimeMillis(),
            calendar = Calendar.getInstance(),
            startOfDayShift = 0,
        )
        testUtils.addComplexRule(
            action = ComplexRule.Action.DisallowMultitasking,
            daysOfWeek = listOf(currentDay),
        )
        clickOnViewWithText(byDay1)
        clickOnViewWithText(byDay2)
        checkNoRunningRecord(byDay1)
        checkRunningRecord(byDay2)
        runBlocking { complexRuleRepo.clear() }
        clickOnViewWithText(byDay1)
        checkRunningRecord(byDay1)
        checkRunningRecord(byDay2)
        stopRunningRecord(byDay1)
        stopRunningRecord(byDay2)

        // Check by starting and current
        testUtils.addComplexRule(
            action = ComplexRule.Action.DisallowMultitasking,
            startingTypeNames = listOf(byStartingAndCurrent3),
            currentTypeNames = listOf(byStartingAndCurrent1),
        )
        clickOnViewWithText(byStartingAndCurrent1)
        checkRunningRecord(byStartingAndCurrent1)
        clickOnViewWithText(byStartingAndCurrent2)
        checkRunningRecord(byStartingAndCurrent1)
        checkRunningRecord(byStartingAndCurrent2)
        clickOnViewWithText(byStartingAndCurrent3)
        checkNoRunningRecord(byStartingAndCurrent1)
        checkNoRunningRecord(byStartingAndCurrent2)
        checkRunningRecord(byStartingAndCurrent3)
    }

    @Test
    fun assignTags() {
        val check1 = "check1"
        val check2 = "check2"
        val byStarting1 = "byStarting1"
        val byStarting2 = "byStarting2"
        val byCurrent1 = "byCurrent1"
        val byCurrent2 = "byCurrent2"
        val byDay1 = "byDay1"
        val byDay2 = "byDay2"
        val byStartingAndCurrent1 = "byStartingAndCurrent1"
        val byStartingAndCurrent2 = "byStartingAndCurrent2"
        val byStartingAndCurrent3 = "byStartingAndCurrent3"
        val tagName1 = "tagName1"
        val tagName2 = "tagName2"
        val tagName3 = "tagName3"
        val tagName4 = "tagName4"
        val tagName5 = "tagName5"

        // Add data
        runBlocking { prefsInteractor.setAllowMultitasking(false) }
        testUtils.addActivity(check1)
        testUtils.addActivity(check2)
        testUtils.addActivity(byStarting1)
        testUtils.addActivity(byStarting2)
        testUtils.addActivity(byCurrent1)
        testUtils.addActivity(byCurrent2)
        testUtils.addActivity(byDay1)
        testUtils.addActivity(byDay2)
        testUtils.addActivity(byStartingAndCurrent1)
        testUtils.addActivity(byStartingAndCurrent2)
        testUtils.addActivity(byStartingAndCurrent3)
        testUtils.addRecordTag(tagName1)
        testUtils.addRecordTag(tagName2)
        testUtils.addRecordTag(tagName3)
        testUtils.addRecordTag(tagName4)
        testUtils.addRecordTag(tagName5)
        Thread.sleep(1000)

        // Check starting by type
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName1),
            startingTypeNames = listOf(byStarting2),
        )
        clickOnViewWithText(byStarting1)
        checkNoRunningRecordWithTag(byStarting1, tagName1)
        clickOnViewWithText(byStarting2)
        checkRunningRecordWithTag(byStarting2, tagName1)

        // Check by current type
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName2),
            currentTypeNames = listOf(byCurrent1),
        )
        clickOnViewWithText(byCurrent1)
        checkNoRunningRecordWithTag(byCurrent1, tagName2)
        clickOnViewWithText(byCurrent2)
        checkRunningRecordWithTag(byCurrent2, tagName2)

        // Check by day
        val currentDay = timeMapper.getDayOfWeek(
            timestamp = System.currentTimeMillis(),
            calendar = Calendar.getInstance(),
            startOfDayShift = 0,
        )
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName3),
            daysOfWeek = listOf(currentDay),
        )
        clickOnViewWithText(byDay1)
        checkRunningRecordWithTag(byDay1, tagName3)
        clickOnViewWithText(byDay2)
        checkRunningRecordWithTag(byDay2, tagName3)
        runBlocking { complexRuleRepo.clear() }
        clickOnViewWithText(byDay1)
        checkNoRunningRecordWithTag(byDay1, tagName3)
        clickOnViewWithText(byDay2)
        checkNoRunningRecordWithTag(byDay2, tagName3)

        // Check by starting and current
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName4),
            startingTypeNames = listOf(byStartingAndCurrent3),
            currentTypeNames = listOf(byStartingAndCurrent1),
        )
        clickOnViewWithText(byStartingAndCurrent1)
        checkNoRunningRecordWithTag(byStartingAndCurrent1, tagName4)
        clickOnViewWithText(byStartingAndCurrent2)
        checkNoRunningRecordWithTag(byStartingAndCurrent2, tagName4)
        clickOnViewWithText(byStartingAndCurrent3)
        checkNoRunningRecordWithTag(byStartingAndCurrent3, tagName4)
        clickOnViewWithText(byStartingAndCurrent1)
        clickOnViewWithText(byStartingAndCurrent3)
        checkRunningRecordWithTag(byStartingAndCurrent3, tagName4)
    }

    @Test
    fun disable() {
        val typeName = "typeName"
        val tagName = "tagName"

        // Add data
        runBlocking { prefsInteractor.setAllowMultitasking(false) }
        testUtils.addActivity(typeName)
        testUtils.addRecordTag(tagName)
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName),
            startingTypeNames = listOf(typeName),
        )
        Thread.sleep(1000)

        // Check enabled
        clickOnViewWithText(typeName)
        checkRunningRecordWithTag(typeName, tagName)
        stopRunningRecord(typeName)

        // Check disabled
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        NavUtils.openComplexRules()
        clickOnViewWithText(R.string.complex_rules_disable)
        pressBack()

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(typeName)
        checkRunningRecord(typeName)
        checkNoRunningRecordWithTag(typeName, tagName)
        stopRunningRecord(typeName)

        // Check enabled again
        NavUtils.openSettingsScreen()
        NavUtils.openComplexRules()
        clickOnViewWithText(R.string.complex_rules_enable)
        pressBack()

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(typeName)
        checkRunningRecordWithTag(typeName, tagName)
    }

    @Test
    fun archiveAndRemoveData() {
        val typeName1 = "typeName1"
        val typeName2 = "typeName2"
        val tagName1 = "tagName1"
        val tagName2 = "tagName2"

        // Add data
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)
        testUtils.addRecordTag(tagName1)
        testUtils.addRecordTag(tagName2)
        testUtils.addComplexRule(
            action = ComplexRule.Action.AssignTag,
            assignTagNames = listOf(tagName1, tagName2),
            startingTypeNames = listOf(typeName1, typeName2),
        )
        Thread.sleep(1000)

        // Check
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        NavUtils.openComplexRules()
        checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf(tagName1, tagName2),
            startingTypeNames = listOf(typeName1, typeName2),
        )
        pressBack()

        // Archive
        NavUtils.openRunningRecordsScreen()
        longClickOnView(withText(typeName1))
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeDelete)
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnView(withText(tagName1))
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        pressBack()

        // Still visible
        NavUtils.openComplexRules()
        checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf(tagName1, tagName2),
            startingTypeNames = listOf(typeName1, typeName2),
        )
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnView(withSubstring(getString(R.string.change_complex_action_assign_tag)))
        clickOnView(
            allOf(
                withId(R.id.btnChangeRecordButtonItem),
                withSubstring(getString(R.string.change_complex_action_assign_tag)),
            ),
        )
        checkViewIsDisplayed(withText(tagName1))
        checkViewIsDisplayed(withText(tagName2))
        pressBack()
        pressBack()
        clickOnViewWithText(R.string.change_complex_starting_activity)
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        pressBack()
        pressBack()
        pressBack()

        // Delete
        NavUtils.openArchiveScreen()
        clickOnViewWithText(typeName1)
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        clickOnViewWithText(tagName1)
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        pressBack()

        // Check again
        NavUtils.openComplexRules()
        checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf(tagName2),
            startingTypeNames = listOf(typeName2),
        )
        clickOnView(withId(complexRulesR.id.containerComplexRuleItem))
        clickOnView(withSubstring(getString(R.string.change_complex_action_assign_tag)))
        clickOnView(
            allOf(
                withId(R.id.btnChangeRecordButtonItem),
                withSubstring(getString(R.string.change_complex_action_assign_tag)),
            ),
        )
        checkViewDoesNotExist(withText(tagName1))
        checkViewIsDisplayed(withText(tagName2))
        pressBack()
        pressBack()
        clickOnViewWithText(R.string.change_complex_starting_activity)
        checkViewDoesNotExist(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
    }

    private fun checkRunningRecord(name: String) {
        checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name))))
    }

    private fun checkNoRunningRecord(name: String) {
        checkViewDoesNotExist(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name))))
    }

    private fun checkRunningRecordWithTag(name: String, tagName: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText("$name - $tagName")),
            ),
        )
    }

    private fun checkNoRunningRecordWithTag(name: String, tagName: String) {
        checkRunningRecord(name)
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText("$name - $tagName")),
            ),
        )
    }

    private fun stopRunningRecord(name: String) {
        clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withSubstring(name))))
    }

    private fun checkListView(
        @StringRes actionStringResId: Int,
        assignTagNames: List<String> = emptyList(),
        startingTypeNames: List<String> = emptyList(),
        currentTypeNames: List<String> = emptyList(),
        daysOfWeek: List<DayOfWeek> = emptyList(),
    ) {
        fun getElementMatcher(
            name: String,
            forConditions: Boolean,
        ): Matcher<View> {
            val containerId = if (forConditions) {
                complexRulesR.id.rvComplexRuleItemConditions
            } else {
                complexRulesR.id.rvComplexRuleItemActions
            }
            return hasDescendant(
                allOf(
                    withId(containerId),
                    hasDescendant(withText(name)),
                ),
            )
        }

        val matchers = mutableListOf<Matcher<View>>()
        matchers += withId(complexRulesR.id.containerComplexRuleItem)
        matchers += hasDescendant(
            allOf(
                withId(complexRulesR.id.rvComplexRuleItemActions),
                hasDescendant(withText(actionStringResId)),
            ),
        )
        matchers += assignTagNames.map {
            getElementMatcher(
                name = it,
                forConditions = false,
            )
        }
        matchers += startingTypeNames.map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }
        matchers += currentTypeNames.map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }
        matchers += daysOfWeek.map(timeMapper::toShortDayOfWeekName).map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }

        checkViewIsDisplayed(allOf(matchers))
    }
}
