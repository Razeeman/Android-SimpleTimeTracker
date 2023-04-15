package com.example.util.simpletimetracker.core.repo

import androidx.lifecycle.LiveData

interface DataEditRepo {

    val inProgress: LiveData<Boolean>
}