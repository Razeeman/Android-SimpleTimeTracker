package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.IconEmojiRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.IconEmojiCategory
import com.example.util.simpletimetracker.domain.model.IconEmojiType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconEmojiMapper @Inject constructor(
    private val repo: IconEmojiRepo,
    private val resourceRepo: ResourceRepo
) {

    fun getAvailableEmojiCategories(): List<IconEmojiCategory> = listOf(
        IconEmojiCategory(
            type = IconEmojiType.SMILEYS,
            name = resourceRepo.getString(R.string.emojiGroupSmileys),
            categoryIcon = R.drawable.icon_category_emoji_emotions
        ),
        IconEmojiCategory(
            type = IconEmojiType.PEOPLE,
            name = resourceRepo.getString(R.string.emojiGroupPeople),
            categoryIcon = R.drawable.icon_category_emoji_people
        ),
        IconEmojiCategory(
            type = IconEmojiType.ANIMALS,
            name = resourceRepo.getString(R.string.emojiGroupAnimals),
            categoryIcon = R.drawable.icon_category_emoji_nature
        ),
        IconEmojiCategory(
            type = IconEmojiType.FOOD,
            name = resourceRepo.getString(R.string.emojiGroupFood),
            categoryIcon = R.drawable.icon_category_emoji_food_beverage
        ),
        IconEmojiCategory(
            type = IconEmojiType.TRAVEL,
            name = resourceRepo.getString(R.string.emojiGroupTravel),
            categoryIcon = R.drawable.icon_category_emoji_transportation
        ),
        IconEmojiCategory(
            type = IconEmojiType.ACTIVITIES,
            name = resourceRepo.getString(R.string.emojiGroupActivities),
            categoryIcon = R.drawable.icon_category_emoji_events
        ),
        IconEmojiCategory(
            type = IconEmojiType.OBJECTS,
            name = resourceRepo.getString(R.string.emojiGroupObjects),
            categoryIcon = R.drawable.icon_category_emoji_objects
        ),
        IconEmojiCategory(
            type = IconEmojiType.SYMBOLS,
            name = resourceRepo.getString(R.string.emojiGroupSymbols),
            categoryIcon = R.drawable.icon_category_emoji_symbols
        ),
        IconEmojiCategory(
            type = IconEmojiType.FLAGS,
            name = resourceRepo.getString(R.string.emojiGroupFlags),
            categoryIcon = R.drawable.icon_category_emoji_flags
        )
    )

    fun getAvailableEmojis(): Map<IconEmojiCategory, List<String>> =
        getAvailableEmojiCategories().associateWith { mapTypeToCodes(it.type) }

    fun hasSkinToneVariations(codes: String): Boolean =
        codes.contains(IconEmojiRepo.SKIN_TONE)

    fun toEmojiString(codes: String): String =
        if (hasSkinToneVariations(codes)) {
            replaceSameSkinTone(codes).replace(IconEmojiRepo.SKIN_TONE, "")
        } else {
            codes
        }

    fun toSkinToneVariations(codes: String): List<String> {
        if (!hasSkinToneVariations(codes)) return listOf(codes)

        // Split to skin tone variants.
        return IconEmojiRepo.skinTones.map { skinToneCode ->
            val newCodes = codes.replaceFirst(IconEmojiRepo.SKIN_TONE, skinToneCode)

            // If has seconds skin tone - split again.
            if (hasSkinToneVariations(newCodes)) {
                IconEmojiRepo.skinTones.map { secondSkinToneCode ->
                    // If has replacement and has same first skin and second skin tone - replace whole thing.
                    if (newCodes.contains(IconEmojiRepo.SAME_TONE_REPLACEMENT)) {
                        if (skinToneCode == secondSkinToneCode) {
                            replaceSameSkinTone(newCodes)
                        } else {
                            removeSameSkinToneReplacement(newCodes)
                        }
                    } else {
                        newCodes
                    }.replaceFirst(IconEmojiRepo.SKIN_TONE, secondSkinToneCode)
                }
            } else {
                listOf(newCodes)
            }
        }.flatten()
    }

    private fun replaceSameSkinTone(codes: String): String {
        // if there is a replacement - return string after marker,
        // if no replacement - return original string
        return codes.replaceBefore(IconEmojiRepo.SAME_TONE_REPLACEMENT, "")
            .replace(IconEmojiRepo.SAME_TONE_REPLACEMENT, "")
    }

    private fun removeSameSkinToneReplacement(codes: String): String {
        return codes.replaceAfter(IconEmojiRepo.SAME_TONE_REPLACEMENT, "")
            .replace(IconEmojiRepo.SAME_TONE_REPLACEMENT, "")
    }

    private fun mapTypeToCodes(type: IconEmojiType): List<String> = when (type) {
        IconEmojiType.SMILEYS -> repo.getGroupSmileys()
        IconEmojiType.PEOPLE -> repo.getGroupPeople()
        IconEmojiType.ANIMALS -> repo.getGroupAnimals()
        IconEmojiType.FOOD -> repo.getGroupFood()
        IconEmojiType.TRAVEL -> repo.getGroupTravel()
        IconEmojiType.ACTIVITIES -> repo.getGroupActivities()
        IconEmojiType.OBJECTS -> repo.getGroupObjects()
        IconEmojiType.SYMBOLS -> repo.getGroupSymbols()
        IconEmojiType.FLAGS -> repo.getGroupFlags()
    }
}