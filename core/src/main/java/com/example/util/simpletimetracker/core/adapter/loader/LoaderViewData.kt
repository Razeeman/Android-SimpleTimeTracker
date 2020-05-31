package com.example.util.simpletimetracker.core.adapter.loader

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class LoaderViewData: ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.LOADER

    override fun getUniqueId(): Long? = 1L

    override fun areContentsTheSame(other: ViewHolderType): Boolean = true
}