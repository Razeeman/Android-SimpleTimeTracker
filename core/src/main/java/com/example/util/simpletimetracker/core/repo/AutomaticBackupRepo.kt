package com.example.util.simpletimetracker.core.repo

import androidx.lifecycle.LiveData

interface AutomaticBackupRepo {

    val inProgress: LiveData<Boolean>
}