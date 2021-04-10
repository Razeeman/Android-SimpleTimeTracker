package com.example.util.simpletimetracker.core.adapter

import android.os.Bundle

interface ViewHolderType {

    fun getUniqueId(): Long

    fun isValidType(other: ViewHolderType): Boolean

    fun areItemsTheSame(other: ViewHolderType): Boolean =
        this.isValidType(other) && this.getUniqueId() == other.getUniqueId()

    fun areContentsTheSame(other: ViewHolderType): Boolean =
        this == other

    fun getChangePayload(other: ViewHolderType): Any? = Bundle()
}