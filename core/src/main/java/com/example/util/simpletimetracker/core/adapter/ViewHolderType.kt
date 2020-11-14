package com.example.util.simpletimetracker.core.adapter

import android.os.Bundle

interface ViewHolderType {

    companion object {
        const val RECORD_TYPE = 1
        const val RUNNING_RECORD = 2
        const val RECORD = 3
        const val VIEW = 4
        const val VIEW2 = 5
        const val FOOTER = 6
        const val HEADER = 7
        const val LOADER = 8
        const val EMPTY = 9
        const val DIVIDER = 10
        const val CATEGORY = 11
    }

    fun getViewType(): Int

    fun getUniqueId(): Long? = null

    fun areItemsTheSame(other: ViewHolderType): Boolean {
        return this.getViewType() == other.getViewType() &&
                this.getUniqueId() != null && other.getUniqueId() != null &&
                this.getUniqueId() == other.getUniqueId()
    }

    fun areContentsTheSame(other: ViewHolderType): Boolean = this == other

    fun getChangePayload(other: ViewHolderType): Any? = Bundle()
}