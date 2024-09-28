package com.example.util.simpletimetracker.data_local.resolver

import android.content.ContentResolver
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.annotation.StringRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.data_local.mapper.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.ClearDataInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeToDefaultTag
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Do not change backup parts order, always add new to the end.
 * Otherwise previous version's backups will be broken.
 */
@Singleton
class BackupRepoImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTagRepo: RecordTagRepo,
    private val activityFilterRepo: ActivityFilterRepo,
    private val favouriteCommentRepo: FavouriteCommentRepo,
    private val favouriteIconRepo: FavouriteIconRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val complexRuleRepo: ComplexRuleRepo,
    private val clearDataInteractor: ClearDataInteractor,
    private val resourceRepo: ResourceRepo,
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
    private val backupPrefsRepo: BackupPrefsRepo,
) : BackupRepo {

    override suspend fun saveBackupFile(
        uriString: String,
        params: BackupOptionsData.Save,
    ): ResultCode = withContext(Dispatchers.IO) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var fileOutputStream: BufferedOutputStream? = null

        try {
            val uri = Uri.parse(uriString)
            fileDescriptor = contentResolver.openFileDescriptor(uri, "wt")
            fileOutputStream = fileDescriptor?.fileDescriptor
                ?.let(::FileOutputStream)?.buffered()

            // Write file identification
            val identificationBackupRow: String = BACKUP_IDENTIFICATION + "\n"
            fileOutputStream?.write(identificationBackupRow.toByteArray())

            // Options
            val saveRecords: Boolean = when (params) {
                is BackupOptionsData.Save.Standard -> true
                is BackupOptionsData.Save.SaveWithoutRecords -> false
            }

            // Write data
            recordTypeRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            if (saveRecords) {
                recordRepo.getAll().forEach {
                    fileOutputStream?.write(it.let(::toBackupString).toByteArray())
                }
            }
            categoryRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            recordTypeCategoryRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            recordTagRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            if (saveRecords) {
                recordToRecordTagRepo.getAll().forEach {
                    fileOutputStream?.write(it.let(::toBackupString).toByteArray())
                }
            }
            recordTypeToTagRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            recordTypeToDefaultTagRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            activityFilterRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            favouriteCommentRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            favouriteIconRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            recordTypeGoalRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            complexRuleRepo.getAll().forEach {
                fileOutputStream?.write(it.let(::toBackupString).toByteArray())
            }
            backupPrefsRepo.saveToBackupString().let {
                fileOutputStream?.write(it.toByteArray())
            }

            fileOutputStream?.close()
            fileDescriptor?.close()
            ResultCode.Success(resourceRepo.getString(R.string.message_backup_saved))
        } catch (e: Exception) {
            Timber.e(e)
            ResultCode.Error(resourceRepo.getString(R.string.message_save_error))
        } finally {
            try {
                fileOutputStream?.close()
                fileDescriptor?.close()
            } catch (e: IOException) {
                // Do nothing
            }
        }
    }

    override suspend fun restoreBackupFile(
        uriString: String,
        params: BackupOptionsData.Restore,
    ): ResultCode = withContext(Dispatchers.IO) {
        // Options
        val restoreSettings = when (params) {
            is BackupOptionsData.Restore.Standard -> false
            is BackupOptionsData.Restore.WithSettings -> true
        }

        readBackup(
            uriString = uriString,
            successCodeMessage = R.string.message_backup_restored,
            errorCodeMessage = R.string.message_restore_error,
            clearData = true,
            migrateTags = {
                migrateTags(
                    types = recordTypeRepo.getAll(),
                    data = it,
                ).forEach { updatedTag ->
                    recordTagRepo.add(updatedTag)
                }
            },
            dataHandler = DataHandler(
                types = recordTypeRepo::add,
                records = recordRepo::add,
                categories = categoryRepo::add,
                typeToCategory = recordTypeCategoryRepo::add,
                tags = recordTagRepo::add,
                recordToTag = recordToRecordTagRepo::add,
                typeToTag = recordTypeToTagRepo::add,
                typeToDefaultTag = recordTypeToDefaultTagRepo::add,
                activityFilters = activityFilterRepo::add,
                favouriteComments = favouriteCommentRepo::add,
                favouriteIcon = favouriteIconRepo::add,
                goals = recordTypeGoalRepo::add,
                rules = complexRuleRepo::add,
                settings = { if (restoreSettings) backupPrefsRepo.restoreFromBackupString(it) },
            ),
        )
    }

    // Replace original id with 0 to add instead of replacing.
    // Replace original ids in other data with actual id after adding.
    override suspend fun partialRestoreBackupFile(
        params: BackupOptionsData.Custom,
    ): ResultCode = withContext(Dispatchers.IO) {
        val originalTypeIdToAddedId: MutableMap<Long, Long> = mutableMapOf()
        val originalCategoryIdToAddedId: MutableMap<Long, Long> = mutableMapOf()
        val originalTagIdToAddedId: MutableMap<Long, Long> = mutableMapOf()
        val originalRecordIdToAddedId: MutableMap<Long, Long> = mutableMapOf()

        params.data.types.values.forEach { type ->
            val originalId = type.id
            val addedId = type.copy(
                id = 0,
            ).let { recordTypeRepo.add(it) }
            originalTypeIdToAddedId[originalId] = addedId
        }
        params.data.records.values.forEach { record ->
            val originalId = record.id
            val newTypeId = originalTypeIdToAddedId[record.typeId]
                ?: return@forEach
            val addedId = record.copy(
                id = 0,
                typeId = newTypeId,
            ).let { recordRepo.add(it) }
            originalRecordIdToAddedId[originalId] = addedId
        }
        params.data.categories.values.forEach {
            val originalId = it.id
            val addedId = it.copy(
                id = 0,
            ).let { categoryRepo.add(it) }
            originalCategoryIdToAddedId[originalId] = addedId
        }
        params.data.typeToCategory.forEach { typeToCategory ->
            val newTypeId = originalTypeIdToAddedId[typeToCategory.recordTypeId]
                ?: return@forEach
            val newCategoryId = originalCategoryIdToAddedId[typeToCategory.categoryId]
                ?: return@forEach
            typeToCategory.copy(
                recordTypeId = newTypeId,
                categoryId = newCategoryId,
            ).let { recordTypeCategoryRepo.add(it) }
        }
        params.data.tags.values.forEach { tag ->
            val originalId = tag.id
            val newColorSource = originalTypeIdToAddedId[tag.iconColorSource].orZero()
            val addedId = tag.copy(
                id = 0,
                iconColorSource = newColorSource,
            ).let { recordTagRepo.add(it) }
            originalTagIdToAddedId[originalId] = addedId
        }
        params.data.recordToTag.forEach { recordToTag ->
            val newRecordId = originalRecordIdToAddedId[recordToTag.recordId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[recordToTag.recordTagId]
                ?: return@forEach
            recordToTag.copy(
                recordId = newRecordId,
                recordTagId = newTagId,
            ).let { recordToRecordTagRepo.add(it) }
        }
        params.data.typeToTag.forEach { typeToTag ->
            val newTypeId = originalTypeIdToAddedId[typeToTag.recordTypeId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[typeToTag.tagId]
                ?: return@forEach
            typeToTag.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            ).let { recordTypeToTagRepo.add(it) }
        }
        params.data.typeToDefaultTag.forEach { typeToDefaultTag ->
            val newTypeId = originalTypeIdToAddedId[typeToDefaultTag.recordTypeId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[typeToDefaultTag.tagId]
                ?: return@forEach
            typeToDefaultTag.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            ).let { recordTypeToDefaultTagRepo.add(it) }
        }
        params.data.activityFilters.values.forEach { activityFilter ->
            val newTypeIds = activityFilter.selectedIds
                .mapNotNull { originalTypeIdToAddedId[it] }
            activityFilter.copy(
                id = 0,
                selectedIds = newTypeIds,
            ).let { activityFilterRepo.add(it) }
        }
        params.data.favouriteComments.values.forEach { favComment ->
            favComment.copy(
                id = 0,
            ).let { favouriteCommentRepo.add(it) }
        }
        params.data.favouriteIcon.values.forEach { favIcon ->
            favIcon.copy(
                id = 0,
            ).let { favouriteIconRepo.add(it) }
        }
        params.data.goals.values.forEach { goal ->
            val newId = when (val idData = goal.idData) {
                is RecordTypeGoal.IdData.Type -> originalTypeIdToAddedId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Type)
                is RecordTypeGoal.IdData.Category -> originalCategoryIdToAddedId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Category)
            } ?: return@forEach
            goal.copy(
                id = 0,
                idData = newId,
            ).let { recordTypeGoalRepo.add(it) }
        }
        params.data.rules.values.forEach { rule ->
            val newStartingTypeIds = rule.conditionStartingTypeIds
                .mapNotNull { originalTypeIdToAddedId[it] }.toSet()
            val newCurrentTypeIds = rule.conditionCurrentTypeIds
                .mapNotNull { originalTypeIdToAddedId[it] }.toSet()
            val newAssignTagIds = rule.actionAssignTagIds
                .mapNotNull { originalTagIdToAddedId[it] }.toSet()
            rule.copy(
                id = 0,
                actionAssignTagIds = newAssignTagIds,
                conditionStartingTypeIds = newStartingTypeIds,
                conditionCurrentTypeIds = newCurrentTypeIds,
            ).let { complexRuleRepo.add(it) }
        }
        return@withContext ResultCode.Success(
            resourceRepo.getString(R.string.message_backup_restored),
        )
    }

    override suspend fun readBackupFile(
        uriString: String,
    ): Pair<ResultCode, PartialBackupRestoreData?> = withContext(Dispatchers.IO) {
        // Result data
        val types: MutableList<RecordType> = mutableListOf()
        val records: MutableList<Record> = mutableListOf()
        val categories: MutableList<Category> = mutableListOf()
        val typeToCategory: MutableList<RecordTypeCategory> = mutableListOf()
        val tags: MutableList<RecordTag> = mutableListOf()
        val recordToTag: MutableList<RecordToRecordTag> = mutableListOf()
        val typeToTag: MutableList<RecordTypeToTag> = mutableListOf()
        val typeToDefaultTag: MutableList<RecordTypeToDefaultTag> = mutableListOf()
        val activityFilters: MutableList<ActivityFilter> = mutableListOf()
        val favouriteComments: MutableList<FavouriteComment> = mutableListOf()
        val favouriteIcon: MutableList<FavouriteIcon> = mutableListOf()
        val goals: MutableList<RecordTypeGoal> = mutableListOf()
        val rules: MutableList<ComplexRule> = mutableListOf()
        val settings: MutableList<List<String>> = mutableListOf()

        val result = readBackup(
            uriString = uriString,
            successCodeMessage = null,
            errorCodeMessage = R.string.settings_file_open_error,
            clearData = false,
            migrateTags = {
                tags += migrateTags(
                    types = types,
                    data = it,
                )
            },
            dataHandler = DataHandler(
                types = types::add,
                records = records::add,
                categories = categories::add,
                typeToCategory = typeToCategory::add,
                tags = tags::add,
                recordToTag = recordToTag::add,
                typeToTag = typeToTag::add,
                typeToDefaultTag = typeToDefaultTag::add,
                activityFilters = activityFilters::add,
                favouriteComments = favouriteComments::add,
                favouriteIcon = favouriteIcon::add,
                goals = goals::add,
                rules = rules::add,
                settings = settings::add,
            ),
        )

        val recordToTagIds = recordToTag.groupBy { it.recordId }
        val processedRecords = records.map {
            val thisTags = recordToTagIds[it.id].orEmpty()
            it.copy(tagIds = thisTags.map(RecordToRecordTag::recordTagId))
        }

        result to PartialBackupRestoreData(
            types = types.associateBy { it.id },
            records = processedRecords.associateBy { it.id },
            categories = categories.associateBy { it.id },
            typeToCategory = typeToCategory,
            tags = tags.associateBy { it.id },
            recordToTag = recordToTag,
            typeToTag = typeToTag,
            typeToDefaultTag = typeToDefaultTag,
            activityFilters = activityFilters.associateBy { it.id },
            favouriteComments = favouriteComments.associateBy { it.id },
            favouriteIcon = favouriteIcon.associateBy { it.id },
            goals = goals.associateBy { it.id },
            rules = rules.associateBy { it.id },
        )
    }

    private suspend fun readBackup(
        uriString: String,
        @StringRes successCodeMessage: Int?,
        @StringRes errorCodeMessage: Int,
        clearData: Boolean,
        migrateTags: suspend (MutableList<Pair<RecordTag, Long>>) -> Unit,
        dataHandler: DataHandler,
    ): ResultCode = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var reader: BufferedReader? = null
        val errorCode = ResultCode.Error(resourceRepo.getString(errorCodeMessage))

        try {
            val uri = Uri.parse(uriString)
            inputStream = contentResolver.openInputStream(uri)
            reader = inputStream?.let(::InputStreamReader)?.let(::BufferedReader)

            var line: String
            var parts: List<String>

            // Check file identification
            line = reader?.readLine().orEmpty()
            if (line != BACKUP_IDENTIFICATION) return@withContext errorCode

            if (clearData) clearDataInteractor.execute()

            // List of tag to related type id.
            val tagsMigrationData: MutableList<Pair<RecordTag, Long>> = mutableListOf()

            // Read data
            while (reader?.readLine()?.also { line = it } != null) {
                parts = line.split("\t")
                when (parts[0]) {
                    ROW_RECORD_TYPE -> {
                        recordTypeFromBackupString(parts).let { (type, goals) ->
                            dataHandler.types.invoke(type)
                            goals.forEach { dataHandler.goals.invoke(it) }
                        }
                    }

                    ROW_RECORD -> {
                        recordFromBackupString(parts).let { (record, recordToRecordTag) ->
                            dataHandler.records.invoke(record)
                            if (recordToRecordTag != null) {
                                dataHandler.recordToTag.invoke(recordToRecordTag)
                            }
                        }
                    }

                    ROW_CATEGORY -> {
                        categoryFromBackupString(parts).let {
                            dataHandler.categories.invoke(it)
                        }
                    }

                    ROW_TYPE_CATEGORY -> {
                        typeCategoryFromBackupString(parts).let {
                            dataHandler.typeToCategory.invoke(it)
                        }
                    }

                    ROW_RECORD_TAG -> {
                        recordTagFromBackupString(parts).let { (tag, typeId) ->
                            dataHandler.tags.invoke(tag)
                            if (typeId != null) {
                                val typeToTag = RecordTypeToTag(
                                    recordTypeId = typeId,
                                    tagId = tag.id,
                                )
                                dataHandler.typeToTag.invoke(typeToTag)
                                tagsMigrationData.add(tag to typeId)
                            }
                        }
                    }

                    ROW_RECORD_TO_RECORD_TAG -> {
                        recordToRecordTagFromBackupString(parts).let {
                            dataHandler.recordToTag.invoke(it)
                        }
                    }

                    ROW_TYPE_TO_RECORD_TAG -> {
                        typeToRecordTagFromBackupString(parts).let {
                            dataHandler.typeToTag.invoke(it)
                        }
                    }

                    ROW_TYPE_TO_DEFAULT_TAG -> {
                        typeToDefaultTagFromBackupString(parts).let {
                            dataHandler.typeToDefaultTag.invoke(it)
                        }
                    }

                    ROW_ACTIVITY_FILTER -> {
                        activityFilterFromBackupString(parts).let {
                            dataHandler.activityFilters.invoke(it)
                        }
                    }

                    ROW_FAVOURITE_COMMENT -> {
                        favouriteCommentFromBackupString(parts)?.let {
                            dataHandler.favouriteComments.invoke(it)
                        }
                    }

                    ROW_FAVOURITE_ICON -> {
                        favouriteIconFromBackupString(parts)?.let {
                            dataHandler.favouriteIcon.invoke(it)
                        }
                    }

                    ROW_RECORD_TYPE_GOAL -> {
                        recordTypeGoalFromBackupString(parts).let {
                            dataHandler.goals.invoke(it)
                        }
                    }

                    ROW_COMPLEX_RULE -> {
                        complexRuleFromBackupString(parts).let {
                            dataHandler.rules.invoke(it)
                        }
                    }

                    BackupPrefsRepo.PREFS_KEY -> {
                        dataHandler.settings.invoke(parts)
                    }
                }
            }
            migrateTags(tagsMigrationData)
            ResultCode.Success(successCodeMessage?.let(resourceRepo::getString))
        } catch (e: Exception) {
            Timber.e(e)
            errorCode
        } finally {
            try {
                inputStream?.close()
                reader?.close()
            } catch (e: IOException) {
                // Do nothing
            }
        }
    }

    private fun toBackupString(recordType: RecordType): String {
        return String.format(
            "$ROW_RECORD_TYPE\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            recordType.id.toString(),
            recordType.name.clean(),
            recordType.icon,
            recordType.color.colorId.toString(),
            (if (recordType.hidden) 1 else 0).toString(),
            "", // goal time is moved to separate table
            recordType.color.colorInt,
            "", // daily goal time is moved to separate table
            "", // weekly goal time is moved to separate table
            "", // monthly goal time is moved to separate table,
            recordType.defaultDuration,
            recordType.note.cleanTabs().replaceNewline(),
        )
    }

    private fun toBackupString(record: Record): String {
        return String.format(
            "$ROW_RECORD\t%s\t%s\t%s\t%s\t%s\t%s\n",
            record.id.toString(),
            record.typeId.toString(),
            record.timeStarted.toString(),
            record.timeEnded.toString(),
            record.comment.cleanTabs().replaceNewline(),
            "", // record tag id is removed from record dbo
        )
    }

    private fun toBackupString(category: Category): String {
        return String.format(
            "$ROW_CATEGORY\t%s\t%s\t%s\t%s\t%s\n",
            category.id.toString(),
            category.name.clean(),
            category.color.colorId.toString(),
            category.color.colorInt,
            category.note.cleanTabs().replaceNewline(),
        )
    }

    private fun toBackupString(recordTypeCategory: RecordTypeCategory): String {
        return String.format(
            "$ROW_TYPE_CATEGORY\t%s\t%s\n",
            recordTypeCategory.recordTypeId.toString(),
            recordTypeCategory.categoryId.toString(),
        )
    }

    private fun toBackupString(recordTag: RecordTag): String {
        return String.format(
            "$ROW_RECORD_TAG\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            recordTag.id.toString(),
            "", // type relation is removed from tag dbo
            recordTag.name.clean(),
            (if (recordTag.archived) 1 else 0).toString(),
            recordTag.color.colorId.toString(),
            recordTag.color.colorInt,
            recordTag.icon,
            recordTag.iconColorSource,
            recordTag.note.cleanTabs().replaceNewline(),
        )
    }

    private fun toBackupString(recordToRecordTag: RecordToRecordTag): String {
        return String.format(
            "$ROW_RECORD_TO_RECORD_TAG\t%s\t%s\n",
            recordToRecordTag.recordId.toString(),
            recordToRecordTag.recordTagId.toString(),
        )
    }

    private fun toBackupString(typeToTag: RecordTypeToTag): String {
        return String.format(
            "$ROW_TYPE_TO_RECORD_TAG\t%s\t%s\n",
            typeToTag.recordTypeId.toString(),
            typeToTag.tagId.toString(),
        )
    }

    private fun toBackupString(typeToDefaultTag: RecordTypeToDefaultTag): String {
        return String.format(
            "$ROW_TYPE_TO_DEFAULT_TAG\t%s\t%s\n",
            typeToDefaultTag.recordTypeId.toString(),
            typeToDefaultTag.tagId.toString(),
        )
    }

    private fun toBackupString(activityFilter: ActivityFilter): String {
        val typeString = when (activityFilter.type) {
            is ActivityFilter.Type.Activity -> 0L
            is ActivityFilter.Type.Category -> 1L
        }.toString()

        return String.format(
            "$ROW_ACTIVITY_FILTER\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            activityFilter.id.toString(),
            activityFilter.selectedIds.joinToString(separator = ","),
            typeString,
            activityFilter.name.clean(),
            activityFilter.color.colorId.toString(),
            activityFilter.color.colorInt,
            (if (activityFilter.selected) 1 else 0).toString(),
        )
    }

    private fun toBackupString(favouriteComment: FavouriteComment): String {
        return String.format(
            "$ROW_FAVOURITE_COMMENT\t%s\t%s\n",
            favouriteComment.id.toString(),
            favouriteComment.comment.cleanTabs().replaceNewline(),
        )
    }

    private fun toBackupString(favouriteIcon: FavouriteIcon): String {
        return String.format(
            "$ROW_FAVOURITE_ICON\t%s\t%s\n",
            favouriteIcon.id.toString(),
            favouriteIcon.icon,
        )
    }

    private fun toBackupString(recordTypeGoal: RecordTypeGoal): String {
        val rangeString = when (recordTypeGoal.range) {
            is RecordTypeGoal.Range.Session -> 0L
            is RecordTypeGoal.Range.Daily -> 1L
            is RecordTypeGoal.Range.Weekly -> 2L
            is RecordTypeGoal.Range.Monthly -> 3L
        }.toString()
        val typeString = when (recordTypeGoal.type) {
            is RecordTypeGoal.Type.Duration -> 0L
            is RecordTypeGoal.Type.Count -> 1L
        }.toString()
        val daysOfWeekString = daysOfWeekDataLocalMapper
            .mapDaysOfWeek(recordTypeGoal.daysOfWeek)

        return String.format(
            "$ROW_RECORD_TYPE_GOAL\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            recordTypeGoal.id.toString(),
            (recordTypeGoal.idData as? RecordTypeGoal.IdData.Type)?.value.orZero(),
            rangeString,
            typeString,
            recordTypeGoal.type.value.toString(),
            (recordTypeGoal.idData as? RecordTypeGoal.IdData.Category)?.value.orZero(),
            daysOfWeekString,
        )
    }

    private fun toBackupString(complexRule: ComplexRule): String {
        val actionString = when (complexRule.action) {
            is ComplexRule.Action.AllowMultitasking -> 0L
            is ComplexRule.Action.DisallowMultitasking -> 1L
            is ComplexRule.Action.AssignTag -> 2L
        }.toString()
        val daysOfWeekString = daysOfWeekDataLocalMapper
            .mapDaysOfWeek(complexRule.conditionDaysOfWeek.toList())

        return String.format(
            "$ROW_COMPLEX_RULE\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
            complexRule.id.toString(),
            (if (complexRule.disabled) 1 else 0).toString(),
            actionString,
            complexRule.actionAssignTagIds.joinToString(separator = ","),
            complexRule.conditionStartingTypeIds.joinToString(separator = ","),
            complexRule.conditionCurrentTypeIds.joinToString(separator = ","),
            daysOfWeekString,
        )
    }

    private fun recordTypeFromBackupString(parts: List<String>): Pair<RecordType, List<RecordTypeGoal>> {
        val typeId = parts.getOrNull(1)?.toLongOrNull().orZero()

        // goal times are moved to separate database, need to support old backup files.
        val goalTime = parts.getOrNull(6)?.toLongOrNull().orZero()
        val dailyGoalTime = parts.getOrNull(8)?.toLongOrNull().orZero()
        val weeklyGoalTime = parts.getOrNull(9)?.toLongOrNull().orZero()
        val monthlyGoalTime = parts.getOrNull(10)?.toLongOrNull().orZero()
        // Didn't exist when goal time was in type db, no need to migrate.
        val daysOfWeek = DayOfWeek.values().toList()

        val goalTimes = mutableListOf<RecordTypeGoal>().apply {
            if (goalTime != 0L) {
                RecordTypeGoal(
                    // Didn't exist when goal time was in type db, no need to migrate.
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    range = RecordTypeGoal.Range.Session,
                    type = RecordTypeGoal.Type.Duration(goalTime),
                    daysOfWeek = daysOfWeek,
                ).let(::add)
            }
            if (dailyGoalTime != 0L) {
                RecordTypeGoal(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    range = RecordTypeGoal.Range.Daily,
                    type = RecordTypeGoal.Type.Duration(dailyGoalTime),
                    daysOfWeek = daysOfWeek,
                ).let(::add)
            }
            if (weeklyGoalTime != 0L) {
                RecordTypeGoal(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    range = RecordTypeGoal.Range.Weekly,
                    type = RecordTypeGoal.Type.Duration(weeklyGoalTime),
                    daysOfWeek = daysOfWeek,
                ).let(::add)
            }
            if (monthlyGoalTime != 0L) {
                RecordTypeGoal(
                    idData = RecordTypeGoal.IdData.Type(typeId),
                    range = RecordTypeGoal.Range.Monthly,
                    type = RecordTypeGoal.Type.Duration(monthlyGoalTime),
                    daysOfWeek = daysOfWeek,
                ).let(::add)
            }
        }

        return RecordType(
            id = typeId,
            name = parts.getOrNull(2).orEmpty(),
            icon = parts.getOrNull(3).orEmpty(),
            color = AppColor(
                colorId = parts.getOrNull(4)?.toIntOrNull().orZero(),
                colorInt = parts.getOrNull(7).orEmpty(),
            ),
            hidden = parts.getOrNull(5)?.toIntOrNull() == 1,
            // parts[6] - goal time is moved to separate table
            // parts[8] - daily time is moved to separate table
            // parts[9] - weekly time is moved to separate table
            // parts[10] - monthly time is moved to separate table,
            defaultDuration = parts.getOrNull(11)?.toLongOrNull().orZero(),
            note = parts.getOrNull(12)?.restoreNewline().orEmpty(),
        ) to goalTimes
    }

    private fun recordFromBackupString(parts: List<String>): Pair<Record, RecordToRecordTag?> {
        val recordId = parts.getOrNull(1)?.toLongOrNull().orZero()
        // tag id is removed from record dbo, need to support old backup files.
        val tagId = parts.getOrNull(6)?.toLongOrNull().orZero()

        return Record(
            id = recordId,
            typeId = parts.getOrNull(2)?.toLongOrNull() ?: 1L,
            timeStarted = parts.getOrNull(3)?.toLongOrNull().orZero(),
            timeEnded = parts.getOrNull(4)?.toLongOrNull().orZero(),
            comment = parts.getOrNull(5)?.restoreNewline().orEmpty(),
            // parts[6] - tag id is removed from record dbo.
        ) to RecordToRecordTag(
            recordId = recordId,
            recordTagId = tagId,
        ).takeUnless { tagId == 0L }
    }

    private fun categoryFromBackupString(parts: List<String>): Category {
        return Category(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            name = parts.getOrNull(2).orEmpty(),
            color = AppColor(
                colorId = parts.getOrNull(3)?.toIntOrNull().orZero(),
                colorInt = parts.getOrNull(4).orEmpty(),
            ),
            note = parts.getOrNull(5)?.restoreNewline().orEmpty(),
        )
    }

    private fun typeCategoryFromBackupString(parts: List<String>): RecordTypeCategory {
        return RecordTypeCategory(
            recordTypeId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            categoryId = parts.getOrNull(2)?.toLongOrNull().orZero(),
        )
    }

    private fun recordTagFromBackupString(parts: List<String>): Pair<RecordTag, Long?> {
        // type id is removed from tag dbo, need to support old backup files.
        val typeId = parts.getOrNull(2)?.toLongOrNull().orZero()

        return RecordTag(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            // parts[2] - type id is removed from tag dbo.
            name = parts.getOrNull(3).orEmpty(),
            archived = parts.getOrNull(4)?.toIntOrNull() == 1,
            color = AppColor(
                colorId = parts.getOrNull(5)?.toIntOrNull().orZero(),
                colorInt = parts.getOrNull(6).orEmpty(),
            ),
            icon = parts.getOrNull(7).orEmpty(),
            iconColorSource = parts.getOrNull(8)?.toLongOrNull().orZero(),
            note = parts.getOrNull(9)?.restoreNewline().orEmpty(),
        ) to typeId.takeUnless { it == 0L }
    }

    private fun recordToRecordTagFromBackupString(parts: List<String>): RecordToRecordTag {
        return RecordToRecordTag(
            recordId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            recordTagId = parts.getOrNull(2)?.toLongOrNull().orZero(),
        )
    }

    private fun typeToRecordTagFromBackupString(parts: List<String>): RecordTypeToTag {
        return RecordTypeToTag(
            recordTypeId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            tagId = parts.getOrNull(2)?.toLongOrNull().orZero(),
        )
    }

    private fun typeToDefaultTagFromBackupString(parts: List<String>): RecordTypeToDefaultTag {
        return RecordTypeToDefaultTag(
            recordTypeId = parts.getOrNull(1)?.toLongOrNull().orZero(),
            tagId = parts.getOrNull(2)?.toLongOrNull().orZero(),
        )
    }

    private fun activityFilterFromBackupString(parts: List<String>): ActivityFilter {
        return ActivityFilter(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            selectedIds = parts.getOrNull(2)?.split(",")
                ?.mapNotNull { it.toLongOrNull() }.orEmpty(),
            type = parts.getOrNull(3)?.toLongOrNull()?.let {
                when (it) {
                    0L -> ActivityFilter.Type.Activity
                    1L -> ActivityFilter.Type.Category
                    else -> ActivityFilter.Type.Activity
                }
            } ?: ActivityFilter.Type.Activity,
            name = parts.getOrNull(4).orEmpty(),
            color = AppColor(
                colorId = parts.getOrNull(5)?.toIntOrNull().orZero(),
                colorInt = parts.getOrNull(6).orEmpty(),
            ),
            selected = parts.getOrNull(7)?.toIntOrNull() == 1,
        )
    }

    private fun favouriteCommentFromBackupString(parts: List<String>): FavouriteComment? {
        return FavouriteComment(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            comment = parts.getOrNull(2)?.restoreNewline()
                ?.takeUnless(String::isEmpty) ?: return null,
        )
    }

    private fun favouriteIconFromBackupString(parts: List<String>): FavouriteIcon? {
        return FavouriteIcon(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            icon = parts.getOrNull(2)
                ?.takeUnless(String::isEmpty) ?: return null,
        )
    }

    private fun recordTypeGoalFromBackupString(parts: List<String>): RecordTypeGoal {
        val typeId = parts.getOrNull(2)?.toLongOrNull().orZero()
        val categoryId = parts.getOrNull(6)?.toLongOrNull().orZero()
        val daysOfWeekString = parts.getOrNull(7).orEmpty()

        return RecordTypeGoal(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            idData = if (typeId != 0L) {
                RecordTypeGoal.IdData.Type(typeId)
            } else {
                RecordTypeGoal.IdData.Category(categoryId)
            },
            range = when (parts.getOrNull(3)?.toLongOrNull()) {
                0L -> RecordTypeGoal.Range.Session
                1L -> RecordTypeGoal.Range.Daily
                2L -> RecordTypeGoal.Range.Weekly
                3L -> RecordTypeGoal.Range.Monthly
                else -> RecordTypeGoal.Range.Session
            },
            type = run {
                val value = parts.getOrNull(5)?.toLongOrNull().orZero()
                when (parts.getOrNull(4)?.toLongOrNull()) {
                    0L -> RecordTypeGoal.Type.Duration(value)
                    1L -> RecordTypeGoal.Type.Count(value)
                    else -> RecordTypeGoal.Type.Duration(value)
                }
            },
            daysOfWeek = daysOfWeekDataLocalMapper.mapDaysOfWeek(daysOfWeekString),
        )
    }

    private fun complexRuleFromBackupString(parts: List<String>): ComplexRule {
        return ComplexRule(
            id = parts.getOrNull(1)?.toLongOrNull().orZero(),
            disabled = parts.getOrNull(2)?.toIntOrNull() == 1,
            action = when (parts.getOrNull(3)?.toLongOrNull()) {
                0L -> ComplexRule.Action.AllowMultitasking
                1L -> ComplexRule.Action.DisallowMultitasking
                2L -> ComplexRule.Action.AssignTag
                else -> ComplexRule.Action.AllowMultitasking
            },
            actionAssignTagIds = parts.getOrNull(4)?.split(",")
                ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet(),
            conditionStartingTypeIds = parts.getOrNull(5)?.split(",")
                ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet(),
            conditionCurrentTypeIds = parts.getOrNull(6)?.split(",")
                ?.mapNotNull { it.toLongOrNull() }.orEmpty().toSet(),
            conditionDaysOfWeek = daysOfWeekDataLocalMapper.mapDaysOfWeek(
                parts.getOrNull(7).orEmpty(),
            ).toSet(),
        )
    }

    private fun migrateTags(
        types: List<RecordType>,
        data: List<Pair<RecordTag, Long>>,
    ): List<RecordTag> {
        if (data.isEmpty()) return emptyList()

        val typesMap = types.associateBy { it.id }
        return data.mapNotNull { (tag, typeId) ->
            val type = typesMap[typeId] ?: return@mapNotNull null
            tag.copy(
                icon = type.icon,
                color = type.color,
                iconColorSource = typeId,
            )
        }
    }

    private fun String.clean() =
        cleanTabs().cleanNewline()

    private fun String.cleanTabs() =
        replace("\t", " ")

    private fun String.cleanNewline() =
        replace("\n", " ")

    private fun String.replaceNewline() =
        replace("\n", "␤")

    private fun String.restoreNewline() =
        replace("␤", "\n")

    private data class DataHandler(
        val types: suspend (RecordType) -> Unit,
        val records: suspend (Record) -> Unit,
        val categories: suspend (Category) -> Unit,
        val typeToCategory: suspend (RecordTypeCategory) -> Unit,
        val tags: suspend (RecordTag) -> Unit,
        val recordToTag: suspend (RecordToRecordTag) -> Unit,
        val typeToTag: suspend (RecordTypeToTag) -> Unit,
        val typeToDefaultTag: suspend (RecordTypeToDefaultTag) -> Unit,
        val activityFilters: suspend (ActivityFilter) -> Unit,
        val favouriteComments: suspend (FavouriteComment) -> Unit,
        val favouriteIcon: suspend (FavouriteIcon) -> Unit,
        val goals: suspend (RecordTypeGoal) -> Unit,
        val rules: suspend (ComplexRule) -> Unit,
        val settings: suspend (List<String>) -> Unit,
    )

    companion object {
        private const val BACKUP_IDENTIFICATION = "app simple time tracker"
        private const val ROW_RECORD_TYPE = "recordType"
        private const val ROW_RECORD = "record"
        private const val ROW_CATEGORY = "category"
        private const val ROW_TYPE_CATEGORY = "typeCategory"
        private const val ROW_RECORD_TAG = "recordTag"
        private const val ROW_RECORD_TO_RECORD_TAG = "recordToRecordTag"
        private const val ROW_TYPE_TO_RECORD_TAG = "typeToRecordTag"
        private const val ROW_TYPE_TO_DEFAULT_TAG = "typeToDefaultTag"
        private const val ROW_ACTIVITY_FILTER = "activityFilter"
        private const val ROW_FAVOURITE_COMMENT = "favouriteComment"
        private const val ROW_FAVOURITE_ICON = "favouriteIcon"
        private const val ROW_RECORD_TYPE_GOAL = "recordTypeGoal"
        private const val ROW_COMPLEX_RULE = "complexRule"
    }
}
