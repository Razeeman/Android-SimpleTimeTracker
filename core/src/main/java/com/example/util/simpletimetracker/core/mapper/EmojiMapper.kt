package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.repo.EmojiRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiMapper @Inject constructor(
    private val repo: EmojiRepo
) {

    fun getAvailableEmojis(): List<List<Int>> =
        (repo.getGroupSmileys() + repo.getGroupPeople())

    fun hasSkinToneVariations(codes: List<Int>): Boolean =
        codes.any { it == EmojiRepo.SKIN_TONE }

    fun toEmojiString(codes: List<Int>): String =
        codes.joinToString(separator = "") { convert(it) }

    private fun convert(code: Int): String {
        return if (code > 0xFFFF) {
            val h = (((code - 0x10000) / 0x400) + 0xD800).toChar()
            val l = (((code - 0x10000) % 0x400) + 0xDC00).toChar()
            "$h$l"
        } else {
            code.toChar().toString()
        }
    }
}