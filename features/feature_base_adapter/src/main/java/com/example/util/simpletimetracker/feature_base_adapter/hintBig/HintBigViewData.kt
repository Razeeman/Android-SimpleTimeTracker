package com.example.util.simpletimetracker.feature_base_adapter.hintBig

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class HintBigViewData(
    val text: String,
    val infoIconVisible: Boolean,
    val closeIconVisible: Boolean,
    val button: Button = Button.Hidden,
) : ViewHolderType {

    sealed interface Button {
        object Hidden : Button

        data class Present(
            val text: String,
            val type: ButtonType,
        ) : Button
    }

    interface ButtonType

    override fun getUniqueId(): Long = text.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is HintBigViewData
}