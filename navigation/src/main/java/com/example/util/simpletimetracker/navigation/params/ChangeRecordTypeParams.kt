package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

sealed class ChangeRecordTypeParams : Parcelable {
    abstract val sizePreview: SizePreview

    @Parcelize
    data class Change(
        val transitionName: String,
        val id: Long,
        override val sizePreview: SizePreview,
        val preview: Preview
    ) : ChangeRecordTypeParams() {

        @Parcelize
        data class Preview(
            val name: String,
            val iconId: RecordTypeIconParams,
            @ColorInt val color: Int
        ) : Parcelable
    }

    @Parcelize
    data class New(
        override val sizePreview: SizePreview
    ) : ChangeRecordTypeParams()

    @Parcelize
    data class SizePreview(
        val width: Int? = null,
        val height: Int? = null,
        val asRow: Boolean = false
    ) : Parcelable
}