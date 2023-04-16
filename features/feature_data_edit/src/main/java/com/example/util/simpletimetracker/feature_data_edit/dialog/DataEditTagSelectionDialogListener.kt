package com.example.util.simpletimetracker.feature_data_edit.dialog

interface DataEditTagSelectionDialogListener {

    fun onTagsSelected(tag: String, tagIds: List<Long>)
    fun onTagsDismissed()
}