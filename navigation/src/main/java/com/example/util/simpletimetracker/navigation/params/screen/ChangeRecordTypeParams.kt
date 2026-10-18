package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ChangeRecordTypeParams : ScreenParams, Parcelable {
    abstract val sizePreview: SizePreview

    @Parcelize
    data class Change(
        val transitionName: String,
        val id: Long,
        override val sizePreview: SizePreview,
        val preview: Preview,
    ) : ChangeRecordTypeParams() {

        @Parcelize
        data class Preview(
            val name: String,
            val iconId: RecordTypeIconParams,
            val color: ColorStyleParams,
        ) : Parcelable
    }

    @Parcelize
    data class New(
        override val sizePreview: SizePreview,
    ) : ChangeRecordTypeParams()

    @Parcelize
    data class SizePreview(
        val width: Int = 0,
        val height: Int = 0,
        val asRow: Boolean = false,
    ) : Parcelable
}