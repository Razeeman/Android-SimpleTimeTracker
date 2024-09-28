package com.example.util.simpletimetracker.core.repo

import androidx.lifecycle.LiveData

interface FileWorkRepo {

    val inProgress: LiveData<Boolean>
}