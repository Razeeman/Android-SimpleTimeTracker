package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import javax.inject.Inject

class WearInteractorImpl @Inject constructor(
    private val wearRPCServer: WearRPCServer,
) : WearInteractor {

    override suspend fun update() {
        wearRPCServer.updateData()
    }
}