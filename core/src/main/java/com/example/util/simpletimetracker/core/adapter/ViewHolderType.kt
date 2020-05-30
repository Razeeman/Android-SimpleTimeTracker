package com.example.util.simpletimetracker.core.adapter

import android.os.Bundle

interface ViewHolderType {

    companion object {
        const val VIEW = 1
        const val VIEW2 = 2
        const val FOOTER = 3
        const val HEADER = 4
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