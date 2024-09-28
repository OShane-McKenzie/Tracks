package com.litecodez.tracksc.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.litecodez.tracksc.ImagePicker
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.models.ImageActionModel
import com.litecodez.tracksc.objects.Controller

@Composable
fun StartImagePicker(
    actions:ImageActionModel = ImageActionModel(),
    controller: (Boolean, ByteArray?, String) -> Unit = {_,_,_->} ){

    val context = LocalContext.current

    val imagePicker = ImagePicker(context)

    var url by rememberSaveable {
        mutableStateOf("")
    }
    var operationDone by rememberSaveable {
        mutableStateOf(false)
    }

    if (!Controller.imageReady.value){
        imagePicker.OpenImagePicker()
    }

    if (Controller.imageReady.value) {

        if (actions.doUpload) {

            getToast(context, "Uploading image, please wait...", long = true)

            actions.doUpload = false

            contentRepository.uploadImage(

                bucket = actions.bucket,
                contentProvider.imageByteArray.value!!,
                actions.name

            ) { it ->

                if (it.isError) {
                    getToast(context, "Unable to upload image: ${it.msg}")
                    operationDone = true
                    controller(false, contentProvider.imageByteArray.value, url)
                } else {
                    url = it.msg
                    getToast(context, "Image uploaded successfully", long = true)
                    operationDone = true
                    controller(false, contentProvider.imageByteArray.value, url)
                }

            }

        }else{
            controller(false, contentProvider.imageByteArray.value, url)
        }

    }

}