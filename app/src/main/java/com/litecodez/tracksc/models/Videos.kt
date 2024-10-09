package com.litecodez.tracksc.models

import com.google.gson.annotations.SerializedName

data class Videos(
    @SerializedName("videos") @JvmField var videos: List<Video> = listOf(
        Video(
            id = "_t0qtSKOpO4",
            title = "Chris Brown - Sensational ft. Davido Lojay",
            genre = "Hip Hop/R&B",
            artist = "Chris Brown",
        )
    )
)
