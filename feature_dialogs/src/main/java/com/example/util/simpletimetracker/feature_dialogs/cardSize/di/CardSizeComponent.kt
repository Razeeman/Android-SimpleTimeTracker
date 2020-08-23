package com.example.util.simpletimetracker.feature_dialogs.cardSize.di

import com.example.util.simpletimetracker.feature_dialogs.cardSize.view.CardSizeDialogFragment
import dagger.Subcomponent

@Subcomponent
interface CardSizeComponent {

    fun inject(fragment: CardSizeDialogFragment)
}