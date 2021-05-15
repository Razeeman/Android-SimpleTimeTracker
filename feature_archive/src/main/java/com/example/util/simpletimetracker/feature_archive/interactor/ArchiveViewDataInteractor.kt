package com.example.util.simpletimetracker.feature_archive.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import javax.inject.Inject

class ArchiveViewDataInteractor @Inject constructor() {

    suspend fun getViewData(): List<ViewHolderType> {
        return emptyList() // TODO
    }
}