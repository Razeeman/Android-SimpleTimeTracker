package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

sealed interface ChangeActivityFilterParams : ScreenParams, Parcelable {

    @Parcelize
    data class Change(
        val transitionName: String,
        val id: Long,
        val preview: Preview? = null,
    ) : ChangeActivityFilterParams {

        @Parcelize
        data class Preview(
            val name: String,
            @ColorInt val color: Int,
        ) : Parcelable
    }

    @Parcelize
    object New : ChangeActivityFilterParams
}