package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.IconEmoji
import com.example.util.simpletimetracker.domain.model.IconEmojiCategory
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import com.example.util.simpletimetracker.domain.model.IconImageCategory
import com.example.util.simpletimetracker.domain.model.IconImageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconEmojiMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun getAvailableEmojiCategories(hasFavourites: Boolean): List<IconEmojiCategory> = listOfNotNull(
        IconEmojiCategory(
            type = IconEmojiType.FAVOURITES,
            name = resourceRepo.getString(R.string.change_record_favourite_comments_hint),
            categoryIcon = R.drawable.icon_category_image_favourite,
        ).takeIf { hasFavourites },
        IconEmojiCategory(
            type = IconEmojiType.SMILEYS,
            name = resourceRepo.getString(R.string.emojiGroupSmileys),
            categoryIcon = R.drawable.icon_category_emoji_emotions,
        ),
        IconEmojiCategory(
            type = IconEmojiType.PEOPLE,
            name = resourceRepo.getString(R.string.emojiGroupPeople),
            categoryIcon = R.drawable.icon_category_emoji_people,
        ),
        IconEmojiCategory(
            type = IconEmojiType.ANIMALS,
            name = resourceRepo.getString(R.string.emojiGroupAnimals),
            categoryIcon = R.drawable.icon_category_emoji_nature,
        ),
        IconEmojiCategory(
            type = IconEmojiType.FOOD,
            name = resourceRepo.getString(R.string.emojiGroupFood),
            categoryIcon = R.drawable.icon_category_emoji_food_beverage,
        ),
        IconEmojiCategory(
            type = IconEmojiType.TRAVEL,
            name = resourceRepo.getString(R.string.emojiGroupTravel),
            categoryIcon = R.drawable.icon_category_emoji_transportation,
        ),
        IconEmojiCategory(
            type = IconEmojiType.ACTIVITIES,
            name = resourceRepo.getString(R.string.emojiGroupActivities),
            categoryIcon = R.drawable.icon_category_emoji_events,
        ),
        IconEmojiCategory(
            type = IconEmojiType.OBJECTS,
            name = resourceRepo.getString(R.string.emojiGroupObjects),
            categoryIcon = R.drawable.icon_category_emoji_objects,
        ),
        IconEmojiCategory(
            type = IconEmojiType.SYMBOLS,
            name = resourceRepo.getString(R.string.emojiGroupSymbols),
            categoryIcon = R.drawable.icon_category_emoji_symbols,
        ),
        IconEmojiCategory(
            type = IconEmojiType.FLAGS,
            name = resourceRepo.getString(R.string.emojiGroupFlags),
            categoryIcon = R.drawable.icon_category_emoji_flags,
        ),
    )

    fun getAvailableEmojis(
        loadSearchHints: Boolean,
    ): Map<IconEmojiCategory, List<IconEmoji>> {
        return getAvailableEmojiCategories(true).associateWith {
            val codes = mapTypeToCodesArray(it.type)
                ?.let(resourceRepo::getStringArray).orEmpty()
            val searchHints = if (loadSearchHints) {
                mapTypeToSearchArray(it.type)
                    ?.let(resourceRepo::getStringArray).orEmpty()
            } else {
                emptyList()
            }

            codes.mapIndexed { index, emojiCode ->
                IconEmoji(
                    emojiCode = emojiCode,
                    emojiSearch = searchHints.getOrNull(index).orEmpty(),
                )
            }
        }
    }

    fun hasSkinToneVariations(codes: String): Boolean {
        return codes.contains(SKIN_TONE)
    }

    fun toEmojiString(codes: String): String {
        return if (hasSkinToneVariations(codes)) {
            replaceSameSkinTone(codes).replace(SKIN_TONE, "")
        } else {
            codes
        }
    }

    fun toSkinToneVariations(codes: String): List<String> {
        if (!hasSkinToneVariations(codes)) return listOf(codes)

        // Split to skin tone variants.
        return skinTones.map { skinToneCode ->
            val newCodes = codes.replaceFirst(SKIN_TONE, skinToneCode)

            // If has seconds skin tone - split again.
            if (hasSkinToneVariations(newCodes)) {
                skinTones.map { secondSkinToneCode ->
                    // If has replacement and has same first skin and second skin tone - replace whole thing.
                    if (newCodes.contains(SAME_TONE_REPLACEMENT)) {
                        if (skinToneCode == secondSkinToneCode) {
                            replaceSameSkinTone(newCodes)
                        } else {
                            removeSameSkinToneReplacement(newCodes)
                        }
                    } else {
                        newCodes
                    }.replaceFirst(SKIN_TONE, secondSkinToneCode)
                }
            } else {
                listOf(newCodes)
            }
        }.flatten()
    }

    private fun replaceSameSkinTone(codes: String): String {
        // if there is a replacement - return string after marker,
        // if no replacement - return original string
        return codes.replaceBefore(SAME_TONE_REPLACEMENT, "")
            .replace(SAME_TONE_REPLACEMENT, "")
    }

    private fun removeSameSkinToneReplacement(codes: String): String {
        return codes.replaceAfter(SAME_TONE_REPLACEMENT, "")
            .replace(SAME_TONE_REPLACEMENT, "")
    }

    private fun mapTypeToCodesArray(type: IconEmojiType): Int? = when (type) {
        IconEmojiType.FAVOURITES -> null
        IconEmojiType.SMILEYS -> R.array.emoji_smileys
        IconEmojiType.PEOPLE -> R.array.emoji_people
        IconEmojiType.ANIMALS -> R.array.emoji_animals
        IconEmojiType.FOOD -> R.array.emoji_food
        IconEmojiType.TRAVEL -> R.array.emoji_travel
        IconEmojiType.ACTIVITIES -> R.array.emoji_activities
        IconEmojiType.OBJECTS -> R.array.emoji_objects
        IconEmojiType.SYMBOLS -> R.array.emoji_symbols
        IconEmojiType.FLAGS -> R.array.emoji_flags
    }

    private fun mapTypeToSearchArray(type: IconEmojiType): Int? = when (type) {
        IconEmojiType.FAVOURITES -> null
        IconEmojiType.SMILEYS -> R.array.emoji_hint_smileys
        IconEmojiType.PEOPLE -> R.array.emoji_hint_people
        IconEmojiType.ANIMALS -> R.array.emoji_hint_animals
        IconEmojiType.FOOD -> R.array.emoji_hint_food
        IconEmojiType.TRAVEL -> R.array.emoji_hint_travel
        IconEmojiType.ACTIVITIES -> R.array.emoji_hint_activities
        IconEmojiType.OBJECTS -> R.array.emoji_hint_objects
        IconEmojiType.SYMBOLS -> R.array.emoji_hint_symbols
        IconEmojiType.FLAGS -> R.array.emoji_hint_flags
    }

    companion object {
        const val SKIN_TONE: String = "SKIN_TONE"
        const val SAME_TONE_REPLACEMENT = "SAME_TONE"
        val skinTones: List<String> = listOf(
            "🏻", // light skin tone
            "🏼", // medium-light skin tone
            "🏽", // medium skin tone
            "🏾", // medium-dark skin tone
            "🏿", // dark skin tone
        )
    }
}