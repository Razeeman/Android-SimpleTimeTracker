package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

sealed class ChangeTagData : Parcelable {
    @Parcelize
    data class Change(
        val transitionName: String,
        val id: Long,
        val preview: Preview? = null,
    ) : ChangeTagData() {

        @Parcelize
        data class Preview(
            val name: String,
            @ColorInt val color: Int,
            val icon: RecordTypeIconParams?,
        ) : Parcelable
    }

    @Parcelize
    data class New(
        val preselectedTypeId: Long? = null,
    ) : ChangeTagData()
}
