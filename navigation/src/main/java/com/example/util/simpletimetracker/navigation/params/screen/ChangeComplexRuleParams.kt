package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface ChangeComplexRuleParams : ScreenParams, Parcelable {

    @Parcelize
    data class Change(
        val id: Long,
    ) : ChangeComplexRuleParams

    @Parcelize
    object New : ChangeComplexRuleParams
}