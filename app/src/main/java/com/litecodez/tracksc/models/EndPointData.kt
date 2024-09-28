package com.litecodez.tracksc.models
import kotlinx.serialization.*

@Serializable
data class EndPointData(
    var showCase:List<String> = listOf("https://robot-instinct.github.io/app/images/showcase/ri_0.jpeg"),
)