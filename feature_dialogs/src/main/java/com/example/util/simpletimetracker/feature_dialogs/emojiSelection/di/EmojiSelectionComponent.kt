package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.di

import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.view.EmojiSelectionDialogFragment
import dagger.Subcomponent

@Subcomponent
interface EmojiSelectionComponent {

    fun inject(fragment: EmojiSelectionDialogFragment)
}