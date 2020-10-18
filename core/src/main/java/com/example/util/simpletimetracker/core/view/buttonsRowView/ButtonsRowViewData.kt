package com.example.util.simpletimetracker.core.view.buttonsRowView

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

abstract class ButtonsRowViewData : ViewHolderType {

    abstract val id: Long
    abstract val name: String
    abstract val isSelected: Boolean

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = id
}