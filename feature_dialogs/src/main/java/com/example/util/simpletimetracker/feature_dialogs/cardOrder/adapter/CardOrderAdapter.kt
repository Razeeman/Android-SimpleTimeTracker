package com.example.util.simpletimetracker.feature_dialogs.cardOrder.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate

class CardOrderAdapter : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = CardOrderAdapterDelegate()
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}