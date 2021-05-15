package com.example.util.simpletimetracker.feature_archive.dialog

interface ArchiveDialogListener {

    fun onDeleteClick(params: ArchiveDialogParams?)
    fun onRestoreClick(params: ArchiveDialogParams?)
}