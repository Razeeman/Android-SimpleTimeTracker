package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardOrderDialogParams(
    val type: Type = Type.RecordType(),
) : Parcelable, ScreenParams {

    sealed interface Type : Parcelable {
        @Parcelize
        data class RecordType(
            val order: CardOrder = CardOrder.MANUAL,
        ) : Type

        @Parcelize
        data class Category(
            val order: CardOrder = CardOrder.MANUAL,
        ) : Type

        @Parcelize
        data class Tag(
            val order: CardTagOrder = CardTagOrder.MANUAL,
        ) : Type
    }
}