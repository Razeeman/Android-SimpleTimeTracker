package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerInPagerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withCardColorInt
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_settings.R as settingsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsBackupTest : BaseUiTest() {

    @Test
    fun restore() {
        // Restore
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        scrollSettingsRecyclerToText(R.string.settings_restore_backup)
        clickOnSettingsRecyclerText(R.string.settings_restore_backup)
        clickOnViewWithText(R.string.ok)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_backup_restored)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // Check types
        NavUtils.openRunningRecordsScreen()
        checkActivities(activityList.take(3))
        longClickOnView(allOf(withText("type3"), isCompletelyDisplayed()))
        onView(withId(changeRecordTypeR.id.etChangeRecordTypeNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.etChangeRecordTypeNote), withText("type note")),
        )
        onView(withText(R.string.change_record_type_additional_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_additional_hint)
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue),
                withText("1$minuteString"),
            ),
        )
        pressBack()
        pressBack()
        NavUtils.openSettingsScreen()
        NavUtils.openArchiveScreen()
        checkActivities(activityList.drop(3))

        // Check records
        pressBack()
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(setDate(2024, 9, 23))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)
        checkRecords(recordList, recordsR.id.rvRecordsList)

        // Check categories
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkCategories(categoryList)

        // Check categories relation
        clickOnViewWithText("category1")
        onView(withText(R.string.change_category_types_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText("type1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type2")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()

        clickOnViewWithText("category2")
        onView(withText(R.string.change_category_types_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText("type2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type1")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()

        clickOnViewWithText("category3")
        onView(withText(R.string.change_category_types_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText("type1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        onView(withId(changeCategoryR.id.etChangeRecordCategoryNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(changeCategoryR.id.etChangeRecordCategoryNote), withText("category note")),
        )
        pressBack()

        // Check tags
        checkTags(tagList.take(3))
        pressBack()
        NavUtils.openArchiveScreen()
        checkTags(tagList.drop(3))
        pressBack()

        // Check tag relations
        NavUtils.openCategoriesScreen()
        longClickOnView(withText("tag1"))
        onView(withText(R.string.change_record_type_field)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_field)
        onView(withText("type1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type2")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        onView(withText(R.string.change_record_tag_default_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_tag_default_hint)
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        pressBack()
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
        onView(withText("type1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()

        longClickOnView(withText("tag2"))
        onView(withText(R.string.change_record_type_field)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_field)
        onView(withText("type2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type1")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        onView(withText(R.string.change_record_tag_default_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_tag_default_hint)
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        pressBack()
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
        onView(withText("type2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()

        longClickOnView(withText("tag3"))
        onView(withText(R.string.change_record_type_field)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        pressBack()
        onView(withText(R.string.change_record_tag_default_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_tag_default_hint)
        onView(allOf(withText("type1"), isCompletelyDisplayed()))
            .check(isTopAlignedWith(allOf(withText("type2"), isCompletelyDisplayed())))
        onView(allOf(withText("type2"), isCompletelyDisplayed()))
            .check(isTopAlignedWith(allOf(withText("type3"), isCompletelyDisplayed())))
        pressBack()
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
        checkViewIsDisplayed(withText(R.string.nothing_selected))
        pressBack()
        onView(withId(changeRecordTagR.id.etChangeRecordTagNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.etChangeRecordTagNote), withText("tag note")),
        )
        pressBack()
        pressBack()

        // Check activity filters
        runBlocking { prefsInteractor.setShowActivityFilters(true) }
        NavUtils.openRunningRecordsScreen()
        checkActivityFilters(
            activityFilterList.mapIndexed { index, data ->
                if (index == 0) {
                    data.copy(color = ColorTestData.Res(R.color.colorFiltered))
                } else {
                    data
                }
            },
        )

        longClickOnView(withText("filter1"))
        checkViewIsDisplayed(
            allOf(
                withId(changeActivityFilterR.id.layoutChangeActivityFilterColorPreview),
                withCardColor(getColorByPosition(0)),
            ),
        )
        onView(withText(R.string.activity_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.activity_hint)
        onView(withText("type1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("type3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()

        longClickOnView(withText("filter2"))
        checkViewIsDisplayed(
            allOf(
                withId(changeActivityFilterR.id.layoutChangeActivityFilterColorPreview),
                withCardColorInt(0xff345678.toInt()),
            ),
        )
        onView(withText(R.string.activity_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.activity_hint)
        onView(withText("category1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("category2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("category3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()
        runBlocking { prefsInteractor.setShowActivityFilters(false) }

        // Check fav comments
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        onView(withText(R.string.change_record_comment_field)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_comment_field)
        checkViewIsDisplayed(withText(com.example.util.simpletimetracker.core.R.string.change_record_favourite_comments_hint))
        commentList.forEach {
            checkViewIsDisplayed(
                allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(it.comment)),
            )
        }
        closeSoftKeyboard()
        pressBack()
        pressBack()

        // Check fav icons
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        closeSoftKeyboard()
        iconsList.forEach {
            when (it.type) {
                is IconTestData.Type.Image -> {
                    clickOnViewWithText(R.string.change_record_type_icon_image_hint)
                    checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
                    onView(withTag(getIconResIdByName(it.icon)))
                        .check(isCompletelyAbove(withText(R.string.imageGroupMaps)))
                }
                is IconTestData.Type.Emoji -> {
                    clickOnView(
                        allOf(
                            isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                            withText(R.string.change_record_type_icon_emoji_hint),
                        ),
                    )
                    checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
                    onView(withText(it.icon))
                        .check(isCompletelyAbove(withText(R.string.emojiGroupSmileys)))
                }
            }
        }
        pressBack()
        pressBack()

        // Check goals
        longClickOnView(allOf(withText("type1"), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalPreview))
        pressBack()
        longClickOnView(allOf(withText("type3"), isCompletelyDisplayed()))
        clickOnViewWithText(com.example.util.simpletimetracker.core.R.string.change_record_type_goal_time_hint)
        onView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalSession)),
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue),
                withText("1$minuteString"),
            ),
        ).perform(nestedScrollTo()).check(matches(isDisplayed()))
        onView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalDaily)),
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue),
                withText("1$hourString"),
            ),
        ).perform(nestedScrollTo()).check(matches(isDisplayed()))
        onView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalWeekly)),
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue),
                withText("4$hourString"),
            ),
        ).perform(nestedScrollTo()).check(matches(isDisplayed()))
        onView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalMonthly)),
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue),
                withText("40$hourString"),
            ),
        ).perform(nestedScrollTo()).check(matches(isDisplayed()))
        pressBack()
        pressBack()

        // Check rules
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        NavUtils.openComplexRules()
        checkRules(ruleList)
    }

    @Test
    fun fullRestore() {
        runBlocking { prefsInteractor.setShowActivityFilters(false) }

        // Restore
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        scrollSettingsRecyclerToText(R.string.settings_backup_options)
        clickOnSettingsRecyclerText(R.string.settings_backup_options)
        clickOnViewWithText(R.string.backup_options_full_restore)
        clickOnViewWithText(R.string.ok)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_backup_restored)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // Settings is restored (filters are shown)
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText("filter2")

        // Check types
        checkViewIsDisplayed(allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText("type1"))))
        checkViewIsDisplayed(allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText("type2"))))
        checkViewIsDisplayed(allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText("type3"))))
    }

    @Test
    fun partialRestore() {
        // Restore
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        scrollSettingsRecyclerToText(R.string.settings_backup_options)
        clickOnSettingsRecyclerText(R.string.settings_backup_options)
        clickOnViewWithText(R.string.backup_options_partial_restore)

        // Check data counts
        checkViewIsDisplayed(withText("${getString(R.string.activity_hint)}(4)"))
        checkViewIsDisplayed(withText("${getString(R.string.category_hint)}(3)"))
        checkViewIsDisplayed(withText("${getString(R.string.record_tag_hint_short)}(4)"))
        checkViewIsDisplayed(withText("${getString(R.string.shortcut_navigation_records)}(4)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_activity_filters_hint)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_comments_hint_long)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(3)"))

        // Check filtering
        // Activities
        clickOnViewWithText("${getString(R.string.activity_hint)}(4)")
        checkActivities(activityList)
        pressBack()
        // Categories
        clickOnViewWithText("${getString(R.string.category_hint)}(3)")
        checkCategories(categoryList)
        pressBack()
        // Tags
        clickOnViewWithText("${getString(R.string.record_tag_hint_short)}(4)")
        checkTags(tagList)
        pressBack()
        // Records
        clickOnViewWithText("${getString(R.string.shortcut_navigation_records)}(4)")
        checkRecords(recordList, settingsR.id.rvSettingsPartialRestoreSelectionContainer)
        pressBack()
        // Activity filters
        clickOnViewWithText("${getString(R.string.change_activity_filters_hint)}(2)")
        checkActivityFilters(activityFilterList)
        pressBack()
        // Favourite comments
        clickOnViewWithText("${getString(R.string.change_record_favourite_comments_hint_long)}(2)")
        checkComments(commentList)
        pressBack()
        // Favourite icons
        clickOnViewWithText("${getString(R.string.change_record_favourite_icons_hint)}(2)")
        checkIcons(iconsList)
        pressBack()
        // Complex rules
        clickOnViewWithText("${getString(R.string.settings_complex_rules)}(3)")
        checkRules(ruleList)
        pressBack()

        // Check filtering
        clickOnViewWithText("${getString(R.string.activity_hint)}(4)")
        activityList.take(1).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.activity_hint)}(3)"))

        clickOnViewWithText("${getString(R.string.category_hint)}(3)")
        categoryList.take(1).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.category_hint)}(2)"))

        clickOnViewWithText("${getString(R.string.record_tag_hint_short)}(4)")
        tagList.take(1).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.record_tag_hint_short)}(3)"))

        clickOnViewWithText("${getString(R.string.shortcut_navigation_records)}(3)")
        recordList[1].let { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.shortcut_navigation_records)}(2)"))

        clickOnViewWithText("${getString(R.string.change_activity_filters_hint)}(2)")
        activityFilterList.take(1).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.change_activity_filters_hint)}(1)"))

        clickOnViewWithText("${getString(R.string.change_record_favourite_comments_hint_long)}(2)")
        commentList.take(1).forEach { clickOnViewWithText(it.comment) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_comments_hint_long)}(1)"))

        clickOnViewWithText("${getString(R.string.change_record_favourite_icons_hint)}(2)")
        iconsList.take(1).forEach {
            when (it.type) {
                is IconTestData.Type.Image -> clickOnView(withTag(getIconResIdByName(it.icon)))
                is IconTestData.Type.Emoji -> clickOnViewWithText(it.icon)
            }
        }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(1)"))

        clickOnViewWithText("${getString(R.string.settings_complex_rules)}(3)")
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(2)"))

        // Check consistency
        removeFilter(R.string.activity_hint)
        removeFilter(R.string.category_hint)
        removeFilter(R.string.record_tag_hint_short)
        checkViewIsDisplayed(withText(getString(R.string.activity_hint)))
        checkViewIsDisplayed(withText(getString(R.string.category_hint)))
        checkViewIsDisplayed(withText(getString(R.string.record_tag_hint_short)))
        checkViewIsDisplayed(withText(getString(R.string.shortcut_navigation_records)))
        checkViewIsDisplayed(withText("${getString(R.string.change_activity_filters_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_comments_hint_long)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(1)"))
        checkViewIsDisplayed(withText(getString(R.string.settings_complex_rules)))

        // Restore only activities
        removeFilter(R.string.change_activity_filters_hint)
        removeFilter(R.string.change_record_favourite_comments_hint_long)
        removeFilter(R.string.change_record_favourite_icons_hint)
        clickOnViewWithText(getString(R.string.activity_hint))
        activityList.take(3).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        clickOnViewWithText(R.string.backup_options_import)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_backup_restored)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // Check data
        NavUtils.openRunningRecordsScreen()
        activityList.take(3).forEach { checkViewIsDisplayed(withText(it.name)) }
        activityList.drop(3).forEach { checkViewDoesNotExist(withText(it.name)) }
    }

    private fun getIconResIdByName(name: String): Int {
        return iconImageMapper.getAvailableImages(loadSearchHints = false)
            .values.flatten().first { it.iconName == name }.iconResId
    }

    @Suppress("ReplaceGetOrSet")
    private fun getColorByPosition(position: Int): Int {
        return ColorMapper.getAvailableColors().get(position)
    }

    private fun removeFilter(@StringRes textResId: Int) {
        clickOnView(allOf(hasSibling(withSubstring(getString(textResId))), withId(R.id.ivFilterItemRemove)))
    }

    private fun Calendar.getMillis(hour: Int, minute: Int): Long {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        return timeInMillis
    }

    private fun checkActivities(
        data: List<ActivityTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(it.name)),
                    hasDescendant(withTag(getIconResIdByName(it.icon))),
                    hasDescendant(getColorMatcher(it.color)),
                ),
            )
        }
    }

    private fun checkCategories(
        data: List<CategoryTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewCategoryItem),
                    hasDescendant(withText(it.name)),
                    getColorMatcher(it.color),
                ),
            )
        }
    }

    private fun checkTags(
        data: List<TagTestData>,
    ) {
        data.forEach {
            val matchers = listOfNotNull(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText(it.name)),
                getColorMatcher(it.color),
                if (it.icon != null) hasDescendant(withTag(getIconResIdByName(it.icon))) else null,
            )
            checkViewIsDisplayed(allOf(matchers))
        }
    }

    private fun checkRecords(
        data: List<RecordTestData>,
        recyclerResId: Int,
    ) {
        val calendarDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, 8)
            set(Calendar.DATE, 23)
        }
        data.forEach {
            val matchers = listOfNotNull(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(it.name)),
                getColorMatcher(it.color),
                hasDescendant(withTag(getIconResIdByName(it.icon))),
                hasDescendant(withText(calendarDate.getMillis(it.startTime, 0).formatTime())),
                hasDescendant(withText(calendarDate.getMillis(it.endTime, 0).formatTime())),
                hasDescendant(withText("${it.duration}$hourString 0$minuteString")),
                if (it.comment != null) hasDescendant(withText(it.comment)) else null,
                isCompletelyDisplayed(),
            )
            scrollRecyclerInPagerToView(
                recyclerResId,
                allOf(withId(R.id.viewRecordItem), hasDescendant(withText(it.name))),
            )
            checkViewIsDisplayed(allOf(matchers))
        }
    }

    private fun checkActivityFilters(
        data: List<ActivityFilterTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewActivityFilterItem),
                    hasDescendant(withText(it.name)),
                    getColorMatcher(it.color),
                ),
            )
        }
    }

    private fun checkComments(
        data: List<CommentTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(withText(it.comment))
        }
    }

    private fun checkIcons(
        data: List<IconTestData>,
    ) {
        data.forEach {
            val matcher = when (it.type) {
                is IconTestData.Type.Image -> withTag(getIconResIdByName(it.icon))
                is IconTestData.Type.Emoji -> withText(it.icon)
            }
            onView(matcher).check(matches(isDisplayed()))
        }
    }

    private fun checkRules(
        data: List<RuleTestData>,
    ) {
        data.forEach {
            ComplexRulesTestUtils.checkListView(
                actionStringResId = it.actionStringResId,
                assignTagNames = it.assignTagNames,
                startingTypeNames = it.startingTypeNames,
                currentTypeNames = it.currentTypeNames,
                daysOfWeek = it.daysOfWeek,
                timeMapper = timeMapper,
            )
        }
    }

    private fun getColorMatcher(data: ColorTestData): Matcher<View> {
        return when (data) {
            is ColorTestData.Position -> withCardColor(getColorByPosition(data.value))
            is ColorTestData.Custom -> withCardColorInt(data.value)
            is ColorTestData.Res -> withCardColor(data.value)
        }
    }

    private val activityList = listOf(
        ActivityTestData("type1", "ic_360_24px", ColorTestData.Position(1)),
        ActivityTestData("type2", "ic_add_business_24px", ColorTestData.Position(2)),
        ActivityTestData("type3", "ic_add_location_24px", ColorTestData.Position(3)),
        ActivityTestData("type4", "ic_add_location_alt_24px", ColorTestData.Custom(0xff646464.toInt())),
    )
    private val categoryList = listOf(
        CategoryTestData("category1", ColorTestData.Position(18)),
        CategoryTestData("category2", ColorTestData.Position(7)),
        CategoryTestData("category3", ColorTestData.Custom(0xff123456.toInt())),
    )
    private val tagList = listOf(
        TagTestData("tag1", "ic_360_24px", ColorTestData.Position(1)),
        TagTestData("tag2", "ic_add_business_24px", ColorTestData.Position(2)),
        TagTestData("tag3", "ic_attractions_24px", ColorTestData.Position(16)),
        TagTestData("tag4", null, ColorTestData.Custom(0xff234567.toInt())),
    )
    private val recordList = listOf(
        RecordTestData("type1", ColorTestData.Position(1), "ic_360_24px", 16, 17, 1, null),
        RecordTestData("type2 - tag2, tag3", ColorTestData.Position(2), "ic_add_business_24px", 14, 15, 1, null),
        RecordTestData("type3 - tag3", ColorTestData.Position(3), "ic_add_location_24px", 12, 13, 1, "record comment"),
        RecordTestData("type4", ColorTestData.Custom(0xff646464.toInt()), "ic_add_location_alt_24px", 9, 11, 2, null),
    )
    private val activityFilterList = listOf(
        ActivityFilterTestData("filter1", ColorTestData.Position(0)),
        ActivityFilterTestData("filter2", ColorTestData.Custom(0xff345678.toInt())),
    )
    private val commentList = listOf(
        CommentTestData("comment favourite 1"),
        CommentTestData("comment favourite 2"),
    )
    private val iconsList = listOf(
        IconTestData("ic_accessibility_24px", IconTestData.Type.Image),
        IconTestData("\uD83C\uDF49", IconTestData.Type.Emoji),
    )
    private val ruleList = listOf(
        RuleTestData(
            actionStringResId = R.string.settings_allow_multitasking,
            startingTypeNames = listOf("type1"),
            currentTypeNames = listOf("type2"),
            daysOfWeek = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY),
        ),
        RuleTestData(
            actionStringResId = R.string.settings_disallow_multitasking,
            currentTypeNames = listOf("type3"),
        ),
        RuleTestData(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf("tag1", "tag2"),
            startingTypeNames = listOf("type1"),
            currentTypeNames = listOf("type2"),
        ),
    )

    private data class ActivityTestData(
        val name: String,
        val icon: String,
        val color: ColorTestData,
    )

    private data class CategoryTestData(
        val name: String,
        val color: ColorTestData,
    )

    private data class TagTestData(
        val name: String,
        val icon: String?,
        val color: ColorTestData,
    )

    private data class RecordTestData(
        val name: String,
        val color: ColorTestData,
        val icon: String,
        val startTime: Int,
        val endTime: Int,
        val duration: Int,
        val comment: String?,
    )

    private data class ActivityFilterTestData(
        val name: String,
        val color: ColorTestData,
    )

    private data class CommentTestData(
        val comment: String,
    )

    private data class IconTestData(
        val icon: String,
        val type: Type,
    ) {

        sealed interface Type {
            object Image : Type
            object Emoji : Type
        }
    }

    private data class RuleTestData(
        @StringRes val actionStringResId: Int,
        val assignTagNames: List<String> = emptyList(),
        val startingTypeNames: List<String> = emptyList(),
        val currentTypeNames: List<String> = emptyList(),
        val daysOfWeek: List<DayOfWeek> = emptyList(),
    )

    private sealed interface ColorTestData {
        data class Position(val value: Int) : ColorTestData
        data class Custom(val value: Int) : ColorTestData
        data class Res(val value: Int) : ColorTestData
    }
}
