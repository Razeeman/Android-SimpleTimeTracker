package com.example.util.simpletimetracker.feature_change_record_tag.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.di.ChangeRecordTagComponentProvider
import com.example.util.simpletimetracker.feature_change_record_tag.viewModel.ChangeRecordTagViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeCategoryParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_tag_fragment.*
import javax.inject.Inject

class ChangeRecordTagFragment : BaseFragment(R.layout.change_record_tag_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRecordTagViewModel>

    private val viewModel: ChangeRecordTagViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptyAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onTypeClick)
        )
    }

    // TODO by delegate?
    private val params: ChangeCategoryParams by lazy {
        arguments?.getParcelable<ChangeCategoryParams>(ARGS_PARAMS) ?: ChangeCategoryParams.New
    }

    override fun initDi() {
        (activity?.application as ChangeRecordTagComponentProvider)
            .changeRecordTagComponent
            ?.inject(this)
    }

    override fun initUi() {
        setPreview()

        // TODO move to utils
        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            previewChangeRecordTag,
            TransitionNames.CATEGORY + params.id
        )

        rvChangeRecordTagType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() {
        etChangeRecordTagName.doAfterTextChanged { viewModel.onNameChange(it.toString()) }
        fieldChangeRecordTagType.setOnClick(viewModel::onTypeChooserClick)
        btnChangeRecordTagSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordTagDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        deleteIconVisibility.observeOnce(viewLifecycleOwner, btnChangeRecordTagDelete::visible::set)
        saveButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordTagSave::setEnabled)
        deleteButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordTagDelete::setEnabled)
        preview.observeOnce(viewLifecycleOwner, ::updateUi)
        preview.observe(viewLifecycleOwner, ::updatePreview)
        types.observe(viewLifecycleOwner, typesAdapter::replace)
        flipTypesChooser.observe(viewLifecycleOwner) { opened ->
            rvChangeRecordTagType.visible = opened
            arrowChangeRecordTagType.apply {
                if (opened) rotateDown() else rotateUp()
            }
        }
        keyboardVisibility.observe(viewLifecycleOwner) { visible ->
            if (visible) showKeyboard(etChangeRecordTagName) else hideKeyboard()
        }
    }

    private fun updateUi(item: CategoryViewData) {
        etChangeRecordTagName.setText(item.name)
        etChangeRecordTagName.setSelection(item.name.length)
    }

    private fun setPreview() = (params as? ChangeCategoryParams.Change)?.preview?.run {
        with(previewChangeRecordTag) {
            itemName = name
            itemColor = color
        }
    }

    private fun updatePreview(item: CategoryViewData) {
        with(previewChangeRecordTag) {
            itemName = item.name
            itemColor = item.color
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeCategoryParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}