package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.EmojiRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.EmojiCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiMapper @Inject constructor(
    private val repo: EmojiRepo,
    private val resourceRepo: ResourceRepo
) {

    fun getAvailableEmojis(): List<EmojiCategory> = listOf(
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupSmileys), repo.getGroupSmileys()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupPeople), repo.getGroupPeople()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupAnimals), repo.getGroupAnimals()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupFood), repo.getGroupFood()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupTravel), repo.getGroupTravel()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupActivities), repo.getGroupActivities()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupObjects), repo.getGroupObjects()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupSymbols), repo.getGroupSymbols()),
        EmojiCategory(resourceRepo.getString(R.string.emojiGroupFlags), repo.getGroupFlags())
    )

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
}