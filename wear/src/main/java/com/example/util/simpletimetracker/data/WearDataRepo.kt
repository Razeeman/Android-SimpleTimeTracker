package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearDataRepo @Inject constructor(
    private val wearRPCClient: WearRPCClient,
) {

    val dataUpdated: SharedFlow<Unit> get() = _dataUpdated.asSharedFlow()

    private var _dataUpdated: MutableSharedFlow<Unit> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun addListener() {
        wearRPCClient.addListener { _dataUpdated.tryEmit(Unit) }
    }

    fun removeListener() {
        wearRPCClient.removeListener()
    }

    suspend fun loadActivities(): List<WearActivity> {
        return wearRPCClient.queryActivities()
    }

    suspend fun loadCurrentActivities(): List<WearCurrentActivity> {
        return wearRPCClient.queryCurrentActivities()
    }

    suspend fun setCurrentActivities(starting: List<WearCurrentActivity>) {
        wearRPCClient.setCurrentActivities(starting)
    }

    suspend fun loadTagsForActivity(activityId: Long): List<WearTag> {
        return wearRPCClient.queryTagsForActivity(activityId)
    }

    suspend fun loadSettings(): WearSettings {
        return wearRPCClient.querySettings()
    }
}