package com.example.util.simpletimetracker.domain.repo

import com.example.util.simpletimetracker.domain.model.ActivityFilter

interface ActivityFilterRepo {

    suspend fun getAll(): List<ActivityFilter>

    suspend fun get(id: Long): ActivityFilter?

    suspend fun getByTypeId(typeId: Long): List<ActivityFilter>

    suspend fun add(activityFilter: ActivityFilter): Long

    suspend fun changeSelected(id: Long, selected: Boolean)

    suspend fun changeSelectedAll(selected: Boolean)

    suspend fun remove(id: Long)

    suspend fun clear()
}