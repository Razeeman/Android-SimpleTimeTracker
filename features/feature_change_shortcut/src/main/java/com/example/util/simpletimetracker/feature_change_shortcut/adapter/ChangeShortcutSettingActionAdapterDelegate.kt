package com.example.util.simpletimetracker.feature_change_shortcut.adapter

import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_change_shortcut.databinding.ChangeShortcutSettingActionItemBinding as Binding
import com.example.util.simpletimetracker.feature_change_shortcut.adapter.ChangeShortcutSettingActionViewData as ViewData

fun createChangeShortcutSettingActionAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as ViewData

        btnChangeShortcutSettingActionItem.text = item.text
        btnChangeShortcutSettingActionItem.setOnClickWith(item, onClick)
    }
}

data class ChangeShortcutSettingActionViewData(
    val action: RecordShortcut.SettingAction,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = action.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
