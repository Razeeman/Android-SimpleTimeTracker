package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.wear_api.WearRequests
import javax.inject.Inject

class WearInteractorImpl @Inject constructor(
    private val wearRPCServer: WearRPCServer,
) : WearInteractor {

    override suspend fun update() {
        wearRPCServer.updateData()
    }
}