package com.example.util.simpletimetracker.feature_change_record_type.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModel
import com.example.util.simpletimetracker.feature_change_record_type.viewModel.ChangeRecordTypeViewModelFactory
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import kotlinx.android.synthetic.main.change_record_type_fragment.*

class ChangeRecordTypeFragment : Fragment() {

    private val viewModel: ChangeRecordTypeViewModel by viewModels(
        factoryProducer = {
            ChangeRecordTypeViewModelFactory(
                arguments?.getString(ARGS_RECORD_NAME).orEmpty()
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.change_record_type_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity?.application as ChangeRecordTypeComponentProvider)
            .changeRecordTypeComponent?.inject(viewModel)

        viewModel.recordType.observe(viewLifecycleOwner) {
            updateUi(it)
        }

        etChangeRecordTypeName.doAfterTextChanged {
            viewModel.onNameChange(it.toString())
        }

        btnChangeRecordTypeSave.setOnClickListener {
            viewModel.onSaveClick()
        }
    }

    private fun updateUi(item: ChangeRecordTypeViewData) {
        etChangeRecordTypeName.setText(item.name)
        etChangeRecordTypeName.setSelection(item.name.length)
    }

    companion object {
        private const val ARGS_RECORD_NAME = "name"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordTypeParams -> putString(ARGS_RECORD_NAME, data.name)
            }
        }
    }
}