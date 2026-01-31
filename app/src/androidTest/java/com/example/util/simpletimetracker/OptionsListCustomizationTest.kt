package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkCheckboxIsChecked
import com.example.util.simpletimetracker.utils.checkCheckboxIsNotChecked
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_settings.R as settingsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OptionsListCustomizationTest : BaseUiTest() {

    @Test
    fun records() {
        // Check default
        NavUtils.openRecordsScreen()
        NavUtils.openOptionsList()

        checkViewIsDisplayed(withText(R.string.records_switch_to_calendar))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.message_action_share))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()

        // Check one hidden
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.shortcut_navigation_records)

        checkCheckboxIsChecked(customizeOptionCheckboxMatcher(R.string.message_action_share))
        clickOnCustomizeOptionCheckbox(R.string.message_action_share)
        checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(R.string.message_action_share))
        pressBack()

        NavUtils.openRecordsScreen()
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.message_action_share))
        checkViewIsDisplayed(withText(R.string.records_switch_to_calendar))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()

        // Check many hidden
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.shortcut_navigation_records)

        listOf(
            R.string.range_select_day,
            R.string.range_back_to_today,
        ).forEach {
            checkCheckboxIsChecked(customizeOptionCheckboxMatcher(it))
            clickOnCustomizeOptionCheckbox(it)
            checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(it))
        }

        pressBack()

        NavUtils.openRecordsScreen()
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.message_action_share))
        checkViewDoesNotExist(withText(R.string.range_select_day))
        checkViewDoesNotExist(withText(R.string.range_back_to_today))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.records_switch_to_calendar))

        pressBack()
    }

    @Test
    fun statistics() {
        // Check default
        NavUtils.openStatisticsScreen()
        NavUtils.openOptionsList()

        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.message_action_share))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_select_range))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()

        // Check one hidden
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.shortcut_navigation_statistics)

        checkCheckboxIsChecked(customizeOptionCheckboxMatcher(R.string.message_action_share))
        clickOnCustomizeOptionCheckbox(R.string.message_action_share)
        checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(R.string.message_action_share))
        pressBack()

        NavUtils.openStatisticsScreen()
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.message_action_share))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_select_range))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()

        // Check many hidden
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.shortcut_navigation_statistics)

        listOf(
            R.string.range_select_day,
            R.string.range_back_to_today,
        ).forEach {
            checkCheckboxIsChecked(customizeOptionCheckboxMatcher(it))
            clickOnCustomizeOptionCheckbox(it)
            checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(it))
        }

        pressBack()

        NavUtils.openStatisticsScreen()
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.message_action_share))
        checkViewDoesNotExist(withText(R.string.range_select_day))
        checkViewDoesNotExist(withText(R.string.range_back_to_today))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_range))

        pressBack()
    }

    @Test
    fun statisticsDetail() {
        val typeName = "typeName"

        // Add data
        testUtils.addActivity(typeName)
        testUtils.addRecord(typeName)

        // Check default
        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(typeName), isCompletelyDisplayed())) }
        checkViewIsDisplayed(withId(statisticsDetailR.id.viewStatisticsDetailItem))
        NavUtils.openOptionsList()

        checkViewIsDisplayed(withText(R.string.types_compare_hint))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_select_range))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()
        pressBack()

        // Check one hidden
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.settings_detailed_statistics)

        checkCheckboxIsChecked(customizeOptionCheckboxMatcher(R.string.types_compare_hint))
        clickOnCustomizeOptionCheckbox(R.string.types_compare_hint)
        checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(R.string.types_compare_hint))

        pressBack()

        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(typeName), isCompletelyDisplayed())) }
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.types_compare_hint))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_day))
        checkViewIsDisplayed(withText(R.string.range_select_range))
        checkViewIsDisplayed(withText(R.string.range_back_to_today))

        pressBack()
        pressBack()

        // Check many hidden
        NavUtils.openSettingsScreen()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.settings_detailed_statistics)

        listOf(
            R.string.range_select_day,
            R.string.range_back_to_today,
        ).forEach {
            checkCheckboxIsChecked(customizeOptionCheckboxMatcher(it))
            clickOnCustomizeOptionCheckbox(it)
            checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(it))
        }

        pressBack()

        NavUtils.openStatisticsScreen()
        tryAction { clickOnView(allOf(withText(typeName), isCompletelyDisplayed())) }
        NavUtils.openOptionsList()

        checkViewDoesNotExist(withText(R.string.types_compare_hint))
        checkViewDoesNotExist(withText(R.string.range_select_day))
        checkViewDoesNotExist(withText(R.string.range_back_to_today))
        checkViewIsDisplayed(withText(R.string.chart_filter_hint))
        checkViewIsDisplayed(withText(R.string.range_select_range))

        pressBack()
    }

    @Test
    fun onlyOneOption() {
        NavUtils.openSettingsScreen()
        NavUtils.openSettingsDisplay()
        scrollSettingsRecyclerToText(coreR.string.settings_customize_options_menu)
        clickOnSettingsRecyclerText(coreR.string.settings_customize_options_menu)
        clickOnViewWithText(R.string.shortcut_navigation_records)

        listOf(
            R.string.records_switch_to_calendar,
            R.string.message_action_share,
            R.string.range_select_day,
            R.string.range_back_to_today,
        ).forEach {
            checkCheckboxIsChecked(customizeOptionCheckboxMatcher(it))
            clickOnCustomizeOptionCheckbox(it)
            checkCheckboxIsNotChecked(customizeOptionCheckboxMatcher(it))
        }

        checkCheckboxIsChecked(customizeOptionCheckboxMatcher(R.string.chart_filter_hint))
        pressBack()

        NavUtils.openRecordsScreen()
        NavUtils.openOptionsList()

        checkViewIsDisplayed(withId(R.id.rvChartFilterContainer))

        pressBack()
    }

    private fun clickOnCustomizeOptionCheckbox(@StringRes textId: Int) {
        clickOnView(customizeOptionCheckboxMatcher(textId))
    }

    private fun customizeOptionCheckboxMatcher(@StringRes textId: Int): Matcher<View> {
        return allOf(
            isDescendantOfA(withId(settingsR.id.rvCustomizeOptionsMenu)),
            hasSibling(withText(textId)),
            withId(settingsR.id.checkboxItemSettings),
        )
    }
}
