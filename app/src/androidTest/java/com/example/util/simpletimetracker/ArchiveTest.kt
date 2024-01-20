package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArchiveTest : BaseUiTest() {

    @Test
    fun archivedType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecord(name1)
        Thread.sleep(1000)

        tryAction { checkTypeVisible(name1) }
        checkTypeVisible(name2)

        // Delete one
        longClickOnView(withText(name2))
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeDelete)
        // Snackbar is in the way of Add button
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        tryAction { checkTypeVisible(name1) }
        checkTypeNotVisible(name2)

        // Not shown on records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Still shown in stat filter
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        tryAction { checkTypeVisible(name1) }
        checkTypeVisible(name2)
        pressBack()

        // Still shown in stat detail filter
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        tryAction { checkTypeVisible(name1) }
        checkTypeVisible(name2)
        pressBack()
        pressBack()

        // Not shown categories selection
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_category)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_types_hint)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()

        // Not shown in record tag selection
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                withText(coreR.string.change_record_tag_type_typed)
            )
        )
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkTypeVisible(name1)
        checkTypeNotVisible(name2)
        pressBack()
        pressBack()

        // Not shown in card size
        NavUtils.openSettingsDisplay()
        NavUtils.openCardSizeScreen()
        tryAction { checkTypeVisible(name1) }
        checkTypeNotVisible(name2)
        pressBack()

        // Not shown in manual order
        clickOnSettingsSpinnerBesideText(R.string.settings_sort_order)
        clickOnViewWithText(coreR.string.settings_sort_manually)
        tryAction { checkTypeVisible(name1) }
        checkTypeNotVisible(name2)
        pressBack()

        // Shown in archive
        NavUtils.openArchiveScreen()
        checkTypeNotVisible(name1)
        checkTypeVisible(name2)

        // Restore
        clickOnViewWithText(name2)
        clickOnViewWithText(coreR.string.archive_dialog_restore)

        // Archive is empty
        checkTypeNotVisible(name1)
        checkTypeNotVisible(name2)
        checkViewIsDisplayed(withText(coreR.string.archive_empty))
        pressBack()

        // Shown again
        NavUtils.openRunningRecordsScreen()
        checkTypeVisible(name1)
        checkTypeVisible(name2)
    }

    @Test
    fun archivedRecordTag() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"
        val tag4 = "Tag4"

        testUtils.addActivity(name1)
        testUtils.addRecord(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)
        testUtils.addRecordTag(tag4)

        NavUtils.openSettingsScreen()
        NavUtils.openSettingsAdditional()
        scrollSettingsRecyclerToText(R.string.settings_show_record_tag_selection)
        clickOnSettingsCheckboxBesideText(R.string.settings_show_record_tag_selection)
        NavUtils.openCategoriesScreen()
        checkTagVisible(tag1)
        checkTagVisible(tag2)
        checkTagVisible(tag3)
        checkTagVisible(tag4)

        // Delete one
        clickOnView(withText(tag2))
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        clickOnView(withText(tag4))
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        checkTagVisible(tag1)
        checkTagNotVisible(tag2)
        checkTagVisible(tag3)
        checkTagNotVisible(tag4)
        // Snackbar is in the way of Add button
        clickOnViewWithId(com.google.android.material.R.id.snackbar_text)
        pressBack()

        // Not shown on records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnView(allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(name1))))
        tryAction { checkTagVisible(tag1) }
        checkTagNotVisible(tag2)
        checkTagVisible(tag3)
        checkTagNotVisible(tag4)
        pressBack()

        // Still shown in stat detail filter
        NavUtils.openStatisticsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsDetailR.id.cardStatisticsDetailFilter)
        clickOnViewWithText(coreR.string.records_filter_select_tags)
        checkTagVisible(tag1)
        checkTagVisible(tag2)
        checkTagVisible(tag3)
        checkTagVisible(tag4)
        pressBack()
        pressBack()

        // Not shown in tag selection dialog
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkTagVisible(tag1)
        checkTagNotVisible(tag2)
        checkTagVisible(tag3)
        checkTagNotVisible(tag4)
        pressBack()

        // Shown in archive
        NavUtils.openSettingsScreen()
        NavUtils.openArchiveScreen()
        checkTagNotVisible(tag1)
        checkTagVisible(tag2)
        checkTagNotVisible(tag3)
        checkTagVisible(tag4)

        // Restore
        clickOnViewWithText(tag2)
        clickOnViewWithText(coreR.string.archive_dialog_restore)
        clickOnViewWithText(tag4)
        clickOnViewWithText(coreR.string.archive_dialog_restore)

        // Archive is empty
        checkTagNotVisible(tag1)
        checkTagNotVisible(tag2)
        checkTagNotVisible(tag3)
        checkTagNotVisible(tag4)
        checkViewIsDisplayed(withText(coreR.string.archive_empty))
        pressBack()

        // Shown again
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withText(name1), isCompletelyDisplayed()))
        checkTagVisible(tag1)
        checkTagVisible(tag2)
        checkTagVisible(tag3)
        checkTagVisible(tag4)
        pressBack()
    }

    @Test
    fun archiveDeletionStatistics() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"
        val tag4 = "Tag4"

        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1)
        testUtils.addRecordTag(tag3)
        testUtils.addRecordTag(tag4)

        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag1))
        testUtils.addRecord(name1, tagNames = listOf(tag3))
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3))
        testUtils.addRecord(name1)
        Thread.sleep(1000)

        // Delete
        tryAction { longClickOnView(withText(name1)) }
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeDelete)
        tryAction { longClickOnView(withText(name2)) }
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeDelete)
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        tryAction { clickOnViewWithText(tag1) }
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        tryAction { clickOnViewWithText(tag2) }
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        tryAction { clickOnViewWithText(tag3) }
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        tryAction { clickOnViewWithText(tag4) }
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        pressBack()

        // Check archive
        NavUtils.openArchiveScreen()
        checkTypeVisible(name1)
        checkTypeVisible(name2)
        checkTagVisible(tag1)
        checkTagVisible(tag2)
        checkTagVisible(tag3)
        checkTagVisible(tag4)

        // Check activity with data
        clickOnViewWithText(name1)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_records_count)),
                hasDescendant(withText("6"))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_record_tags_count)),
                hasDescendant(withText("2"))
            )
        )
        pressBack()

        // Check activity with no data
        clickOnViewWithText(name2)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_records_count)),
                hasDescendant(withText("0"))
            )
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_record_tags_count)),
                hasDescendant(withText("0"))
            )
        )
        pressBack()

        // Check tag with data
        clickOnViewWithText(tag1)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_tagged_records_count)),
                hasDescendant(withText("4"))
            )
        )
        pressBack()

        // Check tag with no data
        clickOnViewWithText(tag2)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_tagged_records_count)),
                hasDescendant(withText("0"))
            )
        )
        pressBack()

        // Check general tag with data
        clickOnViewWithText(tag3)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_tagged_records_count)),
                hasDescendant(withText("2"))
            )
        )
        pressBack()

        // Check general tag with no data
        clickOnViewWithText(tag4)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.layoutArchiveDialogInfoItem),
                hasDescendant(withText(coreR.string.archive_tagged_records_count)),
                hasDescendant(withText("0"))
            )
        )
    }

    private fun checkTypeVisible(name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTypeNotVisible(name: String) {
        checkViewDoesNotExist(
            allOf(
                withId(dialogsR.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTagVisible(name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewCategoryItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkTagNotVisible(name: String) {
        checkViewDoesNotExist(
            allOf(
                withId(dialogsR.id.viewCategoryItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
    }
}
