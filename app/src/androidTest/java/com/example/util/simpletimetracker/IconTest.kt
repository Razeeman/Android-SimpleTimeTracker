package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.swipeUp
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class IconTest : BaseUiTest() {

    @Test
    fun iconEmojiTest() {
        val firstName = "first"
        val secondName = "last"

        // Add activity
        testUtils.addActivity(name = secondName, text = lastEmoji)

        // Open change record type screen
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Select emoji
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.btnChangeRecordTypeIconSwitch)),
                withText(R.string.change_record_type_icon_emoji_hint)
            )
        )
        tryAction { clickOnViewWithText(firstEmoji) }

        // Preview is updated
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), hasDescendant(withText(firstEmoji))))

        // Save
        clickOnViewWithId(R.id.fieldChangeRecordTypeIcon)
        typeTextIntoView(R.id.etChangeRecordTypeName, firstName)
        clickOnViewWithText(R.string.change_record_type_save)

        // Record type is created
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstEmoji))
                )
            )
        }

        // Start timer
        clickOnViewWithText(firstName)

        // Check running record
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(firstName)),
                hasDescendant(withText(firstEmoji))
            )
        )

        // Change running record
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(firstName)))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), hasDescendant(withText(firstEmoji))))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(secondName))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), hasDescendant(withText(lastEmoji))))
        clickOnViewWithText(R.string.change_record_save)

        // Check running record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(secondName)),
                    hasDescendant(withText(lastEmoji))
                )
            )
        }

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(secondName)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(secondName)),
                hasDescendant(withText(lastEmoji)),
                isCompletelyDisplayed()
            )
        )

        // Change record
        clickOnView(allOf(withText(secondName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), hasDescendant(withText(lastEmoji))))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(firstName))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), hasDescendant(withText(firstEmoji))))
        clickOnViewWithText(R.string.change_record_type_save)

        // Check record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstEmoji)),
                    isCompletelyDisplayed()
                )
            )
        }
    }

    @Test
    fun iconTextTest() {
        val firstName = "firstName"
        val firstIconText = "firstIconText"
        val secondName = "secondName"
        val secondIconText = "secondIconText"

        // Add activity
        testUtils.addActivity(name = secondName, text = secondIconText)

        // Open change record type screen
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Select emoji
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.btnChangeRecordTypeIconSwitch)),
                withText(R.string.change_record_type_icon_text_hint)
            )
        )
        tryAction { typeTextIntoView(R.id.etChangeRecordTypeIconText, firstIconText) }

        // Preview is updated
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), hasDescendant(withText(firstIconText))))

        // Save
        clickOnViewWithId(R.id.fieldChangeRecordTypeIcon)
        typeTextIntoView(R.id.etChangeRecordTypeName, firstName)
        clickOnViewWithText(R.string.change_record_type_save)

        // Record type is created
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstIconText))
                )
            )
        }

        // Start timer
        clickOnViewWithText(firstName)

        // Check running record
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(firstName)),
                hasDescendant(withText(firstIconText))
            )
        )

        // Change running record
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(firstName)))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), hasDescendant(withText(firstIconText))))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(secondName))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), hasDescendant(withText(secondIconText))))
        clickOnViewWithText(R.string.change_record_save)

        // Check running record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(secondName)),
                    hasDescendant(withText(secondIconText))
                )
            )
        }

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(secondName)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(secondName)),
                hasDescendant(withText(secondIconText)),
                isCompletelyDisplayed()
            )
        )

        // Change record
        clickOnView(allOf(withText(secondName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), hasDescendant(withText(secondIconText))))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(firstName))
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), hasDescendant(withText(firstIconText))))
        clickOnViewWithText(R.string.change_record_type_save)

        // Check record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstIconText)),
                    isCompletelyDisplayed()
                )
            )
        }
    }

    @Test
    fun iconImageCategorySelection() {
        // Open record type add
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Open image icons
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)

        // Check categories
        iconImageMapper.getAvailableImages().forEach { (category, images) ->
            checkViewIsDisplayed(withTag(category.categoryIcon))
            clickOnView(withTag(category.categoryIcon))
            val firstImage = images.values.first()

            if (category == iconImageMapper.getAvailableCategories().last()) {
                onView(withId(R.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
                onView(withId(R.id.rvChangeRecordTypeIcon)).perform(swipeUp(50))
            }

            // Check category hint
            checkViewIsDisplayed(withText(category.name))
            // Check first icon in category
            checkViewIsDisplayed(withTag(firstImage))
        }
    }

    @Test
    fun iconEmojiCategorySelection() {
        // Open record type add
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Open emoji icons
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.btnChangeRecordTypeIconSwitch)),
                withText(R.string.change_record_type_icon_emoji_hint)
            )
        )

        // Check categories
        iconEmojiMapper.getAvailableEmojis().forEach { (category, emojis) ->
            checkViewIsDisplayed(withTag(category.categoryIcon))
            clickOnView(withTag(category.categoryIcon))
            val firstEmoji = iconEmojiMapper.toEmojiString(emojis.first())

            // Check category hint
            checkViewIsDisplayed(withText(category.name))
            // Check first icon in category
            checkViewIsDisplayed(withText(firstEmoji))
        }
    }

    @Test
    fun skinToneSelectionDialog() {
        val name = "name"
        val category = iconEmojiMapper.getAvailableEmojiCategories().first { it.type == IconEmojiType.PEOPLE }
        val emoji = iconEmojiMapper.getAvailableEmojis()[category]?.first() ?: throw RuntimeException()
        val emojiDefault = emoji.let(iconEmojiMapper::toEmojiString)
        val emojiSkinTones = emoji.let(iconEmojiMapper::toSkinToneVariations)
        val emojiSkinTone = emojiSkinTones.last()

        // Open record type add
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }
        typeTextIntoView(R.id.etChangeRecordTypeName, name)

        // Open emoji icons
        clickOnViewWithText(R.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.btnChangeRecordTypeIconSwitch)),
                withText(R.string.change_record_type_icon_emoji_hint)
            )
        )
        onView(withId(R.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
        scrollRecyclerToView(R.id.rvChangeRecordTypeIcon, hasDescendant(withText(emojiDefault)))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withText(emojiDefault))

        // Check dialog
        onView(withId(R.id.rvEmojiSelectionContainer)).check(recyclerItemCount(6))

        // Check emojis
        checkViewIsDisplayed(withText(emojiDefault))
        emojiSkinTones.forEach {
            checkViewIsDisplayed(withText(it))
        }

        clickOnViewWithText(emojiSkinTone)

        // Preview is updated
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), hasDescendant(withText(emojiSkinTone))))

        // Check record type
        clickOnViewWithText(R.string.change_record_type_save)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(emojiSkinTone))
            )
        )
    }
}
