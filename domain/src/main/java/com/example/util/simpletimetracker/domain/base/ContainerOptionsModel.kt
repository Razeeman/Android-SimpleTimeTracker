package com.example.util.simpletimetracker.domain.base

sealed interface ContainerOptionsModel {

    sealed interface Records : ContainerOptionsModel {
        data object CalendarView : Records
        data object Filter : Records
        data object Share : Records
        data object BackToToday : Records
        data object SelectDate : Records
    }

    sealed interface Statistics : ContainerOptionsModel {
        data object Filter : Statistics
        data object Share : Statistics
        data object BackToToday : Statistics
        data object SelectDate : Statistics
        data object SelectRange : Statistics
    }

    sealed interface DetailedStatistics : ContainerOptionsModel {
        data object Compare : DetailedStatistics
        data object Filter : DetailedStatistics
        data object SelectDate : DetailedStatistics
        data object SelectRange : DetailedStatistics
        data object BackToToday : DetailedStatistics
    }
}