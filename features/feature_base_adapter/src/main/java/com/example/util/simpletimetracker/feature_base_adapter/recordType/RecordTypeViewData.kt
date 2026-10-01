package com.example.util.simpletimetracker.feature_base_adapter.recordType

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class RecordTypeViewData(
    val id: Long,
    val name: String,
    val iconId: RecordTypeIcon,
    @ColorInt val iconColor: Int,
    val iconAlpha: Float = 1.0f,
    @ColorInt val color: Int,
    val width: Int,
    val height: Int,
    val asRow: Boolean = false,
    val checkStates: List<CheckState> = emptyList(),
    val itemIsFiltered: Boolean = false,
    val isComplete: Boolean = false,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordTypeViewData
}