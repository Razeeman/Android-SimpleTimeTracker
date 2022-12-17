package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.RecordTypeDBO
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class RecordTypeDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeDBO): RecordType {
        return RecordType(
            id = dbo.id,
            name = dbo.name,
            icon = dbo.icon,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt
            ),
            hidden = dbo.hidden,
            goalTime = dbo.goalTime,
            dailyGoalTime = dbo.dailyGoalTime,
            weeklyGoalTime = dbo.weeklyGoalTime,
        )
    }

    fun map(domain: RecordType): RecordTypeDBO {
        return RecordTypeDBO(
            id = domain.id,
            name = domain.name,
            icon = domain.icon,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            hidden = domain.hidden,
            goalTime = domain.goalTime,
            dailyGoalTime = domain.dailyGoalTime,
            weeklyGoalTime = domain.weeklyGoalTime,
        )
    }
}