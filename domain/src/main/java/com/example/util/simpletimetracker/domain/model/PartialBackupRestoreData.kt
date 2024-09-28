package com.example.util.simpletimetracker.domain.model

// TODO switch to LongObjectMap from androidx.collections
data class PartialBackupRestoreData(
    val types: Map<Long, RecordType>,
    val records: Map<Long, Record>,
    val categories: Map<Long, Category>,
    val typeToCategory: List<RecordTypeCategory>,
    val tags: Map<Long, RecordTag>,
    val recordToTag: List<RecordToRecordTag>,
    val typeToTag: List<RecordTypeToTag>,
    val typeToDefaultTag: List<RecordTypeToDefaultTag>,
    val activityFilters: Map<Long, ActivityFilter>,
    val favouriteComments: Map<Long, FavouriteComment>,
    val favouriteIcon: Map<Long, FavouriteIcon>,
    val goals: Map<Long, RecordTypeGoal>,
    val rules: Map<Long, ComplexRule>,
)