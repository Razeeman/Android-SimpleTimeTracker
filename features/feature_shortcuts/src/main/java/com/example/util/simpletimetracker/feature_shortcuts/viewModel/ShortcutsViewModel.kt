package com.example.util.simpletimetracker.feature_shortcuts.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_shortcuts.interactor.ShortcutsViewDataInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.ShortcutsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_shortcuts.viewData.ShortcutsButtonViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeShortcutParams
import com.example.util.simpletimetracker.core.extension.toPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutsViewModel @Inject constructor(
    private val router: Router,
    private val shortcutsViewDataInteractor: ShortcutsViewDataInteractor,
    private val shortcutsDataUpdateInteractor: ShortcutsDataUpdateInteractor,
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val prefsInteractor: PrefsInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { updateViewData() }
    }

    private var navBarHeightDp: Int = 0
    private var shortcutOrder: List<Long>? = null
    private var loadJob: Job? = null
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            shortcutsDataUpdateInteractor.dataUpdated.collect {
                updateViewData()
            }
        }
    }

    fun onChangeInsets(navBarHeight: Int) {
        if (navBarHeightDp != navBarHeight) {
            navBarHeightDp = navBarHeight
            updateViewData()
        }
    }

    fun onItemButtonClick(viewData: ButtonViewData) {
        val id = viewData.id as? ShortcutsButtonViewData ?: return
        when (id.block) {
            ShortcutsButtonViewData.Block.ADD -> {
                router.navigate(data = ChangeShortcutParams.New)
            }
        }
    }

    fun onShortcutClick(
        item: RecordShortcutViewData,
        sharedElements: Pair<Any, String>? = null,
    ) {
        router.navigate(
            data = ChangeShortcutParams.Change(
                id = item.id,
                transitionName = sharedElements?.second.orEmpty(),
                preview = item.toPreview(),
            ),
            sharedElements = sharedElements?.let { mapOf(it) },
        )
    }

    fun onItemMoved(items: List<ViewHolderType>) {
        // Prevent an ongoing load with an older order from overwriting the dragged list.
        loadJob?.cancel()
        shortcutOrder = items.filterIsInstance<RecordShortcutViewData>().map { it.id }
        viewData.set(items)
    }

    fun onDragEnded() {
        val order = shortcutOrder ?: return
        saveOrder(order)
    }

    private fun saveOrder(order: List<Long>) {
        // TODO add an options button with option to reset default order
        val previousSave = saveJob
        saveJob = viewModelScope.launch {
            // Wait for the previous save so an older drag cannot overwrite the latest order.
            previousSave?.join()
            setOrder(order)
            updateViewData()
        }
    }

    private suspend fun setOrder(shortcutIds: List<Long>) {
        val existingIds = recordShortcutInteractor.getAll().map { it.id }.toSet()
        val order = shortcutIds
            .distinct()
            .filter { it in existingIds }
            .mapIndexed { index, id -> id to index.toLong() }
            .toMap()
        prefsInteractor.setShortcutOrderManual(order)
    }

    private fun updateViewData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // Apply the latest in-memory order even if this load began before a drag or save.
            val data = shortcutsViewDataInteractor.getViewData(
                navBarHeightDp = navBarHeightDp,
                shortcutOrder = shortcutOrder,
            )
            delayLoad()
            viewData.set(data)
        }
    }
}
