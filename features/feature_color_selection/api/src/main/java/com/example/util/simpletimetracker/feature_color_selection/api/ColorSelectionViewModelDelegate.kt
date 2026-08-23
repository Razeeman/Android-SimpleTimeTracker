package com.example.util.simpletimetracker.feature_color_selection.api

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData

interface ColorSelectionViewModelDelegate {
    val colors: LiveData<List<ViewHolderType>>
    var newColor: AppColor

    fun attach(parent: Parent)
    fun clearColorDelegate()
    suspend fun updateColorViewData()
    fun onColorClick(item: ColorViewData)
    fun onColorPaletteClick()
    fun onColorFavouriteClick()
    fun onCustomColorSelected(tag: String, colorInt: Int)

    interface Parent {
        fun getDialogTag(): String
        suspend fun update()
        fun onColorSelected() = Unit
        suspend fun isColorSelectedCheck(): Boolean = true
    }
}