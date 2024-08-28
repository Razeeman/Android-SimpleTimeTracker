package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRecordTagTest : BaseUiTest() {

    @Test
    fun changeRecordTag() {
        val name = "Test"
        val newName = "Updated"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addRecordTag(name, activity = typeName)

        clickOnViewWithText(name)

        // View is set up
        checkViewIsDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagArchive))
        checkViewIsDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagStatistics))

        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))

        checkViewIsDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagIcon))
        checkViewIsDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagColor))
        checkViewIsDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagType))
        checkViewIsDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagDefaultType))

        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.etChangeRecordTagName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Check statistics navigation
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagStatistics)
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(name)),
            ),
        )
        pressBack()

        // Change item name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, newName)
        tryAction { checkPreviewUpdated(hasDescendant(withText(newName))) }

        // Change item color
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagColor)
        scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(lastColor))
        tryAction { checkPreviewUpdated(withCardColor(lastColor)) }
        pressBack()

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record tag updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.previewChangeRecordTag), matcher))
}
