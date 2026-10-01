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
import androidx.test.espresso.matcher.ViewMatchers.assertThat
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
import com.example.util.simpletimetracker.di.TestActionResolverImpl
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.activitySuggestion.model.ActivitySuggestion
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnCurrentDate
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerInPagerToView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.tryActionWithFallback
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withCardColorInt
import com.example.util.simpletimetracker.utils.withNullTag
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_change_goals.R as changeGoalsR
import com.example.util.simpletimetracker.feature_change_goals.api.R as changeGoalsApiR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_settings.R as settingsR
import com.example.util.simpletimetracker.test.R as testR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsBackupTest : BaseUiTest() {

    // TODO shortcuts, tag values.
    @Test
    fun restore23() {
        restore(DatabaseVersion.VER_23)
    }

    @Test
    fun restore28() {
        restore(DatabaseVersion.VER_28)
    }

    @Test
    fun fullRestore23() {
        fullRestore(DatabaseVersion.VER_23)
    }

    @Test
    fun fullRestore28() {
        fullRestore(DatabaseVersion.VER_28)
    }

    @Test
    fun partialRestore23() {
        partialRestore(DatabaseVersion.VER_23)
    }

    @Test
    fun partialRestore28() {
        partialRestore(DatabaseVersion.VER_28)
    }

    @Test
    fun partialRestoreWithExistingData23() {
        partialRestoreWithExistingData(DatabaseVersion.VER_23)
    }

    @Test
    fun partialRestoreWithExistingData28() {
        partialRestoreWithExistingData(DatabaseVersion.VER_28)
    }

    private fun restore(databaseVersion: DatabaseVersion) {
        setDatabaseFile(databaseVersion)

        // Restore
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        scrollSettingsRecyclerToText(R.string.settings_restore_backup)
        clickOnSettingsRecyclerText(R.string.settings_restore_backup)
        clickOnViewWithText(R.string.ok)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_backup_restored)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // TODO check all data
        // Check data
        runBlocking {
            test(activityDataList, recordTypeRepo.getAll())
            test(recordDataList, testUtils.recordInteractor.getAll())
            if (databaseVersion == DatabaseVersion.VER_28) {
                test(suggestionsDataList, testUtils.activitySuggestionInteractor.getAll())
            }
        }

        // Check types
        NavUtils.openRunningRecordsScreen()
        checkActivities(activityList.take(3))
        longClickOnView(getTypeMatcher("type3"))
        onView(withId(changeRecordTypeR.id.etChangeRecordTypeNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.etChangeRecordTypeNote), withText("type note")),
        )
        onView(withText(R.string.change_record_type_default_duration)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeAdditionalDefaultDurationSelectorValue),
                withText("1$minuteString"),
            ),
        )
        pressBack()
        NavUtils.openSettingsScreen()
        NavUtils.openArchiveScreen()
        checkActivities(activityList.drop(3))

        // Check records
        pressBack()
        NavUtils.openRecordsScreen()
        clickOnCurrentDate()
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
                    data.copy(color = ColorTestData.Res(R.color.colorInactive))
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
        onView(withText(R.string.category_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.category_hint)
        onView(withText("category1")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("category2")).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText("category3")).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        pressBack()
        pressBack()
        runBlocking { prefsInteractor.setShowActivityFilters(false) }

        // Check fav comments
        NavUtils.openRecordsScreen()
        NavUtils.openAddRecord()
        onView(withText(R.string.change_record_comment_field)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_comment_field)
        checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
        commentList.forEach {
            checkViewIsDisplayed(
                allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(it.comment)),
            )
        }
        closeSoftKeyboard()
        pressBack()
        pressBack()

        // Check fav icons
        tryActionWithFallback(onError = { pressBack() }) { NavUtils.openRunningRecordsScreen() }
        clickOnViewWithText(R.string.running_records_add_type)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        iconsList.forEach {
            when (it.type) {
                is FavouriteIconTestData.Type.Image -> {
                    clickOnView(
                        allOf(
                            isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                            withText(R.string.change_record_type_icon_image_hint),
                        ),
                    )
                    checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
                    onView(withTag(getIconResIdByName(it.icon)))
                        .check(isCompletelyAbove(withText(R.string.imageGroupMaps)))
                }
                is FavouriteIconTestData.Type.Emoji -> {
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

        // Check fav colors
        clickOnViewWithText(R.string.running_records_add_type)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_color_hint)
        colorsList.forEach {
            scrollRecyclerToView(
                changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColorInt(it.colorInt),
            )
            checkViewIsDisplayed(withCardColorInt(it.colorInt))
        }
        pressBack()
        pressBack()

        // Check goals
        longClickOnView(getTypeMatcher("type1"))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalPreview))
        pressBack()
        longClickOnView(getTypeMatcher("type3"))
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        checkGoal(RecordTypeGoal.Range.Session, RecordTypeGoal.Type.Duration(1), "1$minuteString")
        checkGoal(RecordTypeGoal.Range.Daily, RecordTypeGoal.Type.Duration(1), "1$hourString")
        checkGoal(RecordTypeGoal.Range.Weekly, RecordTypeGoal.Type.Duration(1), "4$hourString")
        checkGoal(RecordTypeGoal.Range.Monthly, RecordTypeGoal.Type.Duration(1), "40$hourString")
        pressBack()
        pressBack()
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText("category3")
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        val countGoalType = RecordTypeGoal.Type.Count(5)
        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Daily, countGoalType)
        onView(
            allOf(
                GoalsTestUtils.goalField(
                    RecordTypeGoal.Range.Daily,
                    countGoalType,
                    changeGoalsR.id.etChangeRecordTypeGoalCountValue,
                ),
                withText("5"),
            ),
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                isDescendantOfA(GoalsTestUtils.goalCard(RecordTypeGoal.Range.Daily, countGoalType)),
                withId(R.id.btnButtonsRowView),
                when (databaseVersion) {
                    DatabaseVersion.VER_23 -> withText(R.string.change_record_type_goal_time_hint)
                    DatabaseVersion.VER_28 -> withText(R.string.change_record_type_limit_time_hint)
                },
                withTag(ButtonsRowViewData.SELECTED_BUTTON_TEST_TAG),
            ),
        ).check(matches(isDisplayed()))
        pressBack()
        pressBack()
        pressBack()

        // Check rules
        NavUtils.openSettingsAdditional()
        NavUtils.openComplexRules()
        checkRules(ruleList)
        pressBack()

        if (databaseVersion == DatabaseVersion.VER_28) {
            // Check suggestions
            NavUtils.openSuggestions()
            checkSuggestions(suggestionsList)
            pressBack()
        }
    }

    private fun checkGoal(
        range: RecordTypeGoal.Range,
        type: RecordTypeGoal.Type,
        value: String,
    ) {
        val card = GoalsTestUtils.goalCard(range, type)
        scrollRecyclerToView(changeGoalsApiR.id.rvChangeRecordTypeGoals, card)
        checkViewIsDisplayed(
            allOf(
                card,
                hasDescendant(
                    allOf(
                        withId(changeGoalsR.id.tvChangeRecordTypeGoalSummary),
                        withSubstring(value),
                    ),
                ),
            ),
        )
    }

    private fun fullRestore(databaseVersion: DatabaseVersion) {
        setDatabaseFile(databaseVersion)

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
        checkViewIsDisplayed(getTypeMatcher("type1"))
        checkViewIsDisplayed(getTypeMatcher("type2"))
        checkViewIsDisplayed(getTypeMatcher("type3"))
    }

    @Suppress("ComplexRedundantLet")
    private fun partialRestore(databaseVersion: DatabaseVersion) {
        setDatabaseFile(databaseVersion)

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
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_colors_hint)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(3)"))
        if (databaseVersion == DatabaseVersion.VER_28) {
            checkViewIsDisplayed(withText("${getString(R.string.settings_activity_suggestions)}(3)"))
        }

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
        // Favourite colors
        clickOnViewWithText("${getString(R.string.change_record_favourite_colors_hint)}(2)")
        checkColors(colorsList)
        pressBack()
        // Complex rules
        clickOnViewWithText("${getString(R.string.settings_complex_rules)}(3)")
        checkRules(ruleList)
        pressBack()
        if (databaseVersion == DatabaseVersion.VER_28) {
            clickOnViewWithText("${getString(R.string.settings_activity_suggestions)}(3)")
            checkSuggestionsFilters(suggestionsList)
            pressBack()
        }

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
                is FavouriteIconTestData.Type.Image -> clickOnView(withTag(getIconResIdByName(it.icon)))
                is FavouriteIconTestData.Type.Emoji -> clickOnViewWithText(it.icon)
            }
        }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(1)"))

        clickOnViewWithText("${getString(R.string.change_record_favourite_colors_hint)}(2)")
        colorsList.take(1).forEach {
            clickOnView(withCardColorInt(it.colorInt))
        }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_colors_hint)}(1)"))

        clickOnViewWithText("${getString(R.string.settings_complex_rules)}(3)")
        clickOnViewWithText(R.string.settings_allow_multitasking)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(2)"))

        if (databaseVersion == DatabaseVersion.VER_28) {
            clickOnViewWithText("${getString(R.string.settings_activity_suggestions)}(2)")
            suggestionsList[1].let {
                clickOnView(
                    allOf(
                        withId(R.id.rvActivitySuggestionItemActions),
                        hasDescendant(withText(it.forTypeName)),
                    ),
                )
            }
            clickOnViewWithText(R.string.duration_dialog_save)
            checkViewIsDisplayed(withText("${getString(R.string.settings_activity_suggestions)}(1)"))
        }

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
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_colors_hint)}(1)"))
        checkViewIsDisplayed(withText(getString(R.string.settings_complex_rules)))
        if (databaseVersion == DatabaseVersion.VER_28) {
            checkViewIsDisplayed(withText(getString(R.string.settings_activity_suggestions)))
        }

        // Restore only activities
        removeFilter(R.string.change_activity_filters_hint)
        removeFilter(R.string.change_record_favourite_comments_hint_long)
        removeFilter(R.string.change_record_favourite_icons_hint)
        removeFilter(R.string.change_record_favourite_colors_hint)
        clickOnViewWithText(getString(R.string.activity_hint))
        activityList.take(3).forEach { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        clickOnViewWithText(R.string.backup_options_import)
        clickOnViewWithText(R.string.ok)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_import_complete)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // Check data
        NavUtils.openRunningRecordsScreen()
        activityList.take(3).forEach { checkViewIsDisplayed(getTypeMatcher(it.name)) }
        activityList.drop(3).forEach { checkViewDoesNotExist(getTypeMatcher(it.name)) }
    }

    @Suppress("ReplaceGetOrSet", "ComplexRedundantLet")
    private fun partialRestoreWithExistingData(databaseVersion: DatabaseVersion) {
        setDatabaseFile(databaseVersion)

        val colors = ColorMapper.getAvailableColors()

        // Add data
        activityList.take(2).forEach {
            testUtils.addActivity(
                name = it.name,
                color = (it.color as ColorTestData.Position).value.let(colors::get),
                text = it.icon,
            )
        }
        categoryList.first().let {
            testUtils.addCategory(
                tagName = it.name,
                color = (it.color as ColorTestData.Position).value.let(colors::get),
            )
        }
        tagList.first().let {
            testUtils.addRecordTag(
                tagName = it.name,
                typeName = "type1",
                color = (it.color as ColorTestData.Position).value.let(colors::get),
                icon = R.drawable.ic_360_24px,
            )
        }
        recordList.first().let {
            testUtils.addRecord(
                typeName = "type1",
                timeStarted = 1727096400000,
                timeEnded = 1727100000000,
            )
        }
        activityFilterList.first().let {
            testUtils.addActivityFilter(
                name = it.name,
                type = ActivityFilter.Type.Activity,
                color = (it.color as ColorTestData.Position).value.let(colors::get),
                names = listOf("type1", "type2"),
            )
        }
        commentList.first().let {
            testUtils.addFavouriteComment(it.comment)
        }
        iconsList.first().let {
            testUtils.addFavouriteIcon(it.icon)
        }
        colorsList.first().let {
            testUtils.addFavouriteColor(it.colorInt)
        }
        ruleList.first().let {
            testUtils.addComplexRule(
                action = ComplexRule.Action.AllowMultitasking,
                startingTypeNames = it.startingTypeNames,
                currentTypeNames = it.currentTypeNames,
                daysOfWeek = it.daysOfWeek,
            )
        }
        if (databaseVersion == DatabaseVersion.VER_28) {
            suggestionsList.first().let {
                testUtils.addSuggestion(
                    forTypeName = it.forTypeName,
                    names = it.typeNames,
                )
            }
        }

        // Restore
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsBackup()
        scrollSettingsRecyclerToText(R.string.settings_backup_options)
        clickOnSettingsRecyclerText(R.string.settings_backup_options)
        clickOnViewWithText(R.string.backup_options_partial_restore)

        // Check data counts
        checkViewIsDisplayed(withText("${getString(R.string.activity_hint)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.category_hint)}(2)"))
        checkViewIsDisplayed(withText("${getString(R.string.record_tag_hint_short)}(3)"))
        checkViewIsDisplayed(withText("${getString(R.string.shortcut_navigation_records)}(3)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_activity_filters_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_comments_hint_long)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_colors_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(2)"))
        if (databaseVersion == DatabaseVersion.VER_28) {
            checkViewIsDisplayed(withText("${getString(R.string.settings_activity_suggestions)}(2)"))
        }

        // Check filtering
        // Activities
        clickOnViewWithText("${getString(R.string.activity_hint)}(2)")
        checkActivities(activityList.drop(2))
        pressBack()
        // Categories
        clickOnViewWithText("${getString(R.string.category_hint)}(2)")
        checkCategories(categoryList.drop(1))
        pressBack()
        // Tags
        clickOnViewWithText("${getString(R.string.record_tag_hint_short)}(3)")
        checkTags(tagList.drop(1))
        pressBack()
        // Records
        clickOnViewWithText("${getString(R.string.shortcut_navigation_records)}(3)")
        checkRecords(recordList.drop(1), settingsR.id.rvSettingsPartialRestoreSelectionContainer)
        pressBack()
        // Activity filters
        clickOnViewWithText("${getString(R.string.change_activity_filters_hint)}(1)")
        checkActivityFilters(activityFilterList.drop(1))
        pressBack()
        // Favourite comments
        clickOnViewWithText("${getString(R.string.change_record_favourite_comments_hint_long)}(1)")
        checkComments(commentList.drop(1))
        pressBack()
        // Favourite icons
        clickOnViewWithText("${getString(R.string.change_record_favourite_icons_hint)}(1)")
        checkIcons(iconsList.drop(1))
        pressBack()
        // Favourite colors
        clickOnViewWithText("${getString(R.string.change_record_favourite_colors_hint)}(1)")
        checkColors(colorsList.drop(1))
        pressBack()
        // Complex rules
        clickOnViewWithText("${getString(R.string.settings_complex_rules)}(2)")
        checkRules(ruleList.drop(1))
        pressBack()
        // Suggestions
        if (databaseVersion == DatabaseVersion.VER_28) {
            clickOnViewWithText("${getString(R.string.settings_activity_suggestions)}(2)")
            checkSuggestionsFilters(suggestionsList.drop(1))
            pressBack()
        }

        // Check consistency
        removeFilter(R.string.activity_hint)
        removeFilter(R.string.category_hint)
        removeFilter(R.string.record_tag_hint_short)
        checkViewIsDisplayed(withText(getString(R.string.activity_hint)))
        checkViewIsDisplayed(withText(getString(R.string.category_hint)))
        checkViewIsDisplayed(withText(getString(R.string.record_tag_hint_short)))
        checkViewIsDisplayed(withText("${getString(R.string.shortcut_navigation_records)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_activity_filters_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_comments_hint_long)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_icons_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.change_record_favourite_colors_hint)}(1)"))
        checkViewIsDisplayed(withText("${getString(R.string.settings_complex_rules)}(1)"))
        if (databaseVersion == DatabaseVersion.VER_28) {
            checkViewIsDisplayed(withText(getString(R.string.settings_activity_suggestions)))
        }

        // Restore only activities
        removeFilter(R.string.shortcut_navigation_records)
        removeFilter(R.string.change_activity_filters_hint)
        removeFilter(R.string.change_record_favourite_comments_hint_long)
        removeFilter(R.string.change_record_favourite_icons_hint)
        removeFilter(R.string.change_record_favourite_colors_hint)
        removeFilter(R.string.settings_complex_rules)
        clickOnViewWithText(getString(R.string.activity_hint))
        activityList.get(2).let { clickOnViewWithText(it.name) }
        clickOnViewWithText(R.string.duration_dialog_save)
        clickOnViewWithText(R.string.backup_options_import)
        clickOnViewWithText(R.string.ok)

        // Check message
        tryAction { checkViewIsDisplayed(withText(R.string.message_import_complete)) }
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)

        // Check data
        NavUtils.openRunningRecordsScreen()
        activityList.take(3).forEach { checkViewIsDisplayed(getTypeMatcher(it.name)) }
        activityList.last().let { checkViewDoesNotExist(getTypeMatcher(it.name)) }
    }

    private fun setDatabaseFile(version: DatabaseVersion) {
        TestActionResolverImpl.testDatabaseNameResId = when (version) {
            DatabaseVersion.VER_23 -> testR.raw.db_version_23
            DatabaseVersion.VER_28 -> testR.raw.db_version_28
        }
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
        clickOnView(allOf(hasSibling(withSubstring(getString(textResId))), withId(R.id.ivFilterItemButton)))
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
                    withNullTag(),
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
                    hasDescendant(getColorMatcher(it.color)),
                ),
            )
        }
    }

    private fun checkComments(
        data: List<FavouriteCommentTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(withText(it.comment))
        }
    }

    private fun checkIcons(
        data: List<FavouriteIconTestData>,
    ) {
        data.forEach {
            val matcher = when (it.type) {
                is FavouriteIconTestData.Type.Image -> withTag(getIconResIdByName(it.icon))
                is FavouriteIconTestData.Type.Emoji -> withText(it.icon)
            }
            onView(matcher).check(matches(isDisplayed()))
        }
    }

    private fun checkColors(
        data: List<FavouriteColorTestData>,
    ) {
        data.forEach {
            checkViewIsDisplayed(withCardColorInt(it.colorInt))
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

    private fun checkSuggestions(
        data: List<SuggestionTestData>,
    ) = runBlocking {
        val typesMap = testUtils.recordTypeInteractor.getAll().associate { it.name to it.id }
        data.forEach { suggestion ->
            SuggestionsTestUtils.checkType(
                name = suggestion.forTypeName,
                visible = true,
            )
            suggestion.typeNames.forEach {
                SuggestionsTestUtils.checkSuggestion(
                    forType = suggestion.forTypeName,
                    textMatcher = withText(it),
                    tag = typesMap[suggestion.forTypeName],
                    visible = true,
                )
            }
        }
    }

    private fun checkSuggestionsFilters(
        data: List<SuggestionTestData>,
    ) {
        data.forEach { suggestion ->
            val matchers = mutableListOf<Matcher<View>>()
            matchers += withId(R.id.containerActivitySuggestionItem)
            matchers += hasDescendant(
                allOf(
                    withId(R.id.rvActivitySuggestionItemActions),
                    hasDescendant(withText(suggestion.forTypeName)),
                ),
            )
            suggestion.typeNames.forEach {
                matchers += hasDescendant(
                    allOf(
                        withId(R.id.rvActivitySuggestionItemConditions),
                        hasDescendant(withText(it)),
                    ),
                )
            }
            checkViewIsDisplayed(allOf(matchers))
        }
    }

    private fun getColorMatcher(data: ColorTestData): Matcher<View> {
        return when (data) {
            is ColorTestData.Position -> withCardColor(getColorByPosition(data.value))
            is ColorTestData.Custom -> withCardColorInt(data.value)
            is ColorTestData.Res -> withCardColor(data.value)
        }
    }

    private fun getTypeMatcher(name: String): Matcher<View> {
        return allOf(
            withId(R.id.viewRecordTypeItem),
            withNullTag(),
            hasDescendant(withText(name)),
        )
    }

    private fun <T> test(testDataList: List<T>, actualDataList: List<T>) {
        testDataList.forEach { testData ->
            assertThat(
                "expected $testData in $actualDataList",
                testData in actualDataList,
                `is`(true),
            )
        }
    }

    private val activityList = listOf(
        ActivityTestData("type1", "ic_360_24px", ColorTestData.Position(1)),
        ActivityTestData("type2", "ic_add_business_24px", ColorTestData.Position(2)),
        ActivityTestData("type3", "ic_add_location_24px", ColorTestData.Position(3)),
        ActivityTestData("type4", "ic_add_location_alt_24px", ColorTestData.Custom(0xff646464.toInt())),
    )
    private val activityDataList = listOf(
        RecordType(1, "type1", "ic_360_24px", AppColor(1, ""), 0, ""),
        RecordType(2, "type2", "ic_add_business_24px", AppColor(2, ""), 0, ""),
        RecordType(3, "type3", "ic_add_location_24px", AppColor(3, ""), 60, "type note"),
        RecordType(4, "type4", "ic_add_location_alt_24px", AppColor(0, 0xff646464.toInt().toString()), 0, "", true),
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
    private val recordDataList = listOf(
        Record(1, 1, 1727096400000, 1727100000000, "", emptyList()),
        Record(2, 2, 1727089200000, 1727092800000, "", listOf(RecordBase.Tag(2, null), RecordBase.Tag(3, null))),
        Record(4, 3, 1727082000000, 1727085600000, "record comment", listOf(RecordBase.Tag(3, null))),
        Record(5, 4, 1727071200000, 1727078400000, "", emptyList()),
    )
    private val activityFilterList = listOf(
        ActivityFilterTestData("filter1", ColorTestData.Position(0)),
        ActivityFilterTestData("filter2", ColorTestData.Custom(0xff345678.toInt())),
    )
    private val commentList = listOf(
        FavouriteCommentTestData("comment favourite 1"),
        FavouriteCommentTestData("comment favourite 2"),
    )
    private val iconsList = listOf(
        FavouriteIconTestData("ic_accessibility_24px", FavouriteIconTestData.Type.Image),
        FavouriteIconTestData("\uD83C\uDF49", FavouriteIconTestData.Type.Emoji),
    )
    private val colorsList = listOf(
        FavouriteColorTestData(0xff456789.toInt()),
        FavouriteColorTestData(0xff567890.toInt()),
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
    private val suggestionsList = listOf(
        SuggestionTestData(
            forTypeName = "type1",
            typeNames = listOf("type2"),
        ),
        SuggestionTestData(
            forTypeName = "type3",
            typeNames = listOf("type1", "type2"),
        ),
        SuggestionTestData(
            forTypeName = "type2",
            typeNames = listOf("type3"),
        ),
    )
    private val suggestionsDataList = listOf(
        ActivitySuggestion(1, 1, listOf(2L).toSet()),
        ActivitySuggestion(2, 3, listOf(1L, 2L).toSet()),
        ActivitySuggestion(3, 2, listOf(3L).toSet()),
    )

    private enum class DatabaseVersion {
        VER_23,
        VER_28,
    }

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

    private data class FavouriteCommentTestData(
        val comment: String,
    )

    private data class FavouriteIconTestData(
        val icon: String,
        val type: Type,
    ) {

        sealed interface Type {
            data object Image : Type
            data object Emoji : Type
        }
    }

    private data class FavouriteColorTestData(
        val colorInt: Int,
    )

    private data class RuleTestData(
        @StringRes val actionStringResId: Int,
        val assignTagNames: List<String> = emptyList(),
        val startingTypeNames: List<String> = emptyList(),
        val currentTypeNames: List<String> = emptyList(),
        val daysOfWeek: List<DayOfWeek> = emptyList(),
    )

    private data class SuggestionTestData(
        val forTypeName: String,
        val typeNames: List<String>,
    )

    private sealed interface ColorTestData {
        data class Position(val value: Int) : ColorTestData
        data class Custom(val value: Int) : ColorTestData
        data class Res(val value: Int) : ColorTestData
    }
}
