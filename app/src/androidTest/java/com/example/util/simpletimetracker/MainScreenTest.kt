package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainScreenTest : BaseUiTest() {

    @Test
    fun mainScreen() {
        val name = "Test"

        // Add activity
        tryAction { clickOnView(withText(coreR.string.running_records_add_type)) }
        closeSoftKeyboard()
        pressBack()

        clickOnView(withText(coreR.string.running_records_add_type))
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)
        closeSoftKeyboard()
        clickOnView(withText(coreR.string.change_record_type_save))
        Thread.sleep(1000)

        // Start timer
        clickOnViewWithText(name)

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(name)))

        // Records
        NavUtils.openRecordsScreen()

        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        pressBack()

        clickOnViewWithId(recordsR.id.btnRecordAdd)
        pressBack()

        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(coreR.string.change_record_save)

        clickOnViewWithId(recordsR.id.btnRecordsContainerPrevious)
        longClickOnViewWithId(recordsR.id.btnRecordsContainerToday)
        clickOnViewWithId(recordsR.id.btnRecordsContainerNext)

        // Statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        pressBack()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)

        // Settings
        NavUtils.openSettingsScreen()
    }
}
