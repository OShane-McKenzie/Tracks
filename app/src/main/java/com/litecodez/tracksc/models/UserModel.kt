package com.litecodez.tracksc.models

import com.litecodez.tracksc.objects.HasId

data class UserModel(
    override var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var profileImage: String = "",
    var isVerified: Boolean = false,
    var isFirstTimeLogin: Boolean = true,
    var tag:String = ""
):HasId