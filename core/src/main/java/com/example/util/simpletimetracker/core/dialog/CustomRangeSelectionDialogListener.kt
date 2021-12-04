package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.domain.model.Range

interface CustomRangeSelectionDialogListener {

    fun onCustomRangeSelected(range: Range)
}