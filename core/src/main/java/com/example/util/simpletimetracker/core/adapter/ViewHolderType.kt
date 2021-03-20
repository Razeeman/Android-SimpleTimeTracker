package com.example.util.simpletimetracker.core.adapter

import android.os.Bundle

interface ViewHolderType {

    companion object {
        const val RECORD_TYPE = 1
        const val RUNNING_RECORD = 2
        const val RECORD = 3
        const val CATEGORY = 4

        const val VIEW = 5
        const val VIEW2 = 6
        const val FOOTER = 7
        const val HEADER = 8
        const val LOADER = 9
        const val EMPTY = 10
        const val DIVIDER = 11
        const val INFO = 12
        const val HINT = 13
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