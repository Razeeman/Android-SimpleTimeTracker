package com.example.util.simpletimetracker.feature_dialogs.cardSize.adapter

import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderAdapterDelegate

class CardSizeAdapter : BaseRecyclerAdapter() {

    init {
        delegates[ViewHolderType.RECORD_TYPE] = CardSizeAdapterDelegate()
        delegates[ViewHolderType.LOADER] = LoaderAdapterDelegate()
    }
}