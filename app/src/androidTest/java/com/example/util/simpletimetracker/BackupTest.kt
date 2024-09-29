package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomDatePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BackupTest : BaseUiTest() {

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
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText("type1")),
                hasDescendant(withTag(getIconResIdByName("ic_360_24px"))),
                hasDescendant(withCardColor(getColorByPosition(1))),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText("type2")),
                hasDescendant(withTag(getIconResIdByName("ic_add_business_24px"))),
                hasDescendant(withCardColor(getColorByPosition(2))),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText("type3")),
                hasDescendant(withTag(getIconResIdByName("ic_add_location_24px"))),
                hasDescendant(withCardColor(getColorByPosition(3))),
            ),
        )
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
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText("type4")),
                hasDescendant(withTag(getIconResIdByName("ic_add_location_alt_24px"))),
                hasDescendant(withCardColorInt(0xff646464.toInt())),
            ),
        )

        // Check records
        pressBack()
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(CustomDatePicker::class.java.name)))
            .perform(setDate(2024, 9, 23))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        val calendarDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, 8)
            set(Calendar.DATE, 23)
        }
        scrollRecordsToType("type1")
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText("type1")),
                withCardColor(getColorByPosition(1)),
                hasDescendant(withTag(getIconResIdByName("ic_360_24px"))),
                hasDescendant(withText(calendarDate.getMillis(16, 0).formatTime())),
                hasDescendant(withText(calendarDate.getMillis(17, 0).formatTime())),
                hasDescendant(withText("1$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        scrollRecordsToType("type2 - tag2, tag3")
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText("type2 - tag2, tag3")),
                withCardColor(getColorByPosition(2)),
                hasDescendant(withTag(getIconResIdByName("ic_add_business_24px"))),
                hasDescendant(withText(calendarDate.getMillis(14, 0).formatTime())),
                hasDescendant(withText(calendarDate.getMillis(15, 0).formatTime())),
                hasDescendant(withText("1$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )
        scrollRecordsToType("type3 - tag3")
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText("type3 - tag3")),
                withCardColor(getColorByPosition(3)),
                hasDescendant(withTag(getIconResIdByName("ic_add_location_24px"))),
                hasDescendant(withText(calendarDate.getMillis(12, 0).formatTime())),
                hasDescendant(withText(calendarDate.getMillis(13, 0).formatTime())),
                hasDescendant(withText("1$hourString 0$minuteString")),
                hasDescendant(withText("record comment")),
                isCompletelyDisplayed(),
            ),
        )
        scrollRecordsToType("type4")
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText("type4")),
                withCardColorInt(0xff646464.toInt()),
                hasDescendant(withTag(getIconResIdByName("ic_add_location_alt_24px"))),
                hasDescendant(withText(calendarDate.getMillis(9, 0).formatTime())),
                hasDescendant(withText(calendarDate.getMillis(11, 0).formatTime())),
                hasDescendant(withText("2$hourString 0$minuteString")),
                isCompletelyDisplayed(),
            ),
        )

        // Check categories
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("category1")),
                withCardColor(getColorByPosition(18)),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("category2")),
                withCardColor(getColorByPosition(7)),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("category3")),
                withCardColorInt(0xff123456.toInt()),
            ),
        )

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
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("tag1")),
                withCardColor(getColorByPosition(1)),
                hasDescendant(withTag(getIconResIdByName("ic_360_24px"))),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("tag2")),
                withCardColor(getColorByPosition(2)),
                hasDescendant(withTag(getIconResIdByName("ic_add_business_24px"))),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("tag3")),
                withCardColor(getColorByPosition(16)),
                hasDescendant(withTag(getIconResIdByName("ic_attractions_24px"))),
            ),
        )
        pressBack()
        NavUtils.openArchiveScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText("tag4")),
                withCardColorInt(0xff234567.toInt()),
            ),
        )
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
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewActivityFilterItem),
                hasDescendant(withText("filter1")),
                withCardColor(R.color.colorFiltered),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewActivityFilterItem),
                hasDescendant(withText("filter2")),
                withCardColorInt(0xff345678.toInt()),
            ),
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
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText("comment favourite")),
        )
        closeSoftKeyboard()
        pressBack()
        pressBack()

        // Check fav icons
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
        onView(withTag(getIconResIdByName("ic_accessibility_24px")))
            .check(isCompletelyAbove(withText(R.string.imageGroupMaps)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(R.string.change_record_type_icon_emoji_hint),
            ),
        )
        checkViewIsDisplayed(withText(R.string.change_record_favourite_comments_hint))
        onView(withText("\uD83C\uDF49"))
            .check(isCompletelyAbove(withText(R.string.emojiGroupSmileys)))
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
        ComplexRulesTestUtils.checkListView(
            actionStringResId = R.string.settings_allow_multitasking,
            startingTypeNames = listOf("type1"),
            currentTypeNames = listOf("type2"),
            daysOfWeek = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY),
            timeMapper = timeMapper,
        )
        ComplexRulesTestUtils.checkListView(
            actionStringResId = R.string.settings_disallow_multitasking,
            currentTypeNames = listOf("type3"),
            timeMapper = timeMapper,
        )
        ComplexRulesTestUtils.checkListView(
            actionStringResId = R.string.change_complex_action_assign_tag,
            assignTagNames = listOf("tag1", "tag2"),
            startingTypeNames = listOf("type1"),
            currentTypeNames = listOf("type2"),
            timeMapper = timeMapper,
        )
    }

    private fun getIconResIdByName(name: String): Int {
        return iconImageMapper.getAvailableImages(loadSearchHints = false)
            .values.flatten().first { it.iconName == name }.iconResId
    }

    @Suppress("ReplaceGetOrSet")
    private fun getColorByPosition(position: Int): Int {
        return ColorMapper.getAvailableColors().get(position)
    }

    private fun scrollRecordsToType(typeName: String) {
        scrollRecyclerInPagerToView(
            recordsR.id.rvRecordsList,
            allOf(withId(R.id.viewRecordItem), hasDescendant(withText(typeName))),
        )
    }

    private fun Calendar.getMillis(hour: Int, minute: Int): Long {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        return timeInMillis
    }
}
