package com.example.util.simpletimetracker.feature_change_record.adapter

import android.content.res.ColorStateList
import android.text.TextWatcher
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordCommentFieldViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordCommentFieldItemBinding as Binding

fun createChangeRecordCommentFieldAdapterDelegate(
    afterTextChange: (String) -> Unit,
    onFavouriteClick: () -> Unit,
    onFavouriteLongClick: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        if (item.text != null &&
            item.text != etChangeRecordCommentField.text.toString()
        ) {
            etChangeRecordCommentField.setText(item.text)
            etChangeRecordCommentField.setSelection(item.text.length)
        }
        ViewCompat.setBackgroundTintList(
            binding.ivChangeRecordFavouriteComment,
            ColorStateList.valueOf(item.iconColor),
        )
        btnChangeRecordFavouriteComment.setOnClick { onFavouriteClick() }
        btnChangeRecordFavouriteComment.setOnLongClick { onFavouriteLongClick() }

        etChangeRecordCommentField.removeTextChangedListener(textWatcher)
        textWatcher = etChangeRecordCommentField.doAfterTextChanged { afterTextChange(it.toString()) }
    }
}

data class ChangeRecordCommentFieldViewData(
    val id: Long,
    val text: String?,
    @ColorInt val iconColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}

// Avoids setting several watchers and calling onChange several times.
private var textWatcher: TextWatcher? = null