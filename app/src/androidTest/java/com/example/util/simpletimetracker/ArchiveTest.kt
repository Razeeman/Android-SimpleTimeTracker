package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
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
import com.example.util.simpletimetracker.utils.clickOnSpinnerWithId
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArchiveTest : BaseUiTest() {

    @Test
    fun archivedType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecord(name1)

        tryAction { checkTypeVisible(name1) }
        checkTypeVisible(name2)

        // Delete one
        longClickOnView(withText(name2))
        clickOnViewWithId(R.id.btnChangeRecordTypeDelete)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)

        // Not shown on records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_type_field)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Still shown in stat filter
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        checkTypeVisible(name1)
        checkTypeVisible(name2)
        pressBack()

        // Still shown in stat detail filter
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        checkTypeVisible(name1)
        checkTypeVisible(name2)
        pressBack()
        pressBack()

        // Not shown activity tag selection
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_activity_tag)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_category_types_hint)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Not shown in record tag selection
        clickOnViewWithText(R.string.categories_add_record_tag)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_field)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()
        pressBack()

        // Not shown in card size
        NavUtils.openCardSizeScreen()
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Not shown in manual order
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Shown in archive
        NavUtils.openArchiveScreen()
        checkTypeNotVisible(name1)
        checkTypeVisible(name2)

        // Restore
        clickOnViewWithText(name2)
        clickOnViewWithText(R.string.archive_dialog_restore)

        // Archive is empty
        checkTypeNotVisible(name1)
        checkTypeNotVisible(name2)
        checkViewIsDisplayed(withText(R.string.archive_empty))
        pressBack()

        // Shown again
        NavUtils.openRunningRecordsScreen()
        checkTypeVisible(name1)
        checkTypeVisible(name2)
    }

    @Test
    fun archivedRecordTag() {
        val name1 = "TypeName1"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        testUtils.addActivity(name1)
        testUtils.addRecord(name1)
        testUtils.addRecordTag(name1, tag1)
        testUtils.addRecordTag(name1, tag2)

        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        NavUtils.openCategoriesScreen()
        checkTagVisible(tag1)
        checkTagVisible(tag2)

        // Delete one
        clickOnView(withText(tag2))
        clickOnViewWithId(R.id.btnChangeRecordTagDelete)
        checkTagVisible(tag1)
        checkTagNotVisible(tag2)
        pressBack()

        // Not shown on records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnView(withText(name1))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnViewWithText(R.string.change_record_category_field)
        checkTagVisible(tag1)
        checkTagNotVisible(tag2)
        pressBack()

        // Still shown in stat detail filter
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.cardStatisticsDetailFilter)
        checkTagVisible(tag1)
        checkTagVisible(tag2)
        pressBack()
        pressBack()

        // Not shown in tag selection dialog
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkTagVisible(tag1)
        checkTagNotVisible(tag2)
        pressBack()

        // Shown in archive
        NavUtils.openSettingsScreen()
        NavUtils.openArchiveScreen()
        checkTagNotVisible(tag1)
        checkTagVisible(tag2)

        // Restore
        clickOnViewWithText(tag2)
        clickOnViewWithText(R.string.archive_dialog_restore)

        // Archive is empty
        checkTagNotVisible(tag1)
        checkTagNotVisible(tag2)
        checkViewIsDisplayed(withText(R.string.archive_empty))
        pressBack()

        // Shown again
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkTagVisible(tag1)
        checkTagVisible(tag2)
    }

    private fun checkTypeVisible(name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTypeNotVisible(name: String) {
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTagVisible(name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTagNotVisible(name: String) {
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewCategoryItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }
}
