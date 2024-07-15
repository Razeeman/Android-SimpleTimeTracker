package com.example.util.simpletimetracker.utils

import android.widget.DatePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.clickOnSettingsRecyclerText
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.scrollSettingsRecyclerToText
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_main.R as mainR
import com.example.util.simpletimetracker.feature_records.R as recordsR

object NavUtils {

    fun openRunningRecordsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(0))
        Thread.sleep(1000)
    }

    fun openRecordsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(1))
        Thread.sleep(1000)
    }

    fun openGoalsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(2))
        Thread.sleep(1000)
    }

    fun openStatisticsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(2))
        Thread.sleep(1000)
    }

    fun openSettingsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(3))
        Thread.sleep(1000)
    }

    fun openSettingsNotifications() {
        scrollSettingsRecyclerToText(coreR.string.settings_notification_title)
        clickOnSettingsRecyclerText(coreR.string.settings_notification_title)
    }

    fun openSettingsDisplay() {
        scrollSettingsRecyclerToText(coreR.string.settings_display_title)
        clickOnSettingsRecyclerText(coreR.string.settings_display_title)
    }

    fun openSettingsAdditional() {
        scrollSettingsRecyclerToText(coreR.string.settings_additional_title)
        clickOnSettingsRecyclerText(coreR.string.settings_additional_title)
    }

    fun openSettingsBackup() {
        scrollSettingsRecyclerToText(coreR.string.settings_backup_title)
        clickOnSettingsRecyclerText(coreR.string.settings_backup_title)
    }

    fun openSettingsExportImport() {
        scrollSettingsRecyclerToText(coreR.string.settings_export_title)
        clickOnSettingsRecyclerText(coreR.string.settings_export_title)
    }

    fun openCategoriesScreen() {
        scrollSettingsRecyclerToText(coreR.string.settings_edit_categories)
        clickOnSettingsRecyclerText(coreR.string.settings_edit_categories)
    }

    fun openArchiveScreen() {
        scrollSettingsRecyclerToText(coreR.string.settings_archive)
        clickOnSettingsRecyclerText(coreR.string.settings_archive)
    }

    fun openDataEditScreen() {
        scrollSettingsRecyclerToText(coreR.string.settings_data_edit)
        clickOnSettingsRecyclerText(coreR.string.settings_data_edit)
    }

    fun openCardSizeScreen() {
        scrollSettingsRecyclerToText(coreR.string.settings_change_card_size)
        clickOnSettingsRecyclerText(coreR.string.settings_change_card_size)
    }

    fun openPomodoro() {
        Thread.sleep(1000)
        clickOnViewWithText(R.string.running_records_pomodoro)
    }

    fun openPomodoroSettings() {
        clickOnViewWithId(R.id.btnPomodoroSettings)
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        text: String? = null,
        categories: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Name
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_record_type_color_hint)
            scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(color))
            clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(color))
            clickOnViewWithText(coreR.string.change_record_type_color_hint)
        }

        // Icon
        if (icon != null) {
            clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
            scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withTag(icon)))
            clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withTag(icon))
            clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        } else if (text != null) {
            clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
            onView(withId(changeRecordTypeR.id.rvIconSelection)).perform(collapseToolbar())
            scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withText(text)))
            clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withText(text))
            clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        }

        // Categories
        if (categories.isNotEmpty()) {
            clickOnViewWithText(coreR.string.category_hint)
            categories.forEach { categoryName ->
                scrollRecyclerToView(
                    changeRecordTypeR.id.rvChangeRecordTypeCategories, hasDescendant(withText(categoryName)),
                )
                clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName))
            }
            clickOnViewWithText(coreR.string.category_hint)
        }

        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_save)
    }

    fun addGoalToActivity(
        goal: RecordTypeGoal,
    ) {
        val layout = when (goal.range) {
            is RecordTypeGoal.Range.Session -> changeRecordTypeR.id.layoutChangeRecordTypeGoalSession
            is RecordTypeGoal.Range.Daily -> changeRecordTypeR.id.layoutChangeRecordTypeGoalDaily
            is RecordTypeGoal.Range.Weekly -> changeRecordTypeR.id.layoutChangeRecordTypeGoalWeekly
            is RecordTypeGoal.Range.Monthly -> changeRecordTypeR.id.layoutChangeRecordTypeGoalMonthly
        }
        onView(withId(layout)).perform(nestedScrollTo())

        // Select type
        layout.takeUnless { goal.range is RecordTypeGoal.Range.Session }?.let {
            clickOnView(
                allOf(
                    isDescendantOfA(withId(it)),
                    withId(changeRecordTypeR.id.fieldRecordTypeGoalType),
                ),
            )
            val typeToSelect = when (goal.type) {
                is RecordTypeGoal.Type.Duration -> coreR.string.change_record_type_goal_duration
                is RecordTypeGoal.Type.Count -> coreR.string.change_record_type_goal_count
            }
            clickOnViewWithText(typeToSelect)
        }

        // Enter value
        when (goal.type) {
            is RecordTypeGoal.Type.Duration -> {
                clickOnView(
                    allOf(
                        isDescendantOfA(withId(layout)),
                        withId(changeRecordTypeR.id.fieldChangeRecordTypeGoalDuration),
                    ),
                )
                if (goal.type.value == 0L) disableDuration() else enterDuration(goal.type.value)
            }
            is RecordTypeGoal.Type.Count -> {
                onView(
                    allOf(
                        isDescendantOfA(withId(layout)),
                        withId(changeRecordTypeR.id.etChangeRecordTypeGoalCountValue),
                    ),
                ).perform(replaceText(goal.type.value.toString()))
            }
        }

        closeSoftKeyboard()
    }

    fun disableGoalOnActivity(
        goal: RecordTypeGoal,
    ) {
        val newGoal = goal.copy(
            type = when (val type = goal.type) {
                is RecordTypeGoal.Type.Duration -> RecordTypeGoal.Type.Duration(0)
                is RecordTypeGoal.Type.Count -> RecordTypeGoal.Type.Count(0)
            },
        )
        addGoalToActivity(newGoal)
    }

    fun addCategory(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.categories_add_category) }

        // Name
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_category_color_hint)
            scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryColor, withCardColor(color))
            clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withCardColor(color))
            clickOnViewWithText(coreR.string.change_category_color_hint)
        }

        // Activities
        clickOnViewWithText(coreR.string.change_category_types_hint)
        activities.forEach { typeName ->
            scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryType, hasDescendant(withText(typeName)))
            clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName))
        }

        clickOnViewWithText(coreR.string.change_category_save)
    }

    fun addRecordTag(
        name: String,
        activity: String? = null,
        color: Int? = null,
    ) {
        tryAction { clickOnViewWithText(coreR.string.categories_add_record_tag) }

        // Name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)

        // Color
        if (color != null) {
            clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagColor)
            scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(color))
            clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(color))
        }

        // Activity
        if (!activity.isNullOrEmpty()) {
            clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
            scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagType, hasDescendant(withText(activity)))
            clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(activity))
            pressBack()
            clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
            clickOnRecyclerItem(dialogsR.id.rvTypesSelectionContainer, withText(activity))
        }

        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)
    }

    fun addRecord(name: String) {
        tryAction { clickOnViewWithId(recordsR.id.btnRecordAdd) }

        // Activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(coreR.string.change_record_save)
    }

    fun addRecordWithTime(
        name: String,
        hourStarted: Int,
        minutesStarted: Int,
        hourEnded: Int,
        minutesEnded: Int,
        comment: String? = null,
        tag: String? = null,
    ) {
        tryAction { clickOnViewWithId(recordsR.id.btnRecordAdd) }

        // Time started
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Time ended
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Comment
        if (!comment.isNullOrEmpty()) {
            clickOnViewWithText(coreR.string.change_record_comment_field)
            typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
            clickOnViewWithText(coreR.string.change_record_comment_field)
        }

        // Activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Tag
        if (tag != null) {
            tryAction { clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag)) }
            clickOnViewWithText(coreR.string.change_record_tag_field)
        }

        clickOnViewWithText(coreR.string.change_record_save)
    }

    fun addActivityFilter(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList(),
        categories: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_filter) }

        // Name
        typeTextIntoView(changeActivityFilterR.id.etChangeActivityFilterName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_category_color_hint)
            scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(color))
            clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(color))
            clickOnViewWithText(coreR.string.change_category_color_hint)
        }

        // Activity
        if (activities.isNotEmpty()) {
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                    withText(coreR.string.activity_hint),
                ),
            )
            activities.forEach {
                scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterType, hasDescendant(withText(it)))
                clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(it))
            }
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
        }

        // Category
        if (categories.isNotEmpty()) {
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                    withText(coreR.string.category_hint),
                ),
            )
            categories.forEach {
                scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterType, hasDescendant(withText(it)))
                clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(it))
            }
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
        }

        clickOnViewWithText(coreR.string.change_activity_filter_save)
    }

    fun setCustomRange(
        yearStarted: Int,
        monthStarted: Int,
        dayStarted: Int,
        yearEnded: Int,
        monthEnded: Int,
        dayEnded: Int,
    ) {
        // Set time started
        clickOnViewWithId(dialogsR.id.tvCustomRangeSelectionTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearStarted, monthStarted + 1, dayStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Set time ended
        clickOnViewWithId(dialogsR.id.tvCustomRangeSelectionTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearEnded, monthEnded + 1, dayEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        clickOnViewWithId(dialogsR.id.btnCustomRangeSelection)
    }

    private fun disableDuration() {
        clickOnViewWithText(coreR.string.duration_dialog_disable)
    }

    private fun enterDuration(
        value: Long,
    ) {
        fun Long.padWithZeroes() = this.toString().padDuration()

        val hr = value / 3600
        val min = (value - hr * 3600) / 60
        val sec = (value - hr * 3600 - min * 60)
        val reformattedValue = "${hr.padWithZeroes()}${min.padWithZeroes()}${sec.padWithZeroes()}"

        reformattedValue.forEach {
            when (it) {
                '0' -> dialogsR.id.tvNumberKeyboard0
                '1' -> dialogsR.id.tvNumberKeyboard1
                '2' -> dialogsR.id.tvNumberKeyboard2
                '3' -> dialogsR.id.tvNumberKeyboard3
                '4' -> dialogsR.id.tvNumberKeyboard4
                '5' -> dialogsR.id.tvNumberKeyboard5
                '6' -> dialogsR.id.tvNumberKeyboard6
                '7' -> dialogsR.id.tvNumberKeyboard7
                '8' -> dialogsR.id.tvNumberKeyboard8
                '9' -> dialogsR.id.tvNumberKeyboard9
                else -> return
            }.let(::clickOnViewWithId)
        }
        clickOnViewWithText(coreR.string.duration_dialog_save)
    }
}