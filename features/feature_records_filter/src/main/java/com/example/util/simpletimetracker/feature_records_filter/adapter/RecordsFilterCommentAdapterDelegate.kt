package com.example.util.simpletimetracker.feature_records_filter.adapter

import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterCommentViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterCommentBinding as Binding

fun createRecordsFilterCommentAdapterDelegate(
    afterTextChange: (String) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        if (item.text != etRecordsFilterCommentItem.text.toString()) {
            etRecordsFilterCommentItem.setText(item.text)
        }
        etRecordsFilterCommentItem.removeTextChangedListener(textWatcher)
        textWatcher = etRecordsFilterCommentItem.doAfterTextChanged { afterTextChange(it.toString()) }
    }
}

private var textWatcher: TextWatcher? = null