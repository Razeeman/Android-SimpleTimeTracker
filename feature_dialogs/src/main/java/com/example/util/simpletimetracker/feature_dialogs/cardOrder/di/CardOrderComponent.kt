package com.example.util.simpletimetracker.feature_dialogs.cardOrder.di

import com.example.util.simpletimetracker.feature_dialogs.cardOrder.view.CardOrderDialogFragment
import dagger.Subcomponent

@Subcomponent
interface CardOrderComponent {

    fun inject(fragment: CardOrderDialogFragment)
}