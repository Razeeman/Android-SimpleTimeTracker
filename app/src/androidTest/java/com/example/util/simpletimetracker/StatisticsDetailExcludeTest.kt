package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_records_filter.viewData.CategoryFilteredType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.NavUtils.fixToCurrentDate
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_records_filter.R as recordsFilterR
import com.example.util.simpletimetracker.feature_views.R as viewsR

@Suppress("SameParameterValue")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsDetailExcludeTest : BaseUiTest() {

    private val color1 = ColorMapper.getAvailableColors()[0]
    private val color2 = ColorMapper.getAvailableColors()[1]
    private val color3 = ColorMapper.getAvailableColors()[2]
    private val name1 = "name1"
    private val name2 = "name2"
    private val name3 = "name3"
    private val tag1 = "tag1"
    private val tag2 = "tag2"
    private val category1 = "category1"
    private val category2 = "category2"
    private val uncategorized = getString(coreR.string.uncategorized_time_name)
    private val untagged = getString(coreR.string.change_record_untagged)

    @Test
    fun activities() {
        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.ACTIVITY) }
        addData()
        openStats()
        checkDefault()

        // Exclude activity
        clickOnView(withText(R.string.activity_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(name2))))
            .perform(drag(Direction.LEFT, 600))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude category
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(name2)),
                withTag(RecordTypeSuggestionViewData.TEST_TAG),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.category_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(category2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude tag
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(recordsFilterR.id.rvRecordsFilterSelection)),
                withSubstring(getString(R.string.category_hint)),
            ),
        )
        clickOnView(
            allOf(
                withId(baseR.id.viewCategoryItem),
                withCardColor(color1),
                hasDescendant(withText(category2)),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.record_tag_hint_short))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(tag2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Check default
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(withSubstring(getString(coreR.string.record_tag_hint)))
        clickOnView(withSubstring(getString(coreR.string.records_filter_exclude)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewCategoryItem)),
                withText(tag2),
            ),
        )
        pressBack()
        checkDefault()
    }

    @Test
    fun categories() {
        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.CATEGORY) }
        addData()
        openStats()
        checkDefault()

        // Exclude activity
        clickOnView(withText(R.string.activity_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(name2))))
            .perform(drag(Direction.LEFT, 600))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude category
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(name2)),
                withTag(RecordTypeSuggestionViewData.TEST_TAG),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.category_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(category2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude tag
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(recordsFilterR.id.rvRecordsFilterSelection)),
                withSubstring(getString(R.string.category_hint)),
            ),
        )
        clickOnView(
            allOf(
                withId(baseR.id.viewCategoryItem),
                withTag(CategoryFilteredType),
                hasDescendant(withText(category2)),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.record_tag_hint_short))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(tag2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Check default
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(withSubstring(getString(coreR.string.record_tag_hint)))
        clickOnView(withSubstring(getString(coreR.string.records_filter_exclude)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewCategoryItem)),
                withText(tag2),
            ),
        )
        pressBack()
        checkDefault()
    }

    @Test
    fun tags() {
        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.RECORD_TAG) }
        addData()
        openStats()
        checkDefault()

        // Exclude activity
        clickOnView(withText(R.string.activity_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(name2))))
            .perform(drag(Direction.LEFT, 600))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude category
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(name2)),
                withTag(RecordTypeSuggestionViewData.TEST_TAG),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.category_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(category2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Exclude tag
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(recordsFilterR.id.rvRecordsFilterSelection)),
                withSubstring(getString(R.string.category_hint)),
            ),
        )
        clickOnView(
            allOf(
                withId(baseR.id.viewCategoryItem),
                withCardColor(color1),
                hasDescendant(withText(category2)),
            ),
        )
        pressBack()
        clickOnView(withText(R.string.record_tag_hint_short))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(tag2))))
            .perform(drag(Direction.LEFT, 600))
        clickOnView(withText(R.string.activity_hint))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(name2))
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(category2))
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "25%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "75%")
        checkViewDoesNotExist(withText(tag2))
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "25%")

        // Check default
        NavUtils.openFilter()
        Thread.sleep(1000)
        clickOnView(withSubstring(getString(R.string.records_filter_exclude)))
        clickOnView(
            allOf(
                isDescendantOfA(withId(baseR.id.viewCategoryItem)),
                withText(tag2),
            ),
        )
        pressBack()
        checkDefault()
    }

    @Test
    fun excludeOthers() {
        runBlocking { prefsInteractor.setChartFilterType(ChartFilterType.ACTIVITY) }
        addData()
        openStats()
        checkDefault()

        // Exclude other activities
        clickOnView(withText(R.string.activity_hint))
        onView(allOf(withId(baseR.id.viewStatisticsItem), hasDescendant(withText(name1))))
            .perform(drag(Direction.RIGHT, 600))
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "100%")
        checkViewDoesNotExist(withText(name2))
        checkViewDoesNotExist(withText(name3))
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "100%")
        checkViewDoesNotExist(withText(category2))
        checkViewDoesNotExist(withText(uncategorized))
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "100%")
        checkViewDoesNotExist(withText(tag2))
        checkViewDoesNotExist(withText(untagged))
    }

    private fun addData() {
        testUtils.addCategory(category1, color = color1)
        testUtils.addCategory(category2, color = color1)
        testUtils.addRecordTag(tag1, color = color2)
        testUtils.addRecordTag(tag2, color = color2)
        testUtils.addActivity(name1, color = color3, categories = listOf(category1))
        testUtils.addActivity(name2, color = color3, categories = listOf(category2))
        testUtils.addActivity(name3, color = color3)

        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name2, tagNames = listOf(tag2))
        testUtils.addRecord(name2, tagNames = listOf(tag2))
        testUtils.addRecord(name3)
    }

    private fun openStats() {
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(R.string.statistics_total_tracked), isCompletelyDisplayed())) }
        fixToCurrentDate()
    }

    private fun checkDefault() {
        Thread.sleep(1000)
        tryAction { scrollStatDetailRecyclerToTag(StatisticsDetailBlock.DataDistributionMode) }
        tryAction { clickOnView(withText(R.string.activity_hint)) }
        checkTagItem(color3, name1, "3$hourString 0$minuteString", "50%")
        checkTagItem(color3, name2, "2$hourString 0$minuteString", "33%")
        checkTagItem(color3, name3, "1$hourString 0$minuteString", "17%")
        clickOnView(withText(R.string.category_hint))
        checkTagItem(color1, category1, "3$hourString 0$minuteString", "50%")
        checkTagItem(color1, category2, "2$hourString 0$minuteString", "33%")
        checkTagItem(viewsR.color.colorUntracked, uncategorized, "1$hourString 0$minuteString", "17%")
        clickOnView(withText(R.string.record_tag_hint_short))
        checkTagItem(color2, tag1, "3$hourString 0$minuteString", "50%")
        checkTagItem(color2, tag2, "2$hourString 0$minuteString", "33%")
        checkTagItem(viewsR.color.colorUntracked, untagged, "1$hourString 0$minuteString", "17%")
    }

    private fun checkTagItem(color: Int, name: String, duration: String, percentage: String) {
        // If scroll is not possible - view is not displayed.
        scrollStatDetailRecycler(
            hasDescendant(
                allOf(
                    withId(baseR.id.viewStatisticsItem),
                    withCardColor(color),
                    hasDescendant(withText(name)),
                    hasDescendant(withText(duration)),
                    hasDescendant(withText(percentage)),
                ),
            ),
        )
    }
}
