package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.FavouriteRecordsFiltersTest.StringClearedContains.Companion.containsClearedString
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.scrollRecyclerInPagerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.core.SubstringMatcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_records_filter.R as recordsFilterR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@Suppress("SameParameterValue")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class FavouriteRecordsFiltersTest : BaseUiTest() {

    private val type1 = "TypeName1"
    private val type2 = "TypeName2"
    private val type3 = "TypeName3"
    private val type4 = "TypeName4"
    private val category1 = "Category1"
    private val category2 = "Category2"
    private val category3 = "Category3"
    private val category4 = "Category4"
    private val tag1 = "Tag1"
    private val tag2 = "Tag2"
    private val tag3 = "Tag3"
    private val tag4 = "Tag4"

    private val icons by lazy {
        iconImageMapper.getAvailableImages(loadSearchHints = false)[
            iconImageMapper.getAvailableCategories(hasFavourites = false).first(),
        ].orEmpty()
    }

    private fun addData() {
        testUtils.addCategory(category1)
        testUtils.addCategory(category2)
        testUtils.addCategory(category3)
        testUtils.addCategory(category4)
        testUtils.addActivity(type1, icon = icons[1].iconResId, categories = listOf(category1))
        testUtils.addActivity(type2, icon = icons[2].iconResId, categories = listOf(category2))
        testUtils.addActivity(type3, icon = icons[3].iconResId, categories = listOf(category3))
        testUtils.addActivity(type4, icon = icons[4].iconResId, categories = listOf(category4))
        testUtils.addRecordTag(tag1)
        testUtils.addRecordTag(tag2)
        testUtils.addRecordTag(tag3)
        testUtils.addRecordTag(tag4)

        testUtils.addRecord(type1, tagNames = listOf(tag1))
        testUtils.addRecord(type2, tagNames = listOf(tag2))
        testUtils.addRecord(type3, tagNames = listOf(tag3))
        testUtils.addRecord(type4, tagNames = listOf(tag4))
    }

    @Test
    fun activity() {
        // TODO check record counts
        addData()

        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(type1), isCompletelyDisplayed()))
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(type1)
    }

    @Test
    fun activityExclude() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        tryAction {
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withTag(icons[3].iconResId)),
                ),
            )
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withTag(icons[4].iconResId)),
                ),
            )
        }
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(
            "$type1$type2$type3$type4",
            "${getString(R.string.records_filter_exclude)} $type3$type4",
        )
    }

    @Test
    fun category() {
        addData()

        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.CATEGORY) }
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(category1), isCompletelyDisplayed()))
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(category1)
    }

    @Test
    fun categoryExclude() {
        addData()

        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.CATEGORY) }
        NavUtils.openStatisticsScreen()
        openTotalTracked()
        tryAction {
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withText(category3)),
                ),
            )
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withText(category4)),
                ),
            )
        }
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(
            "$category1$category2$category3$category4",
            "${getString(R.string.records_filter_exclude)} $category3$category4",
        )
    }

    @Test
    fun tag() {
        addData()

        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.RECORD_TAG) }
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(tag1), isCompletelyDisplayed()))
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(tag1)
    }

    @Test
    fun tagExclude() {
        addData()

        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.RECORD_TAG) }
        NavUtils.openStatisticsScreen()
        openTotalTracked()
        tryAction {
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withText(tag3)),
                ),
            )
            clickOnView(
                allOf(
                    withId(statisticsDetailR.id.layoutStatisticsDetailPreviewItem),
                    hasDescendant(withText(tag4)),
                ),
            )
        }
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(
            "$tag1$tag2$tag3$tag4",
            "${getString(R.string.records_filter_exclude)} $tag3$tag4",
        )
    }

    @Test
    fun untracked() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnViewWithText(R.string.untracked_time_name)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(getString(R.string.untracked_time_name))
    }

    @Test
    fun multitask() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnViewWithText(R.string.multitask_time_name)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(getString(R.string.multitask_time_name))
    }

    @Test
    fun date() {
        addData()

        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnViewWithText(coreR.string.date_time_dialog_date)
        clickOnViewWithText(baseR.string.title_today)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(getString(R.string.title_today))
    }

    @Test
    fun comment() {
        addData()

        // No comment
        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        clickOnViewWithText(coreR.string.records_filter_no_comment)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(getString(R.string.records_filter_no_comment))
        deleteOnlyFilter()

        // Text comment
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        typeTextIntoView(recordsFilterR.id.etCommentItemField, "CoMm")
        Thread.sleep(500)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter("CoMm")
    }

    @Test
    fun daysOfWeek() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnView(withSubstring(getString(coreR.string.range_day)))
        clickOnViewWithText(coreR.string.day_of_week_monday)
        clickOnViewWithText(coreR.string.day_of_week_tuesday)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        val text = getString(R.string.day_of_week_monday) + getString(R.string.day_of_week_tuesday)
        checkFavouriteFilter(text)
    }

    @Test
    fun timeOfDay() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(8, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(19, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter("08:00 - 19:00")
    }

    @Test
    fun duration() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        repeat(4) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeEnded)
        longClickOnViewWithId(R.id.btnNumberKeyboardDelete)
        clickOnViewWithId(R.id.tvNumberKeyboard2)
        repeat(4) { clickOnViewWithId(R.id.tvNumberKeyboard0) }
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter("1$hourString - 2$hourString")
    }

    @Test
    fun manuallyFiltered() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnViewWithText(R.string.multitask_time_name)
        clickOnViewWithId(R.id.ivRecordsFilterShowList)
        tryAction { clickOnViewWithId(R.id.viewMultitaskRecordClickable) }
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter("${getString(R.string.records_filter_manually_filtered)} 1")
    }

    @Test
    fun duplications() {
        addData()

        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnViewWithText(coreR.string.records_filter_duplications)
        clickOnViewWithText(coreR.string.records_filter_duplications_same_times)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(getString(R.string.records_filter_duplications))
    }

    @Test
    fun complexFilters() {
        addData()

        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()

        // Select filters
        clickOnViewWithText(R.string.record_tag_hint)
        clickOnViewWithText(tag1)

        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        clickOnViewWithText(coreR.string.records_filter_no_comment)

        clickOnView(withSubstring(getString(coreR.string.range_day)))
        val currentDay = Calendar.getInstance()
            .get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)
            .let(timeMapper::toShortDayOfWeekName)
        clickOnViewWithText(currentDay)

        clickOnView(withSubstring(getString(coreR.string.date_time_dialog_time)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnView(withSubstring(getString(coreR.string.records_all_sort_duration)))
        clickOnViewWithId(recordsFilterR.id.fieldRecordsFilterRangeTimeStarted)
        clickOnViewWithText(coreR.string.duration_dialog_save)

        clickOnViewWithText(R.string.multitask_time_name)

        clickOnViewWithId(R.id.ivRecordsFilterShowList)
        tryAction { clickOnViewWithId(R.id.viewMultitaskRecordClickable) }

        // Check
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(
            "$type1$type2$type3$type4",
            tag1,
            getString(R.string.records_filter_no_comment),
            currentDay,
            "00:00 - 23:59",
            "0$secondString - 24$hourString",
            getString(R.string.multitask_time_name),
            "${getString(R.string.records_filter_manually_filtered)} 1",
        )
    }

    @Test
    fun availability() {
        addData()

        // Add from statistics
        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnViewWithText(R.string.untracked_time_name)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        removeFilter(R.string.untracked_time_name)
        clickOnViewWithText(R.string.multitask_time_name)
        clickOnView(withText(R.string.records_filter_save_filter))
        pressBack()
        pressBack()

        // Check data edit
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        onView(
            allOf(getFavouriteFilterMatchers(getString(R.string.untracked_time_name))),
        ).check(isCompletelyBelow(withText(R.string.records_filter_filter_not_available)))
        onView(
            allOf(getFavouriteFilterMatchers(getString(R.string.multitask_time_name))),
        ).check(isCompletelyBelow(withText(R.string.records_filter_filter_not_available)))

        // Delete
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnView(allOf(getFavouriteFilterMatchers(getString(R.string.untracked_time_name))))
        clickOnViewWithText(R.string.ok)
        clickOnView(allOf(getFavouriteFilterMatchers(getString(R.string.multitask_time_name))))
        clickOnViewWithText(R.string.ok)

        // Add from data edit
        clickOnViewWithText(coreR.string.records_filter_duplications)
        clickOnViewWithText(coreR.string.records_filter_duplications_same_times)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        removeFilter(coreR.string.records_filter_duplications)
        clickOnViewWithText(coreR.string.date_time_dialog_date)
        clickOnViewWithText(baseR.string.title_today)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))

        // Check statistics
        pressBack()
        pressBack()
        NavUtils.openStatisticsScreen()
        openTotalTracked()
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        onView(
            allOf(getFavouriteFilterMatchers(getString(R.string.records_filter_duplications))),
        ).check(isCompletelyBelow(withText(R.string.records_filter_filter_not_available)))
        onView(
            allOf(getFavouriteFilterMatchers(getString(R.string.date_time_dialog_date))),
        ).check(isCompletelyBelow(withText(R.string.records_filter_filter_not_available)))
    }

    @Test
    fun delete() {
        addData()

        // Add
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(type1), isCompletelyDisplayed()))
        NavUtils.openFilter()
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(type1)
        checkNoFavouriteFilter(type2)
        removeFilter(R.string.activity_hint)
        clickOnViewWithText(R.string.activity_hint)
        clickOnViewWithText(type2)
        clickOnViewWithText(R.string.change_record_favourite_comments_hint)
        clickOnView(withText(R.string.records_filter_save_filter))
        checkFavouriteFilter(type1)
        checkFavouriteFilter(type2)

        // Delete
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnView(allOf(getFavouriteFilterMatchers(type2)))
        clickOnViewWithText(R.string.ok)
        checkFavouriteFilter(type1)
        checkNoFavouriteFilter(type2)
        clickOnView(allOf(getFavouriteFilterMatchers(type1)))
        clickOnViewWithText(R.string.ok)
        checkNoFavouriteFilter(type1)
        checkNoFavouriteFilter(type2)
    }

    private fun openTotalTracked() {
        scrollRecyclerInPagerToView(
            statisticsR.id.rvStatisticsList,
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(withText(R.string.statistics_total_tracked)),
            ),
        )
        clickOnView(withText(R.string.statistics_total_tracked))
    }

    private fun removeFilter(textResId: Int) {
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(textResId))),
                withId(baseR.id.ivFilterItemRemove),
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun deleteOnlyFilter() {
        clickOnViewWithText(R.string.archive_dialog_delete)
        clickOnView(withId(R.id.containerFavouriteRecordsFilterItem))
        clickOnViewWithText(R.string.ok)
    }

    private fun getFavouriteFilterMatchers(vararg text: String): List<Matcher<View>> {
        return text.map {
            hasDescendant(withClearedSubstring(it))
        } + withId(R.id.containerFavouriteRecordsFilterItem)
    }

    private fun checkFavouriteFilter(vararg text: String) {
        checkViewIsDisplayed(allOf(getFavouriteFilterMatchers(*text)))
    }

    private fun checkNoFavouriteFilter(vararg text: String) {
        checkViewDoesNotExist(allOf(getFavouriteFilterMatchers(*text)))
    }

    private fun withClearedSubstring(substring: String): Matcher<View> {
        return withText(containsClearedString(substring))
    }

    private class StringClearedContains(ignoringCase: Boolean, substring: String) :
        SubstringMatcher("containing clear", ignoringCase, substring) {

        override fun evalSubstringOf(s: String): Boolean {
            return converted(s).contains(converted(substring))
        }

        override fun converted(arg: String): String {
            return super.converted(arg).replace(" ", "")
        }

        companion object {
            fun containsClearedString(substring: String): Matcher<String> {
                return StringClearedContains(false, substring)
            }
        }
    }
}
