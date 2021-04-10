package com.example.util.simpletimetracker.core.adapter.loader

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class LoaderViewData : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is LoaderViewData

    override fun areContentsTheSame(other: ViewHolderType): Boolean = true
}