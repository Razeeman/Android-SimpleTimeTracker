package com.example.util.simpletimetracker.core.mapper

import android.content.Context
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmojiMapper @Inject constructor(
    @AppContext private val context: Context
) {

    val availableEmojis: List<String> by lazy {
        listOf(
            "\uD83D\uDE00",
            "\uD83D\uDE01",
            "\uD83D\uDE02",
            "\uD83D\uDE03",
            "\uD83D\uDE04"
        )
    }
}