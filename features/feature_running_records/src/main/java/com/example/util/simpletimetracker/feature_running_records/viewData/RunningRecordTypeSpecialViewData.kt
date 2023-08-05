package com.example.util.simpletimetracker.feature_running_records.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RunningRecordTypeSpecialViewData(
    val type: Type,
    val name: String,
    val iconId: RecordTypeIcon,
    @ColorInt val color: Int,
    val width: Int,
    val height: Int,
    val asRow: Boolean = false
) : ViewHolderType {

    override fun getUniqueId(): Long = type.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is RunningRecordTypeSpecialViewData

    sealed interface Type {
        object Add : Type
        object Default : Type
        object Repeat : Type
    }
}