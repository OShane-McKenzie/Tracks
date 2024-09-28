package com.litecodez.tracksc.models

import com.litecodez.tracksc.objects.HasId

data class TagsModel(
    override var id:String = "",
    var userId:String = "",
    var name:String = "",
    var type:String = "",
    var photoUrl:String = "",
):HasId
