package com.litecodez.tracksc.objects

import androidx.compose.runtime.Composable

class DataService {
    private val provider = ContentProvider()
    private val repository = ContentRepository()

    fun provider(): ContentProvider {
        return provider
    }


    fun repository(): ContentRepository {
        return repository
    }

    @Composable
    fun WithContent(data: @Composable (DataService) -> Unit){
        data(this)
    }
}