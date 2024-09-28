package com.litecodez.tracksc.models

import com.litecodez.tracksc.baseApi

data class ApiModel(
    var base:String = baseApi,
    var endPoint:String = "",
    var params:List<String> = listOf()
)