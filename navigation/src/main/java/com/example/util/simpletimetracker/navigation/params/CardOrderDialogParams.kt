package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.CardOrder
import kotlinx.parcelize.Parcelize

@Parcelize
data class CardOrderDialogParams(
    val initialOrder: CardOrder = CardOrder.MANUAL
) : Parcelable