package com.example.util.simpletimetracker.feature_dialogs.emojiSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.mapper.EmojiSelectionMapper
import com.example.util.simpletimetracker.navigation.params.EmojiSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmojiSelectionViewModel @Inject constructor(
    private val mapper: EmojiSelectionMapper,
    private val prefsInteractor: PrefsInteractor
) : ViewModel() {

    lateinit var extra: EmojiSelectionDialogParams

    val icons: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadIconsViewData()
            }
            initial
        }
    }
    val iconSelected: LiveData<String> = MutableLiveData()

    fun onEmojiClick(item: EmojiViewData) {
        iconSelected.set(item.emojiText)
    }

    private suspend fun loadIconsViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return mapper.mapIconEmojiData(
            colorId = extra.color,
            isDarkTheme = isDarkTheme,
            emojiCodes = extra.emojiCodes
        )
    }
}
