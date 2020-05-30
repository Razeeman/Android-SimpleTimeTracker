package com.example.util.simpletimetracker.feature_running_records.viewData

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class RunningRecordEmptyViewData: ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.HEADER

    override fun getUniqueId(): Long? = 1L

    override fun areContentsTheSame(other: ViewHolderType): Boolean = true
}