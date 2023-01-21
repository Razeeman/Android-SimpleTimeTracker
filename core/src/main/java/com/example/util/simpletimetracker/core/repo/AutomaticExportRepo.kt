package com.example.util.simpletimetracker.core.repo

import androidx.lifecycle.LiveData

interface AutomaticExportRepo {

    val inProgress: LiveData<Boolean>
}