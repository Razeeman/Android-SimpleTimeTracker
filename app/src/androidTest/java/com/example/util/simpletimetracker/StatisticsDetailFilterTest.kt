package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withPluralText
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_records_filter.R as recordsFilterR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@Suppress("SameParameterValue")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsDetailFilterTest : BaseUiTest() {

    @Test
    fun filterByType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add activity
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        testUtils.addRecord(name2)

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.activity_hint)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Activities,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectNone,
        )
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.activity_hint)))
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Activities,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectAll,
        )
        pressBack()
        checkRecordsCard(3)
    }

    @Test
    fun filterByCategory() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val name4 = "TypeName4"
        val categoryName1 = "CategoryName1"
        val categoryName2 = "CategoryName2"

        // Add data
        testUtils.addCategory(categoryName1)
        testUtils.addCategory(categoryName2)
        testUtils.addActivity(name = name1, categories = listOf(categoryName1))
        testUtils.addActivity(name = name2, categories = listOf(categoryName2))
        testUtils.addActivity(name = name3, categories = listOf(categoryName2))
        testUtils.addActivity(name = name4)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)
        testUtils.addRecord(name3)
        testUtils.addRecord(name4)
        testUtils.addRecord(name4)
        testUtils.addRecord(name4)

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter) }
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()
        tryAction { clickOnView(allOf(withText(categoryName1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(categoryName1))),
        )
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName2)))
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName2)))
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.activity_hint)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.uncategorized_time_name)),
        )
        pressBack()
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName2)))
        pressBack()
        checkRecordsCard(6)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Categories,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectNone,
        )
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.activity_hint)))
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Categories,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectAll,
        )
        pressBack()
        checkRecordsCard(6)
    }

    @Test
    fun comments() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"

        // Add activity
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, comment = comment1)
        testUtils.addRecord(name2, comment = comment1)
        testUtils.addRecord(name1, comment = comment2)
        testUtils.addRecord(name2, comment = comment2)
        testUtils.addRecord(name2, comment = comment2)

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(coreR.string.activity_hint))),
                withId(baseR.id.ivFilterItemRemove),
            ),
        )
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        clickOnViewWithText(coreR.string.records_filter_no_comment)
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnViewWithText(coreR.string.records_filter_any_comment)
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, "CoMm")
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, comment1)
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, comment2)
        pressBack()
        checkRecordsCard(3)
    }

    @Test
    fun filterTags() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2, tag3))

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.change_record_untagged)),
        )
        pressBack()
        checkRecordsCard(6)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(coreR.string.records_filter_filter_tags))),
                withId(baseR.id.ivFilterItemRemove),
            ),
        )
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(4)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(2)
    }

    @Test
    fun selectTags() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2, tag3))

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.change_record_untagged)),
        )
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2)))
        pressBack()
        checkRecordsCard(6)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(coreR.string.records_filter_select_tags))),
                withId(baseR.id.ivFilterItemRemove),
            ),
        )
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3)))
        pressBack()
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)))
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(coreR.string.activity_hint))),
                withId(baseR.id.ivFilterItemRemove),
            ),
        )
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Tags,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectNone,
        )
        pressBack()
        checkRecordsCard(0)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        selectAll(
            type = RecordsFilterSelectionButtonType.Type.Tags,
            subtype = RecordsFilterSelectionButtonType.Subtype.SelectAll,
        )
        pressBack()
        checkRecordsCard(7)
    }

    @Test
    fun selectAndFilterTags() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2, tag3))

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)),
        )
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3)),
        )
        pressBack()
        checkRecordsCard(2)
    }

    @Test
    fun manualFilter() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewFilterItem)),
                withSubstring(getString(coreR.string.activity_hint)),
            ),
        )
        clickOnView(withText("$name1 - $tag1"))
        clickOnView(withText("$name1 - $tag2"))
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_manually_filtered)))
        clickOnView(withText("$name1 - $tag1"))
        pressBack()
        checkRecordsCard(2)

        // Invert selection
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_manually_filtered)))
        clickOnViewWithText(coreR.string.records_filter_invert_selection)
        pressBack()
        checkRecordsCard(1)
    }

    @Test
    fun dayOfWeek() {
        val name1 = "TypeName1"
        val calendar: Calendar = Calendar.getInstance()
        val timeStarted = calendar.apply {
            set(2023, 4, 8, 15, 0)
        }.timeInMillis
        val timeEnded = timeStarted + TimeUnit.HOURS.toMillis(1)

        // Add data
        testUtils.addActivity(name1)
        // Monday
        repeat(1) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted,
                timeEnded = timeEnded,
            )
        }
        // Tuesday
        repeat(2) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(1),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(1),
            )
        }
        // Wednesday
        repeat(3) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(2),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(2),
            )
        }
        // Thursday
        repeat(4) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(3),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(3),
            )
        }
        // Friday
        repeat(5) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(4),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(4),
            )
        }
        // Saturday
        repeat(6) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(5),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(5),
            )
        }
        // Saturday
        repeat(7) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = timeStarted + TimeUnit.DAYS.toMillis(6),
                timeEnded = timeEnded + TimeUnit.DAYS.toMillis(6),
            )
        }

        // Check
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_overall)
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        clickOnViewWithText(coreR.string.title_today)
        clickOnViewWithText(coreR.string.range_overall)
        checkRecordsCard(28)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_monday)
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_monday)
        clickOnViewWithText(coreR.string.day_of_week_tuesday)
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_tuesday)
        clickOnViewWithText(coreR.string.day_of_week_wednesday)
        pressBack()
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_wednesday)
        clickOnViewWithText(coreR.string.day_of_week_thursday)
        pressBack()
        checkRecordsCard(4)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_thursday)
        clickOnViewWithText(coreR.string.day_of_week_friday)
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_friday)
        clickOnViewWithText(coreR.string.day_of_week_saturday)
        pressBack()
        checkRecordsCard(6)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_saturday)
        clickOnViewWithText(coreR.string.day_of_week_sunday)
        pressBack()
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_saturday)
        pressBack()
        checkRecordsCard(13)
    }

    @Test
    fun duration() {
        val name1 = "TypeName1"
        val calendar: Calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.MINUTES.toMillis(30),
        )
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.MINUTES.toMillis(80),
        )
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.MINUTES.toMillis(100),
        )

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkRecordsCard(3)

        // 0s - 1h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        repeat(6) { clickOnViewWithId(R.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        repeat(4) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        pressBack()
        checkRecordsCard(1)

        // 1h - 2h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        repeat(4) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        repeat(5) { clickOnViewWithId(R.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(R.id.tvNumberKeyboard2)
        repeat(4) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        pressBack()
        checkRecordsCard(2)

        // 0s - 2h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        repeat(5) { clickOnViewWithId(R.id.btnNumberKeyboardDelete) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        pressBack()
        checkRecordsCard(3)

        // 0s - 10m
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        repeat(5) { clickOnViewWithId(R.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        repeat(3) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        pressBack()
        checkRecordsCard(0)
    }

    @Test
    fun timeOfDay() {
        val name1 = "TypeName1"
        val calendar: Calendar = Calendar.getInstance().apply {
            set(2023, 4, 21, 0, 0, 0)
        }
        val startOfDay = calendar.timeInMillis

        // Add data
        testUtils.addActivity(name1)
        repeat(1) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = startOfDay + TimeUnit.HOURS.toMillis(2),
                timeEnded = startOfDay + TimeUnit.HOURS.toMillis(4),
            )
        }
        repeat(2) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = startOfDay + TimeUnit.HOURS.toMillis(8),
                timeEnded = startOfDay + TimeUnit.HOURS.toMillis(10),
            )
        }
        repeat(3) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = startOfDay + TimeUnit.HOURS.toMillis(14),
                timeEnded = startOfDay + TimeUnit.HOURS.toMillis(16),
            )
        }
        repeat(4) {
            testUtils.addRecord(
                typeName = name1,
                timeStarted = startOfDay + TimeUnit.HOURS.toMillis(20),
                timeEnded = startOfDay + TimeUnit.HOURS.toMillis(22),
            )
        }

        // Check
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(coreR.string.title_today), isCompletelyDisplayed()))
        clickOnViewWithText(coreR.string.range_overall)
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        clickOnViewWithText(coreR.string.title_today)
        clickOnViewWithText(coreR.string.range_overall)
        checkRecordsCard(10)

        // 0h - 1h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(1, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(0)

        // 0h - 6h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(6, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(1)

        // 0h - 12h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(12, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(3)

        // 0h - 18h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(18, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(6)

        // 0h - 23h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(23, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(10)

        // 12h - 23h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(12, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(7)

        // 18h - 6h
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(18, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(6, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        pressBack()
        checkRecordsCard(5)
    }

    @Test
    fun allFilters() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val comment1 = "comment1"
        val comment2 = "comment2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)

        // Add records
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        testUtils.addRecord(name1, comment = comment1)
        testUtils.addRecord(name1, comment = comment2)
        testUtils.addRecord(name2, comment = comment1)
        testUtils.addRecord(name2, comment = comment2)

        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag2))
        testUtils.addRecord(name2, tagNames = listOf(tag1))
        testUtils.addRecord(name2, tagNames = listOf(tag2))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2))
        testUtils.addRecord(name2, tagNames = listOf(tag1, tag2))

        testUtils.addRecord(name1, tagNames = listOf(tag1), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag2), comment = comment1)
        testUtils.addRecord(name2, tagNames = listOf(tag1), comment = comment1)
        testUtils.addRecord(name2, tagNames = listOf(tag2), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2), comment = comment1)
        testUtils.addRecord(name2, tagNames = listOf(tag1, tag2), comment = comment1)

        testUtils.addRecord(name1, tagNames = listOf(tag1), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag2), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag1), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag2), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag1, tag2), comment = comment2)

        // Check
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(name1), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1))),
        )
        checkRecordsCard(12)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        checkRecordsCard(12)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, comment2)
        pressBack()
        checkRecordsCard(4)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1)),
        )
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2)),
        )
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewFilterItem)),
                withSubstring(getString(coreR.string.activity_hint)),
            ),
        )
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordItem)), withSubstring(name2)))
        pressBack()
        checkRecordsCard(0)
    }

    private fun checkRecordsCard(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(coreR.plurals.statistics_detail_times_tracked, count),
                hasSibling(withText(count.toString())),
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun selectAll(
        type: RecordsFilterSelectionButtonType.Type,
        subtype: RecordsFilterSelectionButtonType.Subtype,
    ) {
        clickOnView(
            withTag(
                RecordsFilterSelectionButtonType(
                    type = type,
                    subtype = subtype,
                ),
            ),
        )
    }
}
