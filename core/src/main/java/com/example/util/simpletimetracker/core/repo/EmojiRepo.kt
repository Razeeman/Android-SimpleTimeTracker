package com.example.util.simpletimetracker.core.repo

import android.content.Context
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class EmojiRepo @Inject constructor(
    @AppContext private val context: Context
) {

    fun getGroupSmileys(): List<String> =
        context.resources.getStringArray(R.array.emoji_smileys).toList()

    fun getGroupPeople(): List<String> =
        context.resources.getStringArray(R.array.emoji_people).toList()

    fun getGroupAnimals(): List<String> =
        context.resources.getStringArray(R.array.emoji_animals).toList()

    fun getGroupFood(): List<String> =
        context.resources.getStringArray(R.array.emoji_food).toList()

    fun getGroupTravel(): List<String> =
        context.resources.getStringArray(R.array.emoji_travel).toList()

    fun getGroupActivities(): List<String> =
        context.resources.getStringArray(R.array.emoji_activities).toList()

    fun getGroupObjects(): List<String> =
        context.resources.getStringArray(R.array.emoji_objects).toList()

    fun getGroupSymbols(): List<String> =
        context.resources.getStringArray(R.array.emoji_symbols).toList()

    fun getGroupFlags(): List<String> =
        context.resources.getStringArray(R.array.emoji_flags).toList()

    companion object {
        const val SKIN_TONE: String = "SKIN_TONE"
        val skinTones: List<String> = listOf(
            "🏻", // light skin tone
            "🏼", // medium-light skin tone
            "🏽", // medium skin tone
            "🏾", // medium-dark skin tone
            "🏿" // dark skin tone
        )
    }
}