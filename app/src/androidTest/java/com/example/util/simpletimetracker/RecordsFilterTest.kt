package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
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
class RecordsFilterTest : BaseUiTest() {

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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
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
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRecordTypeItem)), withText(name2)))
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(categoryName1)))
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
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.uncategorized_time_name))
        )
        pressBack()
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName1)))
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(categoryName2)))
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
        )
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                hasSibling(withSubstring(getString(coreR.string.activity_hint))),
                withId(baseR.id.ivRecordFilterItemRemove)
            )
        )
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        clickOnViewWithText(coreR.string.records_filter_no_comment)
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        clickOnViewWithText(coreR.string.records_filter_any_comment)
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, "CoMm")
        pressBack()
        checkRecordsCard(5)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
        typeTextIntoView(recordsFilterR.id.etRecordsFilterCommentItem, comment1)
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.change_record_comment_field)))
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.change_record_untagged))
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
                withId(baseR.id.ivRecordFilterItemRemove)
            )
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(coreR.string.change_record_untagged))
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
                withId(baseR.id.ivRecordFilterItemRemove)
            )
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
        )
        checkRecordsCard(7)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_select_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1))
        )
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag3))
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
        )
        checkRecordsCard(3)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRecordFilterItem)),
                withSubstring(getString(coreR.string.activity_hint))
            )
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
            allOf(withId(statisticsDetailR.id.viewStatisticsDetailItem), hasDescendant(withText(name1)))
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
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag1))
        )
        pressBack()
        checkRecordsCard(2)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(withSubstring(getString(coreR.string.records_filter_filter_tags)))
        clickOnView(
            allOf(isDescendantOfA(withId(baseR.id.viewCategoryItem)), withText(tag2))
        )
        pressBack()
        checkRecordsCard(1)

        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewRecordFilterItem)),
                withSubstring(getString(coreR.string.activity_hint))
            )
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
                isCompletelyDisplayed()
            )
        )
    }
}
