package com.litecodez.tracksc.models

import kotlinx.serialization.Serializable

@Serializable
data class LocalImages(
    var images: MutableMap<String, LocalImage> = mutableMapOf()
)