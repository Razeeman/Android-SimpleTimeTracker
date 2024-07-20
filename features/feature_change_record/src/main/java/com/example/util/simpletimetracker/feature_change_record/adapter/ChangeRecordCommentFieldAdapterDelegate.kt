package com.example.util.simpletimetracker.feature_change_record.adapter

import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentFieldViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCommentFieldItemBinding as Binding

fun createChangeRecordCommentFieldAdapterDelegate(
    afterTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        if (item.text != etChangeRecordCommentField.text.toString()) {
            etChangeRecordCommentField.setText(item.text)
        }
        btnChangeRecordSearchCommentField.setOnClick { onSearchClick() }

        etChangeRecordCommentField.removeTextChangedListener(textWatcher)
        textWatcher = etChangeRecordCommentField.doAfterTextChanged { afterTextChange(it.toString()) }
    }
}

data class ChangeRecordCommentFieldViewData(
    val id: Long,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}

private var textWatcher: TextWatcher? = null