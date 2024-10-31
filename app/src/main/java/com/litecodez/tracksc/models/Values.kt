package com.litecodez.tracksc.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Values(
    var version:Double = 0.0,
    var enableAd:Boolean = false
)