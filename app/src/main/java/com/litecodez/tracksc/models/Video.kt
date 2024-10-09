package com.litecodez.tracksc.models

import com.google.gson.annotations.SerializedName
import com.litecodez.tracksc.objects.HasId
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    @SerializedName("id") @JvmField var id:String = "",
    @SerializedName("title") @JvmField var title:String = "",
    @SerializedName("genre") @JvmField var genre:String = "",
    @SerializedName("artist") @JvmField var artist:String = ""
)
