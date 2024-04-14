package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.CardOrder
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardOrderDialogParams(
    val type: Type = Type.RecordType,
    val initialOrder: CardOrder = CardOrder.MANUAL,
) : Parcelable, ScreenParams {

    sealed interface Type : Parcelable {
        @Parcelize
        object RecordType : Type

        @Parcelize
        object Category : Type

        @Parcelize
        object Tag : Type
    }
}