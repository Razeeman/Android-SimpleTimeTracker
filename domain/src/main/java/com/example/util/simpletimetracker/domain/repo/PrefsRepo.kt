package com.example.util.simpletimetracker.domain.repo

interface PrefsRepo {

    var recordTypesFilteredOnChart: Set<String>

    var sortRecordTypesByColor: Boolean
}