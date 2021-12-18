package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

object ColorPaletteViewData : ViewHolderType {

    // Only one in recycler
    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is ColorPaletteViewData
}