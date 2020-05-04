package com.example.util.simpletimetracker.core.di.adapter

interface ViewHolderType {

    fun getViewType(): Int

    companion object {
        const val VIEW = 1
    }
}