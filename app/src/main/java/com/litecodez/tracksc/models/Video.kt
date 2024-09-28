package com.litecodez.tracksc.models

import com.litecodez.tracksc.objects.HasId
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    override var id:String = "",
    var title:String = "",
    var genre:String = ""
):HasId
