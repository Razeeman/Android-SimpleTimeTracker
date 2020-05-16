package com.example.util.simpletimetracker.feature_dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class DateDialogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.date_dialog_fragment,
            container,
            false
        )
    }

    companion object {
        fun newInstance() = DateDialogFragment()
    }
}