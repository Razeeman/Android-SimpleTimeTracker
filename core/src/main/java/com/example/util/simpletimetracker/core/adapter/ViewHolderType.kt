package com.example.util.simpletimetracker.core.adapter

interface ViewHolderType {

    fun getViewType(): Int

    companion object {
        const val VIEW = 1
        const val VIEW2 = 2
        const val FOOTER = 3
    }
}