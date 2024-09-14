package com.example.util.simpletimetracker.core.interactor

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompleteTypesStateInteractor @Inject constructor() {

    var widgetTypeIds: Set<Long> = emptySet()
    var notificationTypeIds: Set<Long> = emptySet()
}