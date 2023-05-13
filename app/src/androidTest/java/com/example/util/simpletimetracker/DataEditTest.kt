package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.nthChildOf
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_data_edit.R as dataEditR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@Suppress("SameParameterValue")
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DataEditTest : BaseUiTest() {

    @Test
    fun changeButtonState() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecord(name1)

        // Check
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))

        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo(), click())
        clickOnViewWithText(name2)
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))

        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
        pressBack()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))

        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))

        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo(), click())
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
    }

    @Test
    fun changeType() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"

        // Add data
        testUtils.addActivity(name1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name2, color = lastColor, icon = lastIcon)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)

        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)

        // Check before
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )

        // Select
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()

        // Check
        checkViewIsDisplayed(
            allOf(withId(dataEditR.id.tvDataEditSelectedRecords), withSubstring("2"))
        )
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).check(matches(isNotChecked()))
        clickOnViewWithId(dataEditR.id.checkboxDataEditChangeActivity)
        pressBack()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).check(matches(isNotChecked()))

        // Change
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo(), click())
        clickOnViewWithText(name2)
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).check(matches(isChecked()))
        clickOnViewWithText(coreR.string.data_edit_button_change)
        clickOnViewWithText(coreR.string.data_edit_button_change)
        Thread.sleep(1000)

        // Check
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.data_edit_success_message),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name2 - $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )
    }

    @Test
    fun changeComment() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"
        val comment3 = "comment3"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"

        // Add data
        testUtils.addActivity(name1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name2, color = lastColor, icon = lastIcon)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)

        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)

        // Check before
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )

        // Select
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()

        // Check
        checkViewIsDisplayed(
            allOf(withId(dataEditR.id.tvDataEditSelectedRecords), withSubstring("2"))
        )
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).check(matches(isNotChecked()))

        // Change
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).perform(nestedScrollTo(), click())
        typeTextIntoView(dataEditR.id.etDataEditChangeComment, comment3)
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).check(matches(isChecked()))
        clickOnViewWithText(coreR.string.data_edit_button_change)
        clickOnViewWithText(coreR.string.data_edit_button_change)
        Thread.sleep(1000)

        // Check
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.data_edit_success_message),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment3,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )
    }

    @Test
    fun addTags() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"

        // Add data
        testUtils.addActivity(name1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name2, color = lastColor, icon = lastIcon)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)

        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name1, comment = comment1)
        testUtils.addRecord(name1, comment = comment1)

        // Check before
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = name1,
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )

        // Select
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()

        // Check
        checkViewIsDisplayed(
            allOf(withId(dataEditR.id.tvDataEditSelectedRecords), withSubstring("2"))
        )
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).check(matches(isNotChecked()))
        clickOnViewWithId(dataEditR.id.checkboxDataEditAddTag)
        pressBack()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).check(matches(isNotChecked()))

        // Change
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo(), click())
        clickOnViewWithText(tag1)
        clickOnViewWithText(tag3)
        clickOnViewWithText(coreR.string.records_filter_select)
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).check(matches(isChecked()))
        clickOnViewWithText(coreR.string.data_edit_button_change)
        clickOnViewWithText(coreR.string.data_edit_button_change)
        Thread.sleep(1000)

        // Check
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.data_edit_success_message),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )
    }

    @Test
    fun removeTags() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"

        // Add data
        testUtils.addActivity(name1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name2, color = lastColor, icon = lastIcon)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)

        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)

        // Check before
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )

        // Select
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()

        // Check
        checkViewIsDisplayed(
            allOf(withId(dataEditR.id.tvDataEditSelectedRecords), withSubstring("2"))
        )
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).check(matches(isNotChecked()))
        clickOnViewWithId(dataEditR.id.checkboxDataEditRemoveTag)
        pressBack()
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).check(matches(isNotChecked()))

        // Change
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo(), click())
        clickOnViewWithText(tag1)
        clickOnViewWithText(tag3)
        clickOnViewWithText(coreR.string.records_filter_select)
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).check(matches(isChecked()))
        clickOnViewWithText(coreR.string.data_edit_button_change)
        clickOnViewWithText(coreR.string.data_edit_button_change)
        Thread.sleep(1000)

        // Check
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.data_edit_success_message),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = name1,
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )
    }

    @Test
    fun delete() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val comment1 = "comment1"
        val comment2 = "comment2"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"

        // Add data
        testUtils.addActivity(name1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name2, color = lastColor, icon = lastIcon)

        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)

        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name2, tagNames = listOf(tag2, tag3), comment = comment2)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)
        testUtils.addRecord(name1, tagNames = listOf(tag1, tag3), comment = comment1)

        // Check before
        NavUtils.openRecordsScreen()
        checkRecord(
            indexes = listOf(0, 1),
            name = "$name1 - $tag1, $tag3",
            color = firstColor,
            icon = firstIcon,
            comment = comment1,
        )
        checkRecord(
            indexes = listOf(2, 3, 4),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )

        // Select
        NavUtils.openSettingsScreen()
        NavUtils.openDataEditScreen()
        clickOnViewWithText(coreR.string.data_edit_select_records)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
        pressBack()

        // Check
        checkViewIsDisplayed(
            allOf(withId(dataEditR.id.tvDataEditSelectedRecords), withSubstring("2"))
        )
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isNotEnabled()))

        // Select other changes
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo(), click())
        clickOnViewWithText(name2)
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).check(matches(isChecked()))

        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).perform(nestedScrollTo(), click())
        typeTextIntoView(dataEditR.id.etDataEditChangeComment, "temp")
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).check(matches(isChecked()))

        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo(), click())
        clickOnViewWithText(tag2)
        clickOnViewWithText(coreR.string.records_filter_select)
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).check(matches(isChecked()))

        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo(), click())
        clickOnViewWithText(tag3)
        clickOnViewWithText(coreR.string.records_filter_select)
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).check(matches(isChecked()))

        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditDeleteRecords)).perform(nestedScrollTo(), click())

        // Other changes is reset
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeActivity)).check(matches(isNotChecked()))
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditChangeComment)).check(matches(isNotChecked()))
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditAddTag)).check(matches(isNotChecked()))
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditRemoveTag)).check(matches(isNotChecked()))

        // Change
        onView(withText(coreR.string.data_edit_button_change)).check(matches(isEnabled()))
        onView(withId(dataEditR.id.checkboxDataEditDeleteRecords)).perform(nestedScrollTo())
        onView(withId(dataEditR.id.checkboxDataEditDeleteRecords)).check(matches(isChecked()))
        clickOnViewWithText(coreR.string.data_edit_button_change)
        clickOnViewWithText(coreR.string.data_edit_button_change)
        Thread.sleep(1000)

        // Check
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.data_edit_success_message),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
        NavUtils.openRecordsScreen()
        onView(allOf(withId(recordsR.id.rvRecordsList), isCompletelyDisplayed()))
            .check(recyclerItemCount(4))
        checkRecord(
            indexes = listOf(0, 1, 2),
            name = "$name2 - $tag2, $tag3",
            color = lastColor,
            icon = lastIcon,
            comment = comment2,
        )
    }

    private fun checkRecord(
        indexes: List<Int>,
        name: String,
        color: Int,
        icon: Int,
        comment: String,
    ) {
        indexes.forEach { index ->
            checkViewIsDisplayed(
                allOf(
                    nthChildOf(withId(recordsR.id.rvRecordsList), index),
                    withId(baseR.id.viewRecordItem),
                    withCardColor(color),
                    hasDescendant(withText(name)),
                    hasDescendant(withTag(icon)),
                    hasDescendant(withText(comment)),
                    isCompletelyDisplayed()
                )
            )
        }
    }
}
