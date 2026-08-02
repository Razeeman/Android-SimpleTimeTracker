package com.example.util.simpletimetracker.feature_change_record.api.view

import androidx.appcompat.widget.AppCompatTextView
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.viewData.ChangeRecordDateTimeState
import com.example.util.simpletimetracker.feature_change_record.api.databinding.ChangeRecordCoreLayoutBinding
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

interface ChangeRecordViewDelegate {
    fun initUi(binding: ChangeRecordCoreLayoutBinding)

    fun <T : ViewBinding> initUx(
        fragment: BaseFragment<T>,
        binding: ChangeRecordCoreLayoutBinding,
    )

    fun <T : ViewBinding> initViewModel(
        fragment: BaseFragment<T>,
        binding: ChangeRecordCoreLayoutBinding,
    )

    fun onSetPreview(
        binding: ChangeRecordCoreLayoutBinding,
        color: Int,
        iconId: RecordTypeIcon,
    )

    fun setDateTime(
        state: ChangeRecordDateTimeState,
        dateView: AppCompatTextView,
        timeView: AppCompatTextView,
        hintView: AppCompatTextView,
    )
}