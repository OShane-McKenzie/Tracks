package com.litecodez.tracksc.models

data class ImageActionModel(
    var doUpload: Boolean = false,
    var bucket: String = "",
    var name: String = ""
)
