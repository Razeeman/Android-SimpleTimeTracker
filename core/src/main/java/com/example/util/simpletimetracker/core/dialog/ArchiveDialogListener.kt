package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams

interface ArchiveDialogListener {

    fun onDeleteClick(params: ArchiveDialogParams?)
    fun onRestoreClick(params: ArchiveDialogParams?)
}