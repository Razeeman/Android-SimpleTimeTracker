package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR
import com.example.util.simpletimetracker.feature_views.R as viewsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeUntrackedTest : BaseUiTest() {

    @Test
    fun changeUntracked() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        testUtils.addActivity(name = name, color = color, icon = icon)
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(typeName = name, timeStarted = yesterday, timeEnded = yesterday)

        // Open edit view
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordStatistics))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordType))
        checkPreviewUpdated(withCardColor(viewsR.color.colorUntracked))

        // Change item
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(hasDescendant(withTag(icon)))

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record updated
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                isCompletelyDisplayed(),
            ),
        )

        // Delete record
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordDelete)

        // Untracked is back
        tryAction { checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
    }

    @Test
    fun statisticsNavigation() {
        val name = "Test"

        // Add activities
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        testUtils.addActivity(name)
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        testUtils.addRecord(typeName = name, timeStarted = yesterday, timeEnded = yesterday)

        // Check statistics navigation
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordStatistics)
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
            ),
        )
        pressBack()
        pressBack()

        // From quick actions
        longClickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        clickOnViewWithId(dialogsR.id.btnRecordQuickActionsStatistics)
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
            ),
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), matcher))
}
