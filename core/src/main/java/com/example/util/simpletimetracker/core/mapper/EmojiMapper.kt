package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.EmojiRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.EmojiCategory
import com.example.util.simpletimetracker.domain.model.EmojiType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiMapper @Inject constructor(
    private val repo: EmojiRepo,
    private val resourceRepo: ResourceRepo
) {

    fun getAvailableEmojiCategories(): List<EmojiCategory> = listOf(
        EmojiCategory(
            EmojiType.SMILEYS, resourceRepo.getString(R.string.emojiGroupSmileys), "\uD83D\uDE00"
        ),
        EmojiCategory(
            EmojiType.PEOPLE, resourceRepo.getString(R.string.emojiGroupPeople), "\uD83D\uDC4B"
        ),
        EmojiCategory(
            EmojiType.ANIMALS, resourceRepo.getString(R.string.emojiGroupAnimals), "\uD83D\uDC35"
        ),
        EmojiCategory(
            EmojiType.FOOD, resourceRepo.getString(R.string.emojiGroupFood), "\uD83C\uDF47"
        ),
        EmojiCategory(
            EmojiType.TRAVEL, resourceRepo.getString(R.string.emojiGroupTravel), "\uD83C\uDF0D"
        ),
        EmojiCategory(
            EmojiType.ACTIVITIES, resourceRepo.getString(R.string.emojiGroupActivities), "\uD83C\uDF83"
        ),
        EmojiCategory(
            EmojiType.OBJECTS, resourceRepo.getString(R.string.emojiGroupObjects), "\uD83D\uDC53"
        ),
        EmojiCategory(
            EmojiType.SYMBOLS, resourceRepo.getString(R.string.emojiGroupSymbols), "\uD83C\uDFE7"
        ),
        EmojiCategory(
            EmojiType.FLAGS, resourceRepo.getString(R.string.emojiGroupFlags), "\uD83C\uDFC1"
        )
    )

    fun getAvailableEmojis(): Map<EmojiCategory, List<String>> =
        getAvailableEmojiCategories().map { it to mapTypeToCodes(it.type) }.toMap()

    fun hasSkinToneVariations(codes: String): Boolean =
        codes.contains(EmojiRepo.SKIN_TONE)

    fun toEmojiString(codes: String): String =
        if (hasSkinToneVariations(codes)) {
            replaceSameSkinTone(codes).replace(EmojiRepo.SKIN_TONE, "")
        } else {
            codes
        }

    fun toSkinToneVariations(codes: String): List<String> {
        if (!hasSkinToneVariations(codes)) return listOf(codes)

        // Split to skin tone variants.
        return EmojiRepo.skinTones.map { skinToneCode ->
            val newCodes = codes.replaceFirst(EmojiRepo.SKIN_TONE, skinToneCode)

            // If has seconds skin tone - split again.
            if (hasSkinToneVariations(newCodes)) {
                EmojiRepo.skinTones.map { secondSkinToneCode ->
                    // If has replacement and has same first skin and second skin tone - replace whole thing.
                    if (newCodes.contains(EmojiRepo.SAME_TONE_REPLACEMENT)) {
                        if (skinToneCode == secondSkinToneCode) {
                            replaceSameSkinTone(newCodes)
                        } else {
                            removeSameSkinToneReplacement(newCodes)
                        }
                    } else {
                        newCodes
                    }.replaceFirst(EmojiRepo.SKIN_TONE, secondSkinToneCode)
                }
            } else {
                listOf(newCodes)
            }
        }.flatten()
    }

    private fun replaceSameSkinTone(codes: String): String {
        // if there is a replacement - return string after marker,
        // if no replacement - return original string
        return codes.replaceBefore(EmojiRepo.SAME_TONE_REPLACEMENT, "")
            .replace(EmojiRepo.SAME_TONE_REPLACEMENT, "")
    }

    private fun removeSameSkinToneReplacement(codes: String): String {
        return codes.replaceAfter(EmojiRepo.SAME_TONE_REPLACEMENT, "")
            .replace(EmojiRepo.SAME_TONE_REPLACEMENT, "")
    }

    private fun mapTypeToCodes(type: EmojiType): List<String> = when (type) {
        EmojiType.SMILEYS -> repo.getGroupSmileys()
        EmojiType.PEOPLE -> repo.getGroupPeople()
        EmojiType.ANIMALS -> repo.getGroupAnimals()
        EmojiType.FOOD -> repo.getGroupFood()
        EmojiType.TRAVEL -> repo.getGroupTravel()
        EmojiType.ACTIVITIES -> repo.getGroupActivities()
        EmojiType.OBJECTS -> repo.getGroupObjects()
        EmojiType.SYMBOLS -> repo.getGroupSymbols()
        EmojiType.FLAGS -> repo.getGroupFlags()
    }
}