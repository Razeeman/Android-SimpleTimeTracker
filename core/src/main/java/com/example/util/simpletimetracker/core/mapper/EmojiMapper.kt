package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.repo.EmojiRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiMapper @Inject constructor(
    private val repo: EmojiRepo
) {

    // TODO return categories with names
    fun getAvailableEmojis(): List<String> =
        repo.getGroupSmileys() +
            repo.getGroupPeople() +
            repo.getGroupAnimals() +
            repo.getGroupFood() +
            repo.getGroupTravel() +
            repo.getGroupActivities() +
            repo.getGroupObjects() +
            repo.getGroupSymbols() +
            repo.getGroupFlags()

    fun hasSkinToneVariations(codes: String): Boolean =
        codes.contains(EmojiRepo.SKIN_TONE)

    fun toEmojiString(codes: String): String =
        codes.replace(EmojiRepo.SKIN_TONE, "")

    fun toSkinToneVariations(codes: String): List<String> {
        val result: MutableList<String> = mutableListOf()

        // TODO can have several skin tone placements
        EmojiRepo.skinTones.forEach { skinToneCode ->
            codes.replace(EmojiRepo.SKIN_TONE, skinToneCode).let(result::add)
        }

        return result
    }
}